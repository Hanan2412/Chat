package Messages;

import androidx.room.Ignore;

import java.util.List;


public abstract class BaseMessage {

    private String messageID;
    private String conversationID;
    private String senderToken;
//    @Ignore
//    private List<String> recipients;
    private int messageType;

    public BaseMessage(String messageID, String conversationID, String senderToken, int messageType) {
        this.messageID = messageID;
        this.conversationID = conversationID;
        this.senderToken = senderToken;
        this.messageType = messageType;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getConversationID() {
        return conversationID;
    }

    public void setConversationID(String conversationID) {
        this.conversationID = conversationID;
    }

    public String getSenderToken() {
        return senderToken;
    }

    public void setSenderToken(String senderToken) {
        this.senderToken = senderToken;
    }

//    public List<String> getRecipients() {
//        return recipients;
//    }
//
//    public void setRecipients(List<String> recipients) {
//        this.recipients = recipients;
//    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }
}
