package Backend;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

import Consts.ConversationType;
import NormalObjects.Conversation;
import NormalObjects.Group;
import NormalObjects.Message;
import NormalObjects.User;
import Retrofit.Server;

public class ConversationVM extends AndroidViewModel {

    private Repository repository;
    private LiveData<List<Conversation>>conversations;

    public ConversationVM(@NonNull Application application) {
        super(application);
        repository = new Repository(application);
        conversations = repository.getGetAllConversations();
    }

    public void deleteConversation(String conversationID)
    {
        repository.deleteConversation(conversationID);
        repository.deleteMessages(conversationID);
        repository.deleteGroup(conversationID);
    }

    public LiveData<List<User>> getRecipients(String conversationID) {
        return repository.getRecipients(conversationID);
    }
    public LiveData<List<Group>>getGroups(String conversationID)
    {
        return repository.getGroups(conversationID);
    }
    public LiveData<List<Conversation>>getConversations(){
        return conversations;
    }

    public LiveData<Conversation>getNewConversation(String conversationID)
    {
        return repository.getNewConversation(conversationID);
    }

    public LiveData<Conversation>getNewOrUpdatedConversation()
    {
        return repository.getNewOrUpdatedConversation();
    }

    public void updateConversation(Conversation conversation){repository.updateConversation(conversation);}

    public void updateConversationLastMessage(String conversationID, String message) {
        repository.updateConversationLastMessage(conversationID, message);
    }

    public void updateConversation(Message message) {
        repository.updateConversation(message);
    }

    public void updateMessageStatus(String messageID, String status) {
        repository.updateMessageStatus(messageID, status);
    }

    public void updateMessage(String id, String content, String time) {
        repository.updateMessage(id, content, time);
    }

    public void updateMessage(Message message) {
        repository.updateMessage(message);
    }

    public void deleteMessage(String messageID) {
        repository.deleteMessage(messageID);
    }

    public void saveMessage(Message message) {
        repository.insertNewMessage(message);
    }

    public LiveData<Conversation> loadConversation(String conversationID) {
        return repository.getNewConversation(conversationID);
    }

    public LiveData<List<Message>> loadMessages(String conversationID) {
        return repository.getAllMessageForConversation(conversationID);
    }

    public LiveData<Message>getNewMessage(String currentUser,String conversationID)
    {
        return repository.getNewMessage(currentUser,conversationID);
    }
    public void updateToken(String uid, String token) {
        repository.updateUserToken(uid, token);
    }

    public void updateMessageMetaData(String id, String status, String readAt) {
        repository.updateMessageMetaData(id, status, readAt);
    }

    public LiveData<Boolean> checkIfMessageExists(Message message) {
        return repository.isMessageExists(message.getMessageID());
    }

    public void blockConversation(String conversationID) {
        repository.blockConversation(conversationID);
    }

    public void unBlockConversation(String conversationID)
    {
        repository.unBlockConversation(conversationID);
    }

    public LiveData<Boolean> isConversationBlocked(String conversationID) {
        return repository.isConversationBlocked(conversationID);
    }

    public LiveData<Boolean> blockUser(String userID) {
        repository.blockUser(userID);
        return repository.isUserBlocked(userID);
    }

    public LiveData<Boolean> isUserBlocked(String uid) {
        return repository.isUserBlocked(uid);
    }

    public LiveData<Boolean> muteUser(String uid) {
        repository.muteUser(uid);
        return repository.isUserBlocked(uid);
    }

    public LiveData<Boolean> isUserMuted(String uid) {
        return repository.isUserMuted(uid);
    }

   /* public LiveData<Boolean> muteConversation(String conversationID) {
        repository.muteConversation(conversationID);
        return repository.isConversationMuted(conversationID);
    }*/

    public void muteConversation(String conversationID)
    {
        repository.muteConversation(conversationID);
    }

    public void unMuteConversation(String conversationID)
    {
        repository.unMuteConversation(conversationID);
    }

    public LiveData<Boolean> isConversationMuted(String conversationID) {
        return repository.isConversationMuted(conversationID);
    }

    public LiveData<List<Conversation>>getAllMutedOrBlockedConversations(boolean blocked)
    {
        return repository.getAllMutedOrBlockedConversations(blocked);
    }

    public void updateConversation(Message message, ConversationType type) {

    }

    public void createNewConversation(Message message, String currentUser, ConversationType type) {
        if (message.getRecipients().size() > 1 && type == ConversationType.single)
            Log.e("DB ERROR","conversation type and recipient amount mismatch");
        Conversation conversation = createConversation(message, type);
        if (message.getRecipients().size() == 1) {
            List<String> recipients = new ArrayList<>();
            if (!message.getRecipients().get(0).equals(currentUser)) {
                conversation.setRecipient(message.getRecipients().get(0));
                recipients = message.getRecipients();
            } else {
                conversation.setRecipient(message.getSender());
            recipients.add(message.getSender());
            }
            createNewGroup(message.getConversationID(), recipients);
        } else {
            if (message.getRecipients().contains(currentUser)) {
                message.getRecipients().remove(currentUser);
                message.getRecipients().add(message.getSender());
            }
            createNewGroup(message.getConversationID(), message.getRecipients());
        }
        repository.insertNewConversation(conversation);
    }


    private Conversation createConversation(Message message, ConversationType type) {
        Conversation conversation = new Conversation(message.getConversationID());
        conversation.setLastMessageID(message.getMessageID());
        conversation.setLastMessage(message.getMessage());
        conversation.setMessageType(message.getMessageType());
        conversation.setLastMessageTime(message.getSendingTime());
        conversation.setGroupName(message.getGroupName());
        conversation.setMuted(false);
        conversation.setBlocked(false);
        conversation.setConversationType(type);
        return conversation;
    }

    public void createNewGroup(String conversationID, List<String> recipients) {
        for (String uid : recipients) {
            Group group = new Group(conversationID, uid);
            repository.insertNewGroup(group);
        }
    }

    public LiveData<String>findConversationByPhone(String phone)
    {
        return repository.getConversationByUserPhone(phone);
    }

    public LiveData<String>getUnreadConversationsCount()
    {
        return repository.getUnreadConversationsCount();
    }

    public void setOnFileUploadListener(Server.onFileUpload listener)
    {
        repository.setOnFileUploadListener(listener);
    }

    public void uploadFile(String uploaderID,String msgID, Bitmap bitmap, Context context)
    {
        repository.uploadFile(uploaderID,msgID, bitmap, context);
    }

    public void uploadFile(String msgID, Uri uri, Context context)
    {
        repository.uploadFile(msgID, uri, context);
    }
}
