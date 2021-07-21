package BroadcastReceivers;

import androidx.core.app.RemoteInput;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.Calendar;
import java.util.TimeZone;

import Consts.MessageType;
import Model.MessageSender;
import NormalObjects.Message;

import static android.content.Context.MODE_PRIVATE;

public class ReplyMessageBroadcast extends BroadcastReceiver {

    public interface NotificationReplyListener {
        void onReply(int notificationID);
        void onOpenMap(String geoString,int notificationID);
    }

    private static NotificationReplyListener callback;

    public void setListener(NotificationReplyListener listener) {
        callback = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {

            String replyText = (String) remoteInput.getCharSequence("key_text_reply");
            int notificationID = intent.getIntExtra("notificationID",-1);
            String sender = intent.getStringExtra("sender");
            String recipient = intent.getStringExtra("recipient");
            String conversationID = intent.getStringExtra("conversationID");
            String myName = intent.getStringExtra("MyName");
            String[] tokens = intent.getStringArrayExtra("tokens");
            CreateMessage(context,replyText,sender,myName,recipient,conversationID,tokens);
            callback.onReply(notificationID);

        }
        else {
            if (intent.hasExtra("geoString")) {
                String geoString = intent.getStringExtra("geoString");
                int notificationID = intent.getIntExtra("notificationID",-1);
                callback.onOpenMap(geoString,notificationID);
            }
            callback.onReply(-1);
        }
    }
    private void CreateMessage(Context context,String replyText,String sender,String myName,String recipient,String conversationID,String... recipientsTokens)
    {
        TimeZone timeZone = TimeZone.getTimeZone("GMT-4");
        Calendar calendar = Calendar.getInstance(timeZone);
        String time = calendar.getTimeInMillis() + "";
        String Current_time = System.currentTimeMillis() + "";
        Message message = new Message();
        message.setSender(sender);
        message.setSenderName(myName);
        message.setMessage(replyText);
        message.setMessageType(MessageType.textMessage.ordinal());
        message.setRecipient(recipient);
        message.setConversationID(conversationID);
        message.setMessageID(time);
        message.setMessageTime(Current_time);
        SharedPreferences sharedPreferences = context.getSharedPreferences("Token",MODE_PRIVATE);
        String token = sharedPreferences.getString("token","no token");
        if (!token.equals("no token"))
            message.setSenderToken(token);
        MessageSender messageSender = MessageSender.getInstance();
        messageSender.SendMessage(message,recipientsTokens);
    }
}
