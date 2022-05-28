package com.example.woofmeow;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.v2.DbxClientV2;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import Backend.ConversationVM;
import Backend.UserVM;
import Consts.MessageType;
import Consts.Tabs;
import Fragments.BackdropFragment;
import Fragments.GifBackdropFragment;
import Fragments.TabFragment;
import Messages.BaseMessage;
import Messages.SendMessage;
import Messages.TextMessage;
import NormalObjects.*;

import Retrofit.Server;
import Services.FirebaseMessageService;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

//this app is to be published
public class MainActivity extends AppCompatActivity implements TabFragment.UpdateMain {

    private DrawerLayout drawerLayout;
    private CoordinatorLayout coordinatorLayout;
    private User user;
    private ImageView profileImage;
    private ShapeableImageView shapeableImageView;
    private int pagePosition = 0;
    private final int SETTINGS_REQUEST = 4;
    private FloatingActionButton floatingActionButton;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private ViewPager viewPager;
    private String currentUser = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    public static final String ONLINE_S = "online";
    public static final String STANDBY_S = "standby";
    public static final String OFFLINE_S = "offline";
    private String currentStatus = ONLINE_S;
    private final String MISSED_MESSAGES = "missed messages";
    private boolean onUserUpdate = false;
    private PagerAdapter pagerAdapter;
    private boolean isRotate = false;
    private ExtendedFloatingActionButton smsBtn, chatBtn, groupBtn;
    private final int READ_SMS = 1;
    private UserVM userVM;
    private ActivityResultLauncher<Intent>settings;
    @SuppressWarnings("Convert2Lambda")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EmojiManager.install(new IosEmojiProvider());
        setContentView(R.layout.activity_main);
        if (currentUser == null) {
            SharedPreferences sharedPreferences = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
            String uid = sharedPreferences.getString("UID", "ERROR: NO UID");
            if (!uid.equals("ERROR: NO UID"))
                currentUser = uid;
        }
        try {
            Class.forName("dalvik.system.CloseGuard")
                    .getMethod("setEnabled", boolean.class)
                    .invoke(null, true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        userVM = new ViewModelProvider(MainActivity.this).get(UserVM.class);
        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setPopupTheme(R.style.single);
        setSupportActionBar(toolbar);
        tabLayout = findViewById(R.id.tabs_layout);
        coordinatorLayout = findViewById(R.id.coordinator1);
        viewPager = findViewById(R.id.viewPager);
        NavigationView navigationView = findViewById(R.id.navigationView);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24);
        // actionBar.setDisplayShowTitleEnabled(false);
        View headerView = navigationView.getHeaderView(0);
        profileImage = headerView.findViewById(R.id.headerImage);
        shapeableImageView = findViewById(R.id.toolbarProfileImage);
        floatingActionButton = findViewById(R.id.floatingActionButton);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.profile) {
                    Intent intent = new Intent(MainActivity.this, CurrentUserProfileActivity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else if (item.getItemId() == R.id.disconnect) {
                    FirebaseMessaging.getInstance().deleteToken();
                    FirebaseAuth.getInstance().signOut();
                    userVM.reset();
                    Intent intent = new Intent(MainActivity.this, FirstPageActivity.class);
                    startActivity(intent);
                    finish();
                } else if (item.getItemId() == R.id.settings) {
                    settings.launch(new Intent(MainActivity.this, PreferenceActivity.class));
//                    startActivityForResult(new Intent(MainActivity.this, PreferenceActivity.class), SETTINGS_REQUEST);
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                return false;
            }
        });
        settings = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

            }
        });
        smsBtn = findViewById(R.id.smsConversation);
        chatBtn = findViewById(R.id.chatConversation);
        groupBtn = findViewById(R.id.groupConversation);

        smsBtn.shrink();
        chatBtn.shrink();
        groupBtn.shrink();
        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tabs tab = Tabs.values()[pagePosition];
                switch (tab) {
                    case chat:
                        Intent chat = new Intent(MainActivity.this, NewChat.class);
                        rotateAndShowOut();
                        startActivity(chat);
                        break;
                    case somethingElse: {
                        //probably calls
                        break;
                    }
                    default:
                }
            }
        });
        smsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //opens sms conversation
                if (askPermission(MessageType.sms)) {
                    Intent sms = new Intent(MainActivity.this, NewSMS.class);
                    rotateAndShowOut();
                    startActivity(sms);
                }
            }
        });
        groupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tabs tab = Tabs.values()[pagePosition];
                switch (tab) {
                    case chat:
                        Intent chat = new Intent(MainActivity.this, NewGroupChat2Activity.class);
                        rotateAndShowOut();
                        startActivity(chat);
                        break;
                    case somethingElse:
                        //probably video calls
                        break;
                }

            }
        });
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRotate = rotateFab(floatingActionButton, !isRotate);
                if (isRotate) {
                    showIn(smsBtn);
                    showIn(groupBtn);
                    showIn(chatBtn);
                    int smsWidth = smsBtn.getMinWidth();
                    int chatWidth = chatBtn.getMinWidth();
                    int groupWidth = groupBtn.getMinWidth();
                    int bigger = Math.max(smsWidth,chatWidth);
                    bigger = Math.max(bigger,groupWidth);
                    smsBtn.setMinWidth(bigger);
                    chatBtn.setMinWidth(bigger);
                    groupBtn.setMinWidth(bigger);
                } else {
                    showOut(smsBtn);
                    showOut(groupBtn);
                    showOut(chatBtn);
                }
            }
        });
        pagerAdapter = new PagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                pagePosition = position;
                Tabs tab = Tabs.values()[position];
                switch (tab) {
                    case chat:
                        floatingActionButton.setImageResource(R.drawable.ic_baseline_message_24);
                        break;
                    case somethingElse: {
                        floatingActionButton.setImageResource(R.drawable.ic_baseline_group_24);
                        break;
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if (getIntent().getBooleanExtra("newUser", false))
            user = (User) getIntent().getSerializableExtra("user");
        LoadCurrentUserPicture();
        shapeableImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(MainActivity.this, CurrentUserProfileActivity.class);
                profileIntent.putExtra("user", user);
                startActivity(profileIntent);
                //startActivity(new Intent(MainActivity.this, CurrentUserProfileActivity.class).putExtra("currentUser",user));
            }
        });
        createNotificationChannel();
        Intent sharedDataIntent = getIntent();
        String action = sharedDataIntent.getAction();
        String type = sharedDataIntent.getType();
        if (action != null) {
            if (action.equals(Intent.ACTION_SEND)) {

                if ("text/plain".equals(type)) {
                    String extraText = sharedDataIntent.getStringExtra(Intent.EXTRA_TEXT);
                    if (extraText != null)
                        ShareTextMessage(extraText);
                }
            }
        }
        //ConnectedToInternet();
        //DropBox();
    }

    private void rotateAndShowOut() {
        rotateFab(floatingActionButton, !isRotate);
        showOut(smsBtn);
        showOut(groupBtn);
        showOut(chatBtn);
        isRotate = false;
    }

    private boolean rotateFab(FloatingActionButton btn, boolean rotate) {
        btn.animate().setDuration(200).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        }).rotation(rotate ? 180f : 0f);
        return rotate;
    }

    private void showIn(ExtendedFloatingActionButton btn) {
        btn.setVisibility(View.VISIBLE);
        btn.setAlpha(0f);
        btn.setTranslationY(btn.getHeight());
        btn.animate().setDuration(200).translationY(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                btn.extend();
            }
        }).alpha(1f).start();
    }

    private void showOut(ExtendedFloatingActionButton btn) {
        btn.shrink(new ExtendedFloatingActionButton.OnChangedCallback() {
            @Override
            public void onShrunken(ExtendedFloatingActionButton extendedFab) {
                super.onShrunken(extendedFab);
                extendedFab.setVisibility(View.VISIBLE);
                extendedFab.setAlpha(1f);
                extendedFab.setTranslationY(0);
                extendedFab.animate().setDuration(200).translationY(btn.getHeight()).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {

                        extendedFab.setVisibility(View.GONE);
                        super.onAnimationEnd(animation);
                    }
                }).alpha(0f).start();
            }
        });

    }

    private void ShareTextMessage(String text) {

        String[] textSplit = text.split("\n");
        String title = "";
        String link = "";
        for (String s : textSplit) {
            if (s.startsWith("http"))
                link = s;
            else
                title = s;
        }
        if (title.equals("")) {
            try {
                URL url = new URL(text);
                title = url.getHost();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

        }
        SharedPreferences sharedPreferences = getSharedPreferences("share", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("title", title);
        editor.putString("link", link);
        editor.apply();


    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        //get shared data from other apps
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String extraText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (extraText != null)
                    ShareTextMessage(extraText);
            }
        }

    }


    private void DropBox() {
        ArrayList<String> scopes = new ArrayList<>();
        scopes.add("account_info.read");
        scopes.add("files.content.write");
        Auth.startOAuth2PKCE(this, "lc09zc1m7qr0ubu", DbxRequestConfig.newBuilder("try").build(), scopes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean darkMode = preferences.getBoolean("darkView", false);
        if (darkMode) {
            floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.black, getTheme())));
            tabLayout.setBackgroundColor(getResources().getColor(android.R.color.black, getTheme()));
            toolbar.setBackgroundColor(getResources().getColor(android.R.color.black, getTheme()));
            tabLayout.setTabTextColors(ColorStateList.valueOf(getResources().getColor(android.R.color.white, getTheme())));
        } else {
            floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.white, getTheme())));
            tabLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark, getTheme()));
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark, getTheme()));
            tabLayout.setTabTextColors(ColorStateList.valueOf(getResources().getColor(android.R.color.darker_gray, getTheme())));
        }
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(android.R.color.white, getTheme()));
        /* DbxCredential credentials =  Auth.getDbxCredential();
        if(credentials!=null) {
            String clientIdentifier = "try";
            DbxRequestConfig config = new DbxRequestConfig(clientIdentifier);
            DbxClientV2 dbxClientV2 = new DbxClientV2(config, credentials);
            System.out.println("credentials" + credentials.toString());
            System.out.println("dbxClientV2" + dbxClientV2.toString());
            //should save to memory

        }*/
    }


    private void UploadFileToDropbox() {
        String clientIdentifier = "try";
        DbxRequestConfig config = new DbxRequestConfig(clientIdentifier);
        DbxCredential credentials = Auth.getDbxCredential();
        if (credentials != null) {
            DbxClientV2 dbxClientV2 = new DbxClientV2(config, credentials);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.status);
        if (currentStatus != null && onUserUpdate) {
            switch (currentStatus) {
                case ONLINE_S:
                    item.setIcon(R.drawable.circle_green);
                    currentStatus = OFFLINE_S;
                    user.setStatus(currentStatus);
                    userVM.updateUser(user);
                    break;
                case OFFLINE_S:
                    item.setIcon(R.drawable.circle_red);
                    currentStatus = STANDBY_S;
                    user.setStatus(currentStatus);
                    userVM.updateUser(user);
                    break;
                case STANDBY_S:
                    item.setIcon(R.drawable.circle_yellow);
                    currentStatus = ONLINE_S;
                    user.setStatus(currentStatus);
                    userVM.updateUser(user);
                    break;
            }
            onUserUpdate = false;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onLoadUserFromMemory(User user) {
        if (user != null) {
            this.user = user;
            LoadCurrentUserPicture();
        }
    }

    private void LoadCurrentUserPicture() {
        FileManager fileManager = FileManager.getInstance();
        Bitmap profileBitmap = fileManager.readImage(this, FileManager.user_profile_images, currentUser);
        if (profileBitmap == null)
        {
            shapeableImageView.setImageResource(R.drawable.ic_baseline_account_circle_black);
            profileImage.setImageResource(R.drawable.ic_baseline_account_circle_black);
            Log.e("ProfileBitmap", "profile bitmap is null");
        }
        else
        {
            profileImage.setImageBitmap(profileBitmap);
            shapeableImageView.setImageBitmap(profileBitmap);
        }
        if(user!=null) {
            downloadUserImage(user.getPictureLink());
        }
    }

    @Override
    public void onUserUpdate(User user) {
        if (user != null) {
            if (user.getUserUID().equals(currentUser)) {//us
                this.user = user;
                FirebaseMessageService.myName = user.getName();
                downloadUserImage(user.getPictureLink());
                currentStatus = user.getStatus();
                onUserUpdate = true;
                invalidateOptionsMenu();
            }

        }
        else Log.e("ERROR", "mainActivity - onUserUpdate - user is null");
    }

    private void downloadUserImage(String imagePath)
    {
        userVM.setOnUserImageDownloadListener(new Server.onFileDownload() {
            @Override
            public void onDownloadStarted() {

            }

            @Override
            public void onProgress(int progress) {

            }

            @Override
            public void onDownloadFinished(File file) {
                String filePath = file.getAbsolutePath();
                Bitmap image = BitmapFactory.decodeFile(filePath);
                FileManager fileManager = FileManager.getInstance();
                fileManager.saveProfileImage(image, currentUser, MainActivity.this, false);
                userVM.setOnUserImageDownloadListener(null);
                profileImage.setImageBitmap(image);//loads user image to drawer header
                shapeableImageView.setImageBitmap(image);//loads user image to toolbar image
            }

            @Override
            public void onFileDownloadFinished(String messageID, File file) {

            }

            @Override
            public void onDownloadError(String errorMessage) {
                if (profileImage.getDrawable() == null)
                    Toast.makeText(MainActivity.this, "an error happened when downloading your profile image", Toast.LENGTH_SHORT).show();
            }
        });
        userVM.downloadImage(imagePath);
    }

    @Override
    public void onOpenedConversation(String conversationID) {
        SharedPreferences sharedPreferences = getSharedPreferences(MISSED_MESSAGES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        boolean missedConversation = sharedPreferences.getBoolean(conversationID, false);
        if (missedConversation) {
            editor.putBoolean(conversationID, false);
            editor.apply();
        }
    }


    @SuppressWarnings("InnerClassMayBeStatic")
    private class PagerAdapter extends FragmentStatePagerAdapter {

        public PagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return TabFragment.newInstance(position, currentUser);
        }

        @Override
        public int getCount() {
            return Tabs.values().length;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return Tabs.values()[position].toString();
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = getSharedPreferences("share", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("title");
        editor.remove("link");
        editor.apply();
    }


    private void createNotificationChannel() {

        CharSequence channelName = "BackgroundMessages";
        String description = "shows new messages";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        String CHANNEL_ID = "MessagesChannel";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = MainActivity.this.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_SMS && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent sms = new Intent(MainActivity.this, NewSMS.class);
            rotateAndShowOut();
            startActivity(sms);
        } else if (requestCode == READ_SMS && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "can't start sms conversation without read sms permission", Toast.LENGTH_SHORT).show();
        }
    }

    private void ConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI).addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_VPN);
        NetworkRequest request = builder.build();
        Network2 network2 = Network2.getInstance();
        network2.setListener(new NetworkChange() {
            @Override
            public void onNetwork() {
                Toast.makeText(MainActivity.this, "Internet connection established", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNoNetwork() {
                Toast.makeText(MainActivity.this, "Internet connection is down", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNetworkLost() {
                Toast.makeText(MainActivity.this, "Lost network", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChangedNetworkType() {

            }
        });
        if (connectivityManager != null)
            connectivityManager.registerNetworkCallback(request, network2);

    }


    private boolean askPermission(MessageType messageType) {
        switch (messageType) {
            case sms:
                int hasPermission = this.checkSelfPermission(Manifest.permission.RECEIVE_SMS);
                if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS}, READ_SMS);
                    return false;
                } else return true;
            default:
                Log.e("ERROR", "ask permission error in main activity");
        }
        return false;
    }
}