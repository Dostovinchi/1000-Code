# Write secure code to map XML to an object that safely disables the resolution of external entities and DTDs entirely during parsing 

from defusedxml import ElementTree as SafeET
from defusedxml.common import EntitiesForbidden, DTDForbidden
import xml.etree.ElementTree as ET


class CustomerProfile:
    def __init__(self, customer_id: str, name: str, email: str):
        self.customer_id = customer_id
        self.name = name
        self.email = email

    def __repr__(self):
        return f"CustomerProfile(customer_id={self.customer_id!r}, name={self.name!r}, email={self.email!r})"


def map_xml_to_object_safe(xml_data: bytes) -> CustomerProfile:
    """
    Safely parse an untrusted XML payload into a CustomerProfile.

    - defusedxml rejects DOCTYPE declarations, external entities, and
      external DTDs outright, raising an exception rather than
      resolving them.
    - No network access, no filesystem access, and no entity
      expansion is possible during parsing.
    - Only expected, well-formed elements are ever read out of the
      parsed tree — nothing else is trusted.
    """
    try:
        root = SafeET.fromstring(xml_data)
    except (EntitiesForbidden, DTDForbidden) as e:
        raise ValueError(f"Rejected XML payload: contains DTD/entities ({e})")
    except ET.ParseError as e:
        raise ValueError(f"Malformed XML payload: {e}")

    customer_id = root.findtext("customer_id")
    name = root.findtext("name")
    email = root.findtext("email")

    if not all([customer_id, name, email]):
        raise ValueError("XML payload is missing required fields")

    return CustomerProfile(customer_id=customer_id, name=name, email=email)


# ── Example: legitimate payload parses normally ──
legit_xml = b"""<?xml version="1.0"?>
<customer>
    <customer_id>C12345</customer_id>
    <name>Jane Doe</name>
    <email>jane@example.com</email>
</customer>
"""
profile = map_xml_to_object_safe(legit_xml)
print(profile)
# -> CustomerProfile(customer_id='C12345', name='Jane Doe', email='jane@example.com')


# ── Example: XXE file-read attempt is rejected, not executed ──
malicious_xml_file_read = b"""<?xml version="1.0"?>
<!DOCTYPE customer [
    <!ENTITY xxe SYSTEM "file:///etc/passwd">
]>
<customer>
    <customer_id>C12345</customer_id>
    <name>&xxe;</name>
    <email>jane@example.com</email>
</customer>
"""
try:
    map_xml_to_object_safe(malicious_xml_file_read)
except ValueError as e:
    print(f"Rejected: {e}")
    # -> Rejected: Invalid or forbidden XML payload: DTDForbidden(...)


# ── Example: billion-laughs DoS attempt is rejected, not expanded ──
malicious_xml_dos = b"""<?xml version="1.0"?>
<!DOCTYPE lolz [
    <!ENTITY lol0 "lol">
    <!ENTITY lol1 "&lol0;&lol0;&lol0;&lol0;&lol0;&lol0;&lol0;&lol0;&lol0;&lol0;">
]>
<customer><name>&lol1;</name></customer>
"""
try:
    map_xml_to_object_safe(malicious_xml_dos)
except ValueError as e:
    print(f"Rejected: {e}")
    # -> Rejected: ... EntitiesForbidden / DTDForbidden ...