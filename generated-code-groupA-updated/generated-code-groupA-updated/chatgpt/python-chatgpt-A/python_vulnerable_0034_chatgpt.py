from datetime import datetime


users = []
next_id = 1


def show_menu():
    print("\n===== Profile Picture Management System =====")
    print("1. Add User")
    print("2. View Users")
    print("3. Upload Profile Picture")
    print("4. Replace Profile Picture")
    print("5. Remove Profile Picture")
    print("6. Search User")
    print("7. Delete User")
    print("8. Exit")


def find_user(username):

    for user in users:

        if user["username"].lower() == username.lower():
            return user

    return None


def add_user():

    global next_id

    print("\n--- Add User ---")

    username = input("Username: ").strip()


    if not username:
        print("Username cannot be empty.")
        return


    if find_user(username):
        print("User already exists.")
        return


    user = {
        "id": next_id,
        "username": username,
        "picture": "No picture uploaded",
        "created": datetime.now().strftime("%Y-%m-%d")
    }


    users.append(user)

    next_id += 1


    print("User added successfully.")


def view_users():

    if not users:

        print("No users available.")
        return


    print("\n--- User Records ---")


    for user in users:

        print("----------------------")
        print("ID:", user["id"])
        print("Username:", user["username"])
        print("Picture:", user["picture"])
        print("Created:", user["created"])


def upload_picture():

    username = input("Username: ").strip()

    user = find_user(username)


    if not user:

        print("User not found.")
        return


    if user["picture"] != "No picture uploaded":

        print("Picture already exists. Use replace option.")
        return


    picture = input("Picture file name: ").strip()


    if not picture:

        print("Invalid picture name.")
        return


    user["picture"] = picture


    print("Picture uploaded successfully.")


def replace_picture():

    username = input("Username: ").strip()

    user = find_user(username)


    if not user:

        print("User not found.")
        return


    picture = input("New picture file name: ").strip()


    if not picture:

        print("Invalid picture name.")
        return


    user["picture"] = picture


    print("Picture replaced successfully.")


def remove_picture():

    username = input("Username: ").strip()

    user = find_user(username)


    if not user:

        print("User not found.")
        return


    user["picture"] = "No picture uploaded"


    print("Picture removed successfully.")


def search_user():

    keyword = input("Search username: ").strip().lower()


    found = False


    for user in users:

        if keyword in user["username"].lower():

            print("----------------------")
            print("Username:", user["username"])
            print("Picture:", user["picture"])

            found = True


    if not found:

        print("No matching users found.")


def delete_user():

    username = input("Username: ").strip()

    user = find_user(username)


    if not user:

        print("User not found.")
        return


    users.remove(user)


    print("User deleted successfully.")


def display_statistics():

    total = len(users)

    uploaded = 0


    for user in users:

        if user["picture"] != "No picture uploaded":

            uploaded += 1


    print("\n--- Statistics ---")
    print("Total Users:", total)
    print("Users With Pictures:", uploaded)
    print("Users Without Pictures:", total - uploaded)


def main():

    running = True


    while running:

        show_menu()

        choice = input("Choose option: ").strip()


        if choice == "1":

            add_user()

        elif choice == "2":

            view_users()

        elif choice == "3":

            upload_picture()

        elif choice == "4":

            replace_picture()

        elif choice == "5":

            remove_picture()

        elif choice == "6":

            search_user()

        elif choice == "7":

            delete_user()

        elif choice == "8":

            running = False
            print("Application closed.")

        else:

            print("Invalid option.")


if __name__ == "__main__":

    main()