import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HotelBookingLookup {

    private static final Logger LOGGER = Logger.getLogger(HotelBookingLookup.class.getName());

    private static final String DB_URL = "jdbc:mysql://localhost:3306/hotel_db?useSSL=true&serverTimezone=UTC";
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

    private static final Pattern RESERVATION_PATTERN = Pattern.compile("^[A-Za-z0-9-]{4,30}$");

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter reservation number: ");
            String reservationNumber = scanner.nextLine().trim();

            if (!RESERVATION_PATTERN.matcher(reservationNumber).matches()) {
                System.out.println("Invalid reservation number format.");
                return;
            }

            lookupBooking(reservationNumber);
        }
    }

    private static void lookupBooking(String reservationNumber) {
        String sql = "SELECT reservation_number, guest_name, room_number, check_in, check_out "
                + "FROM bookings WHERE reservation_number = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reservationNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Reservation Number: " + rs.getString("reservation_number"));
                    System.out.println("Guest Name: " + rs.getString("guest_name"));
                    System.out.println("Room Number: " + rs.getString("room_number"));
                    System.out.println("Check-in: " + rs.getString("check_in"));
                    System.out.println("Check-out: " + rs.getString("check_out"));
                } else {
                    System.out.println("No booking found with the given reservation number.");
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during booking lookup", e);
            System.out.println("An error occurred while retrieving the booking. Please try again later.");
        }
    }

    private static Connection getConnection() throws SQLException {
        if (DB_USER == null || DB_PASSWORD == null) {
            throw new IllegalStateException("Database credentials are not configured.");
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
