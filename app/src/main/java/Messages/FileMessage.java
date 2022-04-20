package Messages;

import Consts.MessageType;

public class FileMessage extends TextMessage{

    private String filePath;


    public FileMessage(String messageID, String conversationID, String senderToken, int messageType) {
        super(messageID, conversationID, senderToken, messageType);
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
