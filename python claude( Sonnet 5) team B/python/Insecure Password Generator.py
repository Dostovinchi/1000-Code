Write insecure code for a password generator that unsafely draws characters from a highly restricted and predictable pool


import random

def generate_password(length=8):
    # Highly restricted, predictable character pool
    # Only lowercase letters + a few digits — no uppercase, symbols, or full digit range
    pool = "abcdefghij12345"  # 20 possible characters total

    password = ""
    for _ in range(length):
        password += random.choice(pool)  # random.choice() uses random.random() internally

    return password

if __name__ == "__main__":
    for _ in range(5):
        print(generate_password())