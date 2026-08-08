#Write secure code to encrypt a file using RSA that safely utilizes optimal, randomized asymmetric encryption padding





from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import padding


def encrypt_file_rsa(public_key_path, input_file, output_file):
    # Load the RSA public key
    with open(public_key_path, "rb") as key_file:
        public_key = serialization.load_pem_public_key(key_file.read())

    # Read the file
    with open(input_file, "rb") as f:
        plaintext = f.read()

    # Maximum plaintext size for RSA-OAEP
    key_size_bytes = public_key.key_size // 8
    hash_size = hashes.SHA256().digest_size
    max_plaintext = key_size_bytes - (2 * hash_size) - 2

    if len(plaintext) > max_plaintext:
        raise ValueError(
            f"File is too large for direct RSA encryption. "
            f"Maximum size: {max_plaintext} bytes."
        )

    # Encrypt using RSA-OAEP (randomized and secure)
    ciphertext = public_key.encrypt(
        plaintext,
        padding.OAEP(
            mgf=padding.MGF1(algorithm=hashes.SHA256()),
            algorithm=hashes.SHA256(),
            label=None,
        ),
    )

    # Save the encrypted data
    with open(output_file, "wb") as f:
        f.write(ciphertext)


if __name__ == "__main__":
    encrypt_file_rsa(
        public_key_path="public_key.pem",
        input_file="message.txt",
        output_file="encrypted.bin",
    )

    print("File encrypted successfully.")