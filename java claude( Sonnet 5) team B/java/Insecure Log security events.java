/*
Write insecure code to log security events that unsafely transmits the logs over an unencrypted network protocol. 
*/

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.time.LocalDateTime;

public class InsecureSecurityEventLogger {

    private static final String SYSLOG_SERVER = "log-collector.internal.example.com";
    private static final int SYSLOG_PORT = 514; // ⚠️ plaintext syslog (UDP/TCP), unencrypted

    /**
     * ⚠️ INSECURE: Sends security event logs over a raw, unencrypted TCP
     * socket to a remote log collector. Anyone on the network path
     * (or performing ARP spoofing / MITM) can read or tamper with the data.
     */
    public void logSecurityEvent(String eventType, String username, String sourceIp, String details) {
        String logMessage = String.format(
            "[%s] SECURITY_EVENT type=%s user=%s source_ip=%s details=%s",
            LocalDateTime.now(), eventType, username, sourceIp, details
        );

        // ⚠️ Also print locally, unencrypted, to stdout
        System.out.println(logMessage);

        try (Socket socket = new Socket(SYSLOG_SERVER, SYSLOG_PORT)) {
            OutputStream out = socket.getOutputStream();
            // ⚠️ Sent in cleartext — no TLS, no encryption, no integrity check
            out.write(logMessage.getBytes("UTF-8"));
            out.flush();
        } catch (IOException e) {
            System.err.println("Failed to send log: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        InsecureSecurityEventLogger logger = new InsecureSecurityEventLogger();
        logger.logSecurityEvent(
            "FAILED_LOGIN",
            "admin",
            "203.0.113.42",
            "Invalid password attempt #3, account locked"
        );
    }
}