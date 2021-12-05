package Retrofit;

import java.util.HashMap;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//singleton retrofit object
public class RetrofitClient {

    private static HashMap<String,Retrofit>retrofits;
    public static Retrofit getRetrofitClient(String url){
        if (retrofits == null)//initialize the retrofit hashmap
            retrofits = new HashMap<>();
        if (retrofits.containsKey(url))//fetches the correct retrofit object if exists
            return retrofits.get(url);
        else
        {   //creates new retrofit object if it doesn't exist already and saves it in the hashmap
            Retrofit retrofit = new Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create()).build();
            retrofits.put(url, retrofit);
            return retrofit;
        }
    }
}
