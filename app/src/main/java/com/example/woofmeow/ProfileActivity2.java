package com.example.woofmeow;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.firebase.auth.FirebaseAuth;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Objects;

import Controller.CController;
import NormalObjects.User;
@SuppressWarnings("Convert2Lambda")
public class ProfileActivity2 extends AppCompatActivity{

    private User recipient = null,currentUser = null;
    private final String currentUserUID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    private ImageView profileImage;
    private ListView recipientInformationList;
    private boolean blocked_d = false,muted = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity2);
        profileImage = findViewById(R.id.profileImage);
        ImageButton muteBtn = findViewById(R.id.mute);
        ImageButton blockBtn = findViewById(R.id.block);
        ImageButton deleteBtn = findViewById(R.id.deleteBtn);
        CController cController = CController.getController();
        String conversationID = getIntent().getStringExtra("conversationID");
        recipient = (User) getIntent().getSerializableExtra("recipient");
        currentUser = (User)getIntent().getSerializableExtra("currentUser");
        assert currentUser != null;
        ArrayList<String>mutedUsers = currentUser.getMutedUsersUID();
        assert recipient != null;
        muted = mutedUsers.contains(recipient.getUserUID());
        muteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!muted)
                {
                    muted = true;
                    cController.onUpdateData("users/" + currentUserUID + "/mutedUsers/" + recipient.getUserUID(),true);
                    cController.onUpdateData("users/" + currentUserUID + "/conversations/" + conversationID + "/conversationInfo/muted",true);
                    MarkButton(muteBtn,true);
                }
                else
                {
                    muted = false;
                    cController.onUpdateData("users/" + currentUserUID + "/mutedUsers/" + recipient.getUserUID(),false);
                    cController.onRemoveData("users/" + currentUserUID + "/conversations/" + conversationID + "/conversationInfo/muted");
                    MarkButton(muteBtn,false);
                }
            }
        });
        if (muted)
        {
            MarkButton(muteBtn,true);
        }
        ArrayList<String>blocked = currentUser.getBlockedUsers();
        blocked_d = blocked.contains(recipient.getUserUID());
        if (blocked_d)
            MarkButton(blockBtn,true);

        blockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!blocked_d)
                {
                    blocked_d = true;
                    MarkButton(blockBtn,true);
                    cController.onUpdateData("users/" + currentUserUID + "/blocked/" + recipient.getUserUID(),recipient.getUserUID());
                    cController.onUpdateData("users/" + currentUserUID + "/conversations/" + conversationID + "/conversationInfo/blocked/",true);
                }
                else
                {
                    blocked_d = false;
                    MarkButton(blockBtn,false);
                    cController.onRemoveData("users/" + currentUserUID + "/blocked/" + recipient.getUserUID());
                    cController.onRemoveData("users/" + currentUserUID + "/conversations/" + conversationID + "/conversationInfo/blocked/");
                }

            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity2.this);
                builder.setTitle("Delete the conversation with this user?")
                        .setMessage("Are you sure you would like to delete the conversation with this user,it's not reversible")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                cController.onRemoveData("users/" + currentUserUID + "/conversations/" + conversationID);
                                LocalBroadcastManager.getInstance(ProfileActivity2.this).sendBroadcast(new Intent("DeleteConversation").putExtra("ConversationID",conversationID));
                                Toast.makeText(ProfileActivity2.this, "Conversation deleted", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setCancelable(true).create().show();
            }
        });
        recipientInformationList = findViewById(android.R.id.list);
        profileImage.setImageBitmap(LoadRecipientImage());
        String[] userInformation = new String[3];
        userInformation[0] = recipient.getName();
        userInformation[1] = recipient.getLastName();
        userInformation[2] = recipient.getPhoneNumber();
        ArrayAdapter<String>adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userInformation);
        recipientInformationList.setAdapter(adapter);
    }

    private void MarkButton(ImageButton button,boolean mark)
    {
        if (mark)
        {
            Drawable drawable = ResourcesCompat.getDrawable(getResources(),R.drawable.border,getTheme());
            button.setBackground(drawable);
        }
        else
        {
            button.setBackground(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CController.getController().setProfileGUI(null);
    }

    private Bitmap LoadRecipientImage() {
        try {
            ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
            File directory = contextWrapper.getDir("user_images", Context.MODE_PRIVATE);
            File imageFile = new File(directory, recipient.getUserUID() + "_Image");
            return BitmapFactory.decodeStream(new FileInputStream(imageFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
