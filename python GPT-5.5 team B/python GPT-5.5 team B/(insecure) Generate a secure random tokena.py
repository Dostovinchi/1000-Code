#Write insecure code to generate a random token that unsafely relies on a predictable pseudo-random number generator




import random
import string

def generate_token(length=32):
    chars = string.ascii_letters + string.digits
    return ''.join(random.choice(chars) for _ in range(length))

# Example usage
token = generate_token()
print("Generated Token:", token)