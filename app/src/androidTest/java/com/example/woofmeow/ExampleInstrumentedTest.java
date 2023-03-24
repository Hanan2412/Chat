package com.example.woofmeow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.UUID;

import Backend.ChatDao;
import Backend.ChatDataBase;
import Consts.MessageStatus;
import Consts.MessageType;
import NormalObjects.Conversation;
import NormalObjects.Group;
import NormalObjects.Message;
import NormalObjects.User;


/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@SuppressWarnings({"Convert2Lambda", "Anonymous2MethodRef"})
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExampleInstrumentedTest {
   /*
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.woofmeow", appContext.getPackageName());
    }
  */

    private static ChatDataBase db;
    private static ChatDao dao;
    private static Context context;
    private static int conversationAmount = 0, messagesAmount = 0;
    private static int primaryKey = -1;
    private static int messagePKey = -1;
    private static int userKey = -1;
    private static String conversationID = "aaaaaaaaaaaaa", userID = "abcd";

    @BeforeClass
    public static void initDB() {
        /*appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        model = new ViewModelProvider((ViewModelStoreOwner) appContext).get(AppViewModel.class);

         */
        context = ApplicationProvider.getApplicationContext();
        db = ChatDataBase.getInstance(context);
        //db = Room.inMemoryDatabaseBuilder(context, ChatDataBase.class).build();
        dao = db.chatDao();
        dao.clearConversationsTable();
        dao.clearMessagesTable();
        dao.clearUsersTable();
        dao.clearGroups();
    }

    @Test
    public void gTest()
    {
        dao.insertNewGroup(new Group("C_1653148911051","1221211221122121"));
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                dao.getGroup("C_1653148911051").observeForever(new Observer<List<Group>>() {
                    @Override
                    public void onChanged(List<Group> groups) {
                        for (Group group: groups)
                        {
                            System.out.println(group.toString());
                            assertEquals(group.getConversationID(),"C_1653148911051");
                            assertEquals(group.getUid(),"1221211221122121");
                        }
                    }
                });
                dao.getRecipients("C_1653148911051").observeForever(new Observer<List<User>>() {
                    @Override
                    public void onChanged(List<User> users) {
                        for (User user: users){
                            assertEquals(user.getUserUID(),"1221211221122121");
                        }
                    }
                });
            }
        });
    }

    @Test
    public void groupTest()
    {
        for (int i = 0;i<3;i++)
        {
            Group group = new Group("aaaa","123" + i);
            dao.insertNewGroup(group);
            User user = new User();
            user.setUserUID("123" + i);
            dao.insertNewUser(user);
        }
        Group group = new Group("aaaab","1234");
        dao.insertNewGroup(group);
        User user = new User();
        user.setUserUID("1234");
        dao.insertNewUser(user);
        LiveData<List<String>>uids = dao.getUidFromGroup("aaaab");
        Observer<List<String>>observer = new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {
                for (String uid :strings)
                {
                    assertEquals("1234",uid);
                }
            }
        };
        LiveData<List<User>>recipients = dao.getRecipients("aaaab");
        Observer<List<User>>observer1 = new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                for (User user1 : users)
                {
                    assertEquals("1234",user1.getUserUID());
                }
            }
        };
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                uids.observeForever(observer);
                recipients.observeForever(observer1);
            }
        });
    }
    @Test
    public void c_01getConversations() {

        LiveData<List<Conversation>> conversationsLV = dao.getAllConversations();
        Observer<List<Conversation>> observer = new Observer<List<Conversation>>() {
            @Override
            public void onChanged(List<Conversation> conversations) {
                conversationsLV.removeObserver(this);
                assertEquals(conversationAmount, conversations.size());

            }
        };
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                conversationsLV.observeForever(observer);
            }
        });

    }


    @Test
    public void c_02createNewConversation() {
        Message message = createMessage();
        Conversation conversation = new Conversation(message.getConversationID());
        dao.insertNewConversation(conversation);
        conversationAmount++;
        conversation.setConversationID("bbbbbbbbbb");
        dao.insertNewConversation(conversation);
        conversationAmount++;
        LiveData<List<Conversation>> conversationsLV = dao.getAllConversations();
        Observer<List<Conversation>> observer = new Observer<List<Conversation>>() {
            @Override
            public void onChanged(List<Conversation> conversations) {
                conversationsLV.removeObserver(this);
                assertEquals(conversationAmount, conversations.size());

            }
        };
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                conversationsLV.observeForever(observer);
            }
        });
    }

    @Test
    public void c_030getConversationByID() {
        LiveData<Conversation> conversationByID = dao.getConversation(conversationID);
        Observer<Conversation> observer = new Observer<Conversation>() {
            @Override
            public void onChanged(Conversation conversation) {
                conversationByID.removeObserver(this);
                primaryKey = conversation.getP_key();
                assertEquals(conversationID, conversation.getConversationID());
            }
        };
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                conversationByID.observeForever(observer);
            }
        });
    }

    @Test
    public void c_031getConversationByID() {
        LiveData<Conversation> conversationByID = dao.getConversation("qwerty");
        Observer<Conversation> observer = new Observer<Conversation>() {
            @Override
            public void onChanged(Conversation conversation) {
                conversationByID.removeObserver(this);
                assertNull(conversation);
            }
        };
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                conversationByID.observeForever(observer);
            }
        });
    }

    @Test
    public void c_032getConversationByPhone()
    {
        Conversation conversation = new Conversation(conversationID);
        conversation.setLastMessageID(123);
        conversation.setLastMessage("last message");
        dao.insertNewConversation(conversation);
        User user = new User();
        user.setUserUID(userID);
        user.setName("Hanan");
        user.setLastName("Dorfman");
        user.setPhoneNumber("879");
        dao.insertNewUser(user);
        User user1 = new User();
        user1.setUserUID(userID);
        user1.setName("Hanan");
        user1.setLastName("Dorfman");
        user1.setPhoneNumber("1237");
        dao.insertNewUser(user1);
        Group group0 = new Group("1234",user1.getUserUID());
        dao.insertNewGroup(group0);
        Group group = new Group("s_"+conversationID,user.getUserUID());
        dao.insertNewGroup(group);
        Group group1 = new Group("2",user.getUserUID()+"15");
        dao.insertNewGroup(group1);
        LiveData<String>ld = dao.getConversationIDByPhone("879");
        Observer<String>stringObserver = new Observer<String>() {
            @Override
            public void onChanged(String s) {
                assertEquals("s_"+conversationID,s);
            }
        };
        Handler handler1 = new Handler(Looper.getMainLooper());
        handler1.post(new Runnable() {
            @Override
            public void run() {
                ld.observeForever(stringObserver);
            }
        });
    }


    @Test
    public void c_04updateConversationByObject() {
        Conversation conversation = new Conversation(conversationID);
        conversation.setLastMessageTime(System.currentTimeMillis());
        conversation.setLastMessage("update conversation");
        conversation.setLastMessageID(123456);
        conversation.setP_key(primaryKey);
        dao.updateConversation(conversation);
        LiveData<Conversation> conversationsLV = dao.getConversation(conversationID);
        Observer<Conversation> observer = new Observer<Conversation>() {
            @Override
            public void onChanged(Conversation conversation) {
                conversationsLV.removeObserver(this);
                assertEquals("update conversation", conversation.getLastMessage());
            }
        };

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                conversationsLV.observeForever(observer);
            }
        });
        //dao.updateConversation("second update","qwe",MessageType.textMessage.name(),System.currentTimeMillis()+"","hanan test second","aaaaaaaaaaaaa");
    }

    @Test
    public void c_05updateConversationByData() {
//        dao.updateConversation("second update", "qwe", MessageType.textMessage.name(), System.currentTimeMillis() + "", "hanan test second", "aaaaaaaaaaaaa");
        LiveData<Conversation> conversationsLV = dao.getConversation(conversationID);
        Observer<Conversation> observer = new Observer<Conversation>() {
            @Override
            public void onChanged(Conversation conversation) {
                conversationsLV.removeObserver(this);
                assertEquals("second update", conversation.getLastMessage());
            }
        };
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                conversationsLV.observeForever(observer);
            }
        });
    }
    @Test
    public void c_06muteConversation()
    {
        dao.muteConversation(conversationID);
        LiveData<Boolean>conversationLiveData = dao.isConversationMuted(conversationID);
        Observer<Boolean>observer = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                conversationLiveData.removeObserver(this);
                assertTrue(aBoolean);
            }
        };
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                conversationLiveData.observeForever(observer);
            }
        });
    }

    @Test
    public void c_07blockConversation()
    {
        dao.blockConversation(conversationID);
        LiveData<Boolean>conversationLiveData = dao.isConversationBlocked(conversationID);
        Observer<Boolean>observer = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                conversationLiveData.removeObserver(this);
                assertTrue(aBoolean);
            }
        };
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                conversationLiveData.observeForever(observer);
            }
        });
    }
    @Test
    public void c_08allMuted()
    {
        LiveData<List<Conversation>>allMuted = dao.getAllMutedConversations();
        Observer<List<Conversation>>observer = new Observer<List<Conversation>>() {
            @Override
            public void onChanged(List<Conversation> conversations) {
                allMuted.removeObserver(this);
                for (Conversation conversation : conversations)
                {
                    if (!conversation.isMuted())
                        fail("should be muted conversation is not muted");
                }
            }
        };
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                allMuted.observeForever(observer);
            }
        });
    }

    @Test
    public void c_09allBlocked()
    {
        LiveData<List<Conversation>>allBlocked = dao.getAllBlockedConversations();
        Observer<List<Conversation>>observer = new Observer<List<Conversation>>() {
            @Override
            public void onChanged(List<Conversation> conversations) {
                allBlocked.removeObserver(this);
                for (Conversation conversation : conversations)
                {
                    if (!conversation.isBlocked())
                        fail("should be blocked conversation is not blocked");
                }
            }
        };
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                allBlocked.observeForever(observer);
            }
        });
    }

    @Test
    public void c_99deleteConversationByID() {
        Conversation conversation = new Conversation(conversationID);
        conversation.setP_key(primaryKey);
        dao.deleteConversation(conversation);
        LiveData<List<Conversation>> conversationsLV = dao.getAllConversations();
        Observer<List<Conversation>> observer = new Observer<List<Conversation>>() {
            @Override
            public void onChanged(List<Conversation> conversations) {
                conversationsLV.removeObserver(this);
                assertEquals(--conversationAmount, conversations.size());

            }
        };
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                conversationsLV.observeForever(observer);
            }
        });
    }



    @Test
    public void m_01createNewMessage() {
        Message message = createMessage();
        dao.insertNewMessage(message);
        messagesAmount++;
        Message message2 = createMessage();
        dao.insertNewMessage(message2);
        messagesAmount++;
        LiveData<List<Message>> messages = dao.getAllMessages(conversationID);
        Observer<List<Message>> observer = new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages1) {
                messages.removeObserver(this);
                messagePKey = messages1.get(0).getP_key();
                assertEquals(messagesAmount, messages1.size());

            }
        };
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                messages.observeForever(observer);
            }
        });
    }


    @Test
    public void m_02updateMessage() {
        Message message = createMessage();
        message.setContent("updated Message");
        message.setP_key(messagePKey);
        dao.updateMessage(message);
        LiveData<List<Message>> messages = dao.getAllMessages(conversationID);
        Observer<List<Message>> observer = new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages1) {
                messages.removeObserver(this);
                assertEquals("updated Message", messages1.get(0).getContent());

            }
        };
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                messages.observeForever(observer);
            }
        });
    }

    @Test
    public void m_03deleteMessageByID() {
        Message message = createMessage();
        dao.insertNewMessage(message);
        dao.deleteMessage(message.getMessageID());
        LiveData<List<Message>> messages = dao.getAllMessages(conversationID);
        Observer<List<Message>> observer = new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages1) {
                messages.removeObserver(this);
                assertEquals(2, messages1.size());

            }
        };
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                messages.observeForever(observer);
            }
        });
    }

    @Test
    public void u_01createUser() {
        User user = new User();
        user.setUserUID(userID);
        user.setName("Hanan");
        user.setLastName("Dorfman");
        dao.insertNewUser(user);
        LiveData<User> userLiveData = dao.getUser(userID);
        Observer<User> observer = new Observer<User>() {
            @Override
            public void onChanged(User user) {
                userLiveData.removeObserver(this);
                userKey = user.getP_key();
                assertEquals(userID, user.getUserUID());
            }
        };
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                userLiveData.observeForever(observer);
            }
        });
    }

    @Test
    public void u_02muteUser() {
        User user = new User();
        user.setUserUID(userID);
        user.setP_key(userKey);
        dao.muteUser(userID);
        LiveData<Boolean> muted = dao.isUserMuted(userID);
        Observer<Boolean> observer = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                muted.removeObserver(this);
                assertTrue(aBoolean);
            }
        };
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                muted.observeForever(observer);
            }
        });
    }

    @Test
    public void u_03blockUser() {
        dao.blockUser(userID);
        LiveData<Boolean> block = dao.isUserBlocked(userID);
        Observer<Boolean> observer = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                block.removeObserver(this);
                assertTrue(aBoolean);
            }
        };
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                block.observeForever(observer);
            }
        });
    }

    @Test
    public void u_04isUserExist() {
        dao.deleteUser(userID);
        LiveData<Boolean> exists = dao.isUserExists(userID);
        Observer<Boolean> observer = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                exists.removeObserver(this);
                assertFalse(aBoolean);
            }
        };
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                exists.observeForever(observer);
            }
        });
    }

    @Test
    public void u_05allRecipients()
    {
        Group group = new Group(conversationID,userID);
        dao.insertNewGroup(group);
        User user = new User();
        user.setName("Hanan");
        user.setLastName("Dorfman");
        user.setUserUID(userID);
        dao.insertNewUser(user);
        User user1 = new User();
        user1.setName("mega");
        user1.setLastName("bayir");
        user1.setUserUID("qqqqqqqqqqqqqqqqqqqqqqqqqqq");
        Group group1 = new Group("Asdasdasdasd",user1.getUserUID());
        dao.insertNewGroup(group1);
        dao.insertNewUser(user1);
        User user2 = new User();
        user2.setName("shupersal");
        user2.setLastName("sheli");
        user2.setUserUID("wwwwwwwwwwwwwwww");
        dao.insertNewUser(user2);
        Group group2 = new Group(conversationID,user2.getUserUID());
        dao.insertNewGroup(group2);


        LiveData<List<User>> recipients = dao.getRecipients(conversationID);
        Observer<List<User>> observer = new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                for (User user8:users)
                    Log.e("recipients",user8.getUserUID());
                assertEquals(2,users.size());
            }
        };
        Handler handler1 = new Handler(Looper.getMainLooper());
        handler1.post(new Runnable() {
            @Override
            public void run() {
                recipients.observeForever(observer);
            }
        });

    }

    @Test
    public void u_tokenUpdate()
    {
        User user = new User();
        String uid = UUID.randomUUID()+"";
        user.setUserUID(uid);
        user.setName("omega");
        user.setLastName("gamma");
        dao.insertNewUser(user);
        dao.updateUserToken(uid,"1234567890");
        LiveData<User>getUsers = dao.getUser(uid);
        Observer<User> observer = new Observer<User>() {
            @Override
            public void onChanged(User user1) {
                assertEquals("1234567890",user1.getToken());
            }
        };
        Handler handler1 = new Handler(Looper.getMainLooper());
        handler1.post(new Runnable() {
            @Override
            public void run() {
                getUsers.observeForever(observer);
            }
        });

    }


    public Message createMessage() {
        Message msg = new Message();
        msg.setMessageID(System.currentTimeMillis());
        msg.setContent("test1");
        msg.setMessageType(MessageType.textMessage.ordinal());
        msg.setConversationName("testing");
        msg.setSenderName("hanan");
        msg.setConversationName("hanan test");
        msg.setMessageStatus(MessageStatus.SENT.ordinal());
        msg.setConversationID(conversationID);
        return msg;
    }

    @AfterClass
    public static void closeDB() {
        db.close();
    }
}