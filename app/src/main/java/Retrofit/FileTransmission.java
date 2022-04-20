package Retrofit;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

public interface FileTransmission {
    @Multipart
    @POST("/uploadFile")
    Call<Response> uploadFile(@Part MultipartBody.Part file);

    @Streaming
    @GET("/downloadFile/{fileName}")
    Call<Response>downloadFile(@Path("fileName")String fileName);
}
