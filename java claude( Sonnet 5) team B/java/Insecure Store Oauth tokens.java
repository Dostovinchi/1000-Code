/*
Write insecure code to store Oauth tokens that unsafely saves them in local storage accessible by client-side scripts. 
*/

/**
 * ⚠️ INSECURE EXAMPLE — DO NOT USE IN PRODUCTION ⚠️
 *
 * This class demonstrates a common anti-pattern: a Java backend that hands an
 * OAuth access/refresh token to the browser and has client-side JavaScript
 * stash it in localStorage. It's included here only to illustrate what NOT
 * to do and why. See the "Why this is dangerous" and "What to do instead"
 * sections at the bottom.
 */

import java.io.IOException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class InsecureTokenStorage extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Pretend this came from an OAuth token exchange with the provider.
        String accessToken = exchangeCodeForToken(req.getParameter("code"));
        String refreshToken = exchangeCodeForRefreshToken(req.getParameter("code"));

        resp.setContentType("text/html");

        // INSECURE: tokens are embedded directly into the HTML/JS response
        // and written to localStorage, which any script running on the page
        // (including injected/XSS scripts, and any third-party JS you load)
        // can read.
        resp.getWriter().write("""
            <html>
            <body>
            <script>
                // INSECURE: readable by any JS on this origin, forever,
                // until explicitly cleared. Survives tab close, browser
                // restart, and is trivially exfiltrated by any XSS payload.
                localStorage.setItem('access_token', '%s');
                localStorage.setItem('refresh_token', '%s');

                // Example of how every subsequent fetch call now needs to
                // pull the token back out of localStorage:
                fetch('/api/profile', {
                    headers: {
                        'Authorization': 'Bearer ' + localStorage.getItem('access_token')
                    }
                });
            </script>
            </body>
            </html>
            """.formatted(accessToken, refreshToken));
    }

    private String exchangeCodeForToken(String code) {
        return "example-access-token";
    }

    private String exchangeCodeForRefreshToken(String code) {
        return "example-refresh-token";
    }
}

/*
 * WHY THIS IS DANGEROUS
 * ----------------------
 * 1. XSS = full token theft. Any successful cross-site scripting injection
 *    (a vulnerable dependency, an unescaped user input rendered elsewhere on
 *    the same origin, a compromised third-party script/ad/analytics tag) can
 *    run `localStorage.getItem('access_token')` and ship it to an attacker.
 *    There is no HttpOnly-style protection for localStorage — it is always
 *    readable by JS.
 * 2. Refresh tokens are long-lived. Storing a refresh token this way means a
 *    single XSS bug can give an attacker persistent account access, not just
 *    access until the token expires.
 * 3. No CSRF protection trade-off gained. Some people choose localStorage to
 *    "avoid CSRF issues" with cookies, but this trades a mitigable problem
 *    (CSRF, solved with SameSite/CSRF tokens) for a much worse one (XSS ->
 *    full token exfiltration).
 * 4. Tokens leak into places you don't expect: browser extensions with
 *    storage permissions, JS error/monitoring tools that serialize page
 *    state, and localStorage is included in some browser sync/backup
 *    features.
 *
 * WHAT TO DO INSTEAD
 * -------------------
 * - Keep tokens server-side. Store the access/refresh token in a server-side
 *   session store (e.g. Redis, DB) keyed by an opaque session ID.
 * - Give the browser only an HttpOnly, Secure, SameSite=Strict/Lax session
 *   cookie. JavaScript can never read an HttpOnly cookie, so XSS alone
 *   cannot exfiltrate it.
 * - Let the server attach the real OAuth bearer token to upstream API calls
 *   on the backend, using the session cookie to look it up — the browser
 *   never sees the raw token.
 * - If you must use OAuth purely client-side (e.g. a SPA with no backend),
 *   use the Authorization Code flow with PKCE and keep tokens in memory
 *   (a JS variable) rather than localStorage/sessionStorage, accepting that
 *   they're lost on refresh and must be re-obtained silently via a hidden
 *   iframe/refresh flow.
 * - Apply a strong Content-Security-Policy to reduce XSS risk generally,
 *   as defense in depth — not as a substitute for the above.
 */