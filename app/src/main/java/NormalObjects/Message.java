package NormalObjects;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "messages")
public class Message implements Serializable {

    @PrimaryKey(autoGenerate = true)
    int p_key;

    //    message info
    private long messageID = -1;
    private String conversationID;
    private int conversationType;
    private String content;
    private String senderName;
    private String senderID;
    private String senderToken;
    private String recipientID;
    private String recipientToken;
    private String messageKind;
    private String recipientName;
    private String conversationName;

    //    location
    private String latitude;
    private String longitude;
    private String address;

    //    quote message
    private long quoteID;
    private String quoteMessage;
    private int quoteMessagePosition;
    private int quoteMessageType;

    //    message enums
    private int messageStatus;
    private int messageType;
    private int messageAction;

    //    message times
    private long sendingTime;
    private long arrivingTime;
    private long readingTime;
    private long editTime;

    private boolean star;
    private long starTime;

    //    message contacts
    private String contactName;
    private String contactNumber;

    private String filePath;
    private boolean fileSent;
    private int currentPlayTime;

    @Ignore
    private boolean selected;

    protected long previousMessageID; // the previous messageID
    protected long currentMessageID; // the current messageID
    protected long nextMessageID; // the next messageID

    private long lastUpdateTime;

//    @Ignore
//    private List<String>recipientsIds;

    public Message() {
        sendingTime = 0;
        arrivingTime = 0;
        readingTime = 0;
        editTime = 0;
        star = false;
        selected = false;
        nextMessageID = 0;
        lastUpdateTime = 0;
        fileSent = true;
//        recipientsIds = new ArrayList<>();
    }

    public int getP_key() {
        return p_key;
    }

    public void setP_key(int p_key) {
        this.p_key = p_key;
    }

    public long getMessageID() {
        return messageID;
    }

    public void setMessageID(long messageID) {
        this.messageID = messageID;
    }

    public String getConversationID() {
        return conversationID;
    }

    public void setConversationID(String conversationID) {
        this.conversationID = conversationID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getSenderToken() {
        return senderToken;
    }

    public void setSenderToken(String senderToken) {
        this.senderToken = senderToken;
    }

    public String getRecipientID() {
        return recipientID;
    }

    public void setRecipientID(String recipientID) {
        this.recipientID = recipientID;
    }

    public String getRecipientToken() {
        return recipientToken;
    }

    public void setRecipientToken(String recipientToken) {
        this.recipientToken = recipientToken;
    }

    public String getMessageKind() {
        return messageKind;
    }

    public void setMessageKind(String messageKind) {
        this.messageKind = messageKind;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getQuoteID() {
        return quoteID;
    }

    public void setQuoteID(long quoteID) {
        this.quoteID = quoteID;
    }

    public String getQuoteMessage() {
        return quoteMessage;
    }

    public void setQuoteMessage(String quoteMessage) {
        this.quoteMessage = quoteMessage;
    }

    public int getQuoteMessagePosition() {
        return quoteMessagePosition;
    }

    public void setQuoteMessagePosition(int quoteMessagePosition) {
        this.quoteMessagePosition = quoteMessagePosition;
    }

    public int getQuoteMessageType() {
        return quoteMessageType;
    }

    public void setQuoteMessageType(int quoteMessageType) {
        this.quoteMessageType = quoteMessageType;
    }

    public int getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(int messageStatus) {
        this.messageStatus = messageStatus;
        setReadingTime(System.currentTimeMillis());
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public int getMessageAction() {
        return messageAction;
    }

    public void setMessageAction(int messageAction) {
        this.messageAction = messageAction;
    }

    public long getSendingTime() {
        return sendingTime;
    }

    public void setSendingTime(long sendingTime) {
        this.sendingTime = sendingTime;
    }

    public long getArrivingTime() {
        return arrivingTime;
    }

    public void setArrivingTime(long arrivingTime) {
        this.arrivingTime = arrivingTime;
    }

    public long getReadingTime() {
        return readingTime;
    }

    public void setReadingTime(long readingTime) {
        this.readingTime = readingTime;
    }

    public long getEditTime() {
        return editTime;
    }

    public void setEditTime(long editTime) {
        this.editTime = editTime;
    }

    public boolean isStar() {
        return star;
    }

    public void setStar(boolean star) {
        this.star = star;
    }

    public long getStarTime() {
        return starTime;
    }

    public void setStarTime(long starTime) {
        this.starTime = starTime;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getConversationName() {
        return conversationName;
    }

    public void setConversationName(String conversationName) {
        this.conversationName = conversationName;
    }

//    public List<String> getRecipientsIds() {
//        return recipientsIds;
//    }

//    public void setRecipientsIds(List<String> recipientsIds) {
//        this.recipientsIds = recipientsIds;
//    }


    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public long getNextMessageID() {
        return nextMessageID;
    }

    public long getPreviousMessageID() {
        return previousMessageID;
    }

    public void setPreviousMessageID(long previousMessageID) {
        this.previousMessageID = previousMessageID;
    }

    public long getCurrentMessageID() {
        return currentMessageID;
    }

    public void setCurrentMessageID(long currentMessageID) {
        this.currentMessageID = currentMessageID;
    }

    public void setNextMessageID(long nextMessageID) {
        this.nextMessageID = nextMessageID;
    }

    public int getConversationType() {
        return conversationType;
    }

    public void setConversationType(int conversationType) {
        this.conversationType = conversationType;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public boolean isFileSent() {
        return fileSent;
    }

    public void setFileSent(boolean fileSent) {
        this.fileSent = fileSent;
    }

    public int getCurrentPlayTime() {
        return currentPlayTime;
    }

    public void setCurrentPlayTime(int currentPlayTime) {
        this.currentPlayTime = currentPlayTime;
    }

    @NonNull
    @Override
    public String toString() {
        return "sent by:" + senderName + " id: " + messageID + " at time: " + sendingTime;
    }
}
