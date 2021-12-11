package com.example.woofmeow;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;


import Adapters.ListAdapter;
import DataBase.DBActive;
import NormalObjects.ImageButtonPlus;
import NormalObjects.Conversation;
import NormalObjects.FileManager;
import NormalObjects.User;

@SuppressWarnings("Convert2Lambda")
public class CurrentUserProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity2);
        User user =(User) getIntent().getSerializableExtra("user");
        if (user!=null) {
            ListView dits = findViewById(R.id.userDetails);
            ListAdapter adapter = new ListAdapter();
            dits.setAdapter(adapter);
            DBActive db = DBActive.getInstance(this);
            ImageView profilePic = findViewById(R.id.profileImage);
            LinearLayout linearLayout = findViewById(R.id.rootLayout);
            TextView title = findViewById(R.id.title);
            title.setText("");
            ImageButton goBack = findViewById(R.id.goBack);
            goBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            FileManager fm = FileManager.getInstance();
            Bitmap bitmap = fm.readImage(this, FileManager.user_profile_images, user.getUserUID());
            if (bitmap!=null)
                profilePic.setImageBitmap(bitmap);
            else
                Log.e("Bitmap", "currentUser profile bitmap is null");
            ImageButtonPlus muteBtn =  findViewById(R.id.mute);
            ImageButtonPlus blockBtn = findViewById(R.id.block);
            muteBtn.setResetOnValue(2);
            muteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //shows all muted users and conversations
                    blockBtn.setPressCycle(-1);
                    String[] selectionArgs = {"muted"};
                    if (muteBtn.getPressCycle() == 1)
                    {
                        title.setText("Muted Conversations");
                        List<Conversation>mutedConversations = db.getAllMutedOrBlockedConversation("muted LIKE ?",selectionArgs);
                        adapter.setConversations(mutedConversations);
                    }
                    else if (muteBtn.getPressCycle() == 2)
                    {
                        title.setText("Muted Users");
                        List<User>mutedUsers = db.getAllMutedOrBlockedUsers("muted LIKE ?",selectionArgs);
                        adapter.setUsers(mutedUsers);
                    }
                    else
                    {
                        title.setText("");
                    }
                }
            });
            blockBtn.setResetOnValue(2);
            blockBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //shows all blocked users and conversations
                    muteBtn.setPressCycle(-1);
                    String[] selectionArgs = {"blocked"};
                    if (blockBtn.getPressCycle() == 1)
                    {
                        title.setText("Blocked conversations");
                        List<Conversation>blocked = db.getAllMutedOrBlockedConversation("blocked LIKE ?",selectionArgs);
                        adapter.setConversations(blocked);
                    }
                    else if (blockBtn.getPressCycle() == 2)
                    {
                        title.setText("Blocked users");
                        List<User>mutedUsers = db.getAllMutedOrBlockedUsers("blocked LIKE ?",selectionArgs);
                        adapter.setUsers(mutedUsers);
                    }
                    else
                    {
                        title.setText("");
                    }
                }
            });
            ImageButton stats = findViewById(R.id.stats);
            stats.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //shows the stats of this account
                    Toast.makeText(CurrentUserProfileActivity.this, "Im not implemented yet...", Toast.LENGTH_SHORT).show();
                }
            });
            stats.setVisibility(View.VISIBLE);
            ImageButton deleteBtn = findViewById(R.id.delete);
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //deletes this account and all the data corresponding to it
                    AlertDialog.Builder builder = new AlertDialog.Builder(CurrentUserProfileActivity.this);
                    builder.setTitle("Delete account?")
                            .setMessage("Are you sure you would like to delete your account? it will delete all data and is not recoverable!")
                            .setIcon(R.drawable.ic_baseline_delete_black)
                            .setCancelable(true)
                            .setPositiveButton("Delete!", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    db.resetDB();
                                    FirebaseMessaging.getInstance().deleteToken();
                                    if (FirebaseAuth.getInstance().getCurrentUser() != null)
                                        FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                finishAffinity();
                                            }
                                        });
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
                }
            });
            deleteBtn.setVisibility(View.VISIBLE);
            dits.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String showText,actionText;
                    if (muteBtn.getPressCycle() != -1)
                    {
                        if (adapter.getItem(0) instanceof Conversation)
                        {
                            Conversation conversation = (Conversation) adapter.getItem(position);
                            boolean muted = db.muteConversation(conversation.getConversationID());
                            if (muted)
                            {
                                showText = "Conversation was muted";
                                actionText = "un mute";
                            }
                            else
                            {
                                showText = "Conversation was un muted";
                                actionText = "mute";
                            }
                            Snackbar.make(linearLayout,showText,Snackbar.LENGTH_SHORT)
                                    .setAction(actionText, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            db.muteConversation(conversation.getConversationID());
                                        }
                                    }).show();
                        }
                        else
                        {
                            User recipient = (User) adapter.getItem(position);
                            boolean muted = db.muteUser(recipient.getUserUID());
                            if (muted)
                            {
                                showText = "Recipient was muted";
                                actionText = "un mute";
                            }
                            else
                            {
                                showText = "Recipient was un muted";
                                actionText = "mute";
                            }
                            Snackbar.make(linearLayout,showText,Snackbar.LENGTH_SHORT)
                                    .setAction(actionText, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            db.muteConversation(recipient.getUserUID());
                                        }
                                    }).show();
                        }
                    }
                    else if (blockBtn.getPressCycle() != -1)
                    {
                        if (adapter.getItem(0) instanceof User)
                        {
                            User recipient = (User) adapter.getItem(position);
                            boolean blocked = db.blockUser(recipient.getUserUID());
                            if (blocked)
                            {
                                showText = "Conversation was blocked";
                                actionText = "un blocked";
                            }
                            else
                            {
                                showText = "Conversation was un blocked";
                                actionText = "blocked";
                            }
                            Snackbar.make(linearLayout,showText,Snackbar.LENGTH_SHORT)
                                    .setAction(actionText, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            db.blockUser(recipient.getUserUID());
                                        }
                                    }).show();

                        }
                        else
                        {
                            User recipient = (User) adapter.getItem(position);
                            boolean blocked = db.blockUser(recipient.getUserUID());
                            if (blocked)
                            {
                                showText = "Recipient was blocked";
                                actionText = "un blocked";
                            }
                            else
                            {
                                showText = "Recipient was un blocked";
                                actionText = "blocked";
                            }
                            Snackbar.make(linearLayout,showText,Snackbar.LENGTH_SHORT)
                                    .setAction(actionText, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            db.blockUser(recipient.getUserUID());
                                        }
                                    }).show();
                        }
                    }
                }
            });
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
