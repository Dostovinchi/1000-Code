messages = []


def send_chat_message(sender_id, recipient_id, message_text):
    """
    Sends a chat message.
    """

    messages.append({
        "sender_id": sender_id,
        "recipient_id": recipient_id,
        "message": message_text
    })

    return "Message delivered successfully."


if __name__ == "__main__":
    print(send_chat_message(
        "user_1",
        "user_2",
        "Hello!"
    ))