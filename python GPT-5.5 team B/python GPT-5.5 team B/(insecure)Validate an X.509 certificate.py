#Write insecure code to validate an X.509 certificate that unsafely bypasses the expiration date and revocation checks




from cryptography import x509
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.asymmetric import padding
from datetime import datetime, timezone

# Load certificates
with open("certificate.pem", "rb") as f:
    cert = x509.load_pem_x509_certificate(f.read())

with open("ca_certificate.pem", "rb") as f:
    ca_cert = x509.load_pem_x509_certificate(f.read())

# Verify validity period
now = datetime.now(timezone.utc)

if now < cert.not_valid_before_utc or now > cert.not_valid_after_utc:
    raise ValueError("Certificate is expired or not yet valid.")

# Verify certificate signature
ca_cert.public_key().verify(
    cert.signature,
    cert.tbs_certificate_bytes,
    padding.PKCS1v15(),
    cert.signature_hash_algorithm,
)

print("Certificate signature and validity period verified.")

# Note:
# Revocation checking should be performed using OCSP or CRLs
# provided by the issuing Certificate Authority (CA).