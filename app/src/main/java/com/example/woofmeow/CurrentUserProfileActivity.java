package com.example.woofmeow;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import Adapters.ListAdapter;
import Controller.CController;
import NormalObjects.User;

@SuppressWarnings("Convert2Lambda")
public class CurrentUserProfileActivity extends AppCompatActivity implements ProfileGUI {

    private ListView blockedUsers, mutedUsers;
    private TextView openBlockedUsers, openMutedUsers;
    private CController controller;
    private ListAdapter blockedUsersAdapter, mutedUsersAdapter;
    private User currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.current_user_profile_layout);
        currentUser = (User) getIntent().getSerializableExtra("currentUser");
        blockedUsers = findViewById(R.id.blockedUsersList);
        mutedUsers = findViewById(R.id.mutedUsersList);
        Button deleteAccount = findViewById(R.id.deleteBtn);

        Button resetAccount = findViewById(R.id.reset);
        openBlockedUsers = findViewById(R.id.blockedUsers);
        openMutedUsers = findViewById(R.id.mutedUsers);
        controller = CController.getController();
        controller.setProfileGUI(this);

        TextView name = findViewById(R.id.userName);
        TextView phoneNumber = findViewById(R.id.phoneNumber);
        ImageView profileImage = findViewById(R.id.profileImage);
        String fullName = currentUser.getName() + " " + currentUser.getLastName();
        name.setText(fullName);
        phoneNumber.setText(currentUser.getPhoneNumber());
        Bitmap image = LoadImage();
        if (image != null)
            profileImage.setImageBitmap(image);
        else {
            Picasso.get().load(currentUser.getPictureLink()).into(profileImage);
        }
        if (currentUser != null) {
            ArrayList<String> blockedUsersList = currentUser.getBlockedUsers();
            for (int i = 0; i < blockedUsersList.size(); i++)
                controller.onDownloadUser(this, blockedUsersList.get(i));
        }

        blockedUsersAdapter = new ListAdapter();
        blockedUsersAdapter.setUsers(new ArrayList<>());
        blockedUsers.setAdapter(blockedUsersAdapter);
        openBlockedUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (blockedUsersAdapter.isEmpty())
                    Toast.makeText(CurrentUserProfileActivity.this, "no blocked users", Toast.LENGTH_SHORT).show();
                else if (blockedUsers.getVisibility() == View.GONE) {
                    blockedUsers.setVisibility(View.VISIBLE);
                    openBlockedUsers.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_arrow_drop_up_24, 0);
                } else {
                    blockedUsers.setVisibility(View.GONE);
                    openBlockedUsers.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_arrow_drop_down_24, 0);
                }
            }
        });
        mutedUsersAdapter = new ListAdapter();
        mutedUsersAdapter.setUsers(new ArrayList<>());
        mutedUsers.setAdapter(mutedUsersAdapter);
        openMutedUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mutedUsersAdapter.isEmpty())
                    Toast.makeText(CurrentUserProfileActivity.this, "no muted users", Toast.LENGTH_SHORT).show();
                else if (mutedUsers.getVisibility() == View.GONE) {
                    mutedUsers.setVisibility(View.VISIBLE);
                    openMutedUsers.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_arrow_drop_up_24, 0);
                } else {
                    mutedUsers.setVisibility(View.GONE);
                    openMutedUsers.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_arrow_drop_down_24, 0);
                }
            }
        });
        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CurrentUserProfileActivity.this);
                builder.setTitle("Delete Account")
                        .setMessage("Are you sure you would like to delete your account?")
                        .setCancelable(true)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                controller.onRemoveData("users/" + currentUser.getUserUID());
                                FirebaseMessaging.getInstance().deleteToken();
                                if (FirebaseAuth.getInstance().getCurrentUser() != null)
                                    FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(CurrentUserProfileActivity.this, "Account deleted, good by", Toast.LENGTH_SHORT).show();
                                            Intent startOver = new Intent(CurrentUserProfileActivity.this, FirstPageActivity.class);
                                            startActivity(startOver);
                                            finish();
                                        }
                                    });

                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(CurrentUserProfileActivity.this, "Operation cancelled", Toast.LENGTH_SHORT).show();
                    }
                }).create().show();
            }
        });

        resetAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CurrentUserProfileActivity.this);
                builder.setTitle("Reset Account")
                        .setMessage("Are you sure you would like to reset your account? all your data will be deleted (except the email and password you used to during the signUp process), it will be like opening a new account")
                        .setCancelable(true)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                controller.onRemoveData("users/" + currentUser.getUserUID());
                                Toast.makeText(CurrentUserProfileActivity.this, "Account reset complete, good by", Toast.LENGTH_SHORT).show();
                                FirebaseMessaging.getInstance().deleteToken();
                                FirebaseAuth.getInstance().signOut();
                                Intent startOver = new Intent(CurrentUserProfileActivity.this, FirstPageActivity.class);
                                startActivity(startOver);
                                finish();
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(CurrentUserProfileActivity.this, "Operation cancelled", Toast.LENGTH_SHORT).show();
                    }
                }).create().show();

            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        controller.setProfileGUI(null);
    }

    @Override
    public void onReceiveUser(User user) {
        if (DetermineUserPosition(user.getUserUID()))
            blockedUsersAdapter.addUser(user);
        else
            mutedUsersAdapter.addUser(user);
    }

    private boolean DetermineUserPosition(String userUID) {
        return currentUser.getBlockedUsers().contains(userUID);
    }

    private Bitmap LoadImage() {
        SharedPreferences savedImagesPreferences = getSharedPreferences("SavedImages", Context.MODE_PRIVATE);
        String sender = currentUser.getUserUID();
        if (savedImagesPreferences.getBoolean(sender, false)) {
            try {
                ContextWrapper contextWrapper = new ContextWrapper(this.getApplicationContext());
                File directory = contextWrapper.getDir("user_images", Context.MODE_PRIVATE);
                File imageFile = new File(directory, sender + "_Image");
                return BitmapFactory.decodeStream(new FileInputStream(imageFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
