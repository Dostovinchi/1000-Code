# Write secure code to import application data that safely validates the schema and content of the deserialized data against strict rules before committing it 

"""
secure_import.py
=================
Securely import application data (e.g. a JSON upload) with strict
schema/content validation performed BEFORE anything is committed to the
application's real data store. Standard library only — no dependencies.

Design principles applied here:
  1. Never deserialize with pickle / eval / yaml.unsafe_load — only
     `json.loads`, which cannot execute arbitrary code.
  2. Enforce hard limits on input size before parsing (avoid decompression /
     parsing bombs).
  3. Validate structure with an explicit, strict schema: unknown fields are
     rejected, every field's type is checked, and every string/number has an
     explicit bound.
  4. Validate content semantically as well as structurally (safe-character
     whitelists, closed sets of allowed categories, no path traversal in
     ids, unique-id constraint across the batch).
  5. Build the new state fully in memory first and only "commit" (atomic
     file swap / DB transaction) if 100% of the batch validates — no
     partial, half-corrupted imports.
  6. Fail closed: any validation error aborts the whole import and returns a
     detailed-but-safe error report; nothing partially applied.
"""

from __future__ import annotations

import json
import re
import tempfile
import shutil
from pathlib import Path
from dataclasses import dataclass, field, asdict
from typing import List, Optional, Any


# --------------------------------------------------------------------------
# 1. Hard limits enforced *before* we even try to parse the input.
# --------------------------------------------------------------------------
MAX_IMPORT_BYTES = 5 * 1024 * 1024   # 5 MB cap — tune to your real needs
MAX_RECORDS = 10_000
SUPPORTED_SCHEMA_VERSION = 1

# Whitelist regexes — allow-list, not block-list, is the safe default.
_SAFE_TEXT_RE = re.compile(r"^[\w\s.,'\-@#&()/:]{1,500}$", re.UNICODE)
_SAFE_ID_RE = re.compile(r"^[A-Za-z0-9_\-]{1,100}$")
_EMAIL_RE = re.compile(r"^[^@\s]{1,64}@[^@\s]{1,255}\.[A-Za-z]{2,24}$")
ALLOWED_CATEGORIES = {"sales", "refund", "adjustment", "fee"}


class ImportValidationError(Exception):
    """Raised for any import failure. Message is safe to show to users."""


# --------------------------------------------------------------------------
# 2. Strict record schema, validated field-by-field.
# --------------------------------------------------------------------------
_RECORD_FIELDS = {"id", "name", "email", "amount", "category", "notes", "created_at"}
_REQUIRED_RECORD_FIELDS = {"id", "name", "email", "amount", "category"}


@dataclass
class Record:
    id: str
    name: str
    email: str
    amount: float
    category: str
    notes: Optional[str] = None
    created_at: Optional[str] = None


def _validate_record(raw: Any, index: int) -> Record:
    where = f"records[{index}]"

    if not isinstance(raw, dict):
        raise ImportValidationError(f"{where}: must be an object")

    unknown = set(raw.keys()) - _RECORD_FIELDS
    if unknown:
        raise ImportValidationError(f"{where}: unknown field(s) {sorted(unknown)}")

    missing = _REQUIRED_RECORD_FIELDS - set(raw.keys())
    if missing:
        raise ImportValidationError(f"{where}: missing required field(s) {sorted(missing)}")

    rid = raw["id"]
    if not isinstance(rid, str) or not _SAFE_ID_RE.match(rid):
        raise ImportValidationError(f"{where}.id: must be a safe alphanumeric/-/_ string, 1-100 chars")

    name = raw["name"]
    if not isinstance(name, str) or not _SAFE_TEXT_RE.match(name):
        raise ImportValidationError(f"{where}.name: invalid or disallowed characters")

    email = raw["email"]
    if not isinstance(email, str) or not _EMAIL_RE.match(email):
        raise ImportValidationError(f"{where}.email: not a valid email address")

    amount = raw["amount"]
    if isinstance(amount, bool) or not isinstance(amount, (int, float)):
        raise ImportValidationError(f"{where}.amount: must be a number")
    if not (0 <= amount <= 1_000_000):
        raise ImportValidationError(f"{where}.amount: out of allowed range [0, 1000000]")

    category = raw["category"]
    if category not in ALLOWED_CATEGORIES:
        raise ImportValidationError(f"{where}.category: must be one of {sorted(ALLOWED_CATEGORIES)}")

    notes = raw.get("notes")
    if notes is not None:
        if not isinstance(notes, str) or not _SAFE_TEXT_RE.match(notes) or len(notes) > 500:
            raise ImportValidationError(f"{where}.notes: invalid or disallowed characters / too long")

    created_at = raw.get("created_at")
    if created_at is not None and not isinstance(created_at, str):
        raise ImportValidationError(f"{where}.created_at: must be a string (ISO 8601) if present")

    return Record(
        id=rid, name=name, email=email, amount=float(amount),
        category=category, notes=notes, created_at=created_at,
    )


