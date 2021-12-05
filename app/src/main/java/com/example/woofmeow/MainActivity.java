package com.example.woofmeow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import Consts.Tabs;
import Fragments.NewChatFragment2;
import Fragments.TabFragment;
import NormalObjects.*;

import Services.FirebaseMessageService;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

//this app is to be published
public class MainActivity extends AppCompatActivity implements TabFragment.UpdateMain, NewChatFragment2.NewChat2 {

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
    private boolean search = false;

    private boolean onUserUpdate = false;
    private PagerAdapter pagerAdapter;

    @SuppressWarnings("Convert2Lambda")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (currentUser == null) {
            SharedPreferences sharedPreferences = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
            String uid = sharedPreferences.getString("UID", "ERROR: NO UID");
            if (!uid.equals("ERROR: NO UID"))
                currentUser = uid;
        }
        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolbar);
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
        floatingActionButton = findViewById(R.id.floatingActionButton);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.profile) {
                    Intent intent = new Intent(MainActivity.this, CurrentUserProfileActivity.class);
                    intent.putExtra("currentUser", user);
                    startActivity(intent);
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else if (item.getItemId() == R.id.disconnect) {
                    FirebaseMessaging.getInstance().deleteToken();
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(MainActivity.this, FirstPageActivity.class);
                    startActivity(intent);
                    finish();
                } else if (item.getItemId() == R.id.settings) {
                    startActivityForResult(new Intent(MainActivity.this, PreferenceActivity.class), SETTINGS_REQUEST);
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else if (item.getItemId() == R.id.backUp) {
                    UploadFileToDropbox();
                }
                return false;
            }
        });
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tabs tab = Tabs.values()[pagePosition];
                switch (tab) {

                    case chat:
                        Intent singleChat = new Intent(MainActivity.this, NewGroupChat.class);
                        startActivity(singleChat);

                        break;
                    case somethingElse: {

                        Intent intent = new Intent(MainActivity.this, NewGroupChat.class);
                        startActivity(intent);
                        break;
                    }
                    default:
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

        profileImage = headerView.findViewById(R.id.headerImage);
        shapeableImageView = findViewById(R.id.toolbarProfileImage);
        if (getIntent().getBooleanExtra("newUser", false))
            user = (User) getIntent().getSerializableExtra("user");
        LoadCurrentUserImage();
        shapeableImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(MainActivity.this, CurrentUserProfileActivity.class);
                profileIntent.putExtra("currentUser", user);
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
        ConnectedToInternet();
        //DropBox();
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
                    break;
                case OFFLINE_S:
                    item.setIcon(R.drawable.circle_red);
                    currentStatus = STANDBY_S;
                    break;
                case STANDBY_S:
                    item.setIcon(R.drawable.circle_yellow);
                    currentStatus = ONLINE_S;
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
        Picasso.get().load(user.getPictureLink()).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                FileManager fileManager = FileManager.getInstance();
                fileManager.SaveUserImage(bitmap, currentUser, MainActivity.this);
                profileImage.setImageBitmap(bitmap);//loads user image to drawer header
                shapeableImageView.setImageBitmap(bitmap);//loads user image to toolbar image
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                Log.e("failed to load bitmap", "picasso failed to load bitmap mainActivity");
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }

    @Override
    public void onUserUpdate(User user) {
        if (user != null) {
            if (user.getUserUID().equals(currentUser)) {//us
                this.user = user;
                FirebaseMessageService.myName = user.getName();
                Picasso.get().load(user.getPictureLink()).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        FileManager fileManager = FileManager.getInstance();
                        fileManager.SaveUserImage(bitmap, currentUser, MainActivity.this);
                        profileImage.setImageBitmap(bitmap);//loads user image to drawer header
                        shapeableImageView.setImageBitmap(bitmap);//loads user image to toolbar image
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        Log.e("failed to load bitmap", "picasso failed to load bitmap mainActivity");
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
                currentStatus = user.getStatus();
                onUserUpdate = true;
                UpdateMuted(user.getMutedConversations());
                UpdateBlocked(user.getBlockedUsers());
                invalidateOptionsMenu();
            }

        }
    }

    private void LoadCurrentUserImage() {
        FileManager fileManager = FileManager.getInstance();
        Bitmap bitmap = fileManager.getSavedImage(this, currentUser + "_Image");
        if (bitmap != null) {
            profileImage.setImageBitmap(bitmap);
            shapeableImageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onUserQuery(User user) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("tag");
        if (fragment instanceof NewChatFragment2) {
            NewChatFragment2 newChatFragment2 = (NewChatFragment2) fragment;
            Bundle bundle = new Bundle();
            bundle.putSerializable("user", user);
            newChatFragment2.setArguments(bundle);
        }
    }

    @Override
    public void onNewMessage(boolean group) {
        if (!group) {
            TabLayout.Tab tab = tabLayout.getTabAt(viewPager.getCurrentItem());
            if (tab != null) {
                String tabTitle = pagerAdapter.getPageTitle(viewPager.getCurrentItem()) + "";
                String[] split = tabTitle.split(" ");
                StringBuilder builder = new StringBuilder();
                if (split.length > 1) {
                    int messageCount = Integer.parseInt(split[0]);
                    builder.append(messageCount);

                } else {
                    builder.append("1");
                }
                builder.append(" ");
                builder.append(tabTitle);
                tab.setText(builder.toString());
            }

        }
    }

    private void UpdateBlocked(ArrayList<String> blocked) {
        SharedPreferences sharedPreferences = getSharedPreferences("blocked", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().apply();
        for (String block : blocked)
            editor.putString(block, block);
        editor.apply();
    }

    private void UpdateMuted(ArrayList<String> muted) {
        SharedPreferences sharedPreferences = getSharedPreferences("muted", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().apply();
        for (String mute : muted)
            editor.putString(mute, mute);
        editor.apply();
    }

    @Override
    public void onNewQuery(String query) {
        Toast.makeText(this, "searching...", Toast.LENGTH_SHORT).show();
        Bundle bundle = new Bundle();
        search = false;
        bundle.putString("query", query);
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragmentList) {
            if (!search)
                if (fragment instanceof TabFragment) {
                    fragment.setArguments(bundle);
                    search = true;
                }
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
}