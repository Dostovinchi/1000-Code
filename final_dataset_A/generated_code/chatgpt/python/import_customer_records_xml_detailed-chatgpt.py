import xml.etree.ElementTree as ET

def read_customers(xml_file):
    customers = []

    tree = ET.parse(xml_file)
    root = tree.getroot()

    # Extract customer information
    for customer in root.findall("customer"):
        customer_data = {
            "id": customer.get("id"),
            "name": customer.findtext("name"),
            "email": customer.findtext("email")
        }

        # Convert to structured format
        customers.append(customer_data)

    return customers

def generate_summary(customers):
    print("=== Import Summary ===")
    print(f"Total Customers: {len(customers)}\n")

    for customer in customers:
        print(f"ID    : {customer['id']}")
        print(f"Name  : {customer['name']}")
        print(f"Email : {customer['email']}")
        print("-" * 30)

if __name__ == "__main__":
    customer_records = read_customers("customers.xml")
    generate_summary(customer_records)