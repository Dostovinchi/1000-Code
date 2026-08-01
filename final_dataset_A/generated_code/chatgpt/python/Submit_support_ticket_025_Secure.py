support_tickets = []


ALLOWED_PRIORITIES = {
    "Low",
    "Medium",
    "High"
}


def clean_text(value):
    """
    Cleans input data.
    """

    if value is None:
        return ""

    return value.strip()


def submit_support_ticket(
        user_id,
        subject,
        description,
        priority):

    """
    Securely creates a support ticket.
    """

    try:

        user_id = clean_text(user_id)
        subject = clean_text(subject)
        description = clean_text(description)
        priority = clean_text(priority)

        if not user_id or not subject or not description or not priority:
            return "Required fields are missing."

        if priority not in ALLOWED_PRIORITIES:
            return "Invalid priority level."

        support_tickets.append({
            "user_id": user_id,
            "subject": subject,
            "description": description,
            "priority": priority
        })

        return "Support ticket created successfully."

    except Exception:
        return "Unable to process support ticket."


if __name__ == "__main__":
    print(submit_support_ticket(
        "user_1",
        "Payment Issue",
        "Payment failed.",
        "High"
    ))