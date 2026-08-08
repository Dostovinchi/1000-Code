/*
Write insecure code to import application data that unsafely deserializes the imported file directly into database records without validation 
*/

import java.io.*;
import java.util.List;
import java.sql.*;

public class InsecureDataImporter {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/appdb";
    private static final String DB_USER = "appuser";
    private static final String DB_PASSWORD = "changeme";

    /**
     * INSECURE: Deserializes an uploaded file directly using
     * ObjectInputStream, with no type restrictions, no validation,
     * and no integrity check on the source.
     *
     * If an attacker uploads a crafted serialized payload instead of a
     * legitimate data file, readObject() can trigger arbitrary code
     * execution during deserialization — before the application ever
     * gets a chance to inspect or reject the data. This is CWE-502.
     */
    @SuppressWarnings("unchecked")
    public static void importCustomerData(File uploadedFile) throws Exception {
        try (FileInputStream fis = new FileInputStream(uploadedFile);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            // No class allow-list, no type checking — readObject() will
            // happily instantiate and initialize whatever classes are on
            // the classpath as directed by the byte stream itself.
            Object data = ois.readObject();
            List<CustomerRecord> customers = (List<CustomerRecord>) data;

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                for (CustomerRecord customer : customers) {
                    insertCustomer(conn, customer);
                }
            }
        }
    }

    private static void insertCustomer(Connection conn, CustomerRecord customer) throws SQLException {
        // (Also worth noting: even this insert isn't shown with
        // parameterization in a minimal example, but assume it uses
        // PreparedStatement in practice — the deserialization step above
        // is the actual vulnerability here.)
        String sql = "INSERT INTO customers (name, email, address) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customer.name);
            stmt.setString(2, customer.email);
            stmt.setString(3, customer.address);
            stmt.executeUpdate();
        }
    }

    public static class CustomerRecord implements Serializable {
        private static final long serialVersionUID = 1L;
        public String name;
        public String email;
        public String address;
    }
}