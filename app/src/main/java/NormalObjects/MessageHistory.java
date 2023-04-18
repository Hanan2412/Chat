package NormalObjects;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

// dual linked list design
@Entity(tableName = "messageHistory")
public class MessageHistory extends Message implements Serializable {

    @PrimaryKey(autoGenerate = true)
    int p_key;

    public MessageHistory() {
        nextMessageID = 0;
    }

    public int getP_key() {
        return p_key;
    }

    public void setP_key(int p_key) {
        this.p_key = p_key;
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

    public void copyMessage(Message message)
    {
        setConversationID(message.getConversationID());
        setContent(message.getContent());
        setSenderName(message.getSenderName());
        setSenderID(message.getSenderID());
        setSenderToken(message.getSenderToken());
        setRecipientID(message.getRecipientID());
        setRecipientToken(message.getRecipientToken());
        setMessageKind(message.getMessageKind());
        setRecipientName(message.getRecipientName());
        setConversationName(message.getConversationName());

        setLatitude(message.getLatitude());
        setLongitude(message.getLongitude());
        setAddress(message.getAddress());

        setQuoteID(message.getQuoteID());
        setQuoteMessage(message.getQuoteMessage());
        setQuoteMessagePosition(message.getQuoteMessagePosition());
        setQuoteMessageType(message.getQuoteMessageType());

        setMessageStatus(message.getMessageStatus());
        setMessageType(message.getMessageType());
        setMessageAction(message.getMessageAction());

        setSendingTime(message.getSendingTime());
        setArrivingTime(message.getArrivingTime());
        setReadingTime(message.getReadingTime());
        setEditTime(message.getEditTime());

        setContactName(message.getContactName());
        setContactNumber(message.getContactNumber());
        setFilePath(message.getFilePath());

        setPreviousMessageID(message.getMessageID());
        setMessageID(message.getMessageID());
    }
}
