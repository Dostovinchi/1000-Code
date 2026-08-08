from datetime import datetime


resume_records = []
next_id = 1


def show_menu():
    print("\n===== Resume Upload Management System =====")
    print("1. Add User")
    print("2. Upload Resume")
    print("3. View Resume Records")
    print("4. Organize Resume Records")
    print("5. Replace Resume")
    print("6. Remove Resume")
    print("7. Search Users")
    print("8. Display Statistics")
    print("9. Exit")


def find_user(username):
    for record in resume_records:
        if record["username"].lower() == username.lower():
            return record
    return None


def add_user():
    global next_id

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
        "resume": "No resume uploaded",
        "created_date": datetime.now().strftime("%Y-%m-%d")
    }

    resume_records.append(record)
    next_id += 1

    print("User added successfully.")


def validate_resume(filename):
    extensions = [".pdf", ".doc", ".docx"]

    for extension in extensions:
        if filename.lower().endswith(extension):
            return True

    return False


def upload_resume():
    username = input("Enter username: ").strip()

    user = find_user(username)

    if not user:
        print("User not found.")
        return

    if user["resume"] != "No resume uploaded":
        print("Resume already exists. Use replace option.")
        return

    filename = input("Enter resume filename: ").strip()

    if not validate_resume(filename):
        print("Only PDF, DOC, and DOCX files are accepted.")
        return

    user["resume"] = filename

    print("Resume uploaded successfully.")


def view_resumes():

    if not resume_records:
        print("No records available.")
        return

    print("\n--- Resume Records ---")

    for record in resume_records:
        print("----------------------------")
        print("ID:", record["id"])
        print("Username:", record["username"])
        print("Resume:", record["resume"])
        print("Created:", record["created_date"])


def organize_records():

    resume_records.sort(
        key=lambda record: record["username"].lower()
    )

    print("Resume records organized successfully.")


def replace_resume():

    username = input("Enter username: ").strip()

    user = find_user(username)

    if not user:
        print("User not found.")
        return

    filename = input("Enter new resume filename: ").strip()

    if not validate_resume(filename):
        print("Invalid resume format.")
        return

    user["resume"] = filename

    print("Resume replaced successfully.")


def remove_resume():

    username = input("Enter username: ").strip()

    user = find_user(username)

    if not user:
        print("User not found.")
        return

    user["resume"] = "No resume uploaded"

    print("Resume removed successfully.")


def search_users():

    keyword = input("Enter username keyword: ").strip().lower()

    found = False

    for record in resume_records:

        if keyword in record["username"].lower():

            print("----------------------------")
            print("Username:", record["username"])
            print("Resume:", record["resume"])

            found = True

    if not found:
        print("No users found.")


def display_statistics():

    total = len(resume_records)
    uploaded = 0

    for record in resume_records:

        if record["resume"] != "No resume uploaded":
            uploaded += 1

    print("\n--- Statistics ---")
    print("Total Users:", total)
    print("Uploaded Resumes:", uploaded)
    print("Missing Resumes:", total - uploaded)


def main():

    running = True

    while running:

        show_menu()

        choice = input("Choose option: ").strip()

        if choice == "1":
            add_user()

        elif choice == "2":
            upload_resume()

        elif choice == "3":
            view_resumes()

        elif choice == "4":
            organize_records()

        elif choice == "5":
            replace_resume()

        elif choice == "6":
            remove_resume()

        elif choice == "7":
            search_users()

        elif choice == "8":
            display_statistics()

        elif choice == "9":
            running = False
            print("Application closed.")

        else:
            print("Invalid option.")


if __name__ == "__main__":
    main()