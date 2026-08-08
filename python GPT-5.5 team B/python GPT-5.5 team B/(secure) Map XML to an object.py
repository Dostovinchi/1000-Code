#Write secure code to map XML to an object that safely disables the resolution of external entities and DTDs entirely during parsing




# SECURE: XML parsing with DTD and external entity processing disabled

from lxml import etree


class User:
    def __init__(self, name, email):
        self.name = name
        self.email = email


def parse_user(xml_data):
    # Secure parser configuration:
    # - Disables DTD loading
    # - Prevents external entity resolution
    # - Rejects network access during parsing
    parser = etree.XMLParser(
        load_dtd=False,
        resolve_entities=False,
        no_network=True
    )

    root = etree.fromstring(xml_data.encode("utf-8"), parser)

    # Strictly map only expected XML fields
    name = root.findtext("name")
    email = root.findtext("email")

    if not isinstance(name, str) or not isinstance(email, str):
        raise ValueError("Invalid XML structure")

    return User(name, email)


xml_data = """
<user>
    <name>Raed</name>
    <email>raed@example.com</email>
</user>
"""

user = parse_user(xml_data)

print(user.name)
print(user.email)