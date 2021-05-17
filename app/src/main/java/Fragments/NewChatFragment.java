package Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.example.woofmeow.ConversationActivity;
import com.example.woofmeow.ProfileActivity2;
import com.example.woofmeow.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Objects;

import Adapters.UserListAdapter;
import NormalObjects.Server;
import NormalObjects.User;

@SuppressWarnings({"unchecked", "Convert2Lambda"})
public class NewChatFragment extends DialogFragment implements UserListAdapter.userListInterface {

    private String searchText;
    private ArrayList<User>users;
    private UserListAdapter adapter;
    private final String currentUser = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    public static NewChatFragment newInstance(){
        return new NewChatFragment();
    }

    private NewChatFragment(){}


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.new_chat,null);
        TextInputEditText searchUsers = view.findViewById(R.id.searchUsers);

        RelativeLayout mainLayout = view.findViewById(R.id.mainLayout);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        boolean darkMode = preferences.getBoolean("darkView",false);
        if(darkMode)
        {
            mainLayout.setBackgroundColor(getResources().getColor(android.R.color.black,requireContext().getTheme()));
            searchUsers.setTextColor(getResources().getColor(android.R.color.black,requireContext().getTheme()));

        }
        else {
            mainLayout.setBackgroundColor(getResources().getColor(android.R.color.white, requireContext().getTheme()));
            searchUsers.setTextColor(getResources().getColor(android.R.color.black,requireContext().getTheme()));

        }
        searchUsers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                searchText = s.toString();
                Server.SearchForUsers(searchText,requireContext());
            }
        });
        ListView usersList = view.findViewById(R.id.searchList);
        adapter = new UserListAdapter();
        adapter.setListener(this);
        usersList.setAdapter(adapter);
        usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //opens new conversation
                assert getArguments() != null;
                User currentUser = (User)getArguments().getSerializable("user");
                Intent intent = new Intent(requireActivity(), ConversationActivity.class);
                intent.putExtra("recipient",users.get(position).getUserUID());
                intent.putExtra("user",currentUser);
                intent.putExtra("conversationID", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() + "   " + users.get(position).getUserUID());
                startActivity(intent);

            }
        });
        Button cancelBtn = view.findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getSupportFragmentManager().beginTransaction().remove(NewChatFragment.this).commit();
            }
        });
        setUsersBroadcast();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }

    private void setUsersBroadcast()
    {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                users = (ArrayList<User>) intent.getSerializableExtra("users");
                adapter.setList(users);
                adapter.notifyDataSetChanged();
            }
        };
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiver,new IntentFilter("UsersList"));
    }

    @Override
    public void onStartConversation(User user) {
        //opens conversationActivity
        Toast.makeText(getContext(), "starting conversation", Toast.LENGTH_SHORT).show();
        /*Intent openConversationIntent = new Intent(requireContext(),ConversationActivity.class);
        openConversationIntent.putExtra("recipient",user.getUserUID());
        openConversationIntent.putExtra("conversationID",currentUser + "   " + user.getUserUID());
        startActivity(openConversationIntent);*/
    }

    @Override
    public void onViewProfile(User user) {
        //opens profileActivity
        Toast.makeText(requireContext(), "opening profile", Toast.LENGTH_SHORT).show();
       /* Intent openProfileIntent = new Intent(requireContext(), ProfileActivity2.class);
        openProfileIntent.putExtra("recipient",user.getUserUID());
        startActivity(openProfileIntent);*/
    }
}
