/*
Write secure code to map XML to an object that safely disables the resolution of external entities and DTDs entirely during parsing 
*/

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import org.xml.sax.InputSource;

public class SecureXmlMapper {

    /**
     * Builds a hardened DocumentBuilderFactory with DTDs and external
     * entities disabled entirely. This is the safest configuration
     * recommended by OWASP for preventing XXE attacks.
     */
    private static DocumentBuilderFactory createSecureFactory() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // Primary defense: reject any document containing a DOCTYPE
        // declaration outright. This alone blocks virtually all XXE vectors,
        // since entities must be declared in a DTD.
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        // Defense in depth: explicitly disable external entity resolution
        // in case DOCTYPE processing is ever re-enabled in the future.
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        // Disable XInclude processing (a separate mechanism that can also
        // pull in external content).
        factory.setXIncludeAware(false);

        // Don't expand entity references into the DOM tree.
        factory.setExpandEntityReferences(false);

        // Belt-and-suspenders via the standard JAXP secure-processing flag,
        // which also enables limits on entity expansion (protects against
        // "billion laughs" style DoS).
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        // Disallow all external access for DTDs and schemas.
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        // These are no-ops once DOCTYPE is disallowed, but kept for clarity
        // and as a safety net if disallow-doctype-decl is ever relaxed.
        factory.setValidating(false);
        factory.setNamespaceAware(true);

        return factory;
    }

    public static Customer parseCustomer(String xmlPayload) throws Exception {
        DocumentBuilderFactory factory = createSecureFactory();
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Prevent the builder from silently resolving any external entity
        // it might still be asked to resolve.
        builder.setEntityResolver((publicId, systemId) -> {
            throw new org.xml.sax.SAXException(
                "External entity resolution is disabled: " + systemId);
        });

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