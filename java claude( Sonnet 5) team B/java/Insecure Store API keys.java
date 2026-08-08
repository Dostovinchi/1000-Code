/*
Write insecure code to store API keys that unsafely embeds them directly in the application's source files. 
*/

import java.net.HttpURLConnection;
import java.net.URL;

public final class InsecureApiClient {

    // VULNERABLE: secret credentials hardcoded directly in source code.
    // Once committed to version control, this key is permanently in the
    // repo's history — even if removed later, it remains recoverable via
    // `git log` / `git show` on any prior commit, and in any forks, CI
    // logs, or build artifacts that checked out that revision.
    private static final String API_KEY = "sk_live_51H8xJ2K9dQmR7vN3pL6wYtZa";

    // VULNERABLE: also true for other secret types — DB passwords,
    // OAuth client secrets, signing keys, etc.
    private static final String DB_PASSWORD = "SuperSecretDbPass123!";

    public String fetchData(String endpoint) throws Exception {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // VULNERABLE: key travels with every build artifact (JAR/WAR),
        // meaning anyone who obtains the compiled application — via
        // decompilation, a leaked internal build, or a misconfigured
        // public artifact repository — can extract it trivially with
        // a decompiler or even `strings` on the class file.
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setRequestMethod("GET");

        // ... read response ...
        return "response data";
    }
}