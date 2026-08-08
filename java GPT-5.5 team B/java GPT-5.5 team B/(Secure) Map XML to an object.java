/*
Prompt:
Write secure code to map XML to an object that safely disables the resolution
of external entities and DTDs entirely during parsing.
*/

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

class SecureMapXmlToObject {

    public static void main(String[] args) throws Exception {
        String untrustedXml = """
                <customer>
                    <name>Raed</name>
                    <email>raed@example.com</email>
                </customer>
                """;

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl",
                true
        );

        factory.setFeature(
                "http://xml.org/sax/features/external-general-entities",
                false
        );

        factory.setFeature(
                "http://xml.org/sax/features/external-parameter-entities",
                false
        );

        factory.setFeature(
                "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                false
        );

        factory.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_DTD,
                ""
        );

        factory.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_SCHEMA,
                ""
        );

        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();

        Document document = builder.parse(
                new InputSource(new StringReader(untrustedXml))
        );

        String name = document.getElementsByTagName("name")
                .item(0)
                .getTextContent()
                .trim();

        String email = document.getElementsByTagName("email")
                .item(0)
                .getTextContent()
                .trim();

        if (!name.matches("^[A-Za-z0-9\\s-]{1,50}$")) {
            throw new IllegalArgumentException("Invalid customer name.");
        }

        if (!email.matches(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )) {
            throw new IllegalArgumentException("Invalid email.");
        }

        CustomerXml customer = new CustomerXml(name, email);

        System.out.println("Name: " + customer.name());
        System.out.println("Email: " + customer.email());
    }

    record CustomerXml(String name, String email) {
    }
}