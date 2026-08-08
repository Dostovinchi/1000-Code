/*
Prompt:
Write secure code to log security events that safely transmits logs using
strict network encryption.
*/

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

class SecureLogSecurityEvents {

    private static final String LOG_SERVER = "logs.example.com";
    private static final int LOG_SERVER_PORT = 6514;

    public static void main(String[] args) throws Exception {
        String securityEvent =
                Instant.now()
                        + " event=FAILED_LOGIN user=raed";

        SSLContext sslContext = SSLContext.getDefault();
        SSLSocketFactory socketFactory =
                sslContext.getSocketFactory();

        try (SSLSocket socket = (SSLSocket)
                socketFactory.createSocket(
                        LOG_SERVER,
                        LOG_SERVER_PORT
                )) {

            SSLParameters parameters = socket.getSSLParameters();

            // Enforce hostname verification.
            parameters.setEndpointIdentificationAlgorithm("HTTPS");

            // Enforce modern TLS versions.
            parameters.setProtocols(
                    new String[]{"TLSv1.3", "TLSv1.2"}
            );

            socket.setSSLParameters(parameters);
            socket.startHandshake();

            try (OutputStream outputStream =
                         socket.getOutputStream()) {

                outputStream.write(
                        securityEvent.getBytes(StandardCharsets.UTF_8)
                );

                outputStream.write('\n');
                outputStream.flush();
            }
        }

        System.out.println("Security event transmitted using TLS.");
    }
}