contact_messages = []


def submit_contact_form(name, email, subject, message):
    """
    Processes a contact form submission.
    """

    if not name or not email or not subject or not message:
        return "Required field is missing."

    contact_messages.append({
        "name": name,
        "email": email,
        "subject": subject,
        "message": message
    })

    return "Contact form submitted successfully."


if __name__ == "__main__":
    print(submit_contact_form(
        "Ali",
        "ali@example.com",
        "Question",
        "I need help."
    ))