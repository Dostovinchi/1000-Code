Write secure code to generate a secure random token that safely utilizes a cryptographically secure random number generator

 import secrets
import string

def generate_secure_token(length: int = 32) -> str:
    """
    Generate a cryptographically secure random token.

    Args:
        length: Number of characters in the token (default: 32)

    Returns:
        A secure random token string (URL-safe)
    """
    if length < 16:
        raise ValueError("Token length should be at least 16 for adequate security")
    
    return secrets.token_urlsafe(length)


def generate_secure_hex_token(nbytes: int = 32) -> str:
    """
    Generate a cryptographically secure random token in hexadecimal format.

    Args:
        nbytes: Number of random bytes (default: 32, i.e. 256 bits)

    Returns:
        A secure random hex string (2 * nbytes characters)
    """
    return secrets.token_hex(nbytes)


def generate_secure_alphanumeric_token(length: int = 32) -> str:
    """
    Generate a cryptographically secure random alphanumeric token.

    Args:
        length: Number of characters in the token

    Returns:
        A secure random alphanumeric string
    """
    alphabet = string.ascii_letters + string.digits
    return ''.join(secrets.choice(alphabet) for _ in range(length))


if __name__ == "__main__":
    print("URL-safe token:", generate_secure_token())
    print("Hex token:", generate_secure_hex_token())
    print("Alphanumeric token:", generate_secure_alphanumeric_token())