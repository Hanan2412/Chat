package com.example.woofmeow;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

import NormalObjects.Server;
import NormalObjects.User;

public class ProfileActivity2 extends AppCompatActivity {

    private final String CurrentUserString = "currentUser";
    private final String RecipientString = "recipient";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity2);
        ShapeableImageView profileImage = findViewById(R.id.profileImage);
        ImageButton muteBtn = findViewById(R.id.mute);
        ImageButton blockBtn = findViewById(R.id.block);
        ImageButton favoriteBtn = findViewById(R.id.favorBtn);
        ImageButton deleteBtn = findViewById(R.id.deleteBtn);
        ImageButton notificationExceptionBtn = findViewById(R.id.notificationBtn);

            User currentUser = (User) getIntent().getSerializableExtra("currentUser");
            User recipient = (User) getIntent().getSerializableExtra("recipient");

            if (recipient != null && currentUser != null) {


                if (currentUser.getBlockedUsers().contains(recipient.getUserUID())) {
                    blockBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.border, getTheme()));
                }

                Picasso.get().load(recipient.getPictureLink()).into(profileImage);
                muteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //mutes this user
                        muteBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.border, getTheme()));
                    }
                });
                blockBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //blocks this user
                        if (currentUser.getBlockedUsers().contains(recipient.getUserUID())) {
                            Server.removeServer("users/" + currentUser.getUserUID() + "/block/" + recipient.getUserUID());
                            currentUser.getBlockedUsers().remove(recipient.getUserUID());
                            blockBtn.setBackground(null);
                        } else {
                            Server.updateServer("users" + currentUser.getUserUID() + "/block/" + recipient.getUserUID(), recipient.getUserUID());
                            currentUser.getBlockedUsers().add(recipient.getUserUID());
                            blockBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.border, getTheme()));
                        }
                    }
                });
                favoriteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //favorites this user
                        Server.updateServer("users/" + currentUser.getUserUID() + "/favorite/" + recipient.getUserUID(), recipient.getUserUID());
                        favoriteBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.border, getTheme()));
                    }
                });
                deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //deletes this user from conversations
                        Server.removeServer("users/" + recipient.getUserUID());
                        Toast.makeText(ProfileActivity2.this, "user was deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                notificationExceptionBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //sets this user to ignore notifications settings

                    }
                });
            } else
                Toast.makeText(this, "error, recipient user object is null ", Toast.LENGTH_SHORT).show();

    }
}
