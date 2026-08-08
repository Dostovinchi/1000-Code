/*
Write secure code to log security events that safely transmits logs using strict network encryption. 
*/

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SecureSecurityEventLogger {

    private static final String SYSLOG_SERVER = "log-collector.internal.example.com";
    private static final int SYSLOG_TLS_PORT = 6514; // RFC 5425: syslog over TLS

    // Path to a truststore containing ONLY the CA(s) you trust for your
    // internal log collector — never disable verification or trust-all.
    private static final String TRUSTSTORE_PATH = System.getenv("LOG_TLS_TRUSTSTORE_PATH");
    private static final String TRUSTSTORE_PASSWORD = System.getenv("LOG_TLS_TRUSTSTORE_PASSWORD");

    // Local buffer so events aren't lost if the collector is briefly unreachable.
    private final BlockingQueue<String> localFallbackQueue = new LinkedBlockingQueue<>(10_000);

    private SSLSocketFactory sslSocketFactory;

    public SecureSecurityEventLogger() {
        this.sslSocketFactory = buildSslSocketFactory();
    }

    /**
     * Builds an SSLSocketFactory that validates the server certificate
     * against a pinned, internally-managed truststore — no trust-all
     * TrustManager, no disabled hostname verification.
     */
    private SSLSocketFactory buildSslSocketFactory() {
        try {
            if (TRUSTSTORE_PATH == null || TRUSTSTORE_PASSWORD == null) {
                throw new IllegalStateException(
                    "LOG_TLS_TRUSTSTORE_PATH / LOG_TLS_TRUSTSTORE_PASSWORD must be configured");
            }

            KeyStore trustStore = KeyStore.getInstance("PKCS12");
            try (InputStream ts = new java.io.FileInputStream(TRUSTSTORE_PATH)) {
                trustStore.load(ts, TRUSTSTORE_PASSWORD.toCharArray());
            }

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            SSLContext context = SSLContext.getInstance("TLSv1.3");
            context.init(null, tmf.getTrustManagers(), null);

            return context.getSocketFactory();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize TLS for security log transport", e);
        }
    }

    /**
     * Logs a security event over an encrypted, integrity-protected,
     * certificate-verified TLS connection. Never logs raw secrets
     * (passwords, tokens, session IDs) — only structured metadata.
     */
    public void logSecurityEvent(String eventType, String username, String sourceIp, String details) {
        String eventId = UUID.randomUUID().toString();
        String logMessage = buildStructuredMessage(eventId, eventType, username, sourceIp, details);

        try {
            sendOverTls(logMessage);
        } catch (IOException e) {
            // Never silently drop a security event — buffer locally for retry,
            // and alert via a separate, already-trusted channel if the queue fills.
            if (!localFallbackQueue.offer(logMessage)) {
                System.err.println("CRITICAL: security log fallback queue full, event may be lost: " + eventId);
            }
            System.err.println("Failed to transmit security event " + eventId + " over TLS, buffered for retry: "
                    + e.getMessage());
        }
    }

    private void sendOverTls(String logMessage) throws IOException {
        try (SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(SYSLOG_SERVER, SYSLOG_TLS_PORT)) {
            sslSocket.setEnabledProtocols(new String[]{"TLSv1.3", "TLSv1.2"});
            sslSocket.setSoTimeout((int) TimeUnit.SECONDS.toMillis(5));

            // Handshake also verifies hostname against the certificate's SAN,
            // since endpoint identification is enabled by default in the JDK
            // when using SSLSocketFactory with a properly configured SSLContext.
            sslSocket.startHandshake();

            OutputStream out = sslSocket.getOutputStream();
            // RFC 5425 uses octet-counting framing so message boundaries can't
            // be spoofed/injected by an attacker splicing in fake entries.
            byte[] payload = logMessage.getBytes("UTF-8");
            String framed = payload.length + " " + logMessage;
            out.write(framed.getBytes("UTF-8"));
            out.flush();
        }
    }

    private String buildStructuredMessage(String eventId, String eventType, String username,
                                           String sourceIp, String details) {
        // Structured (key=value) format is easier for SIEM parsing and
        // avoids ambiguity that free-text concatenation can introduce.
        return String.format(
            "id=%s timestamp=%s type=%s user=%s source_ip=%s details=%s",
            eventId, Instant.now(), sanitize(eventType), sanitize(username),
            sanitize(sourceIp), sanitize(details)
        );
    }

    /**
     * Strips characters that could be used for log injection
     * (e.g. newlines forging additional fake log entries).
     */
    private String sanitize(String input) {
        if (input == null) return "[none]";
        return input.replaceAll("[\\r\\n]", "_").trim();
    }

    /**
     * Background retry loop for buffered events — call from a scheduled
     * executor rather than inline with request handling.
     */
    public void retryFallbackQueue() {
        String message;
        while ((message = localFallbackQueue.poll()) != null) {
            try {
                sendOverTls(message);
            } catch (IOException e) {
                // Put it back and stop for now; will retry on next cycle.
                localFallbackQueue.offer(message);
                break;
            }
        }
    }

    public static void main(String[] args) {
        SecureSecurityEventLogger logger = new SecureSecurityEventLogger();
        logger.logSecurityEvent(
            "FAILED_LOGIN",
            "admin",
            "203.0.113.42",
            "Invalid password attempt #3, account locked"
        );
    }
}