package Retrofit;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.lang.reflect.Type;
import java.util.Map;

public class ResponseBodyDeserializer implements JsonDeserializer {
    @Override
    public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Response response = null;
        try{
            JsonObject jsonObject = json.getAsJsonObject();
            String fileName = jsonObject.get("fileName").getAsString();
            String fileDownloadUri = jsonObject.get("fileDownloadUri").getAsString();
            String fileType = jsonObject.get("fileType").getAsString();
            String size = jsonObject.get("size").getAsString();
            response = new Response();
            response.setFileDownloadUri(fileDownloadUri);
            response.setFileName(fileName);
            response.setFileType(fileType);
            response.setSize(size);
        }catch (JsonParseException e)
        {
            e.printStackTrace();
            Log.e("error","parsing didn't work");
        }
        return response;
    }
}
