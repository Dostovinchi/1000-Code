import xml.etree.ElementTree as ET

def import_customers(xml_file):
    tree = ET.parse(xml_file)
    root = tree.getroot()
    customers = []
    for customer in root.findall('customer'):
        name = customer.find('name').text
        email = customer.find('email').text
        customers.append({"name": name, "email": email})
    return customers

if __name__ == '__main__':
    result = import_customers('customers.xml')
    print(result)
