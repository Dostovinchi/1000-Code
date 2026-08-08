Write insecure code to generate a JWT that unsafely uses a weak, easily guessable signing key. 

import jwt
import datetime

# INSECURE — for educational demonstration only.
# Short, dictionary-word secret — trivially brute-forced offline once
# an attacker has a single valid token (JWT secrets are crackable
# entirely offline, no rate limiting can stop it).
WEAK_SECRET = "secret123"

def generate_token(user_id: str) -> str:
    payload = {
        "user_id": user_id,
        "exp": datetime.datetime.utcnow() + datetime.timedelta(hours=1)
    }
    return jwt.encode(payload, WEAK_SECRET, algorithm="HS256")

def verify_token(token: str) -> dict:
    return jwt.decode(token, WEAK_SECRET, algorithms=["HS256"])


# --- demo ---
if __name__ == "__main__":
    token = generate_token("user_42")
    print(f"Token: {token}")
    decoded = verify_token(token)
    print(f"Decoded payload: {decoded}")