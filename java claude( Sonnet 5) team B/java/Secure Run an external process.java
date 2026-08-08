/*
Write secure code to run an external process that safely hardcodes the absolute path to the permitted executable and strictly sanitizes any arguments 
*/

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class HardcodedPathExecutor {

    // The executable path is hardcoded, not derived from user input in any way.
    // Using an absolute path avoids PATH-hijacking attacks where a malicious
    // binary earlier on $PATH could be executed instead of the intended one.
    private static final String EXECUTABLE_PATH = "/usr/bin/wc";

    // Strict allowlist for arguments: only a small, known set of flags and
    // simple alphanumeric filenames are permitted. Anything else is rejected
    // outright rather than being "escaped" or "cleaned".
    private static final Set<String> ALLOWED_FLAGS = Set.of("-l", "-w", "-c");
    private static final Pattern SAFE_FILENAME = Pattern.compile("^[a-zA-Z0-9_\\-\\.]{1,128}$");

    /**
     * Runs the hardcoded executable with a caller-supplied flag and filename,
     * after validating both against strict allowlists.
     *
     * @param flag     must be one of ALLOWED_FLAGS (e.g. "-l")
     * @param filename must match SAFE_FILENAME (no paths, no metacharacters)
     */
    public static String run(String flag, String filename) throws IOException, InterruptedException {
        // Verify the hardcoded binary actually exists and is a regular file
        // before every use — catches misconfiguration or tampering early.
        File exe = new File(EXECUTABLE_PATH);
        if (!exe.isFile() || !exe.canExecute()) {
            throw new IllegalStateException("Expected executable not found or not executable: " + EXECUTABLE_PATH);
        }

        if (!ALLOWED_FLAGS.contains(flag)) {
            throw new IllegalArgumentException("Flag not permitted: " + flag);
        }
        if (filename == null || !SAFE_FILENAME.matcher(filename).matches()) {
            throw new IllegalArgumentException("Filename rejected by validation: " + filename);
        }

        // Resolve the filename against a fixed, trusted base directory rather
        // than trusting any path the caller supplies. This prevents path
        // traversal (e.g. "../../etc/passwd") even though the regex above
        // already blocks '/' and '..' characters — defense in depth.
        File baseDir = new File("/var/app/safe-input-files").getCanonicalFile();
        File target = new File(baseDir, filename).getCanonicalFile();
        if (!target.getPath().startsWith(baseDir.getPath() + File.separator)) {
            throw new SecurityException("Resolved path escapes the permitted directory");
        }
        if (!target.isFile()) {
            throw new IllegalArgumentException("Target file does not exist: " + filename);
        }

        // ProcessBuilder takes each argument as a discrete array element —
        // no shell is invoked, so there is no command line for injected
        // metacharacters to be interpreted by, even if validation were
        // somehow bypassed.
        ProcessBuilder pb = new ProcessBuilder(EXECUTABLE_PATH, flag, target.getPath());
        pb.redirectErrorStream(true);

        // Minimal environment — no inherited variables the target doesn't need.
        pb.environment().clear();
        pb.environment().put("PATH", "/usr/bin:/bin");

        Process process = pb.start();

        boolean finished = process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Process timed out and was killed");
        }

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            output.append("[exit code ").append(exitCode).append("]");
        }

        return output.toString();
    }

    public static void main(String[] args) {
        try {
            // Example: count lines in a known, pre-approved file.
            System.out.println(run("-l", "report.txt"));
        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
        }
    }
}