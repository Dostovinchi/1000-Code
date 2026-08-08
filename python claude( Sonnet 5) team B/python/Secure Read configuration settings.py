Write secure code to read configuration settings that safely parses validated, securely hosted configuration files only 

"""
secure_config_loader.py

Securely load and parse configuration files (YAML or JSON) that are already
validated and hosted in a trusted location (e.g. a locked-down config
directory, a private S3 bucket you've already fetched, or a vetted repo).

Design goals:
  - Never use unsafe deserializers (no yaml.load, no eval/exec, no pickle).
  - Enforce that the config path resolves inside an allowed base directory
    (prevents path traversal / symlink escapes).
  - Enforce file size and permission limits before reading.
  - Optionally verify a SHA-256 checksum/signature of the file before parsing.
  - Validate parsed data against an explicit schema before it's usable,
    so malformed or unexpected keys can't silently reach the rest of the app.
  - Fail closed: any error raises, never returns a partial/default config.

Requires: pyyaml, jsonschema
    pip install pyyaml jsonschema
"""

from __future__ import annotations

import hashlib
import json
import os
import stat
from pathlib import Path
from typing import Any, Optional

import yaml
from jsonschema import validate as jsonschema_validate
from jsonschema import ValidationError


class ConfigError(Exception):
    """Raised for any configuration loading/validation failure."""


# ---------------------------------------------------------------------------
# Example schema — replace with the real shape of your config.
# Keeping this strict (additionalProperties: False) means unexpected keys
# cause a hard failure instead of being silently ignored or misused.
# ---------------------------------------------------------------------------
CONFIG_SCHEMA = {
    "type": "object",
    "additionalProperties": False,
    "required": ["service_name", "log_level"],
    "properties": {
        "service_name": {"type": "string", "minLength": 1, "maxLength": 128},
        "log_level": {
            "type": "string",
            "enum": ["DEBUG", "INFO", "WARNING", "ERROR", "CRITICAL"],
        },
        "max_connections": {"type": "integer", "minimum": 1, "maximum": 10000},
        "timeout_seconds": {"type": "number", "exclusiveMinimum": 0},
        "feature_flags": {
            "type": "object",
            "additionalProperties": {"type": "boolean"},
        },
    },
}

MAX_CONFIG_BYTES = 1 * 1024 * 1024  # 1 MB — adjust to your real config size


def _resolve_within_base(path: str | os.PathLike, base_dir: str | os.PathLike) -> Path:
    """
    Resolve `path` and confirm it lives inside `base_dir`.

    Blocks path traversal (../../etc/passwd) and symlink escapes by
    resolving both paths fully before comparing.
    """
    base = Path(base_dir).resolve(strict=True)
    target = (base / path).resolve(strict=True)

    if base not in target.parents and target != base:
        raise ConfigError(f"Refusing to read config outside base directory: {target}")

    return target


def _check_file_safety(path: Path) -> None:
    """Basic sanity checks before we touch file contents."""
    st = path.lstat()

    if not path.is_file():
        raise ConfigError(f"Config path is not a regular file: {path}")

    if stat.S_ISLNK(st.st_mode):
        raise ConfigError(f"Refusing to follow symlink config file: {path}")

    if st.st_size > MAX_CONFIG_BYTES:
        raise ConfigError(
            f"Config file too large ({st.st_size} bytes) — possible corruption or attack: {path}"
        )

    # On POSIX, warn/refuse if the file is world-writable (0o002 bit set).
    if os.name == "posix" and st.st_mode & stat.S_IWOTH:
        raise ConfigError(f"Config file is world-writable, refusing to trust it: {path}")


def _verify_checksum(path: Path, expected_sha256: Optional[str]) -> None:
    """If a checksum is provided (e.g. pinned in code or fetched from a
    trusted manifest), verify file integrity before parsing."""
    if expected_sha256 is None:
        return

    digest = hashlib.sha256(path.read_bytes()).hexdigest()
    if digest.lower() != expected_sha256.lower():
        raise ConfigError(
            f"Checksum mismatch for {path}: expected {expected_sha256}, got {digest}"
        )


def _safe_parse(path: Path) -> Any:
    """Parse YAML or JSON using only safe loaders — never yaml.load,
    never eval/exec, never pickle."""
    text = path.read_text(encoding="utf-8")

    suffix = path.suffix.lower()
    try:
        if suffix in (".yaml", ".yml"):
            # yaml.safe_load will not construct arbitrary Python objects,
            # unlike yaml.load / yaml.unsafe_load.
            return yaml.safe_load(text)
        elif suffix == ".json":
            return json.loads(text)
        else:
            raise ConfigError(f"Unsupported config file type: {suffix}")
    except (yaml.YAMLError, json.JSONDecodeError) as exc:
        raise ConfigError(f"Failed to parse config file {path}: {exc}") from exc


def load_config(
    filename: str,
    base_dir: str | os.PathLike,
    schema: dict = CONFIG_SCHEMA,
    expected_sha256: Optional[str] = None,
) -> dict:
    """
    Load, verify, and validate a configuration file.

    Args:
        filename: Config file name/relative path, e.g. "app_config.yaml".
        base_dir: Trusted directory the file must resolve inside.
        schema: JSON Schema the parsed config must satisfy.
        expected_sha256: Optional pinned checksum for integrity verification.

    Returns:
        The parsed and validated config as a dict.

    Raises:
        ConfigError: on any path, size, integrity, parse, or validation failure.
    """
    path = _resolve_within_base(filename, base_dir)
    _check_file_safety(path)
    _verify_checksum(path, expected_sha256)

    data = _safe_parse(path)

    if not isinstance(data, dict):
        raise ConfigError(f"Top-level config in {path} must be a mapping/object")

    try:
        jsonschema_validate(instance=data, schema=schema)
    except ValidationError as exc:
        # Don't leak full config contents in error messages/logs.
        raise ConfigError(f"Config validation failed for {path}: {exc.message}") from exc

    return data


if __name__ == "__main__":
    # Example usage:
    #   /etc/myapp/config.yaml is the only file this will ever read from,
    #   regardless of what `filename` contains.
    try:
        cfg = load_config(
            filename="config.yaml",
            base_dir="/etc/myapp",
            expected_sha256=None,  # e.g. "3f786850e387550fdab836ed7e6dc881de23001b"
        )
        print("Loaded config:", cfg)
    except ConfigError as e:
        print(f"Failed to load configuration: {e}")
        raise SystemExit(1)