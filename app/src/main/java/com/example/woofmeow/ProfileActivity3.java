package com.example.woofmeow;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
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
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import Adapters.GridImageAdapter;
import Adapters.GroupProfileAdapter;
import Adapters.ListAdapter2;
import Backend.ConversationVM;
import Backend.UserVM;
import Consts.MessageType;
import Fragments.BinaryFragment;
import Fragments.GridFragment;
import Fragments.ImageFragment;
import Fragments.ListFragment;
import Fragments.SingleFieldFragment;
import NormalObjects.Conversation;
import NormalObjects.FileManager;
import NormalObjects.Message;
import NormalObjects.User;

@SuppressWarnings({"Convert2Lambda", "unchecked"})
public class ProfileActivity3 extends AppCompatActivity {

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
    private final String mutedUsersTitle = "Muted Recipients";
    private final String filesReceivedAmount = "Files Received";
    private final String imagesReceivedAmount = "Images Received";
    private final String recordingsReceivedAmount = "Recordings Received";
    private final String filesSentAmount = "Files Sent";
    private final String recordingSentAmount = "Recording Sent";
    private final String imagesSentAmount = "Images Sent";
    private final String token = "Token";
    private final String name = "Name";
    private final String lastName = "Last Name";
    private final String about = "About";
    private final String phoneNumber = "Phone Number";
    private List<String> blockedTitles;
    private List<String> blockedDetails;
    private List<String> mutedTitles;
    private List<String> mutedDetails;
    private final int WRITE_PERMISSION = 3;
    private ActivityResultLauncher<Intent> takePicture, openGallery;
    private UserVM userVM;
    private ConversationVM conversationVM;
    private String photoPath;
    private Uri imageUri;
    private List<User> mutedUsers1;
    private List<User> blockedUsers;
    private List<Conversation> mutedConversations;
    private List<Conversation> blockedConversations;
    private ListFragment blockedFragment;
    private List<Message> mediaMessages;
    private List<User> recipients;
    private BottomNavigationView navigationView;
    private ListFragment mutedUsers;
    private GroupProfileAdapter adapter;
    private TextView userName;
    private RecyclerView groupMembers;
    private LinearLayoutManager manager;
    private List<String> titles;
    private List<String> information;
    private ListFragment infoFragment;
    private ListFragment profileFragment;
    private List<String> profileInfoTitles;
    private List<String> dits;
    private GridFragment gridFragment;
    private GridImageAdapter gridImageAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity3);
        if (getIntent().hasExtra("user")) {
            User user = (User) getIntent().getSerializableExtra("user");
            recipients = new ArrayList<>();
            recipients.add(user);
        } else if (getIntent().hasExtra("recipients")) {
            recipients = (List<User>) getIntent().getSerializableExtra("recipients");
        }
        userName = findViewById(R.id.userName);

        userVM = new ViewModelProvider(this).get(UserVM.class);
        conversationVM = new ViewModelProvider(this).get(ConversationVM.class);

        gridFragment = new GridFragment();
        gridImageAdapter = new GridImageAdapter();
        groupMembers = findViewById(R.id.previewImages);
        groupMembers.setHasFixedSize(true);
        groupMembers.setItemViewCacheSize(20);
        groupMembers.setDrawingCacheEnabled(true);
        groupMembers.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        groupMembers.setLayoutManager(layoutManager);
        adapter = new GroupProfileAdapter();
        adapter.setRecipients(recipients);
        adapter.setCurrentUID(currentUserID);
        groupMembers.setAdapter(adapter);

        adapter.setListener(new GroupProfileAdapter.onUserClick() {
            @Override
            public void onImageClick(User user) {
                Log.d(PROFILE_ACTIVITY, "clicked on image");
                String path = user.getPictureLink();
                if (path != null && !path.equals("")) {
                    Bundle bundle = new Bundle();
                    bundle.putString("image", path);
                    ImageFragment imageFragment = new ImageFragment();
                    imageFragment.setArguments(bundle);
                    imageFragment.show(getSupportFragmentManager(), "IMAGE_FRAGMENT");
                } else
                    Log.e(PROFILE_ACTIVITY, "user picture link is null");
            }

            @Override
            public void onEditBtnClick(User user) {
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
        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayShowTitleEnabled(false);

        ImageButton goBack = findViewById(R.id.goBack);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        manager = (LinearLayoutManager) groupMembers.getLayoutManager();
        groupMembers.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (manager != null) {
                        onScrollUsers();
                    }

                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (manager != null) {
                    onScrollUsers();

                }
            }
        });

        navigationView = findViewById(R.id.bottomNavigationView);
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
        titles = new ArrayList<>();
        titles.add(messagesSent);
        titles.add(messagesReceived);
        titles.add(timeSpent);
        titles.add(timeSpentLast24);
        titles.add(registrationTime);
        titles.add(blockedConversationAmount);
        titles.add(mutedConversationsAmount);
        titles.add(blockedUserAmount);
        titles.add(mutedUsersTitle);
        titles.add(filesReceivedAmount);
        titles.add(filesSentAmount);
        titles.add(imagesReceivedAmount);
        titles.add(imagesSentAmount);
        titles.add(recordingsReceivedAmount);
        titles.add(recordingSentAmount);
        infoFragment = new ListFragment();
        profileFragment = new ListFragment();
        profileInfoTitles = new ArrayList<>();
        profileInfoTitles.add(name);
        profileInfoTitles.add(lastName);
        profileInfoTitles.add(about);
        profileInfoTitles.add(phoneNumber);
        profileInfoTitles.add(token);

        mutedUsers = new ListFragment();
        blockedFragment = new ListFragment();

        ListAdapter2 listAdapter = new ListAdapter2();
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int x = getSupportFragmentManager().getFragments().size();
                Log.d(PROFILE_ACTIVITY, "amount of fragments:" + x);
                if (item.getItemId() == R.id.info) {
                    listAdapter.setItems(information);
                    listAdapter.setTitles(titles);
                    infoFragment.setAdapter(listAdapter);
                    Log.d(PROFILE_ACTIVITY, "changed to info");
                    getSupportFragmentManager().beginTransaction().replace(R.id.placeholder, infoFragment).commit();
                } else if (item.getItemId() == R.id.media) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.placeholder, gridFragment).commit();
                    Log.d(PROFILE_ACTIVITY, "changed to media");
                } else if (item.getItemId() == R.id.profile) {
                    listAdapter.setItems(dits);
                    listAdapter.setTitles(profileInfoTitles);
                    profileFragment.setAdapter(listAdapter);
                    Log.d(PROFILE_ACTIVITY, "changed to profile");
                    getSupportFragmentManager().beginTransaction().replace(R.id.placeholder, profileFragment).commit();
                } else if (item.getItemId() == R.id.muted) {
                    listAdapter.setItems(mutedDetails);
                    listAdapter.setTitles(mutedTitles);
                    mutedUsers.setAdapter(listAdapter);
                    Log.d(PROFILE_ACTIVITY, "changed to muted");
                    getSupportFragmentManager().beginTransaction().replace(R.id.placeholder, mutedUsers).commit();
                } else if (item.getItemId() == R.id.block) {
                    listAdapter.setItems(blockedDetails);
                    listAdapter.setTitles(blockedTitles);
                    blockedFragment.setAdapter(listAdapter);
                    Log.d(PROFILE_ACTIVITY, "changed to blocked");
                    getSupportFragmentManager().beginTransaction().replace(R.id.placeholder, blockedFragment).commit();
                }
                listAdapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    private void onScrollUsers() {
        int userOnScreenPosition = manager.findFirstVisibleItemPosition();
        user = recipients.get(userOnScreenPosition);
        if (!user.getUserUID().equals(currentUserID)) {
            navigationView.getMenu().findItem(R.id.muted).setVisible(false);
            navigationView.getMenu().findItem(R.id.block).setVisible(false);
        }
        onLoadUserInfo(user);
        navigationView.setSelectedItemId(R.id.profile);
    }

    private void onLoadUserInfo(User user) {
        userName.setText(user.getName());

        information = new ArrayList<>();
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

        dits = new ArrayList<>();
        dits.add(user.getName());
        dits.add(user.getLastName());
        dits.add(user.getAbout());
        dits.add(user.getPhoneNumber());
        dits.add(user.getToken());

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
                        if (title.equals(ProfileActivity3.this.name)) {
                            user.setName(name);
                            userName.setText(user.getName());
                        } else if (title.equals(ProfileActivity3.this.lastName)) {
                            user.setLastName(name);
                        } else if (title.equals(ProfileActivity3.this.about)) {
                            user.setAbout(name);
                        } else if (title.equals(ProfileActivity3.this.phoneNumber)) {
                            user.setPhoneNumber(name);
                        }
                        updateDB();
                    }
                });
                if (profileInfoTitles.get(position).equals(token)) {
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("message", user.getToken());
                    if (clipboardManager != null) {
                        clipboardManager.setPrimaryClip(clipData);
                        Toast.makeText(ProfileActivity3.this, "copied", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(ProfileActivity3.this, "oops, an error has happened", Toast.LENGTH_SHORT).show();
                } else {
                    singleFieldFragment.setHint(profileInfoTitles.get(position));
                    Log.d(PROFILE_ACTIVITY, "info - clicked on: " + profileInfoTitles.get(position));
                    singleFieldFragment.show(getSupportFragmentManager(), "Edit_Content");
                }

            }
        });

        mutedTitles = new ArrayList<>();
        mutedDetails = new ArrayList<>();

        mutedUsers.setListener(new ListFragment.ItemClickListener() {
            @Override
            public void onClickItem(int position) {
//                User user = mutedUsers1.get(position);
//                createConfirmWindow("unmute this conversation?", "Unmute",true, true, user.getUserUID());
            }
        });

        blockedTitles = new ArrayList<>();
        blockedDetails = new ArrayList<>();

        blockedFragment.setListener(new ListFragment.ItemClickListener() {
            @Override
            public void onClickItem(int position) {
//                User user = mutedUsers1.get(position);
//                createConfirmWindow("unmute this conversation?", "Unmute",true, false, user.getUserUID());
            }
        });
        userVM.getAllMutedOrBlockedUsers(true).observe(ProfileActivity3.this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                for (User user : users) {
                    setBlocked(user.getName() + " " + user.getLastName());
                }
                blockedUsers = users;
                userVM.getAllMutedOrBlockedUsers(true).removeObservers(ProfileActivity3.this);
            }
        });
        userVM.getAllMutedOrBlockedUsers(false).observe(ProfileActivity3.this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                for (User user : users) {
                    setMuted(user.getName() + " " + user.getLastName());
                }
                mutedUsers1 = users;
                userVM.getAllMutedOrBlockedUsers(false).removeObservers(ProfileActivity3.this);
            }
        });
        conversationVM.getAllMutedOrBlockedConversations(true).observe(this, new Observer<List<Conversation>>() {
            @Override
            public void onChanged(List<Conversation> conversations) {
                for (Conversation conversation : conversations) {
                    setBlocked(conversation.getConversationName());
                }
                blockedConversations = conversations;
                conversationVM.getAllMutedOrBlockedConversations(true).removeObservers(ProfileActivity3.this);
            }
        });
        conversationVM.getAllMutedOrBlockedConversations(false).observe(this, new Observer<List<Conversation>>() {
            @Override
            public void onChanged(List<Conversation> conversations) {
                for (Conversation conversation : conversations) {
                    setMuted(conversation.getConversationName());
                }
                mutedConversations = conversations;
                conversationVM.getAllMutedOrBlockedConversations(false).removeObservers(ProfileActivity3.this);
            }
        });


        conversationVM.getMediaMessages().observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {
                Log.d(PROFILE_ACTIVITY, "media msg");
                mediaMessages = messages;
                List<String>mediaPaths = new ArrayList<>();
                for (Message message: messages)
                {
                    mediaPaths.add(message.getFilePath());
                }
                gridImageAdapter.setImagePaths(mediaPaths);
                gridFragment.setImageAdapter(gridImageAdapter);
            }
        });
    }

    private void saveAndSetImage() {
        Log.d(PROFILE_ACTIVITY, "save and set message");
        Bitmap imageBitmap = getImageBitmap(imageUri);
        FileManager.getInstance().saveProfileImage(imageBitmap, user.getUserUID(), ProfileActivity3.this, false);
        user.setPictureLink(imageUri.toString());
        adapter.updateUser(user);
        updateDB();
    }

    private Bitmap getImageBitmap(Uri uri) {
        Bitmap image = null;
        try {
            if (Build.VERSION.SDK_INT > 27) {
                ImageDecoder.Source source = ImageDecoder.createSource(ProfileActivity3.this.getContentResolver(), uri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                image = MediaStore.Images.Media.getBitmap(ProfileActivity3.this.getContentResolver(), uri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    private boolean askPermission(MessageType type) {
        if (type == MessageType.photoMessage || type == MessageType.imageMessage) {
            int hasWritePermission = ProfileActivity3.this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
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
        if (!user.getUserUID().equals(currentUserID))
            userVM.updateUserLocal(user);
        else
            userVM.updateUser(user);
    }

    private void takePicture() {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = ProfileActivity3.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File image = File.createTempFile(imageFileName, ".jpg", storageDir);
            setPhotoPath(image.getAbsolutePath());
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(ProfileActivity3.this.getPackageManager()) != null) {
                File photoFile;
                photoFile = image;
                Uri photoURI = FileProvider.getUriForFile(ProfileActivity3.this,
                        "com.example.woofmeow.provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                imageUri = photoURI;
                takePicture.launch(takePictureIntent);
                //startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (user != null)
            if (user.getUserUID().equals(currentUserID)) {
                getMenuInflater().inflate(R.menu.profile_menu, menu);
            }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.delete) {
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

    private synchronized void setBlocked(String name) {
        blockedTitles.add("Name");
        blockedDetails.add(name);
    }

    private synchronized void setMuted(String name) {
        mutedTitles.add("Name");
        mutedDetails.add(name);
    }

    private void createConfirmWindow(String msg, String title, boolean user, boolean mute, String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg).setTitle(title).setCancelable(true).setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (user) {
                    if (mute)
                        userVM.unMuteUser(id);
                    else
                        userVM.unBlockUser(id);
                } else {
                    if (mute)
                        conversationVM.unMuteConversation(id);
                    else
                        conversationVM.unBlockConversation(id);
                }
            }
        }).setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).create().show();
    }
}
