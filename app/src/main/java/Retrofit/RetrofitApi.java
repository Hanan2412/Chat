package Retrofit;

import NormalObjects.ObjectToSend;
import Try.TryMyResponse;
import Try.TrySender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RetrofitApi {

    @Headers({"Content-Type:application/json","Authorization:key=AAAApuGdNCw:APA91bErXqemHj3mxeLP9UXapoxYbo5pVy1mcI5Pg2bpkNsw9lPMWqMtnw0PQCQHY369sl4q8iqyn_hBMbeBxhbosXEAjgkRXqvWDdeAAJQeRGroavmQ91Xqs-r2QZZx7siWUMcJfj3r"})
    @POST("fcm/send")
    Call<TryMyResponse>sendMessage(@Body ObjectToSend objectToSend);

    @Headers({"Content-Type:application/json","Authorization:key=AAAApuGdNCw:APA91bErXqemHj3mxeLP9UXapoxYbo5pVy1mcI5Pg2bpkNsw9lPMWqMtnw0PQCQHY369sl4q8iqyn_hBMbeBxhbosXEAjgkRXqvWDdeAAJQeRGroavmQ91Xqs-r2QZZx7siWUMcJfj3r"})
    @POST("fcm/send")
    Call<TryMyResponse>sendMessage(@Body TrySender body);

}
