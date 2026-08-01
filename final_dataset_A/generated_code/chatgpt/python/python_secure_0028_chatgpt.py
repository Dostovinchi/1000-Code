import datetime

evaluations = []
next_id = 1


def display_menu():
    print("\n===== Course Evaluation Manager =====")
    print("1. Submit Evaluation")
    print("2. View Evaluations")
    print("3. Update Evaluation")
    print("4. Delete Evaluation")
    print("5. Search Evaluations")
    print("6. Display Statistics")
    print("7. Exit")


def find_evaluation(evaluation_id):
    for evaluation in evaluations:
        if evaluation["id"] == evaluation_id:
            return evaluation
    return None


def submit_evaluation():
    global next_id

    student = input("Student name: ").strip()

    if not student:
        print("Student name cannot be empty.")
        return

    course = input("Course name: ").strip()

    if not course:
        print("Course name cannot be empty.")
        return

    try:
        rating = int(input("Rating (1-5): ").strip())
    except ValueError:
        print("Invalid rating.")
        return

    if rating < 1 or rating > 5:
        print("Rating must be between 1 and 5.")
        return

    comment = input("Comment: ").strip()

    evaluations.append({
        "id": next_id,
        "student": student,
        "course": course,
        "rating": rating,
        "comment": comment,
        "created_date": str(datetime.date.today())
    })

    next_id += 1

    print("Evaluation submitted successfully.")


def view_evaluations():

    if not evaluations:
        print("No evaluations available.")
        return

    for evaluation in evaluations:
        print("\n----------------------------")
        print("ID:", evaluation["id"])
        print("Student:", evaluation["student"])
        print("Course:", evaluation["course"])
        print("Rating:", evaluation["rating"])
        print("Comment:", evaluation["comment"])
        print("Created:", evaluation["created_date"])


def update_evaluation():

    try:
        evaluation_id = int(input("Evaluation ID: "))
    except ValueError:
        print("Invalid ID.")
        return

    evaluation = find_evaluation(evaluation_id)

    if evaluation is None:
        print("Evaluation not found.")
        return

    try:
        rating = int(input("New rating (1-5): "))
    except ValueError:
        print("Invalid rating.")
        return

    if rating < 1 or rating > 5:
        print("Rating must be between 1 and 5.")
        return

    evaluation["rating"] = rating
    evaluation["comment"] = input("New comment: ").strip()

    print("Evaluation updated successfully.")


def delete_evaluation():

    try:
        evaluation_id = int(input("Evaluation ID: "))
    except ValueError:
        print("Invalid ID.")
        return

    evaluation = find_evaluation(evaluation_id)

    if evaluation is None:
        print("Evaluation not found.")
        return

    evaluations.remove(evaluation)

    print("Evaluation deleted successfully.")


def search_evaluations():

    keyword = input("Enter keyword: ").strip().lower()

    found = False

    for evaluation in evaluations:

        if (keyword in evaluation["student"].lower()
                or keyword in evaluation["course"].lower()
                or keyword in evaluation["comment"].lower()):

            print("\n----------------------------")
            print("ID:", evaluation["id"])
            print("Student:", evaluation["student"])
            print("Course:", evaluation["course"])
            print("Rating:", evaluation["rating"])

            found = True

    if not found:
        print("No matching evaluations found.")


def display_statistics():

    print("\nStatistics")
    print("------------------")
    print("Total evaluations:", len(evaluations))

    if not evaluations:
        return

    total = sum(item["rating"] for item in evaluations)
    average = total / len(evaluations)

    print("Average rating:", round(average, 2))


def main():

    running = True

    while running:

        display_menu()

        choice = input("Choose option: ").strip()

        if choice == "1":
            submit_evaluation()

        elif choice == "2":
            view_evaluations()

        elif choice == "3":
            update_evaluation()

        elif choice == "4":
            delete_evaluation()

        elif choice == "5":
            search_evaluations()

        elif choice == "6":
            display_statistics()

        elif choice == "7":
            running = False
            print("Application closed.")

        else:
            print("Invalid option.")


if __name__ == "__main__":
    main()