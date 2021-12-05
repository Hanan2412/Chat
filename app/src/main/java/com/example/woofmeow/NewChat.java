package com.example.woofmeow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import Adapters.UsersAdapter;
import Controller.CController;
import NormalObjects.User;


@SuppressWarnings("Convert2Lambda")
public class NewChat extends AppCompatActivity implements FoundUsers{

    private UsersAdapter adapter;
    private final CController controller;
    private String currentUser;
    private final String SINGLE_CHAT = "newSingleChat";

    public NewChat()
    {
        if(FirebaseAuth.getInstance().getCurrentUser()!=null)
            currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        controller = CController.getController();
        controller.setFoundUsers(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_chat2);
        TextInputEditText searchUsers = findViewById(R.id.searchUsers);
        ImageButton searchBtn = findViewById(R.id.searchBtn);
        ListView usersList = findViewById(R.id.searchList);
        Button talk = findViewById(R.id.talk);
        adapter = new UsersAdapter();
        /*adapter.setSingleListener(new UsersAdapter.onSingle() {
            @Override
            public void onTalkTo(User user) {
                DatabaseReference tokenReference = FirebaseDatabase.getInstance().getReference("Tokens");
                Query tokensQuery = tokenReference.orderByKey().equalTo(user.getUserUID());//here the tokens that were retrieved are ordered by the key - which is equal to the recipients UID
                tokensQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Log.d(SINGLE_CHAT,"starting single conversation");
                            String token =  dataSnapshot.getValue(String.class);
                            Toast.makeText(NewChat.this, "starting conversation", Toast.LENGTH_SHORT).show();
                            Intent openConversationIntent = new Intent(NewChat.this, ConversationActivity.class);
                            openConversationIntent.putExtra("recipientUser",user);
                            openConversationIntent.putExtra("recipient",user.getUserUID());
                            openConversationIntent.putExtra("recipientToken",token);
                            openConversationIntent.putExtra("conversationID",CreateConversationID());
                            tokensQuery.removeEventListener(this);
                            startActivity(openConversationIntent);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FireBase Error", "cancelled firebase - didn't retrieve token");
                    }
                });
            }
        });*/
        talk.setVisibility(View.GONE);
        usersList.setAdapter(adapter);
        Button cancelBtn = findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.ClearData();
                if(searchUsers.getText()!=null) {
                    String search = searchUsers.getText().toString();
                    if (search != null) {
                        controller.onFindUsersQuery(search, NewChat.this);
                    }
                }
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        controller.removeInterface(6);
    }

    private String CreateConversationID()
    {
        return "C_" + System.currentTimeMillis();
    }

    @Override
    public void onUserFound(User user) {
        if(!user.getUserUID().equals(currentUser))
            adapter.addUser(user);
        else
            Log.d(SINGLE_CHAT,"ignoring current user - wont display my self to talk to");
    }



    private void prepareConversation()
    {

    }
}
