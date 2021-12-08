package BroadcastReceivers;

import static android.content.Context.MODE_PRIVATE;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.example.woofmeow.ConversationActivity;
import com.example.woofmeow.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import Consts.ConversationType;
import Consts.MessageType;
import DataBase.DBActive;
import Model.NewMessage;
import NormalObjects.Message;
import NormalObjects.User;

//reads incoming sms messages
public class SMSBroadcast extends BroadcastReceiver implements NewMessage {
    //private static final String pdu_type = "pdus";
    @SuppressWarnings("FieldMayBeFinal")
    private List<String> conversations;
    private DBActive db;
    private Context context;

    public SMSBroadcast()
    {
        conversations = new ArrayList<>();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        db = DBActive.getInstance(context);
        this.context = context;
        Message msg = new Message();
        SmsMessage smsMessage = Telephony.Sms.Intents.getMessagesFromIntent(intent)[0];
        String phone = smsMessage.getDisplayOriginatingAddress();
        String message = smsMessage.getMessageBody();

        msg.setMessageType(MessageType.sms.ordinal());
        msg.setMessage(message);
        msg.setContactPhone(phone);
        String currentTime = System.currentTimeMillis() + "";
        TimeZone timeZone = TimeZone.getTimeZone("GMT-4");
        Calendar calendar = Calendar.getInstance(timeZone);
        String time = calendar.getTimeInMillis() + "";
        msg.setMessageID(time);
        msg.setMessageTime(currentTime);
        msg.setMessageStatus(ConversationActivity.MESSAGE_DELIVERED);
        msg.setArrivingTime(currentTime);
        msg.setSender(phone);
        msg.setSenderName("");
        String conversationID = db.findConversationByUserPhone(phone);//will be null if it doesn't exist
        Intent newSmsIntent = new Intent();
        newSmsIntent.putExtra("message", msg);
        newSmsIntent.putExtra("messageID",msg.getMessageID());
        if (conversationID != null) {
            newSmsIntent.putExtra("conversationID",conversationID);
            msg.setConversationID(conversationID);
            String groupName = db.loadConversationName(conversationID);
            msg.setGroupName(groupName);
            db.updateConversation(msg);
            newSmsIntent.setAction("Update Conversation");
        }
        else
        {
            conversationID = createConversationID();
            msg.setConversationID(conversationID);
            msg.setGroupName(phone);
            String userID = saveUser(phone);
            msg.addRecipient(userID);
            msg.setSenderName("");
            db.createNewConversation(msg, ConversationType.sms);
            newSmsIntent.putExtra("conversationID",conversationID);
            newSmsIntent.putExtra("messageID",msg.getMessageID());
            newSmsIntent.setAction("New Conversation");
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(newSmsIntent);
        if (!isConversationBlocked(conversationID))
        {
            saveMessage(msg);
            if (!isConversationOpen(conversationID)) {
                //alerts tab fragment that a new message has arrived
                if (isNotificationsAllowed())
                {
                    if (!isConversationMuted(conversationID))
                    {
                        conversations.add(conversationID);
                        createNotification(msg);
                    }
                }
            }
        }
    }

    private String saveUser(String phoneNumber)
    {
        User user = new User();
        user.setUserUID(UUID.randomUUID()+"");
        user.setPhoneNumber(phoneNumber);
        user.setName(phoneNumber);
        user.setLastName("");
        db.insertUser(user);
        return user.getUserUID();
    }

    @Override
    public boolean isConversationExists(String conversationID) {
        return db.isConversationExists(conversationID);
    }

    @Override
    public boolean isNotificationsAllowed() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("allowNotifications", true);
    }

    @Override
    public boolean isConversationBlocked(String conversationID) {
        return db.isConversationBlocked(conversationID);
    }

    @Override
    public boolean isUserBlocked(String userID) {
        return false;
    }

    @Override
    public boolean isConversationOpen(String conversationID) {
        SharedPreferences conversationPreferences = context.getSharedPreferences("Conversation", MODE_PRIVATE);
        String liveConversation = conversationPreferences.getString("liveConversation", "no conversation");
        return liveConversation.equals(conversationID);
    }

    @Override
    public boolean isConversationMuted(String conversationID) {
        return db.isMuted(conversationID);
    }

    @Override
    public boolean isUserMuted(String userID) {
        return false;
    }

    private String createConversationID() {
        return "S_" + System.currentTimeMillis();
    }

    private void saveMessage(Message msg) {
        db.saveMessage(msg);
    }

    private void createNotification(Message msg)
    {
        createNotificationChannel();
        String notificationTitle;
        int notificationID = getNotificationID(msg.getConversationID());
        if (msg.getSenderName() != null)
            notificationTitle = "New Message From " + msg.getSenderName();
        else
            notificationTitle = "New Message From " + msg.getContactPhone();
        Intent tapOnNotification = new Intent(context, ConversationActivity.class);
        tapOnNotification.putExtra("conversationID",msg.getConversationID());
        tapOnNotification.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,notificationID,tapOnNotification,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_CANCEL_CURRENT );
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"Receive_SMS_Messages_Channel")
                .setSmallIcon(R.drawable.ic_baseline_sms_24)
                .setContentTitle(notificationTitle)
                .setContentText(msg.getMessage())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setNotificationSilent()
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        NotificationManagerCompat nmc = NotificationManagerCompat.from(context);
        nmc.notify(notificationID,builder.build());
    }

    private int getNotificationID(String conversationID)
    {
        int i = 0;
        if (conversations.isEmpty())
            return i;
        for (String conversation : conversations)
        {
            if (conversation.equals(conversationID))
                break;
            else
                i++;
        }
        return i;
    }

    private void createNotificationChannel()
    {
        String channelName = "ReceiveSMSMessages";
        String channelDescription = "Receive sms messages";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        String channelID = "Receive_SMS_Messages_Channel";
        NotificationChannel channel = new NotificationChannel(channelID, channelName, importance);
        channel.setDescription(channelDescription);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        assert notificationManager != null;
        notificationManager.createNotificationChannel(channel);
    }
}
