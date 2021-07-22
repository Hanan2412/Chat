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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.example.woofmeow.ConversationActivity;
import com.example.woofmeow.MainActivity;
import com.example.woofmeow.R;
import com.google.android.gms.dynamic.IFragmentWrapper;
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
import Consts.BackgroundMessages;
import Consts.MessageAction;
import Consts.MessageType;
import Controller.CController;
import DataBase.DataBase;
import Model.MessageSender;
import NormalObjects.Message;
import Retrofit.RetrofitApi;
import DataBase.DataBaseContract;

public class FirebaseMessageService extends com.google.firebase.messaging.FirebaseMessagingService implements ReplyMessageBroadcast.NotificationReplyListener, Notifications {

    private final String CHANNEL_ID = "MessagesChannel";
    private final String GROUP_CONVERSATIONS = "conversations";
    public static ArrayList<String> conversations;
    private static ArrayList<NotificationCompat.Builder> builders;
    public static String myName = "";
    private RetrofitApi api;
    private CController controller;
    private final String currentUser = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

    private static HashMap<Integer, NotificationCompat.Builder> buildersHashMap;
    private final String NOTIFICATION_ERROR = "notification_Error";
    private final String NOTIFICATION_INFO = "notification_info";
    private final String SEND_MESSAGE_ERROR = "sending message error";
    private SQLiteDatabase db = null;

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
        if (remoteMessage.getData().containsKey("typing")) {
            HandleInteractionMessage(remoteMessage);
        }
        else if (remoteMessage.getData().containsKey("message"))
            HandleUserMessage(remoteMessage);
        else if (remoteMessage.getData().containsKey("readAt"))
        {
            HandleDataMessage(remoteMessage);
        }
        else if (remoteMessage.getData().containsKey("userStatus"))
            HandleDataMessage(remoteMessage);

