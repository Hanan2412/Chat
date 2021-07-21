package Retrofit;

import BackgroundDataToSend.DataToSend2;
import BackgroundDataToSend.InteractionToSend;
import BackgroundDataToSend.ReadToSend;
import BackgroundDataToSend.RequestToSend;
import BackgroundDataToSend.StatusToSend;
import NormalObjects.ObjectToSend;
import Try.TryMyResponse;
import Try.TrySender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface RetrofitApi {

    @Headers({"Content-Type:application/json","Authorization:key=AAAApuGdNCw:APA91bErXqemHj3mxeLP9UXapoxYbo5pVy1mcI5Pg2bpkNsw9lPMWqMtnw0PQCQHY369sl4q8iqyn_hBMbeBxhbosXEAjgkRXqvWDdeAAJQeRGroavmQ91Xqs-r2QZZx7siWUMcJfj3r"})
    @POST("fcm/send")
    Call<TryMyResponse>sendMessage(@Body ObjectToSend objectToSend);

    @Headers({"Content-Type:application/json","Authorization:key=AAAApuGdNCw:APA91bErXqemHj3mxeLP9UXapoxYbo5pVy1mcI5Pg2bpkNsw9lPMWqMtnw0PQCQHY369sl4q8iqyn_hBMbeBxhbosXEAjgkRXqvWDdeAAJQeRGroavmQ91Xqs-r2QZZx7siWUMcJfj3r"})
    @POST("fcm/send")
    Call<TryMyResponse>sendMessage(@Body RequestToSend requestToSend);

    @Headers({"Content-Type:application/json","Authorization:key=AAAApuGdNCw:APA91bErXqemHj3mxeLP9UXapoxYbo5pVy1mcI5Pg2bpkNsw9lPMWqMtnw0PQCQHY369sl4q8iqyn_hBMbeBxhbosXEAjgkRXqvWDdeAAJQeRGroavmQ91Xqs-r2QZZx7siWUMcJfj3r"})
    @POST("fcm/send")
    Call<TryMyResponse>sendMessage(@Body TrySender body);

    @Headers({"Content-Type:application/json","Authorization:key=AAAApuGdNCw:APA91bErXqemHj3mxeLP9UXapoxYbo5pVy1mcI5Pg2bpkNsw9lPMWqMtnw0PQCQHY369sl4q8iqyn_hBMbeBxhbosXEAjgkRXqvWDdeAAJQeRGroavmQ91Xqs-r2QZZx7siWUMcJfj3r"})
    @POST("fcm/send")
    Call<TryMyResponse>sendMessage(@Body InteractionToSend interaction);

    @Headers({"Content-Type:application/json","Authorization:key=AAAApuGdNCw:APA91bErXqemHj3mxeLP9UXapoxYbo5pVy1mcI5Pg2bpkNsw9lPMWqMtnw0PQCQHY369sl4q8iqyn_hBMbeBxhbosXEAjgkRXqvWDdeAAJQeRGroavmQ91Xqs-r2QZZx7siWUMcJfj3r"})
    @POST("fcm/send")
    Call<TryMyResponse>sendMessage(@Body StatusToSend status);

    @Headers({"Content-Type:application/json","Authorization:key=AAAApuGdNCw:APA91bErXqemHj3mxeLP9UXapoxYbo5pVy1mcI5Pg2bpkNsw9lPMWqMtnw0PQCQHY369sl4q8iqyn_hBMbeBxhbosXEAjgkRXqvWDdeAAJQeRGroavmQ91Xqs-r2QZZx7siWUMcJfj3r"})
    @POST("fcm/send")
    Call<TryMyResponse>sendMessage(@Body ReadToSend read);

    @Headers({"Content-Type:application/json","Authorization:key=AAAApuGdNCw:APA91bErXqemHj3mxeLP9UXapoxYbo5pVy1mcI5Pg2bpkNsw9lPMWqMtnw0PQCQHY369sl4q8iqyn_hBMbeBxhbosXEAjgkRXqvWDdeAAJQeRGroavmQ91Xqs-r2QZZx7siWUMcJfj3r"})
    @POST("fcm/send")
    Call<TryMyResponse>sendMessage(@Body DataToSend2 data);
}
