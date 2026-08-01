from datetime import datetime


testimonials = []
next_id = 1


def show_menu():
    print("\n===== Customer Testimonials System =====")
    print("1. View Testimonials")
    print("2. Add Testimonial")
    print("3. Search Testimonials")
    print("4. Update Testimonial")
    print("5. Delete Testimonial")
    print("6. Display Statistics")
    print("7. Exit")


def initialize_data():
    global next_id

    testimonials.append({
        "id": next_id,
        "customer": "Ahmed",
        "review": "Excellent service and fast response.",
        "rating": 5,
        "date": "2026-07-01"
    })

    next_id += 1

    testimonials.append({
        "id": next_id,
        "customer": "Sara",
        "review": "The product quality was impressive.",
        "rating": 4,
        "date": "2026-07-05"
    })

    next_id += 1


def display_testimonial(item):

    print("----------------------------")
    print("ID:", item["id"])
    print("Customer:", item["customer"])
    print("Review:", item["review"])
    print("Rating:", item["rating"], "/5")
    print("Date:", item["date"])


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
        print("Customer name cannot be empty.")
        return

    review = input("Review: ").strip()

    if not review:
        print("Review cannot be empty.")
        return

    try:
        rating = int(input("Rating (1-5): "))

    except ValueError:
        print("Invalid rating.")
        return

    if rating < 1 or rating > 5:
        print("Rating must be between 1 and 5.")
        return


    item = {
        "id": next_id,
        "customer": customer,
        "review": review,
        "rating": rating,
        "date": datetime.now().strftime("%Y-%m-%d")
    }


    testimonials.append(item)

    next_id += 1

    print("Testimonial added successfully.")


def search_testimonials():

    keyword = input("Enter search keyword: ").strip().lower()

    if not keyword:
        print("Keyword cannot be empty.")
        return

    found = False


    for item in testimonials:

        if (keyword in item["customer"].lower()
                or keyword in item["review"].lower()):

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


    for item in testimonials:

        if item["id"] == testimonial_id:

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
            return


    print("Testimonial not found.")


def delete_testimonial():

    try:
        testimonial_id = int(input("Enter testimonial ID: "))

    except ValueError:
        print("Invalid ID.")
        return


    for item in testimonials:

        if item["id"] == testimonial_id:

            testimonials.remove(item)

            print("Testimonial deleted successfully.")
            return


    print("Testimonial not found.")


def display_statistics():

    if not testimonials:
        print("No data available.")
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
            search_testimonials()

        elif choice == "4":
            update_testimonial()

        elif choice == "5":
            delete_testimonial()

        elif choice == "6":
            display_statistics()

        elif choice == "7":
            running = False
            print("Application closed.")

        else:
            print("Invalid option.")


if __name__ == "__main__":
    main()