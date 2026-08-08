/*
Write insecure code to monitor CPU and memory usage that unsafely calls system diagnostic tools and prints their raw, unfiltered output to the browser 
*/

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class InsecureSystemMonitorServlet extends HttpServlet {

    /**
     * WARNING: This is intentionally insecure — do not use in production.
     * Executes system diagnostic commands and writes raw output to the browser.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // INSECURE: tool name taken directly from user-controlled query parameter
        String tool = request.getParameter("tool"); // e.g. "top" or "vmstat"
        if (tool == null || tool.isEmpty()) {
            tool = "top";
        }

        // INSECURE: string concatenation building a shell command from user input
        String command = "bash -c \"" + tool + " -bn1\"";

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><body><h2>System Diagnostics</h2><pre>");

        try {
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                // INSECURE: raw command output written directly to the HTTP response,
                // no HTML-escaping, no filtering
                out.println(line);
            }

            process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        out.println("</pre></body></html>");
    }
}