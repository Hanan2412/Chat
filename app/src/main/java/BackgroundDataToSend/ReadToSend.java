package BackgroundDataToSend;


import BackgroundMessages.ReadMessage;

public class ReadToSend {

    public ReadMessage data;
    public String to;
    public String priority;

    public ReadToSend(ReadMessage message, String to) {
        this.data = message;
        this.to = to;
        priority = "high";
    }
}
