package NormalObjects;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
//Chat2 Object - a more up to date object to save a conversation at

public class Conversation implements Serializable {

    private String ConversationID;
    private String recipient;
    private String lastMessage;
    private String lastMessageTime;
    private String recipientImagePath;
    private ArrayList<Message>messages;
    private String senderName;
    private boolean muted = false;
    private int messageType;
    private String lastMessageID;
    private boolean typing;
    private String recipientName;
    private String recipientPhoneNumber;
    private boolean blocked = false;
    private String recipientToken;

    public Conversation(String conversationID)
    {
        this.ConversationID = conversationID;
        messages = new ArrayList<>();
    }

    public String getConversationID() {
        return ConversationID;
    }

    public void setConversationID(String conversationID) {
        ConversationID = conversationID;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        if(recipient != null) {
            if (!recipient.equals(this.recipient))
                Log.d("recipientChangeConversation", "recipient was changed in conversation. new recipient: " + recipient);
            this.recipient = recipient;
        }else
            Log.e("NULL", "conversation.java setting recipient is null" );
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
        if(lastMessageTime!=null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.parseLong(lastMessageTime));
            int minute = calendar.get(Calendar.MINUTE);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR);
            String minutes;
            if(minute<10)
                minutes = "0" + minute;
            else
                minutes = minute + "";
            this.lastMessageTime = day + "/" + month + "/" + year + "  " + hour + ":" + minutes;
        }
    }
    public void setLastMessageTimeFormatted(String lastMessageTime)
    {
        if(lastMessageTime!=null) {
            if (!lastMessageTime.contains("/"))
                parseTime(lastMessageTime);
            else this.lastMessageTime = lastMessageTime;
        }
    }

    public String getRecipientImagePath() {
        return recipientImagePath;
    }

    public void setRecipientImagePath(String recipientImagePath) {
       this.recipientImagePath = recipientImagePath;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public void addMessages(Message message)
    {
        messages.add(message);
    }

    public Message getMessage(int messageIndex)
    {
        return messages.get(messageIndex);
    }

    public void setLastMessage(Message lastMessage)
    {
        recipient = lastMessage.getRecipient();
        this.lastMessage = lastMessage.getMessage();
        lastMessageTime = lastMessage.getMessageTime();
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }



    public String getRecipientPhoneNumber() {
        return recipientPhoneNumber;
    }

    public void setRecipientPhoneNumber(String recipientPhoneNumber) {
        this.recipientPhoneNumber = recipientPhoneNumber;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;

    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getLastMessageID() {
        return lastMessageID;
    }

    public void setLastMessageID(String lastMessageID) {
        this.lastMessageID = lastMessageID;
    }

    public boolean isTyping() {
        return typing;
    }

    public void setTyping(boolean typing) {
        this.typing = typing;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public String getRecipientToken() {
        return recipientToken;
    }

    public void setRecipientToken(String recipientToken) {
        this.recipientToken = recipientToken;
    }

    public void setConversationMetaData(Message message)
    {
        lastMessageID = message.getMessageID();
        lastMessage = message.getMessage();
        parseTime(message.getArrivingTime());
        //lastMessageTime = message.getArrivingTime();
        messageType = message.getMessageType();
        recipientName = message.getSenderName();
        recipientToken = message.getSenderToken();
    }

    private void parseTime(String time)
    {
        if(time!=null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.parseLong(time));
            int minute = calendar.get(Calendar.MINUTE);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR);
            String minutes;
            if(minute<10)
                minutes = "0" + minute;
            else
                minutes = minute + "";
            this.lastMessageTime = day + "/" + month + "/" + year + "  " + hour + ":" + minutes;
        }
    }
}
