package NormalObjects;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.example.woofmeow.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileManager {

    private final String fileError = "Error while creating file";

    private static FileManager fileManager;

    private FileManager(){}

    public static FileManager getInstance(){
        if(fileManager==null)
            fileManager = new FileManager();
        return fileManager;
    }

    public void SaveUserImage(Bitmap bitmap,String userUID,Context context)
    {
        ContextWrapper contextWrapper = new ContextWrapper(context.getApplicationContext());
        File directory = contextWrapper.getDir("user_images", Context.MODE_PRIVATE);
        if (!directory.exists())
            if (!directory.mkdir()) {
                Log.e("error", "couldn't create a directory in conversationAdapter2");
            }
        File Path = new File(directory, userUID + "_Image");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(Path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //save image to app directory and returns the path to that image
    public String saveImage(Bitmap bitmap, Context context)
    {
        String path = null;
        String fileName = "image_" + System.currentTimeMillis() + ".jpg";
        OutputStream out = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = context.getApplicationContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            try {
                if (imageUri != null) {
                    out = resolver.openOutputStream(imageUri);
                    path = imageUri.getPath();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            File imageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File imageFile = new File(imageDirectory, fileName);
            try {
                out = new FileOutputStream(imageFile);
                path = imageFile.getAbsolutePath();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out))
                Log.i("good", "Bitmap successfully written");
            else
                Log.e(fileError, "Bitmap save have failed");
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return path;
    }

    public Bitmap getSavedImage(Context context,String childPath)
    {
        try {
        ContextWrapper contextWrapper = new ContextWrapper(context);
        File directory = contextWrapper.getDir("user_images", Context.MODE_PRIVATE);
        File imageFile = new File(directory,childPath);
        return BitmapFactory.decodeStream(new FileInputStream(imageFile));
        } catch (FileNotFoundException e) {
            Log.e(fileError,"couldn't create bitmap");
            e.printStackTrace();
        }
        return null;
    }

    public String getSavedImagePath(Context context,String childPath)
    {
        ContextWrapper contextWrapper = new ContextWrapper(context);
        File directory = contextWrapper.getDir("user_images", Context.MODE_PRIVATE);
        File imageFile = new File(directory,childPath);
        return imageFile.getAbsolutePath();
    }

    public void sendFile()
    {

    }

    private void receiveFile()
    {

    }
}
