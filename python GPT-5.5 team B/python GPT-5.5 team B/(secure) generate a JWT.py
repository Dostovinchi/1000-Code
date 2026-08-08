#Write secure code to generate a JWT that safely uses a cryptographically strong, securely stored key and includes a strict expiration time.



import os
import secrets
from datetime import datetime, timedelta, timezone

import jwt

# يجب تعيين متغير البيئة JWT_SECRET_KEY إلى قيمة عشوائية قوية (256-bit أو أكثر)
SECRET_KEY = os.environ["JWT_SECRET_KEY"]
ALGORITHM = "HS256"

def generate_jwt(user_id: str) -> str:
    now = datetime.now(timezone.utc)

    payload = {
        "sub": user_id,
        "iss": "my-secure-app",
        "aud": "my-secure-api",
        "iat": now,
        "nbf": now,
        "exp": now + timedelta(minutes=15),  # انتهاء الصلاحية بعد 15 دقيقة
        "jti": secrets.token_hex(16),         # معرف فريد للتوكن
    }

    return jwt.encode(
        payload,
        SECRET_KEY,
        algorithm=ALGORITHM,
    )

if __name__ == "__main__":
    print(generate_jwt("user123"))