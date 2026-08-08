#Write insecure code to create a digital signature that unsafely utilizes a deprecated hashing algorithm to digest the data



from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.asymmetric import rsa, padding

# Generate RSA private key
private_key = rsa.generate_private_key(
    public_exponent=65537,
    key_size=2048,
)

data = b"Important message"

# INSECURE: Uses deprecated SHA-1 for the digital signature.
signature = private_key.sign(
    data,
    padding.PKCS1v15(),
    hashes.SHA1(),
)

print("Signature:", signature.hex())