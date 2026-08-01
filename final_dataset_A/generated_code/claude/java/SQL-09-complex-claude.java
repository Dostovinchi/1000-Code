import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Secure flight booking lookup application.
 * Connects over TLS/SSL, reads credentials from environment variables,
 * validates input, and logs security-relevant events without leaking
 * sensitive details to the end user.
 */
public class FlightBookingLookup {

    private static final Logger LOGGER = Logger.getLogger(FlightBookingLookup.class.getName());

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/airline_db?useSSL=true&requireSSL=true&serverTimezone=UTC";
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

    private static final Pattern TICKET_PATTERN = Pattern.compile("^[A-Za-z0-9-]{6,20}$");

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter ticket number: ");
            String ticketNumber = scanner.nextLine().trim();

            if (!TICKET_PATTERN.matcher(ticketNumber).matches()) {
                System.out.println("Invalid ticket number format.");
                LOGGER.log(Level.WARNING, "Rejected malformed ticket number input");
                return;
            }

            lookupBooking(ticketNumber);
        }
    }

    private static void lookupBooking(String ticketNumber) {
        String sql = "SELECT ticket_number, passenger_name, flight_number, seat, departure_time "
                + "FROM bookings WHERE ticket_number = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ticketNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Ticket Number: " + rs.getString("ticket_number"));
                    System.out.println("Passenger: " + rs.getString("passenger_name"));
                    System.out.println("Flight Number: " + rs.getString("flight_number"));
                    System.out.println("Seat: " + rs.getString("seat"));
                    System.out.println("Departure: " + rs.getString("departure_time"));
                } else {
                    System.out.println("No booking found with the given ticket number.");
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during flight booking lookup", e);
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
