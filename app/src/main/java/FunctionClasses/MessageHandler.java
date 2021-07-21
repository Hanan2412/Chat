package FunctionClasses;
//NOT FINISHED
import java.util.Calendar;
import java.util.TimeZone;

import Consts.MessageType;
import NormalObjects.Message;

public class MessageHandler {

    public MessageHandler() {

    }

    public Message CreateMessage(String conversationID, String messageText, String recipientID, String sender, String senderName) {
        Message message = new Message();
        message.setConversationID(conversationID);
        message.setMessage(messageText);
        message.setMessageType(MessageType.textMessage.ordinal());
        message.setRecipient(recipientID);
        message.setSender(sender);
        message.setSenderName(senderName);
        MessageTimes(message);

        return message;
    }

    public void CreateSpecialMessage(Message message,String longitude,String latitude,String address,MessageType messageType) {

        if (messageType == MessageType.gpsMessage) {
            message.setLongitude(longitude);
            message.setLatitude(latitude);
            message.setLocationAddress(address);
        }
        message.setMessageType(messageType.ordinal());
    }

    public void SetQuoteMessage(Message message,String quote,int quotePosition,String quoteMessageID)
    {
        message.setQuotedMessageID(quoteMessageID);
        message.setQuotedMessagePosition(quotePosition);
        message.setQuoteMessage(quote);
    }

    private void MessageTimes(Message message)
    {
        TimeZone timeZone = TimeZone.getTimeZone("GMT-4");
        Calendar calendar = Calendar.getInstance(timeZone);
        String time = calendar.getTimeInMillis() + "";
        String currentTime = System.currentTimeMillis() + "";
        message.setMessageTime(currentTime);
        message.setMessageID(time);
    }

    public void UpdateMessageData(Message message,String conversationID,String token)
    {
        message.setConversationID(conversationID);
        message.setTo(token);
    }
}
