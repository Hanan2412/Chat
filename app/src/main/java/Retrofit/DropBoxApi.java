package Retrofit;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface DropBoxApi {

    @POST("oauth2/token/")
    Call<JSONObject>RequestToken(@Query("code") String authCode,@Query("grant_type") String authCodeString);
}
