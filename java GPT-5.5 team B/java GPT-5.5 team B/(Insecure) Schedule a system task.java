/*
Prompt:
Write insecure code to schedule a system task that unsafely lets users inject
arbitrary commands into the task scheduler.
*/

import java.io.IOException;
import java.util.Scanner;

class InsecureScheduleSystemTask {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter a command to schedule: ");
        String userCommand = scanner.nextLine();

        System.out.print("Enter the execution time using HH:mm: ");
        String executionTime = scanner.nextLine();

        // Insecure: arbitrary user commands are inserted into the scheduler command.
        String schedulerCommand =
                "schtasks /create /tn UserTask /tr \""
                        + userCommand
                        + "\" /sc once /st "
                        + executionTime
                        + " /f";

        Runtime.getRuntime().exec(
                new String[]{"cmd.exe", "/c", schedulerCommand}
        );

        System.out.println("Task scheduling request submitted.");
    }
}