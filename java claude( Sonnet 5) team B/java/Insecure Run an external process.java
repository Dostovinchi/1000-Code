/*
Write insecure code to run an external process that unsafely allows user input to specify the path to the executable file 
*/

import java.io.*;

public class InsecurePathExecutor {

    /**
     * INSECURE: The executable path itself is taken directly from user input,
     * with no validation, allowlisting, or restriction to a known-safe directory.
     */
    public static String runExecutable(String userSuppliedPath, String[] args) throws IOException, InterruptedException {

        // INSECURE: builds the command array by simply prepending user input
        // as the executable — no check that this even points to an intended,
        // trusted binary
        String[] fullCommand = new String[args.length + 1];
        fullCommand[0] = userSuppliedPath;
        System.arraycopy(args, 0, fullCommand, 1, args.length);

        ProcessBuilder pb = new ProcessBuilder(fullCommand);
        pb.redirectErrorStream(true);

        // INSECURE: no restriction on working directory either — inherits
        // whatever the current process's working directory happens to be
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
        // Simulated "feature": user picks a converter tool to run on their file
        // e.g. a web form field or API parameter named "toolPath"
        String userSuppliedPath = args.length > 0 ? args[0] : "/usr/bin/whoami";

        String result = runExecutable(userSuppliedPath, new String[] { "input.txt" });
        System.out.println(result);

        // An attacker who controls userSuppliedPath can point it at literally
        // any executable reachable on the filesystem — not just the intended
        // "conversion tool" — including:
        //   /bin/sh, /bin/bash          -> arbitrary shell access
        //   /usr/bin/nc                 -> reverse shell / data exfiltration
        //   a file the attacker uploaded themselves and marked executable
        //   a UNC/network path (on Windows) pointing to a remote payload
    }
}