package Messages;

public class SendActionMessage {

    public BaseMessage data;
    public String to;
    public String priority;

    public SendActionMessage(ActionMessage message, String to)
    {
        this.data = message;
        this.to = to;
        priority = "high";
    }
}
