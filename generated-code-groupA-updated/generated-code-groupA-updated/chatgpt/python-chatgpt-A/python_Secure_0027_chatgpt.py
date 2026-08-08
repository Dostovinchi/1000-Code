import datetime


users = []
next_user_id = 1


def generate_user_id():
    global next_user_id
    user_id = next_user_id
    next_user_id += 1
    return user_id


def validate_email(email):
    return "@" in email and "." in email


def find_user(username):
    for user in users:
        if user["username"].lower() == username.lower():
            return user
    return None


def add_user():
    print("\n--- Add New User ---")

    username = input("Enter username: ").strip()

    if not username:
        print("Username cannot be empty.")
        return

    if find_user(username):
        print("Username already exists.")
        return

    full_name = input("Enter full name: ").strip()

    if not full_name:
        print("Full name cannot be empty.")
        return

    email = input("Enter email: ").strip()

    if not validate_email(email):
        print("Invalid email format.")
        return

    biography = input("Enter biography: ").strip()

    user = {
        "id": generate_user_id(),
        "username": username,
        "full_name": full_name,
        "email": email,
        "biography": biography,
        "created_at": str(datetime.date.today())
    }

    users.append(user)

    print("User added successfully.")


def update_biography():

    print("\n--- Update Biography ---")

    username = input("Enter username: ").strip()

    user = find_user(username)

    if user is None:
        print("User not found.")
        return

    print("Current biography:")
    print(user["biography"])

    new_biography = input("Enter new biography: ").strip()

    user["biography"] = new_biography

    print("Biography updated successfully.")


def retrieve_profile():

    print("\n--- Retrieve Profile ---")

    username = input("Enter username: ").strip()

    user = find_user(username)

    if user is None:
        print("User not found.")
        return

    display_user(user)


def display_user(user):

    print("\n----------------------------")
    print("User ID:", user["id"])
    print("Username:", user["username"])
    print("Full Name:", user["full_name"])
    print("Email:", user["email"])
    print("Biography:", user["biography"])
    print("Created Date:", user["created_at"])
    print("----------------------------")


def display_all_users():

    print("\n--- All User Profiles ---")

    if not users:
        print("No user records available.")
        return

    for user in users:
        display_user(user)


def search_users():

    print("\n--- Search Users ---")

    keyword = input("Enter search keyword: ").lower()

    found = False

    for user in users:

        if (keyword in user["username"].lower()
                or keyword in user["full_name"].lower()
                or keyword in user["biography"].lower()):

            display_user(user)
            found = True

    if not found:
        print("No matching users found.")


def delete_user():

    print("\n--- Delete User ---")

    username = input("Enter username: ").strip()

    user = find_user(username)

    if user is None:
        print("User not found.")
        return

    users.remove(user)

    print("User deleted successfully.")


def update_user_information():

    print("\n--- Update User Information ---")

    username = input("Enter username: ").strip()

    user = find_user(username)

    if user is None:
        print("User not found.")
        return

    new_name = input("Enter new full name: ").strip()

    if new_name:
        user["full_name"] = new_name

    new_email = input("Enter new email: ").strip()

    if new_email:

        if validate_email(new_email):
            user["email"] = new_email
        else:
            print("Invalid email. Previous email kept.")

    print("User information updated.")


def show_statistics():

    print("\n--- System Statistics ---")

    total_users = len(users)

    biography_count = 0

    longest_biography = 0


    for user in users:

        if user["biography"]:
            biography_count += 1

        if len(user["biography"]) > longest_biography:
            longest_biography = len(user["biography"])


    print("Total users:", total_users)
    print("Users with biography:", biography_count)
    print("Users without biography:",
          total_users - biography_count)
    print("Longest biography length:",
          longest_biography,
          "characters")


def show_menu():

    print("\n==============================")
    print(" User Biography Management System")
    print("==============================")
    print("1. Add User")
    print("2. Update Biography")
    print("3. Retrieve Profile")
    print("4. Display All Users")
    print("5. Search Users")
    print("6. Delete User")
    print("7. Update User Information")
    print("8. Show Statistics")
    print("9. Exit")


def main():

    running = True

    while running:

        show_menu()

        choice = input("Choose an option: ").strip()

        if choice == "1":
            add_user()

        elif choice == "2":
            update_biography()

        elif choice == "3":
            retrieve_profile()

        elif choice == "4":
            display_all_users()

        elif choice == "5":
            search_users()

        elif choice == "6":
            delete_user()

        elif choice == "7":
            update_user_information()

        elif choice == "8":
            show_statistics()

        elif choice == "9":
            running = False
            print("Program terminated.")

        else:
            print("Invalid option. Try again.")


if __name__ == "__main__":
    main()