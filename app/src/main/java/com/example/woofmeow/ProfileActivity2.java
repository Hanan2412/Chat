package com.example.woofmeow;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import Adapters.ImageAdapterRV;
import Adapters.ListAdapter2;
import Backend.UserVM;
import Consts.MessageType;
import Fragments.BinaryFragment;
import Fragments.ImageFragment;
import Fragments.ListFragment;
import Fragments.SingleFieldFragment;
import NormalObjects.FileManager;
import NormalObjects.User;

@SuppressWarnings("Convert2Lambda")
public class ProfileActivity2 extends AppCompatActivity {

    private User user;
    private final String currentUserID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    private static final String PROFILE_ACTIVITY = "profileActivity";
    private final String messagesSent = "Messages Sent";
    private final String messagesReceived = "Messages Received";
    private final String timeSpent = "Time Spent";
    private final String timeSpentLast24 = "Time Spent In The Last 24h";
    private final String registrationTime = "Registration Time";
    private final String blockedConversationAmount = "Blocked Conversations";
    private final String mutedConversationsAmount = "Muted Conversations";
    private final String blockedUserAmount = "Blocked Recipients";
    private final String mutedUsers = "Muted Recipients";
    private final String filesReceivedAmount = "Files Received";
    private final String imagesReceivedAmount = "Images Received";
    private final String recordingsReceivedAmount = "Recordings Received";
    private final String filesSentAmount = "Files Sent";
    private final String recordingSentAmount = "Recording Sent";
    private final String imagesSentAmount = "Images Sent ";
    private final String name = "Name";
    private final String lastName = "Last Name";
    private final String about = "About";
    private final String phoneNumber = "Phone Number";

    private final int WRITE_PERMISSION = 3;
    private ActivityResultLauncher<Intent> takePicture, openGallery;
    private UserVM userVM;

    private String photoPath;
    private Uri imageUri;

