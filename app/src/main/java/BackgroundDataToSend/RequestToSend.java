package BackgroundDataToSend;

import BackgroundMessages.RequestMessage;

public class RequestToSend {
    public RequestMessage data;
    public String to;
    public String priority;

    public RequestToSend(RequestMessage message, String to) {
        this.data = message;
        this.to = to;
        priority = "high";
    }
}
