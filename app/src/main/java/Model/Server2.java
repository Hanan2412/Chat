package Model;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;


import NormalObjects.Conversation;
import NormalObjects.Message;
import NormalObjects.User;

@SuppressWarnings({"Convert2Lambda", "unchecked"})
public class Server2 implements IServer {

    //singleton class that communicates with Google's Firebase
    private static Server2 server2 = null;
    private boolean inConversation = false;
    private String secondUserPath;

    private Query messagesQuery;
    private boolean delete = false;
    private ChildEventListener messagesChildEventListener;
    private ArrayList<Message> messages;
    private ArrayList<User> users;
    private User user;
    private ValueEventListener userListener;
    private DatabaseReference UserReference;
    private String SERVER2_MESSAGE = "server2Message";

    private static final String USERS_PATH = "users/";
    private static final String CONVERSATIONS = "/conversations/";

    private Server2() {
    }

    public static Server2 getServer() {
        if (server2 == null)
            server2 = new Server2();
        return server2;
    }

    public interface ServerData {
        void onMessagesDownloaded(ArrayList<Message> messages);

        void onUserDownload(User user);

        void onConversationsDownloaded(ArrayList<Conversation> conversations);

        void onConversationDownloaded(Conversation conversation);

        void onConversationChanged(Conversation conversation);

        void onConversationDeleted(Conversation conversation);

        void onFoundUserQuery(User user);

        void onSingleNewMessage(Message message);

        void onItemChange(Message message, int Position);

        void onMessageRemoved(int position);
        void onVersionChange(float newVersion);
    }

    private ServerData callback;

    public void setServerData(ServerData callback) {
        this.callback = callback;
    }


