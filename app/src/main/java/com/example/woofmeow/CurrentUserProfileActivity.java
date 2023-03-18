//package com.example.woofmeow;
//
//import android.Manifest;
//import android.annotation.SuppressLint;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.drawable.BitmapDrawable;
//import android.graphics.drawable.Drawable;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Environment;
//import android.provider.MediaStore;
//import android.util.Log;
//import android.view.View;
//
//import android.widget.AdapterView;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ListView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.activity.result.ActivityResult;
//import androidx.activity.result.ActivityResultCallback;
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.content.FileProvider;
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.Observer;
//import androidx.lifecycle.ViewModelProvider;
//
//
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//
//import com.google.android.material.snackbar.Snackbar;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.messaging.FirebaseMessaging;
//
//import java.io.File;
//import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//
//import Adapters.ListAdapter;
//import Adapters.UserListAdapter;
//import Adapters.UsersAdapter2;
//import Backend.ConversationVM;
//import Backend.UserVM;
//
//import Consts.PermissionType;
//import NormalObjects.ImageButtonPlus;
//import NormalObjects.Conversation;
//import NormalObjects.FileManager;
//import NormalObjects.User;
//
//@SuppressWarnings({"Convert2Lambda", "Anonymous2MethodRef"})
//public class CurrentUserProfileActivity extends AppCompatActivity {
//
//    private UserVM userVM;
//    private ConversationVM conversationVM;
//    private LinearLayout linearLayout;
//    private int WRITE_PERMISSION = 2;
//    private  String photoPath;
//    private Uri imageUri;
//    private ActivityResultLauncher<Intent> takePicture;
//    private Bitmap imageBitmap;
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.profile_activity2);
//        ImageView profilePic = findViewById(R.id.profileImage);
//        ImageButton goBack = findViewById(R.id.goBack);
//        goBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
//        User user =(User) getIntent().getSerializableExtra("user");
//        if (user!=null) {
//            takePicture = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
//                @Override
//                public void onActivityResult(ActivityResult result) {
//                    if (result.getResultCode() == RESULT_OK) {
//                        Drawable drawable = Drawable.createFromPath(photoPath);
//                        if (drawable != null) {
//                            profilePic.setImageDrawable(drawable);
//                            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
//                            imageBitmap = bitmapDrawable.getBitmap();
//                            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, 500, 450, false);
//                            userVM.updateUserImage(user, imageBitmap, CurrentUserProfileActivity.this);
//                        }
//                    }
//                }
//            });
//            TextView userName = findViewById(R.id.username);
//            String fullUserName = user.getName() + " " + user.getLastName();
//            userName.setText(fullUserName);
//            ListView dits = findViewById(R.id.userDetails);
//            ListAdapter adapter = new ListAdapter();
//            dits.setAdapter(adapter);
//           // DBActive db = DBActive.getInstance(this);
//            userVM = new ViewModelProvider(this).get(UserVM.class);
//            conversationVM = new ViewModelProvider(this).get(ConversationVM.class);
//            linearLayout = findViewById(R.id.rootLayout);
//            TextView title = findViewById(R.id.title);
//            title.setText("");
//
//
//            FileManager fm = FileManager.getInstance();
//            Bitmap bitmap = fm.readImage(this, FileManager.user_profile_images, user.getUserUID());
//            if (bitmap!=null)
//                profilePic.setImageBitmap(bitmap);
//            else
//                Log.e("Bitmap", "currentUser profile bitmap is null");
//            profilePic.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    requestCamera();
//                }
//            });
////            ImageButtonPlus muteBtn =  findViewById(R.id.mute);
//            ImageButtonPlus blockBtn = findViewById(R.id.block);
////            muteBtn.setResetOnValue(2);
////            muteBtn.setOnClickListener(new View.OnClickListener() {
////                @Override
////                public void onClick(View v) {
////                    //shows all muted users and conversations
////                    blockBtn.setPressCycle(-1);
////                    if (muteBtn.getPressCycle() == 1)
////                    {
////                        title.setText(R.string.muted_conversations);
////                        LiveData<List<Conversation>> mutedConversations = conversationVM.getAllMutedOrBlockedConversations(false);
////                        mutedConversations.observe(CurrentUserProfileActivity.this, new Observer<List<Conversation>>() {
////                            @Override
////                            public void onChanged(List<Conversation> conversations) {
////                                adapter.setConversations(conversations);
////                            }
////                        });
////                    }
////                    else if (muteBtn.getPressCycle() == 2)
////                    {
////                        title.setText(R.string.muted_users);
////                        LiveData<List<User>>mutedUsers = userVM.getAllMutedOrBlockedUsers(false);
////                        mutedUsers.observe(CurrentUserProfileActivity.this, new Observer<List<User>>() {
////                            @Override
////                            public void onChanged(List<User> users) {
////                                adapter.setUsers(users);
////                            }
////                        });
////
////                    }
////                    else
////                    {
////                        title.setText("");
////                    }
////                }
////            });
//            blockBtn.setResetOnValue(2);
//            blockBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    //shows all blocked users and conversations
////                    muteBtn.setPressCycle(-1);
//                    if (blockBtn.getPressCycle() == 1)
//                    {
//                        title.setText(R.string.blocked_conversations);
//                        LiveData<List<Conversation>> blockedConversations = conversationVM.getAllMutedOrBlockedConversations(true);
//                        blockedConversations.observe(CurrentUserProfileActivity.this, new Observer<List<Conversation>>() {
//                            @Override
//                            public void onChanged(List<Conversation> conversations) {
//                                adapter.setConversations(conversations);
//                            }
//                        });
//                    }
//                    else if (blockBtn.getPressCycle() == 2)
//                    {
//                        title.setText(R.string.blocked_users);
//                        LiveData<List<User>>blockedUsers = userVM.getAllMutedOrBlockedUsers(true);
//                        blockedUsers.observe(CurrentUserProfileActivity.this, new Observer<List<User>>() {
//                            @Override
//                            public void onChanged(List<User> users) {
//                                adapter.setUsers(users);
//                            }
//                        });
//                    }
//                    else
//                    {
//                        title.setText("");
//                    }
//                }
//            });
////            ImageButton stats = findViewById(R.id.stats);
////            stats.setOnClickListener(new View.OnClickListener() {
////                @Override
////                public void onClick(View v) {
////                    //shows the stats of this account
////                    UserListAdapter userListAdapter = new UserListAdapter();
////                    List<String> details = new ArrayList<>();
////                    details.add("Name: " + user.getName());
////                    details.add("Last Name: " + user.getLastName());
////                    details.add("your id: " + user.getUserUID());
////                    details.add("created at: " + user.getTimeCreated());
////                    details.add("your status: " + user.getStatus());
////                    userListAdapter.setDetails(details);
////                }
////            });
////            stats.setVisibility(View.VISIBLE);
//            ImageButton deleteBtn = findViewById(R.id.delete);
//            deleteBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    //deletes this account and all the data corresponding to it
//                    AlertDialog.Builder builder = new AlertDialog.Builder(CurrentUserProfileActivity.this);
//                    builder.setTitle("Delete account?")
//                            .setMessage("Are you sure you would like to delete your account? it will delete all data and is not recoverable!")
//                            .setIcon(R.drawable.ic_baseline_delete_black)
//                            .setCancelable(true)
//                            .setPositiveButton("Delete!", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    userVM.reset();
//                                    FirebaseMessaging.getInstance().deleteToken();
//                                    if (FirebaseAuth.getInstance().getCurrentUser() != null)
//                                        FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<Void> task) {
//                                                finishAffinity();
//                                            }
//                                        });
//                                }
//                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                        }
//                    }).create().show();
//                }
//            });
//            deleteBtn.setVisibility(View.VISIBLE);
//            dits.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
////                    if (muteBtn.getPressCycle() != -1)
////                    {
////                        if (adapter.getItem(0) instanceof Conversation)
////                        {
////                            Conversation conversation = (Conversation) adapter.getItem(position);
////                            muteConversation(conversation.getConversationID());
////                        }
////                        else
////                        {
////                            User recipient = (User) adapter.getItem(position);
////                            muteUser(recipient.getUserUID());
////                        }
////                    }
////                    else if (blockBtn.getPressCycle() != -1)
//                    {
//                        if (adapter.getItem(0) instanceof User)
//                        {
//                            User recipient = (User) adapter.getItem(position);
//                            blockUser(recipient.getUserUID());
//                        }
//                        else
//                        {
//                            Conversation conversation = (Conversation) adapter.getItem(position);
//                            blockConversation(conversation.getConversationID());
//
//                        }
//                    }
//                }
//            });
//        }
//    }
//
//    private void requestCamera()
//    {
//        if(AskPermission(PermissionType.storage))
//            takePicture();
//    }
//
//    private boolean AskPermission(PermissionType type)
//    {
//        switch (type)
//        {
//            case storage:
//                int hasWritePermission = CurrentUserProfileActivity.this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//                if(hasWritePermission != PackageManager.PERMISSION_GRANTED){
//                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_PERMISSION);
//                    return false;
//                }
//                else return true;
//        }
//        return false;
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if(requestCode == WRITE_PERMISSION)
//        {
//            if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
//                Toast.makeText(CurrentUserProfileActivity.this, "permission is required to use the camera", Toast.LENGTH_SHORT).show();
//            else
//                takePicture();
//        }
//    }
//
//    private void takePicture()
//    {
//        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = CurrentUserProfileActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        try {
//            File image = File.createTempFile(imageFileName,".jpg",storageDir);
//            photoPath = image.getAbsolutePath();
//            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            if(takePictureIntent.resolveActivity(CurrentUserProfileActivity.this.getPackageManager())!=null)
//            {
//                File photoFile;
//                photoFile = image;
//                Uri photoURI = FileProvider.getUriForFile(CurrentUserProfileActivity.this,
//                        "com.example.woofmeow.provider",photoFile);
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoURI);
//
//                imageUri = photoURI;
//                takePicture.launch(takePictureIntent);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    public void muteConversation(String conversationID)
//    {
//        LiveData<Boolean> mutedConversation = conversationVM.isConversationMuted(conversationID);
//        mutedConversation.observe(this, new Observer<Boolean>() {
//            @Override
//            public void onChanged(Boolean aBoolean) {
//                String dialog;
//                if(!aBoolean)
//                {
//                    dialog = "Conversation was Muted";
//                    conversationVM.muteConversation(conversationID);
//                }
//                else
//                {
//                    dialog = "Conversation was unMuted";
//                    conversationVM.unMuteConversation(conversationID);
//                }
//                mutedConversation.removeObservers(CurrentUserProfileActivity.this);
//                Snackbar.make(linearLayout,dialog, Snackbar.LENGTH_SHORT)
//                        .setAction("undo", new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                muteConversation(conversationID);
//                            }
//                        }).show();
//                mutedConversation.removeObservers(CurrentUserProfileActivity.this);
//            }
//        });
//    }
//
//    public void muteUser(String uid)
//    {
//        LiveData<Boolean>muteUser = userVM.isUserMuted(uid);
//        muteUser.observe(CurrentUserProfileActivity.this, new Observer<Boolean>() {
//            @Override
//            public void onChanged(Boolean aBoolean) {
//                String showText, actionText;
//                if (aBoolean)
//                {
//                    userVM.unMuteUser(uid);
//                    showText = "user was un muted";
//                    actionText = "mute";
//                }
//                else
//                {
//                    userVM.muteUser(uid);
//                    showText = "user was muted";
//                    actionText = "un mute";
//                }
//                Snackbar.make(linearLayout, showText, Snackbar.LENGTH_SHORT)
//                        .setAction(actionText, new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                muteUser(uid);
//                            }
//                        }).show();
//                muteUser.removeObservers(CurrentUserProfileActivity.this);
//            }
//        });
//    }
//
//    public void blockConversation(String conversationID)
//    {
//        LiveData<Boolean>blockedConversation = conversationVM.isConversationBlocked(conversationID);
//        blockedConversation.observe(this, new Observer<Boolean>() {
//            @Override
//            public void onChanged(Boolean aBoolean) {
//                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(CurrentUserProfileActivity.this);
//                if (aBoolean)
//                {
//                    builder.setTitle("un block conversation")
//                            .setMessage("unblock this conversation to start receiving messages from the conversation")
//                            .setPositiveButton("unblock", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    conversationVM.unBlockConversation(conversationID);
//                                    Toast.makeText(CurrentUserProfileActivity.this, "conversation was unblocked", Toast.LENGTH_SHORT).show();
//                                }
//                            }).setCancelable(true)
//                            .create()
//                            .show();
//                }
//                else
//                {
//                    builder.setTitle("block conversation")
//                            .setMessage("block this conversation to stop receiving messages from this conversation")
//                            .setPositiveButton("block", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    conversationVM.blockConversation(conversationID);
//                                    Toast.makeText(CurrentUserProfileActivity.this, "conversation was blocked", Toast.LENGTH_SHORT).show();
//                                }
//                            }).setCancelable(true)
//                            .create()
//                            .show();
//
//                }
//                blockedConversation.removeObservers(CurrentUserProfileActivity.this);
//            }
//        });
//    }
//
//    public void blockUser(String uid)
//    {
//        LiveData<Boolean>blockUser = userVM.isUserBlocked(uid);
//        blockUser.observe(CurrentUserProfileActivity.this, new Observer<Boolean>() {
//            @Override
//            public void onChanged(Boolean aBoolean) {
//                String showText, actionText;
//                if (aBoolean)
//                {
//                    showText = "user was un blocked";
//                    actionText = "block";
//                    userVM.unBlockUser(uid);
//                }
//                else
//                {
//                    showText = "user was blocked";
//                    actionText = "un block";
//                    userVM.blockUser(uid);
//                }
//                Snackbar.make(linearLayout, showText, Snackbar.LENGTH_SHORT)
//                        .setAction(actionText, new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                blockUser(uid);
//                            }
//                        }).show();
//                blockUser.removeObservers(CurrentUserProfileActivity.this);
//            }
//        });
//    }
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//    }
//}
