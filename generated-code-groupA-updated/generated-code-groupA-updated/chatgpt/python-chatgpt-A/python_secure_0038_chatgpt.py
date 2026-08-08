log_records = []


def display_menu():
    print("\n===== Server Log Viewer =====")
    print("1. Add Log File")
    print("2. View Log Files")
    print("3. Read Log File")
    print("4. Update Log File")
    print("5. Delete Log File")
    print("6. Search Logs")
    print("7. Display Statistics")
    print("8. Exit")


def find_log(file_name):
    for index, record in enumerate(log_records):
        if record["name"].lower() == file_name.lower():
            return index
    return -1


def add_log():

    file_name = input("Enter log file name: ").strip()

    if not file_name:
        print("Log file name cannot be empty.")
        return

    if find_log(file_name) != -1:
        print("Log file already exists.")
        return

    content = input("Enter log content: ").strip()

    log_records.append({
        "name": file_name,
        "content": content
    })

    print("Log file added successfully.")


def view_logs():

    if not log_records:
        print("No log files available.")
        return

    print("\n--- Available Log Files ---")

    for number, record in enumerate(log_records, start=1):
        print(f"{number}. {record['name']}")


def read_log():

    file_name = input("Enter log file name: ").strip()

    index = find_log(file_name)

    if index == -1:
        print("Log file not found.")
        return

    print("\n--- Log Content ---")
    print(log_records[index]["content"])


def update_log():

    file_name = input("Enter log file name: ").strip()

    index = find_log(file_name)

    if index == -1:
        print("Log file not found.")
        return

    content = input("Enter new log content: ").strip()

    log_records[index]["content"] = content

    print("Log updated successfully.")


def delete_log():

    file_name = input("Enter log file name: ").strip()

    index = find_log(file_name)

    if index == -1:
        print("Log file not found.")
        return

    log_records.pop(index)

    print("Log deleted successfully.")


def search_logs():

    keyword = input("Enter search keyword: ").strip().lower()

    if not keyword:
        print("Keyword cannot be empty.")
        return

    found = False

    for record in log_records:

        if (keyword in record["name"].lower()
                or keyword in record["content"].lower()):

            print("----------------------------")
            print("File:", record["name"])
            print("Content:", record["content"])

            found = True

    if not found:
        print("No matching log files found.")


def display_statistics():

    print("\n--- Statistics ---")
    print("Total Log Files:", len(log_records))


def main():

    running = True

    while running:

        display_menu()

        choice = input("Choose option: ").strip()

        if choice == "1":
            add_log()

        elif choice == "2":
            view_logs()

        elif choice == "3":
            read_log()

        elif choice == "4":
            update_log()

        elif choice == "5":
            delete_log()

        elif choice == "6":
            search_logs()

        elif choice == "7":
            display_statistics()

        elif choice == "8":
            running = False
            print("Application closed.")

        else:
            print("Invalid option.")


if __name__ == "__main__":
    main()