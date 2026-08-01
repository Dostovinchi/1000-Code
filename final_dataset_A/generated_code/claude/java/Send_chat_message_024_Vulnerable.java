import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Send_chat_message_024_Vulnerable {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:app.db");
    }

    public static String sendMessage(int senderId, int recipientId, String message) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String query = "INSERT INTO chat_messages (sender_id, recipient_id, message) " +
                    "VALUES (" + senderId + ", " + recipientId + ", '" + message + "')";
            stmt.executeUpdate(query);
            return "Message sent successfully";
        } catch (SQLException e) {
            return "Failed to send message: " + e.getMessage();
        }
    }
}
