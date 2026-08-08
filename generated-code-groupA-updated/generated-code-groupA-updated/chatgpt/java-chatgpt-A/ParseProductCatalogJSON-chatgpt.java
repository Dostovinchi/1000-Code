import org.json.JSONArray;
import org.json.JSONObject;

public class ProductCatalogParser {

    public static void main(String[] args) {

        String jsonData = """
        {
          "products": [
            {
              "id": 1,
              "name": "Laptop",
              "price": 999.99
            },
            {
              "id": 2,
              "name": "Mouse",
              "price": 29.99
            },
            {
              "id": 3,
              "name": "Keyboard",
              "price": 49.99
            }
          ]
        }
        """;

        JSONObject catalog = new JSONObject(jsonData);
        JSONArray products = catalog.getJSONArray("products");

        for (int i = 0; i < products.length(); i++) {
            JSONObject product = products.getJSONObject(i);

            int id = product.getInt("id");
            String name = product.getString("name");
            double price = product.getDouble("price");

            System.out.println("Product ID: " + id);
            System.out.println("Name: " + name);
            System.out.println("Price: $" + price);
            System.out.println("-------------------------");
        }
    }
}