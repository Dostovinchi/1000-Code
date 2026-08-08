from datetime import datetime


users = []
next_id = 1


def display_menu():
    print("\n===== User Profile Management System =====")
    print("1. Add User Profile")
    print("2. View All Profiles")
    print("3. Retrieve Profile Information")
    print("4. Update Profile Description")
    print("5. Search Users")
    print("6. Delete User")
    print("7. Display Statistics")
    print("8. Exit")


def find_user(username):
    for user in users:
        if user["username"].lower() == username.lower():
            return user
    return None


def add_user():
    global next_id

    print("\n--- Add User Profile ---")

    username = input("Enter username: ").strip()

    if not username:
        print("Username cannot be empty.")
        return

    if find_user(username):
        print("Username already exists.")
        return

    full_name = input("Enter full name: ").strip()
    email = input("Enter email: ").strip()
    description = input("Enter profile description: ").strip()

    user = {
        "id": next_id,
        "username": username,
        "name": full_name,
        "email": email,
        "description": description,
        "created": datetime.now().strftime("%Y-%m-%d")
    }

    users.append(user)

    next_id += 1

    print("Profile created successfully.")


def display_profiles():

    if not users:
        print("No profiles available.")
        return

    print("\n--- User Profiles ---")

    for user in users:
        print("----------------------------")
        print("ID:", user["id"])
        print("Username:", user["username"])
        print("Name:", user["name"])
        print("Email:", user["email"])
        print("Description:", user["description"])
        print("Created:", user["created"])


def retrieve_profile():

    username = input("Enter username: ").strip()

    user = find_user(username)

    if user is None:
        print("User not found.")
        return

    print("\n--- Profile Information ---")
    print("ID:", user["id"])
    print("Username:", user["username"])
    print("Name:", user["name"])
    print("Email:", user["email"])
    print("Description:", user["description"])
    print("Created:", user["created"])


def update_description():

    username = input("Enter username: ").strip()

    user = find_user(username)

    if user is None:
        print("User not found.")
        return

    new_description = input("Enter new description: ").strip()

    if not new_description:
        print("Description cannot be empty.")
        return

    user["description"] = new_description

    print("Description updated successfully.")


def search_users():

    keyword = input("Enter search keyword: ").strip().lower()

    if not keyword:
        print("Keyword cannot be empty.")
        return

    found = False

    for user in users:

        if (keyword in user["username"].lower()
                or keyword in user["name"].lower()
                or keyword in user["description"].lower()):

            print("----------------------------")
            print("Username:", user["username"])
            print("Name:", user["name"])
            print("Description:", user["description"])

            found = True

    if not found:
        print("No matching users found.")


def delete_user():

    username = input("Enter username to delete: ").strip()

    user = find_user(username)

    if user is None:
        print("User not found.")
        return

    users.remove(user)

    print("User deleted successfully.")


def display_statistics():

    total = len(users)

    descriptions = 0

    for user in users:
        if user["description"]:
            descriptions += 1

    print("\n--- Statistics ---")
    print("Total Users:", total)
    print("Users With Description:", descriptions)
    print("Users Without Description:", total - descriptions)


def main():

    running = True

    while running:

        display_menu()

        choice = input("Choose option: ").strip()

        if choice == "1":
            add_user()

        elif choice == "2":
            display_profiles()

        elif choice == "3":
            retrieve_profile()

        elif choice == "4":
            update_description()

        elif choice == "5":
            search_users()

        elif choice == "6":
            delete_user()

        elif choice == "7":
            display_statistics()

        elif choice == "8":
            running = False
            print("Application closed.")

        else:
            print("Invalid option.")


if __name__ == "__main__":
    main()