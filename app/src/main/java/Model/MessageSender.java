package Model;

import android.util.Log;
import androidx.annotation.NonNull;

import BackgroundDataToSend.InteractionToSend;
import BackgroundDataToSend.ReadToSend;
import BackgroundDataToSend.RequestToSend;
import BackgroundMessages.InteractionMessage;
import BackgroundMessages.ReadMessage;
import BackgroundMessages.*;
import Consts.BackgroundMessages;
import NormalObjects.Message;
import NormalObjects.ObjectToSend;
import Retrofit.RetrofitApi;
import Retrofit.RetrofitClient;
import Try.TryMyResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//singleton class for sending a message
public class MessageSender {

    private static MessageSender messageSender;
    @SuppressWarnings("FieldMayBeFinal")
    private RetrofitApi api;
    private final String RETROFIT_INFO = "info";
    private final String RETROFIT_ERROR = "error";

    public static MessageSender getInstance(){
        if (messageSender == null)
            messageSender = new MessageSender();
        return messageSender;
    }

    private MessageSender()
    {
        api = RetrofitClient.getRetrofitClient("https://fcm.googleapis.com/").create(RetrofitApi.class);
    }

    public void SendMessage(Message message,String... recipientsTokens)
    {
        for (String token : recipientsTokens) {
            ObjectToSend toSend = new ObjectToSend(message, token);
            api.sendMessage(toSend).enqueue(new Callback<TryMyResponse>() {
                @Override
                public void onResponse(@NonNull Call<TryMyResponse> call, @NonNull Response<TryMyResponse> response) {
                    Log.i(RETROFIT_INFO, "response code: " + response.code());
                    Log.i(RETROFIT_INFO, "response message: " + response.message());
                    if (response.code() == 200) {
                        assert response.body() != null;
                        if (response.body().success != 1) {
                            Log.e(RETROFIT_ERROR, "Couldn't send the message");
                        }
                    }
                }
                @Override
                public void onFailure(@NonNull Call<TryMyResponse> call, @NonNull Throwable t) {
                    Log.e(RETROFIT_ERROR, "retrofit failed!!!");
                }
            });
        }
    }

    public void SendMessage(@NonNull Object message, BackgroundMessages messageType, @NonNull String... recipients)
    {
        switch (messageType)
        {
            case read:
                ReadMessage readMessage = (ReadMessage)message;
                for (String token : recipients) {
                    ReadToSend readToSend = new ReadToSend(readMessage, token);
                    api.sendMessage(readToSend).enqueue(new Callback<TryMyResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<TryMyResponse> call,@NonNull Response<TryMyResponse> response) {
                            Log.i(RETROFIT_INFO, "dataMessage response code: " + response.code());
                            Log.i(RETROFIT_INFO, "dataMessage response message: " + response.message());
                            if (response.code() == 200) {
                                assert response.body() != null;
                                if (response.body().success != 1) {
                                    Log.e(RETROFIT_ERROR, "Couldn't send the dataMessage");
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<TryMyResponse> call,@NonNull Throwable t) {
                            Log.e(RETROFIT_ERROR, "retrofit failed!!! - dataMessage");
                        }
                    });
                }
                break;
            case interaction:
                InteractionMessage interactionMessage = (InteractionMessage)message;
                for (String token : recipients)
                {
                    InteractionToSend interactionToSend = new InteractionToSend(interactionMessage,token);
                    api.sendMessage(interactionToSend).enqueue(new Callback<TryMyResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<TryMyResponse> call,@NonNull Response<TryMyResponse> response) {
                            Log.i(RETROFIT_INFO, "dataMessage response code: " + response.code());
                            Log.i(RETROFIT_INFO, "dataMessage response message: " + response.message());
                            if (response.code() == 200) {
                                assert response.body() != null;
                                if (response.body().success != 1) {
                                    Log.e(RETROFIT_ERROR, "Couldn't send the dataMessage");
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<TryMyResponse> call,@NonNull Throwable t) {
                            Log.e(RETROFIT_ERROR, "retrofit failed!!! - dataMessage");
                        }
                    });
                }
                break;
            case status:

                break;
            case request:
                RequestMessage requestMessage = (RequestMessage)message;
                for (String token : recipients)
                {
                    RequestToSend requestToSend = new RequestToSend(requestMessage,token);
                    api.sendMessage(requestToSend).enqueue(new Callback<TryMyResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<TryMyResponse> call,@NonNull Response<TryMyResponse> response) {
                            Log.i(RETROFIT_INFO, "dataMessage response code: " + response.code());
                            Log.i(RETROFIT_INFO, "dataMessage response message: " + response.message());
                            if (response.code() == 200) {
                                assert response.body() != null;
                                if (response.body().success != 1) {
                                    Log.e(RETROFIT_ERROR, "Couldn't send the dataMessage");
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<TryMyResponse> call,@NonNull Throwable t) {
                            Log.e(RETROFIT_ERROR, "retrofit failed!!! - dataMessage");
                        }
                    });
                }
                break;
        }
    }


}
