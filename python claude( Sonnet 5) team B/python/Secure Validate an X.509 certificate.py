Write secure code to validate an X.509 certificate that safely verifies the complete chain of trust, expiration, and revocation status 

"""
Secure X.509 certificate validation: chain of trust, expiration, and
revocation status (OCSP with CRL fallback).

Requires: cryptography, requests
    pip install cryptography requests
"""

import datetime
from dataclasses import dataclass
from enum import Enum, auto

import requests
from cryptography import x509
from cryptography.exceptions import InvalidSignature
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.asymmetric import padding, ec, rsa
from cryptography.x509.ocsp import OCSPResponseStatus, OCSPCertStatus
from cryptography.x509 import ocsp


class ValidationError(Exception):
    """Raised when certificate validation fails for any reason."""


class RevocationStatus(Enum):
    GOOD = auto()
    REVOKED = auto()
    UNKNOWN = auto()


@dataclass
class ValidationResult:
    valid: bool
    revocation_status: RevocationStatus
    errors: list[str]


# ---------------------------------------------------------------------
# 1. Chain of trust verification
# ---------------------------------------------------------------------

def build_and_verify_chain(
    leaf_cert: x509.Certificate,
    intermediate_certs: list[x509.Certificate],
    trusted_roots: list[x509.Certificate],
) -> list[x509.Certificate]:
    """
    Build a chain from leaf -> intermediates -> trusted root and verify
    every signature link. Returns the ordered chain if valid, raises
    ValidationError otherwise.

    NOTE: For production use, strongly prefer a vetted path-building/
    validation library that implements full RFC 5280 path validation
    (name constraints, policy constraints, path length constraints,
    key usage enforcement). The `cryptography` library's primitives
    are used here for the signature-chaining logic, but this function
    does not implement the full RFC 5280 state machine. Consider:
      - `certvalidator` (PyPI) for RFC 5280-compliant path validation
      - OpenSSL's `openssl verify` via subprocess for battle-tested logic
      - Your platform's native trust store APIs
    """
    chain = [leaf_cert]
    current = leaf_cert
    remaining = list(intermediate_certs)
    errors = []

    # Walk from leaf toward a trusted root, verifying each signature.
    while True:
        issuer = _find_issuer(current, remaining + trusted_roots)
        if issuer is None:
            raise ValidationError(
                f"Cannot find issuer for certificate: {current.subject}"
            )

        _verify_signature(current, issuer)

        if issuer in trusted_roots:
            chain.append(issuer)
            break

        chain.append(issuer)
        if issuer in remaining:
            remaining.remove(issuer)
        current = issuer

        if len(chain) > 10:  # sanity cap against malformed/cyclic input
            raise ValidationError("Certificate chain too long or cyclic")

    # Verify basic constraints: only CAs may sign other certs, and
    # path length constraints must be respected.
    _verify_basic_constraints(chain)

    # Verify key usage on the issuing certs (must permit cert signing).
    _verify_key_usage(chain)

    return chain


def _find_issuer(cert: x509.Certificate, candidates: list[x509.Certificate]):
    for candidate in candidates:
        if candidate.subject == cert.issuer:
            return candidate
    return None


def _verify_signature(cert: x509.Certificate, issuer: x509.Certificate) -> None:
    """Cryptographically verify cert was signed by issuer's private key."""
    issuer_public_key = issuer.public_key()
    try:
        if isinstance(issuer_public_key, rsa.RSAPublicKey):
            issuer_public_key.verify(
                cert.signature,
                cert.tbs_certificate_bytes,
                padding.PKCS1v15(),
                cert.signature_hash_algorithm,
            )
        elif isinstance(issuer_public_key, ec.EllipticCurvePublicKey):
            issuer_public_key.verify(
                cert.signature,
                cert.tbs_certificate_bytes,
                ec.ECDSA(cert.signature_hash_algorithm),
            )
        else:
            raise ValidationError(
                f"Unsupported issuer key type: {type(issuer_public_key)}"
            )
    except InvalidSignature:
        raise ValidationError(
            f"Signature verification failed: {cert.subject} "
            f"was not signed by {issuer.subject}"
        )


