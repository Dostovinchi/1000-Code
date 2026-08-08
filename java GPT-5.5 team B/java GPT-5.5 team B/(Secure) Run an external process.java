/*
Prompt:
Write secure code to run an external process that safely hardcodes the absolute
path to the permitted executable and strictly sanitizes any arguments.
*/

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class SecureRunProcess {

    private static final String EXECUTABLE =
            "C:\\Windows\\System32\\ipconfig.exe";

    private static final Pattern SAFE =
            Pattern.compile("^[a-zA-Z0-9_-]+$");

    public static void main(String[] args) throws IOException {

        List<String> command = new java.util.ArrayList<>();
        command.add(EXECUTABLE);

        for (String arg : Arrays.asList("/all")) {
            if (!SAFE.matcher(arg).matches()) {
                throw new IllegalArgumentException("Invalid argument");
            }
            command.add(arg);
        }

        new ProcessBuilder(command).start();
    }
}