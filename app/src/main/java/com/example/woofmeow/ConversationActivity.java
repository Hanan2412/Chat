package com.example.woofmeow;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
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
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
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
import BackgroundMessages.InteractionMessage;
import BackgroundMessages.ReadMessage;
import BackgroundMessages.RequestMessage;
import BroadcastReceivers.AlarmReceiverBroadcast;
import Consts.BackgroundMessages;
import Consts.ButtonType;
import Consts.MessageAction;
import Consts.MessageType;
import Consts.Requests;
import Controller.CController;
import Fragments.BackdropFragment;
import Fragments.BottomSheetFragment;
import Fragments.GeneralFragment;
import Fragments.PickerFragment;
import DataBase.DataBase;
import DataBase.DataBaseContract;
import Fragments.TimePickerFragment;
import Fragments.VideoFragment;
import Model.MessageSender;
import Model.Server3;
import Model.Uploads;
import NormalObjects.Message;
import NormalObjects.Network2;
import NormalObjects.NetworkChange;
import NormalObjects.User;
import Retrofit.RetrofitApi;
import Retrofit.RetrofitClient;

//ui doesn't scale with accessibility
@SuppressWarnings("Convert2Lambda")
public class ConversationActivity extends AppCompatActivity implements ChatAdapter.MessageInfoListener, PickerFragment.onPickerClick, BottomSheetFragment.onSheetClicked, BackdropFragment.onBottomSheetAction, Serializable, ConversationGUI, Runnable, TimePickerFragment.onTimePicked {

    private static final String LOG_TAG = "AudioRecordingTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName;
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;

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
    private String currentUser1;
    private String messageToSend;
    private EditText messageSent;
    private String recipientUID;

    private final int GALLERY_REQUEST = 2;
    private final int WRITE_PERMISSION = 3;
    private final int CAMERA_REQUEST = 4;
    private final int LOCATION_REQUEST = 5;
    private final int REQUEST_SELECT_PHONE_NUMBER = 6;
    private final int CALL_PHONE = 7;
    private final int SEND_FILE = 80;
    private final int SEND_CONTACT = 9;
    //private final int PICK_PDF_FILE = 10;
    private final int DOCUMENT_REQUEST = 11;
    private User user;
    private User recipient;
    private String recipientToken;
    //private boolean veteran;
    //private ArrayList<Message> messages;
    private int messagesCounter = 0;
    //private String CHANNEL_ID = "MessagesChannel";

    private ImageButton sendActionBtn;
    private RetrofitApi api;
    private final String PICKER_FRAGMENT_TAG = "Picker_fragment";
    private final String BOTTOM_SHEET_TAG = "BottomSheet_fragment";
    private boolean camera;
    //private MessageType finalMessageType;
    private int actionState = 0;
    private boolean editMode = false;
    private Message editMessage;
    //private Button sendMessageButton;
    private TextView quoteText;
    private boolean quoteOn = false;
    private int quotedMessagePosition = -1;
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
    //private final int SEND_VIDEO = 2;
    private boolean startRecording = true;
    private boolean startPlaying = true;
    private boolean startPlaying1 = true;
    private TextView recordingTimeText;

    private LinearLayout voiceLayout;
    private ImageButton playAudioRecordingBtn;
    private SeekBar voiceSeek;
    // private boolean playingON = false;
    //private TextView playbackTime;
    private boolean recorded = false;
    private ImageView closeBtn;
    private Uri fileUri;

    private String link;//,title;
    private RelativeLayout linkedMessagePreview;
    private ImageView linkedImage;
    private TextView linkTitle, linkContent;

    private ImageSwitcher imageSwitcher;

    private ShapeableImageView talkingToImage;
    private boolean messageLongPress = false;
    private Message selectedMessage;
    private View selectedMessageView;
    private boolean goingBack = false;
    private String quotedMessageID;

    private boolean calledRecipient = false;
    private boolean calledMessages = false;

