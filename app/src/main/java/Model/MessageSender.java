package Model;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.List;

import BroadcastReceivers.SMSBroadcastSent;
import NormalObjects.ConversationMessage;
import NormalObjects.ConversationObjectToSend;
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
    private final String RETROFIT_ERROR = "MessageSender Error";
    public interface onMessageSent {
        void onMessageSentSuccessfully(Message message);

        void onMessagePartiallySent(Message message, List<String> token, String error);

        void onMessageNotSent(Message message, String error);

    }

    private onMessageSent listener;

    public static MessageSender getInstance() {
        if (messageSender == null)
            messageSender = new MessageSender();
        return messageSender;
    }

    private MessageSender() {
        api = RetrofitClient.getRetrofitClient("https://fcm.googleapis.com/").create(RetrofitApi.class);
    }

    public void setMessageListener(onMessageSent listener) {
        this.listener = listener;
    }

    public void sendMessage(Message message, List<String> recipientsTokens) {
        for (String token : recipientsTokens) {
            if (token != null)
                Log.d("messageSender, sending to token: ", token);
            else
                Log.e("null", "message sender - token is null");
//            token = "f_wtCVbuTv-DQ9QupetiaZ:APA91bFdFmfFLW2sPcc7ixEh21WV5YPdvbKdxGrMR7olBLeiFxiMfAs4q3LecbHs7iYc_AwOaWWw1ylyigAtzyylbOptvRcv8dVYX3NbRi0_NgOexYsdCTM-I_JhsdTP-bB5mdoradX0";
            ObjectToSend toSend = new ObjectToSend(message, token);//for debug/testing reasons, change to token for regular operations
            api.sendMessage(toSend).enqueue(new Callback<TryMyResponse>() {
                @Override
                public void onResponse(@NonNull Call<TryMyResponse> call, @NonNull Response<TryMyResponse> response) {
                    Log.i(RETROFIT_INFO, "response code: " + response.code());
                    Log.i(RETROFIT_INFO, "response message: " + response.message());
                    if (response.code() == 200) {
                        assert response.body() != null;
                        if (response.body().success != 1) {
                            Log.e(RETROFIT_ERROR, "Couldn't send the message");
                            Log.e(RETROFIT_ERROR, "Number of messages that could not be processed: " + response.body().failure);
                            Log.e(RETROFIT_ERROR, "Array of objects representing the status of the messages processed: " + response.body().results.toString());
                            if (response.body().failure < recipientsTokens.size()) {
                                if (listener != null)
                                    listener.onMessagePartiallySent(message, recipientsTokens, response.message());
                            } else {
                                if (listener != null)
                                    listener.onMessageNotSent(message, response.message());
                            }
                        } else {
                            if (listener != null)
                            {
                                if (message.getMessageID() != -1)
                                    listener.onMessageSentSuccessfully(message);
                                else Log.e(RETROFIT_ERROR, "onResponse: message body is empty");
                            }
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
        if (recipientsTokens.isEmpty())
        {
            Log.e(RETROFIT_ERROR, "no tokens");
            if (listener!=null)
            {
                listener.onMessageNotSent(message, "server error - can't send msg");
            }
        }
    }

    //sends sms message
    public void sendMessage(Message message, String phoneNumber, Context context) {
        String scAddress = null;
        Intent intent = new Intent(context, SMSBroadcastSent.class);
        intent.putExtra("messageID", message.getMessageID());
        intent.putExtra("sending", "yes");
        PendingIntent sent = PendingIntent.getBroadcast(context, 182, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);
        Intent deliveredIntent = new Intent(context, SMSBroadcastSent.class);
        deliveredIntent.putExtra("delivered", "yes");
        deliveredIntent.putExtra("messageID", message.getMessageID());
        PendingIntent delivered = PendingIntent.getBroadcast(context, 183, deliveredIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT);
        //pendingIntent is to see if the sms message was sent and/or delivered
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, scAddress, message.getContent(), sent, delivered);
    }

    public void sendMessage(ConversationMessage message, List<String>recipientsTokens)
    {
        for (String token: recipientsTokens)
        {
            ConversationObjectToSend objectToSend = new ConversationObjectToSend(message, token);
            api.sendMessage(objectToSend).enqueue(new Callback<TryMyResponse>() {
                @Override
                public void onResponse(@NonNull Call<TryMyResponse> call, @NonNull Response<TryMyResponse> response) {
                    if (response.isSuccessful())
                    {
                        Log.d(RETROFIT_INFO, "conversationMessage was sent successfully to token: " + token);
                        if (response.body() != null)
                        {
                            Log.d(RETROFIT_INFO, "conversationMessage body response not null");
                            if (response.body().success != 1)
                            {
                                Log.e(RETROFIT_ERROR, "conversationMessage wasn't sent to token: " + token);
                            }
                            else
                            {
                                Log.d(RETROFIT_INFO, "conversationMessage was sent successfully to token: " + token);
                            }
                        }
                    }
                    else
                        Log.e(RETROFIT_ERROR, "conversationMessage response code wasn't successful for token: " + token);
                }

                @Override
                public void onFailure(@NonNull Call<TryMyResponse> call, @NonNull Throwable t) {
                    Log.e(RETROFIT_ERROR, "sending conversationMessage error");
                    t.printStackTrace();
                }
            });
        }
    }

}
