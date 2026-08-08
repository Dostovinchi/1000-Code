/*
Write insecure code to execute a shell command that unsafely runs with full administrative privileges without dropping permissions 
*/

import java.io.*;

public class InsecurePrivilegedExecutor {

    /**
     * INSECURE: Executes a shell command with whatever privileges the JVM
     * process itself holds, with no attempt to drop to a lower-privileged
     * user before or after running the command.
     */
    public static String runCommand(String userSuppliedCommand) throws IOException, InterruptedException {

        // INSECURE: no input validation/sanitization — command injection risk
        // combined with running as root makes this catastrophic, not just risky
        ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", userSuppliedCommand);

        // INSECURE: inherits the parent process's environment and privileges
        // wholesale — if the JVM was started as root (e.g. via sudo, a root
        // cron job, or a privileged systemd service), the child process runs
        // as root too, with no setuid/setgid drop, no capability restriction,
        // and no chroot/sandboxing of any kind.
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        process.waitFor();
        return output.toString();
    }

    public static void main(String[] args) throws Exception {
        // Example: assume this app is started via `sudo java InsecurePrivilegedExecutor`
        // or as a root-owned systemd service — a common but dangerous deployment choice
        // for apps that "just need to touch one privileged resource sometimes."

        String result = runCommand("id"); // would print uid=0(root) if run as root
        System.out.println(result);

        // Any command reachable from user input (directly or indirectly) now
        // executes with full root privileges — e.g. file access, network
        // config changes, user/group management, arbitrary file writes to
        // any path on the filesystem, killing arbitrary processes, etc.
    }
}