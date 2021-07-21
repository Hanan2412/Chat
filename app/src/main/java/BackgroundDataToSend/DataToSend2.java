package BackgroundDataToSend;

import BackgroundMessages.DataMessage2;

public class DataToSend2 {

    public DataMessage2 data;
    public String to;
    public String priority;

    public DataToSend2(DataMessage2 message, String to) {
        this.data = message;
        this.to = to;
        priority = "high";
    }
}
