package com.example.woofmeow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import Adapters.UsersAdapter;
import Controller.CController;
import NormalObjects.User;

@SuppressWarnings("Convert2Lambda")
public class NewChat extends AppCompatActivity implements FoundUsers{

    private UsersAdapter adapter;
    private final CController controller;
    private ArrayList<User> group;
    private String currentUser;
    private final String NEW_CHAT = "New Chat";
    public NewChat() {
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
        talk.setVisibility(View.GONE);
        adapter = new UsersAdapter();
        adapter.setSingle(true);
        group = new ArrayList<>();
        adapter.setListener(new UsersAdapter.startConversation() {
            @Override
            public void onStart(User user) {
                startSingleConversation(user);
            }

            @Override
            public void onAddToGroup(User user) {
               Log.e(NEW_CHAT,"trying to add to a group");
            }

            @Override
            public void onRemoveFromGroup(User user) {
                Log.e(NEW_CHAT,"trying to remove to a group");
            }
        });
        usersList.setAdapter(adapter);
        talk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (group.isEmpty())
                    Toast.makeText(NewChat.this, "can't start conversation without any recipients", Toast.LENGTH_SHORT).show();
                else if (group.size() == 1)
                    startSingleConversation(group.get(0));
                else
                    Log.e(NEW_CHAT, "trying to create a group chat in single chat");
            }
        });

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
        controller.setFoundUsers(null);
        controller.removeInterface(6);
    }

    private void startSingleConversation(User user)
    {
        Intent openConversationIntent = new Intent(NewChat.this, ConversationActivity.class);
        openConversationIntent.putExtra("recipientUser",user);
        openConversationIntent.putExtra("conversationID", createConversationID());
        startActivity(openConversationIntent);
        finish();
    }

    private String createConversationID()
    {
        return "C_" + System.currentTimeMillis();
    }

    @Override
    public void onUserFound(User user) {
        if(!user.getUserUID().equals(currentUser))
            adapter.addUser(user);
    }
}
