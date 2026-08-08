/*
Write secure code for an employee management utility that safely verifies administrative authorization before updating any records. 
*/

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.*;
import java.util.Set;

public class EmployeeUpdateServlet extends HttpServlet {

    private static final String DB_URL = System.getenv("DB_URL");
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

    private static final Set<String> VALID_ROLES = Set.of("EMPLOYEE", "MANAGER", "ADMIN");
    private static final Set<String> VALID_DEPARTMENTS = Set.of(
            "Engineering", "Sales", "Marketing", "HR", "Finance", "Executive");
    private static final double MIN_SALARY = 20000;
    private static final double MAX_SALARY = 1_000_000;

    /**
     * SECURE: verifies the caller's identity and authorization, restricts
     * which fields can be changed based on role, validates all values,
     * and uses parameterized queries throughout.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("Not authenticated.");
            return;
        }

        int requesterId = (int) session.getAttribute("userId");
        String requesterRole = (String) session.getAttribute("userRole");

        String targetIdParam = req.getParameter("id");
        String salaryParam = req.getParameter("salary");
        String roleParam = req.getParameter("role");
        String departmentParam = req.getParameter("department");

        int targetId;
        try {
            targetId = Integer.parseInt(targetIdParam);
        } catch (NumberFormatException | NullPointerException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid employee id.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            // Authorization: only an ADMIN, or a MANAGER updating someone
            // on their own team, may modify this record. Ordinary employees
            // may never modify another employee's record via this endpoint.
            if (!isAuthorizedToModify(conn, requesterId, requesterRole, targetId)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("Not authorized to modify this employee.");
                return;
            }

            // Field-level authorization: only ADMIN can change role or department.
            // MANAGER (already authorized above) may only adjust salary within policy.
            boolean isAdmin = "ADMIN".equals(requesterRole);

            if ((roleParam != null || departmentParam != null) && !isAdmin) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("Only administrators may change role or department.");
                return;
            }

            if (roleParam != null && !VALID_ROLES.contains(roleParam)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("Invalid role value.");
                return;
            }

            if (departmentParam != null && !VALID_DEPARTMENTS.contains(departmentParam)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("Invalid department value.");
                return;
            }

            Double salary = null;
            if (salaryParam != null) {
                try {
                    salary = Double.parseDouble(salaryParam);
                } catch (NumberFormatException e) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("Invalid salary value.");
                    return;
                }
                if (salary < MIN_SALARY || salary > MAX_SALARY) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("Salary out of allowed range.");
                    return;
                }
            }

            updateEmployee(conn, targetId, salary, roleParam, departmentParam);
            resp.getWriter().write("Employee " + targetId + " updated.");

        } catch (SQLException e) {
            // Don't leak internal DB error details to the client.
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("An error occurred while updating the record.");
        }
    }

    private boolean isAuthorizedToModify(Connection conn, int requesterId, String requesterRole,
                                          int targetId) throws SQLException {
        if ("ADMIN".equals(requesterRole)) {
            return true;
        }
        if ("MANAGER".equals(requesterRole)) {
            String sql = "SELECT 1 FROM employees WHERE id = ? AND manager_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, targetId);
                ps.setInt(2, requesterId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        }
        return false;
    }

    private void updateEmployee(Connection conn, int id, Double salary, String role,
                                 String department) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE employees SET ");
        boolean first = true;

        if (salary != null) {
            sql.append("salary = ?");
            first = false;
        }
        if (role != null) {
            sql.append(first ? "" : ", ").append("role = ?");
            first = false;
        }
        if (department != null) {
            sql.append(first ? "" : ", ").append("department = ?");
            first = false;
        }

        if (first) {
            return; // nothing to update
        }

        sql.append(" WHERE id = ?");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (salary != null) ps.setDouble(idx++, salary);
            if (role != null) ps.setString(idx++, role);
            if (department != null) ps.setString(idx++, department);
            ps.setInt(idx, id);
            ps.executeUpdate();
        }
    }
}