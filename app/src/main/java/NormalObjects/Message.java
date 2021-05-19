package NormalObjects;
import java.io.Serializable;

public class Message implements Serializable {

    private String message;
    private String sender;
    private String recipient;
    private String senderName;
    private String latitude;
    private String longitude;
    private String locationAddress;
    private String messageID;
    private String to;
    private String conversationID;
    private String messageTime;
    private String imagePath;
    private String quoteMessage;
    private int messageType = -1;
    private boolean hasBeenRead = false;
    private String messageStatus="";
    private String recordingPath;
    private int quotedMessagePosition = -1;
    private String quotedMessageID;
    private long readAt=-1;
    private boolean star = false;
    private String starTime;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
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

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public boolean isHasBeenRead() {
        return hasBeenRead;
    }

    public void setHasBeenRead(boolean hasBeenRead) {
        this.hasBeenRead = hasBeenRead;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getConversationID() {
        return conversationID;
    }

    public void setConversationID(String conversationID) {
        this.conversationID = conversationID;
    }

    public String getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(String messageTime) {
        this.messageTime = messageTime;
    }


    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(String messageStatus) {
        this.messageStatus = messageStatus;
    }

    public String getQuoteMessage() {
        return quoteMessage;
    }

    public void setQuoteMessage(String quoteMessage) {
        this.quoteMessage = quoteMessage;
    }

    public String getRecordingPath() {
        return recordingPath;
    }

    public void setRecordingPath(String recordingPath) {
        this.recordingPath = recordingPath;
    }


    public int getQuotedMessagePosition() {
        return quotedMessagePosition;
    }

    public void setQuotedMessagePosition(int quotedMessagePosition) {
        this.quotedMessagePosition = quotedMessagePosition;
    }

    public String getQuotedMessageID() {
        return quotedMessageID;
    }

    public void setQuotedMessageID(String quotedMessageID) {
        this.quotedMessageID = quotedMessageID;
    }

    public long getReadAt() {
        return readAt;
    }

    public void setReadAt(long readAt) {
        this.readAt = readAt;
    }

    public boolean isStar() {
        return star;
    }

    public void setStar(boolean star) {
        this.star = star;
    }

    public String getStarTime() {
        return starTime;
    }

    public void setStarTime(String starTime) {
        this.starTime = starTime;
    }
}
