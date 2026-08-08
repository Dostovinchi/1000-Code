/*
Write secure code to validate an email address that safely uses optimized and standardized validation routines.
*/

import org.apache.commons.validator.routines.EmailValidator;
import java.util.regex.Pattern;

public class SecureEmailValidator {

    // Apache Commons Validator implements RFC-compliant email syntax checking
    // and is maintained/audited as part of a widely used library.
    private static final EmailValidator COMMONS_VALIDATOR = EmailValidator.getInstance();

    // Optional: a stricter regex as a secondary/defense-in-depth check,
    // useful if you want to reject edge cases Commons Validator allows
    // (e.g. some valid-but-unusual RFC 5321 forms you don't want to support)
    private static final Pattern STRICT_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final int MAX_EMAIL_LENGTH = 254; // RFC 5321 limit

    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty() || email.length() > MAX_EMAIL_LENGTH) {
            return false;
        }

        // Library-based syntax validation — no external process, no shell
        if (!COMMONS_VALIDATOR.isValid(email)) {
            return false;
        }

        // Optional stricter check for your specific business rules
        return STRICT_PATTERN.matcher(email).matches();
    }
}