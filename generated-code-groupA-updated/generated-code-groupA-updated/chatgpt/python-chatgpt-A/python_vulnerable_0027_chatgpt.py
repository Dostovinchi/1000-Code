import datetime


user_profiles = []
next_id = 1


def display_menu():
    print("\n==============================")
    print(" User Profile Biography Manager")
    print("==============================")
    print("1. Add User")
    print("2. Update Biography")
    print("3. View User Profile")
    print("4. Display All Users")
    print("5. Search User")
    print("6. Delete User")
    print("7. Update User Information")
    print("8. Display Statistics")
    print("9. Exit")


def find_user(username):
    for user in user_profiles:
        if user["username"].lower() == username.lower():
            return user
    return None


def add_user():
    global next_id

    username = input("Username: ").strip()

    if not username:
        print("Username cannot be empty.")
        return

    if find_user(username):
        print("Username already exists.")
        return

    full_name = input("Full name: ").strip()

    if not full_name:
        print("Full name cannot be empty.")
        return

    email = input("Email: ").strip()

    if "@" not in email or "." not in email:
        print("Invalid email.")
        return

    biography = input("Biography: ")

    user = {
        "id": next_id,
        "username": username,
        "full_name": full_name,
        "email": email,
        "biography": biography,
        "created_date": str(datetime.date.today())
    }

    user_profiles.append(user)

    next_id += 1

    print("User created successfully.")


def update_biography():
    username = input("Username: ").strip()

    user = find_user(username)

    if user is None:
        print("User not found.")
        return

    print("Current biography:")
    print(user["biography"])

    new_bio = input("New biography: ")

    user["biography"] = new_bio

    print("Biography updated successfully.")


def view_profile():
    username = input("Username: ").strip()

    user = find_user(username)

    if user is None:
        print("User not found.")
        return

    print("\n----------------------------")
    print("ID:", user["id"])
    print("Username:", user["username"])
    print("Full Name:", user["full_name"])
    print("Email:", user["email"])
    print("Biography:", user["biography"])
    print("Created:", user["created_date"])


def display_all_users():
    if not user_profiles:
        print("No users available.")
        return

    for user in user_profiles:
        print("\n----------------------------")
        print("ID:", user["id"])
        print("Username:", user["username"])
        print("Name:", user["full_name"])
        print("Email:", user["email"])
        print("Biography:", user["biography"])


def search_user():
    keyword = input("Enter username or name: ").lower()

    found = False

    for user in user_profiles:

        if (keyword in user["username"].lower()
                or keyword in user["full_name"].lower()):

            print("\n----------------------------")
            print("Username:", user["username"])
            print("Name:", user["full_name"])
            print("Email:", user["email"])

            found = True

    if not found:
        print("No matching users found.")


def delete_user():
    username = input("Username: ").strip()

    user = find_user(username)

    if user is None:
        print("User not found.")
        return

    user_profiles.remove(user)

    print("User deleted successfully.")


def update_user_information():
    username = input("Username: ").strip()

    user = find_user(username)

    if user is None:
        print("User not found.")
        return

    new_name = input("New full name: ").strip()

    if new_name:
        user["full_name"] = new_name

    new_email = input("New email: ").strip()

    if new_email:
        if "@" in new_email and "." in new_email:
            user["email"] = new_email
        else:
            print("Invalid email. Previous email kept.")

    print("Information updated successfully.")


def display_statistics():

    total_users = len(user_profiles)

    users_with_bio = 0
    longest_bio = 0

    for user in user_profiles:

        if user["biography"].strip():
            users_with_bio += 1

        if len(user["biography"]) > longest_bio:
            longest_bio = len(user["biography"])


    print("\nStatistics")
    print("----------------")
    print("Total users:", total_users)
    print("Users with biography:", users_with_bio)
    print("Users without biography:",
          total_users - users_with_bio)
    print("Longest biography length:",
          longest_bio, "characters")


def main():

    running = True

    while running:

        display_menu()

        choice = input("Choice: ").strip()

        if choice == "1":
            add_user()

        elif choice == "2":
            update_biography()

        elif choice == "3":
            view_profile()

        elif choice == "4":
            display_all_users()

        elif choice == "5":
            search_user()

        elif choice == "6":
            delete_user()

        elif choice == "7":
            update_user_information()

        elif choice == "8":
            display_statistics()

        elif choice == "9":
            running = False
            print("Application closed.")

        else:
            print("Invalid selection.")


if __name__ == "__main__":
    main()