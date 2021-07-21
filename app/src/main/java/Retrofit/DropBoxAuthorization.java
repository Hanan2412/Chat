package Retrofit;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface DropBoxAuthorization {


    @GET("oauth2/authorize/")
    Call<String> authorize(@Query("client_id") String APP_KEY,@Query("response_type") String code);

}
