package NormalObjects;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import Consts.ConversationType;
import Time.StandardTime;
import Time.TimeFormat;


@Entity(tableName = "conversations")
public class Conversation implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int p_key;

    private String conversationID;
    //@Ignore
    private String recipient;
    private String lastMessage;
    private long lastMessageTime;
    private String lastMessageTimeParse;
    private String recipientImagePath;

    @Ignore
    private ArrayList<Message>messages;
    private String senderName;
    private boolean muted = false;
    private int messageType;
    private long lastMessageID;
    private boolean typing;
    private boolean recording;
    private String recipientName;
    private String recipientPhoneNumber;
    private boolean blocked = false;
    private String recipientToken;
    @Ignore
    private List<String> recipients;
    @Ignore
    private List<String>tokens;
    private String lastMessageRecipient;
    private String conversationName;
    private int conversationType = ConversationType.single.ordinal();
    private int type = 0;
    private boolean pinned;
    private int unreadMessages = 0;
    private long lastUpdate;

    @Ignore
    TimeFormat timeFormat = new TimeFormat();

    public Conversation(@NonNull String conversationID)
    {
        this.conversationID = conversationID;
        messages = new ArrayList<>();
        tokens = new ArrayList<>();
        recipients = new ArrayList<>();
        typing = false;
        recording = false;
    }

    public void setP_key(int p_key) {
        this.p_key = p_key;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getP_key() {
        return p_key;
    }

    public int getConversationType() {
        return conversationType;
    }

    public void setConversationType(int conversationType) {
        this.conversationType = conversationType;
        setLastUpdate(StandardTime.getInstance().getStandardTime());
    }

    public String getConversationName() {
        return conversationName;
    }

    public void setConversationName(String conversationName) {
        this.conversationName = conversationName;
        setLastUpdate(StandardTime.getInstance().getStandardTime());
    }

    public String getConversationID() {
        return conversationID;
    }

    public void setConversationID(String conversationID) {
        this.conversationID = conversationID;
        setLastUpdate(StandardTime.getInstance().getStandardTime());
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
        setLastUpdate(StandardTime.getInstance().getStandardTime());
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
        setLastMessageTimeParse(timeFormat.getFormattedDate(lastMessageTime));
        setLastUpdate(StandardTime.getInstance().getStandardTime());
    }
    public void setLastMessageTimeFormatted(String lastMessageTime)
    {
//        if(lastMessageTime!=null) {
//            if (!lastMessageTime.contains("/"))
//                parseTime(Long.parseLong(lastMessageTime));
//            else this.lastMessageTime = lastMessageTime;
//        }
    }

    public void addRecipient(String uid){
        recipients.add(uid);
    }

    public void addToken(String token)
    {
        tokens.add(token);
    }
    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }

    public String getLastMessageRecipient() {
        return lastMessageRecipient;
    }

    public void setLastMessageRecipient(String lastMessageRecipient) {
        this.lastMessageRecipient = lastMessageRecipient;
        setLastUpdate(StandardTime.getInstance().getStandardTime());
    }

    public String getRecipientImagePath() {
        return recipientImagePath;
    }

    public void setRecipientImagePath(String recipientImagePath) {
       this.recipientImagePath = recipientImagePath;
        setLastUpdate(StandardTime.getInstance().getStandardTime());
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
        lastMessageRecipient = lastMessage.getSenderID();
        this.lastMessage = lastMessage.getContent();
        lastMessageTime = lastMessage.getSendingTime();
        setLastUpdate(StandardTime.getInstance().getStandardTime());
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
        setLastUpdate(StandardTime.getInstance().getStandardTime());
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        setLastUpdate(StandardTime.getInstance().getStandardTime());
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public long getLastMessageID() {
        return lastMessageID;
    }

    public void setLastMessageID(long lastMessageID) {
        this.lastMessageID = lastMessageID;
        setLastUpdate(StandardTime.getInstance().getStandardTime());
//        setLastUpdate(lastMessageID);
    }

    public boolean isTyping() {
        return typing;
    }

    public void setTyping(boolean typing) {
        this.typing = typing;
        setLastUpdate(StandardTime.getInstance().getStandardTime());
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
        setLastUpdate(StandardTime.getInstance().getStandardTime());
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
        setLastUpdate(StandardTime.getInstance().getStandardTime());
    }

    public String getRecipientToken() {
        return recipientToken;
    }

    public void setRecipientToken(String recipientToken) {
        this.recipientToken = recipientToken;
    }

    private void parseTime(long time)
    {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time);
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
            String hour1;
            if (hour<10)
                hour1 = "0" + hour;
            else
                hour1 = hour + "";
            this.lastMessageTimeParse = day + "/" + month + "/" + year + "  " + hour1 + ":" + minutes;

    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
        setLastUpdate(StandardTime.getInstance().getStandardTime());
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public String getLastMessageTimeParse() {
        return lastMessageTimeParse;
    }

    public void setLastMessageTimeParse(String lastMessageTimeParse) {
        this.lastMessageTimeParse = lastMessageTimeParse;
        parseTime(lastMessageTime);
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
        Log.d("CONVERSATION_OBJECT", "last update time: " + lastUpdate);
    }

    public int getUnreadMessages() {
        return unreadMessages;
    }

    public void setUnreadMessages(int unreadMessages) {
        this.unreadMessages = unreadMessages;
        setLastUpdate(StandardTime.getInstance().getStandardTime());
    }

    public boolean isRecording() {
        return recording;
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
    }
}
