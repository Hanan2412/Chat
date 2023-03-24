package Backend;

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import Consts.ConversationType;
import NormalObjects.Conversation;
import NormalObjects.Group;
import NormalObjects.Message;
import NormalObjects.MessageHistory;
import NormalObjects.User;

@Dao
public interface ChatDao {

    @Insert
    void insertNewConversation(Conversation conversation);

    @Insert
    void insertNewUser(User user);

    @Insert
    void insertNewMessage(Message message);

    @Insert
    void insertNewGroup(Group group);

    @Update
    void updateConversation(Conversation conversation);

    @Update
    void updateUser(User user);

    @Update
    void updateMessage(Message message);

    @Delete
    void deleteConversation(Conversation conversation);

    @Delete
    void deleteUser(User user);

    @Query("DELETE FROM groups WHERE conversationID = :conversationID")
    void deleteGroup(String conversationID);

    @Query("DELETE FROM groups WHERE conversationID = :conversationID AND uid = :uid")
    void removeMemberFromGroup(String uid,String conversationID);

    @Query("DELETE FROM users WHERE userUID = :uid")
    void deleteUser(String uid);

    @Delete
    void deleteMessage(Message message);

    @Query("DELETE FROM conversations")
    void clearConversationsTable();

    @Query("DELETE FROM messages")
    void clearMessagesTable();

    @Query("DELETE FROM users")
    void clearUsersTable();

    @Query("DELETE FROM messages where messages.messageID = :messageID")
    void deleteMessage(long messageID);

    @Query("SELECT * FROM groups")
    LiveData<List<Group>>getAllGroups();

    @Query("SELECT * FROM messages")
    LiveData<List<Message>>getAllMessages();

    @Query("DELETE FROM groups")
    void clearGroups();

    @Query("SELECT * FROM conversations ORDER BY lastMessageID DESC LIMIT 1")
    LiveData<Conversation>getNewOrUpdatedConversation();

    @Query("SELECT * FROM conversations WHERE lastUpdate = (SELECT MAX(lastUpdate) FROM conversations)")
    LiveData<Conversation>getLastUpdateConversation();

    @Query("DELETE FROM messages WHERE conversationID = :conversationID")
    void deleteMessages(String conversationID);

    @Query("SELECT * FROM conversations ORDER BY pinned DESC,lastMessageID DESC")
    LiveData<List<Conversation>>getAllConversations();

    @Query("SELECT * FROM users")
    LiveData<List<User>>getAllUsers();

    @Query("SELECT * FROM messages where messages.conversationID = :conversationID")
    LiveData<List<Message>>getAllMessages(String conversationID);

    @Query("SELECT * FROM users where users.userUID = :uid")
    LiveData<User>getUser(String uid);

    @Query("SELECT * FROM conversations where conversations.conversationID = :conversationID")
    LiveData<Conversation>isConversationExist(String conversationID);

    @Query("UPDATE messages SET messageStatus = :status WHERE messageID = :id")
    void updateMessageStatus(long id,int status);

    @Query("UPDATE messages SET content = :content, editTime = :time WHERE messageID = :messageID")
    void updateEditMessage(long messageID,String content,String time);

    @Query("SELECT * FROM conversations WHERE conversationID = :conversationID")
    LiveData<Conversation> getConversation(String conversationID);

    @Query("SELECT * FROM conversations WHERE muted = 1")
    LiveData<List<Conversation>>getAllMutedConversations();

    @Query("SELECT * FROM conversations WHERE blocked = 1")
    LiveData<List<Conversation>>getAllBlockedConversations();

    @Query("SELECT * FROM users WHERE muted = 1")
    LiveData<List<User>>getAllMutedUsers();

    @Query("SELECT * FROM users WHERE blocked = 1")
    LiveData<List<User>>getAllBlockedUsers();

    @Query("SELECT userUID FROM users where phoneNumber = :phoneNumber")
    LiveData<String>getUIDbyPhoneNumber(String phoneNumber);

    @Query("SELECT conversationID FROM groups JOIN users ON uid = userUID WHERE phoneNumber = :phone AND conversationID LIKE 's%'")
    LiveData<String>getConversationIDByPhone(String phone);

    @Query("SELECT conversationID FROM conversations where recipientPhoneNumber = :phone and conversationType = 2")
    LiveData<String>getConversationIdByPhone(String phone);

    @Query("SELECT * FROM users WHERE userUID IN (SELECT uid From groups where groups.conversationID = :conversationID)")
    LiveData<List<User>>getRecipients(String conversationID);

    @Query("select * from groups where groups.conversationID = :conversationID")
    LiveData<List<Group>>getGroup(String conversationID);

    @Query("SELECT uid FROM groups WHERE conversationID = :conversationID")
    LiveData<List<String>>getUidFromGroup(String conversationID);

    @Query("SELECT * FROM messages WHERE senderID != :currentUser and conversationID = :conversationID ORDER BY sendingTime desc limit 1")
    LiveData<Message>getNewMessage(String currentUser,String conversationID);

    @Query("UPDATE conversations SET lastMessage = :message WHERE conversationID = :conversationID")
    void updateConversationLastMessage(String conversationID,String message);

