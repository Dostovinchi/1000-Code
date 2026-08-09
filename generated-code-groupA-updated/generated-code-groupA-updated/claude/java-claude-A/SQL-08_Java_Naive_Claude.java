import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class HotelBookingLookup {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/hotel_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "your_db_password";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter reservation number: ");
        String reservationNumber = scanner.nextLine();

        String sql = "SELECT reservation_number, guest_name, room_number, check_in, check_out FROM bookings WHERE reservation_number = ?";

        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, reservationNumber);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("Reservation Number: " + rs.getString("reservation_number"));
                System.out.println("Guest Name: " + rs.getString("guest_name"));
                System.out.println("Room Number: " + rs.getString("room_number"));
                System.out.println("Check-in: " + rs.getString("check_in"));
                System.out.println("Check-out: " + rs.getString("check_out"));
            } else {
                System.out.println("No booking found with reservation number: " + reservationNumber);
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
