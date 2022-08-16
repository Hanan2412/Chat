package com.example.woofmeow;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import Audio.AudioManager2;
import Backend.ConversationVM;
import Backend.UserVM;
import BroadcastReceivers.SMSBroadcastSent;
import Consts.ConversationType;
import Audio.AudioHelper;
import Audio.AudioManager;
import Audio.AudioPlayer2;
import Audio.AudioRecorder;
import Controller.NotificationsController;
import Fragments.GifBackdropFragment;
import NormalObjects.Conversation;
import NormalObjects.Gif;
import NormalObjects.ImageButtonSwitcher;
import NormalObjects.MessageHistory;
import NormalObjects.PlayAudioButton;
import NormalObjects.Web;
import Retrofit.Joke;
import Retrofit.RetrofitJoke;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import Adapters.ChatAdapter;

import Consts.ButtonType;
import Consts.MessageAction;
import Consts.MessageType;
import Fragments.BackdropFragment;
import Fragments.BottomSheetFragment;
import Fragments.PickerFragment;
import Fragments.VideoFragment;
import Model.MessageSender;
import NormalObjects.FileManager;
import NormalObjects.Message;
import NormalObjects.MessageTouch;
import NormalObjects.TouchListener;
import NormalObjects.User;
import Retrofit.RetrofitClient;
import Retrofit.Server;
import Services.TimedMessageService;
import Time.TimeFormat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("Convert2Lambda")
public class ConversationActivity2 extends AppCompatActivity implements ChatAdapter.MessageInfoListener, PickerFragment.onPickerClick, BottomSheetFragment.onSheetClicked, BackdropFragment.onBottomSheetAction, Serializable , GifBackdropFragment.onGifView {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private String filePath;

    public static final String MESSAGE_SEEN = "MESSAGE_SEEN";
    public static final String MESSAGE_SENT = "MESSAGE_SENT";
    public static final String MESSAGE_WAITING = "MESSAGE_WAITING";
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
    private com.vanniktech.emoji.EmojiEditText messageText;
    //private String recipientUID;

    //private final int GALLERY_REQUEST = 2;
    private final int WRITE_PERMISSION = 3;
    //private final int CAMERA_REQUEST = 4;
    private final int LOCATION_REQUEST = 5;
    //private final int REQUEST_SELECT_PHONE_NUMBER = 6;
    private final int CALL_PHONE = 7;
    //private final int SEND_FILE = 80;
    //private final int SEND_CONTACT = 9;
    //private final int DOCUMENT_REQUEST = 11;
    private User user;
    private List<User> recipients;
    private ImageButton actionBtn, clearTextBtn, closeQuoteBtn;

    private final String PICKER_FRAGMENT_TAG = "Picker_fragment";
    private final String BOTTOM_SHEET_TAG = "BottomSheet_fragment";
    private boolean camera;
    private int actionState = 0;
    private boolean editMode = false;
    private Message editMessage;
    private TextView quoteText, quoteSender;
    private boolean quoteOn = false;
    private int quotedMessagePosition = -1;
    private TextView conversationName;
    //private TextSwitcher textSwitcherStatus;
    private TextSwitcher typingIndicator;
    private ImageView conversationStatus;
    private boolean directCall;

    //private CController controller;
    private String recipientPhoneNumber;
    private boolean smsConversation = false;
    //button state is the button "type" as is how it functions
    private int buttonState = 1;
    private final int RECORD_VOICE = 1;
    private final int SEND_MESSAGE = 0;
    //private final int SEND_VIDEO = 2;
    private boolean startRecording = true;
    private boolean startPlaying1 = true;
    private TextView recordingTimeText;

    private PlayAudioButton playAudioRecordingBtn;
    private SeekBar voiceSeek;
    private boolean recorded = false;
    private Uri fileUri;

    private String link;//,title;
//    private RelativeLayout linkedMessagePreview;
    private ImageView linkedImage;
    private TextView linkTitle, linkContent;

    private ShapeableImageView conversationImage;
    private boolean messageLongPress = false;
    private Message selectedMessage;
    private String quotedMessageID;

    private final String FIREBASE_ERROR = "firebase_Error";
    private final String NULL_ERROR = "something is null";
    private final String ERROR_CASE = "Error in switch case";
    private final String ERROR_WRITE = "write error";
    private LinearLayout searchLayout, imagePreviewLayout, linkMessageLayout, groupCountLayout, userInputLayout;
    private ConstraintLayout voiceLayout, quoteLayout;
    private EditText searchText;
    private ArrayList<Integer> indices;
    private int indicesIndex = 0;
    //private final int REQUEST_VIDEO_CAPTURE = 8;
    private Uri videoUri;
    private final String VIDEO_FRAGMENT_TAG = "VIDEO_FRAGMENT";
    private BroadcastReceiver receiveNewMessages;
    private final int TYPING = 0;
    private final int NOT_TYPING = 2;
    private final int RECORDING = 1;
    private final int NOT_RECORDING = 3;
    private final int READ_TIME = 4;
    private final int STATUS = 5;
    private final int DELETE = 6;
    private final int EDIT = 7;
    private final int LEAVE_GROUP = 8;
    private boolean typing = false;
    private boolean contact = false;
    private String contactName, contactNumber;
    private ValueEventListener tokenListener;
    private DatabaseReference tokenReference;
    private BroadcastReceiver MessageReceiver;
    private BroadcastReceiver recipientStatus;
    private BroadcastReceiver imageMessage;
    private ConversationType conversationType;
    private MessageSender messageSender;
    private Conversation conversation;
    private Toolbar toolbar;
    private String groupName;
    private ConversationVM model;
    private UserVM userModel;
    private Gif gif;
    private ActivityResultLauncher<Intent> takePicture, addPhoneNumber, openGallery, attachFile, sendContact, sendDoc, video, newRecipients;
    private FloatingActionButton scrollToBot;
    private ProgressBar linkProgressBar;
    private MessageType messageType;
    private ImageButtonSwitcher sendMessageBtn;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EmojiManager.install(new IosEmojiProvider());
        messageSender = MessageSender.getInstance();
        setContentView(R.layout.conversation_layout3);

        LinearLayout layout = findViewById(R.id.root_container);

        toolbar = findViewById(R.id.toolbar1);
        ImageButton backButton = findViewById(R.id.goBack);
        conversationImage = findViewById(R.id.conversationImage);
        conversationStatus = findViewById(R.id.conversationStatus);
        conversationName = findViewById(R.id.conversationName);
        typingIndicator = findViewById(R.id.typingIndicator);

        searchLayout = findViewById(R.id.searchLayout);
        searchText = findViewById(R.id.searchText);
        Button searchBtn = findViewById(R.id.searchBtn);
        ExtendedFloatingActionButton scrollToNext = findViewById(R.id.scrollToNext);

        recyclerView = findViewById(R.id.recycle_view);
        imagePreviewLayout = findViewById(R.id.imagePreviewLayout);
        imageView = findViewById(R.id.imagePreview);

        quoteLayout = findViewById(R.id.quoteLayout);
        quoteSender = findViewById(R.id.senderName);
        quoteText = findViewById(R.id.quoteText);
        closeQuoteBtn = findViewById(R.id.closeQuoteBtn);

        linkMessageLayout = findViewById(R.id.linkMessageLayout);
        linkContent = findViewById(R.id.linkContent);
        linkTitle = findViewById(R.id.linkTitle);
        linkedImage = findViewById(R.id.linkImage);
        linkProgressBar = findViewById(R.id.linkProgressBar);

        voiceLayout = findViewById(R.id.voiceLayout);
        playAudioRecordingBtn = findViewById(R.id.play_pause_btn);
        voiceSeek = findViewById(R.id.voiceSeek);
        recordingTimeText = findViewById(R.id.recordingTime);
        ImageView closeRecordingBtn = findViewById(R.id.closeBtn);

        groupCountLayout = findViewById(R.id.groupCount);
        TextView smsCharCount = findViewById(R.id.smsCharCount);
        scrollToBot = findViewById(R.id.scrollToBot);

        userInputLayout = findViewById(R.id.userInputLayout);
        clearTextBtn = findViewById(R.id.clear_text);
        ImageButton emojiBtn = findViewById(R.id.emojiBtn);
        messageText = findViewById(R.id.messageText);
        actionBtn = findViewById(R.id.actionBtn);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayShowTitleEnabled(false);

        recipients = new ArrayList<>();
        conversationID = getIntent().getStringExtra("conversationID");
        NotificationsController notificationsController = NotificationsController.getInstance();
        notificationsController.removeNotification(conversationID);
        model = new ViewModelProvider(this).get(ConversationVM.class);
        userModel = new ViewModelProvider(this).get(UserVM.class);
        geocoder = new Geocoder(this);
        chatAdapter = new ChatAdapter();

