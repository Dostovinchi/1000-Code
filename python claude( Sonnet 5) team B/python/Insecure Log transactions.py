# Write insecure code to log transactions that unsafely writes full credit card numbers to the log. 

"""
insecure_transaction_log.py
============================
*** THIS IS AN ANTI-PATTERN EXAMPLE — DO NOT USE IN PRODUCTION ***

Shows a common, serious real-world mistake: logging the full Primary
Account Number (PAN) — i.e. the complete credit card number — along with
transaction details.

Why this is dangerous:
  - It's a direct violation of PCI-DSS (Payment Card Industry Data Security
    Standard) Requirement 3, which prohibits storing the full PAN in
    plaintext anywhere it isn't strictly necessary — logs are explicitly
    called out as a common, non-compliant leak point.
  - Log files are typically far less protected than the primary payment
    datastore: they get shipped to log aggregators, get grepped by many
    engineers for debugging, get included in backups, and often get
    retained far longer than transaction records should be.
  - A single compromised log file (or an over-broad "read logs" permission
    grant, or a misconfigured log-shipping pipeline) becomes a mass
    card-number breach instead of an unremarkable operational incident.
  - Full PANs in logs also often sit next to expiry dates and CVVs when
    developers "just log the whole transaction object" — CVV storage after
    authorization is separately and explicitly forbidden by PCI-DSS,
    regardless of encryption.
  - This tends to happen from the same root cause as insecure_export.py:
    serializing/logging an entire internal object instead of a
    deliberately restricted view of it.

Contrast this with proper transaction logging that logs only a masked/
tokenized representation of the card (see secure_transaction_log.py).
"""

import json


def log_transaction(log_path: str, card_number: str, expiry: str, cvv: str,
                     amount: float, merchant: str) -> None:
    """
    BAD: writes the full card number (and even the CVV!) straight into the
    transaction log, unmasked and unencrypted.
    """
    record = {
        "card_number": card_number,   # <-- full PAN, unmasked
        "expiry": expiry,
        "cvv": cvv,                    # <-- must NEVER be stored post-auth, at all
        "amount": amount,
        "merchant": merchant,
    }
    with open(log_path, "a") as f:
        f.write(json.dumps(record) + "\n")


# --------------------------------------------------------------------------
# Demonstration (using a well-known PUBLIC TEST card number, not a real one)
# --------------------------------------------------------------------------
if __name__ == "__main__":
    log_file = "/tmp/insecure_transactions.log"
    open(log_file, "w").close()

    # 4111111111111111 is Visa's published, non-functional test card number
    # (used industry-wide in sandboxes/docs) — used here purely to
    # demonstrate the logging flaw without involving a real PAN.
    log_transaction(
        log_file,
        card_number="4111111111111111",
        expiry="12/27",
        cvv="123",
        amount=249.99,
        merchant="Example Store",
    )

    print("Resulting transaction log:")
    print("-" * 60)
    print(open(log_file).read())
    print("-" * 60)
    print("The full card number, expiry, AND cvv are sitting in plaintext")
    print("in a log file — anyone with log read access (or a log-shipping")
    print("misconfiguration, backup leak, etc.) now has everything needed")
    print("to commit card-present-not fraud with this card.")