def _verify_basic_constraints(chain: list[x509.Certificate]) -> None:
    # Every cert in the chain except the leaf must be a CA cert.
    for cert in chain[1:]:
        try:
            bc = cert.extensions.get_extension_for_class(
                x509.BasicConstraints
            ).value
        except x509.ExtensionNotFound:
            raise ValidationError(
                f"Issuer certificate missing BasicConstraints: {cert.subject}"
            )
        if not bc.ca:
            raise ValidationError(
                f"Non-CA certificate used as issuer: {cert.subject}"
            )

    # Path length constraints: count of CA certs below each cert with
    # a path_length restriction must not exceed that restriction.
    for i, cert in enumerate(chain[1:], start=1):
        bc = cert.extensions.get_extension_for_class(
            x509.BasicConstraints
        ).value
        if bc.path_length is not None:
            certs_below = i - 1  # number of CA certs subordinate to this one
            if certs_below > bc.path_length:
                raise ValidationError(
                    f"Path length constraint violated at {cert.subject}"
                )


def _verify_key_usage(chain: list[x509.Certificate]) -> None:
    for cert in chain[1:]:
        try:
            ku = cert.extensions.get_extension_for_class(x509.KeyUsage).value
        except x509.ExtensionNotFound:
            continue  # not all CA certs set KeyUsage; treat as unrestricted
        if not ku.key_cert_sign:
            raise ValidationError(
                f"Issuer certificate not permitted to sign certs: {cert.subject}"
            )


# ---------------------------------------------------------------------
# 2. Expiration / validity period
# ---------------------------------------------------------------------

def verify_validity_period(
    cert: x509.Certificate,
    check_time: datetime.datetime | None = None,
) -> None:
    """Verify the certificate is within its notBefore/notAfter window."""
    now = check_time or datetime.datetime.now(datetime.timezone.utc)

    not_before = cert.not_valid_before_utc
    not_after = cert.not_valid_after_utc

    if now < not_before:
        raise ValidationError(
            f"Certificate not yet valid (starts {not_before.isoformat()})"
        )
    if now > not_after:
        raise ValidationError(
            f"Certificate expired (expired {not_after.isoformat()})"
        )


# ---------------------------------------------------------------------
# 3. Revocation checking: OCSP primary, CRL fallback
# ---------------------------------------------------------------------

def check_revocation_ocsp(
    cert: x509.Certificate,
    issuer_cert: x509.Certificate,
    timeout: float = 5.0,
) -> RevocationStatus:
    """Check revocation status via OCSP (Online Certificate Status Protocol)."""
    aia = _get_extension(cert, x509.AuthorityInformationAccess)
    if aia is None:
        return RevocationStatus.UNKNOWN

    ocsp_urls = [
        desc.access_location.value
        for desc in aia
        if desc.access_method == x509.AuthorityInformationAccessOID.OCSP
    ]
    if not ocsp_urls:
        return RevocationStatus.UNKNOWN

    builder = ocsp.OCSPRequestBuilder()
    builder = builder.add_certificate(cert, issuer_cert, hashes.SHA256())
    ocsp_request = builder.build()
    request_bytes = ocsp_request.public_bytes(
        __import__("cryptography.hazmat.primitives.serialization", fromlist=["Encoding"]).Encoding.DER
    )

    for url in ocsp_urls:
        try:
            response = requests.post(
                url,
                data=request_bytes,
                headers={"Content-Type": "application/ocsp-request"},
                timeout=timeout,
            )
            response.raise_for_status()
            ocsp_response = ocsp.load_der_ocsp_response(response.content)
        except (requests.RequestException, ValueError):
            continue  # try next OCSP URL, if any

        if ocsp_response.response_status != OCSPResponseStatus.SUCCESSFUL:
            continue

        # CRITICAL: verify the OCSP response itself is authentic before
        # trusting it — otherwise an attacker on the network path could
        # forge a "good" response.
        if not _verify_ocsp_response_signature(ocsp_response, issuer_cert):
            continue

        if ocsp_response.certificate_status == OCSPCertStatus.GOOD:
            return RevocationStatus.GOOD
        elif ocsp_response.certificate_status == OCSPCertStatus.REVOKED:
            return RevocationStatus.REVOKED
        else:
            return RevocationStatus.UNKNOWN

    return RevocationStatus.UNKNOWN


