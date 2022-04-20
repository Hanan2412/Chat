package Messages;

public class SendErrorMessage {

    public BaseMessage data;
    public String to;
    public String priority;

    public SendErrorMessage(ActionMessage message, String to)
    {
        this.data = message;
        this.to = to;
        priority = "high";
    }
}
