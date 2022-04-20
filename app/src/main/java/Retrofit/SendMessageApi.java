package Retrofit;

import Messages.SendActionMessage;
import Messages.SendErrorMessage;
import Messages.SendObject;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface SendMessageApi {

    @Headers({"Content-Type:application/json","Authorization:key=AAAApuGdNCw:APA91bErXqemHj3mxeLP9UXapoxYbo5pVy1mcI5Pg2bpkNsw9lPMWqMtnw0PQCQHY369sl4q8iqyn_hBMbeBxhbosXEAjgkRXqvWDdeAAJQeRGroavmQ91Xqs-r2QZZx7siWUMcJfj3r"})
    @POST("fcm/send")
    Call<FCMResponse>sendTextMessage(@Body SendObject sendMessage);
    @Headers({"Content-Type:application/json","Authorization:key=AAAApuGdNCw:APA91bErXqemHj3mxeLP9UXapoxYbo5pVy1mcI5Pg2bpkNsw9lPMWqMtnw0PQCQHY369sl4q8iqyn_hBMbeBxhbosXEAjgkRXqvWDdeAAJQeRGroavmQ91Xqs-r2QZZx7siWUMcJfj3r"})
    @POST("fcm/send")
    Call<FCMResponse>sendActionMessage(@Body SendActionMessage actionMessage);
    @Headers({"Content-Type:application/json","Authorization:key=AAAApuGdNCw:APA91bErXqemHj3mxeLP9UXapoxYbo5pVy1mcI5Pg2bpkNsw9lPMWqMtnw0PQCQHY369sl4q8iqyn_hBMbeBxhbosXEAjgkRXqvWDdeAAJQeRGroavmQ91Xqs-r2QZZx7siWUMcJfj3r"})
    @POST("fcm/send")
    Call<FCMResponse>sendErrorMessage(@Body SendErrorMessage errorMessage);
}
