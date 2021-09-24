package NormalObjects;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.woofmeow.ConversationActivity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

@SuppressWarnings({"unchecked", "Convert2Lambda"})
@Deprecated
public class Server {


    private static User user;
    private static ArrayList<Chat> conversations;
    private static ArrayList<User> users;
    private static String secondUserPath;
    private static boolean inConversation = false;
    private static ChildEventListener messagesChildEventListener;
    private static ArrayList<Message> messages;
    private static DatabaseReference messagesReference;
    private static Query messagesQuery;
    private static boolean delete = false;

    private static final String USERS_PATH = "users/";
    private static final String CONVERSATIONS = "/conversations/";
    private Server() {

        users = new ArrayList<>();
    }

    public static void updateServer(String path, String data) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference(path);
        reference.setValue(data);
    }

    public static void updateServer(String path, HashMap<String, Object> map) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference(path);
        reference.updateChildren(map);
    }

    public static void UploadingImageBitmap(String path, Bitmap imageBitmap, final Context context) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String firebasePath = System.currentTimeMillis() + "image";
        final DatabaseReference reference = database.getReference(path);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        Bitmap bitmap = Bitmap.createScaledBitmap(imageBitmap, 500, 450, false);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
        String childPath;
        if (path.contains("feed"))
            childPath = "feed/";
        else
            childPath = "users/";
        final StorageReference pictureReference = storageReference.child(childPath + firebasePath);
        StorageTask<UploadTask.TaskSnapshot> uploadTask;
        uploadTask = pictureReference.putStream(new ByteArrayInputStream(out.toByteArray()));
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                //long bytesTransferred = snapshot.getBytesTransferred();
                //long totalBytes = snapshot.getTotalByteCount();
                //int progress = (int) ((bytesTransferred / totalBytes) * 100);
            }
        }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (task.isSuccessful())
                    Toast.makeText(context, "Started upload", Toast.LENGTH_SHORT).show();
                else {
                    Toast.makeText(context, "There was a problem while uploading, try again later", Toast.LENGTH_SHORT).show();
                    if (task.getException() != null)
                        throw task.getException();
                }

                return pictureReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri uri = task.getResult();
                    if (uri != null) {
                        String sUri = uri.toString();
                        HashMap<String, Object> pictureMap = new HashMap<>();
                        pictureMap.put("pictureLink", sUri);
                        reference.updateChildren(pictureMap);


                        if (inConversation) {
                            DatabaseReference secondUserReference = secondUserReference(secondUserPath);
                            secondUserReference.updateChildren(pictureMap);
                            inConversation = false;
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "There was a problem while uploading, try again later", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void uploadFile(String path,String filePath,Context context)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        String firebasePath = System.currentTimeMillis() + "recording";
        StorageReference fileReference = storageReference.child("feed/" + firebasePath);
        StorageTask<UploadTask.TaskSnapshot> uploadTask;
        DatabaseReference reference = database.getReference(path);
        uploadTask = fileReference.putFile(Uri.parse(filePath));
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (task.isSuccessful())
                    Toast.makeText(context, "Started upload", Toast.LENGTH_SHORT).show();
                else {
                    Toast.makeText(context, "There was a problem while uploading, try again later", Toast.LENGTH_SHORT).show();
                    if (task.getException() != null)
                        throw task.getException();
                }

                return fileReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri uri = task.getResult();
                    if (uri != null) {
                        String sUri = uri.toString();
                        HashMap<String, Object> pictureMap = new HashMap<>();
                        pictureMap.put("recordingLink", sUri);
                        reference.updateChildren(pictureMap);
                        Toast.makeText(context, "finished uploading voice message", Toast.LENGTH_SHORT).show();
                        if (inConversation) {
                            DatabaseReference secondUserReference = secondUserReference(secondUserPath);
                            secondUserReference.updateChildren(pictureMap);
                            inConversation = false;
                        }
                    }
                }
            }
        });
    }
    public static void uploadImage(String path, String photoPath, final Context context) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String firebasePath = System.currentTimeMillis() + "image";
        final DatabaseReference reference = database.getReference(path);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        final StorageReference pictureReference = storageReference.child("feed/" + firebasePath);
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
        bitmap = Bitmap.createScaledBitmap(bitmap, 500, 450, false);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
        StorageTask<UploadTask.TaskSnapshot> uploadTask;
        uploadTask = pictureReference.putStream(new ByteArrayInputStream(out.toByteArray()));
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                //long bytesTransferred = snapshot.getBytesTransferred();
                //long totalBytes = snapshot.getTotalByteCount();
                // int progress = (int) ((bytesTransferred / totalBytes) * 100);
            }
        }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (task.isSuccessful())
                    Toast.makeText(context, "Started upload", Toast.LENGTH_SHORT).show();
                else {
                    Toast.makeText(context, "There was a problem while uploading, try again later", Toast.LENGTH_SHORT).show();
                    if (task.getException() != null)
                        throw task.getException();
                }

                return pictureReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri uri = task.getResult();
                    if (uri != null) {
                        String sUri = uri.toString();
                        HashMap<String, Object> pictureMap = new HashMap<>();
                        pictureMap.put("pictureLink", sUri);
                        reference.updateChildren(pictureMap);

                        if (inConversation) {
                            DatabaseReference secondUserReference = secondUserReference(secondUserPath);
                            secondUserReference.updateChildren(pictureMap);
                            inConversation = false;
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "There was a problem while uploading, try again later", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void setSecondPath(String path) {
        secondUserPath = path;
        inConversation = true;
    }

    private static DatabaseReference secondUserReference(String path) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        return database.getReference(path);
    }

    public static void removeServer(String path) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference(path);
        reference.removeValue();
    }




    //downloads user by their UID
    public static void DownloadUser(Context context,String userUID)
    {
        if(FirebaseAuth.getInstance().getCurrentUser()!=null)
        {
            final String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(USERS_PATH + userUID);
            ValueEventListener userListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    HashMap<String,Object>userHash = (HashMap<String, Object>) snapshot.getValue();
                    User user = new User();
                    if(userHash!=null) {
                        user.setName((String)userHash.get("name"));
                        user.setLastName((String)userHash.get("lastName"));
                        user.setNickName((String)userHash.get("nickname"));
                        user.setPictureLink((String)userHash.get("pictureLink"));
                        user.setTimeCreated((String)userHash.get("timeCreated"));
                        user.setStatus((String)userHash.get("status"));
                        user.setLastTimeLogIn((String)userHash.get("lastTimeLogIn"));
                        user.setUserUID(userUID);
                        if(currentUserUID.equals(userUID))
                        {
                            HashMap<String,Object>conversations = (HashMap<String, Object>) userHash.get("conversations");
                            if(conversations!=null) {
                                Set<String> conversationKeys = conversations.keySet();
                                for (String key : conversationKeys)
                                    user.addConversation(key);
                            }
                            HashMap<String,Object>blocked = (HashMap<String,Object>)userHash.get("block");
                            if (blocked!=null)
                            {
                                Set<String>blockedKeys = blocked.keySet();
                                for(String key: blockedKeys)
                                    user.addBlockedUser((String)blocked.get(key));
                            }
                            HashMap<String,Object>savedFeed = (HashMap<String,Object>)userHash.get("SaveFeed");
                            if (savedFeed!=null)
                            {
                                Set<String>feedKeys = savedFeed.keySet();
                                for (String key:feedKeys)
                                    user.addSavedFeed((String)savedFeed.get(key));
                            }
                        }
                        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("user").putExtra("user",user));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    error.toException().printStackTrace();
                }
            };
            reference.addValueEventListener(userListener);
        }
    }


    public static void createNewUser(String name, String lastName, String nick, Bitmap userImage, Context context) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference reference = database.getReference("/users/" + currentUserUID);
            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("name", name);
            userMap.put("lastName", lastName);
            userMap.put("nickname", nick);
            String time = System.currentTimeMillis() + "Time";
            userMap.put("lastTimeLogIn", time);
            userMap.put("timeCreated", time);
            reference.updateChildren(userMap);
            if (userImage != null) {
                //uploading user image to firebase
                UploadingImageBitmap("users/" + currentUserUID, userImage, context);
            }
        }
    }



    public static void DownloadConversations2(Context context)
    {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            System.out.println("this is current user: " + currentUserUID);
            DatabaseReference conversationReference = FirebaseDatabase.getInstance().getReference(USERS_PATH + currentUserUID + CONVERSATIONS);
            Query conversationQuery = conversationReference.orderByKey();
            ValueEventListener valueListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Conversation conversation;
                    String[] recipient_sender;
                    ArrayList<Conversation>conversations = new ArrayList<>();
                    HashMap<String,Object>conversationMap = (HashMap<String, Object>) snapshot.getValue();
                    if(conversationMap!=null) {
                        Set<String> conversationKeys = conversationMap.keySet();
                        for (String key: conversationKeys) {
                            conversation = new Conversation(key);//happens once for each conversation
                            recipient_sender = key.split(" {3}");
                            HashMap<String, Object> messageMap = (HashMap<String, Object>) conversationMap.get(conversation.getConversationID());
                            messageMap = sortMap(messageMap);//since this map is unsorted for some reason, sortMap sorts it
                            if (messageMap != null) {
                                Set<String> messagesKeys = messageMap.keySet();
                                Object[] keys = messagesKeys.toArray();
                                String lastKey = (String) keys[keys.length - 1];
                                HashMap<String, Object> lastMessageMap = (HashMap<String, Object>) messageMap.get(lastKey);
                                if (lastMessageMap != null) {
                                    conversation.setLastMessage((String) lastMessageMap.get("message"));
                                    conversation.setSenderName((String) lastMessageMap.get("senderName"));
                                    if (recipient_sender[0].equals(currentUserUID))
                                        conversation.setRecipient(recipient_sender[1]);
                                    else
                                        conversation.setRecipient(recipient_sender[0]);
                                    conversation.setLastMessageTime((String) lastMessageMap.get("messageTime"));
                                }
                            }
                            conversations.add(conversation);
                        }
                    }
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("conversation2").putExtra("conversation",conversations));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    error.toException().printStackTrace();
                }
            };
            conversationQuery.addValueEventListener(valueListener);
        }
    }
