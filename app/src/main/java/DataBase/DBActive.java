package DataBase;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import com.example.woofmeow.ConversationActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import Consts.MessageType;
import NormalObjects.Conversation;
import NormalObjects.Message;
import NormalObjects.User;

public class DBActive {

    @SuppressWarnings("FieldMayBeFinal")
    private SQLiteDatabase db;
    @SuppressWarnings("FieldMayBeFinal")
    private DataBase dbHelper;
    private String currentUserUID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    private final String DataBaseError = "db error";
    private User user;
    private static DBActive active = null;

    public static DBActive getInstance(Context context)
    {
        if(active == null)
        {
            active = new DBActive(context);
        }
        return active;
    }

    private DBActive(Context context)
    {
        dbHelper = new DataBase(context);
        db = dbHelper.getWritableDatabase();
    }



    public void setUser(User user)
    {
        CheckIfUserExist(user);
        this.user = user;

    }

    public User getUser() {
        return user;
    }

    //gets all the conversations from the database
    public List<Conversation> getConversations()
    {
        List<Conversation>conversationList = new ArrayList<>();

            if (db != null) {
                String[] projections = {
                        BaseColumns._ID,
                        DataBaseContract.Conversations.CONVERSATION_ID,
                        DataBaseContract.Conversations.LAST_MESSAGE,
                        DataBaseContract.Conversations.LAST_MESSAGE_TIME,
                        DataBaseContract.Conversations.LAST_MESSAGE_TYPE,
                        DataBaseContract.Conversations.LAST_MESSAGE_ID,
                        DataBaseContract.Conversations.RECIPIENT,
                        DataBaseContract.Conversations.MUTED,
                        DataBaseContract.Conversations.USER_UID,
                        DataBaseContract.Conversations.RECIPIENT_NAME,
                        DataBaseContract.Conversations.IMAGE_PATH,
                        DataBaseContract.User.TOKEN,
                        //DataBaseContract.Conversations.CONVERSATION_INDEX
                };
                String selection = DataBaseContract.Conversations.USER_UID + " LIKE ?";
                String orderBy = DataBaseContract.Conversations.LAST_MESSAGE_TIME + " DESC";
                if (user != null && user.getUserUID() != null) {
                    String[] selectionArgs = {user.getUserUID()};
                    Cursor cursor = db.query(DataBaseContract.Conversations.CONVERSATIONS_TABLE, projections, selection, selectionArgs, null, null, orderBy);
                    while (cursor.moveToNext()) {
                        String conversationIDs = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.CONVERSATION_ID));
                        String lastMessage = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.LAST_MESSAGE));
                        String lastMessageTime = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.LAST_MESSAGE_TIME));
                        int lastMessageType = cursor.getInt(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.LAST_MESSAGE_TYPE));
                        String recipient = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.RECIPIENT));
                        String lastMessageID = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.LAST_MESSAGE_ID));
                        String recipientName = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.RECIPIENT_NAME));
                        String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.IMAGE_PATH));
                        String muted = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.MUTED));
                        String recipientToken = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.TOKEN));
                        Conversation conversation = new Conversation(conversationIDs);
                        conversation.setLastMessageTimeFormatted(lastMessageTime);
                        conversation.setLastMessage(lastMessage);
                        conversation.setMessageType(lastMessageType);
                        conversation.setLastMessageID(lastMessageID);
                        conversation.setRecipient(recipient);
                        conversation.setRecipientImagePath(imagePath);
                        conversation.setSenderName(recipientName);
                        conversation.setRecipientName(recipientName);
                        conversation.setMuted(muted.equals("1"));
                        conversation.setRecipientToken(recipientToken);
                        conversationList.add(conversation);
                    }
                    cursor.close();
                    return conversationList;
                }
            }
        return conversationList;
    }

    public Conversation getNewConversation(String conversationID)
    {
        Conversation conversation = null;

            if (db != null) {
                String[] projections = {
                        BaseColumns._ID,
                        DataBaseContract.Conversations.CONVERSATION_ID,
                        DataBaseContract.Conversations.LAST_MESSAGE,
                        DataBaseContract.Conversations.LAST_MESSAGE_TIME,
                        DataBaseContract.Conversations.LAST_MESSAGE_TYPE,
                        DataBaseContract.Conversations.LAST_MESSAGE_ID,
                        DataBaseContract.Conversations.RECIPIENT,
                        DataBaseContract.Conversations.MUTED,
                        DataBaseContract.Conversations.USER_UID,
                        DataBaseContract.Conversations.RECIPIENT_NAME,
                        DataBaseContract.Conversations.IMAGE_PATH,
                        DataBaseContract.User.TOKEN,
                        //DataBaseContract.Conversations.CONVERSATION_INDEX
                };
                String selection = DataBaseContract.Conversations.CONVERSATION_ID + " LIKE ?";
                String[] selectionArgs = {conversationID};
                Cursor cursor = db.query(DataBaseContract.Conversations.CONVERSATIONS_TABLE, projections, selection, selectionArgs, null, null, null);
                while (cursor.moveToNext()) {
                    String conversationIDs = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.CONVERSATION_ID));
                    String lastMessage = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.LAST_MESSAGE));
                    String lastMessageTime = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.LAST_MESSAGE_TIME));
                    int lastMessageType = cursor.getInt(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.LAST_MESSAGE_TYPE));
                    String recipient = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.RECIPIENT));
                    String lastMessageID = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.LAST_MESSAGE_ID));
                    String recipientName = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.RECIPIENT_NAME));
                    String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.IMAGE_PATH));
                    String muted = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.MUTED));
                    String recipientToken = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.TOKEN));
                    //int position = cursor.getInt(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.CONVERSATION_INDEX));
                    conversation = new Conversation(conversationIDs);
                    conversation.setLastMessageTimeFormatted(lastMessageTime);
                    conversation.setLastMessage(lastMessage);
                    conversation.setMessageType(lastMessageType);
                    conversation.setLastMessageID(lastMessageID);
                    conversation.setRecipient(recipient);
                    conversation.setRecipientImagePath(imagePath);
                    conversation.setSenderName(recipientName);
                    conversation.setRecipientName(recipientName);
                    conversation.setMuted(muted.equals("1"));
                    conversation.setRecipientToken(recipientToken);
                }
                cursor.close();

        }
        return conversation;
    }

    public void InsertConversationToDataBase(Conversation conversation) {
        String conversationID = conversation.getConversationID();

        ContentValues values = new ContentValues();
        values.put(DataBaseContract.Conversations.CONVERSATION_ID, conversationID);
        values.put(DataBaseContract.Conversations.MUTED, conversation.isMuted());
        values.put(DataBaseContract.Conversations.LAST_MESSAGE, conversation.getLastMessage());
        values.put(DataBaseContract.Conversations.LAST_MESSAGE_TIME, conversation.getLastMessageTime());
        values.put(DataBaseContract.Conversations.LAST_MESSAGE_TYPE, conversation.getMessageType());
        values.put(DataBaseContract.Conversations.RECIPIENT, conversation.getRecipient());
        values.put(DataBaseContract.Conversations.LAST_MESSAGE_ID, conversation.getLastMessageID());
        values.put(DataBaseContract.Conversations.IMAGE_PATH, conversation.getRecipientImagePath());
        values.put(DataBaseContract.Conversations.RECIPIENT_NAME, conversation.getSenderName());
        values.put(DataBaseContract.Conversations.USER_UID, user.getUserUID());
        long newRowId = db.insert(DataBaseContract.Conversations.CONVERSATIONS_TABLE, null, values);
        if (newRowId == -1)
            Log.e(DataBaseError, "error inserting data to database");

    }

    public void UpdateConversation(Conversation conversation) {

        ContentValues values = new ContentValues();
        values.put(DataBaseContract.Conversations.CONVERSATION_ID, conversation.getConversationID());
        values.put(DataBaseContract.Conversations.LAST_MESSAGE_ID, conversation.getLastMessageID());
        values.put(DataBaseContract.Conversations.LAST_MESSAGE_TYPE, conversation.getMessageType());
        values.put(DataBaseContract.Conversations.LAST_MESSAGE, conversation.getLastMessage());
        values.put(DataBaseContract.Conversations.LAST_MESSAGE_TIME, conversation.getLastMessageTime());
        values.put(DataBaseContract.Conversations.IMAGE_PATH, conversation.getRecipientImagePath());
        values.put(DataBaseContract.Conversations.RECIPIENT_NAME, conversation.getSenderName());
        values.put(DataBaseContract.Conversations.RECIPIENT,conversation.getRecipient());
        values.put(DataBaseContract.Conversations.MUTED, conversation.isMuted());
        values.put(DataBaseContract.Conversations.USER_UID, user.getUserUID());
        values.put(DataBaseContract.User.TOKEN, conversation.getRecipientToken());
        String selection = DataBaseContract.Conversations.CONVERSATION_ID + " LIKE ?";
        String[] selectionArgs = {conversation.getConversationID()};
        int count = db.update(DataBaseContract.Conversations.CONVERSATIONS_TABLE, values, selection, selectionArgs);
        if (count == -1)
            Log.e(DataBaseError,"updated more than 1 row in conversation update");

    }

    public void UpdateConversation(Message message) {
        if (db != null) {
            ContentValues values = new ContentValues();
            values.put(DataBaseContract.Conversations.CONVERSATION_ID, message.getConversationID());
            values.put(DataBaseContract.Conversations.USER_UID, user.getUserUID());
            values.put(DataBaseContract.Conversations.LAST_MESSAGE_ID, message.getMessageID());
            values.put(DataBaseContract.Conversations.LAST_MESSAGE, message.getMessage());
            values.put(DataBaseContract.Conversations.LAST_MESSAGE_TYPE, message.getMessageType());
            values.put(DataBaseContract.Conversations.LAST_MESSAGE_TIME, message.getSendingTime());
            values.put(DataBaseContract.Conversations.RECIPIENT_NAME, message.getRecipientName());
            values.put(DataBaseContract.Conversations.RECIPIENT,message.getRecipient());
            values.put(DataBaseContract.User.TOKEN, message.getSenderToken());
            String selection = DataBaseContract.Conversations.CONVERSATION_ID + " LIKE ?";
            String[] selectionArgs = {message.getConversationID()};
            long updatedRowNum = db.update(DataBaseContract.Conversations.CONVERSATIONS_TABLE, values, selection, selectionArgs);
            if (updatedRowNum != 1)
                Log.e(DataBaseError, "updated more than 1 row");
        }
    }



    public boolean isConversationExists(String conversationID) {
        if (db != null) {
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
            }
        }
        return false;
    }

    private void PrintUserTable(String currentUser)
    {
        String[] projections = {
                DataBaseContract.User._ID,
                DataBaseContract.User.USER_UID,
                DataBaseContract.User.USER_NAME,
                DataBaseContract.User.USER_LAST_NAME,
                DataBaseContract.User.USER_PICTURE_LINK,
                DataBaseContract.User.USER_TIME_CREATED,
                DataBaseContract.User.USER_PHONE_NUMBER,
                DataBaseContract.User.USER_LAST_STATUS
        };
        String selection = DataBaseContract.User.USER_UID + " LIKE ?";
        String[] selectionArgs = {currentUser};
        Cursor cursor = db.query(DataBaseContract.User.USER_TABLE, projections, selection, selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            String uid = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_UID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_NAME));
            String lastName = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_LAST_NAME));
            String pictureLink = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_PICTURE_LINK));
            String timeCreated = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_TIME_CREATED));
            String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_PHONE_NUMBER));
            String status = cursor.getColumnName(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_LAST_STATUS));
            print(uid);
            print(name);
            print(lastName);
            print(pictureLink);
            print(timeCreated);
            print(phoneNumber);
            print(status);
        }
        cursor.close();
    }

    private void print(String s)
    {
        System.out.println(s);
    }

    public User LoadUserFromDataBase(String userUID) {
        //PrintUserTable(userUID);
        if (db != null) {
            String[] projections = {
                    DataBaseContract.User._ID,
                    DataBaseContract.User.USER_UID,
                    DataBaseContract.User.USER_NAME,
                    DataBaseContract.User.USER_LAST_NAME,
                    DataBaseContract.User.USER_PICTURE_LINK,
                    DataBaseContract.User.USER_TIME_CREATED,
                    DataBaseContract.User.USER_PHONE_NUMBER,
                    DataBaseContract.User.USER_LAST_STATUS
            };
            String selection = DataBaseContract.User.USER_UID + " LIKE ?";
            String[] selectionArgs = {userUID};
            Cursor cursor = db.query(DataBaseContract.User.USER_TABLE, projections, selection, selectionArgs, null, null, null);
            if(cursor.getCount()>0) {
                cursor.moveToNext();
                User user = new User();
                String uid = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_UID));
                if (!uid.equals(userUID))
                    Log.e("dbActive", "got the wrong user from db");
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_NAME));
                String lastName = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_LAST_NAME));
                String pictureLink = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_PICTURE_LINK));
                String timeCreated = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_TIME_CREATED));
                String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_PHONE_NUMBER));
                String status = cursor.getColumnName(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_LAST_STATUS));
                user.setUserUID(uid);
                user.setName(name);
                user.setLastName(lastName);
                user.setPictureLink(pictureLink);
                user.setTimeCreated(timeCreated);
                user.setPhoneNumber(phoneNumber);
                user.setStatus(status);
                if (userUID.equals(currentUserUID))
                    this.user = user;
                cursor.close();
                return user;
            }
            else
                Log.e(DataBaseError, "cursor size in 0 or less for users for userUID: " + userUID);
        } else
            Log.e(DataBaseError, "db is null");
        return null;
    }



    public void UpdateMessage(@NonNull Message message) {

        if (db != null) {
            ContentValues values = new ContentValues();
            values.put(DataBaseContract.Messages.CONTENT, message.getMessage());
            String selection = DataBaseContract.Messages.MESSAGE_ID + " LIKE ?";
            String[] selectionArgs = {message.getMessageID()};
            long updatedRow = db.update(DataBaseContract.Messages.MESSAGES_TABLE, values, selection, selectionArgs);
            if (updatedRow != 1)
                Log.e(DataBaseError, "updated more than 1 message");
        }
        //LoadMessage(messageID, DataBaseContract.Messages.MESSAGE_ID + " = ?", "Edit Message");
    }

    public void DeleteMessage(@NonNull String messageID) {

        if (db != null) {
            String selection = DataBaseContract.Messages.MESSAGE_ID + " LIKE ?";
            String[] selectionArgs = {messageID};
            int deletedRows = db.delete(DataBaseContract.Messages.MESSAGES_TABLE, selection, selectionArgs);
            if (deletedRows == -1)
                Log.e(DataBaseError, "didn't delete anything - deleted rows = -1");
        }
    }

    public void DeleteConversation(String conversationID) {
        if (db != null) {
            String selection = DataBaseContract.Conversations.CONVERSATION_ID + " = ?";
            String[] selectionArgs = {conversationID};
            int rowSum = db.delete(DataBaseContract.Conversations.CONVERSATIONS_TABLE, selection, selectionArgs);
            if (rowSum != 1)
                Log.e(DataBaseError, "deleting conversation failed, more than 1 conversations were deleted or none were");
        }
    }
    public void SaveMessage(Message message) {
        if (db != null) {
            ContentValues values = new ContentValues();
            values.put(DataBaseContract.Messages.MESSAGE_ID, message.getMessageID());
            values.put(DataBaseContract.Conversations.CONVERSATION_ID, message.getConversationID());
            values.put(DataBaseContract.Messages.CONTENT, message.getMessage());
            values.put(DataBaseContract.Messages.RECIPIENT, message.getRecipient());
            values.put(DataBaseContract.Messages.SENDER, message.getSender());
            values.put(DataBaseContract.Messages.TIME_DELIVERED, message.getArrivingTime());
            values.put(DataBaseContract.Messages.TIME_SENT, message.getSendingTime());
            values.put(DataBaseContract.Messages.TYPE, message.getMessageType());
            values.put(DataBaseContract.Messages.STATUS, message.getMessageStatus());
            values.put(DataBaseContract.Messages.MESSAGE_IMAGE_PATH, message.getImagePath());
            values.put(DataBaseContract.Messages.MESSAGE_LONGITUDE, message.getLongitude());
            values.put(DataBaseContract.Messages.MESSAGE_LATITUDE, message.getLatitude());
            values.put(DataBaseContract.Messages.MESSAGE_ADDRESS, message.getLocationAddress());
            values.put(DataBaseContract.Messages.MESSAGE_RECORDING_PATH, message.getRecordingPath());
            if (message.getMessageType() == MessageType.webMessage.ordinal())
                values.put(DataBaseContract.Messages.MESSAGE_LINK, message.getMessage());
            long newRowId = db.insert(DataBaseContract.Messages.MESSAGES_TABLE, null, values);
            if (newRowId == -1)
                Log.e(DataBaseError, "inserted more than 1 row");
        }
    }

   public void CreateNewConversation(Message message) {
        if (db != null) {
            ContentValues values = new ContentValues();
            values.put(DataBaseContract.Conversations.CONVERSATION_ID, message.getConversationID());
            values.put(DataBaseContract.Conversations.USER_UID, user.getUserUID());
            values.put(DataBaseContract.Conversations.LAST_MESSAGE_ID, message.getMessageID());
            values.put(DataBaseContract.Conversations.LAST_MESSAGE, message.getMessage());
            values.put(DataBaseContract.Conversations.LAST_MESSAGE_TYPE, message.getMessageType());
            values.put(DataBaseContract.Conversations.LAST_MESSAGE_TIME, message.getSendingTime());
            values.put(DataBaseContract.Conversations.RECIPIENT_NAME, message.getRecipientName());
            values.put(DataBaseContract.Conversations.RECIPIENT,message.getRecipient());
            values.put(DataBaseContract.Conversations.MUTED, false);
            long newConversationID = db.insert(DataBaseContract.Conversations.CONVERSATIONS_TABLE, null, values);
            if (newConversationID == -1)
                Log.e(DataBaseError, "inserted more than 1 row");
        }
    }

   public void MarkAsRead(String messageID) {
        if (db != null) {
            String selection = DataBaseContract.Messages.MESSAGE_ID + " LIKE ?";
            String[] selectionArgs = {messageID};
            ContentValues contentValues = new ContentValues();
            contentValues.put(DataBaseContract.Messages.STATUS, ConversationActivity.MESSAGE_SEEN);
            db.update(DataBaseContract.Messages.MESSAGES_TABLE, contentValues, selection, selectionArgs);
        }
    }

    public List<Message> LoadMessages(@NonNull final String id, @NonNull final String selection) {
        List<Message>messages = new ArrayList<>();
        if (db != null) {

            String[] projections = {
                    DataBaseContract.Messages.MESSAGE_ID,
                    DataBaseContract.Conversations.CONVERSATION_ID,
                    DataBaseContract.Messages.CONTENT,
                    DataBaseContract.Messages.SENDER,
                    DataBaseContract.Messages.RECIPIENT,
                    DataBaseContract.Messages.TIME_DELIVERED,
                    DataBaseContract.Messages.TIME_SENT,
                    DataBaseContract.Messages.STATUS,
                    DataBaseContract.Messages.MESSAGE_SENDER_NAME,
                    DataBaseContract.Messages.MESSAGE_RECIPIENT_NAME,
                    DataBaseContract.Messages.MESSAGE_FILE_PATH,
                    DataBaseContract.Messages.MESSAGE_ADDRESS,
                    DataBaseContract.Messages.MESSAGE_LATITUDE,
                    DataBaseContract.Messages.MESSAGE_LONGITUDE,
                    DataBaseContract.Messages.MESSAGE_LINK,
                    DataBaseContract.Messages.MESSAGE_LINK_CONTENT,
                    DataBaseContract.Messages.MESSAGE_LINK_TITLE,
                    DataBaseContract.Messages.MESSAGE_STAR
            };
            String[] selectionArgs = {id};
            Cursor cursor = db.query(DataBaseContract.Messages.MESSAGES_TABLE, projections, selection, selectionArgs, null, null, null);
            while (cursor.moveToNext()) {
                String messageID = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_ID));
                String conversationID = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.CONVERSATION_ID));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.CONTENT));
                String senderUID = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.SENDER));
                String timeDelivered = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.TIME_DELIVERED));
                String timeSent = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.TIME_SENT));
                String senderName = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_SENDER_NAME));
                String recipientName = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_RECIPIENT_NAME));
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_FILE_PATH));
                String address = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_ADDRESS));
                String longitude = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_LONGITUDE));
                String latitude = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_LATITUDE));
                String link = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_LINK));
                String star = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_STAR));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.STATUS));
                String recipient = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.RECIPIENT));
                Message message = new Message();
                message.setMessageID(messageID);
                message.setMessage(content);
                message.setConversationID(conversationID);
                message.setMessageTime(timeSent);
                message.setSender(senderUID);
                message.setArrivingTime(timeDelivered);
                message.setSenderName(senderName);
                message.setRecipient(recipient);
                message.setRecipientName(recipientName);
                message.setFilePath(filePath);
                message.setLocationAddress(address);
                message.setLongitude(longitude);
                message.setLatitude(latitude);
                if (link != null)
                    message.setMessage(link);
                if (star != null)
                    message.setStar(star.equals("1"));
                message.setMessageStatus(status);
                messages.add(message);
            }
            cursor.close();
            return messages;
        }
        return null;
    }

    //called only if user doesn't exists - the first lunch of the app
    public void InsertUser(User user) {
        ContentValues values = CreateUserValues(user);
        long rowID = db.insert(DataBaseContract.User.USER_TABLE, null, values);
        if (rowID == -1)
            Log.e(DataBaseError, "error inserting user to database");
    }

    //on each login, the user table is updated with the current login user
    public void UpdateUser(User user) {
        ContentValues values = CreateUserValues(user);
        int count = db.update(DataBaseContract.User.USER_TABLE, values, null, null);
        if (count != 1)
            Log.e(DataBaseError, "more than 1 or 0 rows were updated in the user table");
    }

    private ContentValues CreateUserValues(User user) {
        ContentValues values = new ContentValues();
        values.put(DataBaseContract.User.USER_UID, user.getUserUID());
        values.put(DataBaseContract.User.USER_NAME, user.getName());
        values.put(DataBaseContract.User.USER_LAST_NAME, user.getLastName());
        values.put(DataBaseContract.User.USER_TIME_CREATED, user.getTimeCreated());
        values.put(DataBaseContract.User.USER_PICTURE_LINK, user.getPictureLink());
        if (user.getPhoneNumber() != null)
            values.put(DataBaseContract.User.USER_PHONE_NUMBER, user.getPhoneNumber());
        values.put(DataBaseContract.User.USER_LAST_STATUS, user.getStatus());
        return values;
    }

    public void CheckIfUserExist(User user) {
        if (db != null) {
            String[] projections = {
                    DataBaseContract.User._ID,
                    DataBaseContract.User.USER_UID
            };
            Cursor cursor = db.query(DataBaseContract.User.USER_TABLE, projections, null, null, null, null, null);
            if (cursor.getCount() > 1)
                Log.e(DataBaseError, "cursor contains more than 1 user");
            else if (cursor.moveToNext())
                UpdateUser(user);
            else
                InsertUser(user);
            cursor.close();
        }
    }

    public void ResetDB()
    {
        db.execSQL("DROP TABLE IF EXISTS " + DataBaseContract.Conversations.CONVERSATIONS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DataBaseContract.Messages.MESSAGES_TABLE);
       // db.execSQL("DROP TABLE IF EXISTS " + DataBaseContract.User.USER_TABLE);
        dbHelper.onUpgrade(db, db.getVersion(), db.getVersion() + 1);
    }

    public boolean CheckIfExist(String ID, boolean idType) {
        if (ID != null) {
            if (db != null) {
                String[] projections = {
                        BaseColumns._ID,
                        DataBaseContract.Conversations.CONVERSATION_ID,
                        DataBaseContract.Conversations.LAST_MESSAGE_ID
                };
                //String sortOrder = DataBaseContract.Conversations.CONVERSATIONS_ID_COLUMN_NAME + " DESC";
                String selections = DataBaseContract.Conversations.USER_UID + " LIKE ?";
                String[] selectionArgs = {user.getUserUID()};
                Cursor cursor = db.query(DataBaseContract.Conversations.CONVERSATIONS_TABLE, projections, selections, selectionArgs, null, null, null);
                while (cursor.moveToNext()) {
                    String IDs;
                    if (idType) {

                        IDs = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.CONVERSATION_ID));
                    } else {
                        IDs = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.LAST_MESSAGE_ID));
                    }
                    if (IDs.equals(ID)) {
                        cursor.close();
                        return true;
                    }
                }
                cursor.close();
            }
        } else
            Log.e(DataBaseError, "id is null");
        return false;
    }

    public Message RetrieveMessage(String id, String selection) {
        if (db != null) {
            String[] projections = {
                    DataBaseContract.Messages.MESSAGE_ID,
                    DataBaseContract.Conversations.CONVERSATION_ID,
                    DataBaseContract.Messages.CONTENT,
                    DataBaseContract.Messages.SENDER,
                    DataBaseContract.Messages.MESSAGE_SENDER_NAME,
                    DataBaseContract.Messages.RECIPIENT,
                    DataBaseContract.Messages.TIME_DELIVERED,
                    DataBaseContract.Messages.TIME_SENT,
                    DataBaseContract.Messages.TYPE,
                    DataBaseContract.Messages.STATUS,
                    DataBaseContract.Messages.MESSAGE_IMAGE_PATH,
                    DataBaseContract.Messages.MESSAGE_LONGITUDE,
                    DataBaseContract.Messages.MESSAGE_LATITUDE,
                    DataBaseContract.Messages.MESSAGE_ADDRESS,
                    DataBaseContract.Messages.MESSAGE_RECORDING_PATH,
                    DataBaseContract.Messages.MESSAGE_STAR,
                    DataBaseContract.Messages.MESSAGE_FILE_PATH,
                    DataBaseContract.Messages.MESSAGE_RECIPIENT_NAME
            };
            String sortOrder = DataBaseContract.Messages.TIME_SENT + " DESC LIMIT 1";
            String[] selectionArgs = {id};
            Cursor cursor = db.query(DataBaseContract.Messages.MESSAGES_TABLE, projections, selection, selectionArgs, null, null, sortOrder);
            Message message = new Message();
            while (cursor.moveToNext()) {
                String conversationID = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.CONVERSATION_ID));
                String messageContent = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.CONTENT));
                String messageSender = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.SENDER));
                String recipient = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.RECIPIENT));
                long messageTimeDelivered = cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.TIME_DELIVERED));
                String messageTimeSent = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.TIME_SENT));
                int messageType = cursor.getInt(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.TYPE));
                String messageStatus = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.STATUS));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_IMAGE_PATH));
                String longitude = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_LONGITUDE));
                String latitude = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_LATITUDE));
                String address = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_ADDRESS));
                String recordingPath = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_RECORDING_PATH));
                String star = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_STAR));
                String senderName = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_SENDER_NAME));
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_FILE_PATH));
                message.setMessageID(id);
                message.setMessage(messageContent);
                message.setConversationID(conversationID);
                message.setRecipient(recipient);
                message.setSender(messageSender);
                message.setMessageType(messageType);
                message.setMessageTime(messageTimeSent);
                message.setReadAt(messageTimeDelivered);
                message.setMessageStatus(messageStatus);
                message.setImagePath(imagePath);
                message.setLongitude(longitude);
                message.setLatitude(latitude);
                message.setLocationAddress(address);
                message.setRecordingPath(recordingPath);
                message.setFilePath(filePath);
                message.setSenderName(senderName);
                if (star != null)
                    message.setStar(star.equals("1"));
                message.setMessageID(id);
            }
            cursor.close();
            return message;
        } else
            return null;
    }

    public void UpdateMessageMetaData(String messageID, String messageStatus, String readAt) {

        if (db != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DataBaseContract.Messages.MESSAGE_READ_TIME, readAt);
            contentValues.put(DataBaseContract.Messages.STATUS, messageStatus);
            String selection = DataBaseContract.Messages.MESSAGE_ID + " LIKE ?";
            String[] selectionArgs = {messageID};
            int numRowUpdate = db.update(DataBaseContract.Messages.MESSAGES_TABLE, contentValues, selection, selectionArgs);
            if (numRowUpdate != 1)
                Log.e(DataBaseError, "updated more than 1 row of messageStatus");
        }
    }

    public void UpdateConversationToken(String conversationID, String token) {
        if (db != null) {
            ContentValues conversationValues = new ContentValues();
            conversationValues.put(DataBaseContract.User.TOKEN, token);
            String selection = DataBaseContract.Conversations.CONVERSATION_ID + " LIKE ?";
            String[] selectionArgs = {conversationID};
            long newConversationRowId = db.update(DataBaseContract.Conversations.CONVERSATIONS_TABLE, conversationValues, selection, selectionArgs);
            if (newConversationRowId != 1)
                Log.e(DataBaseError, "updating conversation recipient token failed - updated to many rows");
        }
    }


    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean CheckIfExistsInDataBase(Message message) {
        if (db != null) {
            String[] projections = {
                    BaseColumns._ID,
                    DataBaseContract.Messages.MESSAGE_ID
            };
            // String query = "SELECT " + DataBaseContract.Messages.MESSAGE_ID + " FROM " + DataBaseContract.Messages.MESSAGES_TABLE + " ORDER BY " + DataBaseContract.Messages.MESSAGE_ID + " DESC LIMIT 1";
            //in order to not scan the database each time from the start, we should start scanning from the last message received - time. since messages
            //come in a linear order, a message that was sent now will never arrive prior to the message that was sent before it
            // String selection = DataBaseContract.Messages.MESSAGE_TIME_DELIVERED_COLUMN_NAME + " = ?";
            String selection = DataBaseContract.Conversations.CONVERSATION_ID + " = ?";
            String[] selectionArgs = {message.getConversationID()};
            String sortOrder = DataBaseContract.Messages.TIME_SENT + " DESC LIMIT 1";
            Cursor cursor = db.query(DataBaseContract.Messages.MESSAGES_TABLE, projections, selection, selectionArgs, null, null, sortOrder);
            if (cursor.moveToNext()) {
                String ID = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_ID));
                long id = Long.parseLong(ID);
                long messageId = Long.parseLong(message.getMessageID());
                cursor.close();
                return id >= messageId;
            } else {
                cursor.close();
                return false;
            }
        }
        return false;
    }

    public void PrintConversation(String conversationID) {
        if (db != null) {
            String[] projection = {
                    BaseColumns._ID,
                    DataBaseContract.Messages.MESSAGE_ID,
                    DataBaseContract.Conversations.CONVERSATION_ID,
                    DataBaseContract.Messages.CONTENT,
                    DataBaseContract.Messages.RECIPIENT,
                    DataBaseContract.Messages.SENDER,
                    DataBaseContract.Messages.TIME_DELIVERED,
                    DataBaseContract.Messages.TIME_SENT,
                    DataBaseContract.Messages.TYPE,
                    DataBaseContract.Messages.STATUS
            };
            String selection = DataBaseContract.Conversations.CONVERSATION_ID + " LIKE ?";
            String[] selectionArgs = {conversationID};
            Cursor cursor = db.query(DataBaseContract.Messages.MESSAGES_TABLE, projection, selection, selectionArgs, null, null, null);
            List<String> MessagesIDs = new ArrayList<>();
            List<String> MessagesContent = new ArrayList<>();
            List<String> MessagesRecipient = new ArrayList<>();
            List<String> MessagesSender = new ArrayList<>();
            List<String> MessageTimeDelivered = new ArrayList<>();
            List<String> MessagesTimeSent = new ArrayList<>();
            List<String> MessagesTypes = new ArrayList<>();
            List<String> MessagesStatus = new ArrayList<>();

            while (cursor.moveToNext()) {
                String messageID = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_ID));
                MessagesIDs.add(messageID);
                String messageContent = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.CONTENT));
                MessagesContent.add(messageContent);
                String messagesRecipient = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.RECIPIENT));
                MessagesRecipient.add(messagesRecipient);
                String messagesSender = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.SENDER));
                MessagesSender.add(messagesSender);
                String messageTimeDelivered = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.TIME_DELIVERED));
                MessageTimeDelivered.add(messageTimeDelivered);
                String messageTimeSent = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.TIME_SENT));
                MessagesTimeSent.add(messageTimeSent);
                String messagesType = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.TYPE));
                MessagesTypes.add(messagesType);
                String messagesStatus = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.STATUS));
                MessagesStatus.add(messagesStatus);
            }
            cursor.close();

            System.out.println("the messages: " + MessagesContent);
            System.out.println("messages IDs: " + MessagesIDs);
            System.out.println(MessagesRecipient);
            System.out.println(MessagesSender);
            System.out.println(MessageTimeDelivered);
            System.out.println(MessagesTimeSent);
            System.out.println(MessagesTypes);
            System.out.println(MessagesStatus);
        }
    }

    public void Block(String uid, String conversationID,boolean blocked)
    {
        if (db != null) {
            String selection = DataBaseContract.User.USER_UID + " LIKE ?";
            String[] selectionArgs = {uid};
            if (blocked) {
                ContentValues values = new ContentValues();
                values.put(DataBaseContract.BlockedUsers.USER_UID, uid);
                int rowSum = db.update(DataBaseContract.BlockedUsers.BLOCKED_USERS_TABLE, values, selection, selectionArgs);
                if (rowSum != 1)
                    Log.e(DataBaseError, "updated more than 1 row when blocking users");
            } else {
                int rowSum = db.delete(DataBaseContract.BlockedUsers.BLOCKED_USERS_TABLE, selection, selectionArgs);
                if (rowSum > 1)
                    Log.e(DataBaseError, "deleted more than 1 blocked user from blocked table");
            }

        }
    }

    public void Mute(String conversationID,boolean muted)
    {
        if (db != null) {
            String selection = DataBaseContract.Conversations.CONVERSATION_ID + " LIKE ?";
            String[] selectionArgs = {conversationID};
            ContentValues contentValues = new ContentValues();
            contentValues.put("muted", muted);
            int rowNum = db.update(DataBaseContract.Conversations.CONVERSATIONS_TABLE, contentValues, selection, selectionArgs);
            if (rowNum != 1)
                Log.e(DataBaseError, "Updating mute values failed, updated more than 1 row");
        }
    }
}
