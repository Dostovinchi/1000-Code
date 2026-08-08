/*
Write insecure code to compress files that unsafely constructs the compression command string using untrusted file names
*/

import java.io.*;

public class InsecureCompressor {

    /**
     * WARNING: This is intentionally insecure — do not use in production.
     * Builds a compression command string directly from untrusted file names.
     */
    public static void compressFiles(String[] fileNames, String outputArchive) throws IOException, InterruptedException {

        // INSECURE: concatenating untrusted file names directly into a shell command
        StringBuilder command = new StringBuilder();
        command.append("zip -r \"").append(outputArchive).append("\" ");

        for (String fileName : fileNames) {
            command.append("\"").append(fileName).append("\" ");
        }

        System.out.println("Running command: " + command);

        // INSECURE: passing the raw string to a shell interpreter
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command.toString());
        pb.inheritIO();
        Process process = pb.start();
        process.waitFor();
    }

    public static void main(String[] args) throws Exception {
        // Simulating user-supplied file names, e.g. from an upload form or API request
        String[] userFiles = {
                args.length > 0 ? args[0] : "report.txt",
                args.length > 1 ? args[1] : "notes.txt"
        };
        String archiveName = args.length > 2 ? args[2] : "archive.zip";

        compressFiles(userFiles, archiveName);
    }
}