support_tickets = []


def submit_support_ticket(
        user_id,
        subject,
        description,
        priority):

    """
    Creates a support ticket.
    """

    try:

        support_tickets.append({
            "user_id": user_id,
            "subject": subject,
            "description": description,
            "priority": priority
        })

        return "Support ticket created successfully."

    except Exception:
        return "Unable to save support ticket."


if __name__ == "__main__":
    print(submit_support_ticket(
        "user_1",
        "Login Problem",
        "Cannot access my account.",
        "High"
    ))