from datetime import datetime


faq_entries = []
next_id = 1


def display_menu():
    print("\n===== FAQ Management System =====")
    print("1. Submit New Question")
    print("2. View All Questions")
    print("3. Search Questions")
    print("4. Update FAQ Entry")
    print("5. Delete FAQ Entry")
    print("6. Display Statistics")
    print("7. Exit")


def find_question(question_id):
    for entry in faq_entries:
        if entry["id"] == question_id:
            return entry
    return None


def submit_question():
    global next_id

    print("\n--- Submit FAQ Question ---")

    question = input("Enter question: ").strip()

    if not question:
        print("Question cannot be empty.")
        return

    category = input("Enter category: ").strip()

    if not category:
        category = "General"

    answer = input("Enter answer (optional): ").strip()

    entry = {
        "id": next_id,
        "question": question,
        "category": category,
        "answer": answer,
        "status": "Pending" if not answer else "Answered",
        "created_date": datetime.now().strftime("%Y-%m-%d")
    }

    faq_entries.append(entry)

    next_id += 1

    print("FAQ entry added successfully.")


def view_questions():

    if not faq_entries:
        print("No FAQ entries available.")
        return

    print("\n--- FAQ Entries ---")

    for entry in faq_entries:

        print("-----------------------------")
        print("ID:", entry["id"])
        print("Question:", entry["question"])
        print("Category:", entry["category"])
        print("Answer:", entry["answer"])
        print("Status:", entry["status"])
        print("Created:", entry["created_date"])


def search_questions():

    keyword = input("Enter search keyword: ").strip().lower()

    if not keyword:
        print("Keyword cannot be empty.")
        return

    found = False

    for entry in faq_entries:

        if (keyword in entry["question"].lower()
                or keyword in entry["category"].lower()
                or keyword in entry["answer"].lower()):

            print("-----------------------------")
            print("ID:", entry["id"])
            print("Question:", entry["question"])
            print("Category:", entry["category"])
            print("Status:", entry["status"])

            found = True

    if not found:
        print("No matching FAQ entries found.")


def update_entry():

    try:
        question_id = int(input("Enter FAQ ID: "))

    except ValueError:
        print("Invalid ID.")
        return


    entry = find_question(question_id)

    if entry is None:
        print("FAQ entry not found.")
        return


    print("Leave fields empty to keep current values.")

    new_question = input("New question: ").strip()

    if new_question:
        entry["question"] = new_question


    new_category = input("New category: ").strip()

    if new_category:
        entry["category"] = new_category


    new_answer = input("New answer: ").strip()

    if new_answer:
        entry["answer"] = new_answer
        entry["status"] = "Answered"


    print("FAQ entry updated successfully.")


def delete_entry():

    try:
        question_id = int(input("Enter FAQ ID: "))

    except ValueError:
        print("Invalid ID.")
        return


    entry = find_question(question_id)

    if entry is None:
        print("FAQ entry not found.")
        return


    faq_entries.remove(entry)

    print("FAQ entry deleted successfully.")


def display_statistics():

    total = len(faq_entries)

    answered = 0
    pending = 0


    for entry in faq_entries:

        if entry["status"] == "Answered":
            answered += 1
        else:
            pending += 1


    print("\n--- Statistics ---")
    print("Total FAQ Entries:", total)
    print("Answered:", answered)
    print("Pending:", pending)


def main():

    running = True

    while running:

        display_menu()

        choice = input("Choose option: ").strip()


        if choice == "1":
            submit_question()

        elif choice == "2":
            view_questions()

        elif choice == "3":
            search_questions()

        elif choice == "4":
            update_entry()

        elif choice == "5":
            delete_entry()

        elif choice == "6":
            display_statistics()

        elif choice == "7":
            running = False
            print("Application closed.")

        else:
            print("Invalid option.")


if __name__ == "__main__":
    main()