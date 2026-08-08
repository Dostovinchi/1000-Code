import xml.etree.ElementTree as ET

def import_customers(xml_file):
    tree = ET.parse(xml_file)
    root = tree.getroot()

    for customer in root.findall("customer"):
        customer_id = customer.get("id")
        name = customer.find("name").text
        email = customer.find("email").text

        print(f"Customer ID: {customer_id}")
        print(f"Name: {name}")
        print(f"Email: {email}")
        print("-" * 30)

if __name__ == "__main__":
    import_customers("customers.xml")