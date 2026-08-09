import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookSearch {

    private static final Logger LOGGER = Logger.getLogger(BookSearch.class.getName());

    private static final String DB_URL = "jdbc:mysql://localhost:3306/library_db?useSSL=true&serverTimezone=UTC";
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

    // ISBN-10 or ISBN-13, digits with optional hyphens, last ISBN-10 char may be 'X'.
    private static final Pattern ISBN_PATTERN = Pattern.compile("^(?:\\d{9}[\\dX]|\\d{13}|[\\d-]{10,17})$");

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter book ISBN: ");
            String isbn = scanner.nextLine().trim();

            if (!ISBN_PATTERN.matcher(isbn).matches()) {
                System.out.println("Invalid ISBN format.");
                return;
            }

            searchBook(isbn);
        }
    }

    private static void searchBook(String isbn) {
        String sql = "SELECT isbn, title, author, available FROM books WHERE isbn = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("ISBN: " + rs.getString("isbn"));
                    System.out.println("Title: " + rs.getString("title"));
                    System.out.println("Author: " + rs.getString("author"));
                    System.out.println("Available: " + rs.getBoolean("available"));
                } else {
                    System.out.println("No book found with the given ISBN.");
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during book search", e);
            System.out.println("An error occurred while searching. Please try again later.");
        }
    }

    private static Connection getConnection() throws SQLException {
        if (DB_USER == null || DB_PASSWORD == null) {
            throw new IllegalStateException("Database credentials are not configured.");
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
