package BroadcastReceivers;

import androidx.core.app.RemoteInput;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ReplyMessageBroadcast extends BroadcastReceiver {

    public interface NotificationReplyListener {
        void onReply(String replyText,int notificationID,String sender,String myName,String recipient,String conversationID);
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
            callback.onReply(replyText,notificationID,sender,myName,recipient,conversationID);

        }
        else {
            if (intent.hasExtra("geoString")) {
                String geoString = intent.getStringExtra("geoString");
                int notificationID = intent.getIntExtra("notificationID",-1);
                callback.onOpenMap(geoString,notificationID);
            }
            callback.onReply(null,-1,"-1","-1","-1","-1");
        }
    }


}
