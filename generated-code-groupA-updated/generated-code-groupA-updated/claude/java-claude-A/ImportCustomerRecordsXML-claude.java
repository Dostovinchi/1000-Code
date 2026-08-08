import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImportCustomerRecordsXML {

    public static List<String[]> importCustomers(String filePath) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(filePath));
        doc.getDocumentElement().normalize();

        NodeList customerNodes = doc.getElementsByTagName("customer");
        List<String[]> customers = new ArrayList<>();

        for (int i = 0; i < customerNodes.getLength(); i++) {
            Element el = (Element) customerNodes.item(i);
            String name = el.getElementsByTagName("name").item(0).getTextContent();
            String email = el.getElementsByTagName("email").item(0).getTextContent();
            customers.add(new String[]{name, email});
        }
        return customers;
    }

    public static void main(String[] args) throws Exception {
        List<String[]> result = importCustomers("customers.xml");
        for (String[] customer : result) {
            System.out.println(customer[0] + " - " + customer[1]);
        }
    }
}
