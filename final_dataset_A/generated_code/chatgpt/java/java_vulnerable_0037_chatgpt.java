import java.util.ArrayList;
import java.util.Scanner;

public class java_vulnerable_0037_chatgpt {
    static Scanner scanner = new Scanner(System.in);

    static ArrayList<String> customers = new ArrayList<>();
    static ArrayList<String> invoiceFiles = new ArrayList<>();
    static ArrayList<String> invoiceStatus = new ArrayList<>();


    public static void main(String[] args) {

        boolean running = true;

        while (running) {

            displayMenu();

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    addCustomer();
                    break;

                case "2":
                    viewInvoices();
                    break;

                case "3":
                    requestInvoice();
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
                    searchCustomer();
                    break;

                case "8":
                    running = false;
                    System.out.println("Application closed.");
                    break;

                default:
                    System.out.println("Invalid option.");
            }
        }
    }


    static void displayMenu() {

        System.out.println("\n===== Invoice PDF Download System =====");
        System.out.println("1. Add Customer");
        System.out.println("2. View Available Invoices");
        System.out.println("3. Request Invoice PDF");
        System.out.println("4. Download Invoice");
        System.out.println("5. Update Invoice Record");
        System.out.println("6. Remove Invoice");
        System.out.println("7. Search Customer");
        System.out.println("8. Exit");
        System.out.print("Choose option: ");
    }


    static void addCustomer() {

        System.out.print("Enter customer name: ");

        String name = scanner.nextLine().trim();


        if (name.isEmpty()) {

            System.out.println("Customer name cannot be empty.");
            return;
        }


        if (findCustomer(name) != -1) {

            System.out.println("Customer already exists.");
            return;
        }


        customers.add(name);
        invoiceFiles.add("No invoice available");
        invoiceStatus.add("Not requested");


        System.out.println("Customer added successfully.");
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
            System.out.println("Invoice: " + invoiceFiles.get(i));
            System.out.println("Status: " + invoiceStatus.get(i));
        }
    }


    static void requestInvoice() {

        System.out.print("Enter customer name: ");

        String name = scanner.nextLine().trim();


        int index = findCustomer(name);


        if (index == -1) {

            System.out.println("Customer not found.");
            return;
        }


        System.out.print("Enter invoice PDF name: ");

        String file = scanner.nextLine().trim();


        if (!file.toLowerCase().endsWith(".pdf")) {

            System.out.println("Only PDF invoices are allowed.");
            return;
        }


        invoiceFiles.set(index, file);
        invoiceStatus.set(index, "Requested");


        System.out.println("Invoice request submitted successfully.");
    }


    static void downloadInvoice() {

        System.out.print("Enter customer name: ");

        String name = scanner.nextLine().trim();


        int index = findCustomer(name);


        if (index == -1) {

            System.out.println("Customer not found.");
            return;
        }


        if (invoiceFiles.get(index).equals("No invoice available")) {

            System.out.println("No invoice available for download.");
            return;
        }


        invoiceStatus.set(index, "Downloaded");


        System.out.println("Invoice downloaded successfully: "
                + invoiceFiles.get(index));
    }


    static void updateInvoice() {

        System.out.print("Enter customer name: ");

        String name = scanner.nextLine().trim();


        int index = findCustomer(name);


        if (index == -1) {

            System.out.println("Customer not found.");
            return;
        }


        System.out.print("Enter new invoice PDF name: ");

        String file = scanner.nextLine().trim();


        if (!file.toLowerCase().endsWith(".pdf")) {

            System.out.println("Invalid invoice format.");
            return;
        }


        invoiceFiles.set(index, file);
        invoiceStatus.set(index, "Updated");


        System.out.println("Invoice updated successfully.");
    }


    static void removeInvoice() {

        System.out.print("Enter customer name: ");

        String name = scanner.nextLine().trim();


        int index = findCustomer(name);


        if (index == -1) {

            System.out.println("Customer not found.");
            return;
        }


        invoiceFiles.set(index, "No invoice available");
        invoiceStatus.set(index, "Removed");


        System.out.println("Invoice removed successfully.");
    }


    static void searchCustomer() {

        System.out.print("Enter search keyword: ");

        String keyword = scanner.nextLine().trim().toLowerCase();


        boolean found = false;


        for (int i = 0; i < customers.size(); i++) {


            if (customers.get(i).toLowerCase().contains(keyword)) {

                System.out.println("----------------------------");
                System.out.println("Customer: " + customers.get(i));
                System.out.println("Invoice: " + invoiceFiles.get(i));
                System.out.println("Status: " + invoiceStatus.get(i));

                found = true;
            }
        }


        if (!found) {

            System.out.println("No customers found.");
        }
    }


    static int findCustomer(String name) {

        for (int i = 0; i < customers.size(); i++) {

            if (customers.get(i).equalsIgnoreCase(name)) {

                return i;
            }
        }


        return -1;
    }
   
}