    @Query("UPDATE conversations SET lastMessage = :message, lastMessageTime = :lastMessageTime WHERE conversationID = :conversationID")
    void updateConversationLastMessage(String conversationID, String message, String lastMessageTime);

    @Query("UPDATE conversations SET lastMessage = :message, lastMessageID = :lastMessageID WHERE conversationID = :conversationID")
    void updateConversationLastMessage(String conversationID, String message, long lastMessageID);

    @Query("SELECT * FROM conversations WHERE pinned = 1")
    LiveData<List<Conversation>>getPinnedConversations();

    @Query("UPDATE conversations SET lastMessage = :message, lastMessageID = :id, messageType = :type, lastMessageTime = :time, conversationName = :groupName WHERE conversationID = :conversationID")
    void updateConversation(String message,long id,int type,long time,String groupName,String conversationID);

    @Query("SELECT EXISTS (SELECT * FROM conversations WHERE conversationID = :conversationID)")
    LiveData<Boolean> isConversationExists(String conversationID);

    @Query("SELECT EXISTS (SELECT * FROM conversations WHERE recipientPhoneNumber = :phone and conversationType = 2)")
    LiveData<Boolean> isConversationExistsByPhone(String phone);

    @Query("SELECT blocked from users where userUID = :id")
    Boolean isUserBlocked1(String id);

    @Query("SELECT blocked from conversations where conversationID = :conversationID")
    Boolean isConversationBlocked1(String conversationID);

    @Query("UPDATE messages SET content = :content,arrivingTime = :time WHERE messageID = :id")
    void updateMessage(long id, String content,String time);

    @Query("DELETE FROM conversations WHERE conversationID = :conversationID")
    void deleteConversation(String conversationID);

    @Query("SELECT type From conversations WHERE conversationID = :conversationID")
    LiveData<Integer>getConversationType(String conversationID);

    @Query("SELECT conversationName FROM conversations WHERE conversationID = :conversationID")
    LiveData<String> getConversationName(String conversationID);

    @Query("UPDATE users SET token = :token WHERE userUID = :uid")
    void updateUserToken(String uid,String token);

    @Query("SELECT EXISTS (SELECT * FROM users WHERE userUID = :uid)")
    LiveData<Boolean> isUserExists(String uid);

    @Query("UPDATE messages SET readingTime = :readAt,messageStatus=:status WHERE messageID = :id")
    void updateMessageMetaData(String id,String status, String readAt);

    @Query("SELECT EXISTS (SELECT * FROM messages WHERE messageID = :messageID)")
    LiveData<Boolean> isMessageExists(long messageID);

    @Query("UPDATE conversations SET blocked = 1 WHERE conversationID = :conversationID")
    void blockConversation(String conversationID);

    @Query("UPDATE conversations SET blocked = 0 WHERE conversationID = :conversationID")
    void unBlockConversation(String conversationID);

    @Query("SELECT blocked FROM conversations WHERE conversationID = :conversationID")
    LiveData<Boolean> isConversationBlocked(String conversationID);

    @Query("UPDATE users SET blocked = 1 WHERE userUID = :uid")
    void blockUser(String uid);

    @Query("SELECT blocked FROM users WHERE userUID = :uid")
    LiveData<Boolean> isUserBlocked(String uid);

    @Query("UPDATE users SET muted = 1 WHERE userUID = :uid")
    void muteUser(String uid);

    @Query("SELECT muted FROM users WHERE userUID = :uid")
    LiveData<Boolean> isUserMuted(String uid);

    @Query("UPDATE conversations SET muted = 1 WHERE conversationID = :conversationID")
    void muteConversation(String conversationID);

    @Query("UPDATE conversations SET muted = 0 WHERE conversationID = :conversationID")
    void unMuteConversation(String conversationID);

    @Query("SELECT muted FROM conversations WHERE conversationID = :conversationID")
    LiveData<Boolean>  isConversationMuted(String conversationID);

    @Query("UPDATE users SET blocked = 0 WHERE userUID = :uid")
    void unBlockUser(String uid);

    @Query("UPDATE users SET muted = 0 WHERE userUID = :uid")
    void unMuteUser(String uid);

   /* @Query("SELECT muted,blocked FROM conversations WHERE ConversationID = :conversationID")
    LiveData<ConversationNotify> mutedOrBlockedConversation(String conversationID);

    @Query("SELECT muted,blocked FROM users WHERE userUID = :uid")
    LiveData<List<Boolean>> mutedOrBlockedUser(String uid);*/

    @Query("SELECT users.muted, users.blocked,conversations.muted,conversations.blocked FROM conversations,users WHERE userUID = :uid and conversationID = :conversationID")
    Cursor conversationUserCombo(String uid, String conversationID);

    @Query("SELECT count(conversations.unreadMessages) from conversations where unreadMessages != 0")
    LiveData<String>getUnreadConversationsCount();

    @Query("SELECT * FROM messageHistory WHERE messageID = :messageID")
    LiveData<List<MessageHistory>>getMessageHistory(long messageID);

    @Query("SELECT * FROM messages WHERE messageID = :messageID")
    LiveData<Message>getMessage(long messageID);
    @Insert
    void saveMessageHistory(MessageHistory messageHistory);

    @Query("SELECT * FROM messages WHERE messageType in (4,5,10)")
    LiveData<List<Message>>mediaMessage();
}
