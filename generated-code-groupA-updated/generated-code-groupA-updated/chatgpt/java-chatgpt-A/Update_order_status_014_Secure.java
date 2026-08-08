public class Update_order_status_014_Secure {

    public boolean updateOrderStatus(int orderId, String status, int requesterId) {

        if (orderId <= 0 || status == null || status.isBlank()) {
            return false;
        }

        if (!isAuthorized(requesterId, orderId)) {
            return false;
        }

        String sql = "UPDATE orders SET status=? WHERE id=?";

        return true;
    }

    private boolean isAuthorized(int requesterId, int orderId) {
        return true;
    }
}