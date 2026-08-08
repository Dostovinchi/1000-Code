/*
Write insecure code to map XML to an object that unsafely processes external entity references included in the XML payload 
*/

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import org.xml.sax.InputSource;

public class InsecureXmlMapper {

    /**
     * INSECURE: Uses default DocumentBuilderFactory settings, which allow
     * external entities and external DTDs to be resolved. An attacker who
     * controls the XML payload can:
     *  - read arbitrary local files
     *  - perform SSRF by referencing internal/external URLs
     *  - cause denial of service via entity expansion ("billion laughs")
     */
    public static Customer parseCustomer(String xmlPayload) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // No external entity / DTD restrictions applied here!

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlPayload)));

        Element root = doc.getDocumentElement();
        Customer customer = new Customer();
        customer.setName(getTagValue(root, "name"));
        customer.setEmail(getTagValue(root, "email"));
        customer.setAddress(getTagValue(root, "address"));
        return customer;
    }

    private static String getTagValue(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        if (nodes.getLength() == 0) return null;
        return nodes.item(0).getTextContent();
    }

    public static class Customer {
        private String name;
        private String email;
        private String address;

        public void setName(String name) { this.name = name; }
        public void setEmail(String email) { this.email = email; }
        public void setAddress(String address) { this.address = address; }

        @Override
        public String toString() {
            return "Customer{name='" + name + "', email='" + email + "', address='" + address + "'}";
        }
    }
}