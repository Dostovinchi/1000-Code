import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportCustomerRecordsXMLDetailed {

    static class Customer {
        String name;
        String email;
        String phone;
        String address;

        Customer(String name, String email, String phone, String address) {
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.address = address;
        }
    }

    private static String getElementText(Element parent, String tag, boolean required) {
        NodeList nodes = parent.getElementsByTagName(tag);
        if (nodes.getLength() == 0 || nodes.item(0).getTextContent().trim().isEmpty()) {
            if (required) {
                throw new IllegalArgumentException("Missing required field: " + tag);
            }
            return null;
        }
        return nodes.item(0).getTextContent().trim();
    }

    public static Map<String, Object> importCustomerRecords(String filePath) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(filePath));
        doc.getDocumentElement().normalize();

        NodeList customerNodes = doc.getElementsByTagName("customer");
        List<Customer> customers = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < customerNodes.getLength(); i++) {
            Element el = (Element) customerNodes.item(i);
            try {
                String name = getElementText(el, "name", true);
                String email = getElementText(el, "email", true);
                String phone = getElementText(el, "phone", false);
                String address = getElementText(el, "address", false);
                customers.add(new Customer(name, email, phone, address));
            } catch (IllegalArgumentException e) {
                errors.add("Record " + i + ": " + e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("imported_count", customers.size());
        result.put("error_count", errors.size());
        result.put("customers", customers);
        result.put("errors", errors);
        return result;
    }

    public static void main(String[] args) throws Exception {
        Map<String, Object> result = importCustomerRecords("customers.xml");
        System.out.println("Imported: " + result.get("imported_count") + " records");
        System.out.println("Errors: " + result.get("error_count") + " records");
    }
}
