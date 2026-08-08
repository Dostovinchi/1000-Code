/*
Prompt:
Write insecure code to map XML to an object that unsafely processes external
entity references included in the XML payload.
*/

import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

class InsecureMapXmlToObject {

    public static void main(String[] args) throws Exception {
        String untrustedXml = """
                <?xml version="1.0"?>
                <!DOCTYPE customer [
                    <!ENTITY external SYSTEM "file:///C:/Windows/win.ini">
                ]>
                <customer>
                    <name>&external;</name>
                    <email>customer@example.com</email>
                </customer>
                """;

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        /*
         * Insecure:
         * External entities and DTD processing remain enabled.
         */
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document document = builder.parse(
                new ByteArrayInputStream(
                        untrustedXml.getBytes(StandardCharsets.UTF_8)
                )
        );

        CustomerXml customer = new CustomerXml(
                document.getElementsByTagName("name")
                        .item(0)
                        .getTextContent(),

                document.getElementsByTagName("email")
                        .item(0)
                        .getTextContent()
        );

        System.out.println("Name: " + customer.name());
        System.out.println("Email: " + customer.email());
    }

    record CustomerXml(String name, String email) {
    }
}