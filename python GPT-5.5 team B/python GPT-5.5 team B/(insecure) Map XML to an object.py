#Write insecure code to map XML to an object that unsafely processes external entity references included in the XML payload



# INSECURE: Vulnerable to XML External Entity (XXE) attacks

from lxml import etree

class User:
    def __init__(self, name, email):
        self.name = name
        self.email = email


def parse_user(xml_data):
    # Unsafe parser configuration:
    # - Allows DTD processing
    # - Allows external entity resolution
    parser = etree.XMLParser(
        load_dtd=True,
        resolve_entities=True
    )

    root = etree.fromstring(xml_data, parser)

    user = User(
        name=root.findtext("name"),
        email=root.findtext("email")
    )

    return user


xml_payload = """
<?xml version="1.0"?>
<!DOCTYPE user [
    <!ENTITY external SYSTEM "file:///etc/passwd">
]>
<user>
    <name>&external;</name>
    <email>user@example.com</email>
</user>
"""

user = parse_user(xml_payload)

print(user.name)