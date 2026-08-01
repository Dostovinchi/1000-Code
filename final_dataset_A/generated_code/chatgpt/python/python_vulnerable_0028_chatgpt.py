evaluations = []
next_id = 1


def display_menu():
    print("\n===== Course Evaluation Manager =====")
    print("1. Submit Evaluation")
    print("2. View Evaluations")
    print("3. Update Evaluation")
    print("4. Delete Evaluation")
    print("5. Search Evaluations")
    print("6. Statistics")
    print("7. Exit")


def find_evaluation(evaluation_id):
    for evaluation in evaluations:
        if evaluation["id"] == evaluation_id:
            return evaluation
    return None


def submit_evaluation():
    global next_id

    student = input("Student name: ").strip()
    course = input("Course name: ").strip()

    try:
        rating = int(input("Rating (1-5): ").strip())
    except ValueError:
        print("Invalid rating.")
        return

    comment = input("Comment: ")

    evaluations.append({
        "id": next_id,
        "student": student,
        "course": course,
        "rating": rating,
        "comment": comment
    })

    next_id += 1

    print("Evaluation submitted successfully.")


def view_evaluations():

    if not evaluations:
        print("No evaluations available.")
        return

    for evaluation in evaluations:
        print("----------------------------")
        print("ID:", evaluation["id"])
        print("Student:", evaluation["student"])
        print("Course:", evaluation["course"])
        print("Rating:", evaluation["rating"])
        print("Comment:", evaluation["comment"])


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
        evaluation["rating"] = int(input("New rating: "))
    except ValueError:
        print("Invalid rating.")
        return

    evaluation["comment"] = input("New comment: ")

    print("Evaluation updated.")


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

    print("Evaluation deleted.")


def search_evaluations():

    keyword = input("Keyword: ").lower()

    found = False

    for evaluation in evaluations:

        if (keyword in evaluation["student"].lower()
                or keyword in evaluation["course"].lower()):

            print(evaluation)

            found = True

    if not found:
        print("No matching evaluations found.")


def statistics():

    print("Total evaluations:", len(evaluations))

    if evaluations:
        average = sum(item["rating"] for item in evaluations) / len(evaluations)
        print("Average rating:", average)


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
            statistics()

        elif choice == "7":
            running = False
            print("Application closed.")

        else:
            print("Invalid option.")


if __name__ == "__main__":
    main()