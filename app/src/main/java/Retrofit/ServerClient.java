package Retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServerClient {

    private static Retrofit retrofit;

    public static Retrofit getRetrofitClientServer(String url)
    {
        if (retrofit == null)
            retrofit = new Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create()).build();
        return retrofit;
    }
}
