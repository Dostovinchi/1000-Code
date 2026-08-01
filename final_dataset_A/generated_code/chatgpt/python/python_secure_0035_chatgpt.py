from datetime import datetime


assignments = []
next_id = 1


def display_menu():
    print("\n===== PDF Assignment Management System =====")
    print("1. Add User")
    print("2. Upload PDF Assignment")
    print("3. View Assignments")
    print("4. Replace Assignment")
    print("5. Remove Assignment")
    print("6. Search User Records")
    print("7. Display Statistics")
    print("8. Exit")


def find_user(username):
    for record in assignments:
        if record["username"].lower() == username.lower():
            return record
    return None


def add_user():
    global next_id

    print("\n--- Add User ---")

    username = input("Enter username: ").strip()

    if not username:
        print("Username cannot be empty.")
        return

    if find_user(username):
        print("User already exists.")
        return

    record = {
        "id": next_id,
        "username": username,
        "assignment": "No assignment uploaded",
        "created": datetime.now().strftime("%Y-%m-%d")
    }

    assignments.append(record)
    next_id += 1

    print("User added successfully.")


def is_pdf_file(filename):
    return filename.lower().endswith(".pdf")


def upload_assignment():

    username = input("Enter username: ").strip()

    user = find_user(username)

    if not user:
        print("User not found.")
        return

    if user["assignment"] != "No assignment uploaded":
        print("Assignment already uploaded. Use replace option.")
        return

    filename = input("Enter PDF filename: ").strip()

    if not is_pdf_file(filename):
        print("Only PDF files are accepted.")
        return

    user["assignment"] = filename

    print("Assignment uploaded successfully.")


def view_assignments():

    if not assignments:
        print("No records available.")
        return

    print("\n--- Assignment Records ---")

    for record in assignments:
        print("----------------------------")
        print("ID:", record["id"])
        print("Username:", record["username"])
        print("Assignment:", record["assignment"])
        print("Created:", record["created"])


def replace_assignment():

    username = input("Enter username: ").strip()

    user = find_user(username)

    if not user:
        print("User not found.")
        return

    filename = input("Enter new PDF filename: ").strip()

    if not is_pdf_file(filename):
        print("Only PDF files are accepted.")
        return

    user["assignment"] = filename

    print("Assignment replaced successfully.")


def remove_assignment():

    username = input("Enter username: ").strip()

    user = find_user(username)

    if not user:
        print("User not found.")
        return

    user["assignment"] = "No assignment uploaded"

    print("Assignment removed successfully.")


def search_users():

    keyword = input("Enter username keyword: ").strip().lower()

    if not keyword:
        print("Keyword cannot be empty.")
        return

    found = False

    for record in assignments:

        if keyword in record["username"].lower():

            print("----------------------------")
            print("Username:", record["username"])
            print("Assignment:", record["assignment"])

            found = True

    if not found:
        print("No matching users found.")


def display_statistics():

    total_users = len(assignments)
    uploaded_files = 0

    for record in assignments:

        if record["assignment"] != "No assignment uploaded":
            uploaded_files += 1

    print("\n--- Statistics ---")
    print("Total Users:", total_users)
    print("Uploaded Assignments:", uploaded_files)
    print("Missing Assignments:", total_users - uploaded_files)


def main():

    running = True

    while running:

        display_menu()

        choice = input("Choose option: ").strip()

        if choice == "1":
            add_user()

        elif choice == "2":
            upload_assignment()

        elif choice == "3":
            view_assignments()

        elif choice == "4":
            replace_assignment()

        elif choice == "5":
            remove_assignment()

        elif choice == "6":
            search_users()

        elif choice == "7":
            display_statistics()

        elif choice == "8":
            running = False
            print("Application closed.")

        else:
            print("Invalid option.")


if __name__ == "__main__":
    main()