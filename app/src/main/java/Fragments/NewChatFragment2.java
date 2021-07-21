package Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.woofmeow.ConversationActivity;
import com.example.woofmeow.ProfileActivity2;
import com.example.woofmeow.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import Adapters.UserListAdapter;
import NormalObjects.User;

@SuppressWarnings("Convert2Lambda")
public class NewChatFragment2 extends Fragment implements UserListAdapter.userListInterface {

    private UserListAdapter adapter;
    private final String currentUser = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    public interface NewChat2{
        void onNewQuery(String query);
    }
    private NewChat2 callback;

    public static NewChatFragment2 newInstance() {

        Bundle args = new Bundle();
        NewChatFragment2 fragment = new NewChatFragment2();
        fragment.setArguments(args);
        return fragment;
    }

    private NewChatFragment2() {
        super();
    }

    @Override
    public void setArguments(@Nullable Bundle args) {
        super.setArguments(args);
        if(args!=null && args.containsKey("user"))
        {
            User user = (User) args.getSerializable("user");
            adapter.addUser(user);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            callback = (NewChat2) context;
        }catch (ClassCastException e)
        {
            throw new ClassCastException("activity must implement NewChat2 interface");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_chat,container,false);
        TextInputEditText searchUsers = view.findViewById(R.id.searchUsers);
        ImageButton searchBtn = view.findViewById(R.id.searchBtn);
        ListView usersList = view.findViewById(R.id.searchList);
        adapter = new UserListAdapter();
        adapter.setListener(this);
        usersList.setAdapter(adapter);
        Button cancelBtn = view.findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getSupportFragmentManager().beginTransaction().remove(NewChatFragment2.this).commit();
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sends information to server
                adapter.ClearData();
                if(searchUsers.getText()!=null)
                    callback.onNewQuery(searchUsers.getText().toString());
            }
        });

        return view;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStartConversation(User user) {
        //opens conversationActivity
        Toast.makeText(requireContext(), "starting conversation", Toast.LENGTH_SHORT).show();
        Intent openConversationIntent = new Intent(requireContext(), ConversationActivity.class);
        openConversationIntent.putExtra("recipient",user.getUserUID());
        //openConversationIntent.putExtra("conversationID",currentUser + "   " + user.getUserUID());
        openConversationIntent.putExtra("conversationID",CreateConversationID());
        requireActivity().getSupportFragmentManager().beginTransaction().remove(NewChatFragment2.this).commit();
        startActivity(openConversationIntent);

    }

    @Override
    public void onViewProfile(User user) {
        //opens profileActivity
        Toast.makeText(requireContext(), "opening profile", Toast.LENGTH_SHORT).show();
        Intent openProfileIntent = new Intent(requireContext(), ProfileActivity2.class);
        openProfileIntent.putExtra("recipient",user.getUserUID());
        requireActivity().getSupportFragmentManager().beginTransaction().remove(NewChatFragment2.this).commit();
        startActivity(openProfileIntent);
    }

    private String CreateConversationID()
    {
        return "C_" + System.currentTimeMillis();
    }
}
