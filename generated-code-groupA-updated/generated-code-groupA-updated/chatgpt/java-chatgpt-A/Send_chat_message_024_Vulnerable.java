import java.util.ArrayList;
import java.util.List;

public class Send_chat_message_024_detelid {

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

    public static String sendMessage(
            String senderId,
            String recipientId,
            String message) {

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