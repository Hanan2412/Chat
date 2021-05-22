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
import com.example.woofmeow.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;


import BroadcastReceivers.ReplyMessageBroadcast;
import Consts.MessageType;

import Controller.CController;
import DataBase.DataBase;
import NormalObjects.Conversation;
import NormalObjects.Message;
import NormalObjects.ObjectToSend;
import NormalObjects.Server;
import Retrofit.RetrofitApi;
import Retrofit.RetrofitClient;
import Try.TryMyResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import DataBase.DataBaseContract;

public class FirebaseMessageService extends com.google.firebase.messaging.FirebaseMessagingService implements ReplyMessageBroadcast.NotificationReplyListener, NotificationsControl {

    private String CHANNEL_ID = "MessagesChannel";
    private String GROUP_CONVERSATIONS = "conversations";
    public static ArrayList<String> conversations;
    private static ArrayList<NotificationCompat.Builder> builders;
    public static String myName = "";
    private RetrofitApi api;
    private static boolean exist = false;
    private CController controller;
    private static HashMap<String, Boolean> mutedConversations;
    private ArrayList<String> blocked;
    private static HashMap<Integer, NotificationCompat.Builder> buildersHashMap;
    private String NOTIFICATION_ERROR = "notification_Error";
    private String NOTIFICATION_INFO = "notification_info";
    private SQLiteDatabase db = null;

