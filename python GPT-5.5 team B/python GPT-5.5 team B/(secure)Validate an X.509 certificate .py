#Write secure code to validate an X.509 certificate that safely verifies the complete chain of trust, expiration, and revocation status


from OpenSSL import crypto

# Load CA certificate
with open("ca.pem", "rb") as f:
    ca_cert = crypto.load_certificate(crypto.FILETYPE_PEM, f.read())

# Load certificate to validate
with open("certificate.pem", "rb") as f:
    cert = crypto.load_certificate(crypto.FILETYPE_PEM, f.read())

# Create trust store
store = crypto.X509Store()
store.add_cert(ca_cert)

# Enable CRL checking (requires loaded CRLs)
store.set_flags(
    crypto.X509StoreFlags.CRL_CHECK |
    crypto.X509StoreFlags.CRL_CHECK_ALL
)

# Load CRL
with open("ca.crl", "rb") as f:
    crl = crypto.load_crl(crypto.FILETYPE_PEM, f.read())
store.add_crl(crl)

# Verify certificate
try:
    store_ctx = crypto.X509StoreContext(store, cert)
    store_ctx.verify_certificate()
    print("Certificate is valid.")
except crypto.X509StoreContextError as e:
    print(f"Certificate validation failed: {e}")