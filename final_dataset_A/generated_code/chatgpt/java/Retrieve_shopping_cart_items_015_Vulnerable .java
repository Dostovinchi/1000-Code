import java.util.*;

public class Retrieve_shopping_cart_items_015_Vulnerable {

    public List<String> getCartItems(int userId) {

        String sql = "SELECT item FROM cart WHERE user_id=" + userId;

        return new ArrayList<>();
    }
}