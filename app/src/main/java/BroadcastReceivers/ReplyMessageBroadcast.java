package BroadcastReceivers;
import androidx.core.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import Consts.MessageAction;
import Consts.MessageStatus;
import Consts.MessageType;
import Model.MessageSender;
import NormalObjects.Message;
import Time.StandardTime;

import static android.content.Context.MODE_PRIVATE;

public class ReplyMessageBroadcast extends BroadcastReceiver {

    public interface NotificationReplyListener {
        void onReply(int notificationID);
        void onOpenMap(String geoString,int notificationID);
        void onMute(String conversationID);
    }

    private static NotificationReplyListener callback;

    public void setListener(NotificationReplyListener listener) {
        callback = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationID = intent.getIntExtra("notificationID",-1);
        String senderName = intent.getStringExtra("senderName");
        String senderUID = intent.getStringExtra("senderUID");
        String conversationID = intent.getStringExtra("conversationID");
        String token = intent.getStringExtra("token");
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            String replyText = (String) remoteInput.getCharSequence("key_text_reply");
            List<String>tokens = new ArrayList<>();
            tokens.add(token);
//            createMessage(context,replyText,sender,senderUID,conversationID,tokens);
            Message message = createMessage(replyText,senderName, senderUID, conversationID);
            MessageSender.getInstance().sendMessage(message, tokens);
            callback.onReply(notificationID);

        }
        else {
            if (intent.getAction().equals("mute"))
            {
                callback.onMute(conversationID);
                callback.onReply(notificationID);
            }
            else if (intent.hasExtra("geoString")) {
                String geoString = intent.getStringExtra("geoString");
                callback.onOpenMap(geoString,notificationID);
                callback.onReply(-1);
            }

        }
    }

    private Message createMessage(String content, String senderName, String senderUID,String conversationID)
    {
        Message message = new Message();
        message.setMessageID(StandardTime.getInstance().getStandardTime());
        message.setSenderID(senderUID);
        message.setMessageAction(MessageAction.new_message.ordinal());
        message.setMessageStatus(MessageStatus.WAITING.ordinal());
        message.setSendingTime(StandardTime.getInstance().getCurrentTime());
        message.setConversationID(conversationID);
        message.setContent(content);
        message.setSenderName(senderName);
        message.setMessageType(MessageType.textMessage.ordinal());
        return message;
    }

    private void createMessage(Context context, String replyText, String sender, String recipient, String conversationID, String myName,List<String> recipientsTokens)
    {
        TimeZone timeZone = TimeZone.getTimeZone("GMT-4");
        Calendar calendar = Calendar.getInstance(timeZone);
        String time = calendar.getTimeInMillis() + "";
        long Current_time = System.currentTimeMillis();
        Message message = new Message();
        message.setSenderID(sender);
        message.setSenderName(myName);
        message.setContent(replyText);
        message.setMessageType(MessageType.textMessage.ordinal());
        //message.setRecipient(recipient);
        message.setConversationName(recipient);
        message.setConversationID(conversationID);
        message.setMessageID(StandardTime.getInstance().getStandardTime());
        message.setSendingTime(Current_time);
        SharedPreferences sharedPreferences = context.getSharedPreferences("Token",MODE_PRIVATE);
        String token = sharedPreferences.getString("token","no token");
        if (!token.equals("no token"))
            message.setSenderToken(token);
        MessageSender messageSender = MessageSender.getInstance();
        messageSender.sendMessage(message,recipientsTokens);
    }
}