    private SQLiteDatabase db = null;
    private final String NETWORK_ERROR = "network Error";
    private final String FIREBASE_ERROR = "firebase_Error";
    private final String PROGRAM_INFO = "info";
    private final String NULL_ERROR = "something is null";
    private final String DATABASE_ERROR = "database error";
    //private final String STATUS_INFO = "status";
    private final String ERROR_CASE = "Error in switch case";
    private final String ERROR_WRITE = "write error";
    private LinearLayout searchLayout;
    private EditText searchText;
    private Button searchBtn;
    private ImageButton scrollToNext;
    private ArrayList<Integer> indices;
    private int indicesIndex = 0;
    private boolean networkConnection = true;
    private final int REQUEST_VIDEO_CAPTURE = 8;
    private Uri videoUri;
    private final String VIDEO_FRAGMENT_TAG = "VIDEO_FRAGMENT";
    private boolean iReadThat = true;//allows the recipient to see that we have read the message sent
    private RelativeLayout relativeLayout;
    private Network2 network2;
    private BroadcastReceiver receiveNewMessages;
    private ArrayList<String> recipientsTokens;
    private String conversationID1;
    private int TYPING = 0;
    private int RECORDING = 1;
    private boolean typing = false;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.converastion_layout2);

        conversationID1 = getIntent().getStringExtra("conversationID1");
        recipientsTokens = new ArrayList<>();

        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        ConnectedToInternet();
        if (actionBar != null)
            actionBar.setDisplayShowTitleEnabled(false);
        relativeLayout = findViewById(R.id.root_container);
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
                                MarkMessage(view, true);
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
            Log.i(USER_SERVICE, "downloading user");
        }

        voiceLayout = findViewById(R.id.voiceLayout);
        quoteText = findViewById(R.id.quoteText);

        imageView = findViewById(R.id.imagePreview);

        closeBtn = findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //playingON = false;
                fileName = null;
                if (player != null) {
                    player.stop();
                    stopPlaying();
                }
                recordingTimeText.setText(R.string.zero_time);
                ResetToText();
                buttonState = RECORD_VOICE;
                SetCorrectColor(ButtonType.microphone);
            }
        });
        api = RetrofitClient.getRetrofitClient("https://fcm.googleapis.com/").create(RetrofitApi.class);
        geocoder = new Geocoder(this);
        //messages = new ArrayList<>();
        //conversation id is sent to this activity regardless of from where this activity was lunched
        conversationID = getIntent().getStringExtra("conversationID");
        recipientPhoneNumber = getIntent().getStringExtra("recipientPhone");
        String recipientImagePath = getIntent().getStringExtra("recipientImagePath");
        //title = getIntent().getStringExtra("title");
        link = getIntent().getStringExtra("link");
        isRecipientTyping();

        //someone sent me a message and i clicked on a notification
        if (getIntent().getBooleanExtra("tapMessageNotification", false)) {
            recipientUID = getIntent().getStringExtra("senderUID");

        } else {
            //i clicked on a conversation in the conversations tab
            recipientUID = getIntent().getStringExtra("recipient");
        }
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
                if (chatAdapter.getItemCount() > 0) {//prevents updating and creating new incomplete conversation object in server before first message sent
                    if(!typing) {
                        SendMessageInteraction(conversationID, TYPING, true);
                        typing = true;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (chatAdapter.getItemCount() > 0)//prevents updating and creating new incomplete conversation object in server before first message sent
                    if (s.toString().equals("")) {
                        SendMessageInteraction(conversationID, TYPING, false);
                        typing = false;
                    }
                if (s.toString().equals("")) {
                    if (buttonState != RECORD_VOICE)
                        SetCorrectColor(ButtonType.microphone);
                    sendActionBtn.setVisibility(View.VISIBLE);
                    buttonState = RECORD_VOICE;
                } else {
                    if (buttonState != SEND_MESSAGE)
                        SetCorrectColor(ButtonType.sendMessage);
                    sendActionBtn.setVisibility(View.GONE);
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
            SetCorrectColor(ButtonType.microphone);
        } else {
            SetCorrectColor(ButtonType.sendMessage);

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

        //LoadMessagesFromDataBase();

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
        SetUpBySettings();
        quoteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quoteText.setText("");
                quoteText.setVisibility(View.GONE);
                quoteOn = false;
                recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
            }
        });

        if (chatAdapter.getItemCount() > 0)
            recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
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
                            PrepareEditedMessage(editMessage);
                            //SendEditedMessage();
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


        playAudioRecordingBtn = findViewById(R.id.play_pause_btn);
        playAudioRecordingBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (player == null) {
                    PlayOrNot();
                } else if (player.isPlaying()) {
                    player.pause();
                    SetCorrectColor(ButtonType.play);
                } else {
                    player.start();
                    startPlaying1 = !startPlaying1;
                    Thread playBackThread = new Thread(ConversationActivity.this);
                    playBackThread.setName("playBackThread");
                    playBackThread.start();
                    SetCorrectColor(ButtonType.pause);
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
        sendActionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ButtonType type = ButtonType.values()[actionState];
                switch (type) {
                    case location: {
                        onLocationAction();
                        break;
                    }
                    case attachFile: {
                        onFileAction();
                        break;
                    }
                    case camera: {
                        onCameraAction();
                        break;
                    }
                    case gallery: {
                        onGalleryAction();
                        break;
                    }
                    case delay: {
                        onDelayAction();
                        break;
                    }
                    case video: {
                        onVideoAction();
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
                //opens bottom sheet do display extra options
                BottomSheetFragment bottomSheetFragment = BottomSheetFragment.newInstance();
                bottomSheetFragment.show(getSupportFragmentManager(), BOTTOM_SHEET_TAG);
                return true;
            }
        });
        talkingToImage = findViewById(R.id.toolbarProfileImage);
        LoadRecipientImage();
        talkingToImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openRecipientIntent = new Intent(ConversationActivity.this, ProfileActivity2.class);
                openRecipientIntent.putExtra("recipient", recipient);
                openRecipientIntent.putExtra("currentUser", user);
                openRecipientIntent.putExtra("recipientImagePath", recipientImagePath);
                openRecipientIntent.putExtra("conversationID", conversationID);
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

        init(conversationID);
    }

    private void LoadRecipientImage() {
        try {
            ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
            File directory = contextWrapper.getDir("user_images", Context.MODE_PRIVATE);
            File imageFile = new File(directory, recipientUID + "_Image");
            Bitmap imageBitmap = BitmapFactory.decodeStream(new FileInputStream(imageFile));
            talkingToImage.setImageBitmap(imageBitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void onLocationAction() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ConversationActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Send Current Location").setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
        }).setMessage("Are you sure you want to send your current location to " + recipient.getName());
        builder.show();
    }

    private void onCameraAction() {
        if (AskPermission(MessageType.photoMessage))
            requestCamera();
    }

    private void onFileAction() {
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
                Intent attachFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                attachFileIntent.setType("*/*");
                startActivityForResult(Intent.createChooser(attachFileIntent, "select file"), SEND_FILE);
            }
        }).setMessage("Are you sure you want to send a file");
        builder.create().show();
    }

    private void onContactAction() {

    }

    private void onDocumentAction() {
        Intent openDocIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(openDocIntent, DOCUMENT_REQUEST);
    }

    private void onVideoAction() {
        if (AskPermission(MessageType.photoMessage))
            RecordVideo();
    }

    private void onGalleryAction() {
        openGallery();
    }

    private void onDelayAction() {
        PickerFragment pickerFragment = PickerFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.contentContainer, pickerFragment, PICKER_FRAGMENT_TAG);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void SetUpBySettings() {
        final String SMALL = "small";
        final String MEDIUM = "medium";
        final String LARGE = "large";
        //settings information
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String chosenActionBtn = preferences.getString("ActionButton", "mega");
        darkMode = preferences.getBoolean("darkView", false);
        directCall = preferences.getBoolean("directCall", false);
        switch (chosenActionBtn) {
            case "Location":
                SetCorrectColor(ButtonType.location);
                actionState = ButtonType.location.ordinal();
                break;
            case "Camera":
                SetCorrectColor(ButtonType.camera);
                actionState = ButtonType.camera.ordinal();
                break;
            case "Gallery":
                SetCorrectColor(ButtonType.gallery);
                actionState = ButtonType.gallery.ordinal();
                break;
            case "Delayed Message": {
                SetCorrectColor(ButtonType.delay);
                actionState = ButtonType.delay.ordinal();
                break;
            }
            case "Video Message": {
                SetCorrectColor(ButtonType.video);
                actionState = ButtonType.video.ordinal();
                break;
            }
            default:
                SetCorrectColor(ButtonType.location);
                actionState = ButtonType.location.ordinal();
                Log.e(ERROR_CASE, "default action preference");
        }
        if (darkMode) {
            relativeLayout.setBackgroundColor(getResources().getColor(android.R.color.black, getTheme()));
            messageSent.setBackgroundColor(getResources().getColor(android.R.color.black, getTheme()));
            messageSent.setHintTextColor(getResources().getColor(android.R.color.white, getTheme()));
            messageSent.setTextColor(getResources().getColor(android.R.color.white, getTheme()));
            //sendActionBtn.setImageResource(R.drawable.location_white);
        } else {
            relativeLayout.setBackgroundColor(getResources().getColor(android.R.color.white, getTheme()));
            messageSent.setBackgroundColor(getResources().getColor(android.R.color.white, getTheme()));
            messageSent.setHintTextColor(getResources().getColor(android.R.color.black, getTheme()));
            messageSent.setTextColor(getResources().getColor(android.R.color.black, getTheme()));
            //sendActionBtn.setImageResource(R.drawable.ic_baseline_location_on_24);
        }
        String textSize = preferences.getString("textSize", "medium");
        switch (textSize) {
            case SMALL:
                chatAdapter.setTextSize(12);
                break;
            case MEDIUM:
                chatAdapter.setTextSize(30);
                break;
            case LARGE:
                chatAdapter.setTextSize(48);
                break;
        }
        iReadThat = preferences.getBoolean("readMessage", true);

    }

    private void SetCorrectColor(ButtonType type) {
        switch (type) {
            case location: {
                if (!darkMode)
                    sendActionBtn.setImageResource(R.drawable.ic_baseline_location_on_24);
                else
                    sendActionBtn.setImageResource(R.drawable.location_white);
                break;
            }
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
                    sendActionBtn.setImageResource(R.drawable.ic_baseline_camera_alt_white);
                break;
            }
            case delay: {
                if (!darkMode)
                    sendActionBtn.setImageResource(R.drawable.ic_baseline_access_time_black);
                else
                    sendActionBtn.setImageResource(R.drawable.ic_baseline_access_time_white);
                break;
            }
            case video: {
                if (!darkMode)
                    sendActionBtn.setImageResource(R.drawable.ic_baseline_videocam_black);
                else
                    sendActionBtn.setImageResource(R.drawable.ic_baseline_videocam_white);
                break;
            }
            case sendMessage: {
                if (!darkMode)
                    imageSwitcher.setImageResource(R.drawable.ic_baseline_send_24);
                else
                    imageSwitcher.setImageResource(R.drawable.ic_baseline_send_white);
                break;
            }
            case microphone: {
                if (!darkMode)
                    imageSwitcher.setImageResource(R.drawable.ic_baseline_mic_black);
                else
                    imageSwitcher.setImageResource(R.drawable.ic_baseline_mic_white);
                break;
            }
            case recording: {
                if (!darkMode)
                    imageSwitcher.setImageResource(R.drawable.ic_baseline_settings_voice_black);
                else
                    imageSwitcher.setImageResource(R.drawable.ic_baseline_settings_voice_white);
                break;
            }
            case play: {
                if (!darkMode)
                    playAudioRecordingBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                else
                    playAudioRecordingBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_white);
                break;
            }
            case pause: {
                if (!darkMode)
                    playAudioRecordingBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
                else
                    playAudioRecordingBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_white);
                break;
            }
            default: {
                if (!darkMode)
                    sendActionBtn.setImageResource(R.drawable.ic_baseline_location_on_24);
                else
                    sendActionBtn.setImageResource(R.drawable.location_white);
                Log.i(ERROR_CASE, "default color");
                break;
            }
        }
    }

    private void ShowMessageForUserConfirmation() {
        messageSent.setText(link);
        buttonState = SEND_MESSAGE;
        SetCorrectColor(ButtonType.sendMessage);
    }


    /*private void SendEditedMessage() {
        HashMap<String, Object> editMessageMap = new HashMap<>();
        editMessageMap.put("message", messageToSend);
        controller.onUpdateData("users/" + currentUser + "/conversations/" + editMessage.getConversationID() + "/conversationInfo/conversationMessages/" + editMessage.getMessageID() + "/", editMessageMap);
        String recipientConversationID = RecipientConversationID(editMessage.getConversationID());
        controller.onUpdateData("users/" + recipientUID + "/conversations/" + recipientConversationID + "/conversationInfo/conversationMessages/" + editMessage.getMessageID() + "/", editMessageMap);
        messageSent.setText("");
        editMode = false;
    }*/

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

        if (imageView.getVisibility() == View.VISIBLE) {
            //sendMessage(MessageType.photoMessage.ordinal(), recipientUID);
            PrepareMessageToSend(MessageType.photoMessage.ordinal(), recipientUID);
        } else if (imageView.getVisibility() == View.GONE) {
            PrepareMessageToSend(MessageType.textMessage.ordinal(), recipientUID);
            //sendMessage(MessageType.textMessage.ordinal(), recipientUID);
        }
        linkedMessagePreview.setVisibility(View.GONE);
        link = null;
    }

    private void ResetToText() {
        messageSent.setVisibility(View.VISIBLE);
        messageSent.setText("");//clears the input field
        imageView.setVisibility(View.GONE);
        messageSent.requestFocus();
        recordingTimeText.setVisibility(View.GONE);
        voiceLayout.setVisibility(View.GONE);
        SetCorrectColor(ButtonType.microphone);
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
        SetCorrectColor(ButtonType.sendMessage);
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
        CreateFileUri(fileName);
        String[] names = {recipient.getName()};
        CreateMessage("recording", MessageType.VoiceMessage.ordinal(), names, recipient.getUserUID());
        //sendMessage(MessageType.VoiceMessage.ordinal(), recipientUID);
    }

    private void CreateFileUri(String filePath) {
        File file = new File(filePath);
        fileUri = Uri.fromFile(file);
    }

    private void RecordOrNot() {

        onRecord(startRecording);
        if (startRecording) {
            SetCorrectColor(ButtonType.recording);
        } else {
            SetCorrectColor(ButtonType.microphone);
        }
    }

    private void PlayOrNot() {
        onPlay(startPlaying1);
        if (startPlaying1) {
            SetCorrectColor(ButtonType.pause);
        } else {
            SetCorrectColor(ButtonType.play);
        }
    }

    private void onRecord(boolean start) {
        if (start)
            startRecording();
        else
            stopRecording();
    }

    private void stopRecording() {
        if (recorder != null) {
            try {
                recorder.stop();
                recorder.release();
                recorder = null;
            } catch (RuntimeException ex) {
                Log.e("RECORDER ERROR", "failed to stop");
            }
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
            //playingON = true;
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
            Drawable drawable;
            if (selectedMessage.getSender().equals(currentUser)) {
                drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.outgoing_message_look, getTheme());
            } else {
                drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.incoming_message_look, getTheme());
            }
            selectedMessageView.setBackground(drawable);

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
                                    MarkMessage(view1, true);
                                }
                                recyclerView.removeOnScrollListener(this);
                            }
                        }
                    });
                    //recyclerView.scrollToPosition(index);
                } else
                    Toast.makeText(this, "couldn't fine this quoted message", Toast.LENGTH_SHORT).show();
            }
        } else if (message.getMessage() != null && Patterns.WEB_URL.matcher(message.getMessage()).matches()) {
            Intent openWebSite = new Intent(Intent.ACTION_VIEW);
            openWebSite.setData(Uri.parse(message.getMessage()));
            startActivity(openWebSite);
        }
    }

    private void MarkMessage(View view, boolean scroll) {
        if (view != null) {
            view.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.border, getTheme()));
            if (scroll) {
                Handler handler = new Handler(getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.setBackground(null);
                    }
                }, 2500);
            }
        }
    }

    @Override
    public void onMessageLongClick(Message message, View view, int viewType) {
        //opens fragment that shows information about the specific message
        messageLongPress = !messageLongPress;
        invalidateOptionsMenu();
        selectedMessage = message;
        selectedMessageView = view;
        MarkMessage(view, false);

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
            imageSwitcher.setImageResource(R.drawable.ic_baseline_edit_24);
        } else {
            editMode = false;
            messageSent.setText("");
            SetCorrectColor(ButtonType.sendMessage);
        }
    }


    //saves image to local storage
    @Override
    public String onImageDownloaded(Bitmap bitmap, Message message) {

        String path = null;
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
                    path = imageUri.getPath();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            File imageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File imageFile = new File(imageDirectory, fileName);
            try {
                out = new FileOutputStream(imageFile);
                message.setImagePath(imageFile.getAbsolutePath());
                UpdateDataBase(message);
                path = imageFile.getAbsolutePath();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out))
                Log.i("good", "Bitmap successfully written");
            else
                Log.e(ERROR_WRITE, "Bitmap save have failed");
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return path;
    }

    @Override
    public String onVideoDownloaded(File file, Message message) {
        String path = null;
        String fileName = file.getName();
        OutputStream out = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = ConversationActivity.this.getApplicationContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/*");
            values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES);
            Uri videoUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            try {
                if (videoUri != null) {
                    out = resolver.openOutputStream(videoUri);
                    path = videoUri.getPath();
                    message.setRecordingPath(path);
                    UpdateDataBase(message);
                    File imageDirectory = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
                    File videoFile = new File(imageDirectory, fileName);
                    try {
                        out = new FileOutputStream(videoFile);
                        message.setRecordingPath(videoFile.getAbsolutePath());
                        UpdateDataBase(message);
                        path = videoFile.getAbsolutePath();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            File imageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
            File videoFile = new File(imageDirectory, fileName);
            try {
                out = new FileOutputStream(videoFile);
                message.setRecordingPath(videoFile.getAbsolutePath());
                UpdateDataBase(message);
                path = videoFile.getAbsolutePath();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (out != null) {
            int size = (int) file.length();
            byte[] bytes = new byte[size];
            try {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
                int bytesLength = bufferedInputStream.read(bytes, 0, bytes.length);
                if (bytesLength != size)
                    Log.e(ERROR_WRITE, "writing file wasn't complete");
                bufferedInputStream.close();
                out.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return path;
    }

    @Override
    public void onVideoClicked(Uri uri) {
        VideoFragment videoFragment = VideoFragment.getInstance(uri, false);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.addToBackStack(null);
        if (manager.findFragmentByTag(VIDEO_FRAGMENT_TAG) == null) {
            transaction.add(R.id.contentContainer, videoFragment, VIDEO_FRAGMENT_TAG);
        } else {
            transaction.replace(R.id.contentContainer, videoFragment, VIDEO_FRAGMENT_TAG);
        }
        transaction.commit();
    }

   /* @Override
    public void onUpdateMessageStatus(Message message) {
        HashMap<String, Object> statusMap = new HashMap<>();
        statusMap.put("messageStatus", MESSAGE_SEEN);
        String recipientConversationID = RecipientConversationID(conversationID);
        UpdateMessageStatus("users/" + recipientUID + "/conversations/" + recipientConversationID + "/conversationInfo/conversationMessages/" + message.getMessageID(), statusMap);
    }*/

    @Override
    public void onDeleteMessageClick(Message message) {
        PrepareDeletedMessage(message);
        /*controller.onRemoveData("users/" + currentUser + "/conversations/" + message.getConversationID() + "/conversationInfo/conversationMessages" + message.getMessageID());
        String recipientConversationID = RecipientConversationID(conversationID);
        controller.onRemoveData("users/" + recipientUID + "/conversations/" + recipientConversationID + "/conversationInfo/conversationMessages" + message.getMessageID());
        Toast.makeText(this, "message was deleted", Toast.LENGTH_SHORT).show();*/
    }


    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences conversationPreferences = getSharedPreferences("Conversation", MODE_PRIVATE);
        SharedPreferences.Editor editor = conversationPreferences.edit();
        editor.putString("liveConversation", conversationID);
        editor.apply();

        // LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("disableNotifications").putExtra("ConversationID", conversationID));

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

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String recipientConversationID = RecipientConversationID(conversationID);
        if (recipient != null && recipient.getUserUID() != null)
            controller.onUpdateInteraction("users/" + recipient.getUserUID() + "/conversations/" + recipientConversationID + "/conversationInfo/typing/", false);
        controller.onRemoveChildEvent();
        // Server.removeMessagesChildEvent();
        SharedPreferences conversationPreferences = getSharedPreferences("Conversation", MODE_PRIVATE);
        SharedPreferences.Editor editor = conversationPreferences.edit();
        editor.putString("liveConversation", "noConversation");
        editor.apply();
        controller.removeInterface(1);
        controller.setConversationGUI(null);
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager != null && network2 != null)
            connectivityManager.unregisterNetworkCallback(network2);
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
                    break;
            }
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
                    String name;
                    if (ConversationActivity.this.recipient == null)
                        name = "abc";
                    else
                        name = ConversationActivity.this.recipient.getName();
                    String[] recipientsNames = {name};
                    CreateMessage(messageToSend, MessageType.gpsMessage.ordinal(), recipientsNames, recipient.getUserUID());
                    //sendMessage(MessageType.gpsMessage.ordinal(), recipientUID);
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

    private void PrepareMessageToSend(int messageType, String recipient) {
        String name;
        if (this.recipient == null)
            name = "abc";
        else
            name = this.recipient.getName();
        String[] recipientsNames = {name};
        CreateMessage(messageToSend, messageType, recipientsNames, recipient);
        ResetToText();
    }

    /*
    the send message function updates the server with the message sent and uses Retrofit to access the fcm server to send messages
     */
    /*@Deprecated
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
                    message.setMessage("Voice Message");
                    controller.onSecondPath("users/" + recipient + "/conversations/" + recipientConversationID + "/conversationInfo/conversationMessages/" + time);
                    controller.onUploadFile("users/" + currentUser + "/conversations/" + conversationID + "/conversationInfo/conversationMessages/" + time, fileUri.toString(), ConversationActivity.this);
                    break;
                }
                case fileMessage:
                    break;
                case videoMessage: {
                    message.setMessage(messageToSend);
                    controller.onSecondPath("users/" + recipient + "/conversations/" + recipientConversationID + "/conversationInfo/conversationMessages/" + time);
                    controller.onUploadFile("users/" + currentUser + "/conversations/" + conversationID + "/conversationInfo/conversationMessages/" + time, videoUri.toString(), ConversationActivity.this);
                    break;
                }
                default:
                    Log.e(ERROR_CASE, "error in switch case: couldn't find the correct message type");
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

            //sets message to display so it wont be needed to download it inorder to display it
            if (message.getMessageType() == MessageType.textMessage.ordinal())
                AddMessageToDisplay(message);


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
            conversationInfo.put("conversationID", recipientConversationID);
            controller.onUpdateData("users/" + recipient + "/conversations/" + recipientConversationID + "/conversationInfo", conversationInfo);

            controller.onUpdateData("users/" + currentUser + "/conversations/" + conversationID + "/conversationInfo/conversationMessages", messageMap);
            message.setConversationID(recipientConversationID);
            messageMap.put(time, message);
            controller.onUpdateData("users/" + recipient + "/conversations/" + recipientConversationID + "/conversationInfo/conversationMessages", messageMap);


            messageSent.setText("");//clears the input field
            imageView.setVisibility(View.GONE);
            //this is part 2 of the function - here the function uses the fcm server api with retrofit to send a message to the recipient

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
        } else {
            Toast.makeText(this, "no connection is available, connect to the internet in order to send messages", Toast.LENGTH_SHORT).show();
        }
    }
*/
    //adds the message to the chat
    private void AddMessageToDisplay(Message message) {
        chatAdapter.addNewMessage(message);
        recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
        InsertToDataBase(message);
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

    /*private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (connectivityManager != null) {
                return connectivityManager.getActiveNetwork() != null && connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork()) != null;
            }
        }
        return false;
    }*/

    private void ConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI).addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_VPN);
        NetworkRequest request = builder.build();
        network2 = Network2.getInstance();
        //Network network = new Network();
        network2.setListener(new NetworkChange() {
            @Override
            public void onNetwork() {
                if (!networkConnection) {
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
                networkConnection = true;
                //Toast.makeText(ConversationActivity.this, "network changed", Toast.LENGTH_SHORT).show();
            }
        });
        //ConnectivityManager.NetworkCallback networkCallback = network;
        if (connectivityManager != null)
            connectivityManager.registerNetworkCallback(request, network2);

    }

    @Deprecated
    private String RecipientConversationID(String conversationID) {
        /*String[] conversationIDSplit = conversationID.split(" {3}");
        String recipientConversationID;
        if (currentUser.equals(conversationIDSplit[0])) {
            recipientConversationID = conversationIDSplit[1] + "   " + conversationIDSplit[0];
        } else {
            recipientConversationID = conversationIDSplit[0] + "   " + conversationIDSplit[1];
        }
        return recipientConversationID;*/
        return conversationID;
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

    @Override
    public void onBottomSheetClick(ButtonType buttonType) {
        actionState = buttonType.ordinal();
        SetCorrectColor(buttonType);
        DialogFragment fragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(BOTTOM_SHEET_TAG);
        if (fragment != null)
            fragment.dismiss();
        switch (buttonType) {
            case attachFile: {
                onFileAction();
                break;
            }
            case camera: {
                onCameraAction();
                break;
            }
            case gallery: {
                onGalleryAction();
                break;
            }
            case location: {
                onLocationAction();
                break;
            }
            case delay: {
                onDelayAction();
                break;
            }
            case video: {
                onVideoAction();
                break;
            }
            case contact: {
                onContactAction();
                break;
            }
            case document: {
                onDocumentAction();
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
                    SetCorrectColor(ButtonType.sendMessage);
                sendActionBtn.setVisibility(View.GONE);
                buttonState = SEND_MESSAGE;
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
        } else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            if (data != null) {

                Uri videoUri = data.getData();
                if (videoUri != null) {
                    //File videoFile = new File(videoUri.toString());
                    VideoFragment videoFragment = VideoFragment.getInstance(videoUri, true);
                    videoFragment.setListener(new VideoFragment.onVideo() {
                        @Override
                        public void onSendVideo() {
                            ConversationActivity.this.videoUri = videoUri;
                            String[] name = {recipient.getName()};
                            CreateMessage(messageToSend, MessageType.videoMessage.ordinal(), name, recipient.getName());
                            //sendMessage(MessageType.videoMessage.ordinal(), recipientUID);
                            //ConversationActivity.this.videoUri = null;
                            FragmentManager manager = getSupportFragmentManager();
                            Fragment fragment = manager.findFragmentByTag(VIDEO_FRAGMENT_TAG);
                            FragmentTransaction transaction = manager.beginTransaction();
                            if (fragment != null)
                                transaction.remove(fragment).commit();
                        }
                    });
                    FragmentManager manager = getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.add(R.id.contentContainer, videoFragment, VIDEO_FRAGMENT_TAG).addToBackStack(null).commit();
                    SetCorrectColor(ButtonType.sendMessage);
                }
            }

        } else if (requestCode == SEND_FILE && resultCode == RESULT_OK) {
            //gets a file to send to the recipient
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null && uri.getPath() != null) {
                    File file = new File(uri.getPath());
                }
            }
        } else if (requestCode == SEND_CONTACT && resultCode == RESULT_OK) {
            //needs check
            //gets a contact to send to the recipient
            if (data != null) {
                Uri contactUri = data.getData();
                String[] projections = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
                if (contactUri != null) {
                    Cursor cursor = getContentResolver().query(contactUri, projections, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        String number = cursor.getString(numberIndex);
                        String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        //need to add contact support to message and display the contact - to do later
                        cursor.close();

                    }
                }
            }
        } else if (requestCode == DOCUMENT_REQUEST && resultCode == RESULT_OK) {
            //needs check
            Uri docUri;
            if (data != null) {
                docUri = data.getData();
                if (docUri != null && docUri.getPath() != null) {
                    File file = new File(docUri.getPath());
                    if (file.exists()) {
                        try {
                            ParcelFileDescriptor descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
                            PdfRenderer renderer = new PdfRenderer(descriptor);
                            PdfRenderer.Page page = renderer.openPage(0);
                            Bitmap docBitmap = BitmapFactory.decodeFileDescriptor(descriptor.getFileDescriptor());
                            page.render(docBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                            imageView.setVisibility(View.VISIBLE);
                            imageView.setImageBitmap(docBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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
        if (item.getItemId() == R.id.callBtn) {
            //opening dialer to call the recipient number if exists
            if (recipientPhoneNumber != null)
                if (directCall)
                    CallPhone(AskPermission(MessageType.callPhone));
                else
                    CallPhone(false);
        } else if (item.getItemId() == R.id.addAsContact) {
            //adds current recipient as a contact to contacts list
            Intent addContactIntent = new Intent(Intent.ACTION_INSERT);
            addContactIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);
            addContactIntent.putExtra(ContactsContract.Intents.Insert.NAME, recipient.getName() + " " + recipient.getLastName());//first name ---- space ---- lastName
            addContactIntent.putExtra(ContactsContract.Intents.Insert.PHONE, recipient.getPhoneNumber());
            //addContactIntent.putExtra(ContactsContract.Intents.Insert.EMAIL, "example@wxample.com");
            if (addContactIntent.resolveActivity(getPackageManager()) != null)
                startActivity(addContactIntent);
        } else if (item.getItemId() == R.id.addPhoneNumber) {
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
        } else if (item.getItemId() == R.id.meetUp) {
            //sets a calendar appointment for both participants (after asking permission from the recipient) - not implemented yet
            GeneralFragment fragment = new GeneralFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.contentContainer, fragment, "MeetUp");
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
        } else if (item.getItemId() == R.id.block) {
            //blocks current recipient from sending messages to current user

            ArrayList<String> blocked = user.getBlockedUsers();
            if (blocked.size() == 0)
                controller.onUpdateData("users/" + currentUser + "/blocked/" + recipientUID, recipientUID);
            else {
                for (String block : blocked) {
                    if (block.equals(recipientUID)) {
                        controller.onRemoveData("users/" + currentUser + "/blocked/" + recipientUID);
                        Toast.makeText(this, "user: " + recipientName + " " + recipientLastName + " is un blocked!", Toast.LENGTH_SHORT).show();
                    } else {
                        controller.onUpdateData("users/" + currentUser + "/blocked/" + recipientUID, recipientUID);
                        Toast.makeText(this, "user: " + recipientName + " " + recipientLastName + " is blocked!", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        } else if (item.getItemId() == R.id.share) {
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
        } else if (item.getItemId() == R.id.messageInfo) {
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
        } else if (item.getItemId() == R.id.searchMessage) {
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
        } else if (item.getItemId() == R.id.copy) {
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
        } else if (item.getItemId() == R.id.starMessage) {
            controller.onUpdateData("users/" + currentUser + "/conversations/" + selectedMessage.getConversationID() + "/conversationInfo/conversationMessages/" + selectedMessage.getMessageID() + "/star", true);
            controller.onUpdateData("users/" + currentUser + "/conversations/" + selectedMessage.getConversationID() + "/conversationInfo/conversationMessages/" + selectedMessage.getMessageID() + "/starTime", System.currentTimeMillis() + "");
            selectedMessage = null;
            messageLongPress = false;
            invalidateOptionsMenu();
        } else
            Log.e(ERROR_CASE, "menu error");
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
            chatAdapter.setMessages(messages);
            chatAdapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(messages.size() - 1);
            //sendMessageButton.setText(getResources().getString(R.string.record));
            SetCorrectColor(ButtonType.microphone);
            buttonState = RECORD_VOICE;
            HashMap<String, Object> statusMap = new HashMap<>();
            statusMap.put("messageStatus", MESSAGE_SEEN);
            String recipientConversationID = RecipientConversationID(messages.get(messages.size() - 1).getConversationID());
            UpdateMessageStatus("users/" + recipientUID + "/conversations/" + recipientConversationID + "/conversationInfo/conversationMessages/" + messages.get(messages.size() - 1).getMessageID(), statusMap);

        }
    }

    @Override
    public void onReceiveSingleMessage(Message message) {
        if (message != null) {
            if (buttonState != RECORD_VOICE) {
                if (link == null) {
                    SetCorrectColor(ButtonType.microphone);
                    buttonState = RECORD_VOICE;
                } else {
                    ShowMessageForUserConfirmation();
                    SetCorrectColor(ButtonType.sendMessage);
                }
            }
            //no need to update server database message status after the message is seen
            if (!message.getMessageStatus().equals(MESSAGE_SEEN) && !message.getSender().equals(currentUser)) {
                if (iReadThat) {
                    HashMap<String, Object> statusMap = new HashMap<>();
                    statusMap.put("messageStatus", MESSAGE_SEEN);
                    String recipientConversationID = RecipientConversationID(message.getConversationID());
                    UpdateMessageStatus("users/" + recipientUID + "/conversations/" + recipientConversationID + "/conversationInfo/conversationMessages/" + message.getMessageID(), statusMap);

                }
            }

            if (message.getReadAt() == -1 && !message.getSender().equals(currentUser))//-1 is the default value
            {
                HashMap<String, Object> readAt = new HashMap<>();
                readAt.put("readAt", System.currentTimeMillis());
                message.setReadAt(System.currentTimeMillis());
                UpdateMessageStatus("users/" + currentUser + "/conversations/" + conversationID + "/conversationInfo/conversationMessages/" + message.getMessageID(), readAt);

            }

            // if (!message.getSender().equals(currentUser))
            if (!CheckIfExistsInDataBase(message)) {
                AddMessageToDisplay(message);
            }
            //InsertToDataBase(message);
            messageSent.requestFocus();
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
            UpdateDataBase(message);
            chatAdapter.changeExistingMessage(message);
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
            RetrieveRecipientsTokens(recipient.getUserUID());
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
            /*if (!calledMessages)
                controller.onDownloadMessages(ConversationActivity.this, conversationID, 20);*/
            calledMessages = true;
        }
    }

    private void isRecipientTyping() {
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
                    SetCorrectColor(ButtonType.play);
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
        }
    }

    private void InsertToDataBase(Message message) {
        if (!CheckIfExistsInDataBase(message)) {
            ContentValues values = new ContentValues();
            values.put(DataBaseContract.Messages.MESSAGE_ID, message.getMessageID());
            values.put(DataBaseContract.Conversations.CONVERSATIONS_ID_COLUMN_NAME, conversationID);
            values.put(DataBaseContract.Messages.MESSAGE_CONTENT_COLUMN_NAME, message.getMessage());
            values.put(DataBaseContract.Messages.MESSAGE_RECIPIENT_COLUMN_NAME, message.getRecipient());
            values.put(DataBaseContract.Messages.MESSAGE_SENDER_COLUMN_NAME, message.getSender());
            values.put(DataBaseContract.Messages.MESSAGE_TIME_DELIVERED_COLUMN_NAME, message.getReadAt());
            values.put(DataBaseContract.Messages.MESSAGE_TIME_SENT_COLUMN_NAME, message.getMessageTime());
            values.put(DataBaseContract.Messages.MESSAGE_TYPE_COLUMN_NAME, message.getMessageType());
            values.put(DataBaseContract.Messages.MESSAGE_STATUS_COLUMN_NAME, message.getMessageStatus());
            values.put(DataBaseContract.Messages.MESSAGE_IMAGE_PATH, message.getImagePath());
            values.put(DataBaseContract.Messages.MESSAGE_LONGITUDE, message.getLongitude());
            values.put(DataBaseContract.Messages.MESSAGE_LATITUDE, message.getLatitude());
            values.put(DataBaseContract.Messages.MESSAGE_ADDRESS, message.getLocationAddress());
            values.put(DataBaseContract.Messages.MESSAGE_RECORDING_PATH, message.getRecordingPath());
            if (message.getMessageType() == MessageType.webMessage.ordinal())
                values.put(DataBaseContract.Messages.MESSAGE_LINK, message.getMessage());
            long newRowId = db.insert(DataBaseContract.Messages.MESSAGES_TABLE, null, values);
            if (newRowId == -1)
                Log.e(DATABASE_ERROR, "inserted more than 1 row");
        }
    }

    private void UpdateDataBase(Message message) {
        PrintDataBase();
        ContentValues values = new ContentValues();
        values.put(DataBaseContract.Messages.MESSAGE_ID, message.getMessageID());
        values.put(DataBaseContract.Conversations.CONVERSATIONS_ID_COLUMN_NAME, conversationID);
        values.put(DataBaseContract.Messages.MESSAGE_CONTENT_COLUMN_NAME, message.getMessage());
        values.put(DataBaseContract.Messages.MESSAGE_RECIPIENT_COLUMN_NAME, message.getRecipient());
        values.put(DataBaseContract.Messages.MESSAGE_SENDER_COLUMN_NAME, message.getSender());
        values.put(DataBaseContract.Messages.MESSAGE_TIME_DELIVERED_COLUMN_NAME, message.getReadAt());
        values.put(DataBaseContract.Messages.MESSAGE_TIME_SENT_COLUMN_NAME, message.getMessageTime());
        values.put(DataBaseContract.Messages.MESSAGE_TYPE_COLUMN_NAME, message.getMessageType());
        values.put(DataBaseContract.Messages.MESSAGE_STATUS_COLUMN_NAME, message.getMessageStatus());
        values.put(DataBaseContract.Messages.MESSAGE_IMAGE_PATH, message.getImagePath());
        values.put(DataBaseContract.Messages.MESSAGE_LONGITUDE, message.getLongitude());
        values.put(DataBaseContract.Messages.MESSAGE_LATITUDE, message.getLatitude());
        values.put(DataBaseContract.Messages.MESSAGE_ADDRESS, message.getLocationAddress());
        values.put(DataBaseContract.Messages.MESSAGE_RECORDING_PATH, message.getRecordingPath());
        values.put(DataBaseContract.Messages.MESSAGE_STAR, message.isStar());
        if (message.getMessageType() == MessageType.webMessage.ordinal())
            values.put(DataBaseContract.Messages.MESSAGE_LINK, message.getMessage());
        String selection = DataBaseContract.Messages.MESSAGE_ID + " LIKE ?";
        String[] selectionArgs = {message.getMessageID()};
        int count = db.update(DataBaseContract.Messages.MESSAGES_TABLE, values, selection, selectionArgs);
        PrintDataBase();
        if (count <= 0)
            Log.e(DATABASE_ERROR, "didn't update a thing");
    }

    /*private void DeleteFromDataBase(Message message) {
        String selection = DataBaseContract.Messages.MESSAGE_ID + " LIKE ?";
        String[] selectionArgs = {message.getMessageID()};
        int deletedRows = db.delete(DataBaseContract.Conversations.CONVERSATIONS_TABLE, selection, selectionArgs);
        if (deletedRows == -1)
            Log.e(DATABASE_ERROR, "didn't delete anything - deleted rows = -1");

    }*/

    public void PrintDataBase() {
        if (db != null) {
            String[] projection = {
                    BaseColumns._ID,
                    DataBaseContract.Messages.MESSAGE_ID,
                    DataBaseContract.Conversations.CONVERSATIONS_ID_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_CONTENT_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_RECIPIENT_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_SENDER_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_TIME_DELIVERED_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_TIME_SENT_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_TYPE_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_STATUS_COLUMN_NAME
            };
            String selection = DataBaseContract.Conversations.CONVERSATIONS_ID_COLUMN_NAME + " LIKE ?";
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
            System.out.println(MessagesRecipient);
            System.out.println(MessagesSender);
            System.out.println(MessageTimeDelivered);
            System.out.println(MessagesTimeSent);
            System.out.println(MessagesTypes);
            System.out.println(MessagesStatus);
        }
    }

    //needs major check
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean CheckIfExistsInDataBase(Message message) {
        if (db != null) {
            String[] projections = {
                    BaseColumns._ID,
                    DataBaseContract.Messages.MESSAGE_ID
            };
            // String query = "SELECT " + DataBaseContract.Messages.MESSAGE_ID + " FROM " + DataBaseContract.Messages.MESSAGES_TABLE + " ORDER BY " + DataBaseContract.Messages.MESSAGE_ID + " DESC LIMIT 1";
            //in order to not scan the database each time from the start, we should start scanning from the last message received - time. since messages
            //come in a linear order, a message that was sent now will never arrive prior to the message that was sent before it
            // String selection = DataBaseContract.Messages.MESSAGE_TIME_DELIVERED_COLUMN_NAME + " = ?";
            String selection = DataBaseContract.Conversations.CONVERSATIONS_ID_COLUMN_NAME + " = ?";
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
                    DataBaseContract.Conversations.CONVERSATIONS_ID_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_CONTENT_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_SENDER_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_RECIPIENT_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_TIME_DELIVERED_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_TIME_SENT_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_TYPE_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_STATUS_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_IMAGE_PATH,
                    DataBaseContract.Messages.MESSAGE_LONGITUDE,
                    DataBaseContract.Messages.MESSAGE_LATITUDE,
                    DataBaseContract.Messages.MESSAGE_ADDRESS,
                    DataBaseContract.Messages.MESSAGE_RECORDING_PATH,
                    DataBaseContract.Messages.MESSAGE_STAR
            };
            String selection = DataBaseContract.Conversations.CONVERSATIONS_ID_COLUMN_NAME + " = ?";
            String[] selectionArgs = {conversationID};
            Cursor cursor = db.query(DataBaseContract.Messages.MESSAGES_TABLE, projections, selection, selectionArgs, null, null, null);
            while (cursor.moveToNext()) {

                String messageID = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_ID));
                String conversationID = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.CONVERSATIONS_ID_COLUMN_NAME));
                String messageContent = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_CONTENT_COLUMN_NAME));
                String messageSender = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_SENDER_COLUMN_NAME));
                String recipient = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_RECIPIENT_COLUMN_NAME));
                long messageTimeDelivered = cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_TIME_DELIVERED_COLUMN_NAME));
                String messageTimeSent = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_TIME_SENT_COLUMN_NAME));
                int messageType = cursor.getInt(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_TYPE_COLUMN_NAME));
                String messageStatus = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_STATUS_COLUMN_NAME));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_IMAGE_PATH));
                String longitude = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_LONGITUDE));
                String latitude = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_LATITUDE));
                String address = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_ADDRESS));
                String recordingPath = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_RECORDING_PATH));
                String star = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_STAR));
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
                message.setImagePath(imagePath);
                message.setLongitude(longitude);
                message.setLatitude(latitude);
                message.setLocationAddress(address);
                message.setRecordingPath(recordingPath);
                if (star != null)
                    message.setStar(star.equals("1"));
                AddMessageToDisplay(message);
            }
            cursor.close();
        }
    }

    @Override
    public void onLetsMeet(String start, String end) {
        HashMap<String, Object> meetMap = new HashMap<>();
        meetMap.put("with", recipientName);
        meetMap.put("withID", recipientUID);
        meetMap.put("start", start);
        meetMap.put("end", end);
        controller.onUpdateData("users/" + currentUser + "/meetUp/" + System.currentTimeMillis(), meetMap);
        meetMap.put("with", user.getName());
        meetMap.put("withID", currentUser);
        controller.onUpdateData("users/" + recipientUID + "/meetUp/" + System.currentTimeMillis(), meetMap);
    }

    @Override
    public void onCancel() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag("MeetUp");
        if (fragment != null)
            fragmentManager.beginTransaction().remove(fragment).commit();
    }

    private void RecordVideo() {
        File videoFile = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        String videoFileName = "video_" + System.currentTimeMillis();
        try {
            File video = File.createTempFile(videoFileName, ".mp4", videoFile);
            Uri videoURI = FileProvider.getUriForFile(ConversationActivity.this,
                    "com.example.woofmeow.provider", video);
            Intent recordVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            recordVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoURI);
            if (recordVideoIntent.resolveActivity(getPackageManager()) != null)
                startActivityForResult(recordVideoIntent, REQUEST_VIDEO_CAPTURE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//------------------------------------------------------------------------------------------------------------------------------------------------------------------------//

    private void init(String conversationID) {
        DataBaseSetUp();
        LoadCurrentUserID();
        LoadCurrentUserFromDataBase();
        LoadMessages(conversationID);
        ReceiveMessages(conversationID);
        RequestRecipientsStatus();
    }

    private void LoadCurrentUserID() {
        String currentUser;
        SharedPreferences sharedPreferences = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
        currentUser = sharedPreferences.getString("currentUser", "no user");
        if (!currentUser.equals("no user"))
            this.currentUser1 = currentUser;
    }

    private void LoadCurrentUserFromDataBase() {
        if (db != null) {
            String[] projections = {
                    DataBaseContract.User._ID,
                    DataBaseContract.User.USER_UID,
                    DataBaseContract.User.USER_NAME,
                    DataBaseContract.User.USER_LAST_NAME,
                    DataBaseContract.User.USER_PICTURE_LINK,
                    DataBaseContract.User.USER_TIME_CREATED,
                    DataBaseContract.User.USER_PHONE_NUMBER,
                    DataBaseContract.User.USER_LAST_STATUS
            };
            String selection = DataBaseContract.User.USER_UID + " LIKE ?";
            String[] selectionArgs = {currentUser1};
            Cursor cursor = db.query(DataBaseContract.User.USER_TABLE, projections, selection, selectionArgs, null, null, null);
            cursor.moveToNext();
            if (cursor.getCount() == 1) {
                User user = new User();
                String uid = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_UID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_NAME));
                String lastName = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_LAST_NAME));
                String pictureLink = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_PICTURE_LINK));
                String timeCreated = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_TIME_CREATED));
                String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_PHONE_NUMBER));
                String status = cursor.getColumnName(cursor.getColumnIndexOrThrow(DataBaseContract.User.USER_LAST_STATUS));
                user.setUserUID(uid);
                user.setName(name);
                user.setLastName(lastName);
                user.setPictureLink(pictureLink);
                user.setTimeCreated(timeCreated);
                user.setPhoneNumber(phoneNumber);
                user.setStatus(status);
                this.user = user;
            } else if (cursor.getCount() > 1)
                Log.e(DATABASE_ERROR, "cursor contains more than 1 user");
            else
                Log.e(DATABASE_ERROR, "no user in database ");
            cursor.close();
        } else
            Log.e(DATABASE_ERROR, "db is null");
    }

    /**
     * creates the messages to send
     *
     * @param content         - the message content - the text
     * @param messageType     - the type of the message that is going to be sent - for example: text,picture,video,audio,etc...
     * @param recipientsNames - an array with the names of the recipients, must be at least size of 1
     * @param recipients      - the recipients UID. must much in size to recipients names array
     */

    private void CreateMessage(String content, int messageType, String[] recipientsNames, String... recipients) {
        String currentTime = System.currentTimeMillis() + "";
        TimeZone timeZone = TimeZone.getTimeZone("GMT-4");
        Calendar calendar = Calendar.getInstance(timeZone);
        String time = calendar.getTimeInMillis() + "";
        Message message = new Message();
        message.setMessage(content);
        message.setSendingTime(currentTime);
        message.setConversationID(conversationID);
        if (recipients.length == 1) {
            message.setRecipient(recipients[0]);
            message.setRecipientName(recipientsNames[0]);
        }
        String name;
        if (user == null)
            name = "def";
        else
            name = user.getName();
        message.setSenderName(name);
        message.setSender(currentUser);
        message.setMessageStatus(MESSAGE_SENT);
        message.setMessageType(messageType);
        Server3 server3 = Server3.getInstance();
        server3.SetListener(new Uploads.onResult() {
            @Override
            public void onPathReady(String path) {
                message.setFilePath(path);
                SendMessage(message);
            }

            @Override
            public void onStartedUpload() {
                Toast.makeText(ConversationActivity.this, "starting sending file", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(int progress) {
                Log.e("PROGRESS", progress + "");
            }

            @Override
            public void onError(String errorDescription) {
                Toast.makeText(ConversationActivity.this, "an error accrued while sending the file, try again later", Toast.LENGTH_SHORT).show();
            }
        });
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
        message.setMessageID(time);
        MessageType type = MessageType.values()[messageType];
        switch (type) {
            case textMessage:
                if (Patterns.WEB_URL.matcher(messageToSend).matches())
                    message.setMessageType(MessageType.webMessage.ordinal());
                break;
            case gpsMessage:
                message.setLatitude(latitude);
                message.setLongitude(longitude);
                message.setLocationAddress(gpsAddress);
                message.setMessage("my location: " + gpsAddress);
                break;
            case photoMessage:
                if (camera)//photo from camera
                {
                    server3.uploadImage(photoPath);
                } else//photo from gallery
                {
                    server3.uploadImageBitmap(imageBitmap);
                }
                break;
            case VoiceMessage:
                message.setMessage("Voice Message");
                server3.uploadFile(fileUri.toString());
                break;
            case videoMessage:
                server3.uploadFile(videoUri.toString());
                break;
        }
        if (type == MessageType.textMessage || type == MessageType.gpsMessage || type == MessageType.webMessage)
            SendMessage(message);
    }

    /**
     * sends the message to the correct recipients. recipientsTokens must be greater than 0 or the message won't be sent.
     *
     * @param message - the message to send
     */

    private void SendMessage(@NonNull Message message) {
        String token = getMyToken();
        message.setSenderToken(token);
        MessageSender messageSender = MessageSender.getInstance();
        String[] recipientsToken = recipientsTokens.toArray(new String[0]);
        messageSender.SendMessage(message, recipientsToken);
        ShowMessageOnScreen(message, message.getMessageAction());
    }

    //call this function when activity starts

    /**
     * Call this function when the activity starts to load all the messages in the database for this conversation
     *
     * @param conversationID - the current conversationID for the currently open conversation.
     */
    private void LoadMessages(String conversationID) {
        LoadMessage(conversationID, DataBaseContract.Conversations.CONVERSATIONS_ID_COLUMN_NAME + " = ?", MessageAction.activity_start);
    }

    //gets new messages from fcm service

    /**
     * sets a broadcast receiver for the current conversation. each message received in fcm has conversationID.
     * this broadcast receiver should be unregistered when the activity closes in onDestroy
     *
     * @param conversationID - the current conversationID for the currently open conversation
     */
    private void ReceiveMessages(String conversationID) {
        receiveNewMessages = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra("read"))
                {
                    ReadMessage readMessage = (ReadMessage) intent.getSerializableExtra("read");
                    if (readMessage!=null) {
                        chatAdapter.UpdateMessageStatus(readMessage);
                        MarkAsRead(readMessage.getMessageID());
                    }
                }
                else if (intent.hasExtra("typing") || intent.hasExtra("recording"))
                {
                    boolean typing = intent.getBooleanExtra("typing",false);
                    String recording = intent.getStringExtra("recording");
                    if (typing)
                        textSwitcherTyping.setText("typing");
                    else if (recording!=null)
                        textSwitcherTyping.setText("recording");
                    else
                        textSwitcherTyping.setText("");
                }
                else {
                    Message message = (Message) intent.getSerializableExtra("message");
                    if (message != null) {
                        MessageAction action = message.getMessageAction();
                        if (action == MessageAction.new_message) {
                            ShowMessageOnScreen(message, message.getMessageAction());
                            // LoadNewMessage(messageID);
                        } else if (action == MessageAction.edit_message) {
                            ShowMessageOnScreen(message, message.getMessageAction());
                            UpdateMessage(message);
                        } else if (action == MessageAction.delete_message) {
                            ShowMessageOnScreen(message, message.getMessageAction());
                            DeleteMessage(message.getMessageID());
                        }
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(receiveNewMessages, new IntentFilter(conversationID));
    }

    //call this function when a new message arrives while being at the activity
    private void LoadNewMessage(@NonNull String messageID) {
        LoadMessage(messageID, DataBaseContract.Messages.MESSAGE_ID + " = ?", MessageAction.new_message);
    }

    /**
     * Loads a single message that arrives when the activity is active or loads the entire conversation when the activity is being lunched
     *
     * @param id        the id of the message to display on the screen or the id of the conversation to display
     * @param selection the identifier to decide what to load - a single message or the full conversation
     */
    private void LoadMessage(@NonNull final String id, @NonNull final String selection, MessageAction messageAction) {
        if (db != null) {
            String[] projections = {
                    DataBaseContract.Messages.MESSAGE_ID,
                    DataBaseContract.Conversations.CONVERSATIONS_ID_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_CONTENT_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_SENDER_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_RECIPIENT_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_TIME_DELIVERED_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_TIME_SENT_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_STATUS_COLUMN_NAME,
                    DataBaseContract.Messages.MESSAGE_SENDER_NAME,
                    DataBaseContract.Messages.MESSAGE_RECIPIENT_NAME,
                    DataBaseContract.Messages.MESSAGE_FILE_PATH,
                    DataBaseContract.Messages.MESSAGE_ADDRESS,
                    DataBaseContract.Messages.MESSAGE_LATITUDE,
                    DataBaseContract.Messages.MESSAGE_LONGITUDE,
                    DataBaseContract.Messages.MESSAGE_LINK,
                    DataBaseContract.Messages.MESSAGE_LINK_CONTENT,
                    DataBaseContract.Messages.MESSAGE_LINK_TITLE,
                    DataBaseContract.Messages.MESSAGE_STAR
            };
            String[] selectionArgs = {id};
            Cursor cursor = db.query(DataBaseContract.Messages.MESSAGES_TABLE, projections, selection, selectionArgs, null, null, null);
            while (cursor.moveToNext()) {
                String messageID = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_ID));
                String conversationID = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Conversations.CONVERSATIONS_ID_COLUMN_NAME));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_CONTENT_COLUMN_NAME));
                String senderUID = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_SENDER_COLUMN_NAME));
                String timeDelivered = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_TIME_DELIVERED_COLUMN_NAME));
                String timeSent = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_TIME_SENT_COLUMN_NAME));
                String senderName = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_SENDER_NAME));
                String recipientName = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_RECIPIENT_NAME));
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_FILE_PATH));
                String address = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_ADDRESS));
                String longitude = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_LONGITUDE));
                String latitude = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_LATITUDE));
                String link = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_LINK));
                String star = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_STAR));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_STATUS_COLUMN_NAME));
                String recipient = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseContract.Messages.MESSAGE_RECIPIENT_COLUMN_NAME));
                Message message = new Message();
                message.setMessageID(messageID);
                message.setMessage(content);
                message.setConversationID(conversationID);
                message.setMessageTime(timeSent);
                message.setSender(senderUID);
                message.setArrivingTime(timeDelivered);
                message.setSenderName(senderName);
                message.setRecipient(recipient);
                message.setRecipientName(recipientName);
                message.setFilePath(filePath);
                message.setLocationAddress(address);
                message.setLongitude(longitude);
                message.setLatitude(latitude);
                if (link != null)
                    message.setMessage(link);
                if (star != null)
                    message.setStar(star.equals("1"));
                message.setMessageStatus(status);
                ShowMessageOnScreen(message, messageAction);

            }
            cursor.close();
        }
    }

    private void MarkAsRead(String messageID) {
        if (db != null) {
            String selection = DataBaseContract.Messages.MESSAGE_ID + " LIKE ?";
            String[] selectionArgs = {messageID};
            ContentValues contentValues = new ContentValues();
            contentValues.put(DataBaseContract.Messages.MESSAGE_STATUS_COLUMN_NAME, ConversationActivity.MESSAGE_SEEN);
            db.update(DataBaseContract.Messages.MESSAGES_TABLE, contentValues, selection, selectionArgs);
        }
    }

    private void ShowMessageOnScreen(Message message, MessageAction action) {
        switch (action) {
            case new_message:
                int amount = chatAdapter.getItemCount();
                chatAdapter.addNewMessage(message);
                recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                if (amount == 0)
                    CreateNewConversation(message);
                SaveMessage(message);
                UpdateConversation(message);
                break;
            case edit_message:
                chatAdapter.changeExistingMessage(message);
                break;
            case delete_message:
                chatAdapter.DeleteMessage(message.getMessageID());
                DeleteMessage(message.getMessageID());
                break;
            case activity_start:
                chatAdapter.addNewMessage(message);
                recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                break;
        }
        if (!message.getSender().equals(currentUser) && !message.getMessageStatus().equals(MESSAGE_SEEN))
            SendMessageRead(message.getMessageID(), message.getConversationID(),message);
    }

    private void CreateNewConversation(Message message) {
        if (db != null) {
            ContentValues values = new ContentValues();
            values.put(DataBaseContract.Conversations.CONVERSATIONS_ID_COLUMN_NAME, message.getConversationID());
            values.put(DataBaseContract.Conversations.USER_UID, user.getUserUID());
            values.put(DataBaseContract.Conversations.CONVERSATION_LAST_MESSAGE_ID, message.getMessageID());
            values.put(DataBaseContract.Conversations.CONVERSATION_LAST_MESSAGE_COLUMN_NAME, message.getMessage());
            values.put(DataBaseContract.Conversations.CONVERSATION_LAST_MESSAGE_TYPE_COLUMN_NAME, message.getMessageType());
            values.put(DataBaseContract.Conversations.CONVERSATION_LAST_MESSAGE_TIME_COLUMN_NAME, message.getSendingTime());
            values.put(DataBaseContract.Conversations.CONVERSATION_RECIPIENT_NAME, message.getRecipientName());
            values.put(DataBaseContract.Conversations.CONVERSATIONS_MUTE_COLUMN_NAME, false);
            long newConversationID = db.insert(DataBaseContract.Conversations.CONVERSATIONS_TABLE, null, values);
            if (newConversationID == -1)
                Log.e(DATABASE_ERROR, "inserted more than 1 row");
            SharedPreferences sharedPreferences = getSharedPreferences("New Conversation", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("new conversation", conversationID);
            editor.apply();
        }
    }

    private void UpdateConversation(Message message) {
        if (db != null) {
            ContentValues values = new ContentValues();
            values.put(DataBaseContract.Conversations.CONVERSATIONS_ID_COLUMN_NAME, message.getConversationID());
            values.put(DataBaseContract.Conversations.USER_UID, user.getUserUID());
            values.put(DataBaseContract.Conversations.CONVERSATION_LAST_MESSAGE_ID, message.getMessageID());
            values.put(DataBaseContract.Conversations.CONVERSATION_LAST_MESSAGE_COLUMN_NAME, message.getMessage());
            values.put(DataBaseContract.Conversations.CONVERSATION_LAST_MESSAGE_TYPE_COLUMN_NAME, message.getMessageType());
            values.put(DataBaseContract.Conversations.CONVERSATION_LAST_MESSAGE_TIME_COLUMN_NAME, message.getSendingTime());
            values.put(DataBaseContract.Conversations.CONVERSATION_RECIPIENT_NAME, message.getRecipientName());
            values.put(DataBaseContract.User.TOKEN, message.getSenderToken());
            String selection = DataBaseContract.Conversations.CONVERSATIONS_ID_COLUMN_NAME + " LIKE ?";
            String[] selectionArgs = {message.getConversationID()};
            long updatedRowNum = db.update(DataBaseContract.Conversations.CONVERSATIONS_TABLE, values, selection, selectionArgs);
            if (updatedRowNum != 1)
                Log.e(DATABASE_ERROR, "updated more than 1 row");
            LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(new Intent("Update Conversation")
                            .putExtra("message", message));
            if (!message.getSender().equals(currentUser) && recipientsTokens.size() == 1)
                recipientsTokens.set(0,message.getSenderToken());
           else
               Log.e("MORE THAN 1 RECIPIENT","TO MANY RECIPIENTS");
        }
    }

    //Saves the message in the database
    private void SaveMessage(Message message) {
        if (db != null) {
            ContentValues values = new ContentValues();
            values.put(DataBaseContract.Messages.MESSAGE_ID, message.getMessageID());
            values.put(DataBaseContract.Conversations.CONVERSATIONS_ID_COLUMN_NAME, conversationID);
            values.put(DataBaseContract.Messages.MESSAGE_CONTENT_COLUMN_NAME, message.getMessage());
            values.put(DataBaseContract.Messages.MESSAGE_RECIPIENT_COLUMN_NAME, message.getRecipient());
            values.put(DataBaseContract.Messages.MESSAGE_SENDER_COLUMN_NAME, message.getSender());
            values.put(DataBaseContract.Messages.MESSAGE_TIME_DELIVERED_COLUMN_NAME, message.getArrivingTime());
            values.put(DataBaseContract.Messages.MESSAGE_TIME_SENT_COLUMN_NAME, message.getSendingTime());
            values.put(DataBaseContract.Messages.MESSAGE_TYPE_COLUMN_NAME, message.getMessageType());
            values.put(DataBaseContract.Messages.MESSAGE_STATUS_COLUMN_NAME, message.getMessageStatus());
            values.put(DataBaseContract.Messages.MESSAGE_IMAGE_PATH, message.getImagePath());
            values.put(DataBaseContract.Messages.MESSAGE_LONGITUDE, message.getLongitude());
            values.put(DataBaseContract.Messages.MESSAGE_LATITUDE, message.getLatitude());
            values.put(DataBaseContract.Messages.MESSAGE_ADDRESS, message.getLocationAddress());
            values.put(DataBaseContract.Messages.MESSAGE_RECORDING_PATH, message.getRecordingPath());
            if (message.getMessageType() == MessageType.webMessage.ordinal())
                values.put(DataBaseContract.Messages.MESSAGE_LINK, message.getMessage());
            long newRowId = db.insert(DataBaseContract.Messages.MESSAGES_TABLE, null, values);
            if (newRowId == -1)
                Log.e(DATABASE_ERROR, "inserted more than 1 row");
        }
    }

    /**
     * updates the message in the data base
     *
     * @param message - the message to update
     */
    private void UpdateMessage(@NonNull Message message) {

        if (db != null) {
            ContentValues values = new ContentValues();
            values.put(DataBaseContract.Messages.MESSAGE_CONTENT_COLUMN_NAME, message.getMessage());
            String selection = DataBaseContract.Messages.MESSAGE_ID + " LIKE ?";
            String[] selectionArgs = {message.getMessageID()};
            long updatedRow = db.update(DataBaseContract.Messages.MESSAGES_TABLE, values, selection, selectionArgs);
            if (updatedRow != 1)
                Log.e(DATABASE_ERROR, "updated more than 1 message");
        }
        //LoadMessage(messageID, DataBaseContract.Messages.MESSAGE_ID + " = ?", "Edit Message");
    }

    private void DeleteMessage(@NonNull String messageID) {

        if (db != null) {
            String selection = DataBaseContract.Messages.MESSAGE_ID + " LIKE ?";
            String[] selectionArgs = {messageID};
            int deletedRows = db.delete(DataBaseContract.Messages.MESSAGES_TABLE, selection, selectionArgs);
            if (deletedRows == -1)
                Log.e(DATABASE_ERROR, "didn't delete anything - deleted rows = -1");
        }
    }

    /**
     * Retrieves the tokens of all the recipients in the conversation
     *
     * @param recipients - the UID of all the recipients in the conversation (size of 1 means a 1 to 1 conversation, size > 1 means a group conversation
     *                   and size < 1 means an error)
     */
    @SuppressWarnings("ForLoopReplaceableByForEach")
    private void RetrieveRecipientsTokens(String... recipients) {
        //gets the tokens of all the recipients that are stored in the database
        DatabaseReference tokenReference = FirebaseDatabase.getInstance().getReference("Tokens");
        for (int i = 0; i < recipients.length; i++) {
            Query tokensQuery = tokenReference.orderByKey().equalTo(recipients[i]);//here the tokens that were retrieved are ordered by the key - which is equal to the recipients UID
            // Query tokensQuery = tokenReference.orderByKey().equalTo(currentUser);
            tokensQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String token = dataSnapshot.getValue(String.class);
                        if (!recipientsTokens.contains(token))
                            recipientsTokens.add(token);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(FIREBASE_ERROR, "cancelled firebase - didn't retrieve token");
                }
            });
        }
    }

    private void PrepareEditedMessage(Message message) {
        message.setMessage(messageToSend);
        message.setEditTime(System.currentTimeMillis() + "");
        message.setMessageAction(MessageAction.edit_message);
        SendMessage(message);
        messageSent.setText("");
        editMode = false;
    }

    private void PrepareDeletedMessage(Message message) {
        message.setMessageAction(MessageAction.delete_message);
        SendMessage(message);
    }

    // String[] recipientsToken = recipientsTokens.toArray(new String[0]);


    /**
     * every time a message is read - aka when the activity is opened or when a message is replayed to from a notification, sends a
     * data message to other party with an indicator that a message with id x has been read at time y
     */
    private void SendMessageRead(String messageId, String conversationID,Message message) {
        ReadMessage readMessage = new ReadMessage(messageId, conversationID);
        readMessage.setMessageStatus(MESSAGE_SEEN);
        readMessage.setReadAt(System.currentTimeMillis() + "");
        String[] recipientsToken = recipientsTokens.toArray(new String[0]);
        MessageSender.getInstance().SendMessage(readMessage, BackgroundMessages.read, message.getSenderToken());
    }

    private void SendMessageInteraction(String conversationID, int interactionType, boolean action) {
        InteractionMessage interactionMessage = new InteractionMessage();
        interactionMessage.setConversationID(conversationID);
        if (interactionType == TYPING)
            if (action)
                interactionMessage.setTyping("typing");
            else
                interactionMessage.setTyping("no");
        else if (interactionType == RECORDING)
            if (action)
                interactionMessage.setRecording("recording");
            else
                interactionMessage.setRecording("no");
        String[] recipientsToken = recipientsTokens.toArray(new String[0]);
        MessageSender.getInstance().SendMessage(interactionMessage, BackgroundMessages.interaction, recipientsToken);
    }

    private String getMyToken() {
        SharedPreferences sharedPreferences = getSharedPreferences("Token", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "no token");
        if (!token.equals("no token"))
            return token;
        else
            Log.e("TOKEN ERROR", "no token for current user");
        return null;
    }

    private void RequestRecipientsStatus()
    {
        RequestMessage requestMessage = new RequestMessage(Requests.status);
        String[] recipientsToken = recipientsTokens.toArray(new String[0]);
        MessageSender.getInstance().SendMessage(requestMessage,BackgroundMessages.request,recipientsToken);
    }
}
