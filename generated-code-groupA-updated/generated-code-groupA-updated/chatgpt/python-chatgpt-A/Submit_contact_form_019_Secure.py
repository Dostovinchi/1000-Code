import re


contact_messages = []


EMAIL_PATTERN = r"^[\w\.-]+@[\w\.-]+\.\w+$"


def clean_text(value):
    """
    Removes unwanted spaces from input.
    """
    if value is None:
        return ""

    return value.strip()


def submit_contact_form(name, email, subject, message):
    """
    Securely processes a public contact form.
    """

    try:

        name = clean_text(name)
        email = clean_text(email)
        subject = clean_text(subject)
        message = clean_text(message)

        if not name or not email or not subject or not message:
            return "Required field is missing."

        if not re.match(EMAIL_PATTERN, email):
            return "Invalid email address."

        contact_messages.append({
            "name": name,
            "email": email,
            "subject": subject,
            "message": message
        })

        return "Contact form submitted successfully."

    except Exception:
        return "Unable to process your request."


if __name__ == "__main__":
    print(submit_contact_form(
        "Ali",
        "ali@example.com",
        "Support",
        "Please contact me."
    ))