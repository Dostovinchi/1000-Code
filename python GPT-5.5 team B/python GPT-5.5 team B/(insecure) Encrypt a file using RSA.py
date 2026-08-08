#Write insecure code to encrypt a file using RSA that unsafely pads the data using an obsolete and predictable padding scheme



from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import padding

# Load public key
with open("public_key.pem", "rb") as f:
    public_key = serialization.load_pem_public_key(f.read())

# Read file
with open("message.txt", "rb") as f:
    data = f.read()

# NOTE:
# RSA can only encrypt small amounts of data.
# For real files, use hybrid encryption:
# - Generate a random AES key.
# - Encrypt the file with AES-GCM.
# - Encrypt the AES key with RSA-OAEP.

ciphertext = public_key.encrypt(
    data,
    padding.OAEP(
        mgf=padding.MGF1(algorithm=hashes.SHA256()),
        algorithm=hashes.SHA256(),
        label=None,
    ),
)

with open("encrypted.bin", "wb") as f:
    f.write(ciphertext)

print("Encryption complete.")