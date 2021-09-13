package Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import com.example.woofmeow.ConversationActivity;
import com.example.woofmeow.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import BackgroundMessages.ReadMessage;
import BroadcastReceivers.ReplyMessageBroadcast;
import Consts.MessageAction;
import Consts.MessageType;
import Controller.CController;
import DataBase.DataBase;
import Model.MessageSender;
import NormalObjects.Message;
import NormalObjects.User;
import Retrofit.RetrofitApi;
import DataBase.DataBaseContract;
import DataBase.*;

public class FirebaseMessageService extends com.google.firebase.messaging.FirebaseMessagingService implements ReplyMessageBroadcast.NotificationReplyListener, Notifications {

    private final String CHANNEL_ID = "MessagesChannel";
    private final String GROUP_CONVERSATIONS = "conversations";
    public static ArrayList<String> conversations;
    private static ArrayList<NotificationCompat.Builder> builders;
    public static String myName = "";
    private RetrofitApi api;
    private CController controller;
    private final String currentUser = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    private DBActive dbActive;

    private static HashMap<Integer, NotificationCompat.Builder> buildersHashMap;
    private final String NOTIFICATION_ERROR = "notification_Error";
    private final String NOTIFICATION_INFO = "notification_info";
    private final String SEND_MESSAGE_ERROR = "sending message error";
   // private SQLiteDatabase db = null;

