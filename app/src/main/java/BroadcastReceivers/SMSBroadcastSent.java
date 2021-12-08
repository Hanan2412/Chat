package BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.woofmeow.ConversationActivity;

public class SMSBroadcastSent extends BroadcastReceiver {

    public static final String SENT_SMS_STATUS = "SENT_SMS_STATUS";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra("sending"))
        {
            LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(new Intent(SENT_SMS_STATUS)
                            .putExtra("messageID",intent.getStringExtra("messageID"))
                            .putExtra("status", ConversationActivity.MESSAGE_SENT));
        }
        else if (intent.hasExtra("delivered"))
        {
            LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(new Intent(SENT_SMS_STATUS)
                            .putExtra("messageID",intent.getStringExtra("messageID"))
                            .putExtra("status",ConversationActivity.MESSAGE_DELIVERED));
        }
        Log.e("SMS",SENT_SMS_STATUS);
    }
}
