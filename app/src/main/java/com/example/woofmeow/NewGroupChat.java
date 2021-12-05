package com.example.woofmeow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;


import java.util.ArrayList;

import Adapters.UsersAdapter;
import Controller.CController;
import Fragments.GeneralFragment;
import Fragments.SingleFieldFragment;
import NormalObjects.User;


@SuppressWarnings("Convert2Lambda")
public class NewGroupChat extends AppCompatActivity implements FoundUsers{

    private UsersAdapter adapter;
    private final CController controller;
    private ArrayList<User>group;
    private String currentUser;

    public NewGroupChat()
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
        talk.setVisibility(View.GONE);
        adapter = new UsersAdapter();
        group = new ArrayList<>();
        adapter.setListener(new UsersAdapter.startConversation() {
            @Override
            public void onStart(User user) {
                startSingleConversation(user);
            }

            @Override
            public void onAddToGroup(User user) {
                group.add(user);
                talk.setVisibility(View.VISIBLE);
            }

            @Override
            public void onRemoveFromGroup(User user) {
                group.remove(user);
                if (group.isEmpty())
                    talk.setVisibility(View.GONE);
            }
        });
        usersList.setAdapter(adapter);
        talk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (group.isEmpty())
                    Toast.makeText(NewGroupChat.this, "can't start conversation without any recipients", Toast.LENGTH_SHORT).show();
                else if (group.size() == 1)
                    startSingleConversation(group.get(0));
                else {
                    SingleFieldFragment fragment = new SingleFieldFragment();
                    fragment.setListener(new SingleFieldFragment.onName() {
                        @Override
                        public void onGroupName(String name) {
                            Intent startConversation = new Intent(NewGroupChat.this, ConversationActivity.class);
                            startConversation.putExtra("group", group);
                            startConversation.putExtra("groupName",name);
                            startConversation.putExtra("conversationID", createGroupConversationID());
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            transaction.remove(fragment).commit();
                            startActivity(startConversation);
                            finish();
                        }
                    });

                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.add(R.id.mainLayout, fragment, "groupName");
                    transaction.addToBackStack(null);
                    transaction.commit();


                }
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
                        controller.onFindUsersQuery(search, NewGroupChat.this);
                    }
                }
            }
        });
       /* usersList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.changeSelection(position,view);
                return true;
            }
        });*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        controller.removeInterface(6);
    }

    private void startSingleConversation(User user)
    {
        Intent openConversationIntent = new Intent(NewGroupChat.this, ConversationActivity.class);
        openConversationIntent.putExtra("recipientUser",user);
        openConversationIntent.putExtra("conversationID", createConversationID());
        startActivity(openConversationIntent);
        finish();
    }

    private String createConversationID()
    {
        return "C_" + System.currentTimeMillis();
    }

    private String createGroupConversationID(){return "G_" + System.currentTimeMillis();}

    @Override
    public void onUserFound(User user) {
        if(!user.getUserUID().equals(currentUser))
            adapter.addUser(user);
    }
}

 /*private boolean isSelectedUser(String uid)
    {
        for(int i = 0;i<group.size();i++)
        {
            User user = group.get(i);
            if(user.getUserUID().equals(uid))
                return true;
        }
        return false;
    }*/
 /* DatabaseReference tokensReference = FirebaseDatabase.getInstance().getReference("Tokens");
                tokensReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren())
                        {
                            String uid = dataSnapshot.getKey();
                            if(isSelectedUser(uid))
                            {
                                tokens.add((String)dataSnapshot.getValue());
                            }
                        }
                        Intent startConversation = new Intent(NewGroupChat.this,ConversationActivity.class);
                        startConversation.putExtra("group",group);
                        startConversation.putExtra("groupTokens",tokens);
                        if(group.size()>1)
                            startConversation.putExtra("conversationID",createGroupConversationID());
                        else
                            startConversation.putExtra("conversationID", createConversationID());
                        tokensReference.removeEventListener(this);
                        startActivity(startConversation);
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase Error","newGroupChat: cancelled token retrieval");
                    }
                });*/