from datetime import datetime


users = []
next_user_id = 1


def show_menu():
    print("\n===== User Profile Management System =====")
    print("1. Create User Profile")
    print("2. View All Profiles")
    print("3. Retrieve Profile")
    print("4. Update Description")
    print("5. Search Profiles")
    print("6. Delete Profile")
    print("7. Show Statistics")
    print("8. Exit")


def get_user(username):
    for user in users:
        if user["username"].lower() == username.lower():
            return user
    return None


def create_profile():
    global next_user_id

    print("\n--- Create Profile ---")

    username = input("Username: ").strip()

    if not username:
        print("Username is required.")
        return

    if get_user(username):
        print("Username already exists.")
        return

    name = input("Full name: ").strip()
    email = input("Email: ").strip()
    description = input("Profile description: ").strip()

    user = {
        "id": next_user_id,
        "username": username,
        "name": name,
        "email": email,
        "description": description,
        "created_date": datetime.now().strftime("%Y-%m-%d")
    }

    users.append(user)
    next_user_id += 1

    print("Profile created successfully.")


def display_profile(user):

    print("----------------------------")
    print("ID:", user["id"])
    print("Username:", user["username"])
    print("Name:", user["name"])
    print("Email:", user["email"])
    print("Description:", user["description"])
    print("Created:", user["created_date"])


def view_profiles():

    if not users:
        print("No profiles found.")
        return

    print("\n--- All Profiles ---")

    for user in users:
        display_profile(user)


def retrieve_profile():

    username = input("Enter username: ").strip()

    user = get_user(username)

    if user:
        display_profile(user)
    else:
        print("Profile not found.")


def update_description():

    username = input("Enter username: ").strip()

    user = get_user(username)

    if not user:
        print("Profile not found.")
        return

    new_description = input("New description: ").strip()

    if not new_description:
        print("Description cannot be empty.")
        return

    user["description"] = new_description

    print("Description updated successfully.")


def search_profiles():

    keyword = input("Search keyword: ").strip().lower()

    if not keyword:
        print("Keyword is required.")
        return

    found = False

    for user in users:

        if (
            keyword in user["username"].lower()
            or keyword in user["name"].lower()
            or keyword in user["description"].lower()
        ):
            display_profile(user)
            found = True

    if not found:
        print("No matching profiles found.")


def delete_profile():

    username = input("Enter username: ").strip()

    user = get_user(username)

    if not user:
        print("Profile not found.")
        return

    users.remove(user)

    print("Profile deleted successfully.")


def show_statistics():

    total = len(users)
    completed = 0

    for user in users:
        if user["description"]:
            completed += 1

    print("\n--- Statistics ---")
    print("Total Profiles:", total)
    print("Profiles With Description:", completed)
    print("Profiles Without Description:", total - completed)


def main():

    running = True

    while running:

        show_menu()

        choice = input("Select option: ").strip()

        if choice == "1":
            create_profile()

        elif choice == "2":
            view_profiles()

        elif choice == "3":
            retrieve_profile()

        elif choice == "4":
            update_description()

        elif choice == "5":
            search_profiles()

        elif choice == "6":
            delete_profile()

        elif choice == "7":
            show_statistics()

        elif choice == "8":
            running = False
            print("Application terminated.")

        else:
            print("Invalid option.")


if __name__ == "__main__":
    main()