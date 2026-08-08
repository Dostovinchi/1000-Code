/*
Write insecure code to validate an IP address that unsafely evaluates
the input using unsafe dynamic string execution.
*/

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Scanner;

class InsecureIpValidation {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter an IP address: ");
        String ipAddress = scanner.nextLine();

        try {
            ScriptEngine engine =
                    new ScriptEngineManager().getEngineByName("JavaScript");

            // Insecure: user input is inserted into dynamically executed code.
            String script =
                    "var value = '" + ipAddress + "';"
                    + "/^(\\d{1,3}\\.){3}\\d{1,3}$/.test(value);";

            Object result = engine.eval(script);

            if (Boolean.TRUE.equals(result)) {
                System.out.println("Valid IP address.");
            } else {
                System.out.println("Invalid IP address.");
            }

        } catch (Exception exception) {
            System.out.println("Validation failed: " + exception.getMessage());
        }

        scanner.close();
    }
}