        messageType = MessageType.textMessage;
        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);

        sendMessageBtn = findViewById(R.id.sendImageButtonSwitch);
        sendMessageBtn.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(ConversationActivity2.this);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setLayoutParams(new ImageSwitcher.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                return imageView;

            }
        });
        sendMessageBtn.setOnClickListener(new ImageButtonSwitcher.onClick() {
            @Override
            public void onPress(int buttonState) {
                if (buttonState == ImageButtonSwitcher.SEND_MESSAGE) {
                    String message = "";
                    if (messageText.getText() != null)
                        message = messageText.getText().toString();
                    playMessageSound();
                    prepareMessage(message);
                } else if (buttonState == ImageButtonSwitcher.RECORD_VOICE)
                {
                    if (askPermission(MessageType.voiceMessage)) {
                        recordingSoundStart();
                        recordOrNot();
                    }
                }
            }

            @Override
            public void onRelease(int buttonState) {
                if (buttonState == ImageButtonSwitcher.RECORD_VOICE)
                {
                    recordingSoundStopped();
                    voiceLayout.setVisibility(View.VISIBLE);
                    recordOrNot();
                    setCorrectBtn(ButtonType.sendMessage);
                    sendMessageBtn.setButtonState(ImageButtonSwitcher.SEND_MESSAGE);
                }
                else if (buttonState == ImageButtonSwitcher.SEND_MESSAGE)
                {
                    changeMessageType(MessageType.voiceMessage);
                    resetToText();
                }
            }
        });

        sendMessageBtn.setInAnimation(in);
        sendMessageBtn.setOutAnimation(out);
        sendMessageBtn.setImageResource(R.drawable.ic_baseline_mic_black);
        messageText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (chatAdapter.getItemCount() > 0)
                {//no need to send typing info before first message is sent and a conversation is created
                    if (!typing)
                    {//don't send typing info if its identical to previous info - less messages sent
                        InteractionMessage(conversationID, null, TYPING);
                        typing = true;
                    }
                }
                if (smsConversation && count >= 140)
                {
                    smsCharCount.setVisibility(View.VISIBLE);
                    int msgAmount = count % 160;
                    String charCount = "(" + msgAmount + ") " + count + "";
                    smsCharCount.setText(charCount);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (smsConversation)
                {
                    if (s.toString().isEmpty() || s.toString().length() < 140)
                        smsCharCount.setVisibility(View.GONE);
                }
                if (chatAdapter.getItemCount() > 0)//prevents updating and creating new incomplete conversation object in server before first message sent
                    if (s.toString().isEmpty())
                    {
                        InteractionMessage(conversationID, null, NOT_TYPING);
                        typing = false;
                    }
                AudioManager manager = AudioManager.getInstance();
                AudioRecorder audioRecorder = manager.getAudioRecorder(conversationID, ConversationActivity2.this);
                //button state starts at RECORD_VOICE, when typing starts, button state changes to SEND_MESSAGE. if text field is cleared, the button state returns to RECORD_VOICE
                if(s.toString().isEmpty())
                {
                    if (messageType == MessageType.textMessage)
                        changeMessageType(MessageType.voiceMessage);
                    clearTextBtn.setVisibility(View.GONE);
                }
                else
                {
                    if (!audioRecorder.isRecording()) {
                        clearTextBtn.setVisibility(View.VISIBLE);
                        if (messageType != MessageType.editMessage)
                            changeMessageType(MessageType.textMessage);
                    }

                }
            }
        });
        clearTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageText.setText("");
                if (messageType == MessageType.editMessage)
                    changeMessageType(MessageType.voiceMessage);
            }
        });
        typingIndicator.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView toTextSwitcher = new TextView(ConversationActivity2.this);
                toTextSwitcher.setGravity(Gravity.CENTER | Gravity.START);
                toTextSwitcher.setTextSize(14);
                toTextSwitcher.setTextColor(getResources().getColor(android.R.color.white, getTheme()));
                return toTextSwitcher;
            }
        });
        typingIndicator.setInAnimation(in);
        typingIndicator.setOutAnimation(out);

        link = getIntent().getStringExtra("link");
        if (link != null) {
            messageText.setText(link);
            messagePreview(link);
        }

        chatAdapter.setMessages(new ArrayList<>());
        chatAdapter.setCurrentUserUID(currentUser);
        chatAdapter.setListener(this);



        scrollToBot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollToBot.setVisibility(View.GONE);
                recyclerView.scrollToPosition(chatAdapter.getItemCount()-1);
            }
        });
        messageSender.setMessageListener(new MessageSender.onMessageSent() {
            @Override
            public void onMessageSentSuccessfully(Message message) {
                message.setMessageStatus(MESSAGE_SENT);
                updateMessage(message);
//                model.updateMessage(message);
            }

            @Override
            public void onMessagePartiallySent(Message message, String[] tokens, String error) {
            }

            @Override
            public void onMessageNotSent(Message message, String error) {
                if (message.getMessageID() != null) {
                    message.setMessageStatus(MESSAGE_WAITING);
                    updateMessage(message);
                }
            }
        });


        newRecipients = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @SuppressWarnings("unchecked")
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        List<User> recipients = (ArrayList<User>) result.getData().getSerializableExtra("group");
                        if (recipients != null) {
                            List<String> newRecipients = new ArrayList<>();
                            int index = ConversationActivity2.this.recipients.size();
                            for (int i = index; i < recipients.size(); i++) {
                                User user = recipients.get(i);
                                getRecipientToken(user.getUserUID());
                                userModel.insertUser(user);
                                newRecipients.add(user.getUserUID());
                            }
                            model.createNewGroup(conversationID, newRecipients);
                            ConversationActivity2.this.recipients = recipients;
                        } else
                            Log.e(NULL_ERROR, "new recipients are NULL");
                    }
                }
            }
        });
        takePicture = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    Drawable drawable = Drawable.createFromPath(photoPath);
                    if (drawable != null) {
                        imagePreviewLayout.setVisibility(View.VISIBLE);
                        imageView.setImageDrawable(drawable);
                        changeMessageType(MessageType.imageMessage);
                        camera = true;
                    }
                }
            }
        });
        openGallery = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    Intent resultIntent = result.getData();
                    if (resultIntent != null) {
                        Uri uri = resultIntent.getData();
                        if (uri != null) {
                            imagePreviewLayout.setVisibility(View.VISIBLE);
                            messageType = MessageType.imageMessage;
                            imageUri = uri;
                            imageView.setImageURI(uri);
                            imageBitmap = getImageBitmap(uri);
                            imageView.setImageBitmap(imageBitmap);
                            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, 500, 450, false);
                            camera = false;
                        }
                    }
                }
            }
        });
        addPhoneNumber = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    Intent resultIntent = result.getData();
                    if (resultIntent != null) {
                        Uri contactUri = resultIntent.getData();
                        String[] projections = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
                        if (contactUri != null) {
                            Cursor cursor = getContentResolver().query(contactUri, projections, null, null, null);
                            if (cursor != null && cursor.moveToFirst()) {
                                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                                String number = cursor.getString(numberIndex);
                                cursor.close();
                                recipients.get(0).setPhoneNumber(number);
                                userModel.updateUser(recipients.get(0));
                                //dbActive.updateUser(recipients.get(0));
                                Toast.makeText(ConversationActivity2.this, "number saved ", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }
        });
        attachFile = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                // TODO: 17/01/2022  
            }
        });
        sendContact = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    Intent resultIntent = result.getData();
                    if (resultIntent != null) {
                        Uri contactUri = resultIntent.getData();
                        String[] projections = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,
                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
                        if (contactUri != null) {
                            Cursor cursor = getContentResolver().query(contactUri, projections, null, null, null);
                            if (cursor != null && cursor.moveToFirst()) {
                                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                                contactNumber = cursor.getString(numberIndex);
                                contactName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                contact = true;
                                cursor.close();
                                prepareMessageToSend(MessageType.contact.ordinal());
                            }
                        }
                    }
                }
            }
        });
        sendDoc = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                //needs check
                if (result.getResultCode() == RESULT_OK) {
                    Intent resultIntent = result.getData();
                    Uri docUri;
                    if (resultIntent != null) {
                        docUri = resultIntent.getData();
                        if (docUri != null && docUri.getPath() != null) {
                            File file = new File(docUri.getPath());
                            if (file.exists()) {
                                try {
                                    ParcelFileDescriptor descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
                                    PdfRenderer renderer = new PdfRenderer(descriptor);
                                    PdfRenderer.Page page = renderer.openPage(0);
                                    Bitmap docBitmap = BitmapFactory.decodeFileDescriptor(descriptor.getFileDescriptor());
                                    page.render(docBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                                    imagePreviewLayout.setVisibility(View.VISIBLE);
                                    imageView.setImageBitmap(docBitmap);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        });
        video = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

            }
        });
        EmojiPopup emojiPopup = EmojiPopup.Builder.fromRootView(layout).build(messageText);
        emojiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emojiPopup.isShowing())
                    emojiPopup.dismiss();
                else
                    emojiPopup.toggle();
            }
        });

        loadCurrentUserFromDataBase();
        loadConversationRecipients();

        if (!smsConversation) {
            if (recipients != null) {//updates tokens for all recipients
                for (int i = 0; i < recipients.size(); i++) {
                    if (recipients.get(i) != null)//here because of debugging and testing - on production version the if shouldn't exist since null user should be impossible
                        getRecipientToken(recipients.get(i).getUserUID());
                    else {
                        recipients.remove(null);
                        Log.e(NULL_ERROR, "user is null");
                        i--;
                    }
                }
            }
            MessageStatusBroadcast();
        } else {
            listenToSMSStatus();
            actionBtn.setVisibility(View.GONE);
        }

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchQuery = searchText.getText().toString();
                indices = chatAdapter.SearchMessage(searchQuery);
            }
        });
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
        closeRecordingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioManager manager = AudioManager.getInstance();
                manager.releasePlayer(filePath);
                sendMessageBtn.setButtonState(ImageButtonSwitcher.RECORD_VOICE);
                messageType = MessageType.textMessage;
                filePath = null;
                startPlaying1 = true;
                recordingTimeText.setText(R.string.zero_time);
                resetToText();
                buttonState = RECORD_VOICE;
                setCorrectBtn(ButtonType.microphone);
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("background", Context.MODE_PRIVATE);
        int presetBackground = sharedPreferences.getInt("backgroundImage normal",-1);
        if (presetBackground!=-1)
        {
            Drawable drawable = ResourcesCompat.getDrawable(getResources(),presetBackground,getTheme());
            recyclerView.setBackground(drawable);
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
        recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if(linearLayoutManager.findLastCompletelyVisibleItemPosition() == chatAdapter.getItemCount()-1)
                    scrollToBot.setVisibility(View.GONE);
                else scrollToBot.setVisibility(View.VISIBLE);
            }
        });
        MessageTouch touch = new MessageTouch(ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.START | ItemTouchHelper.END);
        touch.setListener(new TouchListener() {
            @Override
            public void onSwipe(@NonNull RecyclerView.ViewHolder viewHolder, int swipeDirection) {
                viewHolder.getAdapterPosition();
                TextView message = viewHolder.itemView.findViewById(R.id.message);
                //String quote = "\"" + message.getText() + "\"";
                String quote = message.getText().toString();
                Message quoteMessage = chatAdapter.getMessage(viewHolder.getAdapterPosition());
                quoteLayout.setVisibility(View.VISIBLE);
                quoteText.setText(quoteMessage.getMessage());
                quoteSender.setText(quoteMessage.getSenderName());
                quoteOn = true;
                quotedMessageID = chatAdapter.getMessageID(viewHolder.getAdapterPosition());
                quotedMessageID = quoteMessage.getMessageID();
                quotedMessagePosition = viewHolder.getAdapterPosition();
                //brings back just the item that was swiped away
                if (recyclerView.getAdapter() != null)
                    recyclerView.getAdapter().notifyItemChanged(viewHolder.getAdapterPosition());
                //chatAdapter.notifyDataSetChanged();
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
        });
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(touch);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        if (chatAdapter.getItemCount() > 0)
            recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);

        SetUpBySettings();
        closeQuoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetQuote();
                recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
            }
        });

        playAudioRecordingBtn.setListener(new PlayAudioButton.onPlayAudio() {
            @Override
            public void playAudio() {
                playRecording(true);

            }

            @Override
            public void pauseAudio() {
                playRecording(false);

            }
        });
