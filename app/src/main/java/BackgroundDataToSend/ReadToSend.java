package BackgroundDataToSend;


import BackgroundMessages.ReadMessage;

public class ReadToSend {

    private ReadMessage data;
    private String to;
    private String priority;

    public ReadToSend(ReadMessage message, String to) {
        this.data = message;
        this.to = to;
        priority = "high";
    }
}
