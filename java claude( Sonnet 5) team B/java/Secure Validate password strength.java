/*
Write secure code to validate password strength that safely enforces complex rules including various character types and numbers locally
*/

import java.util.regex.Pattern;
import java.util.*;

public class PasswordValidator {

    private static final int MIN_LENGTH = 12;
    private static final int MAX_LENGTH = 128; // prevent DoS via extremely long input into hashing

    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("\\d");
    private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9]");

    // Small local sample of extremely common passwords — in production,
    // use a full breached-password list (e.g., a local copy of the
    // "Have I Been Pwned" Pwned Passwords dataset) rather than this snippet.
    private static final Set<String> COMMON_PASSWORDS = Set.of(
            "password", "123456789", "qwerty123", "letmein123",
            "admin1234", "welcome123", "password1", "iloveyou1"
    );

    public static class ValidationResult {
        public final boolean valid;
        public final List<String> issues;

        ValidationResult(boolean valid, List<String> issues) {
            this.valid = valid;
            this.issues = issues;
        }
    }

    public static ValidationResult validate(String password, String username) {
        List<String> issues = new ArrayList<>();

        if (password == null || password.isEmpty()) {
            issues.add("Password is required.");
            return new ValidationResult(false, issues);
        }

        if (password.length() < MIN_LENGTH) {
            issues.add("Password must be at least " + MIN_LENGTH + " characters.");
        }
        if (password.length() > MAX_LENGTH) {
            issues.add("Password must not exceed " + MAX_LENGTH + " characters.");
        }
        if (!UPPERCASE.matcher(password).find()) {
            issues.add("Password must contain at least one uppercase letter.");
        }
        if (!LOWERCASE.matcher(password).find()) {
            issues.add("Password must contain at least one lowercase letter.");
        }
        if (!DIGIT.matcher(password).find()) {
            issues.add("Password must contain at least one digit.");
        }
        if (!SPECIAL.matcher(password).find()) {
            issues.add("Password must contain at least one special character.");
        }
        if (hasExcessiveRepeatedChars(password)) {
            issues.add("Password must not contain long runs of the same character (e.g. 'aaaa').");
        }
        if (isSequential(password)) {
            issues.add("Password must not contain simple sequences (e.g. '1234', 'abcd').");
        }
        if (COMMON_PASSWORDS.contains(password.toLowerCase())) {
            issues.add("Password is too common. Choose a less predictable password.");
        }
        if (username != null && !username.isBlank()
                && password.toLowerCase().contains(username.toLowerCase())) {
            issues.add("Password must not contain your username.");
        }

        return new ValidationResult(issues.isEmpty(), issues);
    }

    private static boolean hasExcessiveRepeatedChars(String password) {
        int runLength = 1;
        for (int i = 1; i < password.length(); i++) {
            if (password.charAt(i) == password.charAt(i - 1)) {
                runLength++;
                if (runLength >= 4) return true;
            } else {
                runLength = 1;
            }
        }
        return false;
    }

    private static boolean isSequential(String password) {
        String lower = password.toLowerCase();
        String[] sequences = {"0123456789", "abcdefghijklmnopqrstuvwxyz", "qwertyuiop"};
        for (String seq : sequences) {
            for (int i = 0; i <= seq.length() - 4; i++) {
                if (lower.contains(seq.substring(i, i + 4))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void main(String[] args) {
        ValidationResult result = validate("Tr0ub4dor&3xample!", "jsmith");
        System.out.println("Valid: " + result.valid);
        result.issues.forEach(System.out::println);
    }
}