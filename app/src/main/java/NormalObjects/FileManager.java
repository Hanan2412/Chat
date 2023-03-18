package NormalObjects;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

//import DataBase.DBActive;


@SuppressWarnings("Convert2Lambda")
public class FileManager {

    private final String fileError = "Error while creating file";
    public static final String conversationProfileImage = "conversation_profile_image";
    public static final String user_profile_images = "user_images";
    private static FileManager fileManager;

    private FileManager(){}

    public static FileManager getInstance(){
        if(fileManager==null)
            fileManager = new FileManager();
        return fileManager;
    }

    public interface onLoadingImage
    {
        void onSuccess(Bitmap bitmap);
        void onFailed();
    }

    private onLoadingImage listener;

    public void setListener(onLoadingImage listener){this.listener = listener;}
    public void killListener(){listener = null;}
    //saves the profile image of the user or the conversation to local storage
    public void saveProfileImage(Bitmap bitmap, String id, Context context, boolean identifier)
    {
        ContextWrapper contextWrapper = new ContextWrapper(context.getApplicationContext());
        File directory;
        if (!identifier)
            directory = contextWrapper.getDir(user_profile_images, Context.MODE_PRIVATE);
        else
            directory = contextWrapper.getDir(conversationProfileImage,Context.MODE_PRIVATE);
        if (!directory.exists())
            if (!directory.mkdir()) {
                Log.e("error", "couldn't create a directory in fm");
            }
        File path = new File(directory, id + "_Image");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("FileManager", "saveProfileImage - image was saved");

    }

    //reads images that were sent in a message
    public void readImageMessage(String path,Context context)
    {
        if (listener!=null) {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Callable<Bitmap> bitmapCallable = new Callable<Bitmap>() {
                @Override
                public Bitmap call() {
                    Bitmap bitmap = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ContentResolver resolver = context.getApplicationContext().getContentResolver();
                        try {
                            bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, Uri.parse(path)));
                        } catch (IOException e) {
                            e.printStackTrace();
                            listener.onFailed();
                        }
                    } else
                    {
                        try {
                            if (Build.VERSION.SDK_INT > 27) {
                                ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), Uri.parse(path));
                                bitmap = ImageDecoder.decodeBitmap(source);
                            } else {
                                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.fromFile(new File(path)));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            listener.onFailed();
                        }
                        //bitmap = BitmapFactory.decodeFile(path);
                    }
                    if (bitmap != null) {
                        float ratio = (float) bitmap.getWidth() / (float) bitmap.getHeight();
                        int width = 540;
                        int height;
                        if (bitmap.getWidth() > bitmap.getHeight())
                            height = (int) (width / ratio);
                        else
                            height = (int) (width * ratio);
                        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
                    }
                    return bitmap;
                }
            };
            Future<Bitmap> bitmapFuture = executorService.submit(bitmapCallable);
            Thread doneThread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    while (!bitmapFuture.isDone()) {
                        /*
                         * waiting for the picture to load
                         * since get method blocks and makes the app freeze until the image is loaded
                         * noticeable with multiple images
                         * */
                    }
                    try {
                        Bitmap bitmap = bitmapFuture.get();
                        listener.onSuccess(bitmap);
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                        listener.onFailed();
                    } finally {
                        executorService.shutdown();
                    }

                }
            };
            doneThread.setName("readImageMessage");
            doneThread.start();
        }else Log.e("FileManager", "listener is null" );
    }

    public Bitmap readImage(Context context,String dirName,String childPath)
    {
        try {
            ContextWrapper contextWrapper = new ContextWrapper(context.getApplicationContext());
            File directory = contextWrapper.getDir(dirName, Context.MODE_PRIVATE);
            File imageFile = new File(directory, childPath + "_Image");
            FileInputStream inputStream = new FileInputStream(imageFile);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            return bitmap;
        }catch (FileNotFoundException e){
            Log.e("file Manager","no image available at this directory");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Deprecated
    public String getSavedImagePath(Context context,String dirName,String childPath)
    {
        ContextWrapper contextWrapper = new ContextWrapper(context.getApplicationContext());
        File directory = contextWrapper.getDir(dirName, Context.MODE_PRIVATE);
        File imageFile = new File(directory,childPath + "_Image");
        return imageFile.getAbsolutePath();
    }

    @Deprecated
    public String SaveUserImage(Bitmap bitmap,String userUID,Context context)
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
        return Path.getAbsolutePath();
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
                    path = imageUri.toString();
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

    @Deprecated
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

    public String SaveVideo(File file,Context context)
    {
        String path = null;
        String fileName = file.getName();
        OutputStream out = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = context.getApplicationContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/*");
            values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES);
            Uri videoUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            try {
                if (videoUri != null) {
                    out = resolver.openOutputStream(videoUri);
                    path = videoUri.getPath();
                    File imageDirectory = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
                    File videoFile = new File(imageDirectory, fileName);
                    try {
                        out = new FileOutputStream(videoFile);
                        path = videoFile.getAbsolutePath();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            File imageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
            File videoFile = new File(imageDirectory, fileName);
            try {
                out = new FileOutputStream(videoFile);
                path = videoFile.getAbsolutePath();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (out != null) {
            int size = (int) file.length();
            byte[] bytes = new byte[size];
            try {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
                int bytesLength = bufferedInputStream.read(bytes, 0, bytes.length);
                if (bytesLength != size)
                    Log.e("save video error", "writing file wasn't complete");
                bufferedInputStream.close();
                out.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return path;
    }
}
