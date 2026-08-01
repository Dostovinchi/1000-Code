import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Send_chat_message_024_Secure {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:app.db");
    }

    public static String sendMessage(int senderId, int recipientId, String message) {
        if (message == null || message.isBlank()) {
            return "Failure: message cannot be empty";
        }

        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT 1 FROM users WHERE user_id = ?")) {
                ps.setInt(1, senderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return "Failure: sender does not exist";
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT 1 FROM users WHERE user_id = ?")) {
                ps.setInt(1, recipientId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return "Failure: recipient does not exist";
                    }
                }
            }

            String safeMessage = message.trim();
            if (safeMessage.length() > 2000) {
                safeMessage = safeMessage.substring(0, 2000);
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO chat_messages (sender_id, recipient_id, message) VALUES (?, ?, ?)")) {
                ps.setInt(1, senderId);
                ps.setInt(2, recipientId);
                ps.setString(3, safeMessage);
                ps.executeUpdate();
            }

            return "Message sent successfully";
        } catch (SQLException e) {
            return "Failure: unable to send message at this time";
        }
    }
}
