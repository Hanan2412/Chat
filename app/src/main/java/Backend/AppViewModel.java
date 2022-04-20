package Backend;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import Consts.ConversationType;
import NormalObjects.Conversation;
import NormalObjects.Group;
import NormalObjects.Message;
import NormalObjects.User;

public class AppViewModel extends AndroidViewModel {
    private final String currentUserUID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    private Repository repository;
    private LiveData<List<Conversation>>conversations;
    private LiveData<User>currentUser;
    private LiveData<List<User>>recipients;
    public AppViewModel(@NonNull Application application) {
        super(application);
        repository = new Repository(application);
        conversations = repository.getGetAllConversations();
        currentUser = repository.getUserByID(currentUserUID);
    }

    public void updateConversation(Conversation conversation){repository.updateConversation(conversation);}

    public LiveData<List<Conversation>>getConversations(){
        return conversations;
    }

    public LiveData<String> getConversationIDByUserPhone(String phoneNumber)
    {
        return repository.getConversationByUserPhone(phoneNumber);
    }

    public LiveData<List<User>>getRecipients(String conversationID){
        if (recipients==null)
            recipients = repository.getRecipients(conversationID);
        return recipients;
    }

    public LiveData<Conversation>getNewConversation(String conversationID)
    {
        return repository.getNewConversation(conversationID);
    }

    public void updateConversationLastMessage(String conversationID,String message)
    {
        repository.updateConversationLastMessage(conversationID,message);
    }

    public void updateConversation(Message message)
    {
        repository.updateConversation(message);
    }

    /*public LiveData<Boolean> isConversationExists(String conversationID)
    {
        return repository.isConversationExists(conversationID);
    }*/

    public LiveData<User>loadUserByID(String userUID)
    {
        return repository.getUserByID(userUID);
    }

    public void updateMessageStatus(String messageID,String status)
    {
        repository.updateMessageStatus(messageID,status);
    }

    public void updateMessage(String id, String content,String time)
    {
        repository.updateMessage(id,content,time);
    }

    public void updateMessage(Message message)
    {
        repository.updateMessage(message);
    }

    public void deleteMessage(String messageID)
    {
        repository.deleteMessage(messageID);
    }

    public void deleteConversation(String conversationID)
    {
        repository.deleteConversation(conversationID);
    }

    public void saveMessage(Message message)
    {
        repository.insertNewMessage(message);
    }

    public void createNewConversation(Message message, String currentUser, ConversationType type)
    {
        Conversation conversation = createConversation(message,type);
        if (message.getRecipients().size() == 1) {
            List<String>recipients = new ArrayList<>();
            if (!message.getRecipients().get(0).equals(currentUser))
            {
                conversation.setRecipient(message.getRecipients().get(0));
                recipients = message.getRecipients();
            }
            else
            {
                conversation.setRecipient(message.getSender());
                recipients.add(message.getSender());
            }
            createNewGroup(message.getConversationID(), recipients);
        }
        else
        {
            if (message.getRecipients().contains(currentUser)) {
                message.getRecipients().remove(currentUser);
                message.getRecipients().add(message.getSender());
            }
            createNewGroup(message.getConversationID(), message.getRecipients());
        }
        repository.insertNewConversation(conversation);
    }

    public LiveData<Integer>loadConversationType(String conversationID)
    {
        return repository.getConversationType(conversationID);
    }


    private void createNewGroup(String conversationID,List<String>recipients)
    {
        for (String uid:recipients){
            Group group = new Group(conversationID,uid);
            repository.insertNewGroup(group);
        }
    }

    public LiveData<String>loadConversationName(String conversationID)
    {
        return repository.getConversationName(conversationID);
    }

    public LiveData<List<Message>>loadMessages(String conversationID)
    {
        return repository.getAllMessageForConversation(conversationID);
    }

    public void updateToken(String uid,String token)
    {
        repository.updateUserToken(uid,token);
    }

    public void insertUser(User user)
    {
        repository.insertNewUser(user);
    }

    public void updateUser(User user)
    {
        repository.updateUser(user);
    }

    public LiveData<Boolean> checkIfUserExists(User user)
    {
        return repository.isUserExists(user);
    }

    public void updateMessageMetaData(String id,String status,String readAt)
    {
        repository.updateMessageMetaData(id,status,readAt);
    }

    public LiveData<Boolean> checkIfMessageExists(Message message)
    {
        return repository.isMessageExists(message.getMessageID());
    }

    public LiveData<Boolean> blockConversation(String conversationID)
    {
        repository.blockConversation(conversationID);
        return repository.isConversationBlocked(conversationID);
    }

    public LiveData<Boolean> isConversationBlocked(String conversationID)
    {
        return repository.isConversationBlocked(conversationID);
    }

    public LiveData<Boolean> blockUser(String userID)
    {
        repository.blockUser(userID);
        return repository.isUserBlocked(userID);
    }

    public LiveData<Boolean> isUserBlocked(String uid)
    {
        return repository.isUserBlocked(uid);
    }

    public LiveData<Boolean> muteUser(String uid)
    {
        repository.muteUser(uid);
        return repository.isUserBlocked(uid);
    }

    public LiveData<Boolean> isUserMuted(String uid)
    {
        return repository.isUserMuted(uid);
    }

    public LiveData<Boolean> muteConversation(String conversationID)
    {
        repository.muteConversation(conversationID);
        return repository.isConversationMuted(conversationID);
    }

    public LiveData<Boolean> isConversationMuted(String conversationID)
    {
        return repository.isConversationMuted(conversationID);
    }

    public LiveData<List<Conversation>>getAllMutedOrBlockedConversations(boolean blocked)
    {
        return repository.getAllMutedOrBlockedConversations(blocked);
    }

    public LiveData<List<User>>getAllMutedOrBlockedUsers(boolean blocked)
    {
        return repository.getAllMutedOrBlockedUsers(blocked);
    }

    public void updateConversation(Message message,ConversationType type)
    {
        Conversation conversation = createConversation(message,type);
        repository.updateConversation(conversation);
    }

    private Conversation createConversation(Message message,ConversationType type)
    {
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

    public LiveData<User>getCurrentUser(){return currentUser;}
}
