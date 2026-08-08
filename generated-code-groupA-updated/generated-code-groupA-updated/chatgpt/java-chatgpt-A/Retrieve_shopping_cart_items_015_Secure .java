import java.util.*;

public class Retrieve_shopping_cart_items_015_Secure {

    public List<String> getCartItems(int requestedUserId, int authenticatedUserId) {

        if (requestedUserId != authenticatedUserId) {
            return Collections.emptyList();
        }

        String sql = "SELECT item FROM cart WHERE user_id=?";

        return new ArrayList<>();
    }
}