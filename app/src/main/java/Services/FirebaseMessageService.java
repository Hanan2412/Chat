package Services;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.example.woofmeow.ConversationActivity2;
import com.example.woofmeow.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import javax.net.ssl.HttpsURLConnection;

import Backend.ChatDao;
import Backend.ChatDataBase;
import BroadcastReceivers.ReplyMessageBroadcast;
import Consts.ConversationType;
import Consts.MessageAction;
import Consts.MessageStatus;
import Consts.MessageType;
//import Controller.CController;

import Controller.NotificationsController;
import Model.MessageSender;
import NormalObjects.Conversation;
import NormalObjects.FileManager;
import NormalObjects.Group;
import NormalObjects.Message;
import NormalObjects.User;

import Retrofit.Server;
import Time.StandardTime;
import Time.TimeFormat;


@SuppressWarnings({"Convert2Lambda", "AnonymousHasLambdaAlternative"})
public class FirebaseMessageService extends com.google.firebase.messaging.FirebaseMessagingService implements ReplyMessageBroadcast.NotificationReplyListener {

    private final String CHANNEL_ID = "MessagesChannel";
    private final String GROUP_CONVERSATIONS = "conversations";
    private static ArrayList<NotificationCompat.Builder> builders;
    public static String myName = "";
    private String currentUserUID;
    private ChatDao dao;
    private static HashMap<Integer, NotificationCompat.Builder> buildersHashMap;
    private final String NOTIFICATION_INFO = "notification_info";
    private NotificationsController notificationsController;
    private final String FirebaseMessagingService = "FCM";
    private MessageSender sender;
    private ReplyMessageBroadcast replyMessageBroadcast;
    private User currentUser;
    private ExecutorService pool;

