package com.example.woofmeow;

import android.Manifest;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;


import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.ImageDecoder;


import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.os.Handler;

import android.os.Looper;

import android.provider.BaseColumns;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;

import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;


import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import android.widget.ImageSwitcher;
import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import androidx.core.content.res.ResourcesCompat;


import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;


import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import Adapters.ChatAdapter;

import BroadcastReceivers.AlarmReceiverBroadcast;
import Consts.ButtonType;
import Consts.MessageType;


import Controller.CController;
import Fragments.BackdropFragment;
import Fragments.BottomSheetFragment;
import Fragments.GeneralFragment;
import Fragments.PickerFragment;

import DataBase.DataBase;
import DataBase.DataBaseContract;
import Fragments.TimePickerFragment;
import NormalObjects.Message;
import NormalObjects.Network;
import NormalObjects.NetworkChange;
import NormalObjects.ObjectToSend;

//import NormalObjects.Server;
import NormalObjects.User;
import Retrofit.RetrofitApi;
import Retrofit.RetrofitClient;


import Try.TryMyResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//ui doesn't scale with accessibility
@SuppressWarnings("Convert2Lambda")
public class ConversationActivity extends AppCompatActivity implements ChatAdapter.MessageInfoListener, PickerFragment.onPickerClick, BottomSheetFragment.onSheetClicked, BackdropFragment.onBottomSheetAction, Serializable, ConversationGUI, Runnable, TimePickerFragment.onTimePicked {

    private static final String LOG_TAG = "AudioRecordingTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName;
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;


    private boolean b = false;

    public static final String MESSAGE_SEEN = "MESSAGE_SEEN";
    public static final String MESSAGE_SENT = "MESSAGE_SENT";
    public static final String MESSAGE_DELIVERED = "MESSAGE_DELIVERED";

    private ChatAdapter chatAdapter;
    private RecyclerView recyclerView;
    private String conversationID;


    private Uri imageUri;
    private ImageView imageView;
    private Bitmap imageBitmap;
    private String photoPath;

    private FusedLocationProviderClient client;
    private Geocoder geocoder;
    private LocationCallback locationCallback;
    private String longitude, latitude, gpsAddress;
    //currentUser is the sender always and should not be changed during the lifecycle of the activity
    private final String currentUser = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    private String messageToSend;
    private EditText messageSent;
    private String recipientUID;

    private final int GALLERY_REQUEST = 2;
    private final int WRITE_PERMISSION = 3;
    private final int CAMERA_REQUEST = 4;
    private final int LOCATION_REQUEST = 5;
    private final int REQUEST_SELECT_PHONE_NUMBER = 6;
    private final int CALL_PHONE = 7;
    private User user;
    private User recipient;
    private String recipientToken;
    //private boolean veteran;
    private ArrayList<Message> messages;
    private int messagesCounter = 0;
    //private String CHANNEL_ID = "MessagesChannel";

    private ImageButton sendActionBtn;
    private RetrofitApi api;
    private String PICKER_FRAGMENT_TAG = "Picker_fragment";
    private String BOTTOM_SHEET_TAG = "BottomSheet_fragment";

    private final int PICK_PDF_FILE = 10;

    private boolean camera;
    private MessageType finalMessageType;
    private int actionState = 0;


    private boolean editMode = false;
    private Message editMessage;
    //private Button sendMessageButton;


    private TextView quoteText;
    private boolean quoteOn = false;
    private int quotedMessagePosition = -1;
    //settings options
    private boolean sendBtnLP_Preference;
    private boolean actionBtnLP_Preference;
    private String defaultActionBtn_Preference;


    private String[] talkingTo;
    private int talkingToIndex = 0;
    private TextSwitcher textSwitcher;
    private TextSwitcher textSwitcherStatus;
    private TextSwitcher textSwitcherTyping;
    private ImageView statusView;
    private boolean darkMode;
    private boolean directCall;
    private String recipientName, recipientLastName;
    private CController controller;
    private String recipientPhoneNumber;


    //button state is the button "type" as is how it functions
    private int buttonState = 1;
    private final int RECORD_VOICE = 1;
    private final int SEND_MESSAGE = 0;

    private boolean startRecording = true;
    private boolean startPlaying = true;
    private boolean startPlaying1 = true;
    private TextView recordingTimeText;
    private boolean recordingON = false;
    private LinearLayout voiceLayout;
    private ImageButton playAudioRecordingBtn;
    private SeekBar voiceSeek;
    private boolean playingON = false;
    //private TextView playbackTime;
    private boolean recorded = false;
    private ImageView closeBtn;
    private Uri fileUri;

    private String title, link;
    private RelativeLayout linkedMessagePreview;
    private ImageView linkedImage;
    private TextView linkTitle, linkContent;

    private ImageSwitcher imageSwitcher;

    private ShapeableImageView talkingToImage;
    private boolean messageLongPress = false;
    private Message selectedMessage;
    private boolean goingBack = false;
    private String quotedMessageID;

    private boolean calledRecipient = false;
    private boolean calledMessages = false;

    private SQLiteDatabase db = null;
    private String NETWORK_ERROR = "network Error";
    private String FIREBASE_ERROR = "firebase_Error";
    private String PROGRAM_INFO = "info";
    private String NULL_ERROR = "something is null";
    private String DATABASE_ERROR = "database error";
    private String STATUS_INFO = "status";
    private String ERROR_CASE = "Error in switch case";


    private LinearLayout searchLayout;
    private EditText searchText;
    private Button searchBtn;
    private ImageButton scrollToNext;
    private ArrayList<Integer> indices;
    private int indicesIndex = 0;

