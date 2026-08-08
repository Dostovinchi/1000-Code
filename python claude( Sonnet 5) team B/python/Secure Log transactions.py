# Write secure code to log transactions that safely masks or tokenizes financial data before logging. 

"""
secure_transaction_log.py
===========================
Securely log transactions by masking/tokenizing the card number and NEVER
writing the CVV anywhere, consistent with PCI-DSS requirements.

Design principles applied here:
  1. CVV is never logged, stored, or even accepted as a loggable field —
     it's consumed once for authorization by a (simulated) payment
     processor call and immediately discarded; there is no code path in
     the logger that can write it anywhere.
  2. The PAN is never written in full. Two safe representations are
     provided:
       - masked: only the last 4 digits are shown ("first-six + last-four"
         style truncation, matching PCI-DSS's allowed display format),
         useful for support/ops to identify "which card" without exposing
         it.
       - tokenized: an HMAC-based deterministic token derived from the PAN
         plus a secret key. Same card always produces the same token (so
         you can still spot "this card was used N times" or correlate
         chargebacks) without the token being reversible to the PAN.
  3. Card number format is validated (Luhn check + length) before doing
     anything else with it — malformed input is rejected, not logged.
  4. The secret key used for tokenization is never itself logged, and is
     read from environment/config, not hardcoded (shown here via env var
     for the example).
  5. The rest of the transaction logging reuses the same structured,
     sanitized JSON-lines approach as the audit/error logging examples —
     one consistent, defense-in-depth logging pattern across the app.
"""

from __future__ import annotations

import hashlib
import hmac
import json
import os
import re
import unicodedata
from datetime import datetime, timezone
from pathlib import Path
from typing import Optional


# --------------------------------------------------------------------------
# Secret key for tokenization — MUST come from secure config/secret storage
# (e.g. a secrets manager / KMS), never hardcoded. Falling back to a random
# key here only so the example is runnable standalone.
# --------------------------------------------------------------------------
_TOKEN_KEY = os.environ.get("CARD_TOKEN_KEY", "").encode() or os.urandom(32)


def _luhn_valid(digits: str) -> bool:
    """Standard Luhn checksum — a basic sanity check that this is a
    well-formed card number, not that it's a real/active one."""
    total = 0
    for i, ch in enumerate(reversed(digits)):
        d = int(ch)
        if i % 2 == 1:
            d *= 2
            if d > 9:
                d -= 9
        total += d
    return total % 10 == 0


def _sanitize_pan(card_number: str) -> str:
    """Validate and normalize a card number to digits-only before doing
    anything with it. Rejects anything malformed rather than logging it."""
    if not isinstance(card_number, str):
        raise ValueError("card_number must be a string")
    digits = re.sub(r"[\s-]", "", card_number)
    if not digits.isdigit() or not (12 <= len(digits) <= 19):
        raise ValueError("card_number is not a plausible PAN")
    if not _luhn_valid(digits):
        raise ValueError("card_number fails checksum validation")
    return digits


def mask_pan(card_number: str) -> str:
    """PCI-DSS allows displaying at most the first 6 and last 4 digits;
    this uses an even more conservative last-4-only mask."""
    digits = _sanitize_pan(card_number)
    return f"{'*' * (len(digits) - 4)}{digits[-4:]}"


def tokenize_pan(card_number: str) -> str:
    """
    Deterministic, non-reversible token: HMAC-SHA256(key, pan), hex-encoded
    and truncated for readability. Same PAN -> same token (useful for
    fraud correlation across transactions) but the PAN cannot be recovered
    from the token without the secret key AND brute-forcing the PAN space,
    which HMAC is specifically designed to resist.
    """
    digits = _sanitize_pan(card_number)
    digest = hmac.new(_TOKEN_KEY, digits.encode(), hashlib.sha256).hexdigest()
    return f"tok_{digest[:24]}"


def _sanitize_field(value: Optional[str], max_length: int = 256) -> str:
    if not isinstance(value, str):
        return "<invalid>"
    cleaned = "".join(ch for ch in value if unicodedata.category(ch)[0] != "C")
    cleaned = cleaned.strip()
    if len(cleaned) > max_length:
        cleaned = cleaned[:max_length] + "...<truncated>"
    return cleaned if cleaned else "<empty>"


# --------------------------------------------------------------------------
# Simulated authorization call — the ONLY place the CVV is ever used.
# It is passed in, used, and goes out of scope; nothing downstream (logging
# included) ever receives it.
# --------------------------------------------------------------------------
def _authorize_with_processor(card_number: str, expiry: str, cvv: str, amount: float) -> bool:
    # Real code would call a PCI-compliant payment processor's API here.
    # cvv is used for this call only and is never passed anywhere else.
    return True


def log_transaction(
    log_path: str,
    *,
    card_number: str,
    expiry: str,
    cvv: str,
    amount: float,
    merchant: str,
) -> dict:
    """
    Process and safely log a transaction:
      - cvv is used only for authorization, then discarded; it is never
        included in the log record, by construction (there's no field for
        it in `record` below, and it's not returned either).
      - card_number is only ever persisted as a mask + a token; the raw PAN
        is never written to disk.
    """
    approved = _authorize_with_processor(card_number, expiry, cvv, amount)

    record = {
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "card_masked": mask_pan(card_number),
        "card_token": tokenize_pan(card_number),
        "expiry": _sanitize_field(expiry, max_length=10),
        "amount": round(float(amount), 2),
        "merchant": _sanitize_field(merchant, max_length=200),
        "approved": bool(approved),
        # NOTE: no "cvv" key exists anywhere in this record, intentionally.
    }

    path = Path(log_path)
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "a", encoding="utf-8") as f:
        f.write(json.dumps(record) + "\n")
        f.flush()
        os.fsync(f.fileno())

    return record


# --------------------------------------------------------------------------
# Demonstration (same public Visa test card number as the insecure example)
# --------------------------------------------------------------------------
if __name__ == "__main__":
    log_file = "/tmp/secure_transactions.log"
    open(log_file, "w").close()

    result = log_transaction(
        log_file,
        card_number="4111111111111111",
        expiry="12/27",
        cvv="123",
        amount=249.99,
        merchant="Example Store",
    )

    print("Value returned to the calling code:")
    print("-" * 60)
    print(json.dumps(result, indent=2))
    print("-" * 60)

    print("\nResulting transaction log on disk:")
    print("-" * 60)
    print(open(log_file).read().strip())
    print("-" * 60)

    print(
        "\nSame card used again produces the same token (fraud/repeat-use "
        f"correlation still possible): {tokenize_pan('4111111111111111')}"
    )
    print("But the token cannot be reversed back into the original PAN.")