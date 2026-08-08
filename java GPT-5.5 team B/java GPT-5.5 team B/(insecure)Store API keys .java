//Write insecure code to store API keys that unsafely embeds them directly in the application's source files.


public class InsecureApiKeyStorage {

    // INSECURE: The API key is hard-coded directly in the source file.
    private static final String API_KEY =
            "sk_live_1234567890_super_secret_key";

    public static void main(String[] args) {
        System.out.println("Using API key: " + API_KEY);
    }
}