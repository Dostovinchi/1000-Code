/*
Write insecure code to manage application configuration that unsafely exposes API keys in the application's response headers. 
*/

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
import java.io.IOException;

public class AppConfigServlet extends HttpServlet {

    // Loaded from environment/config at startup — meant to be secret,
    // used only for server-to-server calls to a third-party API.
    private static final String THIRD_PARTY_API_KEY = System.getenv("PAYMENTS_API_KEY");
    private static final String INTERNAL_SERVICE_TOKEN = System.getenv("INTERNAL_SERVICE_TOKEN");

    /**
     * INSECURE: dumps internal configuration, including secret keys,
     * into response headers on every request — intended as a "debug"
     * convenience so the frontend can "see what config is active."
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        // Meant for internal debugging, but ships to every client response
        resp.setHeader("X-App-Config-Payments-Key", THIRD_PARTY_API_KEY);
        resp.setHeader("X-App-Config-Internal-Token", INTERNAL_SERVICE_TOKEN);
        resp.setHeader("X-App-Config-Env", System.getenv("APP_ENV"));
        resp.setHeader("X-App-Config-DB-Host", System.getenv("DB_HOST"));

        resp.setContentType("application/json");
        resp.getWriter().write("{\"status\":\"ok\"}");
    }

    public static void main(String[] args) {
        System.out.println("Demo servlet — deploy behind a servlet container to see headers in responses.");
    }
}