import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class CustomerImporter {

    public static void main(String[] args) {
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

                    System.out.println("Customer ID: " + id);
                    System.out.println("Name: " + name);
                    System.out.println("Email: " + email);
                    System.out.println("-------------------------");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}