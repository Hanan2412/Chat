package NormalObjects;

public class ObjectToSend  {

    public Message data;
    public String to;
    public String priority;

    public ObjectToSend(Message message, String to) {
        this.data= message;
        this.to = to;
        priority = "high";
    }
}
