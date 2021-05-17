package NormalObjects;

public class ObjectToSend  {

    public Message data;
    public String to;

    public ObjectToSend(Message message, String to) {
        this.data= message;
        this.to = to;
    }
}
