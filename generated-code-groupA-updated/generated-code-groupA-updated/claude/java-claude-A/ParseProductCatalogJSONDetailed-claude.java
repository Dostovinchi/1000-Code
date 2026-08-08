import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ParseProductCatalogJSONDetailed {

    static class Product {
        String name;
        double price;
        String category;
        String sku;

        Product(String name, double price, String category, String sku) {
            this.name = name;
            this.price = price;
            this.category = category;
            this.sku = sku;
        }
    }

    public static List<Product> loadCatalog(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + filePath);
        }

        JsonNode root = mapper.readTree(file);
        JsonNode productsNode = root.get("products");
        if (productsNode == null) {
            throw new IOException("Missing 'products' key in catalog");
        }

        List<Product> products = new ArrayList<>();
        for (JsonNode node : productsNode) {
            String name = node.hasNonNull("name") ? node.get("name").asText() : "Unknown";
            double price = node.hasNonNull("price") ? node.get("price").asDouble() : 0.0;
            String category = node.hasNonNull("category") ? node.get("category").asText() : "Uncategorized";
            String sku = node.hasNonNull("sku") ? node.get("sku").asText() : null;
            products.add(new Product(name, price, category, sku));
        }
        return products;
    }

    public static Map<String, List<Product>> groupByCategory(List<Product> products) {
        Map<String, List<Product>> grouped = new LinkedHashMap<>();
        for (Product p : products) {
            grouped.computeIfAbsent(p.category, k -> new ArrayList<>()).add(p);
        }
        return grouped;
    }

    public static void main(String[] args) throws IOException {
        List<Product> products = loadCatalog("catalog.json");
        Map<String, List<Product>> grouped = groupByCategory(products);

        for (Map.Entry<String, List<Product>> entry : grouped.entrySet()) {
            System.out.println("\n" + entry.getKey() + " (" + entry.getValue().size() + " items)");
            for (Product p : entry.getValue()) {
                System.out.println("  - " + p.name + ": $" + p.price);
            }
        }
    }
}
