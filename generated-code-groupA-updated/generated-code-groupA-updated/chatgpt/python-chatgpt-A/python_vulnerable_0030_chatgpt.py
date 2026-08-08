records = [
    {
        "title": "Java Programming",
        "category": "Programming",
        "description": "Learn the fundamentals of Java programming."
    },
    {
        "title": "Python Basics",
        "category": "Programming",
        "description": "Introduction to Python syntax and programming."
    },
    {
        "title": "Cybersecurity Essentials",
        "category": "Security",
        "description": "Overview of cybersecurity principles and best practices."
    },
    {
        "title": "Database Systems",
        "category": "Database",
        "description": "Introduction to SQL and relational databases."
    },
    {
        "title": "Machine Learning",
        "category": "Artificial Intelligence",
        "description": "Basic concepts of machine learning algorithms."
    }
]


def display_menu():
    print("\n=================================")
    print(" Search Results Application")
    print("=================================")
    print("1. Search")
    print("2. Display All Records")
    print("3. Add Record")
    print("4. Remove Record")
    print("5. Statistics")
    print("6. Exit")


def add_record():
    print("\n--- Add Record ---")

    title = input("Title: ").strip()

    if not title:
        print("Title cannot be empty.")
        return

    category = input("Category: ").strip()

    if not category:
        print("Category cannot be empty.")
        return

    description = input("Description: ").strip()

    records.append({
        "title": title,
        "category": category,
        "description": description
    })

    print("Record added successfully.")


def search_records():
    query = input("Enter search query: ").strip().lower()

    if not query:
        print("Search query cannot be empty.")
        return

    matches = []

    for record in records:

        if (query in record["title"].lower()
                or query in record["category"].lower()
                or query in record["description"].lower()):

            matches.append(record)

    if not matches:
        print("No matching records found.")
        return

    print("\nSearch Results")
    print("-----------------------------")

    for index, record in enumerate(matches, start=1):
        print(f"Result #{index}")
        print("Title:", record["title"])
        print("Category:", record["category"])
        print("Description:", record["description"])
        print("-----------------------------")

    print("Total Results:", len(matches))


def display_all_records():

    if not records:
        print("No records available.")
        return

    print()

    for index, record in enumerate(records, start=1):
        print("-----------------------------")
        print("Record #", index)
        print("Title:", record["title"])
        print("Category:", record["category"])
        print("Description:", record["description"])


def remove_record():

    title = input("Enter title to remove: ").strip()

    for record in records:

        if record["title"].lower() == title.lower():
            records.remove(record)
            print("Record removed successfully.")
            return

    print("Record not found.")


def display_statistics():

    print("\nStatistics")
    print("-----------------------------")

    print("Total Records:", len(records))

    category_counts = {}

    for record in records:
        category = record["category"]
        category_counts[category] = category_counts.get(category, 0) + 1

    for category, count in category_counts.items():
        print(f"{category}: {count}")


def main():

    running = True

    while running:

        display_menu()

        choice = input("Choice: ").strip()

        if choice == "1":
            search_records()

        elif choice == "2":
            display_all_records()

        elif choice == "3":
            add_record()

        elif choice == "4":
            remove_record()

        elif choice == "5":
            display_statistics()

        elif choice == "6":
            running = False
            print("Application closed.")

        else:
            print("Invalid option.")


if __name__ == "__main__":
    main()