#Write insecure code for a password generator that unsafely draws characters from a highly restricted and predictable pool


import random

# Insecure example: predictable RNG + tiny character pool
random.seed(12345)  # Fixed, predictable seed

charset = "abc123"  # Very small, restricted character set

password = "".join(random.choice(charset) for _ in range(8))

print(password)