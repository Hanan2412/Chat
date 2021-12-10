package com.example.woofmeow;


import android.graphics.Bitmap;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import DataBase.DBActive;
import NormalObjects.FileManager;
import NormalObjects.User;
@SuppressWarnings("Convert2Lambda")
public class ProfileActivity2 extends AppCompatActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity2);
        ImageView profileImage = findViewById(R.id.profileImage);
        String conversationID = getIntent().getStringExtra("conversationID");
        ListView userDetails = findViewById(R.id.userDetails);

        User user =(User) getIntent().getSerializableExtra("user");
        if (user!=null)
        {
            DBActive db = DBActive.getInstance(this);
            ImageButton muteBtn = findViewById(R.id.mute);
            ImageButton blockBtn = findViewById(R.id.block);
            muteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.muteUser(user.getUserUID());
                }
            });
            blockBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.blockUser(user.getUserUID());
                }
            });
            FileManager fm = FileManager.getInstance();
            Bitmap bitmap = fm.readImage(this,FileManager.conversationProfileImage,conversationID);
            if (bitmap!=null)
                profileImage.setImageBitmap(bitmap);
            else Log.e("profileActivity","user image is null");
            String[] userDits = {user.getName(),user.getLastName()};
            List<String>ditsList = new ArrayList<>(Arrays.asList(userDits));
            if (user.getPhoneNumber()!=null)
                ditsList.add(user.getPhoneNumber());
            ArrayAdapter<String>adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ditsList);
            userDetails.setAdapter(adapter);
        }
        else
        {
            throw new NullPointerException("user can't be null");
        }
    }
}
