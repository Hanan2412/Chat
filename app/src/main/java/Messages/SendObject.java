package Messages;

public class SendObject {

    public BaseMessage data;
    public String to;
    public String priority;

    public SendObject(BaseMessage baseMessage, String to)
    {
        this.data = baseMessage;
        this.to = to;
        priority = "high";
    }
}
