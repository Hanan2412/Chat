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
import android.widget.Toast;

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

import Adapters.NewGroupChatAdapter;
import Adapters.UsersAdapter2;
//import Controller.CController;
import Backend.UserVM;
import Fragments.SingleFieldFragment;
import Model.Server3;
import NormalObjects.User;
import Retrofit.Server;

@SuppressWarnings("Convert2Lambda")
public class NewGroupChat2Activity extends AppCompatActivity{

    private NewGroupChatAdapter groupChatAdapter;
    private UsersAdapter2 usersAdapter;
    private String currentUser;
    private UserVM userVM;
    private List<User>existingUsers;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_group);
        existingUsers = new ArrayList<>();
        userVM = new ViewModelProvider(this).get(UserVM.class);
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        RecyclerView groupList = findViewById(R.id.groupList);
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        groupList.setLayoutManager(manager);
        groupChatAdapter = new NewGroupChatAdapter();
        groupList.setAdapter(groupChatAdapter);
        if (getIntent().hasExtra("recipients"))
        {
            List<User>users = (ArrayList<User>) getIntent().getSerializableExtra("recipients");
            groupChatAdapter.setUsers(users);
            existingUsers = users;
        }
        usersAdapter = new UsersAdapter2();
        TextView groupCount = findViewById(R.id.groupCount);
        ListView searchList = findViewById(R.id.searchList);
        searchList.setAdapter(usersAdapter);
        searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!groupChatAdapter.isUserExists(usersAdapter.getItem(position).getUserUID())) {
                    groupChatAdapter.addUser(usersAdapter.getItem(position));
                    usersAdapter.removeUser(position);
                    String count = groupChatAdapter.getItemCount() + "";
                    groupCount.setText(count);
                }
                else{
                    Toast.makeText(NewGroupChat2Activity.this, "can't add the same user more than once to a group", Toast.LENGTH_SHORT).show();
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
        TextInputEditText searchUsers = findViewById(R.id.searchUsers);
        ImageButton searchBtn = findViewById(R.id.searchBtn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usersAdapter.clear();
                if (searchUsers.getText() != null) {
                    String search = searchUsers.getText().toString();
                    if (search != null) {
                        userVM.searchForUsers(search);
                    }
                }
            }
        });
        userVM.setOnUserFoundListener(new Server.onUsersFound() {
            @Override
            public void foundUsers(List<User> users) {
                for(User user:users)
                {
                    if (!user.getUserUID().equals(currentUser))
                        if(!groupChatAdapter.isUserExists(user.getUserUID()))
                            usersAdapter.addUser(user);
                }
            }

            @Override
            public void error(String errorMessage) {
                Log.e("ERROR","error finding users");
            }
        });
//        userVM.setOnUsersFoundListener(new Server3.onUserFound() {
//            @Override
//            public void foundUser(User user) {
//                if (!user.getUserUID().equals(currentUser))
//                    if(!groupChatAdapter.isUserExists(user.getUserUID()))
//                        usersAdapter.addUser(user);
//            }
//        });
        Button start = findViewById(R.id.start_chatting);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (existingUsers.isEmpty()) {
                    SingleFieldFragment fragment = new SingleFieldFragment();
                    fragment.setListener(new SingleFieldFragment.onName() {
                        @Override
                        public void onGroupName(String name) {
                            Intent startConversation = new Intent(NewGroupChat2Activity.this, ConversationActivity.class);
                            startConversation.putExtra("group", (ArrayList<User>) groupChatAdapter.getUsers());
                            startConversation.putExtra("groupName", name);
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
                else
                {
                    Intent startConversation = new Intent(NewGroupChat2Activity.this, ConversationActivity.class);
                    startConversation.putExtra("group", (ArrayList<User>) groupChatAdapter.getUsers());
                    setResult(RESULT_OK,startConversation);
                    finish();
                }
            }
        });
        groupChatAdapter.setListener(new NewGroupChatAdapter.onItemTouchListener() {
            @Override
            public void onItemClick(int position) {
                User user = groupChatAdapter.getItem(position);
                if (!existingUsers.contains(user)) {
                    groupChatAdapter.removeUser(position);
                    usersAdapter.addUser(user);
                    if (groupChatAdapter.getItemCount() == 0)
                        groupCount.setText("");
                    else {
                        String count = groupChatAdapter.getItemCount() + "";
                        groupCount.setText(count);
                    }
                }
            }
        });
    }

    private String createGroupConversationID() {
        return "G_" + System.currentTimeMillis();
    }
}