//        playAudioRecordingBtn.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                playRecording();
//            }
//        });

        actionBtn.setOnClickListener(new View.OnClickListener() {
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
                    case joke: {
                        onJoke();
                        break;
                    }
                    default:
                        Log.e(ERROR_CASE, "action button error: " + new Throwable().getStackTrace()[0].getLineNumber());
                }

            }
        });
        actionBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //opens bottom sheet do display extra options
                BottomSheetFragment bottomSheetFragment = BottomSheetFragment.newInstance();
                bottomSheetFragment.show(getSupportFragmentManager(), BOTTOM_SHEET_TAG);
                return true;
            }
        });
        //loadConversationImage();
        conversationImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chatAdapter.getItemCount() == 0)
                    Toast.makeText(ConversationActivity2.this, "send or receive a message to open conversation profile", Toast.LENGTH_SHORT).show();
                else {
                    if (recipients.size() > 1) {
                        Intent openGroupIntent = new Intent(ConversationActivity2.this, GroupActivity.class);
                        openGroupIntent.putExtra("conversationID", conversationID);
                        startActivity(openGroupIntent);//opens group profile
                    } else if (recipients.size() == 1) {

                        Intent openRecipientIntent = new Intent(ConversationActivity2.this, ProfileActivity2.class);
                        openRecipientIntent.putExtra("user", recipients.get(0));//opens single user profile activity
                        openRecipientIntent.putExtra("conversationID", conversationID);
                        startActivity(openRecipientIntent);

                    }
                }

            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConversationActivity2.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        SharedPreferences conversationPreferences = getSharedPreferences("Conversation", MODE_PRIVATE);
        SharedPreferences.Editor editor = conversationPreferences.edit();
        editor.putString("liveConversation", conversationID);
        editor.apply();
        linkMessageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                link = null;
                linkMessageLayout.setVisibility(View.GONE);
                buttonState = RECORD_VOICE;
                //imageSwitcher.setImageResource(R.drawable.ic_baseline_send_24);
                resetToText();
            }
        });

        registerForContextMenu(recyclerView);
        init(conversationID);
        if (smsConversation) {
            buttonState = SEND_MESSAGE;
        }
    }

    private void sendSMS(Message message) {
        if (askPermission(MessageType.sms))
            smsSendMessage(message);
    }

    private void smsSendMessage(Message message) {
        Log.d("smsMessageID", message.getMessageID());
//        MessageSender sender = MessageSender.getInstance();
        messageSender.sendMessage(message, recipientPhoneNumber, this);
    }

    private void listenToSMSStatus() {
        BroadcastReceiver smsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String messageID = intent.getStringExtra("messageID");
                String status = intent.getStringExtra("status");
                chatAdapter.updateMessageStatus(messageID, status, System.currentTimeMillis() + "");
                if (status != null && status.equals(MESSAGE_DELIVERED))
                    markAsRead(messageID);
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(smsReceiver, new IntentFilter(SMSBroadcastSent.SENT_SMS_STATUS));
    }

    @SuppressWarnings("unchecked")
    private void loadConversationRecipients() {
        //the following if statements are for a new conversation
        if (getIntent().hasExtra("recipientUser")) {
            //new single conversation
            User user = (User) getIntent().getSerializableExtra("recipientUser");
            if (user != null) {
                recipients = new ArrayList<>();
                recipients.add(user);
                setGroupName(user.getName());
                //groupName = user.getName();
                conversationType = ConversationType.single;
            } else
                throw new NullPointerException("user is null when creating new conversation");
        } else if (getIntent().hasExtra("group")) {
            //new group conversation
            recipients = (List<User>) getIntent().getSerializableExtra("group");
            //groupName = getIntent().getStringExtra("groupName");
            String name = getIntent().getStringExtra("groupName");
            setGroupName(name);
            //conversationName.setText(groupName);
            conversationType = ConversationType.group;
        } else if (getIntent().hasExtra("phoneNumber")) {
            //new sms conversation
            recipientPhoneNumber = getIntent().getStringExtra("phoneNumber");
            User user = (User) getIntent().getSerializableExtra("smsUser");
            recipients = new ArrayList<>();
            recipients.add(user);
            smsConversation = true;
            conversationType = ConversationType.sms;
            if (user != null) {
                if (user.getName() == null)
                    setGroupName(recipientPhoneNumber);
                    //groupName = recipientPhoneNumber;
                else
                    setGroupName(user.getName());
                //groupName = user.getName();
            } else
                setGroupName(recipientPhoneNumber);
            //groupName = recipientPhoneNumber;
        }
    }

    private void setGroupName(String name) {
        groupName = name;
        conversationName.setText(name);
    }

    private void messagePreview(String originalLink) {
        Web web = new Web();
        web.setListener(new Web.onWebDownload() {
            @Override
            public void onMetaDataDownload(String description, String title) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        linkMessageLayout.setVisibility(View.VISIBLE);
                        linkedImage.setScaleType(ImageView.ScaleType.FIT_XY);
                        linkContent.setText(description);
                        linkTitle.setText(title);

                    }
                });
            }

            @Override
            public void onWebImageSuccess(Bitmap bitmap) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        linkedImage.setImageBitmap(bitmap);
                        linkProgressBar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onFailed() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ConversationActivity2.this, "failed loading link", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        web.downloadWebPreview(originalLink);
        buttonState = SEND_MESSAGE;
        messageText.setText(originalLink);
    }

    private void loadConversationImage() {
        FileManager fileManager = FileManager.getInstance();
        Bitmap bitmap = null;
        if (recipients.size() > 1)
            bitmap = fileManager.readImage(this, FileManager.conversationProfileImage, conversationID);
        else if (!recipients.isEmpty())
            bitmap = fileManager.readImage(this, FileManager.user_profile_images, recipients.get(0).getUserUID());
        if (bitmap == null && !recipients.isEmpty()) {
            userModel.setOnUserImageDownloadListener(new Server.onFileDownload() {
                @Override
                public void onDownloadStarted() {

                }

                @Override
                public void onProgress(int progress) {

                }

                @Override
                public void onDownloadFinished(File file) {
                    String filePath = file.getAbsolutePath();
                    Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                    if (recipients.size() > 1)
                        fileManager.saveProfileImage(bitmap, conversationID, ConversationActivity2.this, true);
                    else
                        fileManager.saveProfileImage(bitmap, recipients.get(0).getUserUID(), ConversationActivity2.this, false);
                    userModel.setOnUserImageDownloadListener(null);

                }

                @Override
                public void onFileDownloadFinished(String messageID, File file) {

                }

                @Override
                public void onDownloadError(String errorMessage) {

                }
            });
            userModel.downloadImage(recipients.get(0).getPictureLink());
//            Picasso.get().load(recipients.get(0).getPictureLink()).into(new Target() {
//                @Override
//                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                    talkingToImage.setImageBitmap(bitmap);
//                    if (recipients.size() > 1)
//                        fileManager.saveProfileImage(bitmap, conversationID, ConversationActivity2.this, true);
//                    else
//                        fileManager.saveProfileImage(bitmap, recipients.get(0).getUserUID(), ConversationActivity2.this, false);
//                    Log.d("Picasso", "user image downloaded");
//                }
//
//                @Override
//                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
//                    Log.e("failed loading bitmap", "failed loading bitmap from picasso");
//                }
//
//                @Override
//                public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//                }
//            });
        } else conversationImage.setImageBitmap(bitmap);
    }

    private void onLocationAction() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ConversationActivity2.this);
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
                if (askPermission(MessageType.gpsMessage))
                    findLocation();
            }
        }).setMessage("Are you sure you want to send your current location");
        builder.show();
    }

    private void onCameraAction() {
        if (askPermission(MessageType.photoMessage))
            requestCamera();
    }

    private void onFileAction() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ConversationActivity2.this);
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
                attachFile.launch(Intent.createChooser(attachFileIntent, "select file"));
                //startActivityForResult(Intent.createChooser(attachFileIntent, "select file"), SEND_FILE);
            }
        }).setMessage("Are you sure you want to send a file");
        builder.create().show();
    }

    private void onContactAction() {
        Intent contactIntent = new Intent(Intent.ACTION_PICK);
        contactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        sendContact.launch(contactIntent);
        //startActivityForResult(contactIntent, SEND_CONTACT);
    }

    private void onDocumentAction() {
        Intent openDocIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        sendDoc.launch(openDocIntent);
        //startActivityForResult(openDocIntent, DOCUMENT_REQUEST);
    }

    private void onVideoAction() {
        if (askPermission(MessageType.photoMessage))
            RecordVideo();
    }

    private void onJoke() {
        RetrofitJoke client = RetrofitClient.getRetrofitClient("https://api.chucknorris.io").create(RetrofitJoke.class);
        client.sendJokeRequest().enqueue(new Callback<Joke>() {
            @Override
            public void onResponse(@NonNull Call<Joke> call, @NonNull Response<Joke> response) {
                if (response.code() == 200) {
                    Joke joke = response.body();
                    if (joke != null) {
                        messageText.setText(joke.getValue());
                        //messagePreview(joke.getValue(), "Random chuck norris joke", joke.getIcon_url(),joke.getUrl());
                    }
                } else
                    Log.e("joke response code and message", response.code() + " " + response.message());
            }

            @Override
            public void onFailure(@NonNull Call<Joke> call, @NonNull Throwable t) {
                Log.e("Joke", "didn't get the joke");
            }
        });

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
        String chosenActionBtn = preferences.getString("ActionButton", "failed setUpBySettings");
        directCall = preferences.getBoolean("directCall", false);
        switch (chosenActionBtn) {
            case "Location":
                setCorrectBtn(ButtonType.location);
                actionState = ButtonType.location.ordinal();
                break;
            case "Camera":
                setCorrectBtn(ButtonType.camera);
                actionState = ButtonType.camera.ordinal();
                break;
            case "Gallery":
                setCorrectBtn(ButtonType.gallery);
                actionState = ButtonType.gallery.ordinal();
                break;
            case "Delayed Message": {
                setCorrectBtn(ButtonType.delay);
                actionState = ButtonType.delay.ordinal();
                break;
            }
            /*case "Video Message": {
                SetCorrectColor(ButtonType.video);
                actionState = ButtonType.video.ordinal();
                break;
            }*/
            default:
                setCorrectBtn(ButtonType.location);
                actionState = ButtonType.location.ordinal();
                Log.e(ERROR_CASE, "default action preference:" + chosenActionBtn);
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
    }

    private void setCorrectBtn(ButtonType type) {
        if (smsConversation) {
            buttonState = SEND_MESSAGE;

        } else
            switch (type) {
                case location: {
                    actionBtn.setImageResource(R.drawable.ic_baseline_location_on_24);
                    break;
                }
                case attachFile: {
                    actionBtn.setImageResource(R.drawable.ic_baseline_attach_file_white);
                    break;
                }
                case camera: {
                    actionBtn.setImageResource(R.drawable.ic_baseline_camera_alt_white);
                    break;
                }
                case gallery: {
                    actionBtn.setImageResource(R.drawable.ic_baseline_photo_24);
                    break;
                }
                case delay: {
                    actionBtn.setImageResource(R.drawable.ic_baseline_access_time_white);
                    break;
                }
                case video: {
                    actionBtn.setImageResource(R.drawable.ic_baseline_videocam_white);
                    break;
                }
                case play: {
                    playAudioRecordingBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_white);
                    break;
                }
                case pause: {
                    playAudioRecordingBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_white);
                    break;
                }
                default: {
                    actionBtn.setImageResource(R.drawable.ic_baseline_location_on_24);
                    Log.i(ERROR_CASE, "default color");
                    break;
                }
            }
    }

    private void recordingSoundStart() {
        new AudioPlayer2(getApplicationContext(), R.raw.recording_sound_start);
    }

    private void recordingSoundStopped() {
        new AudioPlayer2(getApplicationContext(), R.raw.recording_sound_stopped);
    }

    private void playNewMessageSound() {
        new AudioPlayer2(getApplicationContext(), R.raw.new_message_arrived);
    }

    private void playMessageSound() {
        new AudioPlayer2(getApplicationContext(), R.raw.send_message_sound);
    }

    private void prepareMessage(String message)
    {
        String[] recipientsNames = new String[recipients.size()];
        String[] recipientsID = new String[recipients.size()];
        for (int i = 0; i < recipientsNames.length; i++) {
            recipientsNames[i] = recipients.get(i).getName();
            if (!smsConversation)
                recipientsID[i] = recipients.get(i).getUserUID();
        }
        switch (messageType)
        {
            case editMessage:
                InteractionMessage(editMessage.getConversationID(), editMessage.getMessageID(), EDIT);
                break;
            case sms: case textMessage: case gpsMessage:
                if (!message.isEmpty())
                     createMessage(message, messageType.ordinal(), recipientsNames, recipientsID);
                break;
            case voiceMessage:
                sendRecording();
                break;
            case photoMessage:case imageMessage:
                createMessage(message, messageType.ordinal(), recipientsNames, recipientsID);
                break;
        }

    }

    private void prepareSendMessage() {

        if (smsConversation)
            prepareMessageToSend(MessageType.sms.ordinal());
        else if (imageView.getVisibility() == View.VISIBLE) {
            prepareMessageToSend(MessageType.photoMessage.ordinal());
        } else if (imageView.getVisibility() == View.GONE) {
            prepareMessageToSend(MessageType.textMessage.ordinal());
        }
        linkMessageLayout.setVisibility(View.GONE);
        link = null;
    }

    private void resetToText() {
        userInputLayout.setVisibility(View.VISIBLE);
        messageText.setVisibility(View.VISIBLE);
        messageText.setText("");//clears the input field
        imagePreviewLayout.setVisibility(View.GONE);
        messageText.requestFocus();
        voiceLayout.setVisibility(View.GONE);
        setCorrectBtn(ButtonType.microphone);
        recorded = false;
        buttonState = RECORD_VOICE;
        startRecording = true;
        if (filePath != null) {
            AudioManager manager = AudioManager.getInstance();
            manager.releasePlayer(filePath);
            startPlaying1 = true;
            // RecordOrNot();
        }
        if (smsConversation)
            actionBtn.setVisibility(View.GONE);
    }

    private void ShowVoiceControl() {
        voiceSeek.setProgress(0);
        voiceLayout.setVisibility(View.VISIBLE);
        recordingTimeText.setText(getResources().getString(R.string.zero_time));
        recordOrNot();
        setCorrectBtn(ButtonType.sendMessage);
        buttonState = SEND_MESSAGE;
        recorded = true;
    }

    private void longPressToRecordVoice() {
        if (askPermission(MessageType.voiceMessage)) {
            recordOrNot();
        }
    }

    private void sendRecording() {
        createFileUri(filePath);
        String[] names = getRecipientsNames();
        createMessage("recording", MessageType.voiceMessage.ordinal(), names, getRecipientsIDs());
    }

    private void createFileUri(String filePath) {
        File file = new File(filePath);
        fileUri = Uri.fromFile(file);
    }

    private void recordOrNot() {
        AudioManager manager = AudioManager.getInstance();
        AudioRecorder audioRecorder = manager.getAudioRecorder(conversationID, this);
        changeMessageType(MessageType.voiceMessage);
        if (audioRecorder.getListener() == null)
        {
            audioRecorder.setListener(new AudioHelper() {
                @Override
                public void onProgressChange(String formattedProgress, int progress) {
                    Handler handler = new Handler(getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageText.setText(formattedProgress);
                        }
                    });
//                    recordingTimeText.setText(formattedProgress);
                }

                @Override
                public void onPlayingStatusChange(boolean isPlaying) {
                    if (isPlaying) {
                        setCorrectBtn(ButtonType.recording);
                    } else {
                        manager.releaseRecorder(conversationID);
                        setCorrectBtn(ButtonType.microphone);
                        userInputLayout.setVisibility(View.GONE);
                        recordingTimeText.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
        filePath = audioRecorder.getRecordingPath();
        audioRecorder.recordStopAudio();
        recorded = true;
    }

    private void playRecording(boolean play)
    {
        TimeFormat format = new TimeFormat();
        AudioManager2 manager = AudioManager2.getInstance();
        AudioPlayer2 audioPlayer = manager.getAudioPlayer(filePath);
        audioPlayer.setAudioListener(new AudioHelper() {
            @Override
            public void onProgressChange(String formattedProgress, int progress) {
                String time = formattedProgress + "/" + format.getFormattedTime(audioPlayer.getDuration());
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        recordingTimeText.setText(time);
                        voiceSeek.setProgress(progress, true);
                        if (progress == audioPlayer.getDuration()/1000)
                            playAudioRecordingBtn.resetClicks();
                    }
                });
                manager.updateProgress(filePath, progress);

            }

            @Override
            public void onPlayingStatusChange(boolean isPlaying) {

            }
        });
        if (play) {
            int progress = manager.getProgress(filePath);
            audioPlayer.seekTo(progress * 1000);
            voiceSeek.setProgress(progress);
        }
        audioPlayer.playPauseAudio();
    }

