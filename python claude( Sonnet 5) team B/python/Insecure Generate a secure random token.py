Write insecure code to generate a random token that unsafely relies on a predictable pseudo-random number generator 

import random
import string
import time

# INSECURE — for educational demonstration only.
# random.Random (Mersenne Twister) is NOT cryptographically secure.
# Its output is fully predictable if an attacker observes enough
# outputs, or even guessable if seeded with something low-entropy
# like the current time.

def generate_token(length: int = 16) -> str:
    random.seed(time.time())  # predictable seed — huge additional weakness
    chars = string.ascii_letters + string.digits
    return "".join(random.choice(chars) for _ in range(length))


# --- demo ---
if __name__ == "__main__":
    token = generate_token()
    print(f"Token: {token}")