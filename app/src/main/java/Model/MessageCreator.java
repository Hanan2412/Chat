package Model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.woofmeow.ConversationActivity2;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import Consts.MessageType;
import NormalObjects.Message;
import NormalObjects.User;

public class MessageCreator {

    private List<String> recipientsIds;
    private List<String> tokens;
    private List<String> recipientsNames;
    private String conversationID;
    private String groupName;
    private String sender;
    private String senderName;

    public static final String MESSAGE_SEEN = "MESSAGE_SEEN";
    public static final String MESSAGE_SENT = "MESSAGE_SENT";
    public static final String MESSAGE_WAITING = "MESSAGE_WAITING";
    public static final String MESSAGE_DELIVERED = "MESSAGE_DELIVERED";

    public MessageCreator()
    {

    }

    public MessageCreator(String conversationID, String groupName, String sender, String senderName)
    {
        this.conversationID = conversationID;
        this.groupName = groupName;
        this.sender = sender;
        this.senderName = senderName;
        recipientsIds = new ArrayList<>();
        recipientsNames = new ArrayList<>();
        tokens = new ArrayList<>();
    }

    public Message createBasicMessage(String messageContent, MessageType messageType) {
        String currentTime = System.currentTimeMillis() + "";
        TimeZone timeZone = TimeZone.getTimeZone("GMT-4");
        Calendar calendar = Calendar.getInstance(timeZone);
        String time = calendar.getTimeInMillis() + "";
        Message message = new Message();
        message.setMessage(messageContent);
        message.setSendingTime(currentTime);
        message.setConversationID(conversationID);
        for (String recipientId : recipientsIds)
            message.addRecipient(recipientId);
        for (String name : recipientsNames)
            message.addRecipientName(name);
        message.setGroupName(groupName);
        message.setSenderName(senderName);
        message.setSender(sender);
        message.setMessageStatus(MESSAGE_WAITING);
        message.setMessageType(messageType.ordinal());
        message.setMessageID(time);
        return message;
    }

    public void createQuoteMessage(@NonNull Message message, String quoteText, int quotedMessagePosition, String quotedMessageID)
    {
        message.setQuoteMessage(quoteText);
        message.setQuotedMessagePosition(quotedMessagePosition);
        message.setQuotedMessageID(quotedMessageID);
    }

    public void createContactMessage(@NonNull Message message, String contactName, String contactPhone)
    {
        message.setContactName(contactName);
        message.setContactPhone(contactPhone);
    }

    public void createGeoMessage(@NonNull Message message, String latitude, String longitude, String gpsAddress)
    {
        message.setLatitude(latitude);
        message.setLongitude(longitude);
        message.setLocationAddress(gpsAddress);
        message.setMessage("my location: " + gpsAddress);
    }

    public void createVoiceMessage(@NonNull Message message, String messageContent,String recordingPath)
    {
        message.setRecordingPath(recordingPath);
        message.setMessage("Voice Message");
    }

    public void createImageMessage(@NonNull Message message, String photoPath)
    {
        message.setImagePath(photoPath);
    }

    public void createLinkMessage(@NonNull Message message)
    {
        message.setMessageType(MessageType.webMessage.ordinal());
    }

    public void setConversationID(String conversationID)
    {
        this.conversationID = conversationID;
    }

    public void setRecipientsIds(List<String> recipientsIds) {
        this.recipientsIds = recipientsIds;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }

    public void setRecipientsNames(List<String> recipientsNames) {
        this.recipientsNames = recipientsNames;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }


    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}
