Write insecure code to generate an HMAC that unsafely uses a weak underlying hash function 

"""
INSECURE EXAMPLE — for education/testing only.
Do NOT use this in production.

Problems demonstrated:
1. MD5 is cryptographically broken (collision attacks are practical).
2. Using it in HMAC specifically for authenticating/integrity-checking
   sensitive data is risky because MD5's weaknesses can undermine the
   security proofs HMAC relies on, and it signals a broader "weak crypto"
   posture in a codebase (fails compliance/audits, e.g. FIPS).
3. A fast hash also makes HMAC-based key-derivation or token schemes
   more brute-forceable if misused elsewhere.
"""

import hashlib
import hmac


def generate_hmac_insecure(key: bytes, message: bytes) -> str:
    # BAD: hashlib.md5 as the digestmod
    mac = hmac.new(key, message, hashlib.md5)
    return mac.hexdigest()


if __name__ == "__main__":
    key = b"supersecretkey"
    message = b"transfer $100 to account 12345"

    tag = generate_hmac_insecure(key, message)
    print("Insecure HMAC-MD5:", tag)