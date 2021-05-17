package Controller;

import android.content.Context;
import android.graphics.Bitmap;

import com.example.woofmeow.ConversationGUI;
import com.example.woofmeow.MainGUI;
import com.example.woofmeow.PreferencesUpdate;
import com.example.woofmeow.ProfileGUI;

import java.util.ArrayList;
import java.util.HashMap;

import Model.Server2;
import NormalObjects.Conversation;
import NormalObjects.Message;
import NormalObjects.User;
import Services.NotificationsControl;

//@SuppressWarnings("unchecked")
public class CController implements IConversationController,IMainController, Server2.ServerData, IPreferenceInterface {

    private ConversationGUI conversationGUI;
    private  MainGUI mainGUI;
    private Server2 server2;
    private ProfileGUI profileGUI;
    private NotificationsControl notificationsControl;
    private static CController cController = null;
    private PreferencesUpdate preferencesUpdate;
    private CController()
    {
        //singleton design pattern is implemented so each view won't go null when views are changed
    }

    public static CController getController()
    {
        if(cController==null)
            cController = new CController();
        return cController;
    }

    public void setConversationGUI(ConversationGUI conversationGUI)
    {
        this.conversationGUI = conversationGUI;
        server2 = Server2.getServer();
        server2.setServerData(this);
    }
    public void setMainGUI(MainGUI mainGUI)
    {
        this.mainGUI = mainGUI;
        server2 = Server2.getServer();
        server2.setServerData(this);
    }

    public void setPreferencesInterface(PreferencesUpdate preferencesUpdate)
    {
        this.preferencesUpdate = preferencesUpdate;
        server2 = Server2.getServer();
        server2.setServerData(this);
    }

    public void setNotificationsControl(NotificationsControl notificationsControl)
    {
        this.notificationsControl = notificationsControl;
        server2 = Server2.getServer();
        server2.setServerData(this);
    }

    public void setProfileGUI(ProfileGUI profileGUI)
    {
        this.profileGUI = profileGUI;
        server2 = Server2.getServer();
        server2.setServerData(this);
    }

    public void removeInterface(int interfaceToRemove)
    {
        switch (interfaceToRemove)
        {
            case 0:
                mainGUI = null;
                break;
            case 1:
                conversationGUI = null;
                break;
            case 2:
                notificationsControl = null;
                break;
        }
    }


    @Override
    public void onUpdateData(String path, HashMap<String, Object> map) {
        server2.updateServer(path, map);
    }

    @Override
    public void onUpdateData(String path, String data) {
        server2.updateServer(path, data);
    }

    @Override
    public void onUpdateData(String path, boolean data)
    {
        server2.updateServer(path,data);
    }

    @Override
    public void onDownloadUser(Context context, String userUID) {
        server2.DownloadUser(context, userUID);
    }

    @Override
    public void onRemoveData(String path) {
        server2.deleteMessage(true);
        server2.removeServer(path);
    }

    @Override
    public void onUploadFile(String path, String filePath, Context context) {
        server2.uploadFile(path,filePath,context);
    }

    @Override
    public void onUpdateInteraction(String path, boolean typing) {
        server2.updateServer(path,typing);
    }


    @Override
    public void onDownloadConversations(Context context) {
        //startConversationsBroadcast(context);
        server2.DownloadConversations3(context);

    }

    @Override
    public void onFindUsersQuery(String query,Context context) {
        server2.SearchForUsers(query,context);
    }


    @Override
    public void onSecondPath(String path) {
        server2.setSecondPath(path);
    }

    @Override
    public void onUploadImage(String path, String photoPath, Context context) {
        server2.uploadImage(path, photoPath, context);
    }

    @Override
    public void onUploadImageBitmap(String path, Bitmap imageBitmap, Context context) {
        server2.uploadImageBitmap(path, imageBitmap, context);
    }



    @Override
    public void onDownloadMessages(Context context, String conversationID, int amount) {
        server2.DownloadMessages2(context, conversationID, amount);
    }

    @Override
    public void onRemoveChildEvent() {
        server2.removeMessagesChildEvent();
    }

    @Override
    public void onRemoveUserListener() {
        server2.onRemoveUserListener();
    }


    //Server Data methods

    @Override
    public void onSingleNewMessage(Message message)
    {
        if(conversationGUI!=null)
            conversationGUI.onReceiveSingleMessage(message);
    }


    @Override
    public void onMessagesDownloaded(ArrayList<Message> messages) {
        if(conversationGUI!=null)
            conversationGUI.onReceiveMessages(messages);
    }

    @Override
    public void onItemChange(Message message, int position) {
        if(conversationGUI!=null)
            conversationGUI.onReceiveItemChange(message,position);
    }

    @Override
    public void onMessageRemoved(int position) {
        if(conversationGUI!=null)
            conversationGUI.onRemoveDeletedMessage(position);
    }

    @Override
    public void onVersionChange(float newVersion) {
        if (mainGUI!=null)
            mainGUI.onVersionChange(newVersion);
    }

    @Override
    public void onUserDownload(User user) {
        if(conversationGUI!=null)
            conversationGUI.onReceiveUser(user);
        else if(mainGUI!=null)
            mainGUI.onReceiveUser(user);
        else if (profileGUI!=null)
            profileGUI.onReceiveUser(user);
    }

    @Override
    public void onConversationsDownloaded(ArrayList<Conversation> conversations) {
        if(mainGUI!=null)
            mainGUI.onReceiveConversations(conversations);
        if(notificationsControl!=null)
            notificationsControl.onConversationMute(conversations);
    }

    @Override
    public void onConversationDownloaded(Conversation conversation) {
        if(mainGUI!=null)
         mainGUI.onReceiveConversation(conversation);
    }

    @Override
    public void onConversationChanged(Conversation conversation) {
        if(mainGUI!=null)
            mainGUI.onChangedConversation(conversation);
    }

    @Override
    public void onConversationDeleted(Conversation conversation) {
        if(mainGUI!=null)
            mainGUI.onRemoveConversation(conversation);
    }

    @Override
    public void onFoundUserQuery(User user) {
            if(mainGUI!=null)
                mainGUI.onReceiveUsersQuery(user);
    }

    @Override
    public void onPreferenceChange(String path, boolean data) {
        server2.updateServer(path,data);
    }

    @Override
    public void onPreferenceDelete(String path) {
        server2.removeServer(path);
    }
}
