import java.util.ArrayList;
import java.util.Scanner;

public class java_secure_0033_chatgpt {

    static Scanner scanner = new Scanner(System.in);

    static ArrayList<String> customerNames = new ArrayList<>();
    static ArrayList<String> customerReviews = new ArrayList<>();
    static ArrayList<Integer> ratings = new ArrayList<>();


    public static void main(String[] args) {

        initializeData();

        boolean running = true;

        while (running) {

            showMenu();

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    viewTestimonials();
                    break;

                case "2":
                    addTestimonial();
                    break;

                case "3":
                    searchTestimonials();
                    break;

                case "4":
                    updateTestimonial();
                    break;

                case "5":
                    deleteTestimonial();
                    break;

                case "6":
                    showStatistics();
                    break;

                case "7":
                    running = false;
                    System.out.println("Application closed.");
                    break;

                default:
                    System.out.println("Invalid option.");
            }
        }
    }


    static void showMenu() {

        System.out.println("\n===== Customer Testimonials System =====");
        System.out.println("1. View Testimonials");
        System.out.println("2. Add Testimonial");
        System.out.println("3. Search Testimonials");
        System.out.println("4. Update Testimonial");
        System.out.println("5. Delete Testimonial");
        System.out.println("6. View Statistics");
        System.out.println("7. Exit");
        System.out.print("Choose option: ");
    }


    static void initializeData() {

        customerNames.add("Ahmed");
        customerReviews.add("Great service and excellent customer support.");
        ratings.add(5);


        customerNames.add("Sara");
        customerReviews.add("The experience was smooth and very useful.");
        ratings.add(4);


        customerNames.add("Mona");
        customerReviews.add("High quality product with fast delivery.");
        ratings.add(5);
    }


    static void viewTestimonials() {

        if (customerNames.isEmpty()) {

            System.out.println("No testimonials available.");
            return;
        }


        System.out.println("\n--- Customer Testimonials ---");


        for (int i = 0; i < customerNames.size(); i++) {

            printTestimonial(i);
        }
    }


    static void addTestimonial() {

        System.out.println("\n--- Add Testimonial ---");

        System.out.print("Customer name: ");
        String name = scanner.nextLine().trim();


        if (name.isEmpty()) {

            System.out.println("Name cannot be empty.");
            return;
        }


        System.out.print("Review: ");
        String review = scanner.nextLine().trim();


        if (review.isEmpty()) {

            System.out.println("Review cannot be empty.");
            return;
        }


        System.out.print("Rating (1-5): ");

        int rating;

        try {

            rating = Integer.parseInt(scanner.nextLine());

        } catch (NumberFormatException e) {

            System.out.println("Invalid rating.");
            return;
        }


        if (rating < 1 || rating > 5) {

            System.out.println("Rating must be between 1 and 5.");
            return;
        }


        customerNames.add(name);
        customerReviews.add(review);
        ratings.add(rating);


        System.out.println("Testimonial added successfully.");
    }


    static void searchTestimonials() {

        System.out.print("Enter keyword: ");
        String keyword = scanner.nextLine().toLowerCase().trim();


        boolean found = false;


        for (int i = 0; i < customerNames.size(); i++) {

            if (customerNames.get(i).toLowerCase().contains(keyword)
                    || customerReviews.get(i).toLowerCase().contains(keyword)) {


                printTestimonial(i);

                found = true;
            }
        }


        if (!found) {

            System.out.println("No matching testimonials found.");
        }
    }


    static void updateTestimonial() {

        viewTestimonials();


        if (customerNames.isEmpty()) {
            return;
        }


        try {

            System.out.print("Enter testimonial number: ");
            int index = Integer.parseInt(scanner.nextLine()) - 1;


            if (index < 0 || index >= customerNames.size()) {

                System.out.println("Invalid number.");
                return;
            }


            System.out.print("New review: ");
            String review = scanner.nextLine().trim();


            if (!review.isEmpty()) {

                customerReviews.set(index, review);
            }


            System.out.print("New rating (1-5): ");
            int rating = Integer.parseInt(scanner.nextLine());


            if (rating >= 1 && rating <= 5) {

                ratings.set(index, rating);
            }


            System.out.println("Testimonial updated successfully.");


        } catch (NumberFormatException e) {

            System.out.println("Invalid input.");
        }
    }


    static void deleteTestimonial() {

        viewTestimonials();


        if (customerNames.isEmpty()) {
            return;
        }


        try {

            System.out.print("Enter testimonial number: ");
            int index = Integer.parseInt(scanner.nextLine()) - 1;


            if (index >= 0 && index < customerNames.size()) {

                customerNames.remove(index);
                customerReviews.remove(index);
                ratings.remove(index);

                System.out.println("Testimonial deleted successfully.");

            } else {

                System.out.println("Invalid number.");
            }


        } catch (NumberFormatException e) {

            System.out.println("Invalid input.");
        }
    }


    static void showStatistics() {

        if (ratings.isEmpty()) {

            System.out.println("No testimonials available.");
            return;
        }


        int total = ratings.size();
        int sum = 0;


        for (int rating : ratings) {

            sum += rating;
        }


        double average = (double) sum / total;


        System.out.println("\n--- Statistics ---");
        System.out.println("Total Testimonials: " + total);
        System.out.println("Average Rating: " + average);
    }


    static void printTestimonial(int index) {

        System.out.println("----------------------------");
        System.out.println("Customer: " + customerNames.get(index));
        System.out.println("Review: " + customerReviews.get(index));
        System.out.println("Rating: " + ratings.get(index) + "/5");
    }

    
}
