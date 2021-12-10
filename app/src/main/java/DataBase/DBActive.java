package DataBase;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import Consts.ConversationType;
import Consts.MessageType;
import NormalObjects.Conversation;
import NormalObjects.Message;
import NormalObjects.User;

public class DBActive {

    @SuppressWarnings("FieldMayBeFinal")
    private SQLiteDatabase db;
    @SuppressWarnings("FieldMayBeFinal")
    private DataBase dbHelper;
    private final String currentUserUID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    private final String DataBaseError = "db error";
    private User user;
    private static DBActive active = null;

    public static DBActive getInstance(Context context) {
        if (active == null) {
            active = new DBActive(context);
        }
        return active;
    }

    private DBActive(Context context) {
        dbHelper = new DataBase(context);
        db = dbHelper.getWritableDatabase();
        //resetDB();
        printTablesColumns(DataBaseContract.Messages.MESSAGES_TABLE);
        printTablesColumns(DataBaseContract.Conversations.CONVERSATIONS_TABLE);
        printTablesColumns(DataBaseContract.User.USER_TABLE);
    }

    //the following can also be done with a simple join query but the following is consistent with the rest of the code and sql injection safe
    public String findConversationByUserPhone(String phoneNumber)
    {
        String conversationID = null;
        if (db!=null)
        {
            String[] projections = {
                    DataBaseContract.User.USER_UID
            };
            String selection = DataBaseContract.User.USER_PHONE_NUMBER + " LIKE ?";
            String[] selectionArgs = {phoneNumber};
            Cursor cursor = db.query(DataBaseContract.User.USER_TABLE,projections,selection,selectionArgs,null,null,null);
            cursor.moveToNext();
            if (cursor.getCount() != 0) {
                String userID = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_UID));
                if (userID != null)//should never pass on this if. userID must never be null
                {
                    projections[0] = DataBaseContract.Conversations.CONVERSATION_ID;
                    selection = DataBaseContract.User.USER_UID + " LIKE ?";
                    selectionArgs[0] = userID;
                    cursor = db.query(DataBaseContract.Group.GroupTable, projections, selection, selectionArgs, null, null, null);
                    cursor.moveToNext();
                    conversationID = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.CONVERSATION_ID));
                }
            }
            cursor.close();
        }
        return conversationID;
    }
    //for debug only
    private void printTable(String tableName)
    {
        if (db!=null)
        {

            Cursor cursor = db.query(tableName,null,null,null,null,null,null);
            String[]cNames = cursor.getColumnNames();
            while (cursor.moveToNext())
            {
                for (String cName : cNames) {
                    String s = cursor.getString(cursor.getColumnIndexOrThrow(cName));
                    print(s);
                }
            }
            cursor.close();
        }
    }

    public List<User> loadUsers(String conversationID) {
        //printTable(DataBaseContract.Group.GroupTable);
        Log.i("loading users", "loading users for group");
        List<User> recipients = new ArrayList<>();
        if (isConversationExists(conversationID)) {
            if (db != null) {
                String[] groupProjections = {
                        DataBaseContract.Conversations.CONVERSATION_ID,
                        DataBaseContract.User.USER_UID
                };
                String groupSelection = DataBaseContract.Conversations.CONVERSATION_ID + " LIKE ?";
                String[] groupSelectionArgs = {conversationID};
                Cursor cursor = db.query(DataBaseContract.Group.GroupTable, groupProjections, groupSelection, groupSelectionArgs, null, null, null);
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        String userID = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_UID));
                        recipients.add(loadUserFromDataBase(userID));
                    }
                }

                cursor.close();
            }
        }
        else {
            Log.e(DataBaseError,"conversation doesn't exists - can't load users");
        }
        return recipients;
    }
    //gets all the conversations from the database
    public List<Conversation> getConversations() {
        List<Conversation> conversationList = new ArrayList<>();
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
                    DataBaseContract.Conversations.BLOCKED,
                    DataBaseContract.Conversations.GROUP_NAME,
                    DataBaseContract.Conversations.CONVERSATION_TYPE
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
                    String blocked = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.BLOCKED));
                    String groupName = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.GROUP_NAME));
                    String conversationType = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.CONVERSATION_TYPE));
                    Conversation conversation = new Conversation(conversationIDs);
                    conversation.setLastMessageTimeFormatted(lastMessageTime);
                    conversation.setGroupName(groupName);
                    conversation.setLastMessage(lastMessage);
                    conversation.setMessageType(lastMessageType);
                    conversation.setLastMessageID(lastMessageID);
                    conversation.setRecipient(recipient);
                    conversation.setRecipientImagePath(imagePath);
                    conversation.setSenderName(recipientName);
                    conversation.setRecipientName(recipientName);
                    if (Integer.parseInt(conversationType) == ConversationType.single.ordinal())
                        conversation.setConversationType(ConversationType.single);
                    else if (Integer.parseInt(conversationType) == ConversationType.group.ordinal())
                        conversation.setConversationType(ConversationType.group);
                    else if (Integer.parseInt(conversationType) == ConversationType.sms.ordinal())
                        conversation.setConversationType(ConversationType.sms);

                    if (muted != null)
                        conversation.setMuted(muted.equals("muted"));
                    else
                        conversation.setMuted(false);
                    if (blocked != null)
                        conversation.setBlocked(blocked.equals("blocked"));
                    else
                        conversation.setBlocked(false);
                    conversation.setRecipientToken(recipientToken);
                    conversationList.add(conversation);
                }
                cursor.close();
                return conversationList;
            }
        }
        return conversationList;
    }

    public Conversation getNewConversation(String conversationID) {
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
                    DataBaseContract.Conversations.GROUP_NAME,
                    DataBaseContract.Conversations.CONVERSATION_TYPE
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
                String group = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.GROUP_NAME));
                String conversationType = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.CONVERSATION_TYPE));
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
                conversation.setGroupName(group);
                if (Integer.parseInt(conversationType) == ConversationType.single.ordinal())
                    conversation.setConversationType(ConversationType.single);
                else if (Integer.parseInt(conversationType) == ConversationType.group.ordinal())
                    conversation.setConversationType(ConversationType.group);
                else if (Integer.parseInt(conversationType) == ConversationType.sms.ordinal())
                    conversation.setConversationType(ConversationType.sms);
            }
            cursor.close();

        }
        return conversation;
    }

    public synchronized void insertConversationToDataBase(Conversation conversation) {
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
        values.put(DataBaseContract.Conversations.GROUP_NAME,conversation.getGroupName());
        values.put(DataBaseContract.Conversations.CONVERSATION_TYPE,conversation.getConversationType().name());
        long newRowId = db.insert(DataBaseContract.Conversations.CONVERSATIONS_TABLE, null, values);
        if (newRowId == -1)
            Log.e(DataBaseError, "error inserting data to database");

    }

    public synchronized void updateConversation(Conversation conversation) {

        ContentValues values = new ContentValues();
        values.put(DataBaseContract.Conversations.CONVERSATION_ID, conversation.getConversationID());
        values.put(DataBaseContract.Conversations.LAST_MESSAGE_ID, conversation.getLastMessageID());
        values.put(DataBaseContract.Conversations.LAST_MESSAGE_TYPE, conversation.getMessageType());
        values.put(DataBaseContract.Conversations.LAST_MESSAGE, conversation.getLastMessage());
        values.put(DataBaseContract.Conversations.LAST_MESSAGE_TIME, conversation.getLastMessageTime());
        values.put(DataBaseContract.Conversations.IMAGE_PATH, conversation.getRecipientImagePath());
        values.put(DataBaseContract.Conversations.RECIPIENT_NAME, conversation.getSenderName());
        values.put(DataBaseContract.Conversations.RECIPIENT, conversation.getRecipient());
        values.put(DataBaseContract.Conversations.MUTED, conversation.isMuted());
        values.put(DataBaseContract.Conversations.USER_UID, user.getUserUID());
        values.put(DataBaseContract.User.TOKEN, conversation.getRecipientToken());
        String selection = DataBaseContract.Conversations.CONVERSATION_ID + " LIKE ?";
        String[] selectionArgs = {conversation.getConversationID()};
        int count = db.update(DataBaseContract.Conversations.CONVERSATIONS_TABLE, values, selection, selectionArgs);
        if (count == -1)
            Log.e(DataBaseError, "updated more than 1 row in conversation update");

    }

    public synchronized void updateConversationLastMessage(String conversationID, String message) {
        if (db != null) {
            ContentValues values = new ContentValues();
            values.put(DataBaseContract.Conversations.LAST_MESSAGE, message);
            String selection = DataBaseContract.Conversations.CONVERSATION_ID + " LIKE ?";
            String[] selectionArgs = {conversationID};
            long updatedRowNum = db.update(DataBaseContract.Conversations.CONVERSATIONS_TABLE, values, selection, selectionArgs);
            if (updatedRowNum != 1)
                Log.e(DataBaseError, "updated more than 1 row");
        }
    }

    public synchronized void updateConversation(Message message) {
        if (db != null) {
            ContentValues values = new ContentValues();
            values.put(DataBaseContract.Conversations.CONVERSATION_ID, message.getConversationID());
            values.put(DataBaseContract.Conversations.USER_UID, user.getUserUID());
            values.put(DataBaseContract.Conversations.LAST_MESSAGE_ID, message.getMessageID());
            values.put(DataBaseContract.Conversations.LAST_MESSAGE, message.getMessage());
            values.put(DataBaseContract.Conversations.LAST_MESSAGE_TYPE, message.getMessageType());
            values.put(DataBaseContract.Conversations.LAST_MESSAGE_TIME, message.getSendingTime());
            values.put(DataBaseContract.Conversations.GROUP_NAME,message.getGroupName());
            String selection = DataBaseContract.Conversations.CONVERSATION_ID + " LIKE ?";
            String[] selectionArgs = {message.getConversationID()};
            long updatedRowNum = db.update(DataBaseContract.Conversations.CONVERSATIONS_TABLE, values, selection, selectionArgs);
            if (updatedRowNum != 1)
                Log.e(DataBaseError, "updated more than 1 row");
        }
    }

    public synchronized boolean isConversationExists(String conversationID) {
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

    private void println(String s) {
        System.out.println(s);
    }

    private void print(String s)
    {
        System.out.print(s);
    }


    public synchronized User loadUserFromDataBase(String userUID) {
        if (userUID == null)
            Log.e("DBActive", "user UID is null");
        else {
            if (db != null) {
                String[] projections = {
                        DataBaseContract.User._ID,
                        DataBaseContract.User.USER_UID,
                        DataBaseContract.User.USER_NAME,
                        DataBaseContract.User.USER_LAST_NAME,
                        DataBaseContract.User.USER_PICTURE_LINK,
                        DataBaseContract.User.USER_TIME_CREATED,
                        DataBaseContract.User.USER_PHONE_NUMBER,
                        DataBaseContract.User.USER_LAST_STATUS,
                        DataBaseContract.User.TOKEN,
                        DataBaseContract.User.BLOCKED
                };
                String selection = DataBaseContract.User.USER_UID + " LIKE ?";
                String[] selectionArgs = {userUID};
                Cursor cursor = db.query(DataBaseContract.User.USER_TABLE, projections, selection, selectionArgs, null, null, null);
                if (cursor.getCount() > 0) {
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
                    String status = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_LAST_STATUS));
                    String token = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.TOKEN));
                    String blocked = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.BLOCKED));
                    user.setUserUID(uid);
                    user.setName(name);
                    user.setLastName(lastName);
                    user.setPictureLink(pictureLink);
                    user.setTimeCreated(timeCreated);
                    user.setPhoneNumber(phoneNumber);
                    user.setStatus(status);
                    user.setToken(token);
                    if (blocked != null)
                        user.setBlocked(blocked.equals("blocked"));
                    if (userUID.equals(currentUserUID))
                        this.user = user;
                    cursor.close();
                    return user;
                } else
                    Log.e(DataBaseError, "cursor size in 0 or less for users for userUID: " + userUID);
            } else
                Log.e(DataBaseError, "db is null");
            return null;
        }
        return null;
    }

    public synchronized void updateMessageStatus(String id, String status) {
        if (db != null) {
            ContentValues values = new ContentValues();
            values.put(DataBaseContract.Messages.STATUS, status);
            String selection = DataBaseContract.Messages.MESSAGE_ID + " LIKE ?";
            String[] selectionArgs = {id};
            long updatedRow = db.update(DataBaseContract.Messages.MESSAGES_TABLE, values, selection, selectionArgs);
            if (updatedRow != 1)
                Log.e(DataBaseError, "error updating status - updated more than 1 row");
        }
    }

    public synchronized void updateMessage(String messageID, String content, String time) {
        if (db != null) {
            ContentValues values = new ContentValues();
            values.put(DataBaseContract.Messages.CONTENT, content);
            if (time!=null)
                values.put(DataBaseContract.Messages.TIME_DELIVERED,time);
            String selection = DataBaseContract.Messages.MESSAGE_ID + " LIKE ?";
            String[] selectionArgs = {messageID};
            long updatedRow = db.update(DataBaseContract.Messages.MESSAGES_TABLE, values, selection, selectionArgs);
            if (updatedRow != 1)
                Log.e(DataBaseError, "updated more than 1 message");
        }
    }

    public synchronized void updateMessage(@NonNull Message message) {

        if (db != null) {
            ContentValues values = new ContentValues();
            if (message.getMessage() != null)
                values.put(DataBaseContract.Messages.CONTENT, message.getMessage());
            if (message.getRecordingPath() != null)
                values.put(DataBaseContract.Messages.MESSAGE_RECORDING_PATH, message.getRecordingPath());
            if (message.getImagePath() != null)
                values.put(DataBaseContract.Messages.MESSAGE_IMAGE_PATH, message.getImagePath());
            values.put(DataBaseContract.Messages.MESSAGE_STAR, message.isStar());
            String selection = DataBaseContract.Messages.MESSAGE_ID + " LIKE ?";
            String[] selectionArgs = {message.getMessageID()};
            long updatedRow = db.update(DataBaseContract.Messages.MESSAGES_TABLE, values, selection, selectionArgs);
            if (updatedRow != 1)
                Log.e(DataBaseError, "updated more than 1 message");
        }
    }

    public synchronized void deleteMessage(@NonNull String messageID) {

        if (db != null) {
            String selection = DataBaseContract.Messages.MESSAGE_ID + " LIKE ?";
            String[] selectionArgs = {messageID};
            int deletedRows = db.delete(DataBaseContract.Messages.MESSAGES_TABLE, selection, selectionArgs);
            if (deletedRows == -1)
                Log.e(DataBaseError, "didn't delete anything - deleted rows = -1");
        }
    }

    public synchronized void deleteConversation(String conversationID) {
        if (db != null) {
            String selection = DataBaseContract.Conversations.CONVERSATION_ID + " = ?";
            String[] selectionArgs = {conversationID};
            int rowSum = db.delete(DataBaseContract.Conversations.CONVERSATIONS_TABLE, selection, selectionArgs);
            if (rowSum == 0)
                Log.e(DataBaseError, "deleting conversation failed");
            rowSum = db.delete(DataBaseContract.Group.GroupTable,selection,selectionArgs);
            if (rowSum == 0)
                Log.e(DataBaseError, "deleting conversation failed");
        }
    }

    //for debug only
    private void printTablesColumns(String tableName)
    {
        println("table " + tableName + " columns");
       Cursor cursor = db.query(tableName,null,null,null,null,null,null);
       String[] columnNames = cursor.getColumnNames();
       for (String columnName : columnNames)
       {
           print(columnName + " ");
       }
       println("");
       cursor.close();
    }

    public synchronized void saveMessage(Message message) {
        if (db != null) {
            ContentValues values = new ContentValues();
            values.put(DataBaseContract.Messages.MESSAGE_ID, message.getMessageID());
            values.put(DataBaseContract.Conversations.CONVERSATION_ID, message.getConversationID());
            values.put(DataBaseContract.Messages.CONTENT, message.getMessage());
            values.put(DataBaseContract.Conversations.GROUP_NAME, message.getGroupName());
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
            values.put(DataBaseContract.Messages.MESSAGE_FILE_PATH, message.getFilePath());
            values.put(DataBaseContract.Messages.QUOTE, message.getQuoteMessage());
            values.put(DataBaseContract.Messages.QUOTE_ID, message.getQuotedMessageID());
            if (message.getMessageType() == MessageType.webMessage.ordinal())
                values.put(DataBaseContract.Messages.MESSAGE_LINK, message.getMessage());
            long newRowId = db.insert(DataBaseContract.Messages.MESSAGES_TABLE, null, values);
            if (newRowId == -1)
                Log.e(DataBaseError, "inserted more than 1 row");
        }
    }

    public synchronized void createNewConversation(Message message, ConversationType conversationType)
    {
        if (db != null) {
            ContentValues values = new ContentValues();
            if (message.getRecipients().size() == 1)
                values.put(DataBaseContract.Conversations.RECIPIENT,message.getRecipients().get(0));
            values.put(DataBaseContract.Conversations.CONVERSATION_ID, message.getConversationID());
            values.put(DataBaseContract.Conversations.LAST_MESSAGE_ID, message.getMessageID());
            values.put(DataBaseContract.Conversations.LAST_MESSAGE, message.getMessage());
            values.put(DataBaseContract.Conversations.LAST_MESSAGE_TYPE, message.getMessageType());
            values.put(DataBaseContract.Conversations.LAST_MESSAGE_TIME, message.getSendingTime());
            values.put(DataBaseContract.Conversations.GROUP_NAME, message.getGroupName());
            values.put(DataBaseContract.Conversations.MUTED, false);
            values.put(DataBaseContract.Conversations.CONVERSATION_TYPE,conversationType.ordinal());
            values.put(DataBaseContract.Conversations.BLOCKED,"false");
            values.put(DataBaseContract.Conversations.MUTED,"false");
            long newConversationID = db.insert(DataBaseContract.Conversations.CONVERSATIONS_TABLE, null, values);
            if (newConversationID == -1)
                Log.e(DataBaseError, "inserted more than 1 row");
            printConversationTable();
            if (!message.getRecipients().contains(currentUserUID))
            {
                createNewGroup(message.getConversationID(),message.getRecipients());
            }
            else
            {
                List<String> recipients = message.getRecipients();
                for (String s : recipients)
                {
                    if (s.equals(currentUserUID))
                    {
                        recipients.remove(s);
                        break;
                    }
                }
                recipients.add(message.getSender());
                createNewGroup(message.getConversationID(),recipients);
            }
        }
    }

    public synchronized ConversationType loadConversationType(String conversationID)
    {
        ConversationType type = null;
        if (db!=null)
        {
            String[] projections = {
                    DataBaseContract.Conversations.CONVERSATION_TYPE
            };
            String selection = DataBaseContract.Conversations.CONVERSATION_ID + " LIKE ?";
            String[] selectionArgs = {conversationID};
            Cursor cursor = db.query(DataBaseContract.Conversations.CONVERSATIONS_TABLE, projections, selection, selectionArgs, null, null, null);
            while (cursor.moveToNext())
            {
                String conversationType = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.CONVERSATION_TYPE));
                type = ConversationType.values()[Integer.parseInt(conversationType)];
            }
            cursor.close();
        }
        return type;
    }
    @Deprecated
    public void createNewConversation(Message message) {
        if (db != null) {
            ContentValues values = new ContentValues();
            values.put(DataBaseContract.Conversations.CONVERSATION_ID, message.getConversationID());
            values.put(DataBaseContract.Conversations.LAST_MESSAGE_ID, message.getMessageID());
            values.put(DataBaseContract.Conversations.LAST_MESSAGE, message.getMessage());
            values.put(DataBaseContract.Conversations.LAST_MESSAGE_TYPE, message.getMessageType());
            values.put(DataBaseContract.Conversations.LAST_MESSAGE_TIME, message.getSendingTime());
            values.put(DataBaseContract.Conversations.GROUP_NAME, message.getGroupName());
            values.put(DataBaseContract.Conversations.MUTED, false);
            long newConversationID = db.insert(DataBaseContract.Conversations.CONVERSATIONS_TABLE, null, values);
            if (newConversationID == -1)
                Log.e(DataBaseError, "inserted more than 1 row");
            printConversationTable();
            if (!message.getRecipients().contains(currentUserUID))
            {
                createNewGroup(message.getConversationID(),message.getRecipients());
            }
            else
            {
                List<String> recipients = message.getRecipients();
                for (String s : recipients)
                {
                    if (s.equals(currentUserUID))
                    {
                        recipients.remove(s);
                        break;
                    }
                }
                recipients.add(message.getSender());
                createNewGroup(message.getConversationID(),recipients);
            }
        }
    }

    private synchronized void createNewGroup(String conversationID,List<String>recipients)
    {
        ContentValues groupValues = new ContentValues();
        for (int i = 0;i< recipients.size();i++)//its a hashMap
        {
            groupValues.put(DataBaseContract.Conversations.CONVERSATION_ID,conversationID);
            groupValues.put(DataBaseContract.User.USER_UID,recipients.get(i));
            long code = db.insert(DataBaseContract.Group.GroupTable,null,groupValues);
            if(code == -1)
                Log.e(DataBaseError,"new group wasn't created");
        }
        printGroupTable();
    }

    private void printConversationTable()
    {
        println("conversation table data");
        Cursor cursor = db.query(DataBaseContract.Conversations.CONVERSATIONS_TABLE,null,null,null,null,null,null);
        while (cursor.moveToNext())
        {
            for(int i = 0;i < cursor.getColumnCount();i++)
            {
                println(cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.GROUP_NAME)));
            }
        }
        cursor.close();
    }

    private void printGroupTable()
    {
        println("group table data");
        String[] projections = {
                DataBaseContract.Conversations.CONVERSATION_ID,
                DataBaseContract.User.USER_UID
        };
        Cursor cursor = db.query(DataBaseContract.Group.GroupTable, projections, null, null, null, null, null);
        while(cursor.moveToNext())
        {
            println(cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.CONVERSATION_ID)));
            println(cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_UID)));
        }
        cursor.close();
    }

    public synchronized String loadConversationName(String conversationID)
    {
        String[]projections = {
          DataBaseContract.Conversations.GROUP_NAME
        };
        String selection = DataBaseContract.Conversations.CONVERSATION_ID + " LIKE ?";
        String[] selectionArgs = {conversationID};
        Cursor cursor = db.query(DataBaseContract.Conversations.CONVERSATIONS_TABLE,projections,selection,selectionArgs,null,null,null);
        cursor.moveToNext();
        String gName =  cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.GROUP_NAME));
        cursor.close();
        return gName;
    }

    public List<Message> loadMessages(@NonNull final String id, @NonNull final String selection) {
        List<Message> messages = new ArrayList<>();
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
                    DataBaseContract.Messages.MESSAGE_STAR,
                    DataBaseContract.Messages.MESSAGE_RECORDING_PATH,
                    DataBaseContract.Messages.TYPE,
                    DataBaseContract.Messages.MESSAGE_IMAGE_PATH,
                    DataBaseContract.Messages.QUOTE_ID,
                    DataBaseContract.Messages.QUOTE,
                    DataBaseContract.Conversations.GROUP_NAME
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
                //String recipientName = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_RECIPIENT_NAME));
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_FILE_PATH));
                String address = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_ADDRESS));
                String longitude = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_LONGITUDE));
                String latitude = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_LATITUDE));
                String link = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_LINK));
                String star = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_STAR));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.STATUS));
                //String recipient = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.RECIPIENT));
                String recordingPath = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_RECORDING_PATH));
                int type = cursor.getInt(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.TYPE));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_IMAGE_PATH));
                String quote = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.QUOTE));
                String quoteID = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.QUOTE_ID));
                String group = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.GROUP_NAME));
                Message message = new Message();
                message.setMessageID(messageID);
                message.setMessage(content);
                message.setConversationID(conversationID);
                message.setMessageTime(timeSent);
                message.setSender(senderUID);
                message.setArrivingTime(timeDelivered);
                message.setSenderName(senderName);
                message.setGroupName(group);
                message.setFilePath(filePath);
                message.setLocationAddress(address);
                message.setLongitude(longitude);
                message.setLatitude(latitude);
                if (link != null)
                    message.setMessage(link);
                if (star != null)
                    message.setStar(star.equals("1"));
                message.setMessageStatus(status);
                message.setRecordingPath(recordingPath);
                message.setImagePath(imagePath);
                message.setMessageType(type);
                message.setQuoteMessage(quote);
                message.setQuotedMessageID(quoteID);
                messages.add(message);
            }
            cursor.close();
            return messages;
        }
        return null;
    }

    public void updateUserToken(String uid, String token) {
        ContentValues values = new ContentValues();
        values.put(DataBaseContract.User.TOKEN, token);
        String where = DataBaseContract.User.USER_UID + " LIKE ?";
        String[] whereArgs = {uid};
        int count = db.update(DataBaseContract.User.USER_TABLE, values, where, whereArgs);
        if (count != 1)
            Log.e(DataBaseError, "updated more than 1 token: count:" + count);
    }

    //called only if user doesn't exists - the first lunch of the app
    public synchronized void insertUser(User user) {
        if (user.getUserUID().equals(currentUserUID))
            this.user = user;
        ContentValues values = createUserValues(user);
        long rowID = db.insert(DataBaseContract.User.USER_TABLE, null, values);
        //printUserTable(user.getUserUID());
        if (rowID == -1)
            Log.e(DataBaseError, "error inserting user to database");
    }

    //on each login, the user table is updated with the current login user
    public synchronized void updateUser(User user) {
        ContentValues values = createUserValues(user);
        String where = DataBaseContract.User.USER_UID + " LIKE ?";
        String[] whereArgs = {user.getUserUID()};
        int count = db.update(DataBaseContract.User.USER_TABLE, values, where, whereArgs);
        if (count != 1)
            Log.e(DataBaseError, "more than 1 or 0 rows were updated in the user table");
    }

    private ContentValues createUserValues(User user) {
        ContentValues values = new ContentValues();
        values.put(DataBaseContract.User.USER_UID, user.getUserUID());
        values.put(DataBaseContract.User.USER_NAME, user.getName());
        values.put(DataBaseContract.User.USER_LAST_NAME, user.getLastName());
        values.put(DataBaseContract.User.USER_TIME_CREATED, user.getTimeCreated());
        values.put(DataBaseContract.User.USER_PICTURE_LINK, user.getPictureLink());
        if (user.getPhoneNumber() != null)
            values.put(DataBaseContract.User.USER_PHONE_NUMBER, user.getPhoneNumber());
        values.put(DataBaseContract.User.USER_LAST_STATUS, user.getStatus());
        values.put(DataBaseContract.User.TOKEN, user.getToken());
        values.put(DataBaseContract.User.BLOCKED, "");
        return values;
    }

    public synchronized void checkIfUserExist(User user) {
        if (db != null) {
            String[] projections = {
                    DataBaseContract.User._ID,
                    DataBaseContract.User.USER_UID
            };
            String selection = DataBaseContract.User.USER_UID + " LIKE ?";
            String[] selectionArgs = {user.getUserUID()};
            Cursor cursor = db.query(DataBaseContract.User.USER_TABLE, projections, selection, selectionArgs, null, null, null);
            if (cursor.getCount() > 1)
                Log.e(DataBaseError, "cursor contains more than 1 user entry for user: " + user.getUserUID());
            else if (cursor.moveToNext())
                updateUser(user);
            else
                insertUser(user);
            cursor.close();
        }
    }

    public void resetDB() {
        db.execSQL("DROP TABLE IF EXISTS " + DataBaseContract.Conversations.CONVERSATIONS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DataBaseContract.Messages.MESSAGES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DataBaseContract.User.USER_TABLE);
        //dbHelper.onUpgrade(db, db.getVersion(), db.getVersion() + 1);
        dbHelper.onCreate(db);
    }

    public synchronized boolean checkIfExist(String ID, boolean idType) {
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


    public synchronized void updateMessageMetaData(String messageID, String messageStatus, String readAt) {

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

    public synchronized boolean checkIfExistsInDataBase(Message message) {
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

    public synchronized boolean blockConversation(String conversationID)
    {
        if(db!=null)
        {
            String selection = DataBaseContract.Conversations.CONVERSATION_ID + " LIKE ?";
            String[] selectionArgs = {conversationID};
            ContentValues values = new ContentValues();
            boolean blocked;
            if ((blocked = isConversationBlocked(conversationID)))
            {
                values.put(DataBaseContract.Conversations.BLOCKED,"false");
            }
            else
            {
                values.put(DataBaseContract.Conversations.BLOCKED,"true");
            }
            db.update(DataBaseContract.Conversations.CONVERSATIONS_TABLE,values,selection,selectionArgs);
            return blocked;
        }
        return false;
    }

    public synchronized boolean isConversationBlocked(String conversationID)
    {
        String selection = DataBaseContract.Conversations.CONVERSATION_ID + " LIKE ?";
        String[] selectionArgs = {conversationID};
        String[] projections = {
                DataBaseContract.Conversations.BLOCKED
        };
        Cursor cursor = db.query(DataBaseContract.Conversations.CONVERSATIONS_TABLE, projections, selection, selectionArgs, null, null, null);
        cursor.moveToNext();
        String blocked = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.BLOCKED));
        cursor.close();
        return blocked.equals("true");
    }


    public synchronized boolean blockUser(String userUID) {
        if (db != null) {
            String selection = DataBaseContract.User.USER_UID + " LIKE ?";
            String[] selectionArgs = {userUID};
            ContentValues values = new ContentValues();
            boolean blocked = false;
            if (isBlocked(userUID)) {
                //unblocks the user
                values.put(DataBaseContract.User.BLOCKED, "");
            } else {
                //blocks the user
                values.put(DataBaseContract.User.BLOCKED, "blocked");
                blocked = true;
            }
            db.update(DataBaseContract.User.USER_TABLE, values, selection, selectionArgs);
            return blocked;
        }
        return false;
    }

    public synchronized boolean isBlocked(String userUID) {
        if (db != null) {
            String selection = DataBaseContract.User.USER_UID + " LIKE ?";
            String[] selectionArgs = {userUID};
            String[] projections = {
                    DataBaseContract.User._ID,
                    DataBaseContract.User.BLOCKED,
                    DataBaseContract.User.USER_UID
            };
            Cursor cursor = db.query(DataBaseContract.User.USER_TABLE, projections, selection, selectionArgs, null, null, null);
            while (cursor.moveToNext()) {
                String blocked = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.BLOCKED));
                String uid = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_UID));
                if (uid.equals(userUID)) {
                    cursor.close();
                    if (blocked == null)
                        return false;
                    return blocked.equals("blocked");
                }
            }
            cursor.close();
        }
        return false;
    }

    //mutes/un mutes users in db
    public synchronized boolean muteUser(String userID)
    {
        boolean muted = false;
        if (db!=null)
        {
            String selection = DataBaseContract.User.USER_TABLE + " LIKE ?";
            String[] selectionArgs = {userID};
            ContentValues values = new ContentValues();
            if (isUserMuted(userID))
            {
                values.put(DataBaseContract.User.USER_TABLE,"");
            }
            else
            {
                values.put(DataBaseContract.User.USER_TABLE,"muted");
                muted = true;
            }
            db.update(DataBaseContract.User.USER_TABLE,values,selection,selectionArgs);
        }
        return muted;
    }

    //reads if the user is muted
    public synchronized boolean isUserMuted(String userID)
    {
        String muted = "";
        if (db!=null)
        {
            String selection = DataBaseContract.User.USER_UID + " LIKE ?";
            String[] selectionArgs = {userID};
            String[] projections = {
                    DataBaseContract.Conversations.MUTED
            };
            Cursor cursor = db.query(DataBaseContract.User.USER_TABLE,projections,selection,selectionArgs,null,null,null);
            if (cursor.getCount() > 1)
                Log.e(DataBaseError, "isUserMuted: got more than 1 answer for mute query" );
            cursor.moveToNext();
            muted = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.MUTED));
            cursor.close();
        }
        return muted.equals("muted");
    }

    public synchronized boolean muteConversation(String conversationID) {
        if (db != null) {
            String selection = DataBaseContract.Conversations.CONVERSATION_ID + " LIKE ?";
            String[] selectionArgs = {conversationID};
            ContentValues values = new ContentValues();
            boolean muted = false;
            if (isMuted(conversationID)) {
                //unMutes the user
                values.put(DataBaseContract.Conversations.MUTED, "");
            } else {
                //mutes the user
                values.put(DataBaseContract.Conversations.MUTED, "muted");
                muted = true;
            }
            db.update(DataBaseContract.Conversations.CONVERSATIONS_TABLE, values, selection, selectionArgs);
            return muted;
        }
        return false;
    }

    public synchronized boolean isMuted(String conversationID) {
        if (db != null) {
            String selection = DataBaseContract.Conversations.CONVERSATION_ID + " LIKE ?";
            String[] selectionArgs = {conversationID};
            String[] projections = {
                    DataBaseContract.Conversations._ID,
                    DataBaseContract.Conversations.CONVERSATION_ID,
                    DataBaseContract.Conversations.MUTED
            };
            Cursor cursor = db.query(DataBaseContract.Conversations.CONVERSATIONS_TABLE, projections, selection, selectionArgs, null, null, null);
            while (cursor.moveToNext()) {
                String muted = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.MUTED));
                String uid = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.CONVERSATION_ID));
                if (uid.equals(conversationID)) {
                    cursor.close();
                    return muted.equals("muted");
                }
            }
            cursor.close();
        }
        return false;
    }

    public List<Conversation> getAllMutedOrBlockedConversation(String selection,String[] selectionArgs)
    {
        List<Conversation>conversations = new ArrayList<>();
        if (db!=null)
        {
            String[] projection = {
                    DataBaseContract.Conversations.CONVERSATION_ID,
                    DataBaseContract.Conversations.CONVERSATION_TYPE,
                    DataBaseContract.Conversations.LAST_MESSAGE,
                    DataBaseContract.Conversations.RECIPIENT,
                    DataBaseContract.Conversations.GROUP_NAME,
                    DataBaseContract.Conversations.RECIPIENT_NAME
            };
            //String selection = DataBaseContract.Conversations.MUTED + " LIKE ?";
            //String[] selectionArgs = {"muted"};
            Cursor cursor = db.query(DataBaseContract.Conversations.CONVERSATIONS_TABLE,projection,selection,selectionArgs,null,null,null);
            while (cursor.moveToNext())
            {
                Conversation conversation = new Conversation(cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.CONVERSATION_ID)));
                conversation.setMuted(true);
                conversation.setLastMessage(cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.LAST_MESSAGE)));
                conversation.setRecipient(cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.RECIPIENT)));
                conversation.setRecipientName(cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.RECIPIENT_NAME)));
                conversation.setGroupName(cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.GROUP_NAME)));
                String conversationType = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.CONVERSATION_TYPE));
                if (Integer.parseInt(conversationType) == ConversationType.single.ordinal())
                    conversation.setConversationType(ConversationType.single);
                else if (Integer.parseInt(conversationType) == ConversationType.group.ordinal())
                    conversation.setConversationType(ConversationType.group);
                else if (Integer.parseInt(conversationType) == ConversationType.sms.ordinal())
                    conversation.setConversationType(ConversationType.sms);
                conversations.add(conversation);
            }
            cursor.close();
        }
        return conversations;
    }
}
