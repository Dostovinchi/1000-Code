from datetime import datetime


resumes = []
next_id = 1


def display_menu():

    print("\n===== Resume Upload Management System =====")
    print("1. Add User")
    print("2. Upload Resume")
    print("3. View Resumes")
    print("4. Organize Resume Records")
    print("5. Replace Resume")
    print("6. Remove Resume")
    print("7. Search User")
    print("8. Show Statistics")
    print("9. Exit")


def find_user(username):

    for record in resumes:

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
        "resume": "No resume uploaded",
        "created": datetime.now().strftime("%Y-%m-%d")
    }


    resumes.append(record)

    next_id += 1


    print("User added successfully.")


def validate_resume(filename):

    return (
        filename.lower().endswith(".pdf")
        or filename.lower().endswith(".doc")
        or filename.lower().endswith(".docx")
    )


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

        print("Allowed formats: PDF, DOC, DOCX.")
        return


    user["resume"] = filename


    print("Resume uploaded successfully.")


def view_resumes():

    if not resumes:

        print("No resume records available.")
        return


    print("\n--- Resume Records ---")


    for record in resumes:

        print("----------------------------")
        print("ID:", record["id"])
        print("Username:", record["username"])
        print("Resume:", record["resume"])
        print("Created:", record["created"])


def organize_resumes():

    resumes.sort(key=lambda item: item["username"].lower())


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

    keyword = input("Enter search keyword: ").strip().lower()


    found = False


    for record in resumes:

        if keyword in record["username"].lower():

            print("----------------------------")
            print("Username:", record["username"])
            print("Resume:", record["resume"])

            found = True


    if not found:

        print("No matching users found.")


def show_statistics():

    total_users = len(resumes)
    uploaded = 0


    for record in resumes:

        if record["resume"] != "No resume uploaded":

            uploaded += 1


    print("\n--- Statistics ---")
    print("Total Users:", total_users)
    print("Uploaded Resumes:", uploaded)
    print("Missing Resumes:", total_users - uploaded)


def main():

    running = True


    while running:

        display_menu()

        choice = input("Choose option: ").strip()


        if choice == "1":

            add_user()

        elif choice == "2":

            upload_resume()

        elif choice == "3":

            view_resumes()

        elif choice == "4":

            organize_resumes()

        elif choice == "5":

            replace_resume()

        elif choice == "6":

            remove_resume()

        elif choice == "7":

            search_users()

        elif choice == "8":

            show_statistics()

        elif choice == "9":

            running = False
            print("Application closed.")

        else:

            print("Invalid option.")


if __name__ == "__main__":

    main()