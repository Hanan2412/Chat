package Controller;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.HashMap;

public interface IConversationController {
    void onSecondPath(String path);
    void onUploadImage(String path, String photoPath,Context context);
    void onUploadImageBitmap(String path, Bitmap imageBitmap,Context context);
    void onDownloadMessages(Context context,String conversationID,int amount);
    void onRemoveChildEvent();
    void onRemoveUserListener();
    void onUpdateData(String path, HashMap<String,Object>map);
    void onUpdateData(String path,String data);
    void onUpdateData(String path,boolean data);
    void onDownloadUser(Context context,String userUID);
    void onRemoveData(String path);
    void onUploadFile(String path,String filePath,Context context);
    void onUpdateInteraction(String path,boolean typing);

}
