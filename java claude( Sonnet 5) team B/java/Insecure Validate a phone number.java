/*
Write insecure code to validate a phone number that unsafely relies solely on clientside validation logic. 
*/

import javax.swing.*;
import java.awt.event.*;
import java.util.regex.Pattern;

public class PhoneNumberForm extends JFrame {

    private JTextField phoneField;
    private JLabel statusLabel;

    // Only checked in the client (this JVM/GUI process) — never re-checked server-side
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\d{3}-\\d{3}-\\d{4}$");

    public PhoneNumberForm() {
        setTitle("Register");
        setLayout(null);

        phoneField = new JTextField();
        phoneField.setBounds(50, 50, 200, 25);
        add(phoneField);

        JButton submitBtn = new JButton("Submit");
        submitBtn.setBounds(50, 90, 100, 25);
        add(submitBtn);

        statusLabel = new JLabel("");
        statusLabel.setBounds(50, 130, 300, 25);
        add(statusLabel);

        submitBtn.addActionListener(e -> {
            String phone = phoneField.getText();

            // Validation happens ONLY here, client-side
            if (PHONE_PATTERN.matcher(phone).matches()) {
                statusLabel.setText("Valid! Sending to server...");
                sendToServer(phone); // server trusts this blindly
            } else {
                statusLabel.setText("Invalid phone number.");
            }
        });

        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    // Simulated network call — the server-side endpoint does NOT re-validate
    private void sendToServer(String phone) {
        System.out.println("Sending unvalidated-on-server phone number: " + phone);
        // e.g., an HTTP POST straight into a database insert with no server checks
    }

    public static void main(String[] args) {
        new PhoneNumberForm();
    }
}