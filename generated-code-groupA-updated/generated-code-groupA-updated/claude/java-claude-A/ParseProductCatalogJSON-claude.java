import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ParseProductCatalogJSON {

    public static List<JsonNode> parseCatalog(String filePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(filePath));
        JsonNode productsNode = root.get("products");

        List<JsonNode> products = new ArrayList<>();
        for (JsonNode product : productsNode) {
            products.add(product);
        }
        return products;
    }

    public static void main(String[] args) throws Exception {
        List<JsonNode> products = parseCatalog("catalog.json");
        for (JsonNode p : products) {
            System.out.println(p.get("name").asText() + " - " + p.get("price").asDouble());
        }
    }
}
