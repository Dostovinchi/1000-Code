import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class FlightBookingLookup {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/airline_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "your_db_password";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter ticket number: ");
        String ticketNumber = scanner.nextLine();

        String sql = "SELECT ticket_number, passenger_name, flight_number, seat, departure_time FROM bookings WHERE ticket_number = ?";

        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, ticketNumber);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("Ticket Number: " + rs.getString("ticket_number"));
                System.out.println("Passenger: " + rs.getString("passenger_name"));
                System.out.println("Flight Number: " + rs.getString("flight_number"));
                System.out.println("Seat: " + rs.getString("seat"));
                System.out.println("Departure: " + rs.getString("departure_time"));
            } else {
                System.out.println("No booking found with ticket number: " + ticketNumber);
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        scanner.close();
    }
}
