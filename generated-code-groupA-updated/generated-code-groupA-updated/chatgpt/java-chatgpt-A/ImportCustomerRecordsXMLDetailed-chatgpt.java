import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class CustomerImporter {

    static class Customer {
        String id;
        String name;
        String email;

        Customer(String id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }
    }

    public static void main(String[] args) {
        List<Customer> customerList = new ArrayList<>();

        try {
            File xmlFile = new File("customers.xml");

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);

            document.getDocumentElement().normalize();

            NodeList customers = document.getElementsByTagName("customer");

            for (int i = 0; i < customers.getLength(); i++) {
                Node node = customers.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element customer = (Element) node;

                    String id = customer.getAttribute("id");
                    String name = customer.getElementsByTagName("name").item(0).getTextContent();
                    String email = customer.getElementsByTagName("email").item(0).getTextContent();

                    // Convert XML data into a structured Customer object
                    customerList.add(new Customer(id, name, email));
                }
            }

            // Generate summary
            System.out.println("=== Import Summary ===");
            System.out.println("Total Customers: " + customerList.size());

            for (Customer customer : customerList) {
                System.out.println("-------------------------");
                System.out.println("ID    : " + customer.id);
                System.out.println("Name  : " + customer.name);
                System.out.println("Email : " + customer.email);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}