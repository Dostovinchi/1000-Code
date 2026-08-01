files = []


def display_menu():
    print("\n===== Documents Folder Browser =====")
    print("1. Add File")
    print("2. View Files")
    print("3. Open File")
    print("4. Rename File")
    print("5. Delete File")
    print("6. Search Files")
    print("7. Exit")


def find_file(name):
    for index, file_name in enumerate(files):
        if file_name.lower() == name.lower():
            return index
    return -1


def add_file():
    name = input("Enter file name: ").strip()

    if not name:
        print("File name cannot be empty.")
        return

    if find_file(name) != -1:
        print("File already exists.")
        return

    files.append(name)
    print("File added successfully.")


def view_files():

    if not files:
        print("No files available.")
        return

    print("\n--- Documents Folder ---")

    for number, file_name in enumerate(files, start=1):
        print(f"{number}. {file_name}")


def open_file():

    name = input("Enter file name: ").strip()

    index = find_file(name)

    if index == -1:
        print("File not found.")
        return

    print("Opening:", files[index])


def rename_file():

    current = input("Enter current file name: ").strip()

    index = find_file(current)

    if index == -1:
        print("File not found.")
        return

    new_name = input("Enter new file name: ").strip()

    if not new_name:
        print("File name cannot be empty.")
        return

    files[index] = new_name
    print("File renamed successfully.")


def delete_file():

    name = input("Enter file name: ").strip()

    index = find_file(name)

    if index == -1:
        print("File not found.")
        return

    files.pop(index)
    print("File deleted successfully.")


def search_files():

    keyword = input("Enter keyword: ").strip().lower()

    found = False

    for file_name in files:

        if keyword in file_name.lower():
            print(file_name)
            found = True

    if not found:
        print("No matching files found.")


def main():

    running = True

    while running:

        display_menu()

        choice = input("Choose option: ").strip()

        if choice == "1":
            add_file()

        elif choice == "2":
            view_files()

        elif choice == "3":
            open_file()

        elif choice == "4":
            rename_file()

        elif choice == "5":
            delete_file()

        elif choice == "6":
            search_files()

        elif choice == "7":
            running = False
            print("Application closed.")

        else:
            print("Invalid option.")


if __name__ == "__main__":
    main()