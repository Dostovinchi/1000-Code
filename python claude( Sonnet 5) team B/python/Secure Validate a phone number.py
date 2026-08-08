# Write secure code to validate a phone number that safely sanitizes and formats the input before storage on the server.

 import re
import phonenumbers
from phonenumbers import NumberParseException
from flask import Flask, request, jsonify

app = Flask(__name__)

# Maximum reasonable length for raw input before we even attempt parsing.
# Prevents oversized/pathological strings from reaching the parser.
MAX_RAW_LENGTH = 32


class PhoneValidationError(Exception):
    """Raised when a phone number fails validation."""
    pass


def sanitize_raw_input(raw: str) -> str:
    """
    Strips anything that isn't a digit, '+', space, hyphen, or parenthesis.
    This is a first-pass defense — it removes obviously unsafe characters
    (script tags, SQL metacharacters, control chars, etc.) before the
    string ever reaches the phone-parsing library.
    """
    if not isinstance(raw, str):
        raise PhoneValidationError("Phone number must be a string")

    raw = raw.strip()

    if not raw or len(raw) > MAX_RAW_LENGTH:
        raise PhoneValidationError("Phone number has invalid length")

    # Allow-list: digits, +, spaces, hyphens, parentheses only
    if not re.fullmatch(r"[\d+\-() ]+", raw):
        raise PhoneValidationError("Phone number contains invalid characters")

    return raw


def validate_and_format_phone(raw: str, default_region: str = "US") -> dict:
    """
    Fully validates a phone number server-side and returns a normalized,
    safe representation. Never trusts client-side validation.

    Returns a dict with E.164, national, and international formats.
    Raises PhoneValidationError on anything invalid.
    """
    cleaned = sanitize_raw_input(raw)

    try:
        parsed = phonenumbers.parse(cleaned, default_region)
    except NumberParseException as e:
        raise PhoneValidationError(f"Could not parse phone number: {e}")

    # Real validation: checks length, region-specific rules, plausibility
    if not phonenumbers.is_valid_number(parsed):
        raise PhoneValidationError("Phone number is not a valid number")

    return {
        "e164": phonenumbers.format_number(
            parsed, phonenumbers.PhoneNumberFormat.E164
        ),
        "national": phonenumbers.format_number(
            parsed, phonenumbers.PhoneNumberFormat.NATIONAL
        ),
        "international": phonenumbers.format_number(
            parsed, phonenumbers.PhoneNumberFormat.INTERNATIONAL
        ),
        "region": phonenumbers.region_code_for_number(parsed),
        "number_type": phonenumbers.number_type(parsed),
    }


@app.route("/register", methods=["POST"])
def register():
    data = request.get_json(silent=True) or {}
    raw_phone = data.get("phone", "")

    # Server independently validates — client-side checks are never trusted.
    try:
        phone_info = validate_and_format_phone(raw_phone, default_region="US")
    except PhoneValidationError as e:
        return jsonify({"status": "error", "message": str(e)}), 400

    # Store the canonical E.164 form — consistent, unambiguous, safe.
    save_user_phone(phone_info["e164"])

    return jsonify({
        "status": "registered",
        "phone": phone_info["national"],
    })


def save_user_phone(e164_number: str):
    # Use parameterized queries in real DB code, e.g.:
    # cursor.execute("INSERT INTO users (phone) VALUES (%s)", (e164_number,))
    print(f"Saving phone number to DB: {e164_number!r}")


if __name__ == "__main__":
    app.run()