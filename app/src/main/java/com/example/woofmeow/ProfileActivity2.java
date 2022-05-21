package com.example.woofmeow;


import android.graphics.Bitmap;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Backend.UserVM;
import NormalObjects.FileManager;
import NormalObjects.User;

@SuppressWarnings("Convert2Lambda")
public class ProfileActivity2 extends AppCompatActivity {

    private UserVM userVM;
    private User user;
    private LinearLayout rootLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity2);
        userVM = new ViewModelProvider(this).get(UserVM.class);
        ShapeableImageView profileImage = findViewById(R.id.profileImage);
        //String conversationID = getIntent().getStringExtra("conversationID");
        ListView userDetails = findViewById(R.id.userDetails);
        ImageButton goBack = findViewById(R.id.goBack);
        TextView title = findViewById(R.id.title);
        title.setVisibility(View.GONE);
        rootLayout = findViewById(R.id.rootLayout);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
       user = (User) getIntent().getSerializableExtra("user");
        if (user != null) {
            TextView userNameTV = findViewById(R.id.username);
            String userName = user.getName() + " " + user.getLastName();
            userNameTV.setText(userName);
            ImageButton muteBtn = findViewById(R.id.mute);
            ImageButton blockBtn = findViewById(R.id.block);
            muteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   muteUser();
                }
            });
            blockBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   blockUser();
                }
            });
            FileManager fm = FileManager.getInstance();
            Bitmap bitmap = fm.readImage(this, FileManager.user_profile_images, user.getUserUID());
            if (bitmap != null)
                profileImage.setImageBitmap(bitmap);
            else Log.e("profileActivity", "user image is null");
            String[] userDits = {user.getName(), user.getLastName()};
            List<String> ditsList = new ArrayList<>(Arrays.asList(userDits));
            if (user.getPhoneNumber() != null)
                ditsList.add(user.getPhoneNumber());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ditsList);
            userDetails.setAdapter(adapter);
        } else {
            throw new NullPointerException("user can't be null");
        }
    }

    public void muteUser()
    {
        LiveData<Boolean>muteUser = userVM.isUserMuted(user.getUserUID());
        muteUser.observe(ProfileActivity2.this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                String showText, actionText;
                if (aBoolean)
                {
                    userVM.unMuteUser(user.getUserUID());
                    showText = "user was un muted";
                    actionText = "mute";
                }
                else
                {
                    userVM.muteUser(user.getUserUID());
                    showText = "user was muted";
                    actionText = "un mute";
                }
                Snackbar.make(rootLayout, showText, Snackbar.LENGTH_SHORT)
                        .setAction(actionText, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                muteUser();
                            }
                        }).show();
                muteUser.removeObservers(ProfileActivity2.this);
            }
        });
    }

    public void blockUser()
    {
        LiveData<Boolean>blockUser = userVM.isUserBlocked(user.getUserUID());
        blockUser.observe(ProfileActivity2.this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                String showText, actionText;
                if (aBoolean)
                {
                    showText = "user was un blocked";
                    actionText = "block";
                    userVM.unBlockUser(user.getUserUID());
                }
                else
                {
                    showText = "user was blocked";
                    actionText = "un block";
                    userVM.blockUser(user.getUserUID());
                }
                Snackbar.make(rootLayout, showText, Snackbar.LENGTH_SHORT)
                        .setAction(actionText, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                blockUser();
                            }
                        }).show();
                blockUser.removeObservers(ProfileActivity2.this);
            }
        });
    }
}
