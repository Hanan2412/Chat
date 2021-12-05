package Retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;

public interface RetrofitJoke {

    @Headers({"Content-Type:application/json"})
    @GET("/jokes/random")
    Call<Joke>sendJokeRequest();
}
