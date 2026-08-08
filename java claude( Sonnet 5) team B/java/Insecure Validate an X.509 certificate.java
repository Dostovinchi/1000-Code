/*
Write insecure code to validate an X.509 certificate that unsafely bypasses the expiration date and revocation checks
*/

import javax.net.ssl.*;
import java.security.cert.*;
import java.security.*;

// ⚠️ INSECURE — DO NOT USE IN PRODUCTION ⚠️
// Demonstrates certificate validation bypasses that have caused real-world
// MITM vulnerabilities (e.g., CVE-2014-0224-class issues, mobile app SSL bugs).
public class InsecureCertValidation {

    /**
     * VULNERABILITY: A TrustManager that accepts any certificate chain,
     * regardless of expiration, revocation, or trust anchor.
     */
    public static TrustManager[] getTrustAllTrustManagers() {
        return new TrustManager[] {
            new X509ExtendedTrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    // Does nothing — accepts any client cert, expired or not.
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    // VULNERABILITY: no call to cert.checkValidity(),
                    // no chain building to a trusted root, no revocation check.
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType, java.net.Socket socket) {}
                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType, java.net.Socket socket) {}
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {}
                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {}

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0]; // trusts everyone
                }
            }
        };
    }

    /**
     * VULNERABILITY: manually "validating" a cert but skipping the checks
     * that actually matter.
     */
    public static boolean unsafeValidate(X509Certificate cert) {
        try {
            // Verifies the signature was made by *some* key, but never checks
            // that the issuer is a trusted CA, never checks expiration,
            // and never checks a CRL/OCSP responder for revocation.
            cert.verify(cert.getPublicKey()); // often even self-verifies (self-signed bypass)
            return true; // no checkValidity(), no PKIXParameters, no revocation
        } catch (Exception e) {
            return false;
        }
    }

    public static SSLContext getInsecureSslContext() throws Exception {
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, getTrustAllTrustManagers(), new SecureRandom());
        return sc; // any HTTPS client using this context accepts any server cert
    }

    public static void main(String[] args) throws Exception {
        SSLContext sc = getInsecureSslContext();
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true); // also skips hostname check
        System.out.println("All HTTPS connections will now trust any certificate.");
    }
}