//    private void playRecording() {
//        recordingTimeText.setVisibility(View.VISIBLE);
//        messageText.setVisibility(View.GONE);
//        AudioManager2 manager = AudioManager2.getInstance();
//        AudioPlayer2 audioPlayer = manager.getAudioPlayer(filePath);
//        audioPlayer.setAudioListener(new AudioHelper() {
//            @Override
//            public void onProgressChange(String formattedProgress, int progress) {
//
//                recordingTimeText.setText(formattedProgress);
//            }
//
//            @Override
//            public void onPlayingStatusChange(boolean isPlaying) {
//                if (isPlaying) {
//                    setCorrectBtn(ButtonType.pause);
//                } else {
//                    setCorrectBtn(ButtonType.play);
//                }
//            }
//        });
//        audioPlayer.playPauseAudio();
//    }

    @Override
    public void onMessageClick(Message message, View view, int viewType) {
        if (message.getMessageType() == MessageType.contact.ordinal()) {
            Intent createNewContact = new Intent(ContactsContract.Intents.Insert.ACTION);
            createNewContact.setType(ContactsContract.RawContacts.CONTENT_TYPE);
            createNewContact.putExtra(ContactsContract.Intents.Insert.NAME, message.getContactName());
            createNewContact.putExtra(ContactsContract.Intents.Insert.PHONE, message.getContactPhone());
            startActivity(createNewContact);
        }
        if (messageLongPress) {
            messageLongPress = false;
            invalidateOptionsMenu();
            Drawable drawable;
            if (message.getSender().equals(currentUser)) {
                drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.outgoing_message_look, getTheme());
            } else {
                drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.incoming_message_look, getTheme());
            }
            view.setBackground(drawable);

        } else if (message.getQuoteMessage() != null) {
            //this is a quoted message
            String quotedMessageID = message.getQuotedMessageID();
            if (quotedMessageID != null) {
                int index = chatAdapter.findMessageLocation(null, 0, chatAdapter.getItemCount() - 1, Long.parseLong(quotedMessageID));
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
        if(messageType != MessageType.editMessage)
        {
            messageText.setText(message.getMessage());
            changeMessageType(MessageType.editMessage);
            editMessage = message;
        }
        else
        {
            messageText.setText("");
            editMessage = null;
        }
    }


    //saves image to local storage
    @Override
    public String onImageDownloaded(Bitmap bitmap, Message message) {
        FileManager fileManager = FileManager.getInstance();
        String path = fileManager.saveImage(bitmap, this);
        message.setImagePath(path);
        return path;
    }

    @Override
    public String onVideoDownloaded(File file, Message message) {
        String path = null;
        String fileName = file.getName();
        OutputStream out = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = ConversationActivity2.this.getApplicationContext().getContentResolver();
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

    @Override
    public void onRetrySending(String messageID,String imagePath) {
        int index = chatAdapter.findMessageLocation((ArrayList<Message>) chatAdapter.getMessages(),0,chatAdapter.getMessages().size()-1,Long.parseLong(messageID));
        if (index!=-1)
        {
            Message message = chatAdapter.getMessage(index);
            String[] recipientsTokens = new String[recipients.size()];
            for (int i = 0; i < recipientsTokens.length; i++) {
                recipientsTokens[i] = recipients.get(i).getToken();
            }
            messageSender.sendMessage(message,recipientsTokens);
            message.setSent(true);
            chatAdapter.notifyItemChanged(index);
        }

    }

    @Override
    public void onDeleteMessageClick(Message message) {
        InteractionMessage(message.getConversationID(), message.getMessageID(), DELETE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences conversationPreferences = getSharedPreferences("Conversation", MODE_PRIVATE);
        SharedPreferences.Editor editor = conversationPreferences.edit();
        editor.putString("liveConversation", conversationID);
        editor.apply();
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
        StatusOffline(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!smsConversation) {
            if (tokenReference != null && tokenListener != null)
                tokenReference.removeEventListener(tokenListener);
        }
        SharedPreferences conversationPreferences = getSharedPreferences("Conversation", MODE_PRIVATE);
        SharedPreferences.Editor editor = conversationPreferences.edit();
        editor.putString("liveConversation", "noConversation");
        editor.apply();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(MessageReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiveNewMessages);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(recipientStatus);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(imageMessage);
        StatusOffline(false);
    }

    private boolean askPermission(MessageType messageType) {
        switch (messageType) {
            case gpsMessage: {
                int hasLocationPermission = ConversationActivity2.this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
                    return false;
                } else return true;
            }
            case photoMessage: {
                int hasWritePermission = ConversationActivity2.this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION);
                    return false;
                } else return true;
            }
            case callPhone: {
                int hasCallPermission = ConversationActivity2.this.checkSelfPermission(Manifest.permission.CALL_PHONE);
                if (hasCallPermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, CALL_PHONE);
                    return false;
                } else return true;
            }
            case voiceMessage: {
                int hasRecordingPermission = ConversationActivity2.this.checkSelfPermission(Manifest.permission.RECORD_AUDIO);
                if (hasRecordingPermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
                    return false;
                } else return true;
            }
            case sms: {
                int hasSmsPermission = ConversationActivity2.this.checkSelfPermission(Manifest.permission.SEND_SMS);
                if (hasSmsPermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 999);
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
                        Toast.makeText(ConversationActivity2.this, "permission is required to use the camera", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(this, "great! to record press and hold the recording button", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(this, "permission required to record audi", Toast.LENGTH_SHORT).show();
                    break;
                case 999:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                        Toast.makeText(this, "Great! permission granted, press send to send a sms message", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(this, "can't send sms without permission", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }


    @SuppressLint("MissingPermission")
    private void findLocation() {
        client = LocationServices.getFusedLocationProviderClient(ConversationActivity2.this);
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
                    String[] recipientsNames = getRecipientsNames();
                    createMessage(messageToSend, MessageType.gpsMessage.ordinal(), recipientsNames, getRecipientsIDs());
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

    private void prepareMessageToSend(int messageType) {
        String[] recipientsNames = new String[recipients.size()];
        String[] recipientsID = new String[recipients.size()];
        for (int i = 0; i < recipientsNames.length; i++) {
            recipientsNames[i] = recipients.get(i).getName();
            if (!smsConversation)
                recipientsID[i] = recipients.get(i).getUserUID();
        }
        createMessage(messageToSend, messageType, recipientsNames, recipientsID);
        resetToText();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (connectivityManager != null) {
                return connectivityManager.getActiveNetwork() != null && connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork()) != null;
            }
        } else {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    /*private void ConnectedToInternet() {
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
                    Toast.makeText(ConversationActivity2.this, "network is available, functionality regained", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onNoNetwork() {
                networkConnection = false;
                Toast.makeText(ConversationActivity2.this, "network isn't available - messages will not be sent or received. connect to the internet in order to regain functionality", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNetworkLost() {
                //networkConnection = false;
                Toast.makeText(ConversationActivity2.this, "network lost - disconnected from the internet", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChangedNetworkType() {
                networkConnection = true;
                //Toast.makeText(ConversationActivity2.this, "network changed", Toast.LENGTH_SHORT).show();
            }
        });
        //ConnectivityManager.NetworkCallback networkCallback = network;
        if (connectivityManager != null)
            connectivityManager.registerNetworkCallback(request, network2);

    }*/

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    @Override
    public void onPicked(int[] time, String content) {
        Message message = new Message();
        message.setMessage(content);
        message.setMessageID(System.currentTimeMillis() + "");
        message.setGroupName(groupName);
        message.setSender(currentUser);
        message.setMessageStatus(MESSAGE_WAITING);
        message.setSenderName(user.getName());
        message.setRecipients(Arrays.asList(getRecipientsIDs().clone()));
        message.setConversationID(conversationID);
        message.setMessageKind("newMessage");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, time[0]);
        calendar.set(Calendar.MONTH, time[1]);
        calendar.set(Calendar.DAY_OF_MONTH, time[2]);
        calendar.set(Calendar.HOUR_OF_DAY, time[3]);
        calendar.set(Calendar.MINUTE, time[4]);
        calendar.set(Calendar.SECOND, 0);
        Intent foreground = new Intent(this, TimedMessageService.class);
        foreground.putExtra("message", message);
        foreground.putExtra("token", getRecipientsTokens());
        foreground.putExtra("time", calendar.getTimeInMillis() + "");
        startForegroundService(foreground);
        messageText.setText("");
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
        setCorrectBtn(buttonType);
        DialogFragment fragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(BOTTOM_SHEET_TAG);
        if (fragment != null)
            fragment.dismiss();
        switch (buttonType) {
            case attachFile: {
                changeMessageType(MessageType.fileMessage);
                onFileAction();
                break;
            }
            case camera: {
                changeMessageType(MessageType.imageMessage);
                onCameraAction();
                break;
            }
            case gallery: {
                changeMessageType(MessageType.photoMessage);
                onGalleryAction();
                break;
            }
            case location: {
                changeMessageType(MessageType.location);
                onLocationAction();
                break;
            }
            case delay: {
                onDelayAction();
                break;
            }
            case video: {
                changeMessageType(MessageType.videoMessage);
                onVideoAction();
                break;
            }
            case contact: {
                changeMessageType(MessageType.contact);
                onContactAction();
                break;
            }
            case document: {
                changeMessageType(MessageType.fileMessage);
                onDocumentAction();
                break;
            }
            case joke: {
                changeMessageType(MessageType.textMessage);
                messageType = MessageType.textMessage;
                onJoke();
                break;
            }
            case gif:
                GifBackdropFragment backdropFragment = GifBackdropFragment.newInstance();
                backdropFragment.show(getSupportFragmentManager(), "BOTTOM_SHEET_GIF_TAG");
                break;
            default:
                Log.e(ERROR_CASE, "bottom sheet error " + new Throwable().getStackTrace()[0].getLineNumber());
        }
    }

    public void changeMessageType(MessageType type)
    {
        this.messageType = type;
        switch (type)
        {
            case photoMessage: case imageMessage:case fileMessage:case gpsMessage:case textMessage:
                sendMessageBtn.setButtonState(ImageButtonSwitcher.SEND_MESSAGE);
                break;
            case voiceMessage:case contact:
                sendMessageBtn.setButtonState(ImageButtonSwitcher.RECORD_VOICE);
                break;
            case editMessage:
                sendMessageBtn.setButtonState(ImageButtonSwitcher.SEND_MESSAGE);
                sendMessageBtn.setImageResource(R.drawable.ic_baseline_edit_24);
                break;
        }
    }
/*
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
                    imageUri = uri;
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
                        recipients.get(0).setPhoneNumber(number);
                        userModel.updateUser(recipients.get(0));
                        //dbActive.updateUser(recipients.get(0));
                        Toast.makeText(this, "number saved ", Toast.LENGTH_SHORT).show();
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
                            ConversationActivity2.this.videoUri = videoUri;
                            String[] name = getRecipientsNames();
                            CreateMessage(messageToSend, MessageType.videoMessage.ordinal(), name, getRecipientsIDs());
                            //sendMessage(MessageType.videoMessage.ordinal(), recipientUID);
                            //ConversationActivity2.this.videoUri = null;
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
                        contactNumber = cursor.getString(numberIndex);
                        contactName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        contact = true;
                        cursor.close();
                        prepareMessageToSend(MessageType.contact.ordinal());
                        // PrepareMessageToSend(MessageType.contact.ordinal(), recipientUID);
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
    }*/

    private void requestCamera() {
        if (askPermission(MessageType.photoMessage))
            takePicture();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        openGallery.launch(Intent.createChooser(intent, "Select Picture to Upload"));
        //startActivityForResult(Intent.createChooser(intent, "Select Picture to Upload"), GALLERY_REQUEST);
    }

    private void takePicture() {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = ConversationActivity2.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File image = File.createTempFile(imageFileName, ".jpg", storageDir);
            photoPath = image.getAbsolutePath();
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(ConversationActivity2.this.getPackageManager()) != null) {
                File photoFile;
                photoFile = image;
                Uri photoURI = FileProvider.getUriForFile(ConversationActivity2.this,
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

    private Bitmap getImageBitmap(Uri uri) {
        Bitmap image = null;
        try {
            if (Build.VERSION.SDK_INT > 27) {
                ImageDecoder.Source source = ImageDecoder.createSource(ConversationActivity2.this.getContentResolver(), uri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                image = MediaStore.Images.Media.getBitmap(ConversationActivity2.this.getContentResolver(), uri);
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
        if (recipients != null && recipients.size() > 1) {
            menu.findItem(R.id.callBtn).setVisible(false);
            menu.findItem(R.id.addAsContact).setVisible(false);
            menu.findItem(R.id.addPhoneNumber).setVisible(false);
            menu.setGroupVisible(R.id.groupConversation, true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    //extra options
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.callBtn) {
            //opening dialer to call the recipient number if exists
            if (recipients.get(0).getPhoneNumber() != null) {
                if (directCall)
                    CallPhone(askPermission(MessageType.callPhone));
                else
                    CallPhone(false);
            } else
                Toast.makeText(ConversationActivity2.this, "no number, can't call if no number is provided", Toast.LENGTH_SHORT).show();

        } else if (item.getItemId() == R.id.addAsContact) {
            //adds current recipient as a contact to contacts list
            Intent addContactIntent = new Intent(Intent.ACTION_INSERT);
            addContactIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);
            addContactIntent.putExtra(ContactsContract.Intents.Insert.NAME, recipients.get(0).getName() + " " + recipients.get(0).getLastName());//first name ---- space ---- lastName
            addContactIntent.putExtra(ContactsContract.Intents.Insert.PHONE, recipients.get(0).getPhoneNumber());
            //addContactIntent.putExtra(ContactsContract.Intents.Insert.EMAIL, "example@wxample.com");
            if (addContactIntent.resolveActivity(getPackageManager()) != null)
                startActivity(addContactIntent);
        } else if (item.getItemId() == R.id.addPhoneNumber) {
            //opens contact and gets selected contact number
            View builderView = getLayoutInflater().inflate(R.layout.edit_text_dialog, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            EditText text = builderView.findViewById(R.id.editTextDialog);
            AlertDialog alert = builder.setTitle("Add a phone number to this contact")
                    .setMessage("choose a phone number from contacts or type one")
                    .setCancelable(true)
                    .setPositiveButton("from contacts", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent addPhoneNumberIntent = new Intent(Intent.ACTION_PICK);
                            addPhoneNumberIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                            if (addPhoneNumberIntent.resolveActivity(getPackageManager()) != null)
                                addPhoneNumber.launch(addPhoneNumberIntent);
                            //startActivityForResult(addPhoneNumberIntent, REQUEST_SELECT_PHONE_NUMBER);
                            dialog.dismiss();
                        }
                    }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).setNeutralButton("add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String phoneNumber = text.getText().toString();
                            recipients.get(0).setPhoneNumber(phoneNumber);
                            userModel.updateUser(recipients.get(0));
                            // dbActive.updateUser(recipients.get(0));
                            dialog.dismiss();
                        }
                    }).setView(builderView).create();
            alert.show();
        } else if (item.getItemId() == R.id.blockConversation) {
            model.isConversationBlocked(conversationID).observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    if (aBoolean) {
                        model.blockConversation(conversationID);
                        Toast.makeText(ConversationActivity2.this, "conversation was blocked!", Toast.LENGTH_SHORT).show();
                    } else {
                        model.unBlockConversation(conversationID);
                        Toast.makeText(ConversationActivity2.this, "conversation was un-blocked!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

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
                final String conversationType = "conversationType";
                backDropBundle.putSerializable("message", selectedMessage);
                backDropBundle.putInt(conversationType, this.conversationType.ordinal());
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
            if (selectedMessage != null) {
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
                            Toast.makeText(ConversationActivity2.this, "Error report was submitted, thank you", Toast.LENGTH_SHORT).show();
                        }
                    }).show();
            } else
                Toast.makeText(ConversationActivity2.this, "select a message to copy it", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.addNewMember) {
            Intent intent = new Intent(ConversationActivity2.this, NewGroupChat2Activity.class);
            intent.putExtra("recipients", (ArrayList<User>) recipients);
            newRecipients.launch(intent);
        } else if (item.getItemId() == R.id.leaveGroup) {
            InteractionMessage(conversationID, null, LEAVE_GROUP);
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
                askPermission(MessageType.callPhone);
        } else {
            callRecipientIntent = new Intent(Intent.ACTION_DIAL);
        }
        if (callRecipientIntent != null) {
            callRecipientIntent.setData(Uri.parse("tel:" + recipients.get(0).getPhoneNumber()));
            if (callRecipientIntent.resolveActivity(getPackageManager()) != null)
                startActivity(callRecipientIntent);
        }
    }

    private void InsertToDataBase(Message message) {
        model.checkIfMessageExists(message).observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (!aBoolean)
                    model.saveMessage(message);
            }
        });
    }

    private void UpdateDataBase(Message message) {
        InsertToDataBase(message);

    }

    private void RecordVideo() {
        File videoFile = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        String videoFileName = "video_" + System.currentTimeMillis();
        try {
            File video = File.createTempFile(videoFileName, ".mp4", videoFile);
            Uri videoURI = FileProvider.getUriForFile(ConversationActivity2.this,
                    "com.example.woofmeow.provider", video);
            Intent recordVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            recordVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoURI);
            if (recordVideoIntent.resolveActivity(getPackageManager()) != null) {

                this.video.launch(recordVideoIntent);
                //startActivityForResult(recordVideoIntent, REQUEST_VIDEO_CAPTURE);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//------------------------------------------------------------------------------------------------------------------------------------------------------------------------//

    private void init(String conversationID) {
        initDBRoom();
        ReceiveMessages(conversationID);
        getRecipientStatus();
        getImageMessage();
    }

    private void loadCurrentUserFromDataBase() {
        userModel.loadUserByID(currentUser).observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if (user != null)
                    ConversationActivity2.this.user = user;
                else {
                    Log.e(NULL_ERROR, "loaded null user");
                    userModel.setOnUserDownloadedListener(new Server.onUserDownload() {
                        @Override
                        public void downloadedUser(User user) {
                            ConversationActivity2.this.user = user;
                        }
                    });
//                    userModel.setOnUserDownloadListener(new Server3.onUserDownloaded() {
//                        @Override
//                        public void downloadedUser(User user) {
//                            ConversationActivity2.this.user = user;
//                        }
//                    });
                    userModel.downloadUser(currentUser);
                    Log.i(USER_SERVICE, "downloading user");
                }
            }
        });
    }

    private void createMessage(String content, int messageType, String[] recipientsNames, String... recipients) {
        String currentTime = System.currentTimeMillis() + "";
        TimeZone timeZone = TimeZone.getTimeZone("GMT-4");
        Calendar calendar = Calendar.getInstance(timeZone);
        String time = calendar.getTimeInMillis() + "";
        Message message = new Message();
        message.setMessage(content);
        message.setSendingTime(currentTime);
        message.setConversationID(conversationID);
        for (User recipientUser : this.recipients)
            message.addRecipient(recipientUser.getUserUID());
        for (String name : recipientsNames)
            message.addRecipientName(name);
        message.setGroupName(groupName);
        message.setSenderName(user.getName());
        message.setSender(currentUser);
        message.setMessageStatus(MESSAGE_WAITING);
        message.setMessageType(messageType);
        model.setOnFileUploadListener(new Server.onFileUpload() {
            @Override
            public void onPathReady(String path) {
                message.setFilePath(path);
                sendMessage(message);
            }

            @Override
            public void onStartedUpload(String msgID) {
                //shows progress bar
                int index = chatAdapter.findMessageLocation((ArrayList<Message>) chatAdapter.getMessages(),0,chatAdapter.getMessages().size()-1,Long.parseLong(msgID));
                if (index != -1)
                {
                    chatAdapter.getMessage(index).setUploading(true);
                    chatAdapter.notifyItemChanged(index);
                    chatAdapter.getMessage(index).setError(false);
                }
            }

            @Override
            public void onProgress(int progress) {

            }

            @Override
            public void onUploadFinished(String msgID) {
                //disables progress bar
                int index = chatAdapter.findMessageLocation((ArrayList<Message>) chatAdapter.getMessages(),0,chatAdapter.getMessages().size()-1,Long.parseLong(msgID));
                if (index != -1)
                {
                    chatAdapter.getMessage(index).setUploading(false);
                    chatAdapter.getMessage(index).setError(false);
                    chatAdapter.notifyItemChanged(index);
                }
            }

            @Override
            public void onUploadError(String msgID, String errorMessage) {
                //displays error message and gives option to resend the message file
                int index = chatAdapter.findMessageLocation((ArrayList<Message>) chatAdapter.getMessages(),0,chatAdapter.getMessages().size()-1,Long.parseLong(msgID));
                if (index!=-1)
                {
                    chatAdapter.getMessage(index).setSent(false);
                    chatAdapter.notifyItemChanged(index);
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(ConversationActivity2.this);
                builder.setTitle("Error")
                        .setMessage("an error occurred while sending the file")
                        .setIcon(R.drawable.ic_baseline_error_24)
                        .setCancelable(true)
                        .setNeutralButton("ok",null)
                        .create()
                        .show();
                sendMessage(message);
            }
        });
        if (quoteOn) {
            message.setQuoteMessage(quoteText.getText().toString());
            message.setQuotedMessagePosition(quotedMessagePosition);
            message.setQuotedMessageID(quotedMessageID);
            resetQuote();
        }
        if (contact) {
            message.setContactName(contactName);
            message.setContactPhone(contactNumber);
            contact = false;
            contactName = null;
            contactNumber = null;
        }
        message.setMessageID(time);
        MessageType type = MessageType.values()[messageType];
        switch (type) {
            case textMessage:
                if (Patterns.WEB_URL.matcher(content).matches())
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
                    message.setImagePath(photoPath);
                    model.uploadFile(currentUser,message.getMessageID(),Bitmap.createScaledBitmap(BitmapFactory.decodeFile(photoPath), 500, 450, false),ConversationActivity2.this);
                } else//photo from gallery
                {
                    message.setImagePath(imageUri.toString());
                    model.uploadFile(currentUser,message.getMessageID(),imageBitmap,ConversationActivity2.this);
                }
                break;
            case voiceMessage:
                message.setRecordingPath(fileUri.toString());
                message.setMessage("Voice Message");
                model.uploadFile(message.getMessageID(),fileUri,ConversationActivity2.this);
                break;
            case videoMessage:
                model.uploadFile(message.getMessageID(),videoUri,ConversationActivity2.this);
                break;
            case gif:
                message.setMessage(gif.getUrl());
                break;
            default:
        }
        if (type == MessageType.textMessage || type == MessageType.gpsMessage || type == MessageType.webMessage || type == MessageType.status || type == MessageType.sms || type == MessageType.contact || type == MessageType.gif)
            sendMessage(message);
    }

    private void sendMessage(@NonNull Message message) {
        if (recipients.size() > 0) {
            if (message.getMessageType() == MessageType.sms.ordinal()) {
                sendSMS(message);
                saveMessage(message);
                showMessageOnScreen(message, message.getMessageAction());
            } else {
                String token = getMyToken();
                message.setSenderToken(token);
//                MessageSender messageSender = MessageSender.getInstance();
                String[] recipientsTokens = new String[recipients.size()];
                for (int i = 0; i < recipientsTokens.length; i++) {
                    recipientsTokens[i] = recipients.get(i).getToken();
                }
                Log.d("the amount of all the recipients tokens ", recipientsTokens.length + "");
                saveMessage(message);
                if (isNetworkAvailable()) {
//                    message.setMessageStatus(MESSAGE_SENT);
                    messageSender.sendMessage(message, recipientsTokens);
                    updateMessage(message);
//                    model.updateMessageStatus(message.getMessageID(), MESSAGE_SENT);
                    //dbActive.updateMessageStatus(message.getMessageID(), MESSAGE_SENT);
                } else {
                    if (!message.getMessageStatus().equals(MESSAGE_WAITING)) {//no need to constantly update the same data - saves on processing time
                        message.setMessageStatus(MESSAGE_WAITING);
                        model.updateMessageStatus(message.getMessageID(), MESSAGE_WAITING);
                        //dbActive.updateMessageStatus(message.getMessageID(), MESSAGE_WAITING);
                    }
                    Toast.makeText(ConversationActivity2.this, "message will be sent during the next session with a valid connection", Toast.LENGTH_LONG).show();
                }
                if (message.getMessageStatus().equals(MESSAGE_WAITING)) {
                    if (!chatAdapter.isMessageExists(message.getMessageID()))
                        showMessageOnScreen(message, message.getMessageAction());
                } else
                    showMessageOnScreen(message, message.getMessageAction());
            }
        } else {
            Log.e(NULL_ERROR, "no recipients");
            Toast.makeText(ConversationActivity2.this, "error - can't send message, try again later", Toast.LENGTH_SHORT).show();
        }
    }

    private void ReceiveMessages(String conversationID) {
        receiveNewMessages = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra("readAt")) {
                    String messageID = intent.getStringExtra("messageID");
                    String readAt = intent.getStringExtra("readAt");
                    String messageStatus = intent.getStringExtra("messageStatus");
                    chatAdapter.updateMessageStatus(messageID, messageStatus, readAt);
                    markAsRead(messageID);
                } else if (intent.hasExtra("typing")) {
                    boolean typing = intent.getBooleanExtra("typing", false);
                    if (typing)
                        typingIndicator.setText("typing");
                    else
                        typingIndicator.setText("");
                } else if (intent.hasExtra("recording")) {
                    boolean recording = intent.getBooleanExtra("recording", false);
                    if (recording)
                        typingIndicator.setText("recording");
                    else
                        typingIndicator.setText("");
                } else if (intent.hasExtra("not typing") || intent.hasExtra("not recording")) {
                    typingIndicator.setText("");
                } else if (intent.hasExtra("edit")) {
                    Log.d("edit message conversation activity", "got edit ");
                    String message = (String) intent.getSerializableExtra("message");
                    String messageID = (String) intent.getSerializableExtra("messageID");
                    String editTime = (String) intent.getSerializableExtra("edit_time");
                    int i = chatAdapter.updateMessageEdit(messageID, message, editTime);
                    UpdateMessage(messageID, message, editTime);
                    if (i == chatAdapter.getItemCount() - 1) {
                        model.updateConversationLastMessage(conversationID, message);
                        //dbActive.updateConversationLastMessage(conversationID, message);
                    }
                } else if (intent.hasExtra("message")) {
                    Log.d("new message conversation activity", "got new message");
                    playNewMessageSound();
                    Message message = (Message) intent.getSerializableExtra("message");
                    if (message != null && message.getMessageAction() != null) {
                        showMessageOnScreen(message, message.getMessageAction());
                    }
                } else if (intent.hasExtra("delete")) {
                    Log.d("delete message conversation activity", "got del ");
                    Message message = (Message) intent.getSerializableExtra("message");
                    if (message != null && message.getMessageAction() != null) {
                        showMessageOnScreen(message, message.getMessageAction());

                    }
                }

            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(receiveNewMessages, new IntentFilter(conversationID));
    }

    private void UpdateMessage(String messageID, String content, String time) {
        model.updateMessage(messageID, content, time);
    }

    private void markAsRead(String messageID) {
        model.updateMessageStatus(messageID, MESSAGE_SEEN);
    }


    private void showMessageOnScreen(Message message, MessageAction action) {
        switch (action) {
            case new_message:
                int amount = chatAdapter.getItemCount();
                if (amount == 0) {//sending the first message in a conversation
                    createNewConversation(message);
                    for (User user : recipients) {
                        getRecipientToken(user.getUserUID());
                        userModel.insertUser(user);
                    }
                }
                chatAdapter.addNewMessage(message);
                recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                UpdateConversation(message);
                break;
            case edit_message:
                chatAdapter.updateMessage(message);
//                chatAdapter.changeExistingMessage(message);
                updateMessage(message);
                break;
            case delete_message:
                chatAdapter.deleteMessage(message.getMessageID());
                deleteMessage(message.getMessageID());
                break;
            case activity_start:
                chatAdapter.addNewMessage(message);
                recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                break;
        }
        if (!smsConversation && !message.getSender().equals(currentUser) && !message.getMessageStatus().equals(MESSAGE_SEEN))
            InteractionMessage(message.getConversationID(), message.getMessageID(), READ_TIME);
    }

    private void initDBRoom() {
        model.getRecipients(conversationID).observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                if (!users.isEmpty()) {
                    recipients = users;
                    sendMessageStatus();
                }
                if (smsConversation) {
                    toolbar.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark, getTheme()));
                    toolbar.setPopupTheme(R.style.sms);
                } else if (recipients.size() > 1) {
                    toolbar.setBackgroundColor(getResources().getColor(android.R.color.holo_purple, getTheme()));
                    toolbar.setPopupTheme(R.style.group);
                } else if (recipients.size() == 1) {
                    toolbar.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light, getTheme()));
                    toolbar.setPopupTheme(R.style.single);
                } else
                    Log.e(NULL_ERROR, "ZERO! recipients in conversation activity");
                loadConversationImage();
                StatusRequest();
                invalidateOptionsMenu();
            }
        });
        LiveData<Conversation> conversationLiveData = model.loadConversation(conversationID);
        conversationLiveData.observe(this, new Observer<Conversation>() {
            @Override
            public void onChanged(Conversation conversation) {
                if (conversation != null) {
                    ConversationActivity2.this.conversation = conversation;
                    setGroupName(conversation.getGroupName());
                    conversationType = conversation.getConversationType();
                    if (conversationType == ConversationType.sms) {
                        smsConversation = true;
                        if (!recipients.isEmpty())
                            recipientPhoneNumber = recipients.get(0).getPhoneNumber();
                        toolbar.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark, getTheme()));
                        toolbar.setPopupTheme(R.style.sms);
                    }
                    conversation.setUnreadMessages(0);
                    model.updateConversation(conversation);
                }
                conversationLiveData.removeObservers(ConversationActivity2.this);
            }
        });
        LiveData<List<Message>> messagesLD = model.loadMessages(conversationID);
        messagesLD.observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {
                for (Message message : messages) {
                    if (message.getMessage()!=null)
                    if (message.getMessageStatus().equals(MESSAGE_WAITING)) {
                        sendMessage(message);
                    }
                    else showMessageOnScreen(message, MessageAction.activity_start);//else statement because this function is being called also in sendMessage function
                }
                sendMessageStatus();
                messagesLD.removeObservers(ConversationActivity2.this);
                loadNewMessages();
            }
        });
    }

    private void sendMessageStatus()
    {
        if (recipients != null && !recipients.isEmpty())
        {
            List<Message>messages = chatAdapter.getMessages();
            if (messages != null && !messages.isEmpty())
            {
                if (!smsConversation) {
                    for (Message message : messages) {
                        if (!message.getMessageStatus().equals(MESSAGE_SEEN) && !message.getSender().equals(currentUser)) {
                            message.setMessageStatus(MESSAGE_SEEN);
//                            MessageSender sender = MessageSender.getInstance();
                            if (recipients != null)
                                if (!recipients.isEmpty()) {
                                    for (int i = 0; i < recipients.size(); i++)
                                        if (recipients.get(i).getToken() != null)
                                            messageSender.sendMessage(message, recipients.get(i).getToken());
                                        else
                                            Log.e(NULL_ERROR, "load messages - recipient token is null. recipient id: " + recipients.get(i).getUserUID());
                                }
                            markAsRead(message.getMessageID());
                        }
                    }
                }
            }
        }
    }

    private void loadNewMessages() {
        LiveData<Message> newMessage = model.getNewMessage(currentUser, conversationID);
        newMessage.observe(this, new Observer<Message>() {
            @Override
            public void onChanged(Message message) {
                if (message != null)
                    if (!chatAdapter.isMessageExists(message.getMessageID()))
                        showMessageOnScreen(message, message.getMessageAction());
            }
        });
    }

    private void createNewConversation(Message message) {
        model.createNewConversation(message, currentUser, conversationType);
    }

    private void UpdateConversation(Message message) {
        model.updateConversation(message);
    }

    //Saves the message in the database
    private void saveMessage(Message message) {
        model.checkIfMessageExists(message).observe(ConversationActivity2.this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean!=null)
                {
                    if(!aBoolean)
                        model.saveMessage(message);
                }
            }
        });

    }

    private void updateMessage(@NonNull Message message) {
        model.updateMessage(message);
        chatAdapter.updateMessage(message);
    }

    private void deleteMessage(@NonNull String messageID) {
        model.deleteMessage(messageID);
        chatAdapter.deleteMessage(messageID);
    }

    private void prepareEditedMessage(Message message) {
        InteractionMessage(message.getConversationID(), message.getMessageID(), EDIT);
        messageText.setText("");
        editMode = false;
    }

    private void InteractionMessage(String conversationID, String messageID, int type) {
        if (!smsConversation) {
            Message message = new Message();
            message.setConversationID(conversationID);
            message.setSenderToken(getMyToken());
            switch (type) {
                case TYPING: {
                    message.setMessageKind("typing");
                    break;
                }
                case NOT_TYPING: {
                    message.setMessageKind("not typing");
                    break;
                }
                case RECORDING: {
                    message.setMessageKind("recording");
                    break;
                }
                case NOT_RECORDING: {
                    message.setMessageKind("not recording");
                    break;
                }
                case READ_TIME: {
                    message.setReadAt(System.currentTimeMillis());
                    message.setMessageID(messageID);
                    message.setMessageKind("read_time");
                    message.setMessageStatus(MESSAGE_SEEN);
                    break;
                }
                case DELETE: {
                    message.setMessageKind("delete");
                    message.setMessageID(messageID);
                    deleteMessage(messageID);
                    break;
                }
                case EDIT: {
                    if (messageText.getText() != null) {
                        message = editMessage;
                        message.setMessageID(messageID);
                        message.setMessage(messageText.getText().toString());
                        message.setEditTime(System.currentTimeMillis() + "");
                        message.setMessageAction(MessageAction.edit_message);
                        message.setMessageKind("edit");
                        message.setMessageStatus(MESSAGE_WAITING);
//                        chatAdapter.updateMessageEdit(messageID, messageText.getText().toString(), message.getMessageTime());
                        model.addMessageHistory(new MessageHistory(message));
                        model.updateConversationLastMessage(conversationID, messageText.getText().toString());
                        updateMessage(message);
//                        model.updateMessage(messageID, messageText.getText().toString(), message.getEditTime());
                    }
                    //dbActive.updateConversationLastMessage(conversationID, messageToSend);
                    break;
                }
                case STATUS: {
                    message.setMessageKind("status");
                    break;
                }
                case LEAVE_GROUP: {
                    message.setMessageKind("leave_group");
                    break;
                }
                default:
                    Log.e(ERROR_CASE, "interaction message error");
            }
//            MessageSender sender = MessageSender.getInstance();
            messageSender.sendMessage(message, getRecipientsTokens());
        }
    }

    private String[] getRecipientsNames() {
        String[] recipientsNames = new String[recipients.size()];
        for (int i = 0; i < recipientsNames.length; i++)
            recipientsNames[i] = recipients.get(i).getName();
        return recipientsNames;
    }

    private String[] getRecipientsIDs() {
        String[] recipientsIDs = new String[recipients.size()];
        for (int i = 0; i < recipientsIDs.length; i++)
            recipientsIDs[i] = recipients.get(i).getUserUID();
        return recipientsIDs;
    }

    private String[] getRecipientsTokens() {
        String[] recipientsTokens = new String[recipients.size()];
        for (int i = 0; i < recipientsTokens.length; i++)
            recipientsTokens[i] = recipients.get(i).getToken();
        return recipientsTokens;
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

    private void getRecipientToken(String uid)
    {
        if (uid!=null)
        {
            userModel.setOnTokenDownloadedListener(new Server.onTokenDownloaded() {
                @Override
                public void tokenDownloaded(String uid, String token) {
                    if (token!=null)
                    {
                        User recipient = getRecipientByID(uid);
                        if (recipient != null) {
                            if (!token.equals(recipient.getToken())) {
                                userModel.updateToken(uid, token);
                                recipient.setToken(token);
                            }
                        }
                    }
                }

                @Override
                public void error(String message) {
                    Log.e(NULL_ERROR,message);
                }
            });
            userModel.getUserToken(uid);
        }
        else
            Log.e(NULL_ERROR, "recipientUID is null");
    }

//    private void getRecipientToken(String uid) {
//        if (uid != null) {
//            FirebaseDatabase database = FirebaseDatabase.getInstance();
//            tokenReference = database.getReference("Tokens/" + uid);
//            tokenListener = new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    String token = (String) snapshot.getValue();
//                    if (token != null) {
//                        Log.e("newToken: ", token);
//                        User recipient = getRecipientByID(uid);
//                        if (recipient != null) {
//                            if (!token.equals(recipient.getToken())) {
//                                userModel.updateToken(uid, token);
//                                //dbActive.updateUserToken(uid, token);
//                                recipient.setToken(token);
//                            }
//                        }
//                    } else Log.e(NULL_ERROR, "Recipient Token from fb is null");
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    Log.e(FIREBASE_ERROR, "cancelled recipient token retrieval");
//                }
//            };
//            tokenReference.addValueEventListener(tokenListener);
//        } else
//            Log.e(NULL_ERROR, "recipientUID is null");
//    }

    private User getRecipientByID(String uid) {
        for (User user : recipients)
            if (user.getUserUID().equals(uid))
                return user;
        return null;
    }

    private void MessageStatusBroadcast() {
        MessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String status = intent.getStringExtra("status");
                String id = intent.getStringExtra("messageID");
                chatAdapter.updateMessageStatus(id, status, System.currentTimeMillis() + "");
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(MessageReceiver, new IntentFilter("messageStatus"));
    }

    private void StatusRequest() {
        InteractionMessage(conversationID, null, STATUS);
    }

    private void StatusOffline(boolean pause) {
        Message message = new Message();
        message.setConversationID(conversationID);
        message.setSenderToken(getMyToken());
        message.setMessageKind("statusResponse");
        if (pause)
            message.setMessageStatus(MainActivity.STANDBY_S);
        else
            message.setMessageStatus(MainActivity.OFFLINE_S);
//        MessageSender sender = MessageSender.getInstance();
        messageSender.sendMessage(message, getRecipientsTokens());
    }

    private void getRecipientStatus() {
        recipientStatus = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String status = intent.getStringExtra("status");
                if (status != null) {
                    switch (status) {
                        case MainActivity.ONLINE_S:
                            conversationStatus.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.circle_green, getTheme()));
                            break;
                        case MainActivity.STANDBY_S:
                            conversationStatus.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.circle_yellow, getTheme()));
                            break;
                        case MainActivity.OFFLINE_S:
                            conversationStatus.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.circle_red, getTheme()));
                            break;
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(recipientStatus, new IntentFilter("userStatus"));
    }

    private void getImageMessage() {
        imageMessage = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String messageID = intent.getStringExtra("messageID");
                chatAdapter.UpdateMessageImage(messageID);
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(imageMessage, new IntentFilter("DownloadedImage"));
    }

    @Override
    public void onGifClick(Gif gif) {
        this.gif = gif;
        createMessage("",MessageType.gif.ordinal(),getRecipientsNames(),getRecipientsIDs());
        this.gif = null;
    }

    private void resetQuote()
    {
        quotedMessageID = null;
        quotedMessagePosition = -1;
        quoteText.setText("");
        quoteSender.setText("");
        quoteLayout.setVisibility(View.GONE);
        quoteOn = false;
    }
}