    public FirebaseMessageService() {
        super();
        Log.i(NOTIFICATION_INFO, "messaging service constructor");
        if (builders == null)
            builders = new ArrayList<>();
        if (buildersHashMap == null)
            buildersHashMap = new HashMap<>();
        notificationsController = NotificationsController.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        try {
            disableActiveNotification();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        dataBaseSetUp();
        sender = MessageSender.getInstance();
        replyMessageBroadcast = new ReplyMessageBroadcast();
        replyMessageBroadcast.setListener(this);
        dao.getUser(currentUserUID).observeForever(new Observer<User>() {
            @Override
            public void onChanged(User user) {
                currentUser = user;
            }
        });
        pool = Executors.newFixedThreadPool(3);
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
            Server server = Server.getInstance();
            server.saveToken(s, currentUser);
            //Server3.getInstance().updateData("Tokens/" + currentUser, s);
        }
    }


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> data = remoteMessage.getData();
        String action = data.get("messageAction");
        if (action != null) {
            MessageAction messageAction = MessageAction.values()[Integer.parseInt(action)];
            if (messageAction == MessageAction.new_message) {
                Message message = createMessage(data);
                saveMessageFullProcess(message);
                if (isNotification(message.getConversationID(), message.getSenderID()))
                    createNotification(message);
            } else {
                handleInteractionMessage(messageAction, data);
            }
        } else {
            Log.e(FirebaseMessagingService, "action is NULL");
        }

    }

    private Message createMessage(Map<String, String> messageData)
    {
        Log.d(FirebaseMessagingService, "creating message");
        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(messageData);
        Message message = gson.fromJson(jsonElement, Message.class);
        message.setP_key(0);

        return message;
    }

    private void saveMessageFullProcess(Message message) {
        Log.d(FirebaseMessagingService, "creating message");
        message.setArrivingTime(StandardTime.getInstance().getStandardTime());
        message.setMessageStatus(MessageStatus.DELIVERED.ordinal());
        Log.d(FirebaseMessagingService, message.toString());

        // save message part
        saveConversation(message);
        saveMessage(message);

//        List<String>recipientsTokens = dao.getRecipientsTokens(message.getConversationID());
        List<String> recipientsTokens = new ArrayList<>();
        recipientsTokens.add(message.getSenderToken());
        sendMessageStatus(message, recipientsTokens);
        Log.d(FirebaseMessagingService, "createAndSaveMessage - inserted message and sent a response message");
    }

    private void saveMessage(Message message) {
        Log.d(FirebaseMessagingService, "saveMessage: " + message.getMessageID());
        boolean msgExists = dao.isMessageExists2(message.getMessageID());
        if (msgExists) {
            Log.d(FirebaseMessagingService, "MessageExists: " + message.getMessageID());
            dao.updateMessage(message);
        } else {
            Log.d(FirebaseMessagingService, "MessageDoesn'tExists: " + message.getMessageID());
            dao.insertNewMessage(message);
        }
    }

    private void saveConversation(Message message) {
        Log.d(FirebaseMessagingService, "saveConversation: " + message.getConversationID());

        boolean exists = isConversationExists(message.getConversationID());
        if (exists) {
            Conversation conversation = dao.getConversation2(message.getConversationID());
            conversation.setLastUpdate(StandardTime.getInstance().getStandardTime());
            conversation.setLastMessage(message.getContent());
            conversation.setLastMessageID(message.getMessageID());
            conversation.setLastMessageTime(message.getArrivingTime());
            Log.d(FirebaseMessagingService, "conversationExists: " + message.getConversationID());
            dao.updateConversation(conversation);
            dao.updateUserToken(message.getSenderID(), message.getSenderToken());
        } else {
            Log.d(FirebaseMessagingService, "conversationDoesn'tExists: " + message.getConversationID());
            Conversation conversation = createConversation(message);
            dao.insertNewConversation(conversation);
            User user = new User();
            user.setUserUID(message.getSenderID());
            user.setToken(message.getSenderToken());
            user.setName(message.getSenderName());
            dao.insertNewUser(user);
            Group group = new Group(message.getConversationID(), user.getUserUID());
            dao.insertNewGroup(group);
        }
    }

    private void sendMessageStatus(Message message, List<String> tokens) {
        Log.d(FirebaseMessagingService, "sending status message: " + message.getMessageID());
        message.setMessageAction(MessageAction.status.ordinal());
        sender.sendMessage(message, tokens);
    }


    private void dataBaseSetUp() {
        ChatDataBase chatDataBase = ChatDataBase.getInstance(this);
        dao = chatDataBase.chatDao();
    }

    private boolean isNotification(String conversationID, String uid) {
        if (!isConversationBlocked(conversationID))
            if (!isConversationMuted(conversationID))
                if (!isUserBlocked(uid))
                    if (!isUserMuted(uid))
                        if (isNotificationsAllowed())
                            return !isOpenConversation(conversationID);
        return false;
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private void createNotification(Message message) {
        /*
        the conversationID is also the notificationID, so each conversation will have its own notification
        each notification has a type - as if its just a text, map coordinates or a file type - corresponding with a message type
         */

        int notificationID = notificationsController.getNotificationID(message.getConversationID());
        String notificationTitle = "New Message From " + message.getSenderName();
        int summeryID = 100;

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        //opens activity from notification tap
        Intent tapOnNotificationIntent = new Intent(this, ConversationActivity2.class);
        tapOnNotificationIntent.putExtra("senderName", message.getSenderName());
        tapOnNotificationIntent.putExtra("senderUID", message.getSenderID());
        tapOnNotificationIntent.putExtra("conversationID", message.getConversationID());
        tapOnNotificationIntent.putExtra("conversationType", message.getConversationType());
        tapOnNotificationIntent.putExtra("tapMessageNotification", true);
        tapOnNotificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationID, tapOnNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        //allows to send reply from notification
        String keyTextReply = "key_text_reply";
        RemoteInput remoteInput = new RemoteInput.Builder(keyTextReply)
                .setLabel("reply")
                .build();

        Intent muteIntent = new Intent(this, ReplyMessageBroadcast.class);
        muteIntent.setAction("mute");
        muteIntent.putExtra("notificationID", notificationID);
        muteIntent.putExtra("conversationID", message.getConversationID());

        Intent messageReplyIntent = new Intent(this, ReplyMessageBroadcast.class);
        messageReplyIntent.putExtra("notificationID", notificationID);
        messageReplyIntent.putExtra("senderName", message.getSenderName());
        messageReplyIntent.putExtra("senderUID", message.getSenderID());
        messageReplyIntent.putExtra("conversationID", message.getConversationID());
        messageReplyIntent.putExtra("token", message.getSenderToken());
        if (currentUser != null)
            messageReplyIntent.putExtra("currentUserName", currentUser.getName() + " " + currentUser.getLastName());
        PendingIntent replyPendingIntent;
        PendingIntent mutePendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            replyPendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), notificationID, messageReplyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_MUTABLE);
            mutePendingIntent = PendingIntent.getBroadcast(this, notificationID, muteIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            replyPendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), notificationID, messageReplyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_CANCEL_CURRENT);
            mutePendingIntent = PendingIntent.getBroadcast(this, notificationID, muteIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_CANCEL_CURRENT);
        }
        NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(android.R.drawable.ic_media_play, "reply", replyPendingIntent)
                .addRemoteInput(remoteInput)
                .build();

        NotificationCompat.Action muteAction = new NotificationCompat.Action.Builder(android.R.drawable.ic_media_play, "mute", mutePendingIntent).build();
        //creates a notification for each conversation, each notification is silent and only the group notification have sound

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean showNotificationLikeThis = preferences.getBoolean("iconNotification", false);
        String channelID;
        if (!showNotificationLikeThis) {
            channelID = "Receive_Messages_Channel";
        } else {
            channelID = CHANNEL_ID;
        }

        createNotificationChannel();
        String txt = message.getContent();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.drawable.ic_baseline_chat_black)
                .setContentTitle(notificationTitle)
                .setContentText(txt)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(txt))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(replyAction)
                .addAction(muteAction)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setGroup(GROUP_CONVERSATIONS)
                .setSilent(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        if (message.getMessageType() == MessageType.gpsMessage.ordinal()) {
            txt = message.getAddress();
            String geoString = String.format(Locale.ENGLISH, "geo:%S,%S", message.getLatitude(), message.getLongitude());
            Intent openMapBroadcastIntent = new Intent(this, ReplyMessageBroadcast.class);
            openMapBroadcastIntent.putExtra("geoString", geoString);
            openMapBroadcastIntent.putExtra("notificationID", notificationID);
            PendingIntent openMap = PendingIntent.getBroadcast(this, notificationID, openMapBroadcastIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            builder.addAction(android.R.drawable.sym_action_chat, "open map", openMap);
        }
        Bitmap bitmap = loadSenderImageForNotification(message.getSenderID());
        if (bitmap != null)
            builder.setLargeIcon(bitmap);
        inboxStyle.addLine(txt);
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


    private void downloadConversationImage(String uid, String conversationID) {
        LiveData<Conversation> conversationLiveData = dao.getConversation(conversationID);
        Observer<Conversation> conversationObserver = new Observer<Conversation>() {
            @Override
            public void onChanged(Conversation conversation) {
                if (conversation != null) {
                    //starts download of group image
                    Server server = Server.getInstance();
                    server.setImageDownloadedListener(new Server.onImageDownloaded() {
                        @Override
                        public void downloadedImage(Bitmap bitmap) {
                            saveImage(bitmap, conversationID, true);
                            server.setImageDownloadedListener(null);
                        }

                        @Override
                        public void downloadFailed(String message) {
                            server.setImageDownloadedListener(null);
                        }
                    });
//                    server.setFileDownloadListener(new Server.onFileDownload() {
//                        @Override
//                        public void onDownloadStarted() {
//                            Log.d("FCM","conversation image download started");
//                        }
//
//                        @Override
//                        public void onProgress(int progress) {
//
//                        }
//
//                        @Override
//                        public void onDownloadFinished(File file) {
//                            Log.d("FCM","conversation image download finished");
//                            String filePath = file.getAbsolutePath();
//                            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
//                            saveImage(bitmap, conversationID, true);
//                            server.setFileDownloadListener(null);
//                        }
//
//                        @Override
//                        public void onFileDownloadFinished(String messageID, File file) {
//
//                        }
//
//                        @Override
//                        public void onDownloadError(String errorMessage) {
//                            Log.e("Error downloading conversation image",errorMessage);
//                        }
//                  });
                    server.downloadImage(uid);
//                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users/" + uid + "/pictureLink");
//                    reference.addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            String pictureLink = snapshot.getValue(String.class);
//                            Picasso.get().load(pictureLink).into(new Target() {
//                                @Override
//                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                                    saveImage(bitmap, conversationID, true);
//                                }
//
//                                @Override
//                                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
//                                    e.printStackTrace();
//                                }
//
//                                @Override
//                                public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//                                }
//                            });
//                            reference.removeEventListener(this);
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
                }
            }
        };
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                conversationLiveData.observeForever(conversationObserver);
            }
        });

    }

    //saves the downloaded image in a directory determined by the boolean value
    private void saveImage(Bitmap bitmap, String id, boolean identifier) {
        FileManager fm = FileManager.getInstance();
        fm.saveProfileImage(bitmap, id, this, identifier);
    }

    private Bitmap loadSenderImageForNotification(String sender) {
        SharedPreferences savedImagesPreferences = this.getSharedPreferences("SavedImages", Context.MODE_PRIVATE);
        if (savedImagesPreferences.getBoolean(sender, false)) {//if image is already downloaded,use it
            FileManager fm = FileManager.getInstance();
            return fm.readImage(this, FileManager.user_profile_images, sender);
        } else {//if the image isn't downloaded , download it
            Server server = Server.getInstance();
            server.setImageDownloadedListener(new Server.onImageDownloaded() {
                @Override
                public void downloadedImage(Bitmap bitmap) {
                    saveImage(bitmap, sender, false);
                    SharedPreferences.Editor editor = savedImagesPreferences.edit();
                    editor.putBoolean(sender, true);
                    editor.apply();
                }

                @Override
                public void downloadFailed(String message) {
                    Log.e("sender image download", message);
                }
            });
            server.downloadImage(sender);
//            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users/" + sender + "/pictureLink");
//            reference.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    String pictureLink = snapshot.getValue(String.class);
//                    Picasso.get().load(pictureLink).into(new Target() {
//                        @Override
//                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                            saveImage(bitmap, sender, false);
//                            SharedPreferences.Editor editor = savedImagesPreferences.edit();
//                            editor.putBoolean(sender, true);
//                            editor.apply();
//
//                        }
//
//                        @Override
//                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
//                            Log.e("Error", "couldn't load image");
//                        }
//
//                        @Override
//                        public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//                        }
//                    });
//                    reference.removeEventListener(this);
//
//
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    error.toException().printStackTrace();
//                }
//            });
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

    @Override
    public void onMute(String conversationID) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                dao.muteConversation(conversationID);
            }
        };
        pool.execute(runnable);
    }

    private void createNotificationChannel() {
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

    private void sendInteractionIntent(String name, String conversationID)
    {
        if (isOpenConversation(conversationID))
        {
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(conversationID).putExtra(name, true));
        }
        else if (isConversationExists(conversationID)) //sends a message to tab fragment
        {
            Intent interactionIntent = new Intent("interaction");
            interactionIntent.putExtra(name, true);
            interactionIntent.putExtra("conversationID", conversationID);
            LocalBroadcastManager.getInstance(this).sendBroadcast(interactionIntent);
        }
    }

    private void updateConversation(Conversation conversation)
    {
        Log.d(FirebaseMessagingService, "updating conversation: " + conversation.getConversationID());
        dao.updateConversation(conversation);
    }

    private void updateMessage(Message message)
    {
        Log.d(FirebaseMessagingService, "updating message: " + message.getMessageID() + " content: " + message.getContent());
        dao.updateMessage(message);
    }

    private void handleInteractionMessage(MessageAction action, Map<String, String> data) {
        String conversationID = data.get("conversationID");
        if (dao.isConversationExists2(conversationID)) {
            Conversation conversation = dao.getConversation2(conversationID);
            switch (action) {
                case typing: {
                    conversation.setTyping(true);
                    conversation.setRecording(false);
                    updateConversation(conversation);
                    break;
                }
                case not_typing: {
                    conversation.setTyping(false);
                    conversation.setRecording(false);
                    updateConversation(conversation);
                    break;
                }
                case recording: {
                    conversation.setRecording(true);
                    conversation.setTyping(false);
                    updateConversation(conversation);
                    break;
                }
                case not_recording: {
                    conversation.setRecording(false);
                    conversation.setTyping(false);
                    updateConversation(conversation);
                    break;
                }
                case delete_message: {
                    String msgID = data.get("messageID");
                    if (msgID != null) {
                        long messageID = Long.parseLong(msgID);
                        if (isOpenConversation(conversationID)) {
                            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(conversationID)
                                    .putExtra("delete", false)
                                    .putExtra("messageID", messageID));
                        } else {
                            pool.execute(new Runnable() {
                                @Override
                                public void run() {
                                    dao.deleteMessage(messageID);
                                }
                            });
                        }

                    }
                    break;
                }
                case edit_message: {
                    Message message = createMessage(data);
                    if (dao.isMessageExists2(message.getMessageID()))
                    {
                        Message localMessage = dao.getMessage2(message.getMessageID());
                        message.setP_key(localMessage.getP_key());
                        updateMessage(message);
                    }
                }
                case status: {
                    String MESSAGE_ID = "messageID";
                    String MESSAGE_STATUS = "MessageStatus";
                    if (data.containsKey(MESSAGE_STATUS) && data.containsKey(MESSAGE_ID)) {
                        long msgID = Long.parseLong(Objects.requireNonNull(data.get(MESSAGE_ID)));
                        int msgStatus = Integer.parseInt(Objects.requireNonNull(data.get(MESSAGE_STATUS)));
                        dao.updateMessageStatus(msgID, msgStatus, StandardTime.getInstance().getStandardTime());
                    }
                    break;
                }
                case leave_group: {
                    String sender = data.get("sender");
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            dao.removeMemberFromGroup(sender, conversationID);
                        }
                    };
                    thread.setName("remove member from group");
                    thread.start();
                    break;
                }
            }
        }
    }


    private void HandleNewConversation(Map<String, String> data) {
        String conversationID = data.get("conversationID");
        int conversationType;
        List<String> list = new ArrayList<>();
        Set<String> set = data.keySet();
        for (String s : set) {
            if (s.equals("recipients")) {
                String q = data.get(s);
                String q1 = q.replaceAll("\"", "");
                String q2 = q1.replace("[", "");
                q2 = q2.replace("]", "");
                StringBuilder builder = new StringBuilder();
                for (int k = 0; k < q2.length(); k++) {
                    if (q2.charAt(k) != ',')
                        builder.append(q2.charAt(k));
                    else {
                        list.add(builder.toString());
                        builder.delete(0, builder.length());
                    }
                    if (k == q2.length() - 1)
                        list.add(builder.toString());
                }
            }
        }
        if (list.size() == 0)
            conversationType = ConversationType.single.ordinal();
        else
            conversationType = ConversationType.group.ordinal();
        assert conversationID != null;
        Conversation conversation = new Conversation(conversationID);
        conversation.setRecipients(list);
        conversation.setConversationType(conversationType);
        Thread thread = new Thread() {
            @Override
            public void run() {
                dao.insertNewConversation(conversation);
            }
        };
        thread.setName("create conversation");
        thread.start();
        createNewGroup(conversationID, list);
        downloadRecipients(list);
    }

    private void DownloadVoiceMessage(Message message) {
        if (message.getFilePath() != null) {
            Server server = Server.getInstance();
            server.setFileDownloadListener(new Server.onFileDownload() {
                @Override
                public void onDownloadStarted() {

                }

                @Override
                public void onProgress(int progress) {

                }

                @Override
                public void onDownloadFinished(File file) {

                }

                @Override
                public void onFileDownloadFinished(long messageID, File file) {
                    String path = file.getAbsolutePath();
                    message.setFilePath(path);
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            dao.updateMessage(message);
                        }
                    };
                    thread.setName("download voice");
                    thread.start();
                    Log.d("downloaded voice", "voice message was downloaded");
                }

                @Override
                public void onDownloadError(String errorMessage) {
                    Log.e("file download error", "error downloading voice message file, \n" + errorMessage);
                }
            });
            String messageFilePath = message.getFilePath();
            String[] split = messageFilePath.split("/");
            server.downloadFile(split[split.length - 1], message.getMessageID());
