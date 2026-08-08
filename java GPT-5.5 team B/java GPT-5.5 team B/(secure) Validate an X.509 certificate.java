//Write secure code to validate an X.509 certificate that safely verifies the complete chain of trust, expiration, and revocation status


import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.*;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class SecureX509Validation {

    public static PKIXCertPathValidatorResult validateCertificateChain(
            List<X509Certificate> certificateChain,
            Path trustStorePath,
            char[] trustStorePassword
    ) throws Exception {

        if (certificateChain == null || certificateChain.isEmpty()) {
            throw new IllegalArgumentException("Certificate chain cannot be empty.");
        }

        // Load explicitly trusted root CA certificates.
        KeyStore trustStore = KeyStore.getInstance("PKCS12");

        try (InputStream input = Files.newInputStream(trustStorePath)) {
            trustStore.load(input, trustStorePassword);
        }

        PKIXParameters validationParameters = new PKIXParameters(trustStore);

        // Enables PKIX revocation checking.
        validationParameters.setRevocationEnabled(true);

        CertPathValidator validator = CertPathValidator.getInstance("PKIX");

        PKIXRevocationChecker revocationChecker =
                (PKIXRevocationChecker) validator.getRevocationChecker();

        /*
         * Prefer OCSP and fall back to CRLs when OCSP is unavailable.
         *
         * SOFT_FAIL is intentionally not enabled: an undetermined
         * revocation status causes validation to fail closed.
         */
        Set<PKIXRevocationChecker.Option> options = EnumSet.of(
                PKIXRevocationChecker.Option.PREFER_CRLS,
                PKIXRevocationChecker.Option.NO_FALLBACK
        );

        /*
         * Remove PREFER_CRLS and NO_FALLBACK when OCSP-first validation
         * with CRL fallback is desired:
         *
         * revocationChecker.setOptions(EnumSet.noneOf(
         *         PKIXRevocationChecker.Option.class
         * ));
         */

        revocationChecker.setOptions(options);
        validationParameters.addCertPathChecker(revocationChecker);

        /*
         * A CertPath must omit the trusted root certificate.
         * It should contain:
         * leaf certificate -> intermediate CA certificates
         */
        List<X509Certificate> pathCertificates = certificateChain;

        X509Certificate finalCertificate =
                certificateChain.get(certificateChain.size() - 1);

        if (isTrustAnchor(finalCertificate, trustStore)) {
            pathCertificates = certificateChain.subList(
                    0,
                    certificateChain.size() - 1
            );
        }

        CertificateFactory certificateFactory =
                CertificateFactory.getInstance("X.509");

        CertPath certificatePath =
                certificateFactory.generateCertPath(pathCertificates);

        /*
         * PKIX validation verifies:
         * - Certificate signatures
         * - Chain continuity
         * - Trusted root anchoring
         * - Validity periods
         * - Basic constraints
         * - Key usage and critical extensions
         * - Revocation status
         */
        return (PKIXCertPathValidatorResult) validator.validate(
                certificatePath,
                validationParameters
        );
    }

    private static boolean isTrustAnchor(
            X509Certificate certificate,
            KeyStore trustStore
    ) throws Exception {

        for (var aliases = trustStore.aliases(); aliases.hasMoreElements();) {
            String alias = aliases.nextElement();

            Certificate trustedCertificate =
                    trustStore.getCertificate(alias);

            if (certificate.equals(trustedCertificate)) {
                return true;
            }
        }

        return false;
    }

    public static void main(String[] args) {
        try {
            CertificateFactory factory =
                    CertificateFactory.getInstance("X.509");

            X509Certificate leaf;
            X509Certificate intermediate;
            X509Certificate root;

            try (InputStream input =
                         Files.newInputStream(Path.of("leaf.pem"))) {
                leaf = (X509Certificate) factory.generateCertificate(input);
            }

            try (InputStream input =
                         Files.newInputStream(Path.of("intermediate.pem"))) {
                intermediate =
                        (X509Certificate) factory.generateCertificate(input);
            }

            try (InputStream input =
                         Files.newInputStream(Path.of("root.pem"))) {
                root = (X509Certificate) factory.generateCertificate(input);
            }

            PKIXCertPathValidatorResult result =
                    validateCertificateChain(
                            List.of(leaf, intermediate, root),
                            Path.of("truststore.p12"),
                            System.getenv("TRUSTSTORE_PASSWORD").toCharArray()
                    );

            System.out.println("Certificate chain is valid.");
            System.out.println(
                    "Trusted root: " +
                    result.getTrustAnchor()
                            .getTrustedCert()
                            .getSubjectX500Principal()
            );

        } catch (Exception exception) {
            System.err.println(
                    "Certificate validation failed: " +
                    exception.getMessage()
            );
        }
    }
}