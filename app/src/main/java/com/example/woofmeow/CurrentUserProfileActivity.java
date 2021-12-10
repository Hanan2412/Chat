package com.example.woofmeow;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;


import Adapters.ListAdapter;
import DataBase.DBActive;
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
            FileManager fm = FileManager.getInstance();
            Bitmap bitmap = fm.readImage(this, FileManager.user_profile_images, user.getUserUID());
            if (bitmap!=null)
                profilePic.setImageBitmap(bitmap);
            else
                Log.e("Bitmap", "currentUser profile bitmap is null");
            ImageButton muteBtn = findViewById(R.id.mute);
            muteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //shows all muted users and conversations
                    String[] selectionArgs = {"muted"};
                    List<Conversation>mutedConversations = db.getAllMutedOrBlockedConversation("muted LIKE ?",selectionArgs);
                    adapter.setConversations(mutedConversations);
                }
            });
            ImageButton blockBtn = findViewById(R.id.block);
            blockBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //shows all blocked users and conversations
                    String[] selectionArgs = {"blocked"};
                    List<Conversation>blocked = db.getAllMutedOrBlockedConversation("blocked LIKE ?",selectionArgs);
                    adapter.setConversations(blocked);
                }
            });
            ImageButton stats = findViewById(R.id.stats);
            stats.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //shows the stats of this account
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
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
