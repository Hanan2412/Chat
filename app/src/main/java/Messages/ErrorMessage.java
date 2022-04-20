package Messages;

import Consts.MessageType;

public class ErrorMessage extends BaseMessage {

    private String errorMessage;
    private String sendingTime;
    private String UID;

    public ErrorMessage(String messageID, String conversationID, String senderToken, int messageType, String errorMessage,String uid) {
        super(messageID, conversationID, senderToken, messageType);
        this.errorMessage = errorMessage;
        this.UID = uid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getSendingTime() {
        return sendingTime;
    }

    public void setSendingTime(String sendingTime) {
        this.sendingTime = sendingTime;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }
}
