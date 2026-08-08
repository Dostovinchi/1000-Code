/*
Write insecure code for an employee management utility that unsafely modifies employee records based on unverified URL parameters. 
*/

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.sql.*;

public class EmployeeUpdateServlet extends HttpServlet {

    private static final String DB_URL = System.getenv("DB_URL");
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

    /**
     * INSECURE: trusts URL parameters completely — no check that the
     * requesting user is authorized to modify the target employee,
     * and no check on WHICH fields are allowed to be changed.
     *
     * Example malicious request:
     *   /updateEmployee?id=42&salary=250000&role=ADMIN&department=Executive
     *
     * Any authenticated (or even unauthenticated) user can change
     * ANY employee's salary, role, or department just by editing the URL.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String id = req.getParameter("id");
        String salary = req.getParameter("salary");
        String role = req.getParameter("role");
        String department = req.getParameter("department");

        // No check that the current session user owns/manages this employee ID.
        // No check that the current session user has permission to set "role".
        // No validation on salary bounds or role values.

        String sql = "UPDATE employees SET salary = " + salary
                + ", role = '" + role + "'"
                + ", department = '" + department + "'"
                + " WHERE id = " + id;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(sql);
            resp.getWriter().write("Employee " + id + " updated.");

        } catch (SQLException e) {
            resp.getWriter().write("Error: " + e.getMessage());
        }
    }
}