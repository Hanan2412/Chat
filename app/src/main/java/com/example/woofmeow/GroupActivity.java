package com.example.woofmeow;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import Adapters.GroupProfileAdapter;
import DataBase.DBActive;
import NormalObjects.User;

@SuppressWarnings("Convert2Lambda")
public class GroupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_activity);
        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayShowTitleEnabled(false);
        ImageButton backButton = findViewById(R.id.goBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        String conversationID = getIntent().getStringExtra("conversationID");
        DBActive db = DBActive.getInstance(this);
        RecyclerView groupMembers = findViewById(R.id.groupMembers);
        groupMembers.setHasFixedSize(true);
        groupMembers.setItemViewCacheSize(20);
        groupMembers.setDrawingCacheEnabled(true);
        groupMembers.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        groupMembers.setLayoutManager(layoutManager);
        List<User> recipients = db.loadUsers(conversationID);
        GroupProfileAdapter adapter = new GroupProfileAdapter();
        adapter.setListener(new GroupProfileAdapter.onUserInteraction() {
            @Override
            public void onMute(String userID) {
                 db.muteConversation(conversationID);
            }

            @Override
            public void onBlock(String userID) {
                db.blockConversation(conversationID);
            }
        });
        adapter.setRecipients(recipients);
        groupMembers.setAdapter(adapter);
    }
}
