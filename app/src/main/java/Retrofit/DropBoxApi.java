package Retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DropBoxApi {


    @GET("oauth2/authorize/")
    Call<String> retrieveToken(@Query("client_id") String APP_KEY,@Query("response_type") String code);

}
