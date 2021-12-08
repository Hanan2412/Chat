package Services;

import static com.example.woofmeow.ConversationActivity.MESSAGE_SEEN;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import android.net.Uri;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


import javax.net.ssl.HttpsURLConnection;

import BroadcastReceivers.ReplyMessageBroadcast;
import Consts.ConversationType;
import Consts.MessageAction;
import Consts.MessageType;
import Controller.CController;

import Model.MessageSender;
import NormalObjects.FileManager;
import NormalObjects.Message;
import NormalObjects.User;


import DataBase.*;

@SuppressWarnings("Convert2Lambda")
public class FirebaseMessageService extends com.google.firebase.messaging.FirebaseMessagingService implements ReplyMessageBroadcast.NotificationReplyListener, Notifications {

    private final String CHANNEL_ID = "MessagesChannel";
    private final String GROUP_CONVERSATIONS = "conversations";
    public static ArrayList<String> conversations;
    private static ArrayList<NotificationCompat.Builder> builders;
    public static String myName = "";
    private CController controller;
    private String currentUser;
    private DBActive dbActive;
    private static HashMap<Integer, NotificationCompat.Builder> buildersHashMap;
    private final String NOTIFICATION_INFO = "notification_info";

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
        //DisableActiveNotification();
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        try {
            DisableActiveNotification();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        DataBaseSetUp();
        Map<String, String> data = remoteMessage.getData();
        String action = data.get("messageKind");
        String conversationID = data.get("conversationID");
        if (action != null)
            Log.d("status message", action);
        else {
            Log.e("action is null", "action is NULL");
        }
        if (action != null)
            switch (action) {
                case "newMessage": {
                    HandleUserMessage(remoteMessage);
                    break;
                }
                case "typing": {
                    if (isOpenConversation(conversationID)) {
                        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(conversationID).putExtra("typing", true));
                    }
                    break;
                }
                case "not typing": {
                    if (isOpenConversation(conversationID)) {
                        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(conversationID).putExtra("typing", false));
                    }
                    break;
                }
                case "recording": {
                    if (isOpenConversation(conversationID)) {
                        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(conversationID).putExtra("recording", false));
                    }
                    break;
                }
                case "not recording": {
                    if (isOpenConversation(conversationID)) {
                        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(conversationID).putExtra("not recording", false));
                    }
                    break;
                }
                case "read_time": {
                    String messageStatus = data.get("messageStatus");
                    String readAt = data.get("readAt");
                    String messageID = data.get("messageID");
                    if (isOpenConversation(conversationID)) {
                        Intent readIntent = new Intent(conversationID);
                        readIntent.putExtra("messageStatus", messageStatus);
                        readIntent.putExtra("readAt", readAt);
                        readIntent.putExtra("messageID", messageID);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(readIntent);
                        //UpdateMessageLive(conversationID, messageID, messageStatus, readAt);
                    }
                    UpdateMessageMetaDataInDataBase(messageID, messageStatus, readAt);
                    break;
                }
                case "delete": {
                    String messageID = data.get("messageID");
                    if (isOpenConversation(conversationID)) {
                        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(conversationID)
                                .putExtra("delete", false)
                                .putExtra("messageID", messageID));
                    }
                    break;
                }
                case "edit": {
                    if (isOpenConversation(conversationID)) {
                        String e_t = data.get("editTime");
                        String message = data.get("message");
                        String messageID = data.get("messageID");
                        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(conversationID)
                                .putExtra("edit", false)
                                .putExtra("messageID", messageID)
                                .putExtra("conversationID", conversationID)
                                .putExtra("message", message)
                                .putExtra("edit_time", e_t));
                    }
                }
                case "status": {
                    SharedPreferences sharedPreferences = getSharedPreferences("Status", MODE_PRIVATE);
                    String currentStatus = sharedPreferences.getString("status", MainActivity.OFFLINE_S);
                    String token = data.get("senderToken");
                    SendStatusMessage(currentStatus, token, conversationID);
                    break;
                }
                case "statusResponse": {
                    if (isOpenConversation(conversationID)) {
                        String status = data.get("messageStatus");
                        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("userStatus").putExtra("status", status));
                    } else
                        Log.d("statusResponse", "conversation isn't open - no need to display user status");
                    break;
                }
                default:
                    Log.e("received message error", "default case in on message received");
            }
    }

    private void DataBaseSetUp() {
        dbActive = DBActive.getInstance(this);
    }

    private Bitmap LoadSenderImageForNotification(String conversationID,String sender) {
        SharedPreferences savedImagesPreferences = this.getSharedPreferences("SavedImages", Context.MODE_PRIVATE);
        if (savedImagesPreferences.getBoolean(sender, false)) {
            try {
                ContextWrapper contextWrapper = new ContextWrapper(this.getApplicationContext());
                File directory = contextWrapper.getDir("user_images", Context.MODE_PRIVATE);
                File imageFile = new File(directory, conversationID + "_Image");
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
                            File Path = new File(directory, conversationID + "_Image");
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
        Bitmap bitmap = LoadSenderImageForNotification(conversationID,senderUID);

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

    private void UpdateMessageMetaDataInDataBase(String messageID, String messageStatus, String readAt) {
        dbActive.updateMessageMetaData(messageID, messageStatus, readAt);
    }

    private void HandleUserMessage(RemoteMessage remoteMessage) {
        Message message = new Message();
        String conversationID = remoteMessage.getData().get("conversationID");
        String messageID = remoteMessage.getData().get("messageID");
        if (messageID != null) {
            String status = remoteMessage.getData().get("messageStatus");
            if (!status.equals(ConversationActivity.MESSAGE_SENT)) {//message status update
                dbActive.updateMessageStatus(messageID, status);
                if (isOpenConversation(conversationID)) {
                    Intent intent = new Intent("messageStatus");
                    intent.putExtra("status", status);
                    intent.putExtra("messageID", messageID);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                }
            } else {//new message
                List<String> list = new ArrayList<>();
                Set<String> set = remoteMessage.getData().keySet();
                for (String s : set) {
                    if (s.equals("recipients")) {
                        String q = remoteMessage.getData().get(s);
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
                            if(k == q2.length()-1)
                                list.add(builder.toString());
                        }
                    }
                }
                String content = remoteMessage.getData().get("message");
                String senderUID = remoteMessage.getData().get("sender");
                String senderName = remoteMessage.getData().get("senderName");
                String sendingTime = remoteMessage.getData().get("sendingTime");
                String quote = remoteMessage.getData().get("quoteMessage");
                String quoteMessageID = remoteMessage.getData().get("quotedMessageID");
                String latitude = remoteMessage.getData().get("latitude");
                String longitude = remoteMessage.getData().get("longitude");
                String address = remoteMessage.getData().get("locationAddress");
                String imagePath = remoteMessage.getData().get("imagePath");
                //String recordingPath = remoteMessage.getData().get("recordingPath");
                String editMessageTime = remoteMessage.getData().get("editTime");
                int type = Integer.parseInt(remoteMessage.getData().get("messageType"));
                String action = remoteMessage.getData().get("messageAction");
                if (action.equals("new_message"))
                    message.setMessageAction(MessageAction.new_message);
                String senderToken = remoteMessage.getData().get("senderToken");
                String contactName = remoteMessage.getData().get("contactName");
                String contactPhone = remoteMessage.getData().get("contactPhone");
                String filePath = remoteMessage.getData().get("filePath");
                String group = remoteMessage.getData().get("groupName");
                message.setGroupName(group);
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
                message.setRecipients(list);
                message.setArrivingTime(System.currentTimeMillis() + "");
                message.setLatitude(latitude);
                message.setLongitude(longitude);
                message.setLocationAddress(address);
                message.setImagePath(imagePath);
                //message.setRecordingPath(recordingPath);
                message.setEditTime(editMessageTime);
                message.setSenderToken(senderToken);
                message.setFilePath(filePath);
                message.setMessageStatus(ConversationActivity.MESSAGE_DELIVERED);
                if (type == MessageType.VoiceMessage.ordinal()) {
                    DownloadVoiceMessage(message);
                } else if (type == MessageType.photoMessage.ordinal()) {
                    DownloadImageMassage(message);
                } else if (type == MessageType.videoMessage.ordinal()) {
                    DownloadVideoMessage(message);
                }
                SendBroadcast(conversationID, message);
            }
        }
    }

    private void DownloadVoiceMessage(Message message) {
        if (message.getFilePath() != null) {
            StorageReference downloadAudioFile = FirebaseStorage.getInstance().getReferenceFromUrl(message.getFilePath());
            try {
                File file = File.createTempFile("recording" + message.getMessageID(), ".3gpp");
                downloadAudioFile.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        String path = file.getAbsolutePath();
                        message.setRecordingPath(path);
                        dbActive.updateMessage(message);
                        Log.d("downloaded voice", "voice message was downloaded");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("file download error", "error downloading voice message file");
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

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
                            message.setImagePath(path);
                            dbActive.updateMessage(message);
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
                        message.setRecordingPath(path);
                        dbActive.updateMessage(message);
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
        message.setMessageStatus(ConversationActivity.MESSAGE_DELIVERED);
        MessageSender sender = MessageSender.getInstance();
        //String token = getRecipientToken(message.getSender());
        sender.sendMessage(message, message.getSenderToken());
        String token = message.getSenderToken();
        Log.e("Senders Token",token);
        dbActive.updateMessageStatus(message.getMessageID(), ConversationActivity.MESSAGE_DELIVERED);
    }

    private void markAsSeen(Message message) {
        message.setMessageStatus(MESSAGE_SEEN);
        MessageSender sender = MessageSender.getInstance();
        sender.sendMessage(message, message.getSenderToken());
        dbActive.updateMessageStatus(message.getMessageID(), MESSAGE_SEEN);
    }

    private void SendStatusMessage(String status, String token, String conversationID) {
        Message message = new Message();
        message.setConversationID(conversationID);
        message.setMessageKind("statusResponse");
        message.setMessageStatus(status);
        MessageSender sender = MessageSender.getInstance();
        sender.sendMessage(message, token);
    }

    private void SaveToDataBase(Message message) {
        dbActive.saveMessage(message);
    }


    private boolean isOpenConversation(String conversationID) {
        SharedPreferences conversationPreferences = getSharedPreferences("Conversation", MODE_PRIVATE);
        String liveConversation = conversationPreferences.getString("liveConversation", "no conversation");
        return liveConversation.equals(conversationID);
    }


    private void SendBroadcast(String conversationID, Message message) {

        //if this is the current on going conversation
        if (isOpenConversation(conversationID)) {
            if (!isBlocked(message.getSender())) {
                message.setMessageStatus(MESSAGE_SEEN);
                SaveToDataBase(message);
                Intent newMessageIntent = new Intent(conversationID);
                newMessageIntent.putExtra("message", message);
                LocalBroadcastManager.getInstance(this).sendBroadcast(newMessageIntent);
                markAsSeen(message);
            }
        }//is the conversation exist at all
        else if (isConversationExists(conversationID)) {
            if (isNotificationsAllowed())
                if (!isConversationBlocked(message.getConversationID()))
                    if (!isMuted(message.getConversationID()))
                        if (!isBlocked(message.getSender()))
                            createNotification(message.getMessage(), message.getSenderName(), message.getSender(), message.getGroupName()
                                    , message.getMessageType(), message.getLongitude(), message.getLatitude(),
                                    message.getLocationAddress(), message.getConversationID(), message.getSenderToken());
            markAsDelivered(message);
            SaveToDataBase(message);
            Intent updateConversationIntent = new Intent("Update Conversation");
            updateConversationIntent.putExtra("Message Action", message.getMessageAction());
            updateConversationIntent.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(updateConversationIntent);

            //SaveToDataBase(message);
        } else {//brand new conversation
            if (isNotificationsAllowed())
                createNotification(message.getMessage(), message.getSenderName(), message.getSender(), message.getGroupName(),
                        message.getMessageType(), message.getLongitude(), message.getLatitude(),
                        message.getLocationAddress(), message.getConversationID(), message.getSenderToken());
            CreateNewConversation(message);
            markAsDelivered(message);
            //SaveToDataBase(message);
            Intent newConversationIntent = new Intent("New Conversation");
            newConversationIntent.putExtra("conversationID", conversationID);
            LocalBroadcastManager.getInstance(this).sendBroadcast(newConversationIntent);
        }

    }

    private void CreateNewConversation(Message message) {
        if (message.getConversationID().startsWith("C"))
            dbActive.createNewConversation(message, ConversationType.single);
        else if (message.getConversationID().startsWith("G"))
            dbActive.createNewConversation(message,ConversationType.group);
        SaveToDataBase(message);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("users/" + message.getSender());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                @SuppressWarnings("unchecked")
                HashMap<String, Object> userMap = (HashMap<String, Object>) snapshot.getValue();
                if (userMap != null) {

                    String name = (String) userMap.get("name");
                    User user = new User();
                    user.setPictureLink((String) userMap.get("pictureLink"));
                    user.setName(name);
                    user.setLastName((String) userMap.get("lastName"));
                    user.setUserUID(snapshot.getKey());
                    reference.removeEventListener(this);
                    dbActive.insertUser(user);
                    Log.d("fcm user save", "saved new user from new conversation to database");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                error.toException().printStackTrace();
            }
        });

        DatabaseReference tokenReference = database.getReference("Tokens/" + message.getSender());
        ValueEventListener tokenListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String token = (String) snapshot.getValue();
                if (token != null) {
                    dbActive.updateUserToken(message.getSender(), token);
                    tokenReference.removeEventListener(this);
                } else Log.e("NULL", "Recipient Token from fb is null");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE_ERROR", "cancelled recipient token retrieval");
            }
        };
        tokenReference.addValueEventListener(tokenListener);

    }

    private boolean isConversationExists(String conversationID) {
        return dbActive.isConversationExists(conversationID);
    }

    private void DisableActiveNotification() {
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
        LocalBroadcastManager.getInstance(this).registerReceiver(disableNotificationReceiver, new IntentFilter("disableNotifications"));
    }

    private boolean isMuted(String conversationID) {
        return dbActive.isMuted(conversationID);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isBlocked(String uid) {
        return dbActive.isBlocked(uid);
    }

    private boolean isConversationBlocked(String conversationID)
    {
        return dbActive.isConversationBlocked(conversationID);
    }
}
