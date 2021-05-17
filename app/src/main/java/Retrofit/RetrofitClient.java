package Retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//singleton retrofit object
public class RetrofitClient {
    private static Retrofit retrofit = null;
    public static Retrofit getRetrofitClient(String url){
        if(retrofit == null)
            retrofit = new Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create()).build();
        return retrofit;
    }
}