    @Override
    public void updateServer(String path, String data) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference(path);
        reference.setValue(data);
    }

    @Override
    public void updateServer(String path, HashMap<String, Object> map) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference(path);
        reference.updateChildren(map);
    }

    @Override
    public void updateServer(String path, boolean data) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference(path);
        reference.setValue(data);
    }

    @Override
    public void uploadImageBitmap(String path, Bitmap imageBitmap, final Context context) {
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

    private DatabaseReference secondUserReference(String path) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        return database.getReference(path);
    }

    @Override
    public void setSecondPath(String path) {
        secondUserPath = path;
        inConversation = true;
    }

    @Override
    public void uploadImage(String path, String photoPath, final Context context) {
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

    public void uploadFile(String path,String filePath,Context context)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        String firebasePath = System.currentTimeMillis() + "recording";
        StorageReference fileReference = storageReference.child("feed/" + firebasePath);
        StorageTask<UploadTask.TaskSnapshot> uploadTask;
        DatabaseReference reference = database.getReference(path);
        Toast.makeText(context, "Started upload", Toast.LENGTH_SHORT).show();
        uploadTask = fileReference.putFile(Uri.parse(filePath));
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (task.isSuccessful())
                    Toast.makeText(context, "Stage 2 upload", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(context, "finished uploading message", Toast.LENGTH_SHORT).show();
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

    @Override
    public void removeServer(String path) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference(path);
        reference.removeValue();
    }



    //downloads user by their UID
    @Override
    public void DownloadUser(Context context, String userUID) {

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            final String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            UserReference = FirebaseDatabase.getInstance().getReference(USERS_PATH + userUID);
            userListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    HashMap<String, Object> userHash = (HashMap<String, Object>) snapshot.getValue();
                    User user = new User();
                    if (userHash != null) {
                        user.setName((String) userHash.get("name"));
                        user.setLastName((String) userHash.get("lastName"));
                        user.setNickName((String) userHash.get("nickname"));
                        user.setPictureLink((String) userHash.get("pictureLink"));
                        user.setTimeCreated((String) userHash.get("timeCreated"));
                        user.setStatus((String) userHash.get("status"));
                        user.setLastTimeLogIn((String) userHash.get("lastTimeLogIn"));
                        user.setPhoneNumber((String) userHash.get("phoneNumber"));
                        user.setUserUID(userUID);
                        if (currentUserUID.equals(userUID)) {
                            HashMap<String, Object> conversations = (HashMap<String, Object>) userHash.get("conversations");
                            if (conversations != null) {
                                Set<String> conversationKeys = conversations.keySet();
                                for (String key : conversationKeys)
                                    user.addConversation(key);
                            }
                            HashMap<String, Object> blocked = (HashMap<String, Object>) userHash.get("blocked");
                            if (blocked != null) {
                                Set<String> blockedKeys = blocked.keySet();
                                for (String key : blockedKeys)
                                    user.addBlockedUser(key);
                            }
                            HashMap<String, Object> mutedUsers = (HashMap<String, Object>) userHash.get("mutedUsers");
                            if (mutedUsers != null) {
                                Set<String> mutedKeys = mutedUsers.keySet();
                                for (String key : mutedKeys) {
                                    if ((boolean) mutedUsers.get(key))
                                        user.addMutedUser(key);
                                }

                            }

                            /*HashMap<String, Object> mutedConversations = (HashMap<String, Object>) userHash.get("mute");
                            if (mutedConversations != null) {
                                Set<String> muteKeys = mutedConversations.keySet();
                                for (String key : muteKeys)
                                    user.addMutedConversation(key);

                            }*/
                            HashMap<String, Object> phoneMap = (HashMap<String, Object>) userHash.get("phoneNumbers");
                            if (phoneMap != null) {
                                Set<String> phoneKeys = phoneMap.keySet();
                                for (String key : phoneKeys)
                                    user.addRecipientPhoneNumber(key, (String) phoneMap.get(key));
                            }
                            Server2.this.user = user;//sets the current user at the server2 class
                        }

                        callback.onUserDownload(user);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    error.toException().printStackTrace();
                }
            };
            UserReference.addValueEventListener(userListener);
        }
    }

    @Override
    public void createNewUser(String name, String lastName, String nick, Bitmap userImage, Context context) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference reference = database.getReference("/users/" + currentUserUID);
            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("name", name);
            userMap.put("lastName", lastName);
            userMap.put("nickname", nick);
            String time = System.currentTimeMillis() + "";
            userMap.put("lastTimeLogIn", time);
            userMap.put("timeCreated", time);
            reference.updateChildren(userMap);
            if (userImage != null) {
                //uploading user image to firebase
                uploadImageBitmap("users/" + currentUserUID, userImage, context);
            }
        }
    }


    public void DownloadConversations3(Context context) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference conversationReference = FirebaseDatabase.getInstance().getReference(USERS_PATH + currentUserUID + CONVERSATIONS);
            Query conversationQuery = conversationReference.orderByKey();
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    HashMap<String, Object> conversationInfo = (HashMap<String, Object>) snapshot.child("conversationInfo").getValue();

                    String conversationID = snapshot.getKey();
                    Conversation conversation = new Conversation(conversationID);
                    if (conversationInfo != null) {
                        assert conversationID != null;
                        String[] idSplit = conversationID.split(" {3}");
                        if (idSplit[0].equals(currentUserUID))
                            conversation.setRecipient(idSplit[1]);
                        else conversation.setRecipient(idSplit[0]);
                        if (user!=null)
                        {
                            SharedPreferences sharedPreferences = context.getSharedPreferences("Blocked",Context.MODE_PRIVATE);
                            String isBlocked = sharedPreferences.getString(conversation.getRecipient(),"not blocked");
                            if(isBlocked.equals("not blocked")) {

                                conversation.setLastMessage((String) conversationInfo.get("lastMessage"));
                                conversation.setLastMessageID((String) conversationInfo.get("lastMessageID"));
                                if (conversationInfo.get("lastMessageType") != null)
                                    conversation.setMessageType(((Long) conversationInfo.get("lastMessageType")).intValue());
                                conversation.setLastMessageTime((String) conversationInfo.get("lastMessageTime"));
                                if (conversationInfo.get("muted") != null)
                                    conversation.setMuted((boolean) conversationInfo.get("muted"));
                                else
                                    conversation.setMuted(false);
                                conversation.setLastMessageID((String) conversationInfo.get("lastMessageID"));
                                conversation.setSenderName((String)conversationInfo.get("recipientName"));
                                callback.onConversationDownloaded(conversation);
                            }
                        }
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    HashMap<String, Object> conversationInfo = (HashMap<String, Object>) snapshot.child("conversationInfo").getValue();
                    String conversationID = snapshot.getKey();
                    Conversation conversation = new Conversation(conversationID);
                    if (conversationInfo != null) {
                        conversation.setLastMessage((String) conversationInfo.get("lastMessage"));
                        String[] idSplit = conversationID.split(" {3}");
                        if (idSplit[0].equals(currentUserUID))
                            conversation.setRecipient(idSplit[1]);
                        else conversation.setRecipient(idSplit[0]);
                        conversation.setLastMessageID((String) conversationInfo.get("lastMessageID"));
                        if (conversationInfo.get("lastMessageType") != null)
                            conversation.setMessageType(((Long) conversationInfo.get("lastMessageType")).intValue());
                        conversation.setLastMessageTime((String) conversationInfo.get("lastMessageTime"));
                        if (conversationInfo.get("muted") != null)
                            conversation.setMuted((boolean) conversationInfo.get("muted"));
                        else
                            conversation.setMuted(false);
                        if (conversationInfo.get("typing") != null)
                            conversation.setTyping((boolean)conversationInfo.get("typing"));
                        else
                            conversation.setTyping(false);
                        conversation.setSenderName((String)conversationInfo.get("recipientName"));
                        callback.onConversationChanged(conversation);
                    }

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    HashMap<String, Object> conversationInfo = (HashMap<String, Object>) snapshot.child("conversationInfo").getValue();
                    String conversationID = snapshot.getKey();
                    Conversation conversation = new Conversation(conversationID);
                    if (conversationInfo != null) {
                        conversation.setLastMessage((String) conversationInfo.get("lastMessage"));
                        String[] idSplit = conversationID.split(" {3}");
                        if (idSplit[0].equals(currentUserUID))
                            conversation.setRecipient(idSplit[1]);
                        else conversation.setRecipient(idSplit[0]);
                        conversation.setLastMessageID((String) conversationInfo.get("lastMessageID"));
                        conversation.setMessageType(((Long) conversationInfo.get("lastMessageType")).intValue());
                        conversation.setLastMessageTime((String) conversationInfo.get("lastMessageTime"));
                        conversation.setSenderName((String)conversationInfo.get("recipientName"));
                        callback.onConversationDeleted(conversation);
                    }
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    error.toException().printStackTrace();
                }
            };
            conversationQuery.addChildEventListener(childEventListener);
        }
    }


    @Deprecated
    @Override
    public void DownloadConversations2(Context context) {
        DownloadConversations3(context);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference conversationReference = FirebaseDatabase.getInstance().getReference(USERS_PATH + currentUserUID + CONVERSATIONS);
            Query conversationQuery = conversationReference.orderByKey();
            ValueEventListener valueListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<Conversation> conversations = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String conversationID = dataSnapshot.getKey();
                        DataSnapshot conversationInfo = dataSnapshot.child("conversationInfo");
                        HashMap<String, Object> infoMap = (HashMap<String, Object>) conversationInfo.getValue();
                        Conversation conversation = new Conversation(conversationID);
                        if (infoMap != null) {
                            if (infoMap.get("muted") != null)
                                conversation.setMuted((boolean) infoMap.get("muted"));
                            String[] idSplit = conversationID.split(" {3}");
                            if (idSplit[0].equals(currentUserUID))
                                conversation.setRecipient(idSplit[1]);
                            else conversation.setRecipient(idSplit[0]);
                            conversation.setLastMessage((String) infoMap.get("lastMessage"));
                            conversation.setLastMessageTime((String) infoMap.get("lastMessageTime"));
                            conversation.setConversationID(conversationID);
                            if (infoMap.get("lastMessageType") != null)
                                conversation.setMessageType(((Long) infoMap.get("lastMessageType")).intValue());
                            conversation.setLastMessageID((String) infoMap.get("lastMessageID"));
                        }
                        conversations.add(conversation);
                    }
                    callback.onConversationsDownloaded(conversations);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    error.toException().printStackTrace();
                }
            };
            conversationQuery.addValueEventListener(valueListener);
        }
    }

    //finds the index of the correct message in messages - binary search
    //returns the index of the message or -1 if message doesn't exist
    private int findCorrectMessage(ArrayList<Message> messages, int min, int max, long key) {
        int mid = (max + min) / 2;
        long midKey = Long.parseLong(messages.get(mid).getMessageID());
        if (midKey > key) {
            max = mid;
            mid = findCorrectMessage(messages, min, max, key);
        } else if (midKey < key) {
            min = mid;
            mid = findCorrectMessage(messages, min, max, key);
        }
        if (messages.get(mid).getMessageID().equals(String.valueOf(key)))
            return mid;
        return -1;
    }

    @Override
    public void removeMessagesChildEvent() {
        if (messagesQuery!=null && messagesChildEventListener != null)
            messagesQuery.removeEventListener(messagesChildEventListener);
    }

    @Override
    public void DownloadMessages2(Context context, String conversationID, int amount) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference messagesReference = database.getReference("users/" + currentUserUID + "/conversations/" + conversationID + "/conversationInfo/conversationMessages");
            messages = new ArrayList<>();
            messagesChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    if (snapshot.getKey() != null)
                        if (!snapshot.getKey().equals("null")) {

                            Message message = snapshot.getValue(Message.class);
                            HashMap<String, Object> messageMap = (HashMap<String, Object>) snapshot.getValue();
                            if (messageMap != null && message != null) {
                                message.setImagePath((String) messageMap.get("pictureLink"));
                                message.setMessageID(snapshot.getKey());
                                message.setRecordingPath((String) messageMap.get("recordingLink"));
                               // Log.i(SERVER2_MESSAGE,"messageID: " + message.getMessageID());
                                if (message.getSender() == null)
                                    System.out.println("null message id: " + message.getMessageID());
                                if (message.getSender().equals(currentUserUID))
                                    if (message.getMessageStatus() != null)
                                        if (!message.getMessageStatus().equals(ConversationActivity.MESSAGE_SEEN))
                                            message.setMessageStatus(ConversationActivity.MESSAGE_SENT);
                                        else if (message.getMessageStatus() != null && !message.getMessageStatus().equals(ConversationActivity.MESSAGE_SEEN))
                                            message.setMessageStatus(ConversationActivity.MESSAGE_DELIVERED);
                            }

                            messages.add(message);

                            callback.onSingleNewMessage(message);
                            //callback.onMessagesDownloaded(messages);
                        }
                }


                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Message message = snapshot.getValue(Message.class);
                    HashMap<String, Object> messageMap = (HashMap<String, Object>) snapshot.getValue();
                    if (messageMap != null && message != null) {
                        message.setImagePath((String) messageMap.get("pictureLink"));
                        message.setMessageID(snapshot.getKey());
                        message.setRecordingPath((String) messageMap.get("recordingLink"));
                        if (messages.get(messages.size() - 1).getMessageID().equals(snapshot.getKey())) {
                            //new message arrived or the last message was edited
                            int lastIndex = messages.size() - 1;
                            messages.get(lastIndex).setMessage(message.getMessage());
                            messages.get(lastIndex).setMessageStatus(message.getMessageStatus());
                            messages.get(lastIndex).setImagePath(message.getImagePath());
                            messages.get(lastIndex).setRecordingPath(message.getRecordingPath());

                            callback.onItemChange(messages.get(lastIndex), lastIndex);
                            //callback.onMessagesDownloaded(messages);

                        } else {
                            //change the edited message if its not the last message sent
                            int index = findCorrectMessage(messages, 0, messages.size() - 1, Long.parseLong(message.getMessageID()));
                            if (index != -1)
                            {
                                messages.get(index).setMessage(message.getMessage());
                                messages.get(index).setMessageStatus(message.getMessageStatus());
                            }
                            callback.onItemChange(messages.get(index), index);
                            // callback.onMessagesDownloaded(messages);

                        }
                    }

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    if (delete) {
                        int mid = findCorrectMessage(messages, 0, messages.size() - 1, Long.parseLong(Objects.requireNonNull(snapshot.getKey())));
                        if (messages.get(mid).getMessageID().equals(snapshot.getKey())) {
                            messages.remove(mid);
                            delete = false;
                            callback.onMessageRemoved(mid);
                            //callback.onMessagesDownloaded(messages);

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

        }
    }

    @Override
    public void deleteMessage(boolean delete) {
        this.delete = delete;
    }

    @Override
    public void onRemoveUserListener() {
        UserReference.removeEventListener(userListener);
    }

    @Override
    public void DetectNewVersion() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("app/version");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float newVersion = (float) snapshot.getValue();
                callback.onVersionChange(newVersion);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public void SearchForUsers(final String searchQuery, final Context context) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("users/");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    HashMap<String, Object> userQuery = (HashMap<String, Object>) dataSnapshot.getValue();
                    if (userQuery != null) {
                        String name = (String) userQuery.get("name");
                        if (name != null && name.toLowerCase().contains(searchQuery.toLowerCase())) {
                            User user = new User();
                            user.setPictureLink((String) userQuery.get("pictureLink"));
                            user.setName(name);
                            user.setLastName((String) userQuery.get("lastName"));
                            user.setUserUID(dataSnapshot.getKey());
                            callback.onFoundUserQuery(user);
                            reference.removeEventListener(this);
                        }
                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                error.toException().printStackTrace();
            }
        });
    }

    @Override
    public User getUser() {
        return user;
    }

    private LinkedHashMap<String, Object> sortMap(HashMap<String, Object> map) {
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
