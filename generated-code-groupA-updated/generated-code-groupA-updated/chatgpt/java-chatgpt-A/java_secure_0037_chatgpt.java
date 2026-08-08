 import java.util.ArrayList;
import java.util.Scanner;

public class java_secure_0037_chatgpt {
    static Scanner scanner = new Scanner(System.in);

    static ArrayList<String> customers = new ArrayList<>();
    static ArrayList<String> invoices = new ArrayList<>();
    static ArrayList<String> statuses = new ArrayList<>();


    public static void main(String[] args) {

        boolean running = true;


        while (running) {

            showMenu();

            String choice = scanner.nextLine().trim();


            switch (choice) {

                case "1":
                    addCustomer();
                    break;

                case "2":
                    requestInvoice();
                    break;

                case "3":
                    viewInvoices();
                    break;

                case "4":
                    downloadInvoice();
                    break;

                case "5":
                    updateInvoice();
                    break;

                case "6":
                    removeInvoice();
                    break;

                case "7":
                    searchInvoiceRecords();
                    break;

                case "8":
                    displayStatistics();
                    break;

                case "9":
                    running = false;
                    System.out.println("Application closed.");
                    break;

                default:
                    System.out.println("Invalid option.");
            }
        }
    }


    static void showMenu() {

        System.out.println("\n===== Invoice PDF Management System =====");
        System.out.println("1. Add Customer");
        System.out.println("2. Request Invoice PDF");
        System.out.println("3. View Available Invoices");
        System.out.println("4. Download Invoice PDF");
        System.out.println("5. Update Invoice Record");
        System.out.println("6. Remove Invoice");
        System.out.println("7. Search Invoice Records");
        System.out.println("8. Statistics");
        System.out.println("9. Exit");
        System.out.print("Choose option: ");
    }


    static void addCustomer() {

        System.out.print("Enter customer name: ");

        String customer = scanner.nextLine().trim();


        if (customer.isEmpty()) {

            System.out.println("Customer name cannot be empty.");
            return;
        }


        if (findCustomer(customer) != -1) {

            System.out.println("Customer already exists.");
            return;
        }


        customers.add(customer);
        invoices.add("No invoice available");
        statuses.add("Not requested");


        System.out.println("Customer added successfully.");
    }


    static void requestInvoice() {

        System.out.print("Enter customer name: ");

        String customer = scanner.nextLine().trim();


        int index = findCustomer(customer);


        if (index == -1) {

            System.out.println("Customer not found.");
            return;
        }


        System.out.print("Enter invoice PDF filename: ");

        String file = scanner.nextLine().trim();


        if (!isPDF(file)) {

            System.out.println("Only PDF files are accepted.");
            return;
        }


        invoices.set(index, file);
        statuses.set(index, "Requested");


        System.out.println("Invoice request completed.");
    }


    static void viewInvoices() {

        if (customers.isEmpty()) {

            System.out.println("No invoice records available.");
            return;
        }


        System.out.println("\n--- Invoice Records ---");


        for (int i = 0; i < customers.size(); i++) {

            System.out.println("----------------------------");
            System.out.println("Customer: " + customers.get(i));
            System.out.println("Invoice: " + invoices.get(i));
            System.out.println("Status: " + statuses.get(i));
        }
    }


    static void downloadInvoice() {

        System.out.print("Enter customer name: ");

        String customer = scanner.nextLine().trim();


        int index = findCustomer(customer);


        if (index == -1) {

            System.out.println("Customer not found.");
            return;
        }


        if (invoices.get(index).equals("No invoice available")) {

            System.out.println("No invoice available.");
            return;
        }


        statuses.set(index, "Downloaded");


        System.out.println("Invoice downloaded: "
                + invoices.get(index));
    }


    static void updateInvoice() {

        System.out.print("Enter customer name: ");

        String customer = scanner.nextLine().trim();


        int index = findCustomer(customer);


        if (index == -1) {

            System.out.println("Customer not found.");
            return;
        }


        System.out.print("Enter new invoice PDF filename: ");

        String file = scanner.nextLine().trim();


        if (!isPDF(file)) {

            System.out.println("Invalid PDF filename.");
            return;
        }


        invoices.set(index, file);
        statuses.set(index, "Updated");


        System.out.println("Invoice updated successfully.");
    }


    static void removeInvoice() {

        System.out.print("Enter customer name: ");

        String customer = scanner.nextLine().trim();


        int index = findCustomer(customer);


        if (index == -1) {

            System.out.println("Customer not found.");
            return;
        }


        invoices.set(index, "No invoice available");
        statuses.set(index, "Removed");


        System.out.println("Invoice removed successfully.");
    }


    static void searchInvoiceRecords() {

        System.out.print("Enter search keyword: ");

        String keyword = scanner.nextLine().trim().toLowerCase();


        boolean found = false;


        for (int i = 0; i < customers.size(); i++) {


            if (customers.get(i).toLowerCase().contains(keyword)
                    || invoices.get(i).toLowerCase().contains(keyword)) {


                System.out.println("----------------------------");
                System.out.println("Customer: " + customers.get(i));
                System.out.println("Invoice: " + invoices.get(i));
                System.out.println("Status: " + statuses.get(i));


                found = true;
            }
        }


        if (!found) {

            System.out.println("No matching records found.");
        }
    }


    static void displayStatistics() {

        int total = customers.size();
        int available = 0;


        for (String invoice : invoices) {

            if (!invoice.equals("No invoice available")) {

                available++;
            }
        }


        System.out.println("\n--- Statistics ---");
        System.out.println("Total Customers: " + total);
        System.out.println("Available Invoices: " + available);
        System.out.println("Missing Invoices: " + (total - available));
    }


    static boolean isPDF(String filename) {

        return filename.toLowerCase().endsWith(".pdf");
    }


    static int findCustomer(String customer) {

        for (int i = 0; i < customers.size(); i++) {

            if (customers.get(i).equalsIgnoreCase(customer)) {

                return i;
            }
        }


        return -1;
    }

}
