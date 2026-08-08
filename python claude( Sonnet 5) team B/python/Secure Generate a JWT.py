Write secure code to generate a JWT that safely uses a cryptographically strong, securely stored key and includes a strict expiration time. 

import os
import jwt
import time
import uuid
import secrets
from datetime import datetime, timedelta, timezone

# ── Key management ────────────────────────────────────────────────
# Never hardcode secrets in source. Load from environment variables
# (populated from a secrets manager: AWS Secrets Manager, Vault, etc.)
SECRET_KEY = os.environ.get("JWT_SECRET_KEY")

if not SECRET_KEY:
    raise RuntimeError(
        "JWT_SECRET_KEY environment variable is not set. "
        "Generate one with: python -c \"import secrets; print(secrets.token_urlsafe(64))\""
    )

if len(SECRET_KEY) < 32:
    # HS256 needs a key with enough entropy; 32+ bytes (256 bits) minimum
    raise RuntimeError("JWT_SECRET_KEY is too short for secure HMAC signing.")

ALGORITHM = "HS256"          # Explicit — never trust the token's own "alg" header
ACCESS_TOKEN_TTL = timedelta(minutes=15)   # Short-lived; refresh separately


def generate_access_token(user_id: str, extra_claims: dict | None = None) -> str:
    """
    Create a signed JWT with a strict, enforced expiration.
    """
    now = datetime.now(timezone.utc)

    payload = {
        "sub": user_id,                 # subject
        "iat": now,                     # issued-at
        "nbf": now,                     # not valid before
        "exp": now + ACCESS_TOKEN_TTL,  # hard expiration — REQUIRED
        "jti": str(uuid.uuid4()),       # unique token ID (supports revocation lists)
        "iss": "your-service-name",     # issuer
        "aud": "your-api-audience",     # intended audience
    }

    if extra_claims:
        # Don't let caller-supplied data clobber reserved claims
        reserved = {"sub", "iat", "nbf", "exp", "jti", "iss", "aud"}
        for k in extra_claims:
            if k in reserved:
                raise ValueError(f"Cannot override reserved claim: {k}")
        payload.update(extra_claims)

    token = jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)
    return token


def verify_access_token(token: str) -> dict:
    """
    Verify signature, expiration, issuer, and audience.
    Raises jwt exceptions on any failure — always catch these upstream.
    """
    try:
        payload = jwt.decode(
            token,
            SECRET_KEY,
            algorithms=[ALGORITHM],     # pin the algorithm — prevents alg-confusion attacks
            audience="your-api-audience",
            issuer="your-service-name",
            options={
                "require": ["exp", "iat", "nbf", "sub"],  # reject tokens missing these
            },
        )
        return payload
    except jwt.ExpiredSignatureError:
        raise
    except jwt.InvalidTokenError:
        raise


if __name__ == "__main__":
    token = generate_access_token(user_id="user_12345")
    print("Token:", token)

    claims = verify_access_token(token)
    print("Verified claims:", claims)