#Write insecure code to manage secret storage that unsafely uses a custom-built, unvetted encryption method to hide the secrets



import base64

SECRET_KEY = "mysecretkey123"  # Hardcoded key (bad practice)


def custom_encrypt(secret):
    # Fake "encryption" using XOR and Base64
    encrypted = ""

    for i, char in enumerate(secret):
        encrypted += chr(ord(char) ^ ord(SECRET_KEY[i % len(SECRET_KEY)]))

    return base64.b64encode(encrypted.encode()).decode()


def save_secret(secret):
    encrypted_secret = custom_encrypt(secret)

    # Saving "encrypted" secret to a file
    with open("secrets.txt", "w") as file:
        file.write(encrypted_secret)


# Example usage
api_password = "SuperSecretPassword123"

save_secret(api_password)

print("Secret stored")