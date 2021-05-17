package Model;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;

import NormalObjects.User;

public interface IServer {
    void updateServer(String path,String data);
    void updateServer(String path, HashMap<String,Object> map);
    void updateServer(String path,boolean data);
    void uploadImageBitmap(String path, Bitmap imageBitmap, final Context context);
    void setSecondPath(String path);
    void uploadImage(String path, String photoPath, final Context context);
    void removeServer(String path);


    void DownloadUser(Context context,String userUID);
    void createNewUser(String name, String lastName, String nick, Bitmap userImage, Context context);

    void DownloadConversations2(Context context);
    void removeMessagesChildEvent();
    void DownloadMessages2(Context context, String conversationID,int amount);

    void SearchForUsers(final String searchQuery, final Context context);
    User getUser();
    void deleteMessage(boolean delete);
    void onRemoveUserListener();

    void DetectNewVersion();

}
