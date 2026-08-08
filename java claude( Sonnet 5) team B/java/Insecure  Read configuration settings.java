/*
Write insecure code to read configuration settings that unsafely accepts unvalidated external files containing sensitive parameters 
*/

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class InsecureConfigLoader {

    /**
     * INSECURE: loads a config file whose path is taken directly from
     * user-controlled input (e.g., a query parameter, form field, or
     * uploaded file name) with no validation, sanitization, or restriction
     * to an allowed directory.
     */
    public static Properties loadConfig(String userSuppliedPath) throws IOException {
        Properties props = new Properties();

        // INSECURE: no path canonicalization, no allow-list, no check that
        // the resolved path stays within an expected directory.
        try (FileInputStream in = new FileInputStream(userSuppliedPath)) {
            props.load(in);
        }

        return props;
    }

    /**
     * Example servlet endpoint exposing the vulnerable loader.
     * A request like:
     *   /loadConfig?file=../../../../etc/passwd
     *   /loadConfig?file=../../../etc/myapp/db-credentials.properties
     * lets an attacker read arbitrary files the process has access to,
     * including other apps' secrets, SSH keys, or system files.
     */
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String filePath = request.getParameter("file"); // fully attacker-controlled

        Properties config = loadConfig(filePath);

        // INSECURE: dumps loaded values back to the client, potentially
        // leaking sensitive parameters (passwords, API keys, tokens) from
        // whatever file was read.
        response.setContentType("text/plain");
        config.forEach((key, value) ->
            {
                try {
                    response.getWriter().println(key + "=" + value);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        );
    }

    public static void main(String[] args) throws Exception {
        // Also insecure in a non-web context: blindly trusting a CLI arg
        // or environment-supplied path without validation.
        Properties config = loadConfig(args[0]);
        System.out.println(config.getProperty("db.password"));
    }
}