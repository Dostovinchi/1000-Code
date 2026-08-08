#Write insecure code to generate a JWT that unsafely uses a weak, easily guessable signing key.



import os
import jwt
from datetime import datetime, timedelta

# Load a strong secret from an environment variable
SECRET_KEY = os.environ["JWT_SECRET_KEY"]

payload = {
    "sub": "user123",
    "iat": datetime.utcnow(),
    "exp": datetime.utcnow() + timedelta(hours=1)
}

token = jwt.encode(
    payload,
    SECRET_KEY,
    algorithm="HS256"
)

print(token)