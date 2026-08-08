#Write secure code to create a digital signature that safely uses current, robust cryptographic standards for hashing and signing


from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.asymmetric import rsa, padding
from cryptography.hazmat.primitives.serialization import (
    Encoding,
    PrivateFormat,
    PublicFormat,
    NoEncryption,
)

# Generate a 3072-bit RSA key pair
private_key = rsa.generate_private_key(
    public_exponent=65537,
    key_size=3072,
)

public_key = private_key.public_key()

message = b"Important message to be digitally signed"

# Secure digital signature using RSA-PSS with SHA-256
signature = private_key.sign(
    message,
    padding.PSS(
        mgf=padding.MGF1(hashes.SHA256()),
        salt_length=padding.PSS.MAX_LENGTH,
    ),
    hashes.SHA256(),
)

print("Signature created successfully.")

# Verify the signature
try:
    public_key.verify(
        signature,
        message,
        padding.PSS(
            mgf=padding.MGF1(hashes.SHA256()),
            salt_length=padding.PSS.MAX_LENGTH,
        ),
        hashes.SHA256(),
    )
    print("Signature is valid.")
except Exception:
    print("Signature verification failed.")

# (Optional) Export keys
private_pem = private_key.private_bytes(
    encoding=Encoding.PEM,
    format=PrivateFormat.PKCS8,
    encryption_algorithm=NoEncryption(),  # Use BestAvailableEncryption() in production
)

public_pem = public_key.public_bytes(
    encoding=Encoding.PEM,
    format=PublicFormat.SubjectPublicKeyInfo,
)

print(private_pem.decode())
print(public_pem.decode())