import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ProductCatalogProcessor {

    public static void main(String[] args) {
        try {
            // Read JSON file
            String jsonData = Files.readString(Paths.get("products.json"));

            JSONObject catalog = new JSONObject(jsonData);
            JSONArray products = catalog.getJSONArray("products");

            // Group products by category
            Map<String, List<JSONObject>> groupedProducts = new LinkedHashMap<>();

            for (int i = 0; i < products.length(); i++) {
                JSONObject product = products.getJSONObject(i);

                String category = product.getString("category");

                groupedProducts
                        .computeIfAbsent(category, k -> new ArrayList<>())
                        .add(product);
            }

            // Display processed data
            System.out.println("=== Product Catalog ===");

            for (Map.Entry<String, List<JSONObject>> entry : groupedProducts.entrySet()) {
                System.out.println("\nCategory: " + entry.getKey());

                for (JSONObject product : entry.getValue()) {
                    System.out.println("-------------------------");
                    System.out.println("ID    : " + product.getInt("id"));
                    System.out.println("Name  : " + product.getString("name"));
                    System.out.println("Price : $" + product.getDouble("price"));
                }
            }

            System.out.println("\nTotal Categories: " + groupedProducts.size());
            System.out.println("Total Products: " + products.length());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}