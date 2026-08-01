from datetime import datetime


testimonials = []
next_id = 1


def show_menu():
    print("\n===== Customer Testimonials Management =====")
    print("1. View All Testimonials")
    print("2. Add New Testimonial")
    print("3. Retrieve Testimonial")
    print("4. Search Testimonials")
    print("5. Update Testimonial")
    print("6. Delete Testimonial")
    print("7. Display Statistics")
    print("8. Exit")


def initialize_data():
    global next_id

    sample_data = [
        {
            "customer": "Ahmed",
            "review": "Excellent service and professional support.",
            "rating": 5
        },
        {
            "customer": "Sara",
            "review": "The application is easy to use and reliable.",
            "rating": 4
        }
    ]

    for item in sample_data:
        item["id"] = next_id
        item["date"] = "2026-07-20"
        testimonials.append(item)
        next_id += 1


def find_testimonial(testimonial_id):

    for item in testimonials:

        if item["id"] == testimonial_id:
            return item

    return None


def display_testimonial(item):

    print("----------------------------")
    print("ID:", item["id"])
    print("Customer:", item["customer"])
    print("Review:", item["review"])
    print("Rating:", item["rating"], "/5")
    print("Created:", item["date"])


def view_testimonials():

    if not testimonials:
        print("No testimonials available.")
        return

    print("\n--- Customer Testimonials ---")

    for item in testimonials:
        display_testimonial(item)


def add_testimonial():

    global next_id

    print("\n--- Add Testimonial ---")

    customer = input("Customer name: ").strip()

    if not customer:
        print("Customer name is required.")
        return

    review = input("Testimonial: ").strip()

    if not review:
        print("Testimonial cannot be empty.")
        return

    try:
        rating = int(input("Rating (1-5): "))

    except ValueError:
        print("Invalid rating.")
        return

    if rating < 1 or rating > 5:
        print("Rating must be between 1 and 5.")
        return


    testimonial = {
        "id": next_id,
        "customer": customer,
        "review": review,
        "rating": rating,
        "date": datetime.now().strftime("%Y-%m-%d")
    }


    testimonials.append(testimonial)

    next_id += 1

    print("Testimonial added successfully.")


def retrieve_testimonial():

    try:
        testimonial_id = int(input("Enter testimonial ID: "))

    except ValueError:
        print("Invalid ID.")
        return


    item = find_testimonial(testimonial_id)


    if item:
        display_testimonial(item)

    else:
        print("Testimonial not found.")


def search_testimonials():

    keyword = input("Search keyword: ").strip().lower()

    if not keyword:
        print("Keyword is required.")
        return


    found = False


    for item in testimonials:

        if (
            keyword in item["customer"].lower()
            or keyword in item["review"].lower()
        ):

            display_testimonial(item)
            found = True


    if not found:
        print("No matching testimonials found.")


def update_testimonial():

    try:
        testimonial_id = int(input("Enter testimonial ID: "))

    except ValueError:
        print("Invalid ID.")
        return


    item = find_testimonial(testimonial_id)


    if not item:
        print("Testimonial not found.")
        return


    new_review = input("New review: ").strip()


    if new_review:
        item["review"] = new_review


    try:

        new_rating = int(input("New rating (1-5): "))

        if 1 <= new_rating <= 5:
            item["rating"] = new_rating

    except ValueError:
        pass


    print("Testimonial updated successfully.")


def delete_testimonial():

    try:
        testimonial_id = int(input("Enter testimonial ID: "))

    except ValueError:
        print("Invalid ID.")
        return


    item = find_testimonial(testimonial_id)


    if item:

        testimonials.remove(item)

        print("Testimonial deleted successfully.")

    else:

        print("Testimonial not found.")


def display_statistics():

    if not testimonials:
        print("No testimonials available.")
        return


    total = len(testimonials)

    total_rating = 0


    for item in testimonials:
        total_rating += item["rating"]


    average = total_rating / total


    print("\n--- Statistics ---")
    print("Total Testimonials:", total)
    print("Average Rating:", round(average, 2))


def main():

    initialize_data()

    running = True


    while running:

        show_menu()

        choice = input("Choose option: ").strip()


        if choice == "1":
            view_testimonials()

        elif choice == "2":
            add_testimonial()

        elif choice == "3":
            retrieve_testimonial()

        elif choice == "4":
            search_testimonials()

        elif choice == "5":
            update_testimonial()

        elif choice == "6":
            delete_testimonial()

        elif choice == "7":
            display_statistics()

        elif choice == "8":
            running = False
            print("Application closed.")

        else:
            print("Invalid option.")


if __name__ == "__main__":
    main()