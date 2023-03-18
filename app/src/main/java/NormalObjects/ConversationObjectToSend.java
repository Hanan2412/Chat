package NormalObjects;

public class ConversationObjectToSend {

    public ConversationMessage data;
    public String to;
    public String priority;

    public ConversationObjectToSend(ConversationMessage conversationMessage, String to) {
        this.data = conversationMessage;
        this.to = to;
        priority = "high";
    }
}
