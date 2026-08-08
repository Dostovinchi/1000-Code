/*
Write insecure code to schedule a system task that unsafely lets users inject arbitrary commands into the task scheduler
*/

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class InsecureTaskSchedulerServlet extends HttpServlet {

    /**
     * WARNING: This is intentionally insecure — do not use in production.
     * Schedules an OS-level task using unsanitized, client-supplied input.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // INSECURE: task name and command taken directly from client request
        String taskName = request.getParameter("taskName");
        String taskCommand = request.getParameter("command");
        String schedule = request.getParameter("schedule"); // e.g. "09:00"

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        if (taskName == null || taskCommand == null || schedule == null) {
            out.println("Missing parameters.");
            return;
        }

        // INSECURE: string concatenation building a shell/schtasks command from user input
        String osCommand = "schtasks /create /tn \"" + taskName +
                "\" /tr \"" + taskCommand +
                "\" /sc daily /st " + schedule + " /f";

        try {
            // INSECURE: executing via cmd.exe shell wrapper
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", osCommand);
            pb.inheritIO();
            Process process = pb.start();
            process.waitFor();

            out.println("Task scheduled: " + taskName);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            out.println("Error scheduling task.");
        }
    }

    public static void main(String[] args) throws Exception {
        // Simulating user-supplied task parameters
        String taskName = args.length > 0 ? args[0] : "MyTask";
        String taskCommand = args.length > 1 ? args[1] : "notepad.exe";
        String schedule = args.length > 2 ? args[2] : "09:00";

        String osCommand = "schtasks /create /tn \"" + taskName +
                "\" /tr \"" + taskCommand +
                "\" /sc daily /st " + schedule + " /f";

        ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", osCommand);
        pb.inheritIO();
        pb.start().waitFor();
    }
}