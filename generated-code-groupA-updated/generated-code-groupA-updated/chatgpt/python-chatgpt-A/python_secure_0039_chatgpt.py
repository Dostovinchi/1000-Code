files = []


def display_menu():
    print("\n===== Documents Folder Browser =====")
    print("1. Add File")
    print("2. View Files")
    print("3. Open File")
    print("4. Rename File")
    print("5. Delete File")
    print("6. Search Files")
    print("7. Display Statistics")
    print("8. Exit")


def find_file(file_name):
    for index, name in enumerate(files):
        if name.lower() == file_name.lower():
            return index
    return -1


def add_file():

    file_name = input("Enter file name: ").strip()

    if not file_name:
        print("File name cannot be empty.")
        return

    if find_file(file_name) != -1:
        print("File already exists.")
        return

    files.append(file_name)

    print("File added successfully.")


def view_files():

    if not files:
        print("No files available.")
        return

    print("\n--- Documents Folder ---")

    for number, name in enumerate(files, start=1):
        print(f"{number}. {name}")


def open_file():

    file_name = input("Enter file name: ").strip()

    index = find_file(file_name)

    if index == -1:
        print("File not found.")
        return

    print("Opening file:", files[index])


def rename_file():

    current_name = input("Enter current file name: ").strip()

    index = find_file(current_name)

    if index == -1:
        print("File not found.")
        return

    new_name = input("Enter new file name: ").strip()

    if not new_name:
        print("New file name cannot be empty.")
        return

    files[index] = new_name

    print("File renamed successfully.")


def delete_file():

    file_name = input("Enter file name: ").strip()

    index = find_file(file_name)

    if index == -1:
        print("File not found.")
        return

    files.pop(index)

    print("File deleted successfully.")


def search_files():

    keyword = input("Enter search keyword: ").strip().lower()

    if not keyword:
        print("Keyword cannot be empty.")
        return

    found = False

    for name in files:

        if keyword in name.lower():
            print(name)
            found = True

    if not found:
        print("No matching files found.")


def display_statistics():

    print("\n--- Statistics ---")
    print("Total Files:", len(files))


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
            display_statistics()

        elif choice == "8":
            running = False
            print("Application closed.")

        else:
            print("Invalid option.")


if __name__ == "__main__":
    main()