    private ShapeableImageView profileImage;
    private FileManager fileManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity2);
        user = (User) getIntent().getSerializableExtra("user");
        TextView userName = findViewById(R.id.userName);
        userName.setText(user.getName());
        userVM = new ViewModelProvider(this).get(UserVM.class);
        profileImage = findViewById(R.id.profileImage);

        ImageButton editProfileImageBtn = findViewById(R.id.edit);
        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayShowTitleEnabled(false);
        fileManager = FileManager.getInstance();
        Bitmap profileBitmap = fileManager.readImage(this, FileManager.user_profile_images, user.getUserUID());
        if (profileBitmap != null)
        {
            profileImage.setImageBitmap(profileBitmap);
            profileImage.setBackground(ContextCompat.getDrawable(this, android.R.color.transparent));
        }
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(PROFILE_ACTIVITY,"clicked on image");
                Bundle bundle = new Bundle();
                String path = user.getPictureLink();
                bundle.putString("image", path);
                ImageFragment imageFragment = new ImageFragment();
                imageFragment.setArguments(bundle);
                imageFragment.show(getSupportFragmentManager(), "IMAGE_FRAGMENT");
            }
        });
        editProfileImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BinaryFragment binaryFragment = new BinaryFragment();
                binaryFragment.setListener(new BinaryFragment.BinaryClickListener() {
                    @Override
                    public void onFirstBtnClick() {
                        if (askPermission(MessageType.photoMessage)) {
                            openGallery();
                        } else {
                            Log.d(PROFILE_ACTIVITY, "no permission to access gallery");
                        }
                        binaryFragment.dismiss();
                    }

                    @Override
                    public void onSecondBtnClick() {
                        if (askPermission(MessageType.imageMessage)) {
                            takePicture();
                        } else {
                            Log.d(PROFILE_ACTIVITY, "no permission to access camera");
                        }
                        binaryFragment.dismiss();
                    }
                });
                binaryFragment.show(getSupportFragmentManager(), "Binary fragment");

            }
        });
        ImageButton goBack = findViewById(R.id.goBack);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        List<String> userImages = new ArrayList<>();
        userImages.add(user.getPictureLink());
        ImageAdapterRV adapter = new ImageAdapterRV();
        adapter.setImagesPaths(userImages);
        ListFragment infoFragment = new ListFragment();
        List<String> information = new ArrayList<>();
        information.add(String.valueOf(user.getMsgSentAmount()));
        information.add(String.valueOf(user.getMsgReceivedAmount()));
        information.add(String.valueOf((int) user.getTimeSpentTotalAmount()));
        information.add(String.valueOf((int) user.getTimeSpent24Amount()));
        information.add(String.valueOf((int) user.getTimeRegisteredAmount()));
        information.add(String.valueOf(user.getBlockedConversationsAmount()));
        information.add(String.valueOf(user.getMutedConversationsAmount()));
        information.add(String.valueOf(user.getBlockedUsersAmount()));
        information.add(String.valueOf(user.getMutedUsersAmount()));
        information.add(String.valueOf(user.getFilesReceivedAmount()));
        information.add(String.valueOf(user.getFilesSentAmount()));
        information.add(String.valueOf(user.getImagesReceivedAmount()));
        information.add(String.valueOf(user.getImagesSentAmount()));
        information.add(String.valueOf(user.getRecordingsReceivedAmount()));
        information.add(String.valueOf(user.getRecordingsSentAmount()));
        List<String> titles = new ArrayList<>();
        titles.add(messagesSent);
        titles.add(messagesReceived);
        titles.add(timeSpent);
        titles.add(timeSpentLast24);
        titles.add(registrationTime);
        titles.add(blockedConversationAmount);
        titles.add(mutedConversationsAmount);
        titles.add(blockedUserAmount);
        titles.add(mutedUsers);
        titles.add(filesReceivedAmount);
        titles.add(filesSentAmount);
        titles.add(imagesReceivedAmount);
        titles.add(imagesSentAmount);
        titles.add(recordingsReceivedAmount);
        titles.add(recordingSentAmount);


        ListFragment profileFragment = new ListFragment();
        List<String> profileInfoTitles = new ArrayList<>();
        profileInfoTitles.add(name);
        profileInfoTitles.add(lastName);
        profileInfoTitles.add(about);
        profileInfoTitles.add(phoneNumber);
        List<String> dits = new ArrayList<>();
        dits.add(user.getName());
        dits.add(user.getLastName());
        dits.add(user.getAbout());
        dits.add(user.getPhoneNumber());
        profileFragment.setListener(new ListFragment.ItemClickListener() {
            @Override
            public void onClickItem(int position) {
                SingleFieldFragment singleFieldFragment = new SingleFieldFragment();
                singleFieldFragment.setListener(new SingleFieldFragment.onText() {
                    @Override
                    public void onTextChange(String name) {
                        Log.d(PROFILE_ACTIVITY, "changed name to: " + name);
                        profileFragment.getAdapter().updateItem(name, position);
                        singleFieldFragment.dismiss();
                        String title = profileInfoTitles.get(position);
                        if (title.equals(ProfileActivity2.this.name)) {
                            user.setName(name);
                        } else if (title.equals(ProfileActivity2.this.lastName)) {
                            user.setLastName(name);
                        } else if (title.equals(ProfileActivity2.this.about)) {
                            user.setAbout(name);
                        } else if (title.equals(ProfileActivity2.this.phoneNumber)) {
                            user.setPhoneNumber(name);
                        }
                        updateDB();
                    }
                });
                singleFieldFragment.setHint(profileInfoTitles.get(position));
                Log.d(PROFILE_ACTIVITY, "info - clicked on: " + profileInfoTitles.get(position));
                singleFieldFragment.show(getSupportFragmentManager(), "Edit_Content");

            }
        });


        List<String> mutedTitles = new ArrayList<>();
        List<String> mutedDetails = new ArrayList<>();
        ListFragment mutedUsers = new ListFragment();
        mutedUsers.setListener(new ListFragment.ItemClickListener() {
            @Override
            public void onClickItem(int position) {

            }
        });
        userVM.getAllMutedOrBlockedUsers(false).observe(ProfileActivity2.this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                for (User user : users) {
                    mutedTitles.add("Name");
                    mutedDetails.add(user.getName() + " " + user.getLastName());
                }

            }
        });
        List<String> blockedTitles = new ArrayList<>();
        List<String> blockedDetails = new ArrayList<>();
        ListFragment blockedFragment = new ListFragment();
        blockedFragment.setListener(new ListFragment.ItemClickListener() {
            @Override
            public void onClickItem(int position) {

            }
        });
        userVM.getAllMutedOrBlockedUsers(true).observe(ProfileActivity2.this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                for (User user : users) {
                    blockedTitles.add("name");
                    blockedDetails.add(user.getName() + " " + user.getLastName());
                }
                userVM.getAllMutedOrBlockedUsers(true).removeObservers(ProfileActivity2.this);
            }
        });
        BottomNavigationView navigationView = findViewById(R.id.bottomNavigationView);
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int x = getSupportFragmentManager().getFragments().size();
                Log.d(PROFILE_ACTIVITY, "amount of fragments:" + x);
                if (item.getItemId() == R.id.info) {
                    ListAdapter2 listAdapter = new ListAdapter2();
                    listAdapter.setItems(information);
                    listAdapter.setTitles(titles);
                    infoFragment.setAdapter(listAdapter);
                    Log.d(PROFILE_ACTIVITY, "changed to info");
                    getSupportFragmentManager().beginTransaction().replace(R.id.placeholder, infoFragment).commit();
                } else if (item.getItemId() == R.id.media) {
                    Log.d(PROFILE_ACTIVITY, "changed to media");
                } else if (item.getItemId() == R.id.profile) {
                    ListAdapter2 listAdapter = new ListAdapter2();
                    listAdapter.setItems(dits);
                    listAdapter.setTitles(profileInfoTitles);
                    profileFragment.setAdapter(listAdapter);
                    Log.d(PROFILE_ACTIVITY, "changed to profile");
                    getSupportFragmentManager().beginTransaction().replace(R.id.placeholder, profileFragment).commit();
                } else if (item.getItemId() == R.id.muted) {
                    ListAdapter2 listAdapter = new ListAdapter2();
                    listAdapter.setItems(mutedDetails);
                    listAdapter.setTitles(mutedTitles);
                    mutedUsers.setAdapter(listAdapter);
                    Log.d(PROFILE_ACTIVITY, "changed to muted");
                    getSupportFragmentManager().beginTransaction().replace(R.id.placeholder, mutedUsers).commit();
                } else if (item.getItemId() == R.id.block) {
                    ListAdapter2 listAdapter = new ListAdapter2();
                    listAdapter.setItems(blockedDetails);
                    listAdapter.setTitles(blockedTitles);
                    blockedFragment.setAdapter(listAdapter);
                    Log.d(PROFILE_ACTIVITY, "changed to blocked");
                    getSupportFragmentManager().beginTransaction().replace(R.id.placeholder, blockedFragment).commit();
                }
                return true;
            }
        });
        navigationView.setSelectedItemId(R.id.profile);
        openGallery = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    Intent resultIntent = result.getData();
                    if (resultIntent != null) {
                        Uri uri = resultIntent.getData();
                        if (uri != null) {
                            imageUri = uri;
                            saveAndSetImage();
                        }
                    }
                }
            }
        });
        takePicture = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    saveAndSetImage();
                }
            }
        });

        if (!user.getUserUID().equals(currentUserID))
        {
          editProfileImageBtn.setVisibility(View.GONE);
          navigationView.getMenu().setGroupVisible(R.id.currentUserGroup, false);
        }
    }

    private void saveAndSetImage() {
        Log.d(PROFILE_ACTIVITY, "save and set message");
        Picasso.get().load(imageUri).into(profileImage);
        Bitmap imageBitmap = getImageBitmap(imageUri);
        fileManager.saveProfileImage(imageBitmap, user.getUserUID(), ProfileActivity2.this, false);
        user.setPictureLink(imageUri.toString());
        updateDB();
    }

    private Bitmap getImageBitmap(Uri uri) {
        Bitmap image = null;
        try {
            if (Build.VERSION.SDK_INT > 27) {
                ImageDecoder.Source source = ImageDecoder.createSource(ProfileActivity2.this.getContentResolver(), uri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                image = MediaStore.Images.Media.getBitmap(ProfileActivity2.this.getContentResolver(), uri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    private boolean askPermission(MessageType type) {
        if (type == MessageType.photoMessage || type == MessageType.imageMessage) {
            int hasWritePermission = ProfileActivity2.this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION);
                return false;
            } else return true;
        } else return false;
    }

    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
//        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
//        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        openGallery.launch(Intent.createChooser(galleryIntent, "Select Picture to Upload"));
        //startActivityForResult(Intent.createChooser(intent, "Select Picture to Upload"), GALLERY_REQUEST);
    }

    private void updateDB() {
        Log.d(PROFILE_ACTIVITY, "db user update");
        userVM.updateUser(user);
    }

    private void takePicture() {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = ProfileActivity2.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File image = File.createTempFile(imageFileName, ".jpg", storageDir);
            setPhotoPath(image.getAbsolutePath());
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            if (takePictureIntent.resolveActivity(ProfileActivity2.this.getPackageManager()) != null) {
            File photoFile;
            photoFile = image;
            Uri photoURI = FileProvider.getUriForFile(ProfileActivity2.this,
                    "com.example.woofmeow.provider", photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

            imageUri = photoURI;
            takePicture.launch(takePictureIntent);
            //startActivityForResult(takePictureIntent, CAMERA_REQUEST);
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (user.getUserUID().equals(currentUserID))
        {
            getMenuInflater().inflate(R.menu.profile_menu, menu);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.delete)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    userVM.deleteUser(user);
                    finish();
                }
            }).setMessage("Are you sure you want to delete this account")
                    .setTitle("Confirm deletion")
                    .setCancelable(true)
                    .create()
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }
}
