package Backend;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import NormalObjects.Conversation;
import NormalObjects.Group;
import NormalObjects.Message;
import NormalObjects.User;
import Retrofit.Server;

@SuppressWarnings("Convert2Lambda")
public class Repository {

    private ChatDao chatDao;
    private LiveData<List<Conversation>> getAllConversations;
    private LiveData<List<Conversation>>allBlockedConversations;
    private LiveData<List<Conversation>>allMutedConversations;
    private LiveData<List<User>>allMutedUsers;
    private LiveData<List<User>>allBlockedUsers;
    private LiveData<User> getUser;
    private ExecutorService pool;
   // private Server3 server3;
    private Server server;
    public Repository(Application application)
    {
        ChatDataBase dataBase = ChatDataBase.getInstance(application);
        chatDao = dataBase.chatDao();
        getAllConversations = chatDao.getAllConversations();
        allMutedConversations = chatDao.getAllMutedConversations();
        allBlockedConversations = chatDao.getAllBlockedConversations();
        allMutedUsers = chatDao.getAllMutedUsers();
        allBlockedUsers = chatDao.getAllBlockedUsers();
        pool = Executors.newFixedThreadPool(3);
       // server3 = Server3.getInstance();
        server = Server.getInstance();
    }


    public LiveData<String>getConversationByUserPhone(String phone)
    {
        return chatDao.getConversationIDByPhone(phone);
    }

    public void unMuteUser(String uid)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.unMuteUser(uid);
            }
        };
        pool.execute(runnable);

    }

    public void unBlockUser(String uid)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.unBlockUser(uid);
            }
        };
        pool.execute(runnable);

    }

    public LiveData<List<User>> getRecipients(String conversationID)
    {
        return chatDao.getRecipients(conversationID);
    }

    public LiveData<Conversation>getNewConversation(String conversationID)
    {
        return chatDao.getConversation(conversationID);
    }

    public LiveData<Conversation>getNewOrUpdatedConversation()
    {
        return chatDao.getNewOrUpdatedConversation();
    }

    public void deleteMessages(String conversationID)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.deleteMessages(conversationID);
            }
        };
        pool.execute(runnable);
    }
    public void updateConversationLastMessage(String conversationID,String message)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.updateConversationLastMessage(conversationID,message);
            }
        };
        pool.execute(runnable);
    }

    public void updateConversation(Message message)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.updateConversation(message.getMessage(),message.getMessageID(),message.getMessageType()+"",message.getSendingTime(),message.getGroupName(),message.getConversationID());
            }
        };
        pool.execute(runnable);
    }

    /*public LiveData<Boolean> isConversationExists(String conversationID)
    {
       return chatDao.isConversationExists(conversationID);
    }*/

    public LiveData<User>loadUserByID(String userID)
    {
        return chatDao.getUser(userID);
    }

    public void updateMessageStatus(String id,String status)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.updateMessageStatus(id,status);
            }
        };
        pool.execute(runnable);
    }

    public void updateMessage(String id, String content,String time)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.updateMessage(id,content,time);
            }
        };
        pool.execute(runnable);
    }

    public void updateMessage(Message message)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.updateMessage(message);
            }
        };
        pool.execute(runnable);
    }

    public void deleteConversation(String conversationID)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.deleteConversation(conversationID);
            }
        };
        pool.execute(runnable);
    }

    public LiveData<Integer>getConversationType(String conversationID)
    {
        return chatDao.getConversationType(conversationID);
    }

    public LiveData<String> getConversationName(String conversationID)
    {
        return chatDao.getConversationName(conversationID);
    }

    public void updateUserToken(String uid,String token)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.updateUserToken(uid,token);
            }
        };
        pool.execute(runnable);
        server.saveToken(token,uid);
    }

    public LiveData<Boolean>isUserExists(User user)
    {
        return chatDao.isUserExists(user.getUserUID());
    }

    public void updateMessageMetaData(String id,String status, String readAt)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.updateMessageMetaData(id,status,readAt);
            }
        };
        pool.execute(runnable);
    }

    public LiveData<Boolean>isMessageExists(String messageID)
    {
        return chatDao.isMessageExists(messageID);
    }

    public void blockConversation(String conversationID)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.blockConversation(conversationID);
            }
        };
        pool.execute(runnable);
    }

    public void unBlockConversation(String conversationID)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.unBlockConversation(conversationID);
            }
        };
        pool.execute(runnable);
    }
    public LiveData<Boolean>isConversationBlocked(String conversationID)
    {
        return chatDao.isConversationBlocked(conversationID);
    }

    public void blockUser(String uid)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.blockUser(uid);
            }
        };
        pool.execute(runnable);
    }

    public LiveData<Boolean>isUserBlocked(String uid)
    {
        return chatDao.isUserBlocked(uid);
    }

    public void muteUser(String uid)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.muteUser(uid);
            }
        };
        pool.execute(runnable);
    }

    public LiveData<Boolean>isUserMuted(String uid)
    {
        return chatDao.isUserMuted(uid);
    }

    public void muteConversation(String conversationID)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.muteConversation(conversationID);
            }
        };
        pool.execute(runnable);
    }
    public void unMuteConversation(String conversationID)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.unMuteConversation(conversationID);
            }
        };
        pool.execute(runnable);
    }

    public LiveData<Boolean> isConversationMuted(String conversationID)
    {
        return chatDao.isConversationMuted(conversationID);
    }

    public LiveData<List<Conversation>>getAllMutedOrBlockedConversations(boolean blocked)
    {
        if (blocked)
            return allBlockedConversations;
        else
            return allMutedConversations;
    }
    public LiveData<List<User>>getAllMutedOrBlockedUsers(boolean blocked)
    {
        if (blocked)
            return allBlockedUsers;
        else
            return allMutedUsers;
    }
    public LiveData<List<Message>> getAllMessageForConversation(String conversationID)
    {
        return chatDao.getAllMessages(conversationID);
    }

    public LiveData<User> getUserByID(String uid)
    {
        getUser = chatDao.getUser(uid);
        return getUser;
    }

    public void clearAll()
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.clearGroups();
                chatDao.clearUsersTable();
                chatDao.clearMessagesTable();
                chatDao.clearConversationsTable();
            }
        };
        pool.execute(runnable);
    }

    public LiveData<Message> getNewMessage(String currentUser,String conversationID)
    {
        return chatDao.getNewMessage(currentUser,conversationID);
    }

    public void deleteGroup(String conversationID)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.deleteGroup(conversationID);
            }
        };
        pool.execute(runnable);
    }
    public void insertNewConversation(Conversation conversation)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.insertNewConversation(conversation);
            }
        };
        pool.execute(runnable);
    }

    public void insertNewMessage(Message message)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.insertNewMessage(message);
            }
        };
        pool.execute(runnable);
    }

    /**
     * inserts new user to local database
     * @param user the user to insert
     */
    public void insertNewUser(User user)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.insertNewUser(user);
            }
        };
        pool.execute(runnable);
    }

    public void insertNewGroup(Group group)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.insertNewGroup(group);
            }
        };
        pool.execute(runnable);
    }

    public void updateConversation(Conversation conversation)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.updateConversation(conversation);
            }
        };
        pool.execute(runnable);
    }





    public void updateUser(User user)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.updateUser(user);
            }
        };
        pool.execute(runnable);
    }

    public void deleteConversation(Conversation conversation)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.deleteConversation(conversation);
            }
        };
        pool.execute(runnable);
    }

    public void deleteMessage(Message message)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.deleteMessage(message);
            }
        };
        pool.execute(runnable);
    }

    public void deleteMessage(String messageID)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.deleteMessage(messageID);
            }
        };
        pool.execute(runnable);
    }
    public void deleteUser(User user)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chatDao.deleteUser(user);
            }
        };
        pool.execute(runnable);
    }

    public LiveData<List<Conversation>>getGetAllConversations()
    {
        return getAllConversations;
    }

    public void updateUserImage(User user, Bitmap userImage, Context context)
    {
        server.setFileUploadListener(new Server.onFileUpload() {
            @Override
            public void onPathReady(String path) {
                server.setFileUploadListener(null);
                String relativePath = path.split("downloadFile/")[1];
                user.setPictureLink(relativePath);
                server.updateUser(user);
                isUserExists(user).observeForever(new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        if (aBoolean!=null)
                        {
                            if (!aBoolean)
                            {
                                insertNewUser(user);
                            }
                            else
                            {
                                pool.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        chatDao.updateUser(user);
                                    }
                                });

                            }
                        }
                    }
                });

            }

            @Override
            public void onStartedUpload(String msgID) {
                Log.d("fileUpload", "started uploading user image ");
            }

            @Override
            public void onProgress(int progress) {

            }

            @Override
            public void onUploadFinished(String msgID) {
                Log.d("fileUpload", "finished uploading user image ");
            }

            @Override
            public void onUploadError(String msgID, String errorMessage) {
                Log.e("fileUpload", errorMessage );
            }
        });
        server.uploadFile(user.getUserUID(),null,userImage,context);
    }

    public void createNewUser(User user, Bitmap userImage,Context context)
    {
        String time = System.currentTimeMillis() + "";
        user.setTimeCreated(time);
        user.setLastTimeLogIn(time);
        server.createNewUser(user);
        updateUserImage(user,userImage,context);
    }

    public void createNewUser(User user)
    {
        String time = System.currentTimeMillis() + "";
        user.setTimeCreated(time);
        user.setLastTimeLogIn(time);
        server.createNewUser(user);
    }

    public void updateUserInServer(User user)
    {
        server.updateUser(user);
    }
