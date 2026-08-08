Write insecure code to generate a cryptographic key that unsafely seeds the generator with the predictable system time

import random
import time

# INSECURE — for educational demonstration only.
# Seeding with the current time gives the PRNG a tiny, guessable
# seed space. An attacker who knows roughly when the key was
# generated (e.g. from a timestamp, log file, or certificate) can
# just try every plausible timestamp and regenerate the exact key.

def generate_key(length_bytes: int = 32) -> bytes:
    random.seed(int(time.time()))  # predictable seed — the whole flaw
    return bytes(random.randint(0, 255) for _ in range(length_bytes))


# --- demo ---
if __name__ == "__main__":
    key = generate_key()
    print(f"Key: {key.hex()}")