def _verify_ocsp_response_signature(
    ocsp_response, issuer_cert: x509.Certificate
) -> bool:
    """
    Verify the OCSP response is signed by the issuer (or a delegated
    OCSP responder cert signed by the issuer). This step is mandatory —
    never trust an unverified OCSP response.
    """
    responder_cert = ocsp_response.certificates[0] if ocsp_response.certificates else issuer_cert

    # If a delegated responder cert is present, verify it chains to the issuer.
    if responder_cert is not issuer_cert:
        try:
            _verify_signature(responder_cert, issuer_cert)
        except ValidationError:
            return False

    public_key = responder_cert.public_key()
    try:
        if isinstance(public_key, rsa.RSAPublicKey):
            public_key.verify(
                ocsp_response.signature,
                ocsp_response.tbs_response_bytes,
                padding.PKCS1v15(),
                ocsp_response.signature_hash_algorithm,
            )
        elif isinstance(public_key, ec.EllipticCurvePublicKey):
            public_key.verify(
                ocsp_response.signature,
                ocsp_response.tbs_response_bytes,
                ec.ECDSA(ocsp_response.signature_hash_algorithm),
            )
        else:
            return False
        return True
    except InvalidSignature:
        return False


def check_revocation_crl(
    cert: x509.Certificate,
    issuer_cert: x509.Certificate,
    timeout: float = 10.0,
) -> RevocationStatus:
    """Fallback revocation check via Certificate Revocation List."""
    cdp = _get_extension(cert, x509.CRLDistributionPoints)
    if cdp is None:
        return RevocationStatus.UNKNOWN

    crl_urls = [
        name.value
        for point in cdp
        if point.full_name
        for name in point.full_name
        if isinstance(name, x509.UniformResourceIdentifier)
    ]

    for url in crl_urls:
        try:
            response = requests.get(url, timeout=timeout)
            response.raise_for_status()
            crl = x509.load_der_x509_crl(response.content)
        except (requests.RequestException, ValueError):
            continue

        # Verify the CRL is signed by the issuer before trusting it.
        try:
            issuer_public_key = issuer_cert.public_key()
            if isinstance(issuer_public_key, rsa.RSAPublicKey):
                issuer_public_key.verify(
                    crl.signature,
                    crl.tbs_certlist_bytes,
                    padding.PKCS1v15(),
                    crl.signature_hash_algorithm,
                )
            elif isinstance(issuer_public_key, ec.EllipticCurvePublicKey):
                issuer_public_key.verify(
                    crl.signature,
                    crl.tbs_certlist_bytes,
                    ec.ECDSA(crl.signature_hash_algorithm),
                )
        except InvalidSignature:
            continue  # untrusted CRL, don't rely on it

        # Check CRL freshness.
        now = datetime.datetime.now(datetime.timezone.utc)
        if crl.next_update_utc and now > crl.next_update_utc:
            continue  # stale CRL, don't trust it for a "good" verdict

        revoked = crl.get_revoked_certificate_by_serial_number(
            cert.serial_number
        )
        return RevocationStatus.REVOKED if revoked else RevocationStatus.GOOD

    return RevocationStatus.UNKNOWN


def _get_extension(cert: x509.Certificate, ext_class):
    try:
        return cert.extensions.get_extension_for_class(ext_class).value
    except x509.ExtensionNotFound:
        return None


def check_revocation(
    cert: x509.Certificate, issuer_cert: x509.Certificate
) -> RevocationStatus:
    """Try OCSP first (faster, real-time), fall back to CRL."""
    status = check_revocation_ocsp(cert, issuer_cert)
    if status != RevocationStatus.UNKNOWN:
        return status
    return check_revocation_crl(cert, issuer_cert)


# ---------------------------------------------------------------------
# 4. Full validation entry point
# ---------------------------------------------------------------------

def validate_certificate(
    leaf_cert: x509.Certificate,
    intermediate_certs: list[x509.Certificate],
    trusted_roots: list[x509.Certificate],
    check_revocation_status: bool = True,
) -> ValidationResult:
    errors = []
    revocation_status = RevocationStatus.UNKNOWN

    try:
        chain = build_and_verify_chain(leaf_cert, intermediate_certs, trusted_roots)
    except ValidationError as e:
        return ValidationResult(valid=False, revocation_status=revocation_status, errors=[str(e)])

    # Check expiration for every cert in the chain, not just the leaf.
    for cert in chain:
        try:
            verify_validity_period(cert)
        except ValidationError as e:
            errors.append(str(e))

    if check_revocation_status and len(chain) >= 2:
        try:
            revocation_status = check_revocation(chain[0], chain[1])
            if revocation_status == RevocationStatus.REVOKED:
                errors.append("Certificate has been revoked")
        except Exception as e:
            errors.append(f"Revocation check failed: {e}")

    return ValidationResult(
        valid=(len(errors) == 0),
        revocation_status=revocation_status,
        errors=errors,
    )