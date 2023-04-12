package com.example.woofmeow;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import Adapters.GroupProfileAdapter;
import Backend.AppViewModel;
import Backend.ConversationVM;
import Backend.UserVM;
//import DataBase.DBActive;
import NormalObjects.User;

@SuppressWarnings({"Convert2Lambda", "unchecked"})
public class GroupActivity extends AppCompatActivity {

    private UserVM userVM;
    private ConversationVM conversationVM;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.group_activity);
        setContentView(R.layout.profile_activity3);
        userVM = new ViewModelProvider(this).get(UserVM.class);
        conversationVM = new ViewModelProvider(this).get(ConversationVM.class);

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
        RecyclerView groupMembers = findViewById(R.id.previewImages);
        groupMembers.setHasFixedSize(true);
        groupMembers.setItemViewCacheSize(20);
        groupMembers.setDrawingCacheEnabled(true);
        groupMembers.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        groupMembers.setLayoutManager(layoutManager);
        List<User>recipients = (List<User>)getIntent().getSerializableExtra("recipients");
        GroupProfileAdapter adapter = new GroupProfileAdapter();
        adapter.setRecipients(recipients);
        groupMembers.setAdapter(adapter);
    }

}
