package NormalObjects;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "messageViews")
public class MessageViews implements Serializable {

    @PrimaryKey(autoGenerate = true)
    int p_key;

    private String uid; // the user who got the message
    private String userName; // the users name
    private String conversationID; // the conversationID of which the message belongs to
    private long messageID; // the messageID that was gotten
    private int messageStatus; // the status of the user towards the message - read, received, etc
    private long readTime; // the time the message was read by the user
    private long deliveredTime; // the time the message was delivered to the user
    private long sendingTime; // the time the message was sent

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getConversationID() {
        return conversationID;
    }

    public void setConversationID(String conversationID) {
        this.conversationID = conversationID;
    }

    public long getMessageID() {
        return messageID;
    }

    public void setMessageID(long messageID) {
        this.messageID = messageID;
    }

    public int getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(int messageStatus) {
        this.messageStatus = messageStatus;
    }

    public long getReadTime() {
        return readTime;
    }

    public void setReadTime(long readTime) {
        this.readTime = readTime;
    }

    public long getDeliveredTime() {
        return deliveredTime;
    }

    public void setDeliveredTime(long deliveredTime) {
        this.deliveredTime = deliveredTime;
    }

    public long getSendingTime() {
        return sendingTime;
    }

    public void setSendingTime(long sendingTime) {
        this.sendingTime = sendingTime;
    }

    public int getP_key() {
        return p_key;
    }

    public void setP_key(int p_key) {
        this.p_key = p_key;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
