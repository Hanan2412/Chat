package com.example.woofmeow;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import NormalObjects.Conversation;
import NormalObjects.Message;
import NormalObjects.User;
import Retrofit.Server;

@SuppressWarnings("Convert2Lambda")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CommunicationTest {

    private static Server server;
    private static User user;
    private static final String userID = "567";
    private static final String name = "Hanan12";
    private static final String token = "mnmnmnmn";
    @BeforeClass
    public static void init()
    {
        server = Server.getInstance();
        user = new User();
        user.setUserUID(userID);
        user.setName(name);
        user.setToken(token);
    }

//    @Before
//    public void nullify()
//    {
//        server.setFileUploadListener(null);
//        server.setRestoreListener(null);
//        server.setFoundUsers(null);
//        server.setDownloadedUsers(null);
//        server.setDownloadedTokenListener(null);
//        server.setImageDownloadedListener(null);
//    }

    @Test
    public void backupTest()
    {
        server.setOnBackupListener(new Server.onBackupListener() {
            @Override
            public void onBackupCompleted(String msg) {
                Assert.assertEquals("backup succeeded",msg);
            }

            @Override
            public void onBackupFailed(String message) {
                Assert.assertEquals("failed", message);
            }
        });
        Conversation conversation = new Conversation("123456789");
        List<Conversation>conversations = new ArrayList<>();
        conversations.add(conversation);
        server.backupConversations(conversations,userID);
        Message message = new Message();
        message.setMessageID(212123);
        List<Message>messages = new ArrayList<>();
        messages.add(message);
        server.backupMessages(messages,userID);
    }

    @Test
    public void restoreTest()
    {
        server.setRestoreListener(new Server.onRestoreListener() {
            @Override
            public void onMessagesRestored(List<Message> messages) {
                Assert.assertEquals("212123", messages.get(0).getMessageID());
            }

            @Override
            public void onConversationsRestored(List<Conversation> conversations) {
                Assert.assertEquals("123456789",conversations.get(0).getConversationID());
            }
        });
        server.restoreConversations(userID);
        server.restoreMessages(userID);
    }

    @Test
    public void downloadUserTest()
    {
        server.setDownloadedUsers(new Server.onUserDownload() {
            @Override
            public void downloadedUser(User user) {
                Assert.assertEquals(userID,user.getUserUID());
            }
        });

        server.createNewUser(user);
        server.getUserById(userID);
    }

    @Test
    public void foundUsersTest()
    {
        server.setFoundUsers(new Server.onUsersFound() {
            @Override
            public void foundUsers(List<User> users) {
                User user = users.get(0);
                Assert.assertEquals(name, user.getName());
            }

            @Override
            public void error(String errorMessage) {
                Log.e("aaaaaa", errorMessage);
            }
        });
        server.searchUsers(name);
    }

    @Test
    public void tokenDownloadTest()
    {
        server.setDownloadedTokenListener(new Server.onTokenDownloaded() {
            @Override
            public void tokenDownloaded(String uid, String token) {
                    Assert.assertEquals(CommunicationTest.token, token);
            }

            @Override
            public void error(String message) {

            }
        });
        server.saveToken(token,userID);
        server.getUserToken(userID);
    }

    @Test
    public void fileUploadTest()
    {
        server.setFileUploadListener(new Server.onFileUpload() {
            @Override
            public void onPathReady(long msgID, String path) {

            }

            @Override
            public void onStartedUpload(long msgID) {
                Assert.assertEquals(212123,msgID);
            }

            @Override
            public void onProgress(long msgID, int progress) {

            }

            @Override
            public void onUploadFinished(long msgID) {
                Assert.assertEquals(212123,msgID);
            }

            @Override
            public void onUploadError(long msgID, String errorMessage) {

            }
        });
        Bitmap bitmap = BitmapFactory.decodeResource(ApplicationProvider.getApplicationContext().getResources(), R.drawable.cns);
        server.uploadFile(userID,212123,bitmap,  ApplicationProvider.getApplicationContext());
    }

    @Test
    public void fileDownloadTest()
    {
        server.setFileDownloadListener(new Server.onFileDownload() {
            @Override
            public void onDownloadStarted() {

            }

            @Override
            public void onProgress(int progress) {

            }

            @Override
            public void onDownloadFinished(File file) {
                    Assert.assertNotEquals(null,file);
            }

            @Override
            public void onFileDownloadFinished(String messageID, File file) {

            }

            @Override
            public void onDownloadError(String errorMessage) {

            }
        });
        server.downloadFile("","212123");
    }

    @Test
    public void imageDownloadTest()
    {
        server.setImageDownloadedListener(new Server.onImageDownloaded() {
            @Override
            public void downloadedImage(Bitmap bitmap) {
                System.out.println("this is bitmap");
                Assert.assertNotEquals(null, bitmap);
            }

            @Override
            public void downloadFailed(String message) {
                Assert.fail(message);
            }
        });
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                server.downloadImage("pfghXKWGCja8i8YPQz71DuXxyTI2_1647526597032.png");
            }
        });
    }
}
