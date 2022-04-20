package Retrofit;

import android.graphics.Bitmap;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

import java.util.List;
import java.util.Map;

import NormalObjects.Conversation;
import NormalObjects.Message;
import NormalObjects.User;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

public interface ServerConnect {

    @GET("/users")
    Call<List<User>>getAllUsers();

    @GET("/users/search/{name}")
    Call<List<User>>findUserByName(@Path("name") String name);

    @GET("/users/token/{id}")
    Call<Map<String,String>>getUserToken(@Path("id")String id);


    @GET("/users/{id}")
    Call<User>getUserById(@Path("id")String id);

    @GET("/users/status/{id}")
    Call<String>getUserStatus(@Path("id")String id);

    @PUT("/users/status")
    Call<Void>changeUserStatus(@Body User user);

    @POST("/users")
    Call<String>saveNewUser(@Body User user);

    @PUT("/users")
    Call<Void>updateUser(@Body User user);

    @PUT("users/token")
    Call<Void>saveUserToken(@Body User user);

    @DELETE("/users/{id}")
    Call<Void>deleteUser(@Path("id")String id);

    @POST("/backup/message")
    Call<String>backupMessage(@Body Message message,@Header("uid")String uid);

    @POST("/backup/conversation")
    Call<String>backupConversation(@Body Conversation conversation,@Header("uid")String uid);

    @POST("/backup/messages")
    Call<String>backupMessages(@Body List<Message>messages,@Header("uid")String uid);

    @POST("/backup/conversations")
    Call<String>backupConversations(@Body List<Conversation>conversations,@Header("uid")String uid);

    @PUT("backup/update/message")
    Call<String>updateMessage(@Body Message message,@Header("uid")String uid);

    @PUT("backup/update/conversation")
    Call<String>updateConversation(@Body Conversation conversation,@Header("uid")String uid);

    @GET("/restore/conversations")
    Call<List<Conversation>>restoreConversations(@Header("uid")String uid);

    @GET("/restore/messages")
    Call<List<Message>>restoreMessages(@Header("uid")String uid);

    @PUT("/user/token")
    Call<String>saveToken(@Body Map<String,String>map);

    @POST("/uploadFile/{image_id}")
    Call<String>uploadImage(@Body Bitmap image,@Path("image_id")String image_id);

    @GET("/downloadFile/{image_id}")
    Call<Bitmap>downloadImage(@Path("image_id")String image_id);

    @GET("/users/search/{name}")
    Call<List<User>>searchUsers(@Path("name")String name);

    @Multipart
    @POST("/uploadFile")
    Call<ResponseBody>uploadFile(@Part MultipartBody.Part file);

    @Streaming
    @GET("/downloadFile/{fileName}")
    Call<ResponseBody>downloadFile(@Path("fileName")String fileName);
}
