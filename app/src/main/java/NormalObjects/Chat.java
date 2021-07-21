package NormalObjects;

import java.io.Serializable;
import java.util.ArrayList;

@Deprecated
public class Chat implements Serializable {

    private String lastMessage,lastMessageTime,imagePath;
    private String conversationID,recipientUID,SenderUID;
    private ArrayList<Message>messages;

    public Chat() {
        messages = new ArrayList<>();
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getConversationID() {
        return conversationID;
    }

    public void setConversationID(String conversationID) {
        this.conversationID = conversationID;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public void addMessage(Message message){
        messages.add(message);
    }

    public String getRecipientUID() {
        return recipientUID;
    }

    public void setRecipientUID(String recipientUID) {
        this.recipientUID = recipientUID;
    }

    public String getSenderUID() {
        return SenderUID;
    }

    public void setSenderUID(String senderUID) {
        SenderUID = senderUID;
    }
}
