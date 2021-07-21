package BackgroundDataToSend;

import BackgroundMessages.StatusMessage;

public class StatusToSend {
    public StatusMessage data;
    public String to;
    public String priority;

    public StatusToSend(StatusMessage message, String to) {
        this.data = message;
        this.to = to;
        priority = "high";
    }
}