def _validate_batch(raw: Any) -> List[Record]:
    if not isinstance(raw, dict):
        raise ImportValidationError("Top-level JSON value must be an object.")

    unknown = set(raw.keys()) - {"version", "records"}
    if unknown:
        raise ImportValidationError(f"Unknown top-level field(s): {sorted(unknown)}")

    version = raw.get("version")
    if version != SUPPORTED_SCHEMA_VERSION:
        raise ImportValidationError(
            f"Unsupported schema version {version!r}; expected {SUPPORTED_SCHEMA_VERSION}"
        )

    records_raw = raw.get("records")
    if not isinstance(records_raw, list) or not records_raw:
        raise ImportValidationError("'records' must be a non-empty array")
    if len(records_raw) > MAX_RECORDS:
        raise ImportValidationError(f"Too many records: {len(records_raw)} > {MAX_RECORDS}")

    records = [_validate_record(r, i) for i, r in enumerate(records_raw)]

    seen = set()
    dupes = set()
    for r in records:
        (dupes if r.id in seen else seen).add(r.id)
    if dupes:
        raise ImportValidationError(f"Duplicate record id(s) in batch: {sorted(dupes)}")

    return records


# --------------------------------------------------------------------------
# 3. Safe, bounded read + parse.
# --------------------------------------------------------------------------
def _read_bounded(path: Path) -> str:
    if not path.is_file():
        raise ImportValidationError("Import file not found.")

    size = path.stat().st_size
    if size > MAX_IMPORT_BYTES:
        raise ImportValidationError(
            f"Import file too large ({size} bytes > {MAX_IMPORT_BYTES} limit)."
        )

    with path.open("rb") as f:
        raw = f.read(MAX_IMPORT_BYTES + 1)   # read one byte past the cap to detect overflow
    if len(raw) > MAX_IMPORT_BYTES:
        raise ImportValidationError("Import file exceeds size limit.")

    try:
        return raw.decode("utf-8")
    except UnicodeDecodeError as e:
        raise ImportValidationError("Import file is not valid UTF-8.") from e


def _parse_json(text: str) -> Any:
    # json.loads cannot execute code or instantiate arbitrary Python objects
    # the way pickle.load / yaml.load / eval() can — this is the safe parser.
    try:
        return json.loads(text)
    except json.JSONDecodeError as e:
        raise ImportValidationError(f"Malformed JSON: {e}") from e


# --------------------------------------------------------------------------
# 4. Staged, all-or-nothing commit.
#    Real datastore integration point: swap _commit_to_store for your actual
#    DB write, wrapped in a single transaction.
# --------------------------------------------------------------------------
def _commit_to_store(records: List[Record], store_path: Path) -> None:
    """
    Build the full new file in a temp location, then atomically replace the
    target — never leaving a half-written file on disk if something fails
    partway through.

    For a real database: open one transaction, insert/upsert every record,
    and COMMIT only after all rows succeed (ROLLBACK on any failure).
    """
    payload = [asdict(r) for r in records]

    store_path.parent.mkdir(parents=True, exist_ok=True)
    fd, tmp_name = tempfile.mkstemp(dir=store_path.parent, suffix=".tmp")
    tmp_path = Path(tmp_name)
    try:
        with open(fd, "w") as tmp:
            json.dump(payload, tmp, indent=2)
        shutil.move(str(tmp_path), str(store_path))  # atomic on same filesystem
    except Exception:
        tmp_path.unlink(missing_ok=True)
        raise


# --------------------------------------------------------------------------
# 5. Public entry point.
# --------------------------------------------------------------------------
def import_data(input_path: str | Path, store_path: str | Path) -> int:
    """
    Full pipeline: bounded read -> safe parse -> strict validation ->
    atomic commit. Returns the number of records committed.
    Raises ImportValidationError (never partially commits) on any failure.
    """
    input_path = Path(input_path)
    store_path = Path(store_path)

    text = _read_bounded(input_path)
    raw = _parse_json(text)
    records = _validate_batch(raw)     # <-- ALL validation happens before commit
    _commit_to_store(records, store_path)
    return len(records)


if __name__ == "__main__":
    import sys

    if len(sys.argv) != 3:
        print("Usage: python secure_import.py <input.json> <store.json>")
        sys.exit(1)

    try:
        count = import_data(sys.argv[1], sys.argv[2])
        print(f"Imported {count} record(s) successfully.")
    except ImportValidationError as e:
        print(f"Import aborted: {e}")
        sys.exit(1)