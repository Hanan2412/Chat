package NormalObjects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.lang.reflect.Type;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import Retrofit.*;
public class BackUp {

    private static Retrofit authWeb = null;
    private static Retrofit api = null;
    private static final Type JSON_TYPE = JSONObject.class;
    public static Retrofit getAuthWeb()
    {
        if (authWeb == null)
        {
            authWeb = new Retrofit.Builder().baseUrl("https://www.dropbox.com/").addConverterFactory(GsonConverterFactory.create()).build();
        }
        return authWeb;
    }
}
