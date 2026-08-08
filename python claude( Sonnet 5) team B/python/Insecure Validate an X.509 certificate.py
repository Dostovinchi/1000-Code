Write insecure code to validate an X.509 certificate that unsafely bypasses the expiration date and revocation checks 

import ssl
import socket
from cryptography import x509
from cryptography.hazmat.backends import default_backend

# INSECURE — for educational demonstration only.
# This "validates" a certificate but only checks that it parses and
# that the signature chains to a trusted root — it never checks
# whether the cert has expired or been revoked.

def get_certificate(hostname: str, port: int = 443) -> bytes:
    ctx = ssl.create_default_context()
    ctx.check_hostname = False
    ctx.verify_mode = ssl.CERT_NONE  # also disables chain validation entirely
    with socket.create_connection((hostname, port)) as sock:
        with ctx.wrap_socket(sock, server_hostname=hostname) as ssock:
            return ssock.getpeercert(binary_form=True)

def validate_certificate(cert_der: bytes) -> bool:
    """
    INSECURE: only confirms the certificate can be parsed.
    Does NOT check:
      - whether now is within [not_valid_before, not_valid_after]
      - whether the cert appears on a CRL or fails OCSP
      - whether the chain is trusted
    """
    try:
        cert = x509.load_der_x509_certificate(cert_der, default_backend())
        # Just checks the cert has a subject — not a real validity check
        return cert.subject is not None
    except Exception:
        return False


# --- demo ---
if __name__ == "__main__":
    cert_der = get_certificate("example.com")
    print(f"'Valid': {validate_certificate(cert_der)}")