//            StorageReference downloadAudioFile = FirebaseStorage.getInstance().getReferenceFromUrl(message.getFilePath());
//            try {
//                File file = File.createTempFile("recording" + message.getMessageID(), ".3gpp");
//                downloadAudioFile.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//                        String path = file.getAbsolutePath();
//                        message.setRecordingPath(path);
//                        Thread thread = new Thread() {
//                            @Override
//                            public void run() {
//                                dao.updateMessage(message);
//                            }
//                        };
//                        thread.setName("download voice");
//                        thread.start();
//                        Log.d("downloaded voice", "voice message was downloaded");
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.e("file download error", "error downloading voice message file");
//                        e.printStackTrace();
//                    }
//                });
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

        } else {
            Log.e("file path doesn't exist", "cant download voice message - path is null");
        }
    }

    private void DownloadImageMassage(Message message) {
        Thread downloadPic = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    URL url = new URL(message.getFilePath());
                    HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
                    httpsURLConnection.connect();
                    int responseCode = httpsURLConnection.getResponseCode();
                    if (responseCode == 200) {
                        InputStream inputStream = httpsURLConnection.getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        FileManager manager = FileManager.getInstance();
                        String path = manager.saveImage(bitmap, FirebaseMessageService.this);
                        if (path != null) {
                            message.setFilePath(path);
                            Thread thread = new Thread() {
                                @Override
                                public void run() {
                                    dao.updateMessage(message);
                                }
                            };
                            thread.setName("download image");
                            thread.start();
                            Log.d("download image", "image message was downloaded");
                            if (isOpenConversation(message.getConversationID()))
                                LocalBroadcastManager.getInstance(FirebaseMessageService.this).sendBroadcast(new Intent("DownloadedImage").putExtra("messageID", message.getMessageID()));
                        }
                    } else
                        Log.e("error picture download", "response code is not 200: " + responseCode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        downloadPic.setName("downloading picture");
        downloadPic.start();
        Log.d("downloadPic thread", "started downloading pic");
    }

    private void DownloadVideoMessage(Message message) {
        StorageReference downloadVideoFile = FirebaseStorage.getInstance().getReferenceFromUrl(message.getFilePath());
        try {
            File file = File.createTempFile("recording" + message.getMessageID(), ".mp4");
            downloadVideoFile.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    //Uri videoUri = Uri.fromFile(file);
                    FileManager manager = FileManager.getInstance();
                    String path = manager.SaveVideo(file, FirebaseMessageService.this);
                    if (path == null)
                        Log.e("saved video", "saved video path is null");
                    else {
                        message.setFilePath(path);
                        Thread thread = new Thread() {
                            @Override
                            public void run() {
                                dao.updateMessage(message);
                            }
                        };
                        thread.setName("download video");
                        thread.start();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(FirebaseMessageService.this, "error happened while downloading video message", Toast.LENGTH_SHORT).show();
                    Log.e("DOWNLOAD_ERROR", "error happened while downloading video file");
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void markAsDelivered(Message message) {
        message.setMessageStatus(MessageStatus.DELIVERED.ordinal());
        MessageSender sender = MessageSender.getInstance();
        //String token = getRecipientToken(message.getSender());
        List<String> tokens = new ArrayList<>();
        tokens.add(message.getSenderToken());
        sender.sendMessage(message, tokens);
        String token = message.getSenderToken();
        Log.e("Senders Token", token);
        Thread thread = new Thread() {
            @Override
            public void run() {
                dao.updateMessageStatus(message.getMessageID(), MessageStatus.DELIVERED.ordinal());
            }
        };
        thread.setName("mark as delivered");
        thread.start();
        LiveData<Conversation> conversationLiveData = dao.getConversation(message.getConversationID());
        Observer<Conversation> observer = new Observer<Conversation>() {
            @Override
            public void onChanged(Conversation conversation) {
                conversation.setUnreadMessages(conversation.getUnreadMessages() + 1);
                Thread thread1 = new Thread() {
                    @Override
                    public void run() {
                        dao.updateConversation(conversation);
                    }
                };
                thread1.setName("update conversation");
                thread1.start();
                conversationLiveData.removeObserver(this);
            }
        };
        Handler handler1 = new Handler(Looper.getMainLooper());
        handler1.post(new Runnable() {
            @Override
            public void run() {
                conversationLiveData.observeForever(observer);
            }
        });

    }

    private void markAsSeen(Message message) {
        MessageSender sender = MessageSender.getInstance();
        List<String> tokens = new ArrayList<>();
        tokens.add(message.getSenderToken());
        sender.sendMessage(message, tokens);
        Thread thread = new Thread() {
            @Override
            public void run() {
                dao.updateMessageStatus(message.getMessageID(), MessageStatus.READ.ordinal());
            }
        };
        thread.setName("mark as seen");
        thread.start();
    }

    private void sendStatusMessage(int status, String token, String conversationID) {
        Message message = new Message();
        message.setConversationID(conversationID);
        message.setMessageKind("statusResponse");
        message.setMessageStatus(status);
        MessageSender sender = MessageSender.getInstance();
        List<String> tokens = new ArrayList<>();
        tokens.add(token);
        sender.sendMessage(message, tokens);
    }


    private boolean isOpenConversation(String conversationID) {
        SharedPreferences conversationPreferences = getSharedPreferences("Conversation", MODE_PRIVATE);
        String liveConversation = conversationPreferences.getString("liveConversation", "no conversation");
        return liveConversation.equals(conversationID);
    }

    private boolean isConversationBlocked(String conversationID) {
        return dao.isConversationBlocked1(conversationID);
    }

    private boolean isConversationMuted(String conversationID) {
        return dao.isConversationMuted2(conversationID);
    }

    private boolean isConversationExists(String conversationID)
    {
        return dao.isConversationExists2(conversationID);
    }

    private boolean isUserMuted(String uid) {
        return dao.isUserMuted2(uid);
    }

    private boolean isUserBlocked(String userid) {
        return dao.isUserBlocked2(userid);
    }

    private Conversation createConversation(Message message) {
        Conversation conversation = new Conversation(message.getConversationID());
        conversation.setLastMessageID(message.getMessageID());
        conversation.setLastMessage(message.getContent());
        conversation.setMessageType(message.getMessageType());
        conversation.setLastMessageTime(message.getArrivingTime());
        conversation.setConversationName(message.getConversationName());
        conversation.setMuted(false);
        conversation.setBlocked(false);
        conversation.setConversationType(message.getConversationType());
        conversation.setLastMessageTimeParse(new TimeFormat().getFormattedDate(message.getArrivingTime()));
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
        thread.setName("createNewGroup");
        thread.start();
    }

    private void disableActiveNotification() {

        notificationsController.addOnRemoveListener(new NotificationsController.onNotificationRemoveListener() {
            @Override
            public void onNotificationRemoved(int notificationID) {
                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(FirebaseMessageService.this);
                notificationManagerCompat.cancel(notificationID);
                notificationManagerCompat.cancel(100);
            }
        });
    }

    private void downloadRecipients(List<String> ids) {
        Server server = Server.getInstance();
        server.setDownloadedUsers(new Server.onUserDownload() {
            @Override
            public void downloadedUser(User user) {
                LiveData<Boolean> userExists = dao.isUserExists(user.getUserUID());
                Observer<Boolean> userExistsObserver = new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        userExists.removeObserver(this);
                        Thread thread1 = new Thread() {
                            @Override
                            public void run() {
                                if (aBoolean != null)
                                    if (aBoolean) {
                                        dao.updateUser(user);
                                    } else {
                                        dao.insertNewUser(user);
                                    }
                            }
                        };
                        thread1.setName("update_insert user");
                        thread1.start();
                    }
                };
                userExists.observeForever(userExistsObserver);
                if (user.getUserUID().equals(ids.get(ids.size() - 1)))
                    server.setDownloadedUsers(null);
            }
        });
        for (String uid : ids) {
            server.getUserById(uid);
        }

    }
}
