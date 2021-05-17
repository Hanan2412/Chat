package BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import Consts.MessageType;
import NormalObjects.Message;
import NormalObjects.ObjectToSend;
import NormalObjects.Server;
import Retrofit.RetrofitApi;
import Retrofit.RetrofitClient;


public class AlarmReceiverBroadcast extends BroadcastReceiver {

    private RetrofitApi api;

    @Override
    public void onReceive(Context context, Intent intent) {
        String recipient = intent.getStringExtra("recipient");
        String currentUser = intent.getStringExtra("sender");
        String currentUserName = intent.getStringExtra("senderName");
        String messageToSend = intent.getStringExtra("messageToSend");
        String conversationID = intent.getStringExtra("conversationID");
        api = RetrofitClient.getRetrofitClient("https://fcm.googleapis.com/").create(RetrofitApi.class);
        //sendMessage(MessageType.textMessage.ordinal(),recipient,currentUser,currentUserName,messageToSend,conversationID,context);

    }



}