       /* Log.i(NOTIFICATION_INFO,"onMessageReceived reached");
       if(isNotificationsAllowed())
            setUp(remoteMessage);*/

    }

    private void HandleInteractionMessage(RemoteMessage remoteMessage) {
        String conversationID = remoteMessage.getData().get("conversationID");
        String typing = remoteMessage.getData().get("typing");
        if (isOpenConversation(conversationID))
            if (!typing.equals("no"))
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(conversationID).putExtra("typing", true));
            else
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(conversationID).putExtra("typing", false));
    }

    private boolean isMuted(String senderUID) {
        SharedPreferences sharedPreferences = getSharedPreferences("muted", MODE_PRIVATE);
        String isMuted = sharedPreferences.getString(senderUID, "not muted");
        return isMuted.equals(senderUID);
    }

    private boolean isBlocked(String senderUID) {
        SharedPreferences sharedPreferences = getSharedPreferences("blocked", MODE_PRIVATE);
        String isBlocked = sharedPreferences.getString(senderUID, "not blocked");
        return isBlocked.equals(senderUID);
    }

    @Deprecated
    private void setUp(RemoteMessage remoteMessage) {
        Log.i(NOTIFICATION_INFO, "setUp reached");
        String messageSenderUID = remoteMessage.getData().get("sender");
        String conversationID = remoteMessage.getData().get("conversationID");
        SharedPreferences conversationPreferences = getSharedPreferences("Conversation", MODE_PRIVATE);
        String liveConversation = conversationPreferences.getString("liveConversation", "no conversation");
        String currentUser = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        if (messageSenderUID != null && messageSenderUID.equals(currentUser))
            Log.e(NOTIFICATION_ERROR, "sender is currentUser");

        //checks if the conversation is open when the notification should be displayed, if so, the notification won't be displayed
        if (!liveConversation.equals(conversationID)) {

            //checks if the user who sent the message is blocked or muted, if so, the notification won't be displayed
            if (!isMuted(messageSenderUID) && !isBlocked(messageSenderUID)) {

                //getting the information that was sent in the message
                System.out.println("SHOULD CREATE NOTIFICATION");

                String messageText = remoteMessage.getData().get("message");
                String messageTypeString = remoteMessage.getData().get("messageType");
                String messageRecipient = remoteMessage.getData().get("recipient");
                String messageSenderName = remoteMessage.getData().get("senderName");
                String messageID = remoteMessage.getData().get("messageID");
                String messageTime = remoteMessage.getData().get("messageTime");

                updateServer(messageSenderUID, conversationID, messageID);

                String messageLongitude = "";
                String messageLatitude = "";
                String messageLocationAddress = "";
                int messageType = MessageType.textMessage.ordinal();
                if (messageTypeString != null) {
                    messageType = Integer.parseInt(messageTypeString);
                    if (messageType == MessageType.gpsMessage.ordinal()) {
                        messageLatitude = remoteMessage.getData().get("latitude");
                        messageLongitude = remoteMessage.getData().get("longitude");
                        messageLocationAddress = remoteMessage.getData().get("locationAddress");
                    }
                }
                InsertToDataBase(messageID, conversationID, messageText, messageSenderUID, messageRecipient, messageTime, messageTypeString);
                createNotification(messageText, messageSenderName, messageSenderUID, messageRecipient, messageType, messageLongitude, messageLatitude, messageLocationAddress, conversationID);
            } else {
                Log.i(NOTIFICATION_INFO, "this user is either blocked or muted so no notification from this user");
            }
        } else {
            Log.i(NOTIFICATION_INFO, "current conversation - no need for notification");
        }
    }

    private void DataBaseSetUp() {
        if (db == null) {
            Log.i(NOTIFICATION_INFO, "created database");
            DataBase dbHelper = new DataBase(this);
            db = dbHelper.getWritableDatabase();
        }
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

    @Deprecated
    private void InsertToDataBase(String messageID, String conversationID, String messageContent, String messageSender, String messageRecipient, String messageTime, String messageType) {
        DataBaseSetUp();
        if (!CheckIfExistsInDataBase(conversationID, messageID)) {
            ContentValues values = new ContentValues();
            values.put(DataBaseContract.Messages.MESSAGE_ID, messageID);
            values.put(DataBaseContract.Conversations.CONVERSATIONS_ID_COLUMN_NAME, conversationID);
            values.put(DataBaseContract.Messages.MESSAGE_CONTENT_COLUMN_NAME, messageContent);
            values.put(DataBaseContract.Messages.MESSAGE_RECIPIENT_COLUMN_NAME, messageRecipient);
            values.put(DataBaseContract.Messages.MESSAGE_SENDER_COLUMN_NAME, messageSender);
            values.put(DataBaseContract.Messages.MESSAGE_TIME_DELIVERED_COLUMN_NAME, -1);
            values.put(DataBaseContract.Messages.MESSAGE_TIME_SENT_COLUMN_NAME, messageTime);
            values.put(DataBaseContract.Messages.MESSAGE_TYPE_COLUMN_NAME, messageType);
            values.put(DataBaseContract.Messages.MESSAGE_STATUS_COLUMN_NAME, ConversationActivity.MESSAGE_DELIVERED);
            long newRowId = db.insert(DataBaseContract.Messages.MESSAGES_TABLE, null, values);
            if (newRowId <= 0)
                Log.e(NOTIFICATION_ERROR, "didn't insert data to database in messagingService");
        } else
            Log.e(NOTIFICATION_ERROR, "message already exists, didn't insert new message");
    }

    private boolean CheckIfExistsInDataBase(String conversationID, String messageID) {
        if (db != null) {
            String[] projections = {
                    BaseColumns._ID,
                    DataBaseContract.Messages.MESSAGE_ID
            };

            //in order to not scan the database each time from the start, we should start scanning from the last message received - time. since messages
            //come in a linear order, a message that was sent now will never arrive prior to the message that was sent before it
            // String selection = DataBaseContract.Messages.MESSAGE_TIME_DELIVERED_COLUMN_NAME + " = ?";
            String selection = DataBaseContract.Conversations.CONVERSATIONS_ID_COLUMN_NAME + " = ?";
            String[] selectionArgs = {conversationID};
            String sortOrder = DataBaseContract.Messages.MESSAGE_TIME_SENT_COLUMN_NAME + " DESC LIMIT 1";
            Cursor cursor = db.query(DataBaseContract.Messages.MESSAGES_TABLE, projections, selection, selectionArgs, null, null, sortOrder);
            if (cursor.moveToNext()) {
                String ID = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_ID));
                long id = Long.parseLong(ID);
                long messageId = Long.parseLong(messageID);
                cursor.close();
                return id >= messageId;
            } else {
                cursor.close();
                return false;
            }
        }
        return false;
    }

    private boolean isNotificationsAllowed() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getBoolean("allowNotifications", true);
    }

    @Deprecated
    private void updateServer(String recipient, String conversationID, String messageID) {
        HashMap<String, Object> statusMap = new HashMap<>();
        statusMap.put("messageStatus", ConversationActivity.MESSAGE_DELIVERED);
        if (controller == null) {
            controller = CController.getController();
            controller.setNotifications(this);
        }
        String recipientConversationID = RecipientConversationID(conversationID);
        controller.onUpdateData("users/" + recipient + "/conversations/" + recipientConversationID + "/conversationInfo/conversationMessages/" + messageID, statusMap);
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
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationID, tapOnNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_CANCEL_CURRENT);

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
        PendingIntent replyPendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), notificationID, messageReplyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_CANCEL_CURRENT);
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
            PendingIntent openMap = PendingIntent.getBroadcast(this, notificationID, openMapBroadcastIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);
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

    @Deprecated
    private String RecipientConversationID(String conversationID) {
        String[] conversationIDSplit = conversationID.split(" {3}");
        String recipientConversationID;
        if (currentUser.equals(conversationIDSplit[0])) {
            recipientConversationID = conversationIDSplit[1] + "   " + conversationIDSplit[0];
        } else {
            recipientConversationID = conversationIDSplit[0] + "   " + conversationIDSplit[1];
        }
        return recipientConversationID;
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

    /**
     * Handles the special data message that is being sent. this is not a message that a user is sending
     * this is a message that the app is sending depending on the user interaction with the app
     *
     * @param remoteMessage - message object that is received from fcm
     */
    private void HandleDataMessage(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        if (data.containsKey("messageStatus")) {
            String messageStatus = data.get("messageStatus");
            String readAt = data.get("readAt");
            String conversationID = data.get("conversationID");
            String messageID = data.get("messageID");
            if (isOpenConversation(conversationID))
                UpdateMessageLive(conversationID, messageID, messageStatus, readAt);
            UpdateMessageMetaDataInDataBase(messageID, messageStatus, readAt);
        } /*else if (data.containsKey("typing") || data.containsKey("recording")) {
            String conversationID = data.get("conversationID");
            String typing = data.get("typing");
            String recording = data.get("recording");
            if (typing != null)
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(conversationID).putExtra("typing", true));
            else if (recording != null)
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(conversationID).putExtra("recording", true));
        }*/


        /*String userID = remoteMessage.getData().get("userID");
        DataMessage dataMessage = new DataMessage(userID);
        String status = remoteMessage.getData().get("userStatus");
        String typing = remoteMessage.getData().get("typing");
        String token = remoteMessage.getData().get("token");
        String conversationID = remoteMessage.getData().get("conversationID");
        dataMessage.setUserStatus(status);
        dataMessage.setToken(token);
        dataMessage.setTyping(typing);
        dataMessage.setConversationID(conversationID);
        if (typing!=null)
            AlertTyping(typing.equals("1"),conversationID);
        UpdateConversationToken(conversationID,token);*/
    }

    private void UpdateMessageLive(String conversationID, String messageID, String messageStatus, String readAt) {
        ReadMessage readMessage = new ReadMessage(messageID, conversationID);
        readMessage.setMessageStatus(messageStatus);
        readMessage.setReadAt(readAt);
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(conversationID).putExtra("read", readMessage));
    }

    private void UpdateMessageMetaDataInDataBase(String messageID, String messageStatus, String readAt) {
        if (db != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DataBaseContract.Messages.MESSAGE_READ_TIME, readAt);
            contentValues.put(DataBaseContract.Messages.MESSAGE_STATUS_COLUMN_NAME, messageStatus);
            String selection = DataBaseContract.Messages.MESSAGE_ID + " LIKE ?";
            String[] selectionArgs = {messageID};
            int numRowUpdate = db.update(DataBaseContract.Messages.MESSAGES_TABLE, contentValues, selection, selectionArgs);
            if (numRowUpdate != 1)
                Log.e(NOTIFICATION_ERROR, "updated more than 1 row of messageStatus");
        }
    }

    /**
     * alerts an active conversation determined by the conversationID param that the other party is typing
     * will only update the active conversation,once the conversation is inactive, the broadcast will not be send
     *
     * @param typing         - indicates if the other party is typing
     * @param conversationID - the conversation id of the conversation the sender is typing at
     */
    private void AlertTyping(boolean typing, String conversationID) {
        if (isOpenConversation(conversationID))
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(conversationID).putExtra("typing", typing));
    }

    /**
     * saves the user token that is being sent with each message and updates it in the database per conversation
     *
     * @param conversationID - the conversation identifier
     * @param token          - the token of the user who sent the message
     */
    private void UpdateConversationToken(String conversationID, String token) {
        if (db != null) {
            ContentValues conversationValues = new ContentValues();
            conversationValues.put(DataBaseContract.User.TOKEN, token);
            String selection = DataBaseContract.Conversations.CONVERSATIONS_ID_COLUMN_NAME + " LIKE ?";
            String[] selectionArgs = {conversationID};
            long newConversationRowId = db.update(DataBaseContract.Conversations.CONVERSATIONS_TABLE, conversationValues, selection, selectionArgs);
            if (newConversationRowId != 1)
                Log.e(NOTIFICATION_ERROR, "updating conversation recipient token failed - updated to many rows");
        }
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
            String sendingTime = remoteMessage.getData().get("messageTime");
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
            MarkAsReceived(messageID, conversationID, message.getSenderToken());
            SendBroadcast(conversationID, message);
        }
    }

    private void MarkAsReceived(String messageID, String conversationID, String... tokenToSendTo) {
        ReadMessage readMessage = new ReadMessage(messageID,conversationID);
        readMessage.setMessageStatus(ConversationActivity.MESSAGE_DELIVERED);
        readMessage.setReadAt(System.currentTimeMillis() + "");
        MessageSender.getInstance().SendMessage(readMessage, BackgroundMessages.read,tokenToSendTo);
    }


    private String getMyToken() {
        SharedPreferences sharedPreferences = getSharedPreferences("Token", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "no token");
        if (!token.equals("no token"))
            return token;
        else
            Log.e("TOKEN ERROR", "no token for current user");
        return null;
    }

    /**
     * saves the message received to the database
     *
     * @param message - the message that was sent by the user
     */
    private void SaveToDataBase(Message message) {
        if (db != null) {
            ContentValues values = new ContentValues();
            values.put(DataBaseContract.Messages.MESSAGE_ID, message.getMessageID());
            values.put(DataBaseContract.Conversations.CONVERSATIONS_ID_COLUMN_NAME, message.getConversationID());
            values.put(DataBaseContract.Messages.MESSAGE_CONTENT_COLUMN_NAME, message.getMessage());
            values.put(DataBaseContract.Messages.MESSAGE_SENDER_COLUMN_NAME, message.getSender());
            values.put(DataBaseContract.Messages.MESSAGE_TIME_DELIVERED_COLUMN_NAME, System.currentTimeMillis() + "");
            values.put(DataBaseContract.Messages.MESSAGE_TIME_SENT_COLUMN_NAME, message.getSendingTime());
            values.put(DataBaseContract.Messages.MESSAGE_STATUS_COLUMN_NAME, ConversationActivity.MESSAGE_DELIVERED);
            values.put(DataBaseContract.Messages.MESSAGE_TYPE_COLUMN_NAME, message.getMessageType());
            if (message.getMessageType() == MessageType.webMessage.ordinal())
                values.put(DataBaseContract.Messages.MESSAGE_LINK, message.getMessage());
            values.put(DataBaseContract.Messages.MESSAGE_SENDER_NAME, message.getSenderName());
            values.put(DataBaseContract.Messages.MESSAGE_LATITUDE, message.getLatitude());
            values.put(DataBaseContract.Messages.MESSAGE_LONGITUDE, message.getLongitude());
            values.put(DataBaseContract.Messages.MESSAGE_ADDRESS, message.getLocationAddress());
            values.put(DataBaseContract.Messages.MESSAGE_IMAGE_PATH, message.getImagePath());
            values.put(DataBaseContract.Messages.MESSAGE_ADDRESS, message.getLocationAddress());
            values.put(DataBaseContract.Messages.MESSAGE_LONGITUDE, message.getLongitude());
            values.put(DataBaseContract.Messages.MESSAGE_LATITUDE, message.getLatitude());
            values.put(DataBaseContract.Messages.MESSAGE_RECORDING_PATH, message.getRecordingPath());
            values.put(DataBaseContract.Messages.MESSAGE_RECIPIENT_COLUMN_NAME, message.getRecipient());
            long newRowId = db.insert(DataBaseContract.Messages.MESSAGES_TABLE, null, values);
            if (newRowId == -1)
                Log.e(NOTIFICATION_ERROR, "inserted more than 1 row");
            UpdateConversationToken(message.getConversationID(), message.getSenderToken());
        }
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
     * sends the message to the correct activity or fragment. it also saves the message if it's destanation is not an active conversation
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

    private void CreateNewConversation(Message message) {
        if (db != null) {
            ContentValues values = new ContentValues();
            values.put(DataBaseContract.Conversations.CONVERSATIONS_ID_COLUMN_NAME, message.getConversationID());
            values.put(DataBaseContract.Conversations.USER_UID, LoadCurrentUser());
            values.put(DataBaseContract.Conversations.CONVERSATION_LAST_MESSAGE_ID, message.getMessageID());
            values.put(DataBaseContract.Conversations.CONVERSATION_LAST_MESSAGE_COLUMN_NAME, message.getMessage());
            values.put(DataBaseContract.Conversations.CONVERSATION_LAST_MESSAGE_TYPE_COLUMN_NAME, message.getMessageType());
            values.put(DataBaseContract.Conversations.CONVERSATION_LAST_MESSAGE_TIME_COLUMN_NAME, message.getArrivingTime());
            values.put(DataBaseContract.Conversations.CONVERSATION_RECIPIENT_NAME, message.getRecipientName());
            values.put(DataBaseContract.Conversations.CONVERSATION_RECIPIENT,message.getSender());
            values.put(DataBaseContract.User.TOKEN,message.getSenderToken());
            values.put(DataBaseContract.Conversations.CONVERSATIONS_MUTE_COLUMN_NAME, false);
            long newConversationID = db.insert(DataBaseContract.Conversations.CONVERSATIONS_TABLE, null, values);
            if (newConversationID == -1)
                Log.e(NOTIFICATION_ERROR, "inserted more than 1 row");

        }
    }

    private boolean isConversationExists(String conversationID) {
        if (db != null) {
            String[] projections = {
                    DataBaseContract.Conversations.CONVERSATIONS_ID_COLUMN_NAME
            };
            String selection = DataBaseContract.Conversations.CONVERSATIONS_ID_COLUMN_NAME + " = ?";
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
        return false;
    }
}
