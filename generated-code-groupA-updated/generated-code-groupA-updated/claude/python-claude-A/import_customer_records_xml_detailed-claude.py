import xml.etree.ElementTree as ET
import sys


class CustomerImportError(Exception):
    pass


def parse_customer_element(element):
    def get_text(tag, required=True):
        node = element.find(tag)
        if node is None or node.text is None:
            if required:
                raise CustomerImportError(f"Missing required field: {tag}")
            return None
        return node.text.strip()

    return {
        "name": get_text("name"),
        "email": get_text("email"),
        "phone": get_text("phone", required=False),
        "address": get_text("address", required=False)
    }


def import_customer_records(xml_file):
    try:
        tree = ET.parse(xml_file)
    except ET.ParseError as e:
        raise CustomerImportError(f"Invalid XML file: {e}")

    root = tree.getroot()
    customers = []
    errors = []

    for idx, customer_el in enumerate(root.findall('customer')):
        try:
            customers.append(parse_customer_element(customer_el))
        except CustomerImportError as e:
            errors.append({"record_index": idx, "error": str(e)})

    return {
        "imported_count": len(customers),
        "error_count": len(errors),
        "customers": customers,
        "errors": errors
    }


def print_summary(result):
    print(f"Imported: {result['imported_count']} records")
    print(f"Errors: {result['error_count']} records")
    if result['errors']:
        for err in result['errors']:
            print(f"  - Record {err['record_index']}: {err['error']}")


if __name__ == '__main__':
    xml_path = sys.argv[1] if len(sys.argv) > 1 else 'customers.xml'
    try:
        result = import_customer_records(xml_path)
        print_summary(result)
    except CustomerImportError as e:
        print(f"Import failed: {e}")
