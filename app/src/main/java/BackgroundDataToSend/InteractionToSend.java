package BackgroundDataToSend;


import BackgroundMessages.InteractionMessage;

public class InteractionToSend {
    public InteractionMessage data;
    public String to;
    public String priority;

    public InteractionToSend(InteractionMessage message, String to) {
        this.data = message;
        this.to = to;
        priority = "high";
    }
}
