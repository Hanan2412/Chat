package com.example.woofmeow;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import NormalObjects.User;

@Deprecated
public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_layout);
        TextView name = findViewById(R.id.profileName);
        TextView lastName = findViewById(R.id.profileLastName);
        TextView nickName = findViewById(R.id.nickname);
        TextView timeCreated = findViewById(R.id.timeCreated);
        TextView lastTimeLogIn = findViewById(R.id.lastLogin);
        TextView savedItems = findViewById(R.id.SavedItems);
        ListView savedItemsList = findViewById(R.id.savedFeed);
        ImageView image = findViewById(R.id.userPhoto);
        User user = (User) getIntent().getSerializableExtra("user");
        if(user!=null) {
            Picasso.get().load(user.getPictureLink()).into(image);
            name.setText(user.getName());
            lastName.setText(user.getLastName());
            nickName.setText(user.getNickName());
            timeCreated.setText(user.getTimeCreated());
            lastTimeLogIn.setText(user.getTimeCreated());
            if(user.getSavedFeed()!=null) {
                if (!user.getSavedFeed().isEmpty()) {
                    savedItems.setVisibility(View.VISIBLE);

                }
            }
            else
                savedItems.setVisibility(View.GONE);


        }

    }
}
