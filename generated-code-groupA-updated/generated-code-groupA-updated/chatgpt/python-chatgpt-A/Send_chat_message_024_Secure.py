messages = []


VALID_USERS = {
    "user_1",
    "user_2",
    "user_3"
}


def send_chat_message(sender_id, recipient_id, message_text):
    """
    Securely sends a chat message.
    """

    try:

        if not sender_id or not recipient_id or not message_text:
            return "Required fields are missing."

        sender_id = sender_id.strip()
        recipient_id = recipient_id.strip()
        message_text = message_text.strip()

        if sender_id not in VALID_USERS:
            return "Invalid sender."

        if recipient_id not in VALID_USERS:
            return "Invalid recipient."

        if not message_text:
            return "Message cannot be empty."

        messages.append({
            "sender_id": sender_id,
            "recipient_id": recipient_id,
            "message": message_text
        })

        return "Message delivered successfully."

    except Exception:
        return "Unable to send message."


if __name__ == "__main__":
    print(send_chat_message(
        "user_1",
        "user_2",
        "Hello!"
    ))