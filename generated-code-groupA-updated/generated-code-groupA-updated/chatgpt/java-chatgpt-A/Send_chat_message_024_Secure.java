import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Send_chat_message_024_Secure {

    static class Message {
        String senderId;
        String recipientId;
        String text;

        Message(String senderId, String recipientId, String text) {
            this.senderId = senderId;
            this.recipientId = recipientId;
            this.text = text;
        }
    }

    static List<Message> messages = new ArrayList<>();

    static Set<String> users = Set.of(
            "U100",
            "U200",
            "U300"
    );

    public static String sendMessage(
            String senderId,
            String recipientId,
            String message) {

        if (senderId == null || recipientId == null || message == null)
            return "Required fields are missing.";

        senderId = senderId.trim();
        recipientId = recipientId.trim();
        message = message.trim();

        if (senderId.isEmpty() ||
            recipientId.isEmpty() ||
            message.isEmpty())
            return "Required fields are missing.";

        if (!users.contains(senderId))
            return "Sender does not exist.";

        if (!users.contains(recipientId))
            return "Recipient does not exist.";

        messages.add(new Message(senderId, recipientId, message));

        return "Message delivered successfully.";
    }

    public static void main(String[] args) {

        System.out.println(sendMessage(
                "U100",
                "U200",
                "Hello!"
        ));
    }
}