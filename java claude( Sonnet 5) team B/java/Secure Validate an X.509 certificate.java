/*
Write secure code to validate an X.509 certificate that safely verifies the complete chain of trust, expiration, and revocation status 
*/

import java.security.KeyStore;
import java.security.cert.*;
import java.util.EnumSet;
import java.util.Set;

/**
 * Secure X.509 certificate chain validator.
 *
 * Validates:
 *  - Full chain of trust up to a trusted anchor (PKIX path building/validation)
 *  - Expiration (validity period) of every certificate in the chain
 *  - Revocation status via OCSP, with CRL as fallback
 *
 * This uses only java.security.cert APIs — no custom/naive TrustManager,
 * no disabling of hostname or path checks.
 */
public final class SecureCertificateValidator {

    private final PKIXParameters pkixParams;

    /**
     * @param trustStore a KeyStore already loaded with your trusted CA certificates
     *                   (e.g. the JVM default cacerts, or a custom trust store you control)
     */
    public SecureCertificateValidator(KeyStore trustStore) throws Exception {
        // Build trust anchors from the trust store's CA certs.
        this.pkixParams = new PKIXParameters(trustStore);

        // Never disable revocation checking at this level; we configure a
        // PKIXRevocationChecker explicitly below for finer control.
        this.pkixParams.setRevocationEnabled(false);

        CertPathValidator validator = CertPathValidator.getInstance("PKIX");
        PKIXRevocationChecker revocationChecker =
                (PKIXRevocationChecker) validator.getRevocationChecker();

        // Revocation checker options, deliberately left empty:
        //   - ONLY_END_ENTITY is NOT set, so every cert in the chain is checked
        //     for revocation, not just the leaf.
        //   - PREFER_CRLS is NOT set, so OCSP is tried first (the default order).
        //   - NO_FALLBACK is NOT set, so if OCSP is unavailable the checker
        //     falls back to CRL rather than skipping the check.
        //   - SOFT_FAIL is NOT set, so an inconclusive revocation check is
        //     treated as failure (fail-closed), not treated as "not revoked".
        Set<PKIXRevocationChecker.Option> options = EnumSet.noneOf(PKIXRevocationChecker.Option.class);

        revocationChecker.setOptions(options);
        this.pkixParams.addCertPathChecker(revocationChecker);
    }

    /**
     * Validates a certificate chain: chain of trust, expiration (all certs),
     * and revocation status (OCSP with CRL fallback).
     *
     * @param chain the certificate chain to validate, leaf certificate first
     * @throws CertificateException if validation fails for any reason
     *         (expired, untrusted, revoked, malformed path, etc.)
     */
    public void validate(X509Certificate[] chain) throws CertificateException {
        if (chain == null || chain.length == 0) {
            throw new CertificateException("Empty certificate chain");
        }

        try {
            // Explicit expiration check on every certificate in the chain.
            // (CertPathValidator also enforces this as part of PKIX path
            // validation, but checking explicitly gives a clearer error.)
            for (X509Certificate cert : chain) {
                cert.checkValidity();
            }

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            CertPath certPath = cf.generateCertPath(java.util.Arrays.asList(chain));

            CertPathValidator validator = CertPathValidator.getInstance("PKIX");

            // This single call verifies:
            //   - signature chain up to a trust anchor
            //   - validity periods
            //   - basic constraints / path length / key usage
            //   - revocation status per the PKIXRevocationChecker configured above
            validator.validate(certPath, pkixParams);

        } catch (CertificateExpiredException | CertificateNotYetValidException e) {
            throw new CertificateException("Certificate is not within its validity period", e);
        } catch (CertPathValidatorException e) {
            // Thrown for untrusted chain, revoked cert, or other path errors.
            throw new CertificateException("Certificate path validation failed: " + e.getReason(), e);
        } catch (Exception e) {
            throw new CertificateException("Certificate validation failed", e);
        }
    }
}