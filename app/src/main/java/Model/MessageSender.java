package Model;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.util.Log;
import androidx.annotation.NonNull;

import BroadcastReceivers.SMSBroadcastSent;
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

    public void sendMessage(Message message, String... recipientsTokens)
    {
        for (String token : recipientsTokens) {
            if(token!=null)
                Log.d("sending to token",token);
            else
                Log.e("null","message sender - token is null");
            ObjectToSend toSend = new ObjectToSend(message, message.getSenderToken());//for debug/testing reasons, change to token for regular operations
            api.sendMessage(toSend).enqueue(new Callback<TryMyResponse>() {
                @Override
                public void onResponse(@NonNull Call<TryMyResponse> call, @NonNull Response<TryMyResponse> response) {
                    Log.i(RETROFIT_INFO, "response code: " + response.code());
                    Log.i(RETROFIT_INFO, "response message: " + response.message());
                    if (response.code() == 200) {
                        assert response.body() != null;
                        if (response.body().success != 1) {
                            Log.e(RETROFIT_ERROR, "Couldn't send the message");
                            Log.e(RETROFIT_ERROR,"Number of messages that could not be processed: " + response.body().failure);
                            Log.e(RETROFIT_ERROR,"Array of objects representing the status of the messages processed: " + response.body().results.toString());
                        }
                    }
                }
                @Override
                public void onFailure(@NonNull Call<TryMyResponse> call, @NonNull Throwable t) {
                    Log.e(RETROFIT_ERROR, "retrofit failed!!!" + t.getMessage());
                    t.printStackTrace();
                }
            });
        }
    }

    //sends sms message
    public void sendMessage(Message message, String phoneNumber, Context context)
    {
        String scAddress = null;
        Intent intent = new Intent(context, SMSBroadcastSent.class);
        intent.putExtra("messageID",message.getMessageID());
        intent.putExtra("sending", "yes");
        PendingIntent sent = PendingIntent.getBroadcast(context, 182, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);
        Intent deliveredIntent = new Intent(context,SMSBroadcastSent.class);
        deliveredIntent.putExtra("delivered","yes");
        deliveredIntent.putExtra("messageID",message.getMessageID());
        PendingIntent delivered = PendingIntent.getBroadcast(context, 183, deliveredIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);
        //pendingIntent is to see if the sms message was sent and/or delivered
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, scAddress, message.getMessage(), sent, delivered);
    }


}
