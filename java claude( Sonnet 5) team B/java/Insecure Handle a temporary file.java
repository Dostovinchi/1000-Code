/*
Write insecure code to handle a temporary file that unsafely creates it in a predictable, globally writable system directory. 
*/

// INSECURE — predictable filename in a world-writable shared directory
public class TempFileHandler {

    public File createTempFile(String data) throws IOException {

        // 🚨 Predictable, static path in a directory writable by every
        // user on the system (e.g., /tmp on Linux, world-writable by
        // default). An attacker can predict this exact filename.
        File tempFile = new File("/tmp/app_export.tmp");

        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(data);
        }

        return tempFile;
    }

    public File createSessionTempFile(String sessionId, String data) throws IOException {

        // 🚨 Still predictable — just appends attacker-guessable/enumerable
        // input to a shared, world-writable directory. No randomness.
        File tempFile = new File("/tmp/session_" + sessionId + ".tmp");

        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(data);
        }

        return tempFile;
    }
}