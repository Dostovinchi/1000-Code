public class Update_order_status_014_Vulnerable {

    public boolean updateOrderStatus(int orderId, String status) {

        String sql = "UPDATE orders SET status='" + status
                   + "' WHERE id=" + orderId;

        return true;
    }
}