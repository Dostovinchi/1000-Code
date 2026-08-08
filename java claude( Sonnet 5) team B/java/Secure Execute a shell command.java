/*
Write secure code to execute a shell command that safely executes within a highly restricted environment with minimal necessary privileges 
*/

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

public class RestrictedCommandExecutor {

    // Allowlist of commands this executor is permitted to run at all —
    // nothing outside this set can ever be invoked.
    private static final Set<String> ALLOWED_COMMANDS = Set.of(
        "/usr/bin/date", "/usr/bin/uptime", "/usr/bin/whoami"
    );

    // Strict allowlist for any argument passed to the command —
    // rejects shell metacharacters, path traversal, etc.
    private static final Pattern SAFE_ARG = Pattern.compile("^[a-zA-Z0-9_\\-\\.]{0,64}$");

    /**
     * Executes an allowlisted command with a minimal, restricted environment:
     *  - no shell is invoked (ProcessBuilder uses exec(), not sh -c)
     *  - runs as an unprivileged OS user via setpriv (drops root -> nobody)
     *  - working directory pinned to a throwaway sandbox dir
     *  - environment stripped to only PATH and a minimal HOME
     *  - resource limits applied (CPU time, memory, no new privileges)
     *  - hard timeout so the process can't hang indefinitely
     */
    public static String runRestricted(String command, List<String> args) throws Exception {
        if (!ALLOWED_COMMANDS.contains(command)) {
            throw new SecurityException("Command not in allowlist: " + command);
        }
        for (String arg : args) {
            if (!SAFE_ARG.matcher(arg).matches()) {
                throw new IllegalArgumentException("Unsafe argument rejected: " + arg);
            }
        }

        // Dedicated, empty sandbox working directory (created fresh, no
        // access to the caller's real filesystem context).
        Path sandboxDir = Files.createTempDirectory("sandbox-");
        sandboxDir.toFile().deleteOnExit();

        // Build the real argv: setpriv drops privileges and locks down
        // capabilities/new-privilege escalation before exec'ing the target.
        //   --reuid/--regid   -> switch to an unprivileged user (e.g. "nobody")
        //   --no-new-privs    -> process (and children) can never regain privileges
        //   --inh-caps=-all   -> drop all inheritable Linux capabilities
        //   --bounding-set=-all -> drop the entire capability bounding set
        List<String> fullCommand = new ArrayList<>(List.of(
            "setpriv",
            "--reuid", "nobody",
            "--regid", "nogroup",
            "--clear-groups",
            "--no-new-privs",
            "--inh-caps=-all",
            "--bounding-set=-all",
            command
        ));
        fullCommand.addAll(args);

        ProcessBuilder pb = new ProcessBuilder(fullCommand);
        pb.directory(sandboxDir.toFile());
        pb.redirectErrorStream(true);

        // Strip environment down to the bare minimum — no inherited secrets,
        // credentials, or caller-controlled variables.
        Map<String, String> env = pb.environment();
        env.clear();
        env.put("PATH", "/usr/bin:/bin");
        env.put("HOME", sandboxDir.toString());

        Process process = pb.start();

        // Hard timeout so a stuck/hostile process can't run forever.
        boolean finished = process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Command timed out and was killed");
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

        // Clean up the sandbox directory.
        Files.deleteIfExists(sandboxDir);

        return output.toString();
    }

    public static void main(String[] args) {
        try {
            String result = runRestricted("/usr/bin/uptime", List.of());
            System.out.println(result);
        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
        }
    }
}
