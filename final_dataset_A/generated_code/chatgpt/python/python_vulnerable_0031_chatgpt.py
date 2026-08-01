from datetime import datetime


faq_entries = []
next_id = 1


def show_menu():
    print("\n===== FAQ Submission System =====")
    print("1. Submit New Question")
    print("2. View All Questions")
    print("3. Search Questions")
    print("4. Update Question Status")
    print("5. Delete Question")
    print("6. Exit")


def add_question():
    global next_id

    print("\n--- Submit FAQ Question ---")

    question = input("Enter question: ").strip()

    if not question:
        print("Question cannot be empty.")
        return

    category = input("Enter category: ").strip()

    if not category:
        category = "General"

    entry = {
        "id": next_id,
        "question": question,
        "category": category,
        "status": "Pending",
        "created": datetime.now().strftime("%Y-%m-%d")
    }

    faq_entries.append(entry)

    next_id += 1

    print("Question submitted successfully.")


def display_questions():

    if not faq_entries:
        print("No FAQ entries available.")
        return

    print("\n--- FAQ Questions ---")

    for entry in faq_entries:
        print("----------------------")
        print("ID:", entry["id"])
        print("Question:", entry["question"])
        print("Category:", entry["category"])
        print("Status:", entry["status"])
        print("Created:", entry["created"])


def search_questions():

    keyword = input("Enter search keyword: ").strip().lower()

    if not keyword:
        print("Keyword cannot be empty.")
        return

    found = False

    for entry in faq_entries:

        if (keyword in entry["question"].lower()
                or keyword in entry["category"].lower()):

            print("----------------------")
            print("ID:", entry["id"])
            print("Question:", entry["question"])
            print("Category:", entry["category"])
            print("Status:", entry["status"])

            found = True

    if not found:
        print("No matching questions found.")


def update_status():

    try:
        question_id = int(input("Enter question ID: "))
    except ValueError:
        print("Invalid ID.")
        return


    for entry in faq_entries:

        if entry["id"] == question_id:

            new_status = input(
                "Enter new status (Pending/Answered): "
            ).strip()

            if new_status:
                entry["status"] = new_status
                print("Status updated successfully.")
            return

    print("Question not found.")


def delete_question():

    try:
        question_id = int(input("Enter question ID: "))
    except ValueError:
        print("Invalid ID.")
        return


    for entry in faq_entries:

        if entry["id"] == question_id:

            faq_entries.remove(entry)

            print("Question deleted successfully.")
            return

    print("Question not found.")


def main():

    running = True

    while running:

        show_menu()

        choice = input("Choose option: ").strip()

        if choice == "1":
            add_question()

        elif choice == "2":
            display_questions()

        elif choice == "3":
            search_questions()

        elif choice == "4":
            update_status()

        elif choice == "5":
            delete_question()

        elif choice == "6":
            running = False
            print("Application closed.")

        else:
            print("Invalid option.")


if __name__ == "__main__":
    main()