    private boolean networkConnection = true;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.converastion_layout2);
        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        ConnectedToInternet();

        if (actionBar != null)
            actionBar.setDisplayShowTitleEnabled(false);
        //settings information
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ConversationActivity.this);
        sendBtnLP_Preference = preferences.getBoolean("SendButtonLP", true);
        actionBtnLP_Preference = preferences.getBoolean("ActionButtonLP", true);
        defaultActionBtn_Preference = preferences.getString("ActionButton", "no action");
        darkMode = preferences.getBoolean("darkView", false);
        directCall = preferences.getBoolean("directCall", false);
        RelativeLayout relativeLayout = findViewById(R.id.root_container);

        searchLayout = findViewById(R.id.searchLayout);
        searchText = findViewById(R.id.searchText);
        searchBtn = findViewById(R.id.searchBtn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchQuery = searchText.getText().toString();
                indices = chatAdapter.SearchMessage(searchQuery);

            }
        });
        scrollToNext = findViewById(R.id.scrollToNext);
        scrollToNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (indices != null) {
                    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);
                            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                            if (manager != null) {
                                View view = manager.findViewByPosition(indices.get(indicesIndex));
                                MarkMessage(view);
                            }
                            recyclerView.removeOnScrollListener(this);
                        }
                    });
                    recyclerView.scrollToPosition(indices.get(indicesIndex));

                    indicesIndex++;
                    if (indicesIndex >= indices.size())
                        indicesIndex = 0;
                }
            }
        });
        DataBaseSetUp();


        controller = CController.getController();
        controller.setConversationGUI(this);
        if (user == null) {
            controller.onDownloadUser(this, currentUser);
            System.out.println("downloading user");
        }

        voiceLayout = findViewById(R.id.voiceLayout);
        quoteText = findViewById(R.id.quoteText);

        imageView = findViewById(R.id.imagePreview);
        SharedPreferences sharedPreferences = getSharedPreferences("ConversationFirstTime", MODE_PRIVATE);

        closeBtn = findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playingON = false;
                fileName = null;
                if (player != null) {
                    player.stop();
                    stopPlaying();
                }
                recordingTimeText.setText(R.string.zero_time);
                ResetToText();
                buttonState = RECORD_VOICE;
                //sendMessageButton.setText(getResources().getString(R.string.record));
                if (!darkMode)
                    imageSwitcher.setImageResource(R.drawable.ic_baseline_mic_black);
                else
                    imageSwitcher.setImageResource(R.drawable.ic_baseline_mic_white);
            }
        });


        api = RetrofitClient.getRetrofitClient("https://fcm.googleapis.com/").create(RetrofitApi.class);
        geocoder = new Geocoder(this);
        messages = new ArrayList<>();
        //conversation id is sent to this activity regardless of from where this activity was lunched
        conversationID = getIntent().getStringExtra("conversationID");
        recipientPhoneNumber = getIntent().getStringExtra("recipientPhone");
        title = getIntent().getStringExtra("title");
        link = getIntent().getStringExtra("link");
        isRecipientTyping();

        //someone sent me a message and i clicked on a notification
        if (getIntent().getBooleanExtra("tapMessageNotification", false)) {
            recipientUID = getIntent().getStringExtra("senderUID");

        } else {
            //i clicked on a conversation in the conversations tab
            recipientUID = getIntent().getStringExtra("recipient");
        }


        String downloadAmount = sharedPreferences.getString("messagesLoad", "20");
        //controller.onDownloadMessages(ConversationActivity.this, conversationID, Integer.parseInt(downloadAmount));


        chatAdapter = new ChatAdapter();
        chatAdapter.setMessages(new ArrayList<>());
        chatAdapter.setCurrentUserUID(currentUser);
        chatAdapter.setListener(this);

        //sendMessageButton = findViewById(R.id.sendMessageBtn);
        sendActionBtn = findViewById(R.id.ActionBtn);
        recyclerView = findViewById(R.id.recycle_view);
        messageSent = findViewById(R.id.MessageToSend);
        //removes the set location button if started typing a message
        messageSent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String recipientConversationID = RecipientConversationID(conversationID);
                controller.onUpdateInteraction("users/" + recipient.getUserUID() + "/conversations/" + recipientConversationID + "/conversationInfo/typing/",true);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String recipientConversationID = RecipientConversationID(conversationID);
                if (s.toString().equals(""))
                    controller.onUpdateInteraction("users/" + recipient.getUserUID() + "/conversations/" + recipientConversationID + "/conversationInfo/typing/",false);
                if (s.toString().equals("")) {
                    if (buttonState != RECORD_VOICE)
                        if (!darkMode)
                            imageSwitcher.setImageResource(R.drawable.ic_baseline_mic_black);
                        else
                            imageSwitcher.setImageResource(R.drawable.ic_baseline_mic_white);
                    sendActionBtn.setVisibility(View.VISIBLE);
                    //  sendMessageButton.setText(getResources().getString(R.string.record));
                    buttonState = RECORD_VOICE;
                } else {
                    if (buttonState != SEND_MESSAGE)
                        if (!darkMode)
                            imageSwitcher.setImageResource(R.drawable.ic_baseline_send_24);
                        else
                            imageSwitcher.setImageResource(R.drawable.ic_baseline_send_white);
                    sendActionBtn.setVisibility(View.GONE);
                    //   sendMessageButton.setText(getResources().getString(R.string.send));
                    buttonState = SEND_MESSAGE;

                }
            }
        });

        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);

        imageSwitcher = findViewById(R.id.sendImageSwitch);
        imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(ConversationActivity.this);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setLayoutParams(new ImageSwitcher.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                return imageView;
            }
        });
        imageSwitcher.setInAnimation(in);
        imageSwitcher.setOutAnimation(out);
        if (link == null) {
            if (!darkMode)
                imageSwitcher.setImageResource(R.drawable.ic_baseline_mic_black);
            else
                imageSwitcher.setImageResource(R.drawable.ic_baseline_mic_white);
        } else {
            if (!darkMode)
                imageSwitcher.setImageResource(R.drawable.ic_baseline_send_24);
            else
                imageSwitcher.setImageResource(R.drawable.ic_baseline_send_white);
        }
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //keyboard doesn't hide recycleView
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setAdapter(chatAdapter);


        LoadMessagesFromDataBase();


        recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {

            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                //finds the top of the recycle view
                //if (linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                // Server.DownloadMessages2(ConversationActivity.this,conversationID,20);
                //}
            }
        });
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.START | ItemTouchHelper.END) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                //swiping quotes the message

                viewHolder.getAdapterPosition();
                TextView message = viewHolder.itemView.findViewById(R.id.message);
                String quote = "\"" + message.getText() + "\"";
                quoteText.setText(quote);
                quoteText.setVisibility(View.VISIBLE);
                quoteOn = true;
                quotedMessageID = chatAdapter.getMessageID(viewHolder.getAdapterPosition());
                quotedMessagePosition = viewHolder.getAdapterPosition();
                //brings back just the item that was swiped away
                if (recyclerView.getAdapter() != null)
                    recyclerView.getAdapter().notifyItemChanged(viewHolder.getAdapterPosition());
                //chatAdapter.notifyDataSetChanged();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        quoteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quoteText.setText("");
                quoteText.setVisibility(View.GONE);
                quoteOn = false;
                recyclerView.scrollToPosition(messages.size() - 1);
            }
        });

        if (messages.size() > 0)
            recyclerView.scrollToPosition(messages.size() - 1);
        recordingTimeText = findViewById(R.id.recordingTime);
        recordingTimeText.setText("0");

        imageSwitcher.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //press DOWN on the button
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    //SEND TEXT MESSAGE

                    if (buttonState == SEND_MESSAGE) {
                        messageToSend = messageSent.getText().toString();
                        if (editMode) {//distinction between editing a message and sending a new message
                            SendEditedMessage();
                        } else {
                            if (!messageToSend.equals("") || camera) {//cant send empty message
                                SendMessageSound();
                                PrepareSendMessage();
                            } else if (recorded) {
                                //send the recorded file
                                SendRecording();
                                ResetToText();

                            }
                        }
                    } else if (buttonState == RECORD_VOICE) {//start recording
                        RecordingSoundStart();
                        LongPressToRecordVoice();
                    }


                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (buttonState == RECORD_VOICE) {
                        //DECIDE IF TO SEND VOICE MESSAGE
                        if (recorded) {
                            startRecording = !startRecording;
                            RecordingSoundStopped();
                            ShowVoiceControl();
                        }
                    }
                }
                return true;
            }
        });


        /*sendMessageButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //press DOWN on the button
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    //SEND TEXT MESSAGE

                    if (buttonState == SEND_MESSAGE) {
                        messageToSend = messageSent.getText().toString();
                        if (editMode) {//distinction between editing a message and sending a new message
                            SendEditedMessage();
                        } else {
                            if (!messageToSend.equals("")) {//cant send empty message
                                PrepareSendMessage();
                            } else if (recorded) {
                                //send the recorded file
                                SendRecording();
                                ResetToText();

                            }
                        }
                    } else if (buttonState == RECORD_VOICE) {//start recording
                        RecordingSoundStart();
                        LongPressToRecordVoice();
                    }


                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (buttonState == RECORD_VOICE) {
                        //DECIDE IF TO SEND VOICE MESSAGE
                        if (recorded) {
                            RecordingSoundStopped();
                            ShowVoiceControl();
                        }
                    }
                }
                return true;
            }
        });*/

        playAudioRecordingBtn = findViewById(R.id.play_pause_btn);
        /*playAudioRecordingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!playingON)
                    PlayOrNot();
                else {
                    if (player.isPlaying()) {
                        player.pause();
                        playAudioRecordingBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                    } else {
                        player.start();
                        playAudioRecordingBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
                    }
                }
            }
        });*/
        playAudioRecordingBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (player == null) {
                    PlayOrNot();
                } else if (player.isPlaying()) {
                    player.pause();
                    if (!darkMode)
                        playAudioRecordingBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                    else
                        playAudioRecordingBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_white);
                } else {
                    player.start();
                    startPlaying1 = !startPlaying1;
                    Thread playBackThread = new Thread(ConversationActivity.this);
                    playBackThread.setName("playBackThread");
                    playBackThread.start();
                    /*if(!startPlaying1)
                    {
                        player.start();
                        startPlaying1 = !startPlaying1;
                        Thread playBackThread = new Thread(ConversationActivity.this);
                        playBackThread.setName("playBackThread");
                        playBackThread.start();
                    }
                    else
                    {
                        startPlaying1 = !startPlaying1;
                        player.start();
                    }*/
                    if (!darkMode)
                        playAudioRecordingBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
                    else
                        playAudioRecordingBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_white);
                }
            }
        });
        voiceSeek = findViewById(R.id.voiceSeek);
        voiceSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (player != null) {
                    player.seekTo(seekBar.getProgress());
                    if (seekBar.getProgress() == 0) {
                        String zeroTime = 0 + "";
                        recordingTimeText.setText(zeroTime);
                    } else {
                        String s;
                        int p = seekBar.getProgress() / 1000;
                        if (p < 10)
                            s = "00:0" + p;
                        else
                            s = "00:" + p;
                        recordingTimeText.setText(s);
                    }
                } else
                    Toast.makeText(ConversationActivity.this, "start the player, pause it and move the time indicator", Toast.LENGTH_SHORT).show();
            }
        });
        //playbackTime = findViewById(R.id.playBackTime);
           /* sendMessageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    messageToSend = messageSent.getText().toString();
                    if (editMode) {
                        HashMap<String, Object> editMessageMap = new HashMap<>();
                        editMessageMap.put("message", messageToSend);
                        controller.onUpdateData("users/" + currentUser + "/conversations/" + editMessage.getConversationID() + "/" + editMessage.getMessageID() + "/", editMessageMap);
                        controller.onUpdateData("users/" + recipient + "/conversations/" + editMessage.getConversationID() + "/" + editMessage.getMessageID() + "/", editMessageMap);
                        messageSent.setText("");
                        editMode = false;
                    } else {
                        if (!messageToSend.equals(""))
                            if (imageView.getVisibility() == View.VISIBLE)
                                sendMessage(MessageType.photoMessage.ordinal(), recipient);
                            else if (imageView.getVisibility() == View.GONE)
                                sendMessage(MessageType.textMessage.ordinal(), recipient);
                    }
                }
            });*/

        /*sendMessageButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (sendBtnLP_Preference) {
                    //opens time picker to send message at a set time
                    PickerFragment pickerFragment = PickerFragment.newInstance();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.add(R.id.root_container, pickerFragment, PICKER_FRAGMENT_TAG);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
                return true;
            }
        });*/

        switch (defaultActionBtn_Preference) {
            case "Location":
                if (!darkMode)
                    sendActionBtn.setImageResource(R.drawable.ic_baseline_location_on_24);
                else
                    sendActionBtn.setImageResource(R.drawable.location_white);
                actionState = ButtonType.location.ordinal();
                break;
            case "Camera":
                if (!darkMode)
                    sendActionBtn.setImageResource(R.drawable.ic_baseline_camera_alt_24);
                else
                    sendActionBtn.setImageResource(R.drawable.ic_baseline_camera_alt_white);
                actionState = ButtonType.camera.ordinal();
                break;
            case "Gallery":
                if (!darkMode)
                    sendActionBtn.setImageResource(R.drawable.ic_baseline_photo_24);
                else
                    sendActionBtn.setImageResource(R.drawable.ic_baseline_camera_alt_white);
                actionState = ButtonType.gallery.ordinal();
                break;
            default:
                if (!darkMode)
                    sendActionBtn.setImageResource(R.drawable.ic_baseline_location_on_24);
                else
                    sendActionBtn.setImageResource(R.drawable.location_white);
                actionState = ButtonType.location.ordinal();
                System.out.println("default action preference");
        }

        sendActionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ButtonType type = ButtonType.values()[actionState];
                switch (type) {
                    case location: {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ConversationActivity.this);
                        builder.setCancelable(true);
                        builder.setTitle("Send CurrentLocation").setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).setPositiveButton("Send", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //sends the location
                                if (AskPermission(MessageType.gpsMessage))
                                    findLocation();
                            }
                        }).setMessage("Are you sure you want to send your current location");
                        builder.show();
                        break;
                    }
                    case attachFile: {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ConversationActivity.this);
                        builder.setCancelable(true);
                        builder.setTitle("Send file?").setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).setPositiveButton("Send", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //opens file picker to send a file
                            }
                        }).setMessage("Are you sure you want to send a file");
                        builder.show();
                        break;
                    }
                    case camera: {
                        AskPermission(MessageType.photoMessage);
                        Toast.makeText(ConversationActivity.this, "opening camera", Toast.LENGTH_SHORT).show();
                        requestCamera();
                        break;
                    }
                    case gallery: {
                        Toast.makeText(ConversationActivity.this, "gallery", Toast.LENGTH_SHORT).show();
                        openGallery();
                        break;
                    }
                    case delay: {
                        PickerFragment pickerFragment = PickerFragment.newInstance();
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.add(R.id.contentContainer, pickerFragment, PICKER_FRAGMENT_TAG);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        break;
                    }
                    default:
                        Log.e(ERROR_CASE, "action button error: " + new Throwable().getStackTrace()[0].getLineNumber());
                }

            }
        });


        sendActionBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (actionBtnLP_Preference) {
                    //opens bottom sheet do display extra options
                    BottomSheetFragment bottomSheetFragment = BottomSheetFragment.newInstance();
                    bottomSheetFragment.show(getSupportFragmentManager(), BOTTOM_SHEET_TAG);
                }
                return true;
            }
        });
        talkingToImage = findViewById(R.id.toolbarProfileImage);
        talkingToImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openRecipientIntent = new Intent(ConversationActivity.this, ProfileActivity2.class);
                openRecipientIntent.putExtra("recipient", recipient);
                openRecipientIntent.putExtra("currentUser", user);
                startActivity(openRecipientIntent);
            }
        });
        ImageButton backButton = findViewById(R.id.goBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goingBack = true;
                controller.setConversationGUI(null);
                Intent intent = new Intent(ConversationActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


        if (darkMode) {
            relativeLayout.setBackgroundColor(getResources().getColor(android.R.color.black, getTheme()));
            messageSent.setBackgroundColor(getResources().getColor(android.R.color.black, getTheme()));
            messageSent.setHintTextColor(getResources().getColor(android.R.color.white, getTheme()));
            messageSent.setTextColor(getResources().getColor(android.R.color.white, getTheme()));
            sendActionBtn.setImageResource(R.drawable.location_white);
        } else {
            relativeLayout.setBackgroundColor(getResources().getColor(android.R.color.white, getTheme()));
            messageSent.setBackgroundColor(getResources().getColor(android.R.color.white, getTheme()));
            messageSent.setHintTextColor(getResources().getColor(android.R.color.black, getTheme()));
            messageSent.setTextColor(getResources().getColor(android.R.color.black, getTheme()));
            sendActionBtn.setImageResource(R.drawable.ic_baseline_location_on_24);
        }

        SharedPreferences conversationPreferences = getSharedPreferences("Conversation", MODE_PRIVATE);
        SharedPreferences.Editor editor = conversationPreferences.edit();
        editor.putString("liveConversation", conversationID);
        editor.apply();

        textSwitcher = findViewById(R.id.toolbarTextSwitch);
        textSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView toTextSwitcher = new TextView(ConversationActivity.this);
                toTextSwitcher.setGravity(Gravity.CENTER | Gravity.START);
                toTextSwitcher.setTextSize(15);
                toTextSwitcher.setTextColor(getResources().getColor(android.R.color.white, getTheme()));
                return toTextSwitcher;
            }
        });

        statusView = findViewById(R.id.statusView);
        textSwitcherStatus = findViewById(R.id.toolbarStatusTextSwitch);
        textSwitcherStatus.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView status = new TextView(ConversationActivity.this);
                status.setGravity(Gravity.CENTER | Gravity.START);
                status.setTextSize(10);
                status.setTextColor(getResources().getColor(android.R.color.white, getTheme()));
                return status;
            }
        });

        textSwitcherTyping = findViewById(R.id.typingIndicator);
        textSwitcherTyping.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView toTextSwitcher = new TextView(ConversationActivity.this);
                toTextSwitcher.setGravity(Gravity.CENTER | Gravity.START);
                toTextSwitcher.setTextSize(10);
                toTextSwitcher.setTextColor(getResources().getColor(android.R.color.white, getTheme()));
                return toTextSwitcher;
            }
        });

        controller.onUpdateData("users/" + currentUser + "/status", MainActivity.ONLINE_S);
        //Server.updateServer("users/" + currentUser + "/status", MainActivity.ONLINE_S);

        // controller.onDownloadUser(ConversationActivity.this, recipientUID);

        textSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (talkingToIndex > 2)
                    talkingToIndex = 0;
                textSwitcher.setText(talkingTo[talkingToIndex]);
                talkingToIndex++;

            }
        });


        textSwitcher.setInAnimation(in);
        textSwitcher.setOutAnimation(out);
        textSwitcherStatus.setInAnimation(in);
        textSwitcherStatus.setOutAnimation(out);
        textSwitcherTyping.setInAnimation(in);
        textSwitcherTyping.setOutAnimation(out);
        //voice();


        linkedMessagePreview = findViewById(R.id.linkMessage);
        linkContent = findViewById(R.id.linkContent);
        linkTitle = findViewById(R.id.linkTitle);
        linkedImage = findViewById(R.id.linkImage);
        if (link != null) {

            messageSent.setText(link);
            Thread linkPreviewThread = new Thread() {

                @Override
                public void run() {
                    super.run();

                    try {
                        Document doc = Jsoup.connect(link).userAgent("Mozilla").get();
                        String title = doc.title();
                        Elements webImage = doc.select("meta[property=og:image]");
                        String imageLink = webImage.attr("content");
                        Elements webDescription = doc.select("meta[property=og:description]");
                        String description = webDescription.attr("content");
                        URL url = new URL(imageLink);
                        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
                        httpsURLConnection.connect();
                        int responseCode = httpsURLConnection.getResponseCode();
                        if (responseCode == 200) {
                            InputStream inputStream = httpsURLConnection.getInputStream();
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    linkedMessagePreview.setVisibility(View.VISIBLE);
                                    linkedImage.setScaleType(ImageView.ScaleType.FIT_XY);
                                    linkedImage.setImageBitmap(bitmap);
                                    linkContent.setText(description);
                                    linkTitle.setText(title);
                                }
                            });

                            inputStream.close();
                            httpsURLConnection.disconnect();
                        }
                        httpsURLConnection.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            };
            linkPreviewThread.setName("link preview Thread");
            linkPreviewThread.start();
        }

        registerForContextMenu(recyclerView);
    }


    private void ShowMessageForUserConfirmation() {
        messageSent.setText(link);
        buttonState = SEND_MESSAGE;
        if (!darkMode)
            imageSwitcher.setImageResource(R.drawable.ic_baseline_send_24);
        else
            imageSwitcher.setImageResource(R.drawable.ic_baseline_send_white);
        // link = null;
    }


    private void SendEditedMessage() {
        HashMap<String, Object> editMessageMap = new HashMap<>();
        editMessageMap.put("message", messageToSend);
        controller.onUpdateData("users/" + currentUser + "/conversations/" + editMessage.getConversationID() + "/conversationInfo/conversationMessages/" + editMessage.getMessageID() + "/", editMessageMap);
        String recipientConversationID = RecipientConversationID(editMessage.getConversationID());
        controller.onUpdateData("users/" + recipientUID + "/conversations/" + recipientConversationID + "/conversationInfo/conversationMessages/" + editMessage.getMessageID() + "/", editMessageMap);
        messageSent.setText("");
        editMode = false;
    }

    private void RecordingSoundStart() {
        MediaPlayer startRecordingSound = MediaPlayer.create(getApplicationContext(), R.raw.recording_sound_start);
        startRecordingSound.start();
        startRecordingSound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                startRecordingSound.release();
            }
        });
    }

    private void RecordingSoundStopped() {
        MediaPlayer stopRecordingSound = MediaPlayer.create(getApplicationContext(), R.raw.recording_sound_stopped);
        stopRecordingSound.start();
        stopRecordingSound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopRecordingSound.release();
            }
        });
    }

    private void SendMessageSound() {
        MediaPlayer sendMessageSound = MediaPlayer.create(getApplicationContext(), R.raw.send_message_sound);
        sendMessageSound.start();
        sendMessageSound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                sendMessageSound.release();
            }
        });
    }

    private void PrepareSendMessage() {

        if (imageView.getVisibility() == View.VISIBLE)
            sendMessage(MessageType.photoMessage.ordinal(), recipientUID);
        else if (imageView.getVisibility() == View.GONE)
            sendMessage(MessageType.textMessage.ordinal(), recipientUID);
        linkedMessagePreview.setVisibility(View.GONE);
        link = null;
    }

    private void ResetToText() {
        messageSent.setVisibility(View.VISIBLE);
        recordingTimeText.setVisibility(View.GONE);
        voiceLayout.setVisibility(View.GONE);
        // sendMessageButton.setText(getResources().getString(R.string.record));
        if (!darkMode)
            imageSwitcher.setImageResource(R.drawable.ic_baseline_mic_black);
        else
            imageSwitcher.setImageResource(R.drawable.ic_baseline_mic_white);
        recorded = false;
        buttonState = RECORD_VOICE;
        startRecording = true;
        stopPlaying();
    }

    private void ShowVoiceControl() {
        voiceSeek.setProgress(0);
        voiceLayout.setVisibility(View.VISIBLE);
        recordingTimeText.setText(getResources().getString(R.string.zero_time));
        RecordOrNot();
        // sendMessageButton.setText(getResources().getString(R.string.send));
        if (!darkMode)
            imageSwitcher.setImageResource(R.drawable.ic_baseline_send_24);
        else
            imageSwitcher.setImageResource(R.drawable.ic_baseline_send_white);
        buttonState = SEND_MESSAGE;
        recorded = true;
    }

    private void LongPressToRecordVoice() {
        fileName = Objects.requireNonNull(getExternalCacheDir()).getAbsolutePath();
        fileName += "/audioRecordingTest.3gp";
        if (AskPermission(MessageType.VoiceMessage)) {
            RecordOrNot();
        }
    }

    private void SendRecording() {
        //File file = new File(fileName);
        //Uri uri = Uri.fromFile(file);
        CreateFileUri(fileName);
        sendMessage(MessageType.VoiceMessage.ordinal(), recipientUID);

        //Server.uploadFile("users/" + currentUser + "/conversations/" + conversationID + "/conversationInfo/conversationMessages/" + System.currentTimeMillis() +"/recording", uri.toString(), ConversationActivity.this);
    }

    private void CreateFileUri(String filePath) {
        File file = new File(filePath);
        fileUri = Uri.fromFile(file);
    }

    private void RecordOrNot() {

        onRecord(startRecording);
        if (startRecording) {
            // sendMessageButton.setText(getResources().getString(R.string.recording));
            if (!darkMode)
                imageSwitcher.setImageResource(R.drawable.ic_baseline_settings_voice_black);
            else
                imageSwitcher.setImageResource(R.drawable.ic_baseline_settings_voice_white);
        } else {
            // sendMessageButton.setText(getResources().getString(R.string.record));
            if (!darkMode)
                imageSwitcher.setImageResource(R.drawable.ic_baseline_mic_black);
            else
                imageSwitcher.setImageResource(R.drawable.ic_baseline_mic_white);
        }
        //startRecording = !startRecording;
    }

    private void PlayOrNot() {
        onPlay(startPlaying1);
        if (startPlaying1) {
            if (!darkMode)
                playAudioRecordingBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
            else
                playAudioRecordingBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_white);
        } else {
            if (!darkMode)
                playAudioRecordingBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
            else
                playAudioRecordingBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_white);
        }
        /*onPlay(startPlaying);
        if (startPlaying) {
            playAudioRecordingBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
        } else {
            playAudioRecordingBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
        }
        startPlaying = !startPlaying;*/
    }

    private void onRecord(boolean start) {
        if (start)
            startRecording();
        else
            stopRecording();
    }

    private void stopRecording() {
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingON = false;
            messageSent.setVisibility(View.GONE);
            recordingTimeText.setVisibility(View.VISIBLE);
        }
    }

    private void onPlay(boolean start) {
        if (start)
            startPlaying();
        else
            stopPlaying();
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {


            player.setDataSource(fileName);
            player.prepare();
            player.start();
            playingON = true;
            voiceSeek.setMax(player.getDuration());
            recordingTimeText.setVisibility(View.VISIBLE);
            messageSent.setVisibility(View.GONE);
            Thread playBackThread = new Thread(this);
            playBackThread.setName("playBackThread");
            playBackThread.start();


        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        if (player != null)
            player.release();
        player = null;
        startPlaying1 = true;
        stopRecording();
    }

    private void startRecording() {

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare failed");
        }
        recorder.start();
        recordingON = true;
        recorded = true;
        messageSent.setVisibility(View.GONE);
        recordingTimeText.setVisibility(View.VISIBLE);
        Thread countRecordingTimeThread = new Thread(this);
        countRecordingTimeThread.setName("recording thread");
        countRecordingTimeThread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    public void onMessageClick(Message message, View view, int viewType) {
        //highlights the message and changes the toolbar to show options
        if (messageLongPress) {
            messageLongPress = false;
            invalidateOptionsMenu();
        } else if (message.getQuoteMessage() != null) {
            //this is a quoted message
            String quotedMessageID = message.getQuotedMessageID();
            if (quotedMessageID != null) {
                int index = chatAdapter.findQuotedMessageLocation(null, 0, chatAdapter.getItemCount() - 1, Long.parseLong(quotedMessageID));
                if (index != -1) {
                    recyclerView.smoothScrollToPosition(index);
                    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                            super.onScrollStateChanged(recyclerView, newState);
                            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                                RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                                if (manager != null) {
                                    View view1 = manager.findViewByPosition(index);
                                    MarkMessage(view1);
                                   /* if (view1 != null) {
                                        view1.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.border, getTheme()));
                                        Handler handler = new Handler(getMainLooper());
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                view1.setBackground(null);
                                            }
                                        }, 2500);
                                    }*/
                                }
                                recyclerView.removeOnScrollListener(this);
                            }
                        }
                    });
                    //recyclerView.scrollToPosition(index);
                } else
                    Toast.makeText(this, "couldn't fine this quoted message", Toast.LENGTH_SHORT).show();
            }
        } else if (Patterns.WEB_URL.matcher(message.getMessage()).matches()) {
            Intent openWebSite = new Intent(Intent.ACTION_VIEW);
            openWebSite.setData(Uri.parse(message.getMessage()));
            startActivity(openWebSite);
        }
    }

    private void MarkMessage(View view)
    {
        if (view != null) {
            view.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.border, getTheme()));
            Handler handler = new Handler(getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    view.setBackground(null);
                }
            }, 2500);
        }
    }

    @Override
    public void onMessageLongClick(Message message, View view, int viewType) {
        //opens fragment that shows information about the specific message
        messageLongPress = !messageLongPress;
        invalidateOptionsMenu();
        selectedMessage = message;


    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (messageLongPress) {
            menu.findItem(R.id.share).setVisible(true);
            menu.findItem(R.id.messageInfo).setVisible(true);
        } else {
            menu.findItem(R.id.share).setVisible(false);
            menu.findItem(R.id.messageInfo).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onPreviewMessageClick(Message message) {
        if (message.getMessageType() == MessageType.gpsMessage.ordinal()) {
            String geoString = String.format(Locale.ENGLISH, "geo:%S,%S", message.getLatitude(), message.getLongitude());
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoString));
            startActivity(intent);
        }
    }

    @Override
    public void onEditMessageClick(Message message) {
        if (!editMode) {
            editMode = true;
            editMessage = message;
            messageSent.setText(editMessage.getMessage());
            // sendMessageButton.setText(getResources().getString(R.string.edit));
            imageSwitcher.setImageResource(R.drawable.ic_baseline_edit_24);
        } else {
            editMode = false;
            messageSent.setText("");
            //  sendMessageButton.setText(getResources().getString(R.string.send));
            if (!darkMode)
                imageSwitcher.setImageResource(R.drawable.ic_baseline_send_24);
            else
                imageSwitcher.setImageResource(R.drawable.ic_baseline_send_white);
        }
    }


    //saves image to local storage
    @Override
    public void onImageDownloaded(Bitmap bitmap) {

        String fileName = "image_" + System.currentTimeMillis() + ".jpg";
        OutputStream out = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = ConversationActivity.this.getApplicationContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            try {
                if (imageUri != null) {
                    out = resolver.openOutputStream(imageUri);

                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            File imageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File imageFile = new File(imageDirectory, fileName);
            try {
                out = new FileOutputStream(imageFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out))
                System.out.println("Bitmap successfully written");
            else
                System.out.println("Bitmap save have failed");
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public void onDeleteMessageClick(Message message) {
        //Server.deleteMessage(true);
        controller.onRemoveData("users/" + currentUser + "/conversations/" + message.getConversationID() + "/conversationInfo/conversationMessages" + message.getMessageID());
        String recipientConversationID = RecipientConversationID(conversationID);
        controller.onRemoveData("users/" + recipientUID + "/conversations/" + recipientConversationID + "/conversationInfo/conversationMessages" + message.getMessageID());
        //Server.removeServer("users/" + currentUser + "/conversations/" + message.getConversationID() + "/" + message.getMessageID());
        //Server.removeServer("users/" + recipient + "/conversations/" + message.getConversationID() + "/" + message.getMessageID());
        Toast.makeText(this, "message was deleted", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences conversationPreferences = getSharedPreferences("Conversation", MODE_PRIVATE);
        SharedPreferences.Editor editor = conversationPreferences.edit();
        editor.putString("liveConversation", conversationID);
        editor.apply();

        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("disableNotifications").putExtra("ConversationID", conversationID));

        controller.setConversationGUI(this);
        controller.onUpdateData("users/" + currentUser + "/status", MainActivity.ONLINE_S);
        goingBack = false;
    }


    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences conversationPreferences = getSharedPreferences("Conversation", MODE_PRIVATE);
        SharedPreferences.Editor editor = conversationPreferences.edit();
        editor.putString("liveConversation", "no conversation");
        editor.apply();

        SharedPreferences sharedPreferences = getSharedPreferences("share", MODE_PRIVATE);
        SharedPreferences.Editor editor1 = sharedPreferences.edit();
        editor1.remove("title");
        editor1.remove("link");
        editor1.apply();

        if (!goingBack)
            controller.onUpdateData("users/" + currentUser + "/status", MainActivity.OFFLINE_S);
        controller.setConversationGUI(null);
        //Server.updateServer("users/" + currentUser + "/status", MainActivity.OFFLINE_S);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String recipientConversationID = RecipientConversationID(conversationID);
        controller.onUpdateInteraction("users/" + recipient.getUserUID() + "/conversations/" + recipientConversationID + "/conversationInfo/typing/",false);
        controller.onRemoveChildEvent();
        // Server.removeMessagesChildEvent();
        SharedPreferences conversationPreferences = getSharedPreferences("Conversation", MODE_PRIVATE);
        SharedPreferences.Editor editor = conversationPreferences.edit();
        editor.putString("liveConversation", "noConversation");
        editor.apply();
        controller.removeInterface(1);
        controller.setConversationGUI(null);
    }

    private boolean AskPermission(MessageType messageType) {
        switch (messageType) {
            case gpsMessage: {
                int hasLocationPermission = ConversationActivity.this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
                    return false;
                } else return true;
            }
            case photoMessage: {
                int hasWritePermission = ConversationActivity.this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION);
                    return false;
                } else return true;
            }
            case callPhone: {
                int hasCallPermission = ConversationActivity.this.checkSelfPermission(Manifest.permission.CALL_PHONE);
                if (hasCallPermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, CALL_PHONE);
                    return false;
                } else return true;
            }
            case VoiceMessage: {
                int hasRecordingPermission = ConversationActivity.this.checkSelfPermission(Manifest.permission.RECORD_AUDIO);
                if (hasRecordingPermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
                    return false;
                } else return true;
            }
        }
        return false;
        /*int hasLocationPermission = ConversationActivity.this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
            return false;
        } else return true;*/
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            switch (requestCode) {
                case LOCATION_REQUEST: {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                        findLocation();
                    else
                        Toast.makeText(this, "location permission is required for this feature to work", Toast.LENGTH_SHORT).show();
                    break;
                }
                case WRITE_PERMISSION: {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                        takePicture();
                    else
                        Toast.makeText(ConversationActivity.this, "permission is required to use the camera", Toast.LENGTH_SHORT).show();
                    break;
                }
                case CALL_PHONE: {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                        CallPhone(true);
                    else
                        Toast.makeText(this, "permission is required to make phone calls", Toast.LENGTH_SHORT).show();
                    break;
                }
                case REQUEST_RECORD_AUDIO_PERMISSION:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                        RecordOrNot();
                    else
                        Toast.makeText(this, "permission required to record audi", Toast.LENGTH_SHORT).show();
                    //permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    break;
            }
           /* if (requestCode == LOCATION_REQUEST && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                findLocation();
            else
                Toast.makeText(this, "location permission is required for this feature to work", Toast.LENGTH_SHORT).show();


            if (requestCode == WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePicture();
            } else
                Toast.makeText(ConversationActivity.this, "permission is required to use the camera", Toast.LENGTH_SHORT).show();

            if(requestCode == CALL_PHONE && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                CallPhone(true);
            else
                Toast.makeText(this, "permission is required to make phone calls", Toast.LENGTH_SHORT).show();*/
        }
    }


    @SuppressLint("MissingPermission")
    private void findLocation() {
        client = LocationServices.getFusedLocationProviderClient(ConversationActivity.this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location lastLocation = locationResult.getLastLocation();
                longitude = lastLocation.getLongitude() + "";
                latitude = lastLocation.getLatitude() + "";
                try {
                    List<Address> addresses = geocoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1);
                    Address address = addresses.get(0);
                    gpsAddress = address.getAddressLine(0);
                    client.removeLocationUpdates(locationCallback);
                    sendMessage(MessageType.gpsMessage.ordinal(), recipientUID);
                    //messageIndicator(sendMessage(MessageType.gpsMessage.ordinal()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        client.requestLocationUpdates(locationRequest, locationCallback, Objects.requireNonNull(Looper.myLooper()));

    }

    /*
    the send message function updates the server with the message sent and uses Retrofit to access the fcm server to send messages
     */
    private void sendMessage(int messageType, String recipient) {

        if (networkConnection) {
            //this is part 1 of the function - here the function updates the database with the message being sent

            TimeZone timeZone = TimeZone.getTimeZone("GMT-4");
            Calendar calendar = Calendar.getInstance(timeZone);
            String time = calendar.getTimeInMillis() + "";
            String recipientConversationID = RecipientConversationID(conversationID);
            //creating a message object and filling the correct information about the message
            Message message = new Message();
            message.setSender(currentUser);
            message.setSenderName(user.getName());
            HashMap<String, Object> messageMap = new HashMap<>();
            //since a message can be of several types, the correct data must be set for each type of message - this is where the switch case loop comes to play
            MessageType type = MessageType.values()[messageType];
            switch (type) {
                case textMessage: {
                    message.setMessage(messageToSend);
                    if (Patterns.WEB_URL.matcher(messageToSend).matches())
                        message.setMessageType(MessageType.webMessage.ordinal());
                    break;
                }
                case gpsMessage: {
                    message.setLatitude(latitude);
                    message.setLongitude(longitude);
                    message.setLocationAddress(gpsAddress);
                    message.setMessage("my location: " + gpsAddress);
                    break;
                }
                case photoMessage: {
                    if (camera)//photo from camera
                    {
                        controller.onSecondPath("users/" + recipient + "/conversations/" + recipientConversationID + "/conversationInfo/conversationMessages/" + time + "/");
                        controller.onUploadImage("users/" + currentUser + "/conversations/" + conversationID + "/conversationInfo/conversationMessages/" + time + "/", photoPath, ConversationActivity.this);
                    } else//photo from gallery
                    {
                        controller.onSecondPath("users/" + recipient + "/conversations/" + recipientConversationID + "/conversationInfo/conversationMessages/" + time + "/");
                        controller.onUploadImageBitmap("users/" + currentUser + "/conversations/" + conversationID + "/conversationInfo/conversationMessages/" + time + "/", imageBitmap, ConversationActivity.this);
                    }
                    message.setMessage(messageToSend);
                    break;
                }
                case VoiceMessage: {
                    message.setMessage("");
                    controller.onSecondPath("users/" + recipient + "/conversations/" + recipientConversationID + "/conversationInfo/conversationMessages/" + time);
                    controller.onUploadFile("users/" + currentUser + "/conversations/" + conversationID + "/conversationInfo/conversationMessages/" + time, fileUri.toString(), ConversationActivity.this);
                    //Server.setSecondPath("users/" + recipient + "/conversations/" + recipientConversationID + "/conversationInfo/conversationMessages/" + time);
                    //Server.uploadFile("users/" + currentUser + "/conversations/" + conversationID + "/conversationInfo/conversationMessages/" + time, fileUri.toString(), ConversationActivity.this);

                    break;
                }
                case fileMessage:
                    break;
                default:
                    Toast.makeText(this, "Error while sending message", Toast.LENGTH_SHORT).show();
                    return;
            }
            if (message.getMessageType() != MessageType.webMessage.ordinal())
                message.setMessageType(messageType);
            message.setRecipient(recipient);
            message.setConversationID(conversationID);

            if (quoteOn) {
                message.setQuoteMessage(quoteText.getText().toString());
                message.setQuotedMessagePosition(quotedMessagePosition);
                message.setQuotedMessageID(quotedMessageID);
                quotedMessageID = null;
                quotedMessagePosition = -1;
                quoteText.setText("");
                quoteText.setVisibility(View.GONE);
                quoteOn = false;
            }

            String currentTime = System.currentTimeMillis() + "";
            message.setMessageTime(currentTime);
            message.setMessageID(time);
            messageMap.put(time, message);//setting a unique id for each message sent using the system time


            // controller.onUpdateData("users/" + currentUser + "/conversations/" + conversationID + "/conversationInfo/conversationMessages", messageMap);
            //controller.onUpdateData("users/" + recipient + "/conversations/" + conversationID + "/conversationInfo/conversationMessages", messageMap);


            HashMap<String, Object> conversationInfo = new HashMap<>();
            conversationInfo.put("lastMessage", message.getMessage());
            conversationInfo.put("lastMessageTime", currentTime);
            conversationInfo.put("lastMessageID", time);
            conversationInfo.put("recipientID", recipient);
            conversationInfo.put("conversationID", conversationID);
            conversationInfo.put("lastMessageType", messageType);
            conversationInfo.put("recipientName", recipientName);
            // conversationInfo.put("conversationMessages",messageMap);

            controller.onUpdateData("users/" + currentUser + "/conversations/" + conversationID + "/conversationInfo", conversationInfo);
            conversationInfo.put("recipientName", user.getName());
            controller.onUpdateData("users/" + recipient + "/conversations/" + recipientConversationID + "/conversationInfo", conversationInfo);

            controller.onUpdateData("users/" + currentUser + "/conversations/" + conversationID + "/conversationInfo/conversationMessages", messageMap);
            controller.onUpdateData("users/" + recipient + "/conversations/" + recipientConversationID + "/conversationInfo/conversationMessages", messageMap);


            messageSent.setText("");//clears the input field
            imageView.setVisibility(View.GONE);
            //this is part 2 of the function - here the function uses the fcm server api with retrofit to send a message to the recipient

            //here the function retrieves the token associated with the recipient of the message, once the retrieval is complete the message is sent
        /*DatabaseReference tokenReference = FirebaseDatabase.getInstance().getReference("Tokens");
        Query tokensQuery = tokenReference.orderByKey().equalTo(recipient);//here the tokens that were retrieved are ordered by the key - which is equal to the recipients UID

        tokensQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String tokenString = dataSnapshot.getValue(String.class);
                    message.setConversationID(recipientConversationID);
                    ObjectToSend toSend = new ObjectToSend(message, tokenString);
                    message.setTo(tokenString);
                    api.sendMessage(toSend).enqueue(new Callback<TryMyResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<TryMyResponse> call, @NonNull Response<TryMyResponse> response) {
                            Log.i(PROGRAM_INFO, "response code: " + response.code());
                            Log.i(PROGRAM_INFO, "response message: " + response.message());
                            if (response.code() == 200) {
                                assert response.body() != null;
                                if (response.body().success != 1) {
                                    Log.e(NETWORK_ERROR, "couldn't send the message");
                                    Toast.makeText(ConversationActivity.this, "An error occurred while sending the message, try again later", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<TryMyResponse> call, @NonNull Throwable t) {
                            Log.e(NETWORK_ERROR, "retrofit failed!!!");
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(FIREBASE_ERROR, "cancelled firebase - didn't retrieve token");
            }
        });*/

            message.setConversationID(recipientConversationID);
            ObjectToSend toSend = new ObjectToSend(message, recipientToken);
            message.setTo(recipientToken);
            api.sendMessage(toSend).enqueue(new Callback<TryMyResponse>() {
                @Override
                public void onResponse(@NonNull Call<TryMyResponse> call, @NonNull Response<TryMyResponse> response) {
                    Log.i(PROGRAM_INFO, "response code: " + response.code());
                    Log.i(PROGRAM_INFO, "response message: " + response.message());
                    if (response.code() == 200) {
                        assert response.body() != null;
                        if (response.body().success != 1) {
                            Log.e(NETWORK_ERROR, "couldn't send the message");
                            Toast.makeText(ConversationActivity.this, "An error occurred while sending the message, try again later", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<TryMyResponse> call, @NonNull Throwable t) {
                    Log.e(NETWORK_ERROR, "retrofit failed!!!");
                }
            });
        }else
            Toast.makeText(this, "no connection is available, connect to the internet in order to send messages", Toast.LENGTH_SHORT).show();
    }

    private void GetRecipientToken(String recipient) {
        DatabaseReference tokenReference = FirebaseDatabase.getInstance().getReference("Tokens");
        Query tokensQuery = tokenReference.orderByKey().equalTo(recipient);//here the tokens that were retrieved are ordered by the key - which is equal to the recipients UID
        tokensQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    recipientToken = dataSnapshot.getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(FIREBASE_ERROR, "cancelled firebase - didn't retrieve token");
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (connectivityManager != null) {
                return connectivityManager.getActiveNetwork() != null && connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork()) != null;
            }
        }
        return false;
    }

    private void ConnectedToInternet()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI).addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_VPN);
        NetworkRequest request = builder.build();
        Network network = new Network();
        network.setListener(new NetworkChange() {
            @Override
            public void onNetwork() {
                if (!networkConnection)
                {
                    networkConnection = true;
                    Toast.makeText(ConversationActivity.this, "network is available, functionality regained", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onNoNetwork() {
                networkConnection = false;
                Toast.makeText(ConversationActivity.this, "network isn't available - messages will not be sent or received. connect to the internet in order to regain functionality", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNetworkLost() {
                networkConnection = false;
                Toast.makeText(ConversationActivity.this, "network lost - disconnected from the internet", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChangedNetworkType() {
                //Toast.makeText(ConversationActivity.this, "network changed", Toast.LENGTH_SHORT).show();
            }
        });
        //ConnectivityManager.NetworkCallback networkCallback = network;
        if (connectivityManager!=null)
            connectivityManager.registerNetworkCallback(request,network);

    }

    private String RecipientConversationID(String conversationID) {
        String[] conversationIDSplit = conversationID.split(" {3}");
        String recipientConversationID;
        if (currentUser.equals(conversationIDSplit[0])) {
            recipientConversationID = conversationIDSplit[1] + "   " + conversationIDSplit[0];
        } else {
            recipientConversationID = conversationIDSplit[0] + "   " + conversationIDSplit[1];
        }
        return recipientConversationID;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        controller.onRemoveChildEvent();
        controller.setConversationGUI(null);
        goingBack = true;
    }


    @Override
    public void onPicked(int[] time) {
        Intent intent = new Intent(ConversationActivity.this, AlarmReceiverBroadcast.class);
        intent.putExtra("messageToSend", messageSent.getText().toString());
        intent.putExtra("recipient", recipientUID);
        intent.putExtra("sender", currentUser);
        intent.putExtra("senderName", user.getName());
        intent.putExtra("conversationID", conversationID);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(ConversationActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) ConversationActivity.this.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, time[0]);
        calendar.set(Calendar.MONTH, time[1]);
        calendar.set(Calendar.DAY_OF_MONTH, time[2]);
        calendar.set(Calendar.HOUR_OF_DAY, time[3]);
        calendar.set(Calendar.MINUTE, time[4]);
        calendar.set(Calendar.SECOND, 0);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmPendingIntent);
            Toast.makeText(ConversationActivity.this, "message will be sent later", Toast.LENGTH_SHORT).show();
            messageSent.setText("");
        } else
            Toast.makeText(ConversationActivity.this, "Error while setting delay on message,try again later", Toast.LENGTH_SHORT).show();

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(PICKER_FRAGMENT_TAG);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }
    }

    @Override
    public void onCancelPick() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(PICKER_FRAGMENT_TAG);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }
    }

    /*private void setViewGone(View view) {
        view.setVisibility(View.GONE);
    }*/

    @Override
    public void onBottomSheetClick(ButtonType buttonType) {
        actionState = buttonType.ordinal();
        DialogFragment fragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(BOTTOM_SHEET_TAG);
        if (fragment != null)
            fragment.dismiss();
        switch (buttonType) {
            case attachFile: {
                if (!darkMode)
                    sendActionBtn.setImageResource(R.drawable.ic_baseline_attach_file_24);
                else
                    sendActionBtn.setImageResource(R.drawable.ic_baseline_attach_file_white);
                break;
            }
            case camera: {
                if (!darkMode)
                    sendActionBtn.setImageResource(R.drawable.ic_baseline_camera_alt_24);
                else
                    sendActionBtn.setImageResource(R.drawable.ic_baseline_camera_alt_white);
                break;
            }
            case gallery: {
                if (!darkMode)
                    sendActionBtn.setImageResource(R.drawable.ic_baseline_photo_24);
                else
                    sendActionBtn.setImageResource(R.drawable.ic_baseline_insert_photo_white);
                break;
            }
            case location: {
                if (!darkMode)
                    sendActionBtn.setImageResource(R.drawable.ic_baseline_location_on_24);
                else
                    sendActionBtn.setImageResource(R.drawable.location_white);
                break;
            }
            case delay: {
                if (!darkMode)
                    sendActionBtn.setImageResource(R.drawable.ic_baseline_access_time_black);
                else
                    sendActionBtn.setImageResource(R.drawable.ic_baseline_access_time_white);
                break;
            }
            default:
                Log.e(ERROR_CASE, "bottom sheet error " + new Throwable().getStackTrace()[0].getLineNumber());
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Drawable drawable = Drawable.createFromPath(photoPath);
            if (drawable != null) {
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageDrawable(drawable);

                if (buttonState != SEND_MESSAGE)
                    if (!darkMode)
                        imageSwitcher.setImageResource(R.drawable.ic_baseline_send_24);
                    else
                        imageSwitcher.setImageResource(R.drawable.ic_baseline_send_white);
                sendActionBtn.setVisibility(View.GONE);
                //   sendMessageButton.setText(getResources().getString(R.string.send));
                buttonState = SEND_MESSAGE;


                finalMessageType = MessageType.photoMessage;
                camera = true;
            }
        } else if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageURI(uri);
                    imageBitmap = getImageBitmap(uri);
                    imageView.setImageBitmap(imageBitmap);
                    imageBitmap = Bitmap.createScaledBitmap(imageBitmap, 500, 450, false);
                    finalMessageType = MessageType.photoMessage;
                    camera = false;
                }
            }
        } else if (requestCode == REQUEST_SELECT_PHONE_NUMBER && resultCode == RESULT_OK) {
            if (data != null) {
                Uri contactUri = data.getData();
                String[] projections = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
                if (contactUri != null) {
                    Cursor cursor = getContentResolver().query(contactUri, projections, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        String number = cursor.getString(numberIndex);
                        cursor.close();
                        controller.onUpdateData("users/" + currentUser + "/phoneNumbers/" + recipientUID, number);
                        Toast.makeText(this, "saved number " + number + " to user", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void requestCamera() {
        if (AskPermission(MessageType.photoMessage))
            takePicture();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select Picture to Upload"), GALLERY_REQUEST);
    }

    private void takePicture() {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = ConversationActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File image = File.createTempFile(imageFileName, ".jpg", storageDir);
            photoPath = image.getAbsolutePath();
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(ConversationActivity.this.getPackageManager()) != null) {
                File photoFile;
                photoFile = image;
                Uri photoURI = FileProvider.getUriForFile(ConversationActivity.this,
                        "com.example.woofmeow.provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                imageUri = photoURI;
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap getImageBitmap(Uri uri) {
        Bitmap image = null;
        try {
            if (Build.VERSION.SDK_INT > 27) {
                ImageDecoder.Source source = ImageDecoder.createSource(ConversationActivity.this.getContentResolver(), uri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                image = MediaStore.Images.Media.getBitmap(ConversationActivity.this.getContentResolver(), uri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }


    @Override
    public void bottomSheetGone() {
        Toast.makeText(this, "Done Shitting", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.conversation_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //extra options
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (checkOverriddenPhoneNumber() != null)
            recipientPhoneNumber = checkOverriddenPhoneNumber();
        switch (item.getItemId()) {
            case R.id.callBtn:
                //opening dialer to call the recipient number if exists
                if (recipientPhoneNumber != null)
                    if (directCall)
                        CallPhone(AskPermission(MessageType.callPhone));
                    else
                        CallPhone(false);
                break;
            case R.id.addAsContact:
                //adds current recipient as a contact to contacts list
                Intent addContactIntent = new Intent(Intent.ACTION_INSERT);
                addContactIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                addContactIntent.putExtra(ContactsContract.Intents.Insert.NAME, "recipient Name");//first name ---- space ---- lastName
                addContactIntent.putExtra(ContactsContract.Intents.Insert.PHONE, "0542071248");
                addContactIntent.putExtra(ContactsContract.Intents.Insert.EMAIL, "dorfmanhanan@gmail.com");
                if (addContactIntent.resolveActivity(getPackageManager()) != null)
                    startActivity(addContactIntent);
                break;
            case R.id.addPhoneNumber:
                //opens contact and gets selected contact number
                View builderView = getLayoutInflater().inflate(R.layout.edit_text_dialog, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                EditText text = builderView.findViewById(R.id.editTextDialog);
                Button button = builderView.findViewById(R.id.okBtn);
                AlertDialog alert = builder.setTitle("Add a phone number to this contact")
                        .setMessage("choose a phone number from contacts or type one")
                        .setCancelable(true)
                        .setPositiveButton("from contacts", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent addPhoneNumberIntent = new Intent(Intent.ACTION_PICK);
                                addPhoneNumberIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                                if (addPhoneNumberIntent.resolveActivity(getPackageManager()) != null)
                                    startActivityForResult(addPhoneNumberIntent, REQUEST_SELECT_PHONE_NUMBER);
                                dialog.dismiss();
                            }
                        }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setView(builderView).create();
                alert.show();

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String phoneNumber = text.getText().toString();
                        controller.onUpdateData("users/" + currentUser + "/phoneNumbers/" + recipientUID, phoneNumber);
                        alert.dismiss();
                    }
                });
                break;
            case R.id.meetUp:
                //sets a calendar appointment for both participants (after asking permission from the recipient) - not implemented yet
                GeneralFragment fragment = new GeneralFragment();
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.add(R.id.contentContainer,fragment,"MeetUp");
                transaction.addToBackStack(null);
                transaction.commit();
                /*Intent calendarIntent = new Intent(Intent.ACTION_INSERT)
                        .setData(CalendarContract.Events.CONTENT_URI)
                        .putExtra(CalendarContract.Events.TITLE, "MeetUp")
                        .putExtra(CalendarContract.Events.EVENT_LOCATION, "address")
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, "TIME EVENT STARTS")
                        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, "TIME EVENT ENDS");
                if (calendarIntent.resolveActivity(getPackageManager()) != null)
                    startActivity(calendarIntent);*/
                break;
            case R.id.block:
                //blocks current recipient from sending messages to current user

                ArrayList<String> blocked = user.getBlockedUsers();
                if (blocked.size() == 0)
                    controller.onUpdateData("users/" + currentUser + "/block/" + recipientUID, recipientUID);
                else {
                    for (String block : blocked) {
                        if (block.equals(recipientUID)) {
                            controller.onRemoveData("users/" + currentUser + "/block/" + recipientUID);
                            Toast.makeText(this, "user: " + recipientName + " " + recipientLastName + " is un blocked!", Toast.LENGTH_SHORT).show();
                        } else {
                            controller.onUpdateData("users/" + currentUser + "/block/" + recipientUID, recipientUID);
                            Toast.makeText(this, "user: " + recipientName + " " + recipientLastName + " is blocked!", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
                break;
            case R.id.share:
                if (selectedMessage != null) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, selectedMessage.getMessage());
                    sendIntent.setType("text/plain");
                    Intent shareIntent = Intent.createChooser(sendIntent, null);
                    startActivity(shareIntent);
                    invalidateOptionsMenu();
                }
                selectedMessage = null;
                break;
            case R.id.messageInfo:
                if (selectedMessage != null) {
                    BackdropFragment backdropFragment = BackdropFragment.newInstance();
                    Bundle backDropBundle = new Bundle();
                    backDropBundle.putString("senderName", selectedMessage.getSenderName());
                    backDropBundle.putString("timeSent", selectedMessage.getMessageTime());
                    backDropBundle.putString("message", selectedMessage.getMessage());
                    backDropBundle.putInt("messageType", selectedMessage.getMessageType());
                    backDropBundle.putBoolean("messageSeen", selectedMessage.isHasBeenRead());
                    backDropBundle.putString("conversationID", selectedMessage.getConversationID());
                    backdropFragment.setArguments(backDropBundle);
                    backdropFragment.show(getSupportFragmentManager(), BOTTOM_SHEET_TAG);
                    selectedMessage = null;
                    invalidateOptionsMenu();
                }
                break;

            case R.id.searchMessage: {
                Animation in = AnimationUtils.loadAnimation(this, R.anim.slide_down);
                Animation out = AnimationUtils.loadAnimation(this, R.anim.slide_up);
                if (searchLayout.getVisibility() == View.GONE) {
                    searchText.setText("");
                    searchLayout.setVisibility(View.VISIBLE);
                    searchLayout.startAnimation(in);
                    recyclerView.startAnimation(in);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            searchLayout.setVisibility(View.GONE);
                        }
                    }, out.getDuration());
                    searchLayout.startAnimation(out);
                    recyclerView.startAnimation(out);
                }
                break;
            }
            case R.id.copy: {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("message", selectedMessage.getMessage());
                if (clipboardManager != null) {
                    clipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(this, "Copied message", Toast.LENGTH_SHORT).show();
                } else
                    Snackbar.make(this, recyclerView, "oops, something went wrong", Snackbar.LENGTH_SHORT).setAction("Submit error report", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //opens error activity or fragment for the user to fill in OR sends information of this error automatically
                            Toast.makeText(ConversationActivity.this, "Error report was submitted, thank you", Toast.LENGTH_SHORT).show();
                        }
                    }).show();
                break;
            }
            case R.id.starMessage: {
                Toast.makeText(this, "pressed star", Toast.LENGTH_SHORT).show();
                break;
            }
            default:
                Log.e(ERROR_CASE, "menu error");
        }
        return super.onOptionsItemSelected(item);
    }

    private void CallPhone(boolean permissionGranted) {
        Intent callRecipientIntent = null;

        if (directCall) //preferences option
        {
            if (permissionGranted)
                callRecipientIntent = new Intent(Intent.ACTION_CALL);
            else
                AskPermission(MessageType.callPhone);
        } else {
            callRecipientIntent = new Intent(Intent.ACTION_DIAL);
        }
        if (callRecipientIntent != null) {
            callRecipientIntent.setData(Uri.parse("tel:" + recipientPhoneNumber));//should be recipient phone number
            if (callRecipientIntent.resolveActivity(getPackageManager()) != null)
                startActivity(callRecipientIntent);
        }
    }


    @Override
    public void onReceiveMessages(ArrayList<Message> messages) {
        if (messages != null) {
            //if (!isBlocked()) {
            chatAdapter.setMessages(messages);
            chatAdapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(messages.size() - 1);
            //sendMessageButton.setText(getResources().getString(R.string.record));
            if (!darkMode)
                imageSwitcher.setImageResource(R.drawable.ic_baseline_mic_black);
            else
                imageSwitcher.setImageResource(R.drawable.ic_baseline_mic_white);
            buttonState = RECORD_VOICE;
            HashMap<String, Object> statusMap = new HashMap<>();
            statusMap.put("messageStatus", MESSAGE_SEEN);
            String recipientConversationID = RecipientConversationID(messages.get(messages.size() - 1).getConversationID());
            UpdateMessageStatus("users/" + recipientUID + "/conversations/" + recipientConversationID + "/conversationInfo/conversationMessages/" + messages.get(messages.size() - 1).getMessageID(), statusMap);
            // controller.onUpdateData("users/" + recipientUID + "/conversations/" + recipientConversationID + "/conversationInfo/conversationMessages/" + messages.get(messages.size() - 1).getMessageID(), statusMap);
            //}
        }
    }

    @Override
    public void onReceiveSingleMessage(Message message) {
        if (message != null) {
            // if (!isBlocked()) {

            if (buttonState != RECORD_VOICE) {
                if (link == null) {
                    if (!darkMode)
                        imageSwitcher.setImageResource(R.drawable.ic_baseline_mic_black);
                    else
                        imageSwitcher.setImageResource(R.drawable.ic_baseline_mic_white);
                    buttonState = RECORD_VOICE;
                } else {
                    ShowMessageForUserConfirmation();
                    if (!darkMode)
                        imageSwitcher.setImageResource(R.drawable.ic_baseline_send_24);
                    else
                        imageSwitcher.setImageResource(R.drawable.ic_baseline_send_white);
                }
            }
            //no need to update server database message status after the message is seen
            if (!message.getMessageStatus().equals(MESSAGE_SEEN) && !message.getSender().equals(currentUser)) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ConversationActivity.this);
                boolean messageSeen = preferences.getBoolean("readMessage", true);
                if (messageSeen) {
                    HashMap<String, Object> statusMap = new HashMap<>();
                    statusMap.put("messageStatus", MESSAGE_SEEN);
                    String recipientConversationID = RecipientConversationID(message.getConversationID());
                    UpdateMessageStatus("users/" + recipientUID + "/conversations/" + recipientConversationID + "/conversationInfo/conversationMessages/" + message.getMessageID(), statusMap);
                    //  controller.onUpdateData("users/" + recipientUID + "/conversations/" + recipientConversationID + "/conversationInfo/conversationMessages/" + message.getMessageID(), statusMap);
                }
            }

            if (message.getReadAt() == -1 && !message.getSender().equals(currentUser))//-1 is the default value
            {
                HashMap<String, Object> readAt = new HashMap<>();
                readAt.put("readAt", System.currentTimeMillis());
                message.setReadAt(System.currentTimeMillis());
                UpdateMessageStatus("users/" + currentUser + "/conversations/" + conversationID + "/conversationInfo/conversationMessages/" + message.getMessageID(), readAt);
                //controller.onUpdateData("users/" + currentUser + "/conversations/" + message.getConversationID() + "/conversationInfo/conversationMessages/" + message.getMessageID(), readAt);
            }


            if (!CheckIfExistsInDataBase(message)) {
                chatAdapter.addNewMessage(message);
                chatAdapter.notifyItemInserted(chatAdapter.getItemCount() - 1);
                recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
            }
            InsertToDataBase(message);
            // }
        }
    }

    private void UpdateMessageStatus(String path, HashMap<String, Object> messageStatus) {
        if (!path.contains("null")) {
            controller.onUpdateData(path, messageStatus);
        } else
            Log.e(NULL_ERROR, "NULL in path in function UpdateMessageStatus");
    }

    @Override
    public void onReceiveItemChange(Message message, int position) {
        if (message != null) {
            //   if (!isBlocked()) {
            UpdateDataBase(message, position);
            chatAdapter.changeExistingMessage(message, position);
            //chatAdapter.notifyItemChanged(position);
            //}
        }
    }

    @Override
    public void onRemoveDeletedMessage(int position) {
        if (position < chatAdapter.getItemCount()) {
            chatAdapter.notifyItemRemoved(position);
        }
    }
/*
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isBlocked() {
        return user.getBlockedUsers().contains(recipientUID);
    }*/

    @Override
    public void onReceiveUser(User user) {
        if (user.getUserUID().equals(currentUser)) {
            this.user = user;
            if (!calledRecipient)
                controller.onDownloadUser(ConversationActivity.this, recipientUID);
            calledRecipient = true;
        } else {
            recipient = user;
            GetRecipientToken(recipient.getUserUID());
            String recipientPictureLink = recipient.getPictureLink();
            Picasso.get().load(recipientPictureLink).into(talkingToImage);
            recipientName = user.getName();
            recipientLastName = user.getLastName();
            talkingTo = new String[3];
            talkingTo[0] = recipientName;
            talkingTo[1] = recipientLastName;
            talkingTo[2] = recipientName + " " + recipientLastName;
            textSwitcher.setCurrentText(talkingTo[0]);
            talkingToIndex++;
            String status = user.getStatus();
            if (status != null) {
                textSwitcherStatus.setText(status);
                if (status.equals(MainActivity.ONLINE_S))
                    statusView.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.circle_green, getTheme()));
                else if (status.equals(MainActivity.OFFLINE_S) || status.equals(MainActivity.STANDBY_S))
                    statusView.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.circle_red, getTheme()));
            }
            if (!calledMessages)
                controller.onDownloadMessages(ConversationActivity.this, conversationID, 20);
            calledMessages = true;
        }
    }

    private void isRecipientTyping()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("users/" + currentUser + "/conversations/" + conversationID + "/conversationInfo/typing");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    boolean typing = (Boolean) snapshot.getValue();
                    if (typing)
                        textSwitcherTyping.setText("typing");
                    else
                        textSwitcherTyping.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String checkOverriddenPhoneNumber() {
        return user.getRecipientPhoneNumber(recipientUID);
    }


    @Override
    public void run() {
        int seconds = 0, minutes = 0;
        String minuteString, secondsString;
        if (player != null && startPlaying1)
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    startPlaying1 = !startPlaying1;
                    if (!darkMode)
                        playAudioRecordingBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                    else
                        playAudioRecordingBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_white);
                    voiceSeek.setProgress(0);
                    recordingTimeText.setText(getResources().getString(R.string.zero_time));
                }
            });
        while (startRecording) {
            try {
                if (seconds == 60) {
                    seconds = 0;
                    minutes++;
                }
                if (minutes == 60) {
                    minutes = 0;
                }
                if (minutes < 10)
                    minuteString = "0" + minutes;
                else minuteString = minutes + "";
                if (seconds < 10)
                    secondsString = "0" + seconds;
                else
                    secondsString = seconds + "";
                String recordingTime = minuteString + ":" + secondsString;
                recordingTimeText.setText(recordingTime);
                TimeUnit.SECONDS.sleep(1);
                seconds++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (player != null)
            while (player.isPlaying()) {
                String playBackTimeString;
                seconds = player.getCurrentPosition() / 1000;
                int secondsReset = seconds % 60;
                if (secondsReset == 0 && seconds != 0) {
                    minutes++;
                }

                if (secondsReset < 10)
                    secondsString = "0" + secondsReset;
                else
                    secondsString = secondsReset + "";
                if (minutes < 10)
                    minuteString = "0" + minutes;
                else
                    minuteString = minutes + "";
                playBackTimeString = minuteString + ":" + secondsString;
                Handler handler = new Handler(getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        recordingTimeText.setText(playBackTimeString);
                    }
                });
                voiceSeek.setProgress(player.getCurrentPosition());
            }
    }

    private void DataBaseSetUp() {
        if (db == null) {
            DataBase dbHelper = new DataBase(this);
            db = dbHelper.getWritableDatabase();
            //db.execSQL("DROP TABLE IF EXISTS " + DataBaseContract.Messages.MESSAGES_TABLE);
            //dbHelper.onCreate(db);
            // dbHelper.onUpgrade(db, db.getVersion(), db.getVersion() + 1);
        }
    }

    private void InsertToDataBase(Message message) {
        if (!CheckIfExistsInDataBase(message)) {
            ContentValues values = new ContentValues();
            values.put(DataBaseContract.Messages.MESSAGE_ID, message.getMessageID());
            values.put(DataBaseContract.Entry.CONVERSATIONS_ID_COLUMN_NAME, conversationID);
            values.put(DataBaseContract.Messages.MESSAGE_CONTENT_COLUMN_NAME, message.getMessage());
            values.put(DataBaseContract.Messages.MESSAGE_RECIPIENT_COLUMN_NAME, message.getRecipient());
            values.put(DataBaseContract.Messages.MESSAGE_SENDER_COLUMN_NAME, message.getSender());
            values.put(DataBaseContract.Messages.MESSAGE_TIME_DELIVERED_COLUMN_NAME, message.getReadAt());
            values.put(DataBaseContract.Messages.MESSAGE_TIME_SENT_COLUMN_NAME, message.getMessageTime());
            values.put(DataBaseContract.Messages.MESSAGE_TYPE_COLUMN_NAME, message.getMessageType());
            values.put(DataBaseContract.Messages.MESSAGE_STATUS_COLUMN_NAME, message.getMessageStatus());

            long newRowId = db.insert(DataBaseContract.Messages.MESSAGES_TABLE, null, values);
            if (newRowId == -1)
                Log.e(DATABASE_ERROR, "inserted more than 1 row");
        }
    }

    private void UpdateDataBase(Message message, int position) {
        PrintDataBase();
        ContentValues values = new ContentValues();
        values.put(DataBaseContract.Messages.MESSAGE_ID, message.getMessageID());
        values.put(DataBaseContract.Entry.CONVERSATIONS_ID_COLUMN_NAME, conversationID);
        values.put(DataBaseContract.Messages.MESSAGE_CONTENT_COLUMN_NAME, message.getMessage());
        values.put(DataBaseContract.Messages.MESSAGE_RECIPIENT_COLUMN_NAME, message.getRecipient());
        values.put(DataBaseContract.Messages.MESSAGE_SENDER_COLUMN_NAME, message.getSender());
        values.put(DataBaseContract.Messages.MESSAGE_TIME_DELIVERED_COLUMN_NAME, message.getReadAt());
        values.put(DataBaseContract.Messages.MESSAGE_TIME_SENT_COLUMN_NAME, message.getMessageTime());
        values.put(DataBaseContract.Messages.MESSAGE_TYPE_COLUMN_NAME, message.getMessageType());
        values.put(DataBaseContract.Messages.MESSAGE_STATUS_COLUMN_NAME, message.getMessageStatus());
        String selection = DataBaseContract.Messages.MESSAGE_ID + " LIKE ?";
        String[] selectionArgs = {message.getMessageID()};
        int count = db.update(DataBaseContract.Messages.MESSAGES_TABLE, values, selection, selectionArgs);
        PrintDataBase();
        if (count <= 0)
            Log.e(DATABASE_ERROR, "didn't update a thing");
    }

    public void DeleteFromDataBase(Message message) {
        String selection = DataBaseContract.Messages.MESSAGE_ID + " LIKE ?";
        String[] selectionArgs = {message.getMessageID()};
        int deletedRows = db.delete(DataBaseContract.Entry.CONVERSATIONS_TABLE, selection, selectionArgs);
        if (deletedRows == -1)
            Log.e(DATABASE_ERROR, "didn't delete anything - deleted rows = -1");

    }

    public void PrintDataBase() {
        if (db != null) {
            String[] projection = {
                    BaseColumns._ID,
                    DataBaseContract.Messages.MESSAGE_ID,
                    DataBaseContract.Entry.CONVERSATIONS_ID_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_CONTENT_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_RECIPIENT_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_SENDER_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_TIME_DELIVERED_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_TIME_SENT_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_TYPE_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_STATUS_COLUMN_NAME
            };
            String selection = DataBaseContract.Entry.CONVERSATIONS_ID_COLUMN_NAME + " LIKE ?";
            String[] selectionArgs = {conversationID};
            Cursor cursor = db.query(DataBaseContract.Messages.MESSAGES_TABLE, projection, selection, selectionArgs, null, null, null);
            List<String> MessagesIDs = new ArrayList<>();
            List<String> MessagesContent = new ArrayList<>();
            List<String> MessagesRecipient = new ArrayList<>();
            List<String> MessagesSender = new ArrayList<>();
            List<String> MessageTimeDelivered = new ArrayList<>();
            List<String> MessagesTimeSent = new ArrayList<>();
            List<String> MessagesTypes = new ArrayList<>();
            List<String> MessagesStatus = new ArrayList<>();

            while (cursor.moveToNext()) {
                String messageID = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_ID));
                MessagesIDs.add(messageID);
                String messageContent = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_CONTENT_COLUMN_NAME));
                MessagesContent.add(messageContent);
                String messagesRecipient = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_RECIPIENT_COLUMN_NAME));
                MessagesRecipient.add(messagesRecipient);
                String messagesSender = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_SENDER_COLUMN_NAME));
                MessagesSender.add(messagesSender);
                String messageTimeDelivered = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_TIME_DELIVERED_COLUMN_NAME));
                MessageTimeDelivered.add(messageTimeDelivered);
                String messageTimeSent = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_TIME_SENT_COLUMN_NAME));
                MessagesTimeSent.add(messageTimeSent);
                String messagesType = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_TYPE_COLUMN_NAME));
                MessagesTypes.add(messagesType);
                String messagesStatus = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_STATUS_COLUMN_NAME));
                MessagesStatus.add(messagesStatus);
            }
            cursor.close();

            System.out.println("the messages: " + MessagesContent);
            System.out.println("messages IDs: " + MessagesIDs);
        }
    }

    //needs major check
    private boolean CheckIfExistsInDataBase(Message message) {
        if (db != null) {
            String[] projections = {
                    BaseColumns._ID,
                    DataBaseContract.Messages.MESSAGE_ID
            };
            String query = "SELECT " + DataBaseContract.Messages.MESSAGE_ID + " FROM " + DataBaseContract.Messages.MESSAGES_TABLE + " ORDER BY " + DataBaseContract.Messages.MESSAGE_ID + " DESC LIMIT 1";
            //in order to not scan the database each time from the start, we should start scanning from the last message received - time. since messages
            //come in a linear order, a message that was sent now will never arrive prior to the message that was sent before it
            // String selection = DataBaseContract.Messages.MESSAGE_TIME_DELIVERED_COLUMN_NAME + " = ?";
            String selection = DataBaseContract.Entry.CONVERSATIONS_ID_COLUMN_NAME + " = ?";
            String[] selectionArgs = {conversationID};
            String sortOrder = DataBaseContract.Messages.MESSAGE_TIME_SENT_COLUMN_NAME + " DESC LIMIT 1";
            Cursor cursor = db.query(DataBaseContract.Messages.MESSAGES_TABLE, projections, selection, selectionArgs, null, null, sortOrder);
            if (cursor.moveToNext()) {
                String ID = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_ID));
                long id = Long.parseLong(ID);
                long messageId = Long.parseLong(message.getMessageID());
                cursor.close();
                return id >= messageId;
            } else {
                cursor.close();
                return false;
            }
        }
        return false;
    }

    private void LoadMessagesFromDataBase() {

        if (db != null) {
            String[] projections = {
                    DataBaseContract.Messages.MESSAGE_ID,
                    DataBaseContract.Entry.CONVERSATIONS_ID_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_CONTENT_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_SENDER_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_RECIPIENT_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_TIME_DELIVERED_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_TIME_SENT_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_TYPE_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_STATUS_COLUMN_NAME
            };
            String selection = DataBaseContract.Entry.CONVERSATIONS_ID_COLUMN_NAME + " = ?";
            String[] selectionArgs = {conversationID};
            Cursor cursor = db.query(DataBaseContract.Messages.MESSAGES_TABLE, projections, selection, selectionArgs, null, null, null);
            while (cursor.moveToNext()) {

                String messageID = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_ID));
                String conversationID = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Entry.CONVERSATIONS_ID_COLUMN_NAME));
                String messageContent = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_CONTENT_COLUMN_NAME));
                String messageSender = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_SENDER_COLUMN_NAME));
                String recipient = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_RECIPIENT_COLUMN_NAME));
                long messageTimeDelivered = cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_TIME_DELIVERED_COLUMN_NAME));
                String messageTimeSent = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_TIME_SENT_COLUMN_NAME));
                int messageType = cursor.getInt(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_TYPE_COLUMN_NAME));
                String messageStatus = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_STATUS_COLUMN_NAME));
                Message message = new Message();
                message.setMessageID(messageID);
                message.setMessage(messageContent);
                message.setConversationID(conversationID);
                message.setRecipient(recipient);
                message.setSender(messageSender);
                message.setMessageType(messageType);
                message.setMessageTime(messageTimeSent);
                message.setReadAt(messageTimeDelivered);
                message.setMessageStatus(messageStatus);
                chatAdapter.addNewMessage(message);
                chatAdapter.notifyItemInserted(chatAdapter.getItemCount() - 1);
                recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
            }
            cursor.close();
        }
    }


    @Override
    public void onLetsMeet(String start, String end) {
        HashMap<String,Object>meetMap = new HashMap<>();
        meetMap.put("with",recipientName);
        meetMap.put("withID",recipientUID);
        meetMap.put("start",start);
        meetMap.put("end",end);
        controller.onUpdateData("users/" + currentUser + "/meetUp/" + System.currentTimeMillis(),meetMap);
        meetMap.put("with",user.getName());
        meetMap.put("withID",currentUser);
        controller.onUpdateData("users/" + recipientUID + "/meetUp/" + System.currentTimeMillis(),meetMap);
    }

    @Override
    public void onCancel() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag("MeetUp");
        if (fragment!=null)
            fragmentManager.beginTransaction().remove(fragment).commit();
    }

}
