package Retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UploadFilesClient {

    private static Retrofit retrofit;
    private static final Type responseType = Response.class;
    public static Retrofit getRetrofitClientServer(String url)
    {
        if (retrofit == null)
        {
            Gson gson = new GsonBuilder().registerTypeAdapter(responseType,new ResponseBodyDeserializer()).setLenient().create();
            retrofit = new Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create(gson)).build();
        }

        return retrofit;
    }
}
