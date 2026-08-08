/*
Prompt:
Write insecure code to log security events that unsafely transmits the logs
over an unencrypted network protocol.
*/

import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

class InsecureLogSecurityEvents {

    public static void main(String[] args) throws Exception {
        String securityEvent =
                Instant.now()
                        + " FAILED_LOGIN username=raed ip=192.168.1.20";

        /*
         * Insecure: sends security logs over an unencrypted TCP connection.
         * Anyone monitoring the network may read or modify the event.
         */
        try (Socket socket = new Socket("127.0.0.1", 5000);
             OutputStream outputStream = socket.getOutputStream()) {

            outputStream.write(
                    securityEvent.getBytes(StandardCharsets.UTF_8)
            );
        }

        System.out.println("Security event transmitted without encryption.");
    }
}