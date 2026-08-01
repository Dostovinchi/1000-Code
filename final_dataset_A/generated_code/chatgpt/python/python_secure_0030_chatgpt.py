from datetime import datetime


records = [
    {
        "id": 1,
        "title": "Java Programming",
        "category": "Programming",
        "description": "Introduction to Java programming language.",
        "created": "2026-01-10"
    },
    {
        "id": 2,
        "title": "Python Basics",
        "category": "Programming",
        "description": "Learn Python syntax and basic programming concepts.",
        "created": "2026-01-15"
    },
    {
        "id": 3,
        "title": "Cybersecurity Essentials",
        "category": "Security",
        "description": "Overview of cybersecurity principles and practices.",
        "created": "2026-02-01"
    },
    {
        "id": 4,
        "title": "Database Systems",
        "category": "Database",
        "description": "Introduction to SQL and relational database systems.",
        "created": "2026-02-12"
    },
    {
        "id": 5,
        "title": "Machine Learning",
        "category": "Artificial Intelligence",
        "description": "Fundamentals of machine learning algorithms.",
        "created": "2026-03-05"
    }
]

next_id = 6


def show_menu():
    print("\n=================================")
    print(" Search Results Application")
    print("=================================")
    print("1. Search")
    print("2. Display All Records")
    print("3. Add Record")
    print("4. Update Record")
    print("5. Delete Record")
    print("6. Statistics")
    print("7. Exit")


def display_record(record):
    print("--------------------------------")
    print("ID:", record["id"])
    print("Title:", record["title"])
    print("Category:", record["category"])
    print("Description:", record["description"])
    print("Created:", record["created"])


def display_all_records():
    if not records:
        print("No records available.")
        return

    for record in records:
        display_record(record)


def search_records():
    query = input("Enter search query: ").strip().lower()

    if not query:
        print("Search query cannot be empty.")
        return

    matches = []

    for record in records:
        if (
            query in record["title"].lower()
            or query in record["category"].lower()
            or query in record["description"].lower()
        ):
            matches.append(record)

    if not matches:
        print("No matching records found.")
        return

    print("\nSearch Results")
    print("==============")

    for record in matches:
        display_record(record)

    print("Total Results:", len(matches))


def add_record():
    global next_id

    title = input("Title: ").strip()

    if not title:
        print("Title cannot be empty.")
        return

    category = input("Category: ").strip()

    if not category:
        print("Category cannot be empty.")
        return

    description = input("Description: ").strip()

    record = {
        "id": next_id,
        "title": title,
        "category": category,
        "description": description,
        "created": datetime.now().strftime("%Y-%m-%d")
    }

    records.append(record)

    next_id += 1

    print("Record added successfully.")


def update_record():
    try:
        record_id = int(input("Record ID: "))
    except ValueError:
        print("Invalid ID.")
        return

    for record in records:

        if record["id"] == record_id:

            title = input("New title: ").strip()
            category = input("New category: ").strip()
            description = input("New description: ").strip()

            if title:
                record["title"] = title

            if category:
                record["category"] = category

            if description:
                record["description"] = description

            print("Record updated successfully.")
            return

    print("Record not found.")


def delete_record():
    try:
        record_id = int(input("Record ID: "))
    except ValueError:
        print("Invalid ID.")
        return

    for record in records:
        if record["id"] == record_id:
            records.remove(record)
            print("Record deleted successfully.")
            return

    print("Record not found.")


def show_statistics():
    print("\nStatistics")
    print("==========")
    print("Total Records:", len(records))

    category_counts = {}

    longest_title = ""

    for record in records:

        category = record["category"]

        category_counts[category] = category_counts.get(category, 0) + 1

        if len(record["title"]) > len(longest_title):
            longest_title = record["title"]

    print("\nRecords by Category")

    for category, count in category_counts.items():
        print(category + ":", count)

    print("\nLongest Title:", longest_title)


def main():

    running = True

    while running:

        show_menu()

        choice = input("Choice: ").strip()

        if choice == "1":
            search_records()

        elif choice == "2":
            display_all_records()

        elif choice == "3":
            add_record()

        elif choice == "4":
            update_record()

        elif choice == "5":
            delete_record()

        elif choice == "6":
            show_statistics()

        elif choice == "7":
            running = False
            print("Application closed.")

        else:
            print("Invalid option.")


if __name__ == "__main__":
    main()