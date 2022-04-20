package Messages;

import androidx.room.Ignore;

import java.util.ArrayList;
import java.util.List;

public class TextMessage extends BaseMessage
{
    private String message;
    private String sendingTime;
    private String arrivingTime;
    private String readTime;
    private String senderName;
    private String quotedMessageID;

    @Ignore
    private List<String>readBy;

    public TextMessage(String messageID, String conversationID, String senderToken, int messageType) {
        super(messageID, conversationID, senderToken,messageType);
        readBy = new ArrayList<>();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSendingTime() {
        return sendingTime;
    }

    public void setSendingTime(String sendingTime) {
        this.sendingTime = sendingTime;
    }

    public String getArrivingTime() {
        return arrivingTime;
    }

    public void setArrivingTime(String arrivingTime) {
        this.arrivingTime = arrivingTime;
    }

    public String getReadTime() {
        return readTime;
    }

    public void setReadTime(String readTime) {
        this.readTime = readTime;
    }

    public List<String> getReadBy() {
        return readBy;
    }

    public void setReadBy(List<String> readBy) {
        this.readBy = readBy;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getQuotedMessageID() {
        return quotedMessageID;
    }

    public void setQuotedMessageID(String quotedMessageID) {
        this.quotedMessageID = quotedMessageID;
    }


}
