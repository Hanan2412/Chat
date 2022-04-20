package Messages;

import android.util.Log;

import androidx.annotation.NonNull;

import Retrofit.FCMResponse;
import Retrofit.RetrofitClient;
import Retrofit.SendMessageApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendMessage {
    private final String NULL = "null";
    private final String INFO = "info";
    private final String ERROR = "error";

    private static SendMessage sendMessage;
    private SendMessageApi api;

    public static SendMessage getInstance()
    {
        if (sendMessage == null)
            sendMessage = new SendMessage();
        return sendMessage;
    }

    private SendMessage()
    {
        api = RetrofitClient.getRetrofitClient("https://fcm.googleapis.com/").create(SendMessageApi.class);
    }

    public void sendTextMessage(BaseMessage baseMessage, String... recipientsToken)
    {
        for (String token : recipientsToken){
            if (token == null)
                Log.e(NULL,"token is null - cant send message");
            else{
                SendObject sendObject = new SendObject(baseMessage, token);
                api.sendTextMessage(sendObject).enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<FCMResponse> call,@NonNull Response<FCMResponse> response) {
                        Log.i(INFO, "response code: " + response.code());
                        Log.i(INFO, "response message: " + response.message());
                        if (response.code() == 200)
                        {
                            if (response.body()!=null)
                                if (response.body().success!=1)
                                {
                                    Log.e(ERROR, "message wasn't sent");
                                    Log.e(ERROR, "Number of messages that could not be processed: " + response.body().failure);
                                    Log.e(ERROR, "Array of objects representing the status of the messages processed: " + response.body().results.toString());
                                }
                        }
                        else
                            Log.e(ERROR,"response code:" + response.code());
                    }

                    @Override
                    public void onFailure(@NonNull Call<FCMResponse> call,@NonNull Throwable t) {
                        t.printStackTrace();
                    }
                });
            }

        }
    }
}
