from datetime import datetime


assignments = []
next_id = 1


def show_menu():
    print("\n===== PDF Assignment Management System =====")
    print("1. Add User")
    print("2. Upload Assignment")
    print("3. View Assignments")
    print("4. Replace Assignment")
    print("5. Remove Assignment")
    print("6. Search Users")
    print("7. Show Statistics")
    print("8. Exit")


def find_user(username):

    for assignment in assignments:

        if assignment["username"].lower() == username.lower():
            return assignment

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
        "file": "No assignment uploaded",
        "date": datetime.now().strftime("%Y-%m-%d")
    }


    assignments.append(record)

    next_id += 1


    print("User added successfully.")


def validate_pdf(filename):

    return filename.lower().endswith(".pdf")


def upload_assignment():

    username = input("Enter username: ").strip()

    user = find_user(username)


    if not user:

        print("User not found.")
        return


    if user["file"] != "No assignment uploaded":

        print("Assignment already exists. Use replace option.")
        return


    filename = input("Enter PDF file name: ").strip()


    if not validate_pdf(filename):

        print("Only PDF files are allowed.")
        return


    user["file"] = filename


    print("Assignment uploaded successfully.")


def view_assignments():

    if not assignments:

        print("No records available.")
        return


    print("\n--- Assignment Records ---")


    for item in assignments:

        print("----------------------------")
        print("ID:", item["id"])
        print("Username:", item["username"])
        print("File:", item["file"])
        print("Date:", item["date"])


def replace_assignment():

    username = input("Enter username: ").strip()

    user = find_user(username)


    if not user:

        print("User not found.")
        return


    filename = input("Enter new PDF file name: ").strip()


    if not validate_pdf(filename):

        print("Only PDF files are allowed.")
        return


    user["file"] = filename


    print("Assignment replaced successfully.")


def remove_assignment():

    username = input("Enter username: ").strip()

    user = find_user(username)


    if not user:

        print("User not found.")
        return


    user["file"] = "No assignment uploaded"


    print("Assignment removed successfully.")


def search_users():

    keyword = input("Enter search keyword: ").strip().lower()


    found = False


    for item in assignments:

        if keyword in item["username"].lower():

            print("----------------------------")
            print("Username:", item["username"])
            print("Assignment:", item["file"])

            found = True


    if not found:

        print("No matching users found.")


def show_statistics():

    total = len(assignments)

    uploaded = 0


    for item in assignments:

        if item["file"] != "No assignment uploaded":

            uploaded += 1


    print("\n--- Statistics ---")
    print("Total Users:", total)
    print("Uploaded Assignments:", uploaded)
    print("Missing Assignments:", total - uploaded)


def main():

    running = True


    while running:

        show_menu()

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

            show_statistics()

        elif choice == "8":

            running = False
            print("Application closed.")

        else:

            print("Invalid option.")


if __name__ == "__main__":

    main()