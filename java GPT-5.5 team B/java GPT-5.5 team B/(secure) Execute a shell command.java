//Write secure code to execute a shell command that safely executes within a highly restricted environment with minimal necessary privileges



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class RestrictedCommandRunner {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final int MAX_OUTPUT_CHARACTERS = 8_000;

    /*
     * Only explicitly approved operations are available.
     * Users never provide an executable or arbitrary command string.
     */
    private static final Map<String, List<String>> ALLOWED_COMMANDS = Map.of(
            "disk-usage", List.of("/usr/bin/du", "-sh", "/srv/example/data"),
            "service-status", List.of(
                    "/usr/bin/systemctl",
                    "--no-pager",
                    "is-active",
                    "example-worker.service"
            )
    );

    private static final Set<String> SAFE_ENVIRONMENT_KEYS = Set.of(
            "LANG",
            "LC_ALL"
    );

    private static final Path PRIVATE_WORK_DIRECTORY =
            Path.of("/srv/example/command-work");

    private RestrictedCommandRunner() {
    }

    public static CommandResult execute(String operation)
            throws IOException, InterruptedException {

        List<String> command = ALLOWED_COMMANDS.get(operation);

        if (command == null) {
            throw new SecurityException("Operation is not permitted.");
        }

        verifyRuntimeIsNotPrivileged();
        prepareRestrictedWorkingDirectory();

        /*
         * No shell is involved:
         * - no sh -c
         * - no cmd.exe /c
         * - no string concatenation
         * - no user-controlled executable or arguments
         */
        ProcessBuilder builder = new ProcessBuilder(command);

        builder.directory(PRIVATE_WORK_DIRECTORY.toFile());
        builder.redirectErrorStream(true);

        /*
         * Remove inherited credentials, tokens, proxy settings,
         * library injection variables and other unnecessary context.
         */
        Map<String, String> environment = builder.environment();

        String lang = environment.getOrDefault("LANG", "C.UTF-8");
        String locale = environment.getOrDefault("LC_ALL", "C.UTF-8");

        environment.clear();

        environment.put("LANG", sanitizeEnvironmentValue(lang));
        environment.put("LC_ALL", sanitizeEnvironmentValue(locale));
        environment.put("PATH", "/usr/bin:/bin");

        Process process = builder.start();

        String output;

        try {
            output = readLimitedOutput(process);

            boolean completed = process.waitFor(
                    TIMEOUT.toMillis(),
                    TimeUnit.MILLISECONDS
            );

            if (!completed) {
                process.destroy();

                if (!process.waitFor(500, TimeUnit.MILLISECONDS)) {
                    process.destroyForcibly();
                }

                throw new IOException(
                        "Command exceeded the execution time limit."
                );
            }

            return new CommandResult(
                    process.exitValue(),
                    output
            );

        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    private static String readLimitedOutput(Process process)
            throws IOException {

        StringBuilder output = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        process.getInputStream(),
                        StandardCharsets.UTF_8
                )
        )) {
            int character;

            while ((character = reader.read()) != -1) {
                if (output.length() >= MAX_OUTPUT_CHARACTERS) {
                    process.destroyForcibly();

                    throw new IOException(
                            "Command output exceeded the permitted limit."
                    );
                }

                /*
                 * Remove control characters except newline and tab.
                 */
                if (character == '\n'
                        || character == '\t'
                        || character >= 32) {
                    output.append((char) character);
                }
            }
        }

        return output.toString();
    }

    private static void verifyRuntimeIsNotPrivileged() {
        String username = System.getProperty("user.name", "");

        /*
         * This is only a defense-in-depth check.
         * The service must be configured by the operating system to run
         * under a dedicated unprivileged account.
         */
        if ("root".equals(username)
                || "administrator".equalsIgnoreCase(username)) {
            throw new SecurityException(
                    "The command runner must not run as an administrator."
            );
        }
    }

    private static void prepareRestrictedWorkingDirectory()
            throws IOException {

        Files.createDirectories(PRIVATE_WORK_DIRECTORY);

        if (Files.isSymbolicLink(PRIVATE_WORK_DIRECTORY)) {
            throw new SecurityException(
                    "The working directory must not be a symbolic link."
            );
        }
    }

    private static String sanitizeEnvironmentValue(String value) {
        if (value == null
                || value.length() > 64
                || value.contains("\n")
                || value.contains("\r")
                || value.contains("\0")) {
            return "C.UTF-8";
        }

        return value;
    }

    public record CommandResult(
            int exitCode,
            String output
    ) {
    }

    public static void main(String[] args) {
        try {
            CommandResult result = execute("disk-usage");

            System.out.println("Exit code: " + result.exitCode());
            System.out.println(result.output());

        } catch (SecurityException exception) {
            System.err.println("Command rejected.");

        } catch (IOException | InterruptedException exception) {
            Thread.currentThread().interrupt();
            System.err.println("Restricted command execution failed.");
        }
    }
}
