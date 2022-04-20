package Messages;

import Consts.MessageType;

public class ActionMessage extends BaseMessage {
    public enum MessageAction{
        edit_message,
        delete_message,
        activity_start
    }
    private MessageAction messageAction;
    private String newMessageContent;
    private String editTime;
    private String deleteTime;

    public ActionMessage(String messageID, String conversationID, String senderToken, int messageType) {
        super(messageID, conversationID, senderToken, messageType);
    }

    public MessageAction getMessageAction() {
        return messageAction;
    }

    public void setMessageAction(MessageAction messageAction) {
        this.messageAction = messageAction;
    }

    public String getNewMessageContent() {
        return newMessageContent;
    }

    public void setNewMessageContent(String newMessageContent) {
        this.newMessageContent = newMessageContent;
    }

    public String getEditTime() {
        return editTime;
    }

    public void setEditTime(String editTime) {
        this.editTime = editTime;
    }

    public String getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(String deleteTime) {
        this.deleteTime = deleteTime;
    }
}
