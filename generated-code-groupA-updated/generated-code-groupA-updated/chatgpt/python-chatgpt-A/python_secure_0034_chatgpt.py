from datetime import datetime


users = []
next_user_id = 1


def display_menu():
    print("\n===== Profile Picture Management System =====")
    print("1. Add User")
    print("2. View User Profiles")
    print("3. Upload Profile Picture")
    print("4. Replace Profile Picture")
    print("5. Remove Profile Picture")
    print("6. Search Users")
    print("7. Delete User")
    print("8. Show Statistics")
    print("9. Exit")


def find_user(username):
    for user in users:
        if user["username"].lower() == username.lower():
            return user
    return None


def add_user():
    global next_user_id

    print("\n--- Add User ---")

    username = input("Enter username: ").strip()

    if not username:
        print("Username cannot be empty.")
        return

    if find_user(username):
        print("User already exists.")
        return

    user = {
        "id": next_user_id,
        "username": username,
        "profile_picture": "No picture uploaded",
        "created_date": datetime.now().strftime("%Y-%m-%d")
    }

    users.append(user)
    next_user_id += 1

    print("User added successfully.")


def view_users():

    if not users:
        print("No user records available.")
        return

    print("\n--- User Profiles ---")

    for user in users:
        print("----------------------------")
        print("ID:", user["id"])
        print("Username:", user["username"])
        print("Profile Picture:", user["profile_picture"])
        print("Created:", user["created_date"])


def upload_picture():

    username = input("Enter username: ").strip()

    user = find_user(username)

    if not user:
        print("User not found.")
        return

    if user["profile_picture"] != "No picture uploaded":
        print("Picture already exists. Use replace option.")
        return

    picture = input("Enter picture file name: ").strip()

    if not picture:
        print("Invalid picture name.")
        return

    user["profile_picture"] = picture

    print("Profile picture uploaded successfully.")


def replace_picture():

    username = input("Enter username: ").strip()

    user = find_user(username)

    if not user:
        print("User not found.")
        return

    picture = input("Enter new picture file name: ").strip()

    if not picture:
        print("Invalid picture name.")
        return

    user["profile_picture"] = picture

    print("Profile picture replaced successfully.")


def remove_picture():

    username = input("Enter username: ").strip()

    user = find_user(username)

    if not user:
        print("User not found.")
        return

    user["profile_picture"] = "No picture uploaded"

    print("Profile picture removed successfully.")


def search_users():

    keyword = input("Enter username keyword: ").strip().lower()

    if not keyword:
        print("Keyword cannot be empty.")
        return

    found = False

    for user in users:

        if keyword in user["username"].lower():

            print("----------------------------")
            print("Username:", user["username"])
            print("Picture:", user["profile_picture"])

            found = True

    if not found:
        print("No matching users found.")


def delete_user():

    username = input("Enter username: ").strip()

    user = find_user(username)

    if not user:
        print("User not found.")
        return

    users.remove(user)

    print("User deleted successfully.")


def show_statistics():

    total_users = len(users)
    users_with_pictures = 0

    for user in users:

        if user["profile_picture"] != "No picture uploaded":
            users_with_pictures += 1

    print("\n--- Statistics ---")
    print("Total Users:", total_users)
    print("Users With Pictures:", users_with_pictures)
    print("Users Without Pictures:", total_users - users_with_pictures)


def main():

    running = True

    while running:

        display_menu()

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
            search_users()

        elif choice == "7":
            delete_user()

        elif choice == "8":
            show_statistics()

        elif choice == "9":
            running = False
            print("Application closed.")

        else:
            print("Invalid option.")


if __name__ == "__main__":
    main()