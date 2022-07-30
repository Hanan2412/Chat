package NormalObjects;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import Consts.MessageAction;

@Entity(tableName = "messageHistory")
public class MessageHistory implements Serializable {


    @PrimaryKey(autoGenerate = true)
    int p_key;
    private String messageID;

    private String message;
    private String sender;
    //private String recipient;
    private String senderName;
    private String latitude;
    private String longitude;
    private String locationAddress;
    private String to;
    private String conversationID;
    private String messageTime;
    private String sendingTime;
    private String arrivingTime;
    private String imagePath;
    private String quoteMessage;
    private int messageType = -1;
    private boolean hasBeenRead = false;
    private String messageStatus="";
    private String recordingPath;
    private int quotedMessagePosition = -1;
    private String quotedMessageID;
    private long readAt=-1;
    @Ignore
    private boolean star = false;
    private String starTime;
    //private String recipientName;
    private MessageAction messageAction = MessageAction.new_message;
    private String filePath;
    private String editTime;
    private String senderToken;
    private String messageKind = "newMessage";
    private String contactName,contactPhone;
    @Ignore
    private boolean uploading = false;
    @Ignore
    private boolean error = false;
    @Ignore
    private boolean sent = true;
    @Ignore
    private List<String>recipients;
    @Ignore
    private List<String>recipientNames;
    private String groupName;

    public MessageHistory()
    {
        recipients = new ArrayList<>();
        recipientNames = new ArrayList<>();
    }
    public MessageHistory(Message message)
    {
        setMessageID(message.getMessageID());
        setMessage(message.getMessage());
        setArrivingTime(message.getArrivingTime());
        setConversationID(message.getConversationID());
        setFilePath(message.getFilePath());
        setGroupName(message.getGroupName());
        setImagePath(message.getImagePath());
        setEditTime(message.getEditTime());
        setLatitude(message.getLatitude());
        setLongitude(message.getLongitude());
        setLocationAddress(message.getLocationAddress());
        setSender(message.getSender());
        setSenderName(message.getSenderName());
        setSenderToken(message.getSenderToken());
        setSendingTime(message.getSendingTime());
        setMessageStatus(message.getMessageStatus());
    }

    public void setP_key(int p_key) {
        this.p_key = p_key;
    }

    public int getP_key() {
        return p_key;
    }

    public boolean isUploading() {
        return uploading;
    }

    public void setUploading(boolean uploading) {
        this.uploading = uploading;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public void setRecipientNames(List<String> recipientNames) {
        this.recipientNames = recipientNames;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setRecipients(List<String>recipients){this.recipients = recipients;}
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void addRecipient(String recipient){
        recipients.add(recipient);
    }

    public String getRecipient(int i){
        return recipients.get(i);
    }

    public List<String> getRecipients(){
        return recipients;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getMessageKind() {
        return messageKind;
    }

    public void setMessageKind(String messageKind) {
        this.messageKind = messageKind;
    }

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

    /*public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
*/
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

    public List<String>getRecipientNames()
    {
        return recipientNames;
    }

    public void addRecipientName(String name)
    {
        recipientNames.add(name);
    }

    public String getRecipientName(int i){
        return recipientNames.get(i);
    }
    /*public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }*/

    public MessageAction getMessageAction() {
        return messageAction;
    }

    public void setMessageAction(MessageAction messageAction) {
        this.messageAction = messageAction;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getEditTime() {
        return editTime;
    }

    public void setEditTime(String editTime) {
        this.editTime = editTime;
    }

    public String getSenderToken() {
        return senderToken;
    }

    public void setSenderToken(String senderToken) {
        this.senderToken = senderToken;
    }


    @Override
    public String toString() {
        return "Message{" +
                "message='" + message + '\'' +
                ", sender='" + sender + '\'' +
                ", senderName='" + senderName + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", locationAddress='" + locationAddress + '\'' +
                ", messageID='" + messageID + '\'' +
                ", to='" + to + '\'' +
                ", conversationID='" + conversationID + '\'' +
                ", messageTime='" + messageTime + '\'' +
                ", sendingTime='" + sendingTime + '\'' +
                ", arrivingTime='" + arrivingTime + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", quoteMessage='" + quoteMessage + '\'' +
                ", messageType=" + messageType +
                ", hasBeenRead=" + hasBeenRead +
                ", messageStatus='" + messageStatus + '\'' +
                ", recordingPath='" + recordingPath + '\'' +
                ", quotedMessagePosition=" + quotedMessagePosition +
                ", quotedMessageID='" + quotedMessageID + '\'' +
                ", readAt=" + readAt +
                ", star=" + star +
                ", starTime='" + starTime + '\'' +
                ", messageAction=" + messageAction +
                ", filePath='" + filePath + '\'' +
                ", editTime='" + editTime + '\'' +
                ", senderToken='" + senderToken + '\'' +
                ", messageKind='" + messageKind + '\'' +
                ", contactName='" + contactName + '\'' +
                ", contactPhone='" + contactPhone + '\'' +
                ", recipients=" + recipients +
                ", recipientNames=" + recipientNames +
                ", groupName='" + groupName + '\'' +
                '}';
    }
}
