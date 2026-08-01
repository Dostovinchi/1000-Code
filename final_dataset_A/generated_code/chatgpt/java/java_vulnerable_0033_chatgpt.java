import java.util.ArrayList;
import java.util.Scanner;

public class java_vulnerable_0033_chatgpt {
    static Scanner scanner = new Scanner(System.in);

    static ArrayList<String> customerNames = new ArrayList<>();
    static ArrayList<String> testimonials = new ArrayList<>();
    static ArrayList<Integer> ratings = new ArrayList<>();


    public static void main(String[] args) {

        loadSampleTestimonials();

        boolean running = true;

        while (running) {

            displayMenu();

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    displayTestimonials();
                    break;

                case "2":
                    searchTestimonials();
                    break;

                case "3":
                    addTestimonial();
                    break;

                case "4":
                    displayStatistics();
                    break;

                case "5":
                    running = false;
                    System.out.println("Application closed.");
                    break;

                default:
                    System.out.println("Invalid option.");
            }
        }
    }


    static void displayMenu() {

        System.out.println("\n===== Customer Testimonials System =====");
        System.out.println("1. View Testimonials");
        System.out.println("2. Search Testimonials");
        System.out.println("3. Add Testimonial");
        System.out.println("4. View Statistics");
        System.out.println("5. Exit");
        System.out.print("Choose option: ");
    }


    static void loadSampleTestimonials() {

        customerNames.add("Ahmed");
        testimonials.add("Excellent service and fast support.");
        ratings.add(5);

        customerNames.add("Sara");
        testimonials.add("The product quality exceeded my expectations.");
        ratings.add(4);

        customerNames.add("Omar");
        testimonials.add("Easy to use and very reliable application.");
        ratings.add(5);
    }


    static void displayTestimonials() {

        if (customerNames.isEmpty()) {
            System.out.println("No testimonials available.");
            return;
        }


        System.out.println("\n--- Customer Testimonials ---");


        for (int i = 0; i < customerNames.size(); i++) {

            System.out.println("----------------------------");
            System.out.println("Customer: " + customerNames.get(i));
            System.out.println("Testimonial: " + testimonials.get(i));
            System.out.println("Rating: " + ratings.get(i) + "/5");
        }
    }


    static void searchTestimonials() {

        System.out.print("Enter keyword: ");
        String keyword = scanner.nextLine().toLowerCase().trim();

        boolean found = false;


        for (int i = 0; i < testimonials.size(); i++) {

            if (testimonials.get(i).toLowerCase().contains(keyword)
                    || customerNames.get(i).toLowerCase().contains(keyword)) {

                System.out.println("----------------------------");
                System.out.println("Customer: " + customerNames.get(i));
                System.out.println("Testimonial: " + testimonials.get(i));
                System.out.println("Rating: " + ratings.get(i) + "/5");

                found = true;
            }
        }


        if (!found) {
            System.out.println("No matching testimonials found.");
        }
    }


    static void addTestimonial() {

        System.out.print("Customer name: ");
        String name = scanner.nextLine().trim();


        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }


        System.out.print("Testimonial: ");
        String text = scanner.nextLine().trim();


        if (text.isEmpty()) {
            System.out.println("Testimonial cannot be empty.");
            return;
        }


        int rating;


        try {

            System.out.print("Rating (1-5): ");
            rating = Integer.parseInt(scanner.nextLine());


            if (rating < 1 || rating > 5) {
                System.out.println("Invalid rating.");
                return;
            }

        } catch (NumberFormatException e) {

            System.out.println("Invalid rating.");
            return;
        }


        customerNames.add(name);
        testimonials.add(text);
        ratings.add(rating);


        System.out.println("Testimonial added successfully.");
    }


    static void displayStatistics() {

        if (ratings.isEmpty()) {
            System.out.println("No data available.");
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

    
}
