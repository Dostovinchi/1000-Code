from datetime import datetime


invoice_records = []
next_id = 1


def display_menu():
    print("\n===== Invoice PDF Management System =====")
    print("1. Add Customer")
    print("2. Request Invoice PDF")
    print("3. View Invoice Records")
    print("4. Download Invoice PDF")
    print("5. Update Invoice Record")
    print("6. Remove Invoice")
    print("7. Search Invoice Records")
    print("8. Display Statistics")
    print("9. Exit")


def find_customer(customer_name):

    for record in invoice_records:
        if record["customer"].lower() == customer_name.lower():
            return record

    return None


def add_customer():

    global next_id

    customer_name = input("Enter customer name: ").strip()


    if not customer_name:

        print("Customer name cannot be empty.")
        return


    if find_customer(customer_name):

        print("Customer already exists.")
        return


    record = {
        "id": next_id,
        "customer": customer_name,
        "invoice_file": "No invoice available",
        "status": "Not requested",
        "created_date": datetime.now().strftime("%Y-%m-%d")
    }


    invoice_records.append(record)

    next_id += 1


    print("Customer added successfully.")


def validate_pdf(filename):

    return filename.lower().endswith(".pdf")


def request_invoice():

    customer_name = input("Enter customer name: ").strip()

    customer = find_customer(customer_name)


    if not customer:

        print("Customer not found.")
        return


    filename = input("Enter invoice PDF filename: ").strip()


    if not validate_pdf(filename):

        print("Only PDF files are accepted.")
        return


    customer["invoice_file"] = filename
    customer["status"] = "Requested"


    print("Invoice requested successfully.")


def view_invoices():

    if not invoice_records:

        print("No invoice records found.")
        return


    print("\n--- Invoice Records ---")


    for record in invoice_records:

        print("----------------------------")
        print("ID:", record["id"])
        print("Customer:", record["customer"])
        print("Invoice:", record["invoice_file"])
        print("Status:", record["status"])
        print("Created:", record["created_date"])


def download_invoice():

    customer_name = input("Enter customer name: ").strip()

    customer = find_customer(customer_name)


    if not customer:

        print("Customer not found.")
        return


    if customer["invoice_file"] == "No invoice available":

        print("No invoice file available.")
        return


    customer["status"] = "Downloaded"


    print("Invoice downloaded successfully:")
    print(customer["invoice_file"])


def update_invoice():

    customer_name = input("Enter customer name: ").strip()

    customer = find_customer(customer_name)


    if not customer:

        print("Customer not found.")
        return


    filename = input("Enter new invoice PDF filename: ").strip()


    if not validate_pdf(filename):

        print("Invalid invoice format.")
        return


    customer["invoice_file"] = filename
    customer["status"] = "Updated"


    print("Invoice updated successfully.")


def remove_invoice():

    customer_name = input("Enter customer name: ").strip()

    customer = find_customer(customer_name)


    if not customer:

        print("Customer not found.")
        return


    customer["invoice_file"] = "No invoice available"
    customer["status"] = "Removed"


    print("Invoice removed successfully.")


def search_records():

    keyword = input("Enter search keyword: ").strip().lower()


    found = False


    for record in invoice_records:

        if (keyword in record["customer"].lower()
                or keyword in record["invoice_file"].lower()):


            print("----------------------------")
            print("Customer:", record["customer"])
            print("Invoice:", record["invoice_file"])
            print("Status:", record["status"])

            found = True


    if not found:

        print("No matching records found.")


def display_statistics():

    total_records = len(invoice_records)
    available_invoices = 0


    for record in invoice_records:

        if record["invoice_file"] != "No invoice available":

            available_invoices += 1


    print("\n--- Statistics ---")
    print("Total Customers:", total_records)
    print("Available Invoices:", available_invoices)
    print("Missing Invoices:", total_records - available_invoices)


def main():

    running = True


    while running:

        display_menu()

        choice = input("Choose option: ").strip()


        if choice == "1":

            add_customer()

        elif choice == "2":

            request_invoice()

        elif choice == "3":

            view_invoices()

        elif choice == "4":

            download_invoice()

        elif choice == "5":

            update_invoice()

        elif choice == "6":

            remove_invoice()

        elif choice == "7":

            search_records()

        elif choice == "8":

            display_statistics()

        elif choice == "9":

            running = False
            print("Application closed.")

        else:

            print("Invalid option.")


if __name__ == "__main__":

    main()