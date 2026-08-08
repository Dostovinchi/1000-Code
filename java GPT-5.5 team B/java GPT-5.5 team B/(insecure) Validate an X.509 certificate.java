//Write insecure code to validate an X.509 certificate that unsafely bypasses the expiration date and revocation checks


import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class InsecureCertificateValidation {

    public static void main(String[] args) throws Exception {

        // INSECURE: Accepts certificates without checking:
        // - Expiration dates
        // - Revocation status
        // - Certificate chain trust
        // - Hostname identity
        TrustManager[] trustAllCertificates = {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(
                            X509Certificate[] chain,
                            String authType
                    ) {
                        // Intentionally bypassed.
                    }

                    @Override
                    public void checkServerTrusted(
                            X509Certificate[] chain,
                            String authType
                    ) {
                        // INSECURE: No checkValidity(), PKIX, CRL, or OCSP checks.
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(
                null,
                trustAllCertificates,
                new SecureRandom()
        );

        HttpsURLConnection.setDefaultSSLSocketFactory(
                sslContext.getSocketFactory()
        );

        // INSECURE: Disables hostname verification.
        HttpsURLConnection.setDefaultHostnameVerifier(
                (hostname, session) -> true
        );

        System.out.println(
                "WARNING: Certificate expiration, revocation, trust, and hostname checks are disabled."
        );
    }
}