    public FirebaseMessageService() {
        super();
        Log.i(NOTIFICATION_INFO, "messaging service constructor");
        if (conversations == null)
            conversations = new ArrayList<>();
        if (builders == null)
            builders = new ArrayList<>();
        if (buildersHashMap == null)
            buildersHashMap = new HashMap<>();
        controller = CController.getController();
        controller.setNotifications(this);
        DisableActiveNotification();

    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.i(NOTIFICATION_INFO, "new token generated - in the service: " + s);
        SharedPreferences sharedPreferences = getSharedPreferences("Token", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", s);
        editor.apply();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
            controller = CController.getController();
            controller.setNotifications(this);
            controller.onUpdateData("Tokens/" + currentUser, s);
        }
    }


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String,String> data = remoteMessage.getData();
        String action = data.get("messageKind");
        String conversationID = data.get("conversationID");
        if(action !=null)
            Log.d("status message",  action);
        else
        {
            Log.e("action is null", "action is NULL" );
        }
        if(action!=null)
        switch (action)
        {
            case "newMessage":
            {
                HandleUserMessage(remoteMessage);
                break;
            }
            case "typing":
            {
                if (isOpenConversation(conversationID))
                {
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(conversationID).putExtra("typing", true));
                }
                break;
            }
            case "not typing":
            {
                if (isOpenConversation(conversationID))
                {
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(conversationID).putExtra("typing", false));
                }
                break;
            }
            case "recording":
            {
                if (isOpenConversation(conversationID))
                {
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(conversationID).putExtra("recording", false));
                }
                break;
            }
            case "not recording":
            {
                if (isOpenConversation(conversationID))
                {
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(conversationID).putExtra("not recording", false));
                }
                break;
            }
            case "read_time":
            {
                String messageStatus = data.get("messageStatus");
                String readAt = data.get("readAt");
                String messageID = data.get("messageID");
                if (isOpenConversation(conversationID))
                {
                    Intent readIntent = new Intent(conversationID);
                    readIntent.putExtra("messageStatus",messageStatus);
                    readIntent.putExtra("readAt",readAt);
                    readIntent.putExtra("messageID",messageID);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(readIntent);
                    //UpdateMessageLive(conversationID, messageID, messageStatus, readAt);
                }
                UpdateMessageMetaDataInDataBase(messageID, messageStatus, readAt);
                break;
            }
            case "delete":
            {
                String messageID = data.get("messageID");
                if (isOpenConversation(conversationID))
                {
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(conversationID)
                            .putExtra("delete", false)
                            .putExtra("messageID",messageID));
                }
                break;
            }
            case "edit":
            {
                if (isOpenConversation(conversationID))
                {
                    String e_t = data.get("editTime");
                    String message = data.get("message");
                    String messageID = data.get("messageID");
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(conversationID)
                            .putExtra("edit", false)
                    .putExtra("messageID",messageID)
                    .putExtra("conversationID",conversationID)
                    .putExtra("message",message)
                    .putExtra("edit_time",e_t));
                }
            }
            case "userStatus":
            {
                break;
            }
            default:
                Log.e("received message error","default case in on message received");
        }
    }

    private void DataBaseSetUp() {
        dbActive = DBActive.getInstance(this);
    }

    private Bitmap LoadSenderImageForNotification(String sender) {
        SharedPreferences savedImagesPreferences = this.getSharedPreferences("SavedImages", Context.MODE_PRIVATE);
        if (savedImagesPreferences.getBoolean(sender, false)) {
            try {
                ContextWrapper contextWrapper = new ContextWrapper(this.getApplicationContext());
                File directory = contextWrapper.getDir("user_images", Context.MODE_PRIVATE);
                File imageFile = new File(directory, sender + "_Image");
                return BitmapFactory.decodeStream(new FileInputStream(imageFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users/" + sender + "/pictureLink");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String pictureLink = snapshot.getValue(String.class);
                    Picasso.get().load(pictureLink).into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            ContextWrapper contextWrapper = new ContextWrapper(FirebaseMessageService.this.getApplicationContext());
                            File directory = contextWrapper.getDir("user_images", Context.MODE_PRIVATE);
                            if (!directory.exists())
                                if (!directory.mkdir()) {
                                    Log.e("error", "couldn't create a directory in conversationAdapter2");
                                }
                            File Path = new File(directory, sender + "_Image");
                            try {
                                FileOutputStream fileOutputStream = new FileOutputStream(Path);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                                fileOutputStream.close();
                                SharedPreferences.Editor editor = savedImagesPreferences.edit();
                                editor.putBoolean(sender, true);
                                editor.apply();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            Log.e("Error", "couldn't load image");
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });
                    reference.removeEventListener(this);


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    error.toException().printStackTrace();
                }
            });
        }
        return null;
    }



    private boolean isNotificationsAllowed() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getBoolean("allowNotifications", true);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
        Log.e("ON DELETE", "delete11111111111111");
    }

    private void createNotification(String messageText, String senderName, String senderUID, String recipient, int messageType, String longitude, String latitude, String locationAddress, String conversationID, String... recipientTokens) {
        /*
        the conversationID is also the notificationID, so each conversation will have its own notification
        each notification has a type - as if its just a text, map coordinates or a file type - corresponding with a message type
         */

        //int notificationID = conversations.indexOf(conversationID);
        int notificationID = getNotificationID(conversationID);
        String notificationTitle = "New Message From " + senderName;
        int summeryID = 100;

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        //opens activity from notification tap
        Intent tapOnNotificationIntent = new Intent(this, ConversationActivity.class);
        tapOnNotificationIntent.putExtra("senderName", senderName);
        tapOnNotificationIntent.putExtra("senderUID", senderUID);

        tapOnNotificationIntent.putExtra("conversationID", conversationID);
        tapOnNotificationIntent.putExtra("tapMessageNotification", true);
        tapOnNotificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationID, tapOnNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        //allow to send reply from notification
        ReplyMessageBroadcast replyMessageBroadcast = new ReplyMessageBroadcast();
        replyMessageBroadcast.setListener(this);

        String keyTextReply = "key_text_reply";
        RemoteInput remoteInput = new RemoteInput.Builder(keyTextReply)
                .setLabel("reply")
                .build();

        Intent messageReplyIntent = new Intent(this, ReplyMessageBroadcast.class);
        messageReplyIntent.putExtra("notificationID", notificationID);
        messageReplyIntent.putExtra("sender", recipient);
        messageReplyIntent.putExtra("recipient", senderUID);
        messageReplyIntent.putExtra("conversationID", conversationID);
        messageReplyIntent.putExtra("MyName", myName);
        messageReplyIntent.putExtra("tokens", recipientTokens);
        PendingIntent replyPendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), notificationID, messageReplyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(android.R.drawable.ic_media_play, "reply", replyPendingIntent)
                .addRemoteInput(remoteInput)
                .build();
        //creates a notification for each conversation, each notification is silent and only the group notification have sound

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean showNotificationLikeThis = preferences.getBoolean("iconNotification", false);
        String channelID;
        if (!showNotificationLikeThis) {
            channelID = "Receive_Messages_Channel";
        } else {
            channelID = CHANNEL_ID;
        }

        CreateNotificationChannel();
        if (messageType == MessageType.gpsMessage.ordinal())
            messageText = locationAddress;
        Bitmap bitmap = LoadSenderImageForNotification(senderUID);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.drawable.ic_baseline_chat_black)
                .setContentTitle(notificationTitle)
                .setContentText(messageText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(messageText))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(action)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setGroup(GROUP_CONVERSATIONS)
                .setNotificationSilent()
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        if (bitmap != null)
            builder.setLargeIcon(bitmap);

        if (messageType == MessageType.gpsMessage.ordinal()) {
            String geoString = String.format(Locale.ENGLISH, "geo:%S,%S", latitude, longitude);
            Intent openMapBroadcastIntent = new Intent(this, ReplyMessageBroadcast.class);
            openMapBroadcastIntent.putExtra("geoString", geoString);
            openMapBroadcastIntent.putExtra("notificationID", notificationID);
            PendingIntent openMap = PendingIntent.getBroadcast(this, notificationID, openMapBroadcastIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            builder.addAction(android.R.drawable.sym_action_chat, "open map", openMap);
        }
        inboxStyle.addLine(messageText);

        buildersHashMap.put(notificationID, builder);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        //create a group notifications


        Notification groupNotification = new NotificationCompat.Builder(this, channelID)
                .setContentTitle(builders.size() + " new messages")
                .setContentText("new messages are waiting for you")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setStyle(inboxStyle)
                .setGroup(GROUP_CONVERSATIONS)
                .setGroupSummary(true)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        notificationManagerCompat.notify(notificationID, builder.build());

        notificationManagerCompat.notify(summeryID, groupNotification);


    }

    //if notification already exists, will return it id. else will create a new id for the notification
    private int getNotificationID(String conversationID) {
        int i = 0;
        if (conversations.isEmpty()) {
            conversations.add(conversationID);
            return i;
        }
        for (String conversation : conversations) {
            if (conversation.equals(conversationID))
                break;
            else
                i++;
        }
        if (i >= conversations.size()) {
            conversations.add(conversationID);
        }

        return i;
    }

    @Override
    public void onReply(int notificationID) {
        if (notificationID != -1) {
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
            notificationManagerCompat.cancel(notificationID);
            notificationManagerCompat.cancel(100);
        }
    }

    @Override
    public void onOpenMap(String geoString, int notificationID) {
        if (notificationID != -1) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoString));
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
            notificationManagerCompat.cancel(notificationID);
            startActivity(intent);
        }
    }

    private void CreateNotificationChannel() {
        String channelName = "ReceiveMessages";
        String channelDescription = "Receive messages from contacts";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        String channelID = "Receive_Messages_Channel";
        NotificationChannel channel = new NotificationChannel(channelID, channelName, importance);
        channel.setDescription(channelDescription);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        assert notificationManager != null;
        notificationManager.createNotificationChannel(channel);
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------//

    private void UpdateMessageLive(String conversationID, String messageID, String messageStatus, String readAt) {
        ReadMessage readMessage = new ReadMessage(messageID, conversationID);
        readMessage.setMessageStatus(messageStatus);
        readMessage.setReadAt(readAt);
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(conversationID).putExtra("read", readMessage));
    }

    private void UpdateMessageMetaDataInDataBase(String messageID, String messageStatus, String readAt) {
        dbActive.UpdateMessageMetaData(messageID, messageStatus, readAt);

    }


    /**
     * saves the user token that is being sent with each message and updates it in the database per conversation
     *
     * @param conversationID - the conversation identifier
     * @param token          - the token of the user who sent the message
     */
    private void UpdateConversationToken(String conversationID, String token) {
        dbActive.UpdateConversationToken(conversationID, token);

    }

    /**
     * Handles the user sent message
     *
     * @param remoteMessage - message object that is received from fcm
     */
    private void HandleUserMessage(RemoteMessage remoteMessage) {
        DataBaseSetUp();
        Message message = new Message();
        String conversationID = remoteMessage.getData().get("conversationID");
        String messageID = remoteMessage.getData().get("messageID");
        if (messageID != null) {
            String content = remoteMessage.getData().get("message");
            String senderUID = remoteMessage.getData().get("sender");
            String senderName = remoteMessage.getData().get("senderName");
            String sendingTime = remoteMessage.getData().get("sendingTime");
            String quote = remoteMessage.getData().get("quoteMessage");
            String quoteMessageID = remoteMessage.getData().get("quotedMessageID");
            String recipientName = remoteMessage.getData().get("recipientName");
            String latitude = remoteMessage.getData().get("latitude");
            String longitude = remoteMessage.getData().get("longitude");
            String address = remoteMessage.getData().get("locationAddress");
            String imagePath = remoteMessage.getData().get("imagePath");
            String recordingPath = remoteMessage.getData().get("recordingPath");
            String editMessageTime = remoteMessage.getData().get("editTime");
            int type = Integer.parseInt(remoteMessage.getData().get("messageType"));
            String action = remoteMessage.getData().get("messageAction");
            if (action.equals("new_message"))
                message.setMessageAction(MessageAction.new_message);
            String senderToken = remoteMessage.getData().get("senderToken");
            String contactName = remoteMessage.getData().get("contactName");
            String contactPhone = remoteMessage.getData().get("contactPhone");
            message.setContactPhone(contactPhone);
            message.setContactName(contactName);
            message.setConversationID(conversationID);
            message.setMessageID(messageID);
            message.setMessage(content);
            message.setSender(senderUID);
            message.setSenderName(senderName);
            message.setSendingTime(sendingTime);
            message.setArrivingTime(System.currentTimeMillis() + "");
            message.setQuotedMessageID(quoteMessageID);
            message.setQuoteMessage(quote);
            message.setMessageType(type);
            message.setRecipient(currentUser);
            message.setRecipientName(recipientName);
            message.setArrivingTime(System.currentTimeMillis() + "");
            message.setLatitude(latitude);
            message.setLongitude(longitude);
            message.setLocationAddress(address);
            message.setImagePath(imagePath);
            message.setRecordingPath(recordingPath);
            message.setEditTime(editMessageTime);
            message.setSenderToken(senderToken);
            message.setMessageStatus(ConversationActivity.MESSAGE_DELIVERED);
            //MarkAsReceived(messageID, conversationID, message.getSenderToken());
            SendBroadcast(conversationID, message);
        }
    }

    private void MarkAsReceived(String messageID, String conversationID, String... tokenToSendTo) {
        Message message = new Message();
        message.setMessageID(messageID);
        message.setConversationID(conversationID);
        message.setMessageStatus(ConversationActivity.MESSAGE_DELIVERED);
        message.setArrivingTime(System.currentTimeMillis() + "");
        MessageSender.getInstance().SendMessage(message,tokenToSendTo);
    }

    /**
     * saves the message received to the database
     *
     * @param message - the message that was sent by the user
     */
    private void SaveToDataBase(Message message) {
        if(!dbActive.CheckIfExist(message.getMessageID(),false))
        dbActive.SaveMessage(message);

    }

    /**
     * Determines whether the conversation is currently active
     *
     * @param conversationID - the conversation id of the conversation the message that was sent belongs to
     * @return true if the conversation is open, false otherwise
     */
    private boolean isOpenConversation(String conversationID) {
        SharedPreferences conversationPreferences = getSharedPreferences("Conversation", MODE_PRIVATE);
        String liveConversation = conversationPreferences.getString("liveConversation", "no conversation");
        return liveConversation.equals(conversationID);
    }

    /**
     * sends the message to the correct activity or fragment. it also saves the message if it's destination is not an active conversation
     *
     * @param conversationID - the conversation identifier
     * @param message        - the message being sent
     */
    private void SendBroadcast(String conversationID, Message message) {

        //if this is the current on going conversation
        if (isOpenConversation(conversationID)) {
            Intent newMessageIntent = new Intent(conversationID);
            newMessageIntent.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(newMessageIntent);
        }//is the conversation exist at all
        else if (isConversationExists(conversationID)) {
            if (isNotificationsAllowed())
                createNotification(message.getMessage(), message.getSenderName(), message.getSender(), message.getRecipient()
                        , message.getMessageType(), message.getLongitude(), message.getLatitude(),
                        message.getLocationAddress(), message.getConversationID(), message.getSenderToken());
            Intent updateConversationIntent = new Intent("Update Conversation");
            updateConversationIntent.putExtra("Message Action", message.getMessageAction());
            updateConversationIntent.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(updateConversationIntent);
            SaveToDataBase(message);
        } else {//brand new conversation
            if (isNotificationsAllowed())
                createNotification(message.getMessage(), message.getSenderName(), message.getSender(), message.getRecipient(),
                        message.getMessageType(), message.getLongitude(), message.getLatitude(),
                        message.getLocationAddress(), message.getConversationID(), message.getSenderToken());
            CreateNewConversation(message);
            SaveToDataBase(message);
            Intent newConversationIntent = new Intent("New Conversation");
            newConversationIntent.putExtra("conversationID", conversationID);
            LocalBroadcastManager.getInstance(this).sendBroadcast(newConversationIntent);
        }
    }

    private String LoadCurrentUser() {
        SharedPreferences sharedPreferences = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
        return sharedPreferences.getString("currentUser", "no user");
    }

    /**
     * creates a new conversation that wasn't initiated by the current user and saves the sending party to the database
     * @param message - the first message in a new conversation
     */
    private void CreateNewConversation(Message message) {
        dbActive.CreateNewConversation(message);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("users/" + message.getSender());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                @SuppressWarnings("unchecked")
                HashMap<String, Object> userMap = (HashMap<String, Object>)snapshot.getValue();
                if (userMap != null) {
                    String name = (String) userMap.get("name");
                    User user = new User();
                    user.setPictureLink((String) userMap.get("pictureLink"));
                    user.setName(name);
                    user.setLastName((String) userMap.get("lastName"));
                    user.setUserUID(snapshot.getKey());
                    reference.removeEventListener(this);
                    dbActive.InsertUser(user);
                    Log.d("fcm user save", "saved new user from new conversation to database");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                error.toException().printStackTrace();
            }
        });
    }

    private boolean isConversationExists(String conversationID) {
        return dbActive.isConversationExists(conversationID);
        /*if (db != null) {
            String[] projections = {
                    DataBaseContract.Conversations.CONVERSATION_ID
            };
            String selection = DataBaseContract.Conversations.CONVERSATION_ID + " = ?";
            String[] selectionArgs = {conversationID};
            Cursor cursor = db.query(DataBaseContract.Conversations.CONVERSATIONS_TABLE, projections, selection, selectionArgs, null, null, null);
            if (cursor.getCount() == 1) {
                cursor.close();
                return true;
            } else if (cursor.getCount() == 0) {
                cursor.close();
                return false;
            } else
                Log.e(NOTIFICATION_ERROR, "isConversationExists function presents an error - > more than 1 rows retrieved");
        }
        return false;*/
    }

    private void DisableActiveNotification()
    {
        BroadcastReceiver disableNotificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String conversationID = intent.getStringExtra("ConversationID");
                int notificationID = getNotificationID(conversationID);
                if (notificationID != -1) {
                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
                    notificationManagerCompat.cancel(notificationID);
                    notificationManagerCompat.cancel(100);
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(disableNotificationReceiver,new IntentFilter("disableNotifications"));
    }
}
