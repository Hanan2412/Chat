package Controller;

import android.content.Context;
import android.graphics.Bitmap;

public interface INewUser {
    void onNewUser(String name, String lastName, String nickname, Bitmap userImage, Context context);
}
