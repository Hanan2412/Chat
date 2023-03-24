package Retrofit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.common.util.IOUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import NormalObjects.Conversation;
import NormalObjects.FileManager;
import NormalObjects.Message;
import NormalObjects.User;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Server {

    private ServerConnect api;
    private FileTransmission fileTransmissionApi;
    private static Server server;
    private final String info = "server.class";

    public interface onUserDownload{
        void downloadedUser(User user);
    }

    public interface onUsersFound{
        void foundUsers(List<User>users);
        void error(String errorMessage);
    }

    public interface onTokenDownloaded{
        void tokenDownloaded(String uid,String token);
        void error(String message);
    }

    public interface onFileUpload{
        void onPathReady(long msgID, String path);
        void onStartedUpload(long msgID);
        void onProgress(long msgID, int progress);
        void onUploadFinished(long msgID);
        void onUploadError(long msgID,String errorMessage);
    }

    public interface onFileDownload{
        void onDownloadStarted();
        void onProgress(int progress);
        void onDownloadFinished(File file);
        void onFileDownloadFinished(long messageID,File file);
        void onDownloadError(String errorMessage);
    }

    public interface onImageDownloaded{
        void downloadedImage(Bitmap bitmap);
        void downloadFailed(String message);
    }

    public interface onRestoreListener{
        void onMessagesRestored(List<Message>messages);
        void onConversationsRestored(List<Conversation>conversations);
    }

    public interface onBackupListener{
        void onBackupCompleted(String msg);
        void onBackupFailed(String message);
    }

    private onUsersFound foundUsers;
    private onUserDownload downloadedUsers;
    private onTokenDownloaded downloadedToken;
    private onFileUpload fileUpload;
    private onFileDownload fileDownload;
    private onImageDownloaded imageDownloaded;
    private onRestoreListener onRestore;
    private onBackupListener onBackupListener;

    public void setRestoreListener(onRestoreListener listener){
        onRestore = listener;
    }

    public void setImageDownloadedListener(onImageDownloaded imageDownloaded) {
        this.imageDownloaded = imageDownloaded;
    }

    public void setDownloadedUsers(onUserDownload downloadedUsers) {
        this.downloadedUsers = downloadedUsers;
    }

    public void setDownloadedTokenListener(onTokenDownloaded downloadedToken) {
        this.downloadedToken = downloadedToken;
    }

    public void setFoundUsers(onUsersFound foundUsers) {
        this.foundUsers = foundUsers;
    }

    public void setFileUploadListener(onFileUpload fileUpload) {
        this.fileUpload = fileUpload;
    }

    public void setFileDownloadListener(onFileDownload fileDownload) {
        this.fileDownload = fileDownload;
    }

    public void setOnBackupListener(onBackupListener listener)
    {
        this.onBackupListener = listener;
    }
    public static Server getInstance() {
        if (server == null)
            server = new Server();
        return server;
    }

    private Server()
    {
        api = ServerClient.getRetrofitClientServer("http://192.168.1.11:8081").create(ServerConnect.class);
        fileTransmissionApi = UploadFilesClient.getRetrofitClientServer("http://192.168.1.11:8081").create(FileTransmission.class);
    }

    public void createNewUser(User user)
    {
        api.saveNewUser(user).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {
                Log.i(info,response.code() + " " + response.message() + " " + response.body());
            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void updateUserStatus(User user)
    {
        api.changeUserStatus(user).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!response.isSuccessful())
                    Log.e(info,"response code is not good: " + response.code() + " " + response.message());
            }

            @Override
            public void onFailure(@NonNull Call<Void> call,@NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void findUserByName(String name)
    {
        api.findUserByName(name).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call,@NonNull Response<List<User>> response) {
                if (response.isSuccessful())
                {
                    List<User>users = response.body();
                    if (users!=null) {
                        for (User user : users) {
                            Log.i(info + "find user by name","user: " + user.getName() + " " + user.getLastName());
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call,@NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void getUserToken(String uid)
    {
        api.getUserToken(uid).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call,@NonNull Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Map<String,String> tokenMap = response.body();
                    if (tokenMap!=null) {
                        downloadedToken.tokenDownloaded(uid, tokenMap.get("token"));
                        Log.i(info + "get user token", "token: " + tokenMap.get("token"));
                    }
                }
                else
                    downloadedToken.error(response.message());
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call,@NonNull Throwable t) {
                t.printStackTrace();
                downloadedToken.error("failed to send request to server");
            }
        });
    }

    public void saveUserToken(User user)
    {
        api.saveUserToken(user).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call,@NonNull Response<Void> response) {

            }

            @Override
            public void onFailure(@NonNull Call<Void> call,@NonNull Throwable t) {

            }
        });
    }

    public void saveToken(String token,String uid)
    {
        Map<String,String>tokenMap = new HashMap<>();
        tokenMap.put(uid,token);
        api.saveToken(tokenMap).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {
                Log.d("token", "response: " + response.message());
            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void getUserById(String uid)
    {
        Log.i("uid","uid: " + uid);
        api.getUserById(uid).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call,@NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    if (user!=null) {
                        Log.i(info + "get user by id", user.getName() + " " + user.getLastName());
                        if (downloadedUsers!=null)
                            downloadedUsers.downloadedUser(user);
                    }
                    else Log.e("Server Error", "no user with this uid: " + uid);
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call,@NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void getUserStatus(String uid)
    {
        api.getUserStatus(uid).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {
                if (response.isSuccessful())
                {
                    String status = response.body();
                    Log.i(info,"status" + status);
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void updateUser(User user)
    {
        api.updateUser(user).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call,@NonNull Response<Void> response) {
                if (response.isSuccessful())
                {
                    Log.i(info,"update user worked");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call,@NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void deleteUser(String id)
    {
        api.deleteUser(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call,@NonNull Response<Void> response) {
                if (response.isSuccessful())
                {
                    Log.i(info,"user deleted!");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call,@NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void backupConversation(Conversation conversation,String uid)
    {
        api.backupConversation(conversation,uid).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {
               handleMessage(response);
            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void backupMessage(Message message,String uid)
    {
        api.backupMessage(message,uid).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {
               handleMessage(response);
            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {

            }
        });
    }

    public void backupConversations(List<Conversation>conversations,String uid)
    {
        api.backupConversations(conversations,uid).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {
                handleMessage(response);
                onBackupListener.onBackupCompleted(response.message());
            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
                t.printStackTrace();
                onBackupListener.onBackupFailed(t.getMessage());
            }
        });
    }

    public void backupMessages(List<Message>messages,String uid)
    {
        api.backupMessages(messages,uid).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {
                handleMessage(response);
            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void updateConversation(Conversation conversation,String uid)
    {
        api.updateConversation(conversation,uid).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {
                handleMessage(response);
            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void updateMessages(Message message,String uid)
    {
        api.updateMessage(message,uid).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {
                handleMessage(response);
            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void restoreConversations(String uid)
    {
        api.restoreConversations(uid).enqueue(new Callback<List<Conversation>>() {
            @Override
            public void onResponse(@NonNull Call<List<Conversation>> call,@NonNull Response<List<Conversation>> response) {
                if (response.body()!=null)
                    if (onRestore!=null)
                        onRestore.onConversationsRestored(response.body());
                    else
                        Log.e("error", "restore conversations listener is null");
            }

            @Override
            public void onFailure(@NonNull Call<List<Conversation>> call,@NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void restoreMessages(String uid)
    {
        api.restoreMessages(uid).enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(@NonNull Call<List<Message>> call,@NonNull Response<List<Message>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (onRestore!=null)
                            onRestore.onMessagesRestored(response.body());
                        else Log.e("error", "restore messages listener is null");
                    }
                }
                else
                    Log.e("error", "failed to restore messages, respond code: " + response.code());
            }

            @Override
            public void onFailure(@NonNull Call<List<Message>> call,@NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void handleMessage(Response<String>response)
    {
        if (response.isSuccessful())
        {
            if (response.code() == 200)
                Log.i(info,response.message());
            else
            {
                Log.e("error",response.message());
                Log.e("error",response.code()+"");
            }
        }
        else
        {
            Log.e("error",response.message());
            Log.e("error",response.code()+"");
        }
    }

    public void downloadFile(String fileName,long messageID)
    {
        if (fileDownload!=null)
        fileDownload.onDownloadStarted();
        api.downloadFile(fileName).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,@NonNull Response<ResponseBody> response) {
                if (response.isSuccessful())
                {
                    if (response.body()!=null) {
                        Thread networkThread = new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                try
                                {
                                    InputStream in = response.body().byteStream();
                                    File path = Environment.getExternalStorageDirectory();
                                    File file = new File(path, "/" + fileName);
                                    if(file.createNewFile()) {
                                        FileOutputStream outputStream = new FileOutputStream(file);
                                        BufferedInputStream bufferedInputStream = new BufferedInputStream(in);
                                        int byteRead;
                                        byte[] dataBuffer = new byte[1024];
                                        while ((byteRead = bufferedInputStream.read(dataBuffer, 0, 1024)) != -1)
                                            outputStream.write(dataBuffer, 0, byteRead);
                                        if (fileDownload != null)
                                            if (messageID == -1)
                                                fileDownload.onDownloadFinished(file);
                                            else
                                                fileDownload.onFileDownloadFinished(messageID, file);
                                        outputStream.close();
                                        bufferedInputStream.close();
                                    }
                                    in.close();
                                } catch(IOException e) {
                                    e.printStackTrace();
                                    if (fileDownload!=null)
                                    fileDownload.onDownloadError("couldn't download");
                                }
                            }

                        };
                        networkThread.setName("download image");
                        networkThread.start();
                    }
                    else
                    if (fileDownload!=null)
                        fileDownload.onDownloadError("body is null");
                }
                else
                if (fileDownload!=null)
                    fileDownload.onDownloadError("couldn't reach server");
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call,@NonNull Throwable t) {
                t.printStackTrace();
                if (fileDownload!=null)
                    fileDownload.onDownloadError("server is offline or response is malformed");
            }
        });
    }


    public void downloadImage(String userID)
    {
        downloadFile(userID,-1);
    }

    public void uploadFile(String uid,long msgID,Bitmap bitmap, Context context) {
        try {

            Log.d("started", "uploadFile: ");
            File file = new File(context.getCacheDir(), uid + "_" + System.currentTimeMillis());
            if (file.createNewFile()) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                byte[] bitmapdata = bos.toByteArray();
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();
                RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
                Log.d("about to", "uploadFile: ");
                fileUpload.onStartedUpload(msgID);
                fileTransmissionApi.uploadFile(body).enqueue(new Callback<Retrofit.Response>() {
                    @Override
                    public void onResponse(@NonNull Call<Retrofit.Response> call,@NonNull Response<Retrofit.Response> response) {
                        if (response.isSuccessful()) {
                            fileUpload.onUploadFinished(msgID);
                            if (response.body() != null)
                                fileUpload.onPathReady(msgID,response.body().getFileDownloadUri());
                            else
                                fileUpload.onUploadError(msgID,"sending file failed");
                        }
                        else
                            fileUpload.onUploadError(msgID,"sending file was not successful");
                    }

                    @Override
                    public void onFailure(@NonNull Call<Retrofit.Response> call,@NonNull Throwable t) {
                        t.printStackTrace();
                        fileUpload.onUploadError(msgID,t.getMessage());
                    }
                });
            }
        }catch (IOException e){e.printStackTrace();}
    }

    public void uploadFile(long msgID, Uri uri,Context context)
    {
        File file = new File(uri.getPath());
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
        fileUpload.onStartedUpload(msgID);
        fileTransmissionApi.uploadFile(body).enqueue(new Callback<Retrofit.Response>() {
            @Override
            public void onResponse(@NonNull Call<Retrofit.Response> call,@NonNull Response<Retrofit.Response> response) {
                if (response.isSuccessful()) {
                    fileUpload.onUploadFinished(msgID);
                    if (response.body() != null)
                        fileUpload.onPathReady(msgID,response.body().getFileDownloadUri());
                    else
                        fileUpload.onUploadError(msgID,"sending file failed");
                }
                fileUpload.onUploadError(msgID,"sending file was not successful");
            }

            @Override
            public void onFailure(@NonNull Call<Retrofit.Response> call,@NonNull Throwable t) {
                t.printStackTrace();
                fileUpload.onUploadError(msgID,t.getMessage());
            }
        });
    }
    public void searchUsers(String query)
    {
        api.searchUsers(query).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call,@NonNull Response<List<User>> response) {
                if (response.isSuccessful())
                    foundUsers.foundUsers(response.body());
                else
                    foundUsers.error(response.message());

            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call,@NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
