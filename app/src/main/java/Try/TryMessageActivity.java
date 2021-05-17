package Try;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.example.woofmeow.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import Retrofit.RetrofitApi;
import Retrofit.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TryMessageActivity extends AppCompatActivity {

    private boolean notify;
    private String userID,currentUserID;
    private Intent intent;
    private FirebaseUser currentFirebaseUser;
    private RetrofitApi api;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        intent = getIntent();
        //userID = intent.getStringExtra("userID");
        currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentFirebaseUser.getUid().equals("pfghXKWGCja8i8YPQz71DuXxyTI2"))
            userID = "knffgLWH6gfY6wMdsxCujSsaipI2";//this is user aaa
        else
            userID = "pfghXKWGCja8i8YPQz71DuXxyTI2";///this is user bbb

        ImageButton button  = findViewById(R.id.btn_send);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                sendMessage(currentFirebaseUser.getUid(),userID,"this is the message to send");
            }
        });
        api = RetrofitClient.getRetrofitClient("https://fcm.googleapis.com/").create(RetrofitApi.class);
    }

    private void sendMessage(String sender,String receiver,String message)
    {
        HashMap<String,Object>hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isSeen", false);
        //updates database with the message

        if (notify)
            sendNotification(receiver,"currentUserName",message);
        notify = false;
    }

    private void sendNotification(String receiver, final String sender, final String message)
    {
        /*
        all the tokens are stored in a token path directory in the database and not in the user the token assigned to.
        the tokens are stored as a key-value pair when the key is the users uid and the value is the token itself
         */
        DatabaseReference tokenReference = FirebaseDatabase.getInstance().getReference("Tokens");
        Query tokensQuery = tokenReference.orderByKey().equalTo(receiver);
        tokensQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    String tokenString = dataSnapshot.getValue(String.class);
                    TryToken tryToken = new TryToken(tokenString);
                    TryData tryData = new TryData(currentFirebaseUser.getUid(),message,"new message from" + sender,userID);
                    TrySender trySender = new TrySender(tryData,tryToken.getToken());
                    api.sendMessage(trySender).enqueue(new Callback<TryMyResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<TryMyResponse> call, @NonNull Response<TryMyResponse> response) {
                            if(response.code() == 200) {
                                assert response.body() != null;
                                if (response.body().success != 1)
                                    Toast.makeText(TryMessageActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<TryMyResponse> call, Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
