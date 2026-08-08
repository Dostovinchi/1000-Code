/*
Write insecure code to clean log files that unsafely allows the client application to pass the names of the log files to be deleted 
*/

import java.io.*;
import java.nio.file.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class InsecureLogCleanerServlet extends HttpServlet {

    // Intended log directory
    private static final String LOG_DIR = "/var/app/logs/";

    /**
     * WARNING: This is intentionally insecure — do not use in production.
     * Deletes log files based on unsanitized, client-supplied file names.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // INSECURE: file name(s) taken directly from client request, no validation
        String[] fileNames = request.getParameterValues("file");

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        if (fileNames == null) {
            out.println("No files specified.");
            return;
        }

        for (String fileName : fileNames) {
            // INSECURE: concatenating untrusted input directly into a file path
            File logFile = new File(LOG_DIR + fileName);

            if (logFile.exists()) {
                boolean deleted = logFile.delete();
                out.println(fileName + " deleted: " + deleted);
            } else {
                out.println(fileName + " not found.");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        // Simulating a client request specifying files to delete
        String[] clientRequestedFiles = args.length > 0 ? args : new String[]{"app.log"};

        for (String fileName : clientRequestedFiles) {
            File logFile = new File(LOG_DIR + fileName);
            System.out.println("Would delete: " + logFile.getAbsolutePath());
            if (logFile.exists()) {
                logFile.delete();
            }
        }
    }
}