/*
    public static void DownloadConversations(final Context context) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference reference = database.getReference("users/" + currentUserUID + "/conversations/");
            Query query = reference.orderByKey();
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Chat chat;
                    conversations = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        chat = new Chat();
                        ArrayList<Message> messages = new ArrayList<>();
                        HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();
                        map = sortMap(map);
                        if (map != null) {
                            HashMap<String, Object> conversationInfo = (HashMap<String, Object>) map.get("ConversationInfo");
                            if (conversationInfo != null) {
                                chat.setRecipientUID((String) conversationInfo.get("recipient"));
                                chat.setConversationID(dataSnapshot.getKey());
                                chat.setLastMessageTime((String) conversationInfo.get("lastMessageTime"));
                                chat.setLastMessage((String) conversationInfo.get("lastMessage"));
                                chat.setSenderUID((String) conversationInfo.get("sender"));

                                Calendar calendar = Calendar.getInstance();
                                calendar.setTimeInMillis(Long.parseLong(chat.getLastMessageTime()));
                                int year = calendar.get(Calendar.YEAR);
                                int month = calendar.get(Calendar.MONTH) + 1;
                                int day = calendar.get(Calendar.DAY_OF_MONTH);
                                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                                int minute = calendar.get(Calendar.MINUTE);
                                String time = day + "/" + month + "/" + year + "   " + hour + ":" + minute;
                                chat.setLastMessageTime(time);
                            }
                            Set<String> keys = map.keySet();
                            for (String key : keys) {
                                if (!key.equals("ConversationInfo")) {
                                    Message message = new Message();
                                    HashMap<String, Object> FMessage = (HashMap<String, Object>) map.get(key);
                                    if (FMessage != null) {
                                        message.setMessage((String) FMessage.get("message"));
                                        message.setSender((String) FMessage.get("sender"));
                                        message.setRecipient((String) FMessage.get("recipient"));
                                        message.setSenderName((String) FMessage.get("senderName"));
                                        if(FMessage.get("hasBeenRead") != null)
                                            message.setHasBeenRead((Boolean) FMessage.get("hasBeenRead"));
                                        if(FMessage.get("messageType")!=null)
                                             message.setMessageType(Math.toIntExact((Long) FMessage.get("messageType")));
                                        message.setLatitude((String) FMessage.get("latitude"));
                                        message.setLongitude((String) FMessage.get("longitude"));
                                        message.setImagePath((String) FMessage.get("pictureLink"));
                                        Calendar calendar = Calendar.getInstance();
                                        if(FMessage.get("messageTime") !=null)
                                        calendar.setTimeInMillis(Long.parseLong((String) FMessage.get("messageTime")));
                                        int year = calendar.get(Calendar.YEAR);
                                        int month = calendar.get(Calendar.MONTH) + 1;
                                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                                        int hour = calendar.get(Calendar.HOUR_OF_DAY);
                                        int minute = calendar.get(Calendar.MINUTE);
                                        String time = day + "/" + month + "/" + year + "   " + hour + ":" + minute;
                                        message.setMessageTime(time);
                                        messages.add(message);
                                    }
                                }
                            }
                            chat.setMessages(messages);
                            chat.setConversationID(dataSnapshot.getKey());
                            String conversationID = dataSnapshot.getKey();
                            assert conversationID != null;
                            String[] splitID = conversationID.split(" {3}");
                            if (currentUserUID.equals(splitID[0]))
                                chat.setRecipientUID(splitID[1]);
                            else
                                chat.setRecipientUID(splitID[0]);
                            chat.setSenderUID(currentUserUID);
                            conversations.add(chat);
                        }

                    }
                    // conversations.add(chat);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("Conversations"));

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }*/

    //finds the index of the correct message in messages - binary search
    //returns the index of the message or -1 if message doesn't exist
    private static int findCorrectMessage(ArrayList<Message>messages,int min,int max,long key){
        int mid = (max + min)/2;
        long midKey = Long.parseLong(messages.get(mid).getMessageID());
        if(midKey > key)
        {
            max = mid;
            mid =  findCorrectMessage(messages,min,max,key);
        }
        else if(midKey < key)
        {
            min = mid;
            mid = findCorrectMessage(messages,min,max,key);
        }
        if(messages.get(mid).getMessageID().equals(String.valueOf(key)))
            return mid;
        return -1;
    }

    public static void removeMessagesChildEvent()
    {
        if(messagesQuery!=null)
            messagesQuery.removeEventListener(messagesChildEventListener);
    }

    @Deprecated
    public static void DownloadMessages2(Context context, String conversationID,int amount) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            messagesReference = database.getReference("users/" + currentUserUID + "/conversations/" + conversationID);
            messages = new ArrayList<>();
             messagesChildEventListener  = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Message message = snapshot.getValue(Message.class);
                    HashMap<String, Object> messageMap = (HashMap<String, Object>) snapshot.getValue();
                    if (messageMap != null && message != null) {
                        message.setImagePath((String) messageMap.get("pictureLink"));
                        message.setMessageID(snapshot.getKey());
                        if(message.getSender().equals(currentUserUID))
                            if(message.getMessageStatus()!=null)
                                if(!message.getMessageStatus().equals(ConversationActivity.MESSAGE_SEEN))
                                    message.setMessageStatus(ConversationActivity.MESSAGE_SENT);
                                else
                                if(message.getMessageStatus()!=null && !message.getMessageStatus().equals(ConversationActivity.MESSAGE_SEEN))
                                    message.setMessageStatus(ConversationActivity.MESSAGE_DELIVERED);
                    }

                    messages.add(message);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(conversationID).putExtra("messages", messages));

                }



                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Message message = snapshot.getValue(Message.class);
                    HashMap<String, Object> messageMap = (HashMap<String, Object>) snapshot.getValue();
                    if (messageMap != null && message != null) {
                        message.setImagePath((String) messageMap.get("pictureLink"));
                        message.setMessageID(snapshot.getKey());
                        if(messages.get(messages.size()-1).getMessageID().equals(snapshot.getKey()))
                        {
                            //new message arrived or the last message was edited
                            int lastIndex = messages.size()-1;
                            messages.get(lastIndex).setMessage(message.getMessage());
                            messages.get(lastIndex).setMessageStatus(message.getMessageStatus());
                            messages.get(lastIndex).setImagePath(message.getImagePath());
                            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(conversationID).putExtra("messages", messages));
                        }
                        else
                        {
                            //change the edited message if its not the last message sent
                           int index =  findCorrectMessage(messages,0,messages.size()-1,Long.parseLong(message.getMessageID()));
                           if(index!=-1)
                                messages.get(index).setMessage(message.getMessage());
                            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(conversationID).putExtra("messages", messages));
                        }
                    }

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    if(delete) {
                        int mid = findCorrectMessage(messages, 0, messages.size() - 1, Long.parseLong(Objects.requireNonNull(snapshot.getKey())));
                        if (messages.get(mid).getMessageID().equals(snapshot.getKey())) {
                            messages.remove(mid);
                            delete = false;
                            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(conversationID).putExtra("messages", messages));
                        }
                    }
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "an error happened while downloading messages, try again later", Toast.LENGTH_SHORT).show();
                    System.out.println("downloading messages error: ");
                    error.toException().printStackTrace();
                }
            };
             messagesQuery = messagesReference.orderByKey().limitToLast(amount);
             //messagesReference.orderByKey().limitToLast(40).endAt(messages.get(0).getMessageID());
             messagesQuery.addChildEventListener(messagesChildEventListener);
            //messagesReference.addChildEventListener(messagesChildEventListener);

            /*reference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    noMoreChildren++;

                    Message message = snapshot.getValue(Message.class);
                    HashMap<String, Object> messageMap = (HashMap<String, Object>) snapshot.getValue();
                    if (messageMap != null && message != null) {
                        message.setImagePath((String) messageMap.get("pictureLink"));
                        message.setMessageID(snapshot.getKey());
                        if(message.getSender().equals(currentUserUID))
                            if(message.getMessageStatus()!=null)
                            if(!message.getMessageStatus().equals(ConversationActivity.MESSAGE_SEEN))
                                message.setMessageStatus(ConversationActivity.MESSAGE_SENT);
                        else
                            if(message.getMessageStatus()!=null && !message.getMessageStatus().equals(ConversationActivity.MESSAGE_SEEN))
                                message.setMessageStatus(ConversationActivity.MESSAGE_DELIVERED);
                    }

                    messages.add(message);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(conversationID).putExtra("messages", messages));

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Message message = snapshot.getValue(Message.class);
                    HashMap<String, Object> messageMap = (HashMap<String, Object>) snapshot.getValue();
                    if (messageMap != null && message != null) {
                        message.setImagePath((String) messageMap.get("pictureLink"));
                        message.setMessageID(snapshot.getKey());
                        int mid = -1;
                        if(messages.get(messages.size()-1).getMessageID().equals(snapshot.getKey()))
                        {
                            int lastIndex = messages.size()-1;
                            messages.get(lastIndex).setMessage(message.getMessage());
                            messages.get(lastIndex).setMessageStatus(message.getMessageStatus());
                            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(conversationID).putExtra("messages", messages));
                        }
                        /*else if((mid = (int) findCorrectMessage(snapshot.getKey())) != -1)
                        {
                            if(messages.get(mid).getMessageID().equals(snapshot.getKey()))
                            {
                                messages.get(mid).setMessage(message.getMessage());
                                messages.get(mid).setMessageStatus(message.getMessageStatus());
                                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(conversationID).putExtra("messages", messages));
                            }
                        }//

                    }

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                   int mid = (int) findCorrectMessage(snapshot.getKey());
                   if(messages.get(mid).getMessageID().equals(snapshot.getKey()))
                   {
                       messages.remove(mid);
                       LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(conversationID).putExtra("messages", messages));
                   }
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "an error happened while downloading messages, try again later", Toast.LENGTH_SHORT).show();
                    System.out.println("downloading messages error: ");
                    error.toException().printStackTrace();
                }
            });*/
        }
    }

    public static void deleteMessage(boolean delete){
        Server.delete = delete;
    }
   /* public static void DownloadMessages(final Context context, final String conversationID) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference reference = database.getReference("users/" + currentUserUID + "/conversations/" + conversationID);
            reference.addValueEventListener(new ValueEventListener() {
                @SuppressWarnings("unchecked")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<Message> messages = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        HashMap<String, Object> messageMap = (HashMap<String, Object>) dataSnapshot.getValue();
                        if (messageMap != null) {
                            if (!messageMap.containsKey("lastMessage")) {
                                Message message = new Message();
                                message.setMessage((String) messageMap.get("message"));
                                message.setRecipient((String) messageMap.get("recipient"));
                                message.setSender((String) messageMap.get("sender"));
                                if (messageMap.get("messageType") != null)
                                    message.setMessageType(Math.toIntExact((Long) messageMap.get("messageType")));
                                message.setLocationAddress((String) messageMap.get("locationAddress"));
                                message.setLongitude((String) messageMap.get("longitude"));
                                message.setLatitude((String) messageMap.get("latitude"));
                                message.setSenderName((String) messageMap.get("senderName"));
                                message.setMessageID(dataSnapshot.getKey());
                                message.setConversationID(conversationID);
                                message.setHasBeenRead((boolean) messageMap.get("hasBeenRead"));
                                message.setImagePath((String) messageMap.get("pictureLink"));
                                message.setMessageTime((String) messageMap.get("messageTime"));
                                messages.add(message);
                            }
                        }
                    }
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(conversationID).putExtra("messages", messages));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }*/


    public static ArrayList<Chat> getConversations() {
        return conversations;
    }





    public static void SearchForUsers(final String searchQuery, final Context context) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("users/");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean userFound = false;
                users = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    HashMap<String, Object> userMap = (HashMap<String, Object>) dataSnapshot.getValue();
                    if (userMap != null) {
                        String name = (String) userMap.get("name");
                        String lastName = (String) userMap.get("lastName");
                        String nickname = (String) userMap.get("nickname");
                        if (name != null && lastName != null && nickname != null)
                            if (searchQuery.contains(name) || searchQuery.contains(lastName) || searchQuery.contains(nickname)) {
                                System.out.println("user found");
                                User user = new User();
                                user.setName(name);
                                user.setLastName(lastName);
                                user.setNickName(nickname);
                                user.setUserUID(dataSnapshot.getKey());
                                user.setPictureLink((String)userMap.get("pictureLink"));
                                users.add(user);
                                userFound = true;
                            }
                    }
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("UsersList").putExtra("users", users));
                if (!userFound)
                    System.out.println("user wasn't found");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static User getUser() {
        return user;
    }

    /*public static ArrayList<User> getUsers()
    {
        return users;
    }*/
    private static LinkedHashMap<String, Object> sortMap(HashMap<String, Object> map) {
        if (map != null) {
            TreeMap<String, Object> sortedMap = new TreeMap<>(map);
            LinkedHashMap<String, Object> mapToSendBack = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : sortedMap.entrySet()) {
                mapToSendBack.put(entry.getKey(), entry.getValue());
            }

            return mapToSendBack;
        } else return null;
    }
}
