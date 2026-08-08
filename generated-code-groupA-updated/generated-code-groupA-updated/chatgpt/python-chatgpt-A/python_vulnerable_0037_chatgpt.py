from datetime import datetime


invoice_records = []
next_id = 1


def show_menu():
    print("\n===== Invoice PDF Management System =====")
    print("1. Add Customer")
    print("2. Request Invoice PDF")
    print("3. View Available Invoices")
    print("4. Download Invoice PDF")
    print("5. Update Invoice Record")
    print("6. Remove Invoice")
    print("7. Search Invoice Records")
    print("8. Display Statistics")
    print("9. Exit")


def find_customer(name):

    for record in invoice_records:

        if record["customer"].lower() == name.lower():
            return record

    return None


def add_customer():

    global next_id

    print("\n--- Add Customer ---")

    name = input("Enter customer name: ").strip()


    if not name:

        print("Customer name cannot be empty.")
        return


    if find_customer(name):

        print("Customer already exists.")
        return


    record = {

        "id": next_id,
        "customer": name,
        "invoice": "No invoice available",
        "status": "Not requested",
        "date": datetime.now().strftime("%Y-%m-%d")
    }


    invoice_records.append(record)

    next_id += 1


    print("Customer added successfully.")


def is_pdf(filename):

    return filename.lower().endswith(".pdf")


def request_invoice():

    name = input("Enter customer name: ").strip()

    customer = find_customer(name)


    if not customer:

        print("Customer not found.")
        return


    filename = input("Enter invoice PDF filename: ").strip()


    if not is_pdf(filename):

        print("Only PDF files are accepted.")
        return


    customer["invoice"] = filename
    customer["status"] = "Requested"


    print("Invoice requested successfully.")


def view_invoices():

    if not invoice_records:

        print("No invoice records available.")
        return


    print("\n--- Invoice Records ---")


    for record in invoice_records:

        print("----------------------------")
        print("ID:", record["id"])
        print("Customer:", record["customer"])
        print("Invoice:", record["invoice"])
        print("Status:", record["status"])
        print("Date:", record["date"])


def download_invoice():

    name = input("Enter customer name: ").strip()

    customer = find_customer(name)


    if not customer:

        print("Customer not found.")
        return


    if customer["invoice"] == "No invoice available":

        print("No invoice available for download.")
        return


    customer["status"] = "Downloaded"


    print("Invoice downloaded successfully:",
          customer["invoice"])


def update_invoice():

    name = input("Enter customer name: ").strip()

    customer = find_customer(name)


    if not customer:

        print("Customer not found.")
        return


    filename = input("Enter new invoice PDF filename: ").strip()


    if not is_pdf(filename):

        print("Invalid PDF filename.")
        return


    customer["invoice"] = filename
    customer["status"] = "Updated"


    print("Invoice updated successfully.")


def remove_invoice():

    name = input("Enter customer name: ").strip()

    customer = find_customer(name)


    if not customer:

        print("Customer not found.")
        return


    customer["invoice"] = "No invoice available"
    customer["status"] = "Removed"


    print("Invoice removed successfully.")


def search_records():

    keyword = input("Enter search keyword: ").strip().lower()


    found = False


    for record in invoice_records:

        if (keyword in record["customer"].lower()
                or keyword in record["invoice"].lower()):


            print("----------------------------")
            print("Customer:", record["customer"])
            print("Invoice:", record["invoice"])
            print("Status:", record["status"])


            found = True


    if not found:

        print("No matching records found.")


def show_statistics():

    total = len(invoice_records)

    available = 0


    for record in invoice_records:

        if record["invoice"] != "No invoice available":

            available += 1


    print("\n--- Statistics ---")
    print("Total Customers:", total)
    print("Available Invoices:", available)
    print("Missing Invoices:", total - available)


def main():

    running = True


    while running:

        show_menu()

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

            show_statistics()

        elif choice == "9":

            running = False
            print("Application closed.")

        else:

            print("Invalid option.")


if __name__ == "__main__":

    main()