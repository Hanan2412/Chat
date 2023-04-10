package com.example.woofmeow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import Adapters.NewConversationAdapter;
import Adapters.UsersAdapter2;
import Backend.UserVM;
import Fragments.SingleFieldFragment;
import NormalObjects.User;
import Retrofit.Server;

@SuppressWarnings("Convert2Lambda")
public class NewConversationActivity extends AppCompatActivity {

    private String currentUser;
    private final String NEW_CONVERSATION_ACTIVITY = "NEW_CONVERSATION_ACTIVITY";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_conversation);
        TextView groupCount = findViewById(R.id.groupCount);
        ListView searchList = findViewById(R.id.searchList);
        RecyclerView recipients = findViewById(R.id.groupList);
        TextInputEditText searchUsers = findViewById(R.id.searchUsers);
        ImageButton searchBtn = findViewById(R.id.searchBtn);
        Button cancelBtn = findViewById(R.id.cancelBtn);
        Button start = findViewById(R.id.start_chatting);

        UsersAdapter2 usersAdapter = new UsersAdapter2();
        NewConversationAdapter newConversationAdapter = new NewConversationAdapter();
        UserVM userVM = new ViewModelProvider(this).get(UserVM.class);
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();


        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recipients.setLayoutManager(manager);
        recipients.setAdapter(newConversationAdapter);
        searchList.setAdapter(usersAdapter);

        searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(NEW_CONVERSATION_ACTIVITY, "tapped on recipient");
                if (!newConversationAdapter.isUserExists(usersAdapter.getItem(position).getUserUID())) {
                    Log.d(NEW_CONVERSATION_ACTIVITY, "added recipient to list");
                    newConversationAdapter.addUser(usersAdapter.getItem(position));
                    usersAdapter.removeUser(position);
                    String count = newConversationAdapter.getItemCount() + "";
                    groupCount.setText(count);
                } else {
                    Log.e(NEW_CONVERSATION_ACTIVITY, "the same user appeared twice in search");
                }
            }
        });
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(NEW_CONVERSATION_ACTIVITY, "tapped on search");

                usersAdapter.clear();
                if (searchUsers.getText() != null) {
                    String search = searchUsers.getText().toString();
                    userVM.searchForUsers(search);
                }
                User user = new User();
                user.setUserUID("123456789");
                user.setName("Hope");
                user.setLastName("Yentis");
                user.setTimeCreated(System.currentTimeMillis() + "");
                user.setBlocked(false);
                user.setMuted(false);
                user.setToken("987654321");
                usersAdapter.addUser(user);
                User user1 = new User();
                user1.setName("Hope");
                user1.setLastName("Yentis");
                user1.setTimeCreated(System.currentTimeMillis() + "");
                user1.setBlocked(false);
                user1.setMuted(false);
                user1.setToken("987654321");
                user1.setUserUID("1234567890");
                usersAdapter.addUser(user1);
                User user2 = new User();
                user2.setName("Hope");
                user2.setLastName("Yentis");
                user2.setTimeCreated(System.currentTimeMillis() + "");
                user2.setBlocked(false);
                user2.setMuted(false);
                user2.setToken("987654321");
                user2.setUserUID("12345678901");
                usersAdapter.addUser(user2);
            }
        });
        userVM.setOnUserFoundListener(new Server.onUsersFound() {
            @Override
            public void foundUsers(List<User> users) {
                for (User user : users) {
                    if (!user.getUserUID().equals(currentUser))
                        if (!newConversationAdapter.isUserExists(user.getUserUID()))
                            usersAdapter.addUser(user);
                }
            }

            @Override
            public void error(String errorMessage) {
                Log.e(NEW_CONVERSATION_ACTIVITY, "error finding users");
            }
        });
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getIntent().hasExtra("additional_recipients"))
                {
                    Intent startConversation = new Intent(NewConversationActivity.this, ConversationActivity2.class);
                    startConversation.putExtra("group", (ArrayList<User>) newConversationAdapter.getUsers());
                    setResult(RESULT_OK,startConversation);
                    finish();
                }
                else if (newConversationAdapter.getItemCount() == 1)
                {
                    startConversation(newConversationAdapter.getItem(0).getName(), newConversationAdapter.getUsers());
                    finish();
                }
                else
                {
                    SingleFieldFragment fragment = new SingleFieldFragment();
                    fragment.setListener(new SingleFieldFragment.onText() {
                        @Override
                        public void onTextChange(String name) {
                            Log.d(NEW_CONVERSATION_ACTIVITY, "starting group conversation");
                            startConversation(name, newConversationAdapter.getUsers());
                            fragment.dismiss();
                            finish();
                        }
                    });
                    fragment.setHint(getResources().getString(R.string.conversation_name));
                    fragment.show(getSupportFragmentManager(), "group name");

                }
//                else
//                {
//                    Intent startConversation = new Intent(NewConversationActivity.this, ConversationActivity2.class);
//                    startConversation.putExtra("group", (ArrayList<User>) newConversationAdapter.getUsers());
//                    setResult(RESULT_OK,startConversation);
//                    finish();
//                }
            }
        });
        newConversationAdapter.setListener(new NewConversationAdapter.onItemTouchListener() {
            @Override
            public void onItemClick(int position) {
                User user = newConversationAdapter.getItem(position);
                newConversationAdapter.removeUser(position);
                usersAdapter.addUser(user);
                String count = newConversationAdapter.getItemCount() + "";
                groupCount.setText(count);
            }
        });


        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private String createGroupConversationID() {
        return "G_" + System.currentTimeMillis();
    }

    private void startConversation(String conversationName, List<User>users)
    {
        Intent startConversation = new Intent(NewConversationActivity.this, ConversationActivity2.class);
        startConversation.putExtra("recipients", (ArrayList<User>) users);
        startConversation.putExtra("conversationName", conversationName);
        startConversation.putExtra("conversationID", createGroupConversationID());
        startActivity(startConversation);
    }
}