    public FirebaseMessageService() {
        super();
        System.out.println("messaging service constructor");
        if (conversations == null)
            conversations = new ArrayList<>();
        if (builders == null)
            builders = new ArrayList<>();
        if (buildersHashMap == null)
            buildersHashMap = new HashMap<>();
        controller = CController.getController();
        controller.setNotificationsControl(this);

        blocked = new ArrayList<>();
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        System.out.println("new token generated - in the service: " + s);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
            //HashMap<String, Object> tokenMap = new HashMap<>();
            //tokenMap.put("token", s);
            controller = CController.getController();
            controller.setNotificationsControl(this);
            controller.onUpdateData("Tokens/" + currentUser,s);
            //Server.updateServer("users/" + currentUser, tokenMap);
        }
    }


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        System.out.println("in onMessageReceived");
       // DisableNotification();
        //String messageSenderUID = remoteMessage.getData().get("sender");
        //String conversationID = remoteMessage.getData().get("conversationID");
        if(isNotificationsAllowed())
            setUp(remoteMessage);

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

    private void setUp(RemoteMessage remoteMessage) {
        System.out.println("in SetUp");
        String messageSenderUID = remoteMessage.getData().get("sender");
        String conversationID = remoteMessage.getData().get("conversationID");
        SharedPreferences conversationPreferences = getSharedPreferences("Conversation", MODE_PRIVATE);
        String liveConversation = conversationPreferences.getString("liveConversation", "no conversation");
        String currentUser = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        if (messageSenderUID.equals(currentUser))
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

    private Bitmap LoadSenderImageForNotification(String sender)
    {
        SharedPreferences savedImagesPreferences = this.getSharedPreferences("SavedImages",Context.MODE_PRIVATE);
        if (savedImagesPreferences.getBoolean(sender,false))
        {
            try {
                ContextWrapper contextWrapper = new ContextWrapper(this.getApplicationContext());
                File directory = contextWrapper.getDir("user_images", Context.MODE_PRIVATE);
                File imageFile = new File(directory,sender + "_Image");
                Bitmap imageBitmap = BitmapFactory.decodeStream(new FileInputStream(imageFile));
                return imageBitmap;
                //holder.profileImage.setImageBitmap(imageBitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        else
        {
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
                                editor.putBoolean(sender,true);
                                editor.apply();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            Log.e("Error","couldn't load image");
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

    private void InsertToDataBase(String messageID,String conversationID,String messageContent,String messageSender,String messageRecipient,String messageTime,String messageType)
    {
        DataBaseSetUp();
        if(!CheckIfExistsInDataBase(conversationID,messageID)) {
            ContentValues values = new ContentValues();
            values.put(DataBaseContract.Messages.MESSAGE_ID, messageID);
            values.put(DataBaseContract.Entry.CONVERSATIONS_ID_COLUMN_NAME, conversationID);
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
        }
        else
            Log.e(NOTIFICATION_ERROR,"message already exists, didn't insert new message");
    }

    private boolean CheckIfExistsInDataBase(String conversationID,String messageID) {
        if (db != null) {
            String[] projections = {
                    BaseColumns._ID,
                    DataBaseContract.Messages.MESSAGE_ID
            };

            //in order to not scan the database each time from the start, we should start scanning from the last message received - time. since messages
            //come in a linear order, a message that was sent now will never arrive prior to the message that was sent before it
            // String selection = DataBaseContract.Messages.MESSAGE_TIME_DELIVERED_COLUMN_NAME + " = ?";
            String selection = DataBaseContract.Entry.CONVERSATIONS_ID_COLUMN_NAME + " = ?";
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
        return preferences.getBoolean("allowNotifications",true);
    }

    private void updateServer(String recipient, String conversationID, String messageID) {
        HashMap<String, Object> statusMap = new HashMap<>();
        statusMap.put("messageStatus", ConversationActivity.MESSAGE_DELIVERED);
       // Server.updateServer("users/" + recipient + "/conversations/" + conversationID + "/conversationInfo/conversationMessages/" + messageID, statusMap);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    private void createNotification(String messageText, String senderName, String senderUID, String recipient, int messageType, String longitude, String latitude, String locationAddress, String conversationID) {
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
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationID, tapOnNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_CANCEL_CURRENT);

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
        PendingIntent replyPendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), notificationID, messageReplyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(android.R.drawable.ic_media_play, "reply", replyPendingIntent)
                .addRemoteInput(remoteInput)
                .build();
        //creates a notification for each conversation, each notification is silent and only the group notification have sound

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean showNotificationLikeThis = preferences.getBoolean("iconNotification",false);
        String channelID="";
        if(!showNotificationLikeThis){
             channelID = "Receive_Messages_Channel";
        }
        else
        {
            channelID = CHANNEL_ID;
        }

        CreateNotificationChannel();

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

        if (bitmap!=null)
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


        Notification groupNotification = new NotificationCompat.Builder(this,channelID)
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

    private void DisableNotification()
    {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String conversationID = intent.getStringExtra("ConversationID");
                if(conversations.contains(conversationID))
                {
                    int index = conversations.indexOf(conversationID);
                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
                    notificationManagerCompat.cancel(index);
                    notificationManagerCompat.cancel(100);
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,new IntentFilter("disableNotifications"));
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
    public void onReply(String replyText, int notificationID, String sender, String myName, String recipient, String conversationID) {
        if (notificationID != -1) {
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
            notificationManagerCompat.cancel(notificationID);
            notificationManagerCompat.cancel(100);

            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Message message = new Message();
                message.setSender(sender);
                message.setSenderName(myName);
                HashMap<String, Object> messageMap = new HashMap<>();
                message.setMessage(replyText);
                message.setMessageType(MessageType.textMessage.ordinal());
                message.setRecipient(recipient);
                message.setConversationID(conversationID);
                messageMap.put(System.currentTimeMillis() + "", message);//setting a unique id for each message sent using the system time
                Server.updateServer("users/" + currentUser + "/conversations/" + conversationID + "/conversationInfo/conversationMessages", messageMap);//updates the current user - the sender in the database with the sent message
                Server.updateServer("users/" + recipient + "/conversations/" + conversationID + "/conversationInfo/conversationMessages", messageMap);//updates the recipient in the database with the message

                TimeZone timeZone = TimeZone.getTimeZone("GMT-4");
                Calendar calendar = Calendar.getInstance(timeZone);
                String time = calendar.getTimeInMillis() + "";
                HashMap<String, Object> conversationInfo = new HashMap<>();
                conversationInfo.put("lastMessage", message.getMessage());
                conversationInfo.put("lastMessageTime", System.currentTimeMillis() + "");
                conversationInfo.put("lastMessageID", time);
                conversationInfo.put("recipientID", recipient);
                conversationInfo.put("conversationID", conversationID);
                conversationInfo.put("lastMessageType", MessageType.textMessage.ordinal());
                Server.updateServer("users/" + currentUser + "/conversations/" + conversationID + "/conversationInfo", conversationInfo);
                Server.updateServer("users/" + recipient + "/conversations/" + conversationID + "/conversationInfo", conversationInfo);

                api = RetrofitClient.getRetrofitClient("https://fcm.googleapis.com/").create(RetrofitApi.class);

                DatabaseReference tokenReference = FirebaseDatabase.getInstance().getReference("Tokens");
                Query tokensQuery = tokenReference.orderByKey().equalTo(recipient);//here the tokens that were retrieved are ordered by the key - which is equal to the recipients UID
                tokensQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            //tryData is the message object
                            String tokenString = dataSnapshot.getValue(String.class);
                            //TryToken tryToken = new TryToken(tokenString);
                            //TryData tryData = new TryData(currentUser,message.getMessage(),"new message from" + myName,recipient);
                            ObjectToSend toSend = new ObjectToSend(message, tokenString);
                            message.setTo(tokenString);
                            api.sendMessage(toSend).enqueue(new Callback<TryMyResponse>() {
                                @Override
                                public void onResponse(@NonNull Call<TryMyResponse> call, @NonNull Response<TryMyResponse> response) {
                                    System.out.println("this is the response code: " + response.code());
                                    System.out.println("this is the response message: " + response.message());
                                    if (response.code() == 200) {
                                        // notificationManagerCompat.cancel(notificationID);
                                        assert response.body() != null;
                                        if (response.body().success != 1)
                                            Toast.makeText(FirebaseMessageService.this, "Failed", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<TryMyResponse> call, @NonNull Throwable t) {
                                    System.out.println("Retrofit failed!!!!!!!! " + Arrays.toString(t.getStackTrace()));
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
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

    private void CreateNotificationChannel()
    {
        String channelName = "ReceiveMessages";
        String channelDescription = "Receive messages from contacts";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        String channelID = "Receive_Messages_Channel";
        NotificationChannel channel = new NotificationChannel(channelID,channelName,importance);
        channel.setDescription(channelDescription);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        assert notificationManager != null;
        notificationManager.createNotificationChannel(channel);
    }


    @Override
    public void onConversationMute(ArrayList<Conversation> conversations) {
        if (mutedConversations == null)
            mutedConversations = new HashMap<>();
        for (Conversation conversation : conversations)
            mutedConversations.put(conversation.getConversationID(), conversation.isMuted());
    }
}
