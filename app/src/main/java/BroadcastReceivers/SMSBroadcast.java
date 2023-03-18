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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;

import com.example.woofmeow.ConversationActivity2;
import com.example.woofmeow.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;

import Backend.ChatDao;
import Backend.ChatDataBase;

import Consts.ConversationType;
import Consts.MessageStatus;
import Consts.MessageType;

import Controller.NotificationsController;
import NormalObjects.Conversation;
import NormalObjects.Group;
import NormalObjects.Message;
import NormalObjects.User;

//reads incoming sms messages
@SuppressWarnings({"AnonymousHasLambdaAlternative", "Convert2Lambda"})
public class SMSBroadcast extends BroadcastReceiver {
    @SuppressWarnings("FieldMayBeFinal")
    private List<String> conversations;
    private Context context;
    private final String currentUser = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    private ChatDao dao;
    private boolean blocked = false;

    public SMSBroadcast() {
        conversations = new ArrayList<>();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ChatDataBase chatDataBase = ChatDataBase.getInstance(context);
        dao = chatDataBase.chatDao();
        this.context = context;
        Message msg = new Message();
        SmsMessage smsMessage = Telephony.Sms.Intents.getMessagesFromIntent(intent)[0];
        String phone = smsMessage.getDisplayOriginatingAddress();
        String message = smsMessage.getMessageBody();
        LiveData<Boolean>blockedUser = dao.isUserBlocked(phone);
        Observer<Boolean>blockedObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean != null)
                    blocked = aBoolean;
            }
        };
        blockedUser.observeForever(blockedObserver);
        msg.setMessageType(MessageType.sms.ordinal());
        msg.setContent(message);
        msg.setContactNumber(phone);
        long currentTime = System.currentTimeMillis();
        TimeZone timeZone = TimeZone.getTimeZone("GMT-4");
        Calendar calendar = Calendar.getInstance(timeZone);
        String time = calendar.getTimeInMillis() + "";
        msg.setMessageID(time);
        msg.setArrivingTime(currentTime);
        msg.setMessageStatus(MessageStatus.DELIVERED.ordinal());
        msg.setArrivingTime(currentTime);
        msg.setSenderID(phone);
        msg.setSenderName(phone);
        msg.setSendingTime(System.currentTimeMillis());
        msg.setConversationName(phone);
        LiveData<String> conversationLiveData = dao.getConversationIDByPhone(phone);
        Observer<String> observer = new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (!blocked) {
                    if (s != null) {
                        msg.setConversationID(s);
                        LiveData<Conversation> conversation = dao.getConversation(s);
                        Observer<Conversation> conversationObserver = new Observer<Conversation>() {
                            @Override
                            public void onChanged(Conversation conversation1) {
                                if (conversation1.getConversationName() != null)
                                    msg.setConversationName(conversation1.getConversationName());
                                Thread thread = new Thread() {
                                    @Override
                                    public void run() {
                                        dao.updateConversation(msg.getContent(), msg.getMessageID(), msg.getMessageType(), msg.getArrivingTime(), msg.getConversationName(), msg.getConversationID());
                                    }
                                };
                                thread.setName("update conversation");
                                thread.start();
                                conversation.removeObserver(this);
                                sendNotification(conversation1.getConversationID(), msg);
                            }
                        };
                        conversation.observeForever(conversationObserver);
                    } else {
                        s = createConversationID();
                        msg.setConversationID(s);
                        msg.setConversationName(phone);
                        String userID = saveUser(phone);
                        msg.setRecipientID(userID);
                        msg.setSenderName("");
                        createNewConversation(msg, currentUser, ConversationType.sms);
                        conversations.add(s);
                        createNotification(msg);
                    }
                    saveMessage(msg);
                }
                conversationLiveData.removeObserver(this);
            }
        };
        conversationLiveData.observeForever(observer);
    }

    private void sendNotification(String conversationID, Message msg) {
        LiveData<Boolean> blocked = dao.isConversationBlocked(conversationID);
        Observer<Boolean> observer = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (!aBoolean) {
                    //saveMessage(msg);
                    if (!isConversationOpen(conversationID)) {
                        if (isNotificationsAllowed()) {
                            LiveData<Boolean> muted = dao.isConversationMuted(conversationID);
                            Observer<Boolean> mutedConversationObserver = new Observer<Boolean>() {
                                @Override
                                public void onChanged(Boolean aBoolean) {
                                    if (!aBoolean) {
                                        conversations.add(conversationID);
                                        createNotification(msg);
                                        muted.removeObserver(this);
                                    }
                                }
                            };
                            muted.observeForever(mutedConversationObserver);
                        }
                    }
                }
                blocked.removeObserver(this);
            }
        };
        blocked.observeForever(observer);
    }


    private String saveUser(String phoneNumber) {
        User user = new User();
        user.setUserUID(UUID.randomUUID() + "");
        user.setPhoneNumber(phoneNumber);
        user.setName(phoneNumber);
        user.setLastName("");
        Thread thread = new Thread() {
            @Override
            public void run() {
                dao.insertNewUser(user);
            }
        };
        thread.setName("insert user");
        thread.start();
        return user.getUserUID();
    }


    public boolean isNotificationsAllowed() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("allowNotifications", true);
    }


    public boolean isConversationOpen(String conversationID) {
        SharedPreferences conversationPreferences = context.getSharedPreferences("Conversation", MODE_PRIVATE);
        String liveConversation = conversationPreferences.getString("liveConversation", "no conversation");
        return liveConversation.equals(conversationID);
    }


    private String createConversationID() {
        return "S_" + System.currentTimeMillis();
    }

    private void saveMessage(Message msg) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                dao.insertNewMessage(msg);
            }
        };
        thread.setName("insert message");
        thread.start();

    }

    private void createNotification(Message msg) {
        createNotificationChannel();
        String notificationTitle;
        NotificationsController notificationsController = NotificationsController.getInstance();
        int notificationID = notificationsController.getNotificationID(msg.getConversationID());
        notificationsController.addOnRemoveListener(new NotificationsController.onNotificationRemoveListener() {
            @Override
            public void onNotificationRemoved(int notificationID) {
                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
                notificationManagerCompat.cancel(notificationID);
            }
        });
        //int notificationID = getNotificationID(msg.getConversationID());
        if (msg.getSenderName() != null)
            notificationTitle = "New Message From " + msg.getSenderName();
        else
            notificationTitle = "New Message From " + msg.getContactNumber();
        Intent tapOnNotification = new Intent(context, ConversationActivity2.class);
        tapOnNotification.putExtra("conversationID", msg.getConversationID());
        tapOnNotification.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationID, tapOnNotification, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Receive_SMS_Messages_Channel")
                .setSmallIcon(R.drawable.ic_baseline_sms_24)
                .setContentTitle(notificationTitle)
                .setContentText(msg.getContent())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setNotificationSilent()
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        NotificationManagerCompat nmc = NotificationManagerCompat.from(context);
        nmc.notify(notificationID, builder.build());
    }

    private void createNotificationChannel() {
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


    public void createNewConversation(Message message, String currentUser, ConversationType type) {
//        if (message.getRecipients().size() > 1 && type == ConversationType.single)
//            Log.e("DB ERROR", "conversation type and recipient amount mismatch");
//        Conversation conversation = createConversation(message, type);
//        if (message.getRecipients().size() == 1) {
//            List<String> recipients = new ArrayList<>();
//            if (!message.getRecipients().get(0).equals(currentUser)) {
//                conversation.setRecipient(message.getRecipients().get(0));
//                recipients = message.getRecipients();
//            } else {
//                conversation.setRecipient(message.getSender());
//                recipients.add(message.getSender());
//            }
//            createNewGroup(message.getConversationID(), recipients);
//        } else {
//            if (message.getRecipients().contains(currentUser)) {
//                message.getRecipients().remove(currentUser);
//                message.getRecipients().add(message.getSender());
//            }
//            createNewGroup(message.getConversationID(), message.getRecipients());
//        }
//        Thread thread = new Thread() {
//            @Override
//            public void run() {
//                dao.insertNewConversation(conversation);
//            }
//        };
//        thread.setName("insert conversation");
//        thread.start();

    }


    private Conversation createConversation(Message message, ConversationType type) {
        Conversation conversation = new Conversation(message.getConversationID());
        conversation.setLastMessageID(message.getMessageID());
        conversation.setLastMessage(message.getContent());
        conversation.setMessageType(message.getMessageType());
        conversation.setLastMessageTime(System.currentTimeMillis() + "");
        conversation.setConversationName(message.getConversationName());
        conversation.setMuted(false);
        conversation.setBlocked(false);
        conversation.setConversationType(type);
        return conversation;
    }

    private void createNewGroup(String conversationID, List<String> recipients) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                for (String uid : recipients) {
                    Group group = new Group(conversationID, uid);
                    dao.insertNewGroup(group);
                }
            }
        };
        thread.setName("group thread");
        thread.start();

    }
}