//    public void searchForUsers(String query)
//    {
//        server3.searchForUsers(query);
//    }
//
//    public void setOnUserDownloadListener(Server3.onUserDownloaded listener)
//    {
//        server3.setUserDownloadListener(listener);
//    }
//
//    public void setOnUsersFoundListener(Server3.onUserFound listener)
//    {
//        server3.setUserFoundListener(listener);
//    }

    public LiveData<String>getUnreadConversationsCount()
    {
        return chatDao.getUnreadConversationsCount();
    }


    public void uploadFile(String uid,String msgID,Bitmap bitmap,Context context)
    {
        server.uploadFile(uid,msgID,bitmap,context);
    }

    public void setOnFileUploadListener(Server.onFileUpload listener)
    {
        server.setFileUploadListener(listener);
    }

    public void uploadFile(String msgID, Uri uri,Context context)
    {
        server.uploadFile(msgID, uri, context);
    }

    public void downloadImage(String iid)
    {
        server.downloadImage(iid);
    }

    public void downloadUser(String uid)
    {
        //server3.downloadUser(uid);
        server.getUserById(uid);
    }

    public void setOnUserFoundListener(Server.onUsersFound listener)
    {
        server.setFoundUsers(listener);
    }

    public void setOnUserDownloadedListener(Server.onUserDownload listener)
    {
        server.setDownloadedUsers(listener);
    }

    public void searchUsers(String query)
    {
        server.searchUsers(query);
    }

    public void getUserToken(String uid)
    {
        server.getUserToken(uid);
    }
    public void setOnTokenDownloadListener(Server.onTokenDownloaded listener)
    {
        server.setDownloadedTokenListener(listener);
    }

    public void setOnFileDownloadListener(Server.onFileDownload listener)
    {
        server.setFileDownloadListener(listener);
    }
}
