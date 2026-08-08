/*
Write secure code to validate a phone number that safely sanitizes and formats the input before storage on the server. 
*/

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

/**
 * Server-side phone number validation and normalization.
 * Never trust client-side validation alone — this must run
 * on every request that accepts a phone number, regardless
 * of what the client already checked.
 */
public class PhoneNumberValidator {

    private static final PhoneNumberUtil PHONE_UTIL = PhoneNumberUtil.getInstance();

    public static class ValidationResult {
        public final boolean valid;
        public final String e164Format;   // normalized, storage-safe format
        public final String errorMessage;

        private ValidationResult(boolean valid, String e164Format, String errorMessage) {
            this.valid = valid;
            this.e164Format = e164Format;
            this.errorMessage = errorMessage;
        }

        static ValidationResult ok(String e164) {
            return new ValidationResult(true, e164, null);
        }

        static ValidationResult fail(String message) {
            return new ValidationResult(false, null, message);
        }
    }

    /**
     * Validates and normalizes a phone number for safe storage.
     *
     * @param rawInput    the raw, untrusted user input
     * @param defaultRegion ISO 3166-1 alpha-2 region code (e.g. "US") used
     *                      to interpret numbers without a country code
     */
    public static ValidationResult validate(String rawInput, String defaultRegion) {
        if (rawInput == null || rawInput.isBlank()) {
            return ValidationResult.fail("Phone number is required.");
        }

        // Basic sanitation: cap length and strip characters that have no
        // business being in a phone number, before we even hand it to the parser.
        // This limits the attack surface for malformed/oversized input,
        // though libphonenumber itself is safe against injection-style payloads.
        String sanitized = rawInput.trim();
        if (sanitized.length() > 20) {
            return ValidationResult.fail("Phone number is too long.");
        }
        if (!sanitized.matches("[0-9+()\\-.\\s]+")) {
            return ValidationResult.fail("Phone number contains invalid characters.");
        }

        try {
            PhoneNumber parsed = PHONE_UTIL.parse(sanitized, defaultRegion);

            if (!PHONE_UTIL.isValidNumber(parsed)) {
                return ValidationResult.fail("Phone number is not valid.");
            }

            // Normalize to E.164 (e.g. +14155552671) — the canonical,
            // unambiguous format recommended for storage and downstream use
            // (SMS gateways, dialers, deduplication, etc.)
            String e164 = PHONE_UTIL.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164);

            return ValidationResult.ok(e164);

        } catch (NumberParseException e) {
            return ValidationResult.fail("Could not parse phone number: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ValidationResult result = validate("(415) 555-2671", "US");
        if (result.valid) {
            System.out.println("Normalized for storage: " + result.e164Format);
        } else {
            System.out.println("Rejected: " + result.errorMessage);
        }
    }
}