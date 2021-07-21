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
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private final String currentUser = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
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
                if (item.getItemId() == R.id.profile)
                {
                    Intent intent = new Intent(MainActivity.this,  CurrentUserProfileActivity.class);
                    intent.putExtra("currentUser", user);
                    startActivity(intent);
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                else if (item.getItemId() == R.id.disconnect)
                {
                    FirebaseMessaging.getInstance().deleteToken();
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(MainActivity.this, FirstPageActivity.class);
                    startActivity(intent);
                    finish();
                }
                else if (item.getItemId() == R.id.settings)
                {
                    startActivityForResult(new Intent(MainActivity.this, PreferenceActivity.class), SETTINGS_REQUEST);
                    drawerLayout.closeDrawer(GravityCompat.START);
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
                        NewChatFragment2 chatFragment2 = NewChatFragment2.newInstance();
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.add(R.id.drawerLayout, chatFragment2, "tag");
                        transaction.addToBackStack(null);
                        transaction.commit();

                        break;
                    case groups:
                    {

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
                    case groups:
                    {
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
        LoadCurrentUserImage();
        shapeableImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CurrentUserProfileActivity.class).putExtra("currentUser",user));
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
        System.out.println("getIntent:" + getIntent());


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
    public void onLoadUserFromMemory(User user){
        this.user = user;
    }

    @Override
    public void onUserUpdate(User user) {
        if (user != null) {
            if (user.getUserUID().equals(currentUser)) {//us
                this.user = user;
                FirebaseMessageService.myName = user.getName();
                SharedPreferences savedImagesPreferences = getSharedPreferences("SavedImages", Context.MODE_PRIVATE);
                if (savedImagesPreferences.getBoolean(user.getUserUID(),false))
                    LoadCurrentUserImage();
                else
                {
                    SaveCurrentUserImage();

                }
                currentStatus = user.getStatus();
                onUserUpdate = true;
                UpdateMuted(user.getMutedConversations());
                UpdateBlocked(user.getBlockedUsers());
                invalidateOptionsMenu();
            }

        }
    }

    private void LoadCurrentUserImage()
    {
        try {
            ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
            File directory = contextWrapper.getDir("user_images", Context.MODE_PRIVATE);
            File imageFile = new File(directory,currentUser + "_Image");
            Bitmap imageBitmap = BitmapFactory.decodeStream(new FileInputStream(imageFile));
            profileImage.setImageBitmap(imageBitmap);//loads user image to drawer header
            shapeableImageView.setImageBitmap(imageBitmap);//loads user image to toolbar image
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void SaveCurrentUserImage()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users/" + currentUser + "/pictureLink");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String pictureLink = snapshot.getValue(String.class);
                Picasso.get().load(pictureLink).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                        profileImage.setImageBitmap(bitmap);//loads user image to drawer header
                        shapeableImageView.setImageBitmap(bitmap);//loads user image to toolbar image

                        ContextWrapper contextWrapper = new ContextWrapper(MainActivity.this.getApplicationContext());
                        File directory = contextWrapper.getDir("user_images", Context.MODE_PRIVATE);
                        if (!directory.exists())
                            if (!directory.mkdir()) {
                                Log.e("error", "couldn't create a directory in conversationAdapter2");
                            }
                        File Path = new File(directory, currentUser + "_Image");
                        try {
                            FileOutputStream fileOutputStream = new FileOutputStream(Path);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                            fileOutputStream.close();
                            SharedPreferences savedImagesPreferences = getSharedPreferences("SavedImages", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = savedImagesPreferences.edit();
                            editor.putBoolean(currentUser,true);
                            editor.apply();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        Log.e("Error","couldn't load image");
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
                reference.removeEventListener(this);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                error.toException().printStackTrace();
            }
        });
    }

    @Override
    public void onConversationAction() {

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
                if (fragment instanceof TabFragment)
                {
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
        System.out.println("going off");
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



}