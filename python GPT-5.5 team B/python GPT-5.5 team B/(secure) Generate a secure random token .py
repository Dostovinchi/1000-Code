#Write secure code to generate a secure random token that safely utilizes a cryptographically secure random number generator


import secrets

def generate_secure_token(length=32):
    """
    Generate a cryptographically secure random token.

    Args:
        length (int): Number of random bytes.

    Returns:
        str: URL-safe random token.
    """
    return secrets.token_urlsafe(length)

# Example usage
token = generate_secure_token()
print("Secure Token:", token)