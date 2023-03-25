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
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
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
import androidx.lifecycle.Lifecycle;
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
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import Audio.Audio;
import Audio.AudioMessageRecorder;
import Backend.ConversationVM;
import Backend.UserVM;
import BroadcastReceivers.SMSBroadcastSent;
import Consts.ConversationMessageKind;
import Consts.ConversationType;
import Audio.AudioPlayer3;
import Consts.MessageStatus;
import Controller.NotificationsController;
import Fragments.GifBackdropFragment;
import Fragments.ImageFragment;
import NormalObjects.Conversation;
import NormalObjects.ConversationMessage;
import NormalObjects.Gif;
import NormalObjects.ImageButtonState;
import NormalObjects.ImageButtonType;
import NormalObjects.Network2;
import NormalObjects.NetworkChange;
import NormalObjects.Web;
import NormalObjects.OnClick;
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
import java.util.Calendar;
import java.util.Date;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

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
import Time.StandardTime;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings({"Convert2Lambda", "Anonymous2MethodRef"})
public class ConversationActivity2 extends AppCompatActivity implements ChatAdapter.MessageInfoListener, PickerFragment.onPickerClick, BottomSheetFragment.onSheetClicked, BackdropFragment.onBottomSheetAction, Serializable, GifBackdropFragment.onGifView {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private String filePath;

    private ChatAdapter chatAdapter;
    private RecyclerView recyclerView;
    private String conversationID;

    private Uri previewImageUri;
    private ImageView previewImageView;
    private Bitmap imageBitmap;
    private String photoPath;

    private FusedLocationProviderClient client;
    private Geocoder geocoder;
    private LocationCallback locationCallback;
    private String longitude, latitude, gpsAddress;
    //currentUser is the sender always and should not be changed during the lifecycle of the activity
    private final String currentUserID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    private com.vanniktech.emoji.EmojiEditText messageText;

    private final int WRITE_PERMISSION = 3;
    private final int LOCATION_REQUEST = 5;
    private final int CALL_PHONE = 7;
    private User user;
    private List<User> recipients;
    private ImageButtonState playAudioRecordingBtn;
    private ImageButtonType sendMessageBtn, actionBtn;
    private final String PICKER_FRAGMENT_TAG = "Picker_fragment";
    private final String BOTTOM_SHEET_TAG = "BottomSheet_fragment";
    private Message editMessage;
    private TextView quoteText, quoteSender;
    private TextView conversationName;
    private boolean directCall;
    private String recipientPhoneNumber;
    private boolean smsConversation = false;
    private final int PLAY = 1;
    private final int PAUSE = 0;
    private TextView recordingTimeText, recordingTimeLive;
    private SeekBar voiceSeek;
    private Uri fileUri;
    private String link;//,title;
    private ImageView linkedImage;
    private TextView linkTitle, linkContent;
    private boolean messageLongPress = false;
    private Message selectedMessage;
    private final String NULL_ERROR = "something is null";
    private final String ERROR_CASE = "Error in switch case";
    private final String ERROR_WRITE = "write error";
    private LinearLayout searchLayout, imagePreviewLayout, linkMessageLayout, groupCountLayout, userInputLayout, voiceLayout, textLayout;
    private ConstraintLayout quoteLayout;
    private EditText searchText;
    private ArrayList<Integer> indices;
    private int indicesIndex = 0;
    private Uri videoUri;
    private String contactName, contactNumber;
    private ConversationType conversationType = ConversationType.undefined;
    private MessageSender messageSender;
    private Conversation conversation;
    private Toolbar toolbar;
    private ConversationVM model;
    private UserVM userModel;
    private Gif gif;
    private ActivityResultLauncher<Intent> takePicture, addPhoneNumber, openGallery, attachFile, sendContact, sendDoc, video, newRecipients;
    private FloatingActionButton scrollToBot;
    private ProgressBar linkProgressBar, mainProgressBar;
    private AudioPlayer3 audioPlayer3;
    private boolean online;
    private AudioMessageRecorder audioMessageRecorder;
    private int PAUSE_RECORDING = 0, RESUME_RECORDING = 1, RECORD = 2;
    private final String CONVERSATION_ACTIVITY = "ConversationActivity";
    private final String AUDIO_PLAYER = "Audio Player";
    private final String AUDIO_RECORDER = "Audio Recorder";

    private MessageType previewMessageType = MessageType.undefined;

    private BroadcastReceiver delayedMsgReceiver, interactionMsgReceiver, smsReceiver;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EmojiManager.install(new IosEmojiProvider());

        setContentView(R.layout.conversation_layout3);
        messageSender = MessageSender.getInstance();
        LinearLayout layout = findViewById(R.id.root_container);
        toolbar = findViewById(R.id.toolbar1);
        ImageButton backButton = findViewById(R.id.goBack);
        ShapeableImageView conversationImage = findViewById(R.id.conversationImage);
        conversationName = findViewById(R.id.conversationName);
        TextSwitcher typingIndicator = findViewById(R.id.typingIndicator);

        searchLayout = findViewById(R.id.searchLayout);
        searchText = findViewById(R.id.searchText);
        ExtendedFloatingActionButton nextSearch = findViewById(R.id.scrollToNext);

        recyclerView = findViewById(R.id.recycle_view);
        imagePreviewLayout = findViewById(R.id.imagePreviewLayout);
        previewImageView = findViewById(R.id.imagePreview);
        ImageButton closePreviewBtn = findViewById(R.id.closePreviewBtn);

        quoteLayout = findViewById(R.id.quoteLayout);
        quoteSender = findViewById(R.id.senderName);
        quoteText = findViewById(R.id.quoteText);
        ImageButton closeQuoteBtn = findViewById(R.id.closeQuoteBtn);

        linkMessageLayout = findViewById(R.id.linkMessageLayout);
        linkContent = findViewById(R.id.linkContent);
        linkTitle = findViewById(R.id.linkTitle);
        linkedImage = findViewById(R.id.linkImage);
        linkProgressBar = findViewById(R.id.linkProgressBar);
        mainProgressBar = findViewById(R.id.mainProgressBar);
        voiceLayout = findViewById(R.id.voiceLayout);

        playAudioRecordingBtn = findViewById(R.id.play_pause_btn);
        List<Integer>playAudioBtnStates = new ArrayList<>();
        playAudioBtnStates.add(PLAY);
        playAudioBtnStates.add(PAUSE);
        List<Integer>playAudioImages = new ArrayList<>();
        playAudioImages.add(R.drawable.ic_baseline_play_circle_outline_24);
        playAudioImages.add(R.drawable.ic_baseline_pause_circle_outline_24);
        playAudioRecordingBtn.setImages(playAudioImages);
        playAudioRecordingBtn.setBtnStates(playAudioBtnStates);

        voiceSeek = findViewById(R.id.voiceSeek);
        recordingTimeText = findViewById(R.id.recordingTime);

        groupCountLayout = findViewById(R.id.groupCount);
        TextView smsCharCount = findViewById(R.id.smsCharCount);
        scrollToBot = findViewById(R.id.scrollToBot);

        userInputLayout = findViewById(R.id.userInputLayout);
        ImageButton emojiBtn = findViewById(R.id.emojiBtn);
        messageText = findViewById(R.id.messageText);
        actionBtn = findViewById(R.id.actionBtn);
        textLayout = findViewById(R.id.textLayout);

        recordingTimeLive = findViewById(R.id.recordingTimeLive);
        sendMessageBtn = findViewById(R.id.sendMessageBtn);
        setRecipients(new ArrayList<>());
        initBackend();
        initConversation();
        initDB();
        // loads map with possible images for action button
        actionBtn.setTypeBtnClickListener(new ImageButtonType.onTypeButtonClick() {
            @Override
            public void onPress(ButtonType buttonType) {

            }

            @Override
            public void onRelease(ButtonType buttonType) {
                onActionMessageSelected(buttonType);
            }
        });
        audioPlayer3 = new AudioPlayer3();
        voiceSeek.setEnabled(false);
        voiceSeek.setMin(0);
        voiceSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                String time = onTime(i);
                recordingTimeText.setText(time);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                audioPlayer3.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                audioPlayer3.seekTo(seekBar.getProgress());
            }
        });
        playAudioRecordingBtn.setEnabled(false);
        playAudioRecordingBtn.setOnClickListener(new OnClick() {
            @Override
            public void onPress() {

            }

            @Override
            public void onRelease() {
                Log.d(AUDIO_PLAYER, "Start Playing");
                audioPlayer3.playPause();
            }
        });

        audioPlayer3.setListener(new Audio() {
            @Override
            public void onLoad(long duration) {
                playAudioRecordingBtn.setSelected(true);
                playAudioRecordingBtn.setEnabled(true);
                voiceSeek.setEnabled(true);
                voiceSeek.setMax(((int) duration)/1000);
                voiceLayout.setVisibility(View.VISIBLE);
                actionBtn.setCurrentButtonType(ButtonType.cancel);
            }

            @Override
            public void onUnLoad() {
                playAudioRecordingBtn.setSelected(false);
                playAudioRecordingBtn.setEnabled(false);
                voiceSeek.setProgress(0);
                voiceSeek.setEnabled(false);
                voiceLayout.setVisibility(View.GONE);
            }

            @Override
            public void onStart() {
                Log.d(AUDIO_PLAYER, "playRecording - start");
                voiceSeek.setProgress(0,true);
                recordingTimeText.setText(onTime(0));
            }

            @Override
            public void onPause() {
                Log.d(AUDIO_PLAYER, "playRecording - pause");
            }

            @Override
            public void onResume(long progress) {
                Log.d(AUDIO_PLAYER, "playRecording - resume");
//                playAudioRecordingBtn.changeBtnState(PAUSE);
            }

            @Override
            public void onStopped(String fileName) {
                Log.d(AUDIO_PLAYER, "playRecording - stopped");
                playAudioRecordingBtn.changeBtnState(PLAY);
            }

            @Override
            public void onFinished(long duration) {
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(AUDIO_PLAYER, "playRecording - play");
                        playAudioRecordingBtn.changeBtnState(PLAY);
                        voiceSeek.setProgress(((int)duration)/1000, true);
                        recordingTimeText.setText(onTime(duration/1000));
                    }
                });
            }

            @Override
            public void onFailed(String msg) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        onShowToastMessage("an error happened during attempt to play file");
                    }
                });
            }

            @Override
            public void onProgressChange(long progress) {
                int seconds = (int)progress/1000;
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (seconds != 0) {
                            voiceSeek.setProgress(seconds, true);
                            recordingTimeText.setText(onTime(seconds));
                        }
                    }
                });

            }
        });
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayShowTitleEnabled(false);

        audioMessageRecorder = new AudioMessageRecorder();
        audioMessageRecorder.setListener(new Audio() {
            @Override
            public void onLoad(long duration) {

            }

            @Override
            public void onUnLoad() {

            }

            @Override
            public void onStart() {
                messageText.setVisibility(View.GONE);
                recordingTimeLive.setVisibility(View.VISIBLE);
                playSound(R.raw.recording_sound_start);
                onInteractionMessage(-1, MessageAction.recording);
            }

            @Override
            public void onPause() {

            }

            @Override
            public void onResume(long progress) {

            }

            @Override
            public void onStopped(String fileName) {
                messageText.setVisibility(View.VISIBLE);
                recordingTimeLive.setVisibility(View.GONE);
                voiceLayout.setVisibility(View.VISIBLE);
                textLayout.setVisibility(View.GONE);
                playSound(R.raw.recording_sound_stopped);
                audioPlayer3.setDataSource(fileName);
            }

            @Override
            public void onFinished(long duration) {
                onInteractionMessage(-1, MessageAction.not_recording);
            }

            @Override
            public void onFailed(String msg) {
                onShowError(msg);
                onReset();
            }

            @Override
            public void onProgressChange(long progress) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    recordingTimeLive.setText(onTime(progress));
                                }
                            });
            }
        });
        NotificationsController notificationsController = NotificationsController.getInstance();
        notificationsController.removeNotification(conversationID);
        geocoder = new Geocoder(this);
        chatAdapter = new ChatAdapter();
        model.setOnFileUploadListener(new Server.onFileUpload() {
            @Override
            public void onPathReady(long msgID, String path) {
                int index = chatAdapter.findMessageLocation(chatAdapter.getMessages(), 0, chatAdapter.getMessages().size() - 1, msgID);
                Message message = chatAdapter.getMessage(index);
                message.setFilePath(path);
                sendMessage2(message);
            }

            @Override
            public void onStartedUpload(long msgID) {
                //shows progress bar
                int index = chatAdapter.findMessageLocation(chatAdapter.getMessages(), 0, chatAdapter.getMessages().size() - 1, msgID);
                if (index != -1) {
                    chatAdapter.notifyItemChanged(index);
                }
            }

            @Override
            public void onProgress(long msgID, int progress) {

            }

            @Override
            public void onUploadFinished(long msgID) {
                //disables progress bar
                int index = chatAdapter.findMessageLocation(chatAdapter.getMessages(), 0, chatAdapter.getMessages().size() - 1, msgID);
                if (index != -1) {
                    chatAdapter.notifyItemChanged(index);
                }
            }

            @Override
            public void onUploadError(long msgID, String errorMessage) {
                if(getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                    //displays error message and gives option to resend the message file
                    int index = chatAdapter.findMessageLocation(chatAdapter.getMessages(), 0, chatAdapter.getMessages().size() - 1, msgID);
                    Message message = chatAdapter.getMessage(index);
                    if (index != -1) {
                        chatAdapter.notifyItemChanged(index);
                    }
                    onShowError("an error occurred while sending the file");
                    sendMessage2(message);
                }
            }
        });
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
                if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == chatAdapter.getItemCount() - 1)
                    scrollToBot.setVisibility(View.GONE);
                else scrollToBot.setVisibility(View.VISIBLE);
            }
        });
        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);

        sendMessageBtn.setSelected(true);
        sendMessageBtn.setOnFullClickListener(new OnClick() {
            @Override
            public void onPress() {
                if (sendMessageBtn.getCurrentButtonType() == ButtonType.microphone)
                {
                    recordOrStop(RECORD);
                }
            }

            @Override
            public void onRelease() {
                if (sendMessageBtn.getCurrentButtonType() == ButtonType.microphone)
                {
                    recordOrStop(RECORD);
                }
                else if (sendMessageBtn.getCurrentButtonType() == ButtonType.sendMessage)
                {
                    if (editMessage != null)
                        onInteractionMessage(editMessage.getMessageID(), MessageAction.edit_message);
                    else
                    {
                        if (previewMessageType != MessageType.undefined)
                            fullMessageProcess(previewMessageType);
                        else
                            fullMessageProcess(MessageType.textMessage);
                    }
                    playSound(R.raw.send_message_sound);
                    onReset();
                }
            }
        });
        messageText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() == 1 && before == 0)//start typing
                {
                    sendMessageBtn.setCurrentButtonType(ButtonType.sendMessage);
                    actionBtn.setCurrentButtonType(ButtonType.cancel);
                    if (chatAdapter.getItemCount() > 0) {
                        onInteractionMessage(-1,MessageAction.typing);
                    }
                    Log.d(CONVERSATION_ACTIVITY, "new text starting from 0 was entered");
                }
                else if(s.toString().length() == 0)//no typing
                {
                    sendMessageBtn.setCurrentButtonType(ButtonType.microphone);
                    onInteractionMessage(-1, MessageAction.not_typing);
                    actionBtn.setCurrentButtonType(actionBtn.getPreviousButtonType());
                    Log.d(CONVERSATION_ACTIVITY, "no more text in text area");
                }
                else if (s.toString().length() > 0) {
                    sendMessageBtn.setCurrentButtonType(ButtonType.sendMessage);
                    actionBtn.setCurrentButtonType(ButtonType.cancel);
                    Log.d(CONVERSATION_ACTIVITY, "changed actionBtn to cancel after a chunk of text was inserted");
                }

                if (smsConversation && count >= 140) {
                    smsCharCount.setVisibility(View.VISIBLE);
                    int msgAmount = count % 160;
                    String charCount = "(" + msgAmount + ") " + count + "";
                    smsCharCount.setText(charCount);
                }
                Log.d(CONVERSATION_ACTIVITY, "on text change was called");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (smsConversation) {
                    if (s.toString().isEmpty() || s.toString().length() < 140)
                        smsCharCount.setVisibility(View.GONE);
                }
                Log.d(CONVERSATION_ACTIVITY, "after text changed is called");
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
        chatAdapter.setCurrentUserUID(currentUserID);
        chatAdapter.setListener(this);
        scrollToBot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollToBot.setVisibility(View.GONE);
                recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
            }
        });
        messageSender.setMessageListener(new MessageSender.onMessageSent() {
            @Override
            public void onMessageSentSuccessfully(Message message) {
                Log.d(CONVERSATION_ACTIVITY, "message sent successfully");
                message.setMessageStatus(MessageStatus.SENT.ordinal());
                updateMessage(message);
                user.setMsgSentAmount(user.getMsgSentAmount() + 1);
                updateUser(user);
            }

            @Override
            public void onMessagePartiallySent(Message message, List<String> tokens, String error) {
                Log.d(CONVERSATION_ACTIVITY, "message wasn't sent to all recipients");
            }

            @Override
            public void onMessageNotSent(Message message, String error) {
                Log.d(CONVERSATION_ACTIVITY, "message wasn't sent");
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
                                userModel.saveUser(user);
                                newRecipients.add(user.getUserUID());
                            }
                            model.createNewGroup(conversationID, newRecipients);
                            setRecipients(recipients);
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
                    onShowPreview(MessageType.photoMessage, photoPath);
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
                        ContentResolver resolver = ConversationActivity2.this.getApplicationContext().getContentResolver();
                        resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        if (uri != null) {
                            onShowPreview(MessageType.imageMessage, uri.toString());
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
                                updateUser(recipients.get(0));
//                                userModel.updateUser(recipients.get(0));
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
                onShowPreview(MessageType.fileMessage, "");
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
                                cursor.close();
                                fullMessageProcess(MessageType.contact);
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
                            onShowPreview(MessageType.document, docUri.toString());
//                            File file = new File(docUri.getPath());
//                            if (file.exists()) {
//                                try {
//                                    ParcelFileDescriptor descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
//                                    PdfRenderer renderer = new PdfRenderer(descriptor);
//                                    PdfRenderer.Page page = renderer.openPage(0);
//                                    Bitmap docBitmap = BitmapFactory.decodeFileDescriptor(descriptor.getFileDescriptor());
//                                    page.render(docBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
//
//                                    imagePreviewLayout.setVisibility(View.VISIBLE);
//                                    previewImageView.setImageBitmap(docBitmap);
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            }
                        }
                    }
                }
            }
        });
        video = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                onShowPreview(MessageType.videoMessage, "");
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
        nextSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (indices == null) {
                    String searchQuery = searchText.getText().toString();
                    indices = chatAdapter.SearchMessage(searchQuery);
                }
                if (indices != null) {
                    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);
                            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                            if (manager != null) {
                                View view = manager.findViewByPosition(indices.get(indicesIndex));
                                markMessage(view);
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

        MessageTouch touch = new MessageTouch(ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.START | ItemTouchHelper.END);
        touch.setListener(new TouchListener() {
            @Override
            public void onSwipe(@NonNull RecyclerView.ViewHolder viewHolder, int swipeDirection) {
                viewHolder.getAdapterPosition();
//                TextView message = viewHolder.itemView.findViewById(R.id.message);
                Message quoteMessage = chatAdapter.getMessage(viewHolder.getAdapterPosition());
                quoteLayout.setVisibility(View.VISIBLE);
                quoteText.setText(quoteMessage.getContent());
                quoteSender.setText(quoteMessage.getSenderName());
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
        actionBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //opens bottom sheet do display extra options
                BottomSheetFragment bottomSheetFragment = BottomSheetFragment.newInstance();
                bottomSheetFragment.show(getSupportFragmentManager(), BOTTOM_SHEET_TAG);
                return true;
            }
        });
        closePreviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagePreviewLayout.setVisibility(View.GONE);
                setPreviewMessageType(MessageType.undefined);
            }
        });
        conversationImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conversationType == ConversationType.single)
                {
                    Intent profileIntent = new Intent(ConversationActivity2.this, ProfileActivity2.class);
                    profileIntent.putExtra("user", recipients.get(0));
                    startActivity(profileIntent);
                }
                else if (conversationType == ConversationType.group)
                {
                    Intent openGroupIntent = new Intent(ConversationActivity2.this, GroupActivity.class);
                    openGroupIntent.putExtra("conversationID", conversationID);
                    openGroupIntent.putExtra("recipients", (ArrayList<User>)recipients);
                    startActivity(openGroupIntent);//opens group profile
                }
//                else {
//                    if (recipients.size() > 1) {
//                        Intent openGroupIntent = new Intent(ConversationActivity2.this, GroupActivity.class);
//                        openGroupIntent.putExtra("conversationID", conversationID);
//                        startActivity(openGroupIntent);//opens group profile
//                    } else if (recipients.size() == 1) {
//                        Intent openRecipientIntent = new Intent(ConversationActivity2.this, ProfileActivity2.class);
//                        openRecipientIntent.putExtra("user", recipients.get(0));//opens single user profile activity
//                        openRecipientIntent.putExtra("conversationID", conversationID);
//                        startActivity(openRecipientIntent);
//                    }
//                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                sendMessageBtn.setCurrentButtonType(ButtonType.microphone);
                onReset();
            }
        });
        SharedPreferences sharedPreferences = getSharedPreferences("background", Context.MODE_PRIVATE);
        int presetBackground = sharedPreferences.getInt("backgroundImage normal", -1);
        if (presetBackground != -1) {
            Drawable drawable = ResourcesCompat.getDrawable(getResources(), presetBackground, getTheme());
            recyclerView.setBackground(drawable);
        }

        onDelayedMessageBroadcast();
        onInteractionMessageReceived();
    }

    private void sendSMS(Message message) {
        if (askPermission(MessageType.sms))
            smsSendMessage(message);
    }

    private void smsSendMessage(Message message) {
        Log.d("smsMessageID", message.getMessageID()+"");
//        MessageSender sender = MessageSender.getInstance();
        messageSender.sendMessage(message, recipientPhoneNumber, this);
    }

    private void listenToSMSStatus() {
        BroadcastReceiver smsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long messageID = intent.getLongExtra("messageID", -1);
//                String status = intent.getStringExtra("status");
                MessageStatus status = MessageStatus.READ;
                chatAdapter.updateMessageStatus(messageID, status, System.currentTimeMillis() + "");
//                if (status != null && status.equals(MESSAGE_DELIVERED))
//                    markAsRead(messageID);
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(smsReceiver, new IntentFilter(SMSBroadcastSent.SENT_SMS_STATUS));
    }

    private void onShowError(String error)
    {
        Log.d(CONVERSATION_ACTIVITY, "show error: " + error);
        AlertDialog.Builder builder = new AlertDialog.Builder(ConversationActivity2.this);
        builder.setTitle(getResources().getString(R.string.error))
                .setMessage(error)
                .setIcon(R.drawable.ic_baseline_error_24)
                .setCancelable(true)
                .setNeutralButton("ok", null)
                .create()
                .show();
    }

    private void onShowToastMessage(String message)
    {
        Log.d(CONVERSATION_ACTIVITY, "on show toast msg: " + message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    private void onSMSReceived()
    {
        smsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(CONVERSATION_ACTIVITY, "received sms");
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(smsReceiver, new IntentFilter(conversationID));
    }


    private void onDelayedMessageBroadcast()
    {
        delayedMsgReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Message message = (Message) intent.getSerializableExtra("message");
                showMessageOnScreen(message, message.getMessageAction());
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(delayedMsgReceiver, new IntentFilter("delayedMessage"));
    }

    private void onInteractionMessageReceived()
    {
        interactionMsgReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra("typing"))
                {
                    Log.d(CONVERSATION_ACTIVITY, "received typing");
                }
                else if (intent.hasExtra("recording"))
                {
                    Log.d(CONVERSATION_ACTIVITY, "received recording");
                }
                else if (intent.hasExtra("delete"))
                {
                    Log.d(CONVERSATION_ACTIVITY, "received delete");
                }
                else if (intent.hasExtra("edit"))
                {
                    Log.d(CONVERSATION_ACTIVITY, "received edit");
                }
                else if (intent.hasExtra("status"))
                {
                    Log.d(CONVERSATION_ACTIVITY, "received status");
                }
                else if (intent.hasExtra("leave_conversation"))
                {
                    Log.d(CONVERSATION_ACTIVITY, "received leave");
                }
                else if (intent.hasExtra("messageStatus"))
                {
                    Log.d(CONVERSATION_ACTIVITY, "received message status");
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(interactionMsgReceiver, new IntentFilter(conversationID));
    }

    @Override
    protected void onStart() {
        super.onStart();
        onInternetConnection();
    }

    private String onTime(long seconds)
    {
        Log.d("onRecordingTimeChange - start: ",seconds + "");
        String min, sec;
        long minutes = seconds/60;
        if (minutes > 0)
        {
            seconds = seconds % 60;
            if(minutes < 10)
                min = "0" + minutes;
            else min = String.valueOf(minutes);
        }
        else{
            min = "00";
        }
        if (seconds < 10)
            sec = "0" + seconds;
        else
            sec = String.valueOf(seconds);
        Log.d("onRecordingTimeChange - end: ",min + ":" + sec);
        return min + ":" + sec;
    }

    private void onReset()
    {
        userInputLayout.setVisibility(View.VISIBLE);
        messageText.setText("");
        quoteLayout.setVisibility(View.GONE);
        voiceLayout.setVisibility(View.GONE);
        textLayout.setVisibility(View.VISIBLE);
        linkMessageLayout.setVisibility(View.GONE);
        imagePreviewLayout.setVisibility(View.GONE);
        if (selectedMessage != null)
            onDeselectMessage();
    }

    private void recordOrStop(int recordingState)
    {
        if (askPermission(MessageType.voiceMessage))
            if (recordingState == PAUSE_RECORDING)
            {
                audioMessageRecorder.onPauseRecording();
            }
            else if (recordingState == RESUME_RECORDING)
            {
                audioMessageRecorder.onResumeRecording();
            }
            else if (recordingState == RECORD)
            {
                if (audioMessageRecorder.isRecording())
                    audioMessageRecorder.onStopRecording();
                else {
                    String fileName = getExternalCacheDir().getAbsolutePath() + "/audioRecording_" + System.currentTimeMillis() + ".3pg";
                    audioMessageRecorder.startRecording(fileName);
                }
            }
    }

    private String getMessage()
    {
        Editable txt = messageText.getText();
        if(txt != null)
            return txt.toString();
        else return "";
    }

    private void setCurrentUserID(User user) {
        Log.d(CONVERSATION_ACTIVITY, "current user is set");
        ConversationActivity2.this.user = user;
    }

    private void setRecipientPhoneNumber(String phoneNumber) {
        this.recipientPhoneNumber = phoneNumber;
    }

    private void setConversation(Conversation conversation)
    {
        Log.d(CONVERSATION_ACTIVITY, "set conversation");
        conversation.setUnreadMessages(0);
        this.conversation = conversation;
        setConversationName(conversation.getConversationName());
        setConversationType(ConversationType.values()[conversation.getConversationType()]);
    }

    private void setConversationName(String conversationName)
    {
        Log.d(CONVERSATION_ACTIVITY, "set conversation name");
        this.conversationName.setText(conversationName);
    }

    private void setConversationType(ConversationType conversationType) {
        this.conversationType = conversationType;
        if (conversationType == ConversationType.sms)
            listenToSMSStatus();
//        Log.d(CONVERSATION_ACTIVITY, "conversation type was set to: " + conversationType.name());
//        switch (conversationType)
//        {
//            case sms:
//                toolbar.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark, getTheme()));
//                toolbar.setPopupTheme(R.style.sms);
//                listenToSMSStatus();
//                actionBtn.setVisibility(View.GONE);
//                break;
//            case group:
//                toolbar.setBackgroundColor(getResources().getColor(android.R.color.holo_purple, getTheme()));
//                toolbar.setPopupTheme(R.style.group);
//                break;
//            case single:
//                toolbar.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light, getTheme()));
//                toolbar.setPopupTheme(R.style.single);
//                break;
//            default:
//                Log.e(CONVERSATION_ACTIVITY, "unsupported conversation type");
//        }
//        invalidateOptionsMenu();
    }

    private void setRecipients(List<User> recipients) {
        Log.d(CONVERSATION_ACTIVITY, "set recipients");
        this.recipients = recipients;
        for (User user: recipients)
        {
            getRecipientToken(user.getUserUID());
        }

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
//        sendMessageBtn.changeBtnState(SEND_MESSAGE);
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
                public void onFileDownloadFinished(long messageID, File file) {

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
        }
//        else conversationImage.setImageBitmap(bitmap);
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
        if (askPermission(MessageType.imageMessage))
            takePicture();
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
        showHideProgress();
        RetrofitJoke client = RetrofitClient.getRetrofitClient("https://api.chucknorris.io").create(RetrofitJoke.class);
        client.sendJokeRequest().enqueue(new Callback<Joke>() {
            @Override
            public void onResponse(@NonNull Call<Joke> call, @NonNull Response<Joke> response) {
                showHideProgress();
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
        String failedActionBtn =  "failed setUpBySettings";
        String chosenActionBtn = preferences.getString("ActionButton", "failed setUpBySettings");
        if (!chosenActionBtn.equals(failedActionBtn))
        {
            ButtonType type = ButtonType.valueOf(chosenActionBtn.toLowerCase());
            actionBtn.setCurrentButtonType(type);
        }
        directCall = preferences.getBoolean("directCall", false);
        Log.d(ACCESSIBILITY_SERVICE, "direct call: " + directCall);
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

        if (smsConversation)
            sendMessageBtn.setCurrentButtonType(ButtonType.sendMessage);
         else  actionBtn.setCurrentButtonType(type);
    }

    private void playSound(int soundID)
    {
        new AudioPlayer3(getApplicationContext(), soundID);
    }

    private Message prepareMessage(MessageType messageType) {
        String messageContent = getMessage();
        Message message = new Message();
        message.setContent(messageContent);
        message.setMessageType(messageType.ordinal());
        message.setSenderID(currentUserID);
        message.setSenderName("Hanan");
        message.setConversationName(conversation.getConversationName());
        message.setSendingTime(System.currentTimeMillis());
        message.setMessageStatus(MessageStatus.WAITING.ordinal());
        //todo
        message.setMessageID(StandardTime.getInstance().getStandardTime());
        message.setConversationID(conversationID);
        message.setMessageAction(MessageAction.new_message.ordinal());
        MessageType type = MessageType.values()[messageType.ordinal()];
        switch (type) {
            case textMessage:
                if (Patterns.WEB_URL.matcher(messageContent).matches())
                    message.setContent(messageContent);
                break;
            case gpsMessage:
                message.setLatitude(latitude);
                message.setLongitude(longitude);
                message.setAddress(gpsAddress);
                break;
            case imageMessage://photo from gallery
                message.setFilePath(previewImageUri.toString());
                model.uploadFile(currentUserID, message.getMessageID(), imageBitmap, ConversationActivity2.this);
                break;
            case photoMessage://image from camera
                message.setFilePath(photoPath);
                model.uploadFile(currentUserID, message.getMessageID(), Bitmap.createScaledBitmap(BitmapFactory.decodeFile(photoPath), 500, 450, false), ConversationActivity2.this);
                break;
            case voiceMessage:
                message.setFilePath(fileUri.toString());
                model.uploadFile(message.getMessageID(), fileUri, ConversationActivity2.this);
                break;
            case videoMessage:
                message.setFilePath(videoUri.toString());
                model.uploadFile(message.getMessageID(), videoUri, ConversationActivity2.this);
                break;
            case gif:
                message.setContent(gif.getUrl());
                break;
            case contact:
                message.setContactName(contactName);
                message.setContactNumber(contactNumber);
                break;
            default:
                Log.e(ERROR_CASE, "onMessageReady: UNHANDLED CASE: " + messageType);
                break;
        }
        return message;
    }

//    private void sendRecording() {
//        createFileUri(filePath);
//        createMessage("recording", MessageType.voiceMessage.ordinal());
//    }

    private void createFileUri(String filePath) {
        File file = new File(filePath);
        fileUri = Uri.fromFile(file);
    }

    @Override
    public void onMessageClick(Message message, View view, int viewType) {
        if (message.getMessageType() == MessageType.contact.ordinal()) {
            Intent createNewContact = new Intent(ContactsContract.Intents.Insert.ACTION);
            createNewContact.setType(ContactsContract.RawContacts.CONTENT_TYPE);
            createNewContact.putExtra(ContactsContract.Intents.Insert.NAME, message.getContactName());
            createNewContact.putExtra(ContactsContract.Intents.Insert.PHONE, message.getContactNumber());
            startActivity(createNewContact);
        }
        if (messageLongPress) {
            messageLongPress = false;
            invalidateOptionsMenu();
            Drawable drawable;
            if (message.getSenderID().equals(currentUserID)) {
                drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.outgoing_message_look, getTheme());
            } else {
                drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.incoming_message_look, getTheme());
            }
            view.setBackground(drawable);

        } else if (message.getQuoteMessage() != null) {
            //this is a quoted message
            String quotedMessageID = message.getQuoteID();
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
                                    markMessage(view1);
                                }
                                recyclerView.removeOnScrollListener(this);
                            }
                        }
                    });
                    //recyclerView.scrollToPosition(index);
                } else
                    Toast.makeText(this, "couldn't fine this quoted message", Toast.LENGTH_SHORT).show();
            }
        } else if (message.getContent() != null && Patterns.WEB_URL.matcher(message.getContent()).matches()) {
            Intent openWebSite = new Intent(Intent.ACTION_VIEW);
            openWebSite.setData(Uri.parse(message.getContent()));
            startActivity(openWebSite);
        }
    }

    private void markMessage(View view) {
        if (view != null) {
            view.setSelected(true);
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    view.setSelected(false);
                }
            }, 2500);
        }
    }

    @Override
    public void onMessageLongClick(Message message, View view, int viewType) {
        messageLongPress = !messageLongPress;
        selectedMessage = message;
        invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (messageLongPress)
        {
            menu.setGroupVisible(R.id.selectedMessageOptions, true);
            menu.setGroupVisible(R.id.singlePersonConversation, false);
            menu.setGroupVisible(R.id.groupConversationOptions, false);
            menu.setGroupVisible(R.id.allConversationsOptions, false);
            Log.d(CONVERSATION_ACTIVITY, "option menu - on selected message options were set");
        }
        else
        {
            menu.setGroupVisible(R.id.allConversationsOptions, true);
            menu.setGroupVisible(R.id.selectedMessageOptions, false);
            if (conversationType == ConversationType.single)
            {
                menu.setGroupVisible(R.id.singlePersonConversation, true);
                menu.findItem(R.id.addPhoneNumber).setVisible(true);
                menu.setGroupVisible(R.id.groupConversationOptions, false);
            }
            else if (conversationType == ConversationType.group)
            {
                menu.setGroupVisible(R.id.groupConversationOptions, true);
                menu.setGroupVisible(R.id.singlePersonConversation, false);
            }
            else if (conversationType == ConversationType.sms)
            {
                menu.setGroupVisible(R.id.singlePersonConversation, true);
                menu.setGroupVisible(R.id.groupConversationOptions, false);
                menu.findItem(R.id.addPhoneNumber).setVisible(false);
            }
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
        else if (message.getMessageType() == MessageType.photoMessage.ordinal() || message.getMessageType() == MessageType.imageMessage.ordinal())
        {
            ImageFragment imageFragment = new ImageFragment();
            Bundle bundle = new Bundle();
            bundle.putString("image", message.getFilePath());
            imageFragment.setArguments(bundle);
            imageFragment.show(getSupportFragmentManager(), "imageFragment");
        }
    }

    @Override
    public void onRadioBtnClick(RadioGroup group, int radioBtnPosition) {
    }


    //saves image to local storage
    @Override
    public String onImageDownloaded(Bitmap bitmap, Message message) {
        FileManager fileManager = FileManager.getInstance();
        String path = fileManager.saveImage(bitmap, this);
        message.setFilePath(path);
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
                    message.setFilePath(path);
                    UpdateDataBase(message);
                    File imageDirectory = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
                    File videoFile = new File(imageDirectory, fileName);
                    try {
                        out = new FileOutputStream(videoFile);
                        message.setFilePath(videoFile.getAbsolutePath());
                        UpdateDataBase(message);
                        path = videoFile.getAbsolutePath();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }finally {
                        out.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            File imageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
            File videoFile = new File(imageDirectory, fileName);
            try {
                out = new FileOutputStream(videoFile);
                message.setFilePath(videoFile.getAbsolutePath());
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
        String VIDEO_FRAGMENT_TAG = "VIDEO_FRAGMENT";
        if (manager.findFragmentByTag(VIDEO_FRAGMENT_TAG) == null) {
            transaction.add(R.id.contentContainer, videoFragment, VIDEO_FRAGMENT_TAG);
        } else {
            transaction.replace(R.id.contentContainer, videoFragment, VIDEO_FRAGMENT_TAG);
        }
        transaction.commit();
    }

    @Override
    public void onRetrySending(String messageID, String imagePath) {
        int index = chatAdapter.findMessageLocation(chatAdapter.getMessages(), 0, chatAdapter.getMessages().size() - 1, Long.parseLong(messageID));
        if (index != -1) {
            Message message = chatAdapter.getMessage(index);
            messageSender.sendMessage(message, getRecipientsTokens());
            chatAdapter.notifyItemChanged(index);
        }

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences conversationPreferences = getSharedPreferences("Conversation", MODE_PRIVATE);
        SharedPreferences.Editor editor = conversationPreferences.edit();
        editor.putString("liveConversation", "noConversation");
        editor.apply();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(delayedMsgReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(interactionMsgReceiver);
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
            case photoMessage: case imageMessage:{
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
                        callPhone();
                    else
                        Toast.makeText(this, "permission is required to make phone calls", Toast.LENGTH_SHORT).show();
                    break;
                }
                case REQUEST_RECORD_AUDIO_PERMISSION:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    {
                        Toast.makeText(this, "great! to record press and hold the recording button", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(this, "permission required to record audio", Toast.LENGTH_SHORT).show();
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

    private void showHideProgress()
    {
        if (mainProgressBar.getVisibility() == View.VISIBLE)
            mainProgressBar.setVisibility(View.GONE);
        else
            mainProgressBar.setVisibility(View.VISIBLE);
    }

    @SuppressLint("MissingPermission")
    private void findLocation() {
        showHideProgress();
        client = LocationServices.getFusedLocationProviderClient(ConversationActivity2.this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                showHideProgress();
                Location lastLocation = locationResult.getLastLocation();
                longitude = lastLocation.getLongitude() + "";
                latitude = lastLocation.getLatitude() + "";
                try {
                    List<Address> addresses = geocoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1);
                    Address address = addresses.get(0);
                    gpsAddress = address.getAddressLine(0);
                    client.removeLocationUpdates(locationCallback);
                    fullMessageProcess(MessageType.gpsMessage);
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

    private void onInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI).addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_VPN);
        NetworkRequest request = builder.build();
        Network2 network2 = Network2.getInstance();
        //Network network = new Network();
        network2.setListener(new NetworkChange() {
            @Override
            public void onNetwork() {
                if (!online) {
                    online = true;
                    onConnectivityChange(true);
                }

            }

            @Override
            public void onNoNetwork() {
                online = false;
                onConnectivityChange(false);
            }

            @Override
            public void onNetworkLost() {
                //networkConnection = false;
                onConnectivityChange(false);
            }

            @Override
            public void onChangedNetworkType() {
                online = true;
                onConnectivityChange(true);
            }
        });
        //ConnectivityManager.NetworkCallback networkCallback = network;
        if (connectivityManager != null)
            connectivityManager.registerNetworkCallback(request, network2);

    }

    private void onConnectivityChange(boolean online)
    {
        sendMessageBtn.setSelected(online);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    @Override
    public void onPicked(int[] time, String content) {
        messageText.setText(content);
        Message message = prepareMessage(MessageType.textMessage);
        messageText.setText("");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, time[0]);
        calendar.set(Calendar.MONTH, time[1]);
        calendar.set(Calendar.DAY_OF_MONTH, time[2]);
        calendar.set(Calendar.HOUR_OF_DAY, time[3]);
        calendar.set(Calendar.MINUTE, time[4]);
        calendar.set(Calendar.SECOND, 0);
        Intent foreground = new Intent(this, TimedMessageService.class);
        foreground.putExtra("message", message);
        foreground.putExtra("tokens", (ArrayList<String>) getRecipientsTokens());
        foreground.putExtra("time", calendar.getTimeInMillis());
        startForegroundService(foreground);
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
        setCorrectBtn(buttonType);
        DialogFragment fragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(BOTTOM_SHEET_TAG);
        if (fragment != null)
            fragment.dismiss();
        onActionMessageSelected(buttonType);
    }

    private void onActionMessageSelected(ButtonType buttonType)
    {
        Log.d(CONVERSATION_ACTIVITY, "called onActionMessageSelected");
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
            case joke: {
                onJoke();
                break;
            }
            case gif: {
                GifBackdropFragment backdropFragment = GifBackdropFragment.newInstance();
                backdropFragment.show(getSupportFragmentManager(), "BOTTOM_SHEET_GIF_TAG");
                break;
            }
            case poll: {
                Log.d(CONVERSATION_ACTIVITY, "POLL!!!!");
                break;
            }
            case undefined: {
                Log.e(CONVERSATION_ACTIVITY, "action button type is undefined");
                break;
            }
            case cancel: {
                audioPlayer3.releasePlayer();
                playAudioRecordingBtn.setSelected(false);
                playAudioRecordingBtn.setEnabled(false);
                voiceSeek.setEnabled(false);
                onReset();
                break;
            }
            default:
                Log.e(ERROR_CASE, "onActionMessageSelected error " + new Throwable().getStackTrace()[0].getLineNumber());
        }
        if (buttonType != ButtonType.location && buttonType != ButtonType.delay)
            sendMessageBtn.setCurrentButtonType(ButtonType.sendMessage);
    }

    private void setPreviewMessageType(MessageType messageType)
    {
        Log.d(CONVERSATION_ACTIVITY, "setting preview message type: " + messageType.name());
        previewMessageType = messageType;
    }

    public void onShowPreview(MessageType messageType, String info)
    {
        Log.d(CONVERSATION_ACTIVITY, "on show message preview: " + messageType.name());
        setPreviewMessageType(messageType);
        switch (messageType)
        {
            case imageMessage:
                Uri imageUri = Uri.parse(info);
                previewImageUri = imageUri;
                imagePreviewLayout.setVisibility(View.VISIBLE);
                previewImageView.setImageURI(imageUri);
                imageBitmap = getImageBitmap(imageUri);
                previewImageView.setImageBitmap(imageBitmap);
                imageBitmap = Bitmap.createScaledBitmap(imageBitmap, 500, 450, false);
                break;
            case photoMessage:
                imagePreviewLayout.setVisibility(View.VISIBLE);
                Drawable drawable = Drawable.createFromPath(info);
                previewImageView.setImageDrawable(drawable);
                break;
            case document:
                Uri docUri = Uri.parse(info);
                File file = new File(docUri.getPath());
                if (file.exists()) {
                    try {
                        ParcelFileDescriptor descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
                        PdfRenderer renderer = new PdfRenderer(descriptor);
                        PdfRenderer.Page page = renderer.openPage(0);
                        Bitmap docBitmap = BitmapFactory.decodeFileDescriptor(descriptor.getFileDescriptor());
                        page.render(docBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                        imagePreviewLayout.setVisibility(View.VISIBLE);
                        previewImageView.setImageBitmap(docBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
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

    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
//        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        openGallery.launch(Intent.createChooser(galleryIntent, "Select Picture to Upload"));
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

                previewImageUri = photoURI;
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
        menu.setGroupVisible(R.id.allConversationsOptions, true);
        menu.setGroupVisible(R.id.selectedMessageOptions, false);
        switch (conversationType)
        {
            case single:
                menu.setGroupVisible(R.id.singlePersonConversation, true);
                menu.findItem(R.id.addPhoneNumber).setVisible(true);
                menu.setGroupVisible(R.id.groupConversationOptions, false);
                break;
            case group:
                menu.setGroupVisible(R.id.groupConversationOptions, true);
                menu.setGroupVisible(R.id.singlePersonConversation, false);
                break;
            case sms:
                menu.setGroupVisible(R.id.singlePersonConversation, true);
                menu.setGroupVisible(R.id.groupConversationOptions, false);
                menu.findItem(R.id.addPhoneNumber).setVisible(false);
                break;
        }
        return super.onCreateOptionsMenu(menu);
    }

    //extra options
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.callBtn) {
            callPhone();
            //opening dialer to call the recipient number if exists
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
            Button saveBtn = builderView.findViewById(R.id.saveBtn);
            Button cancelBtn = builderView.findViewById(R.id.cancelBtn);
            Button fromContacts = builderView.findViewById(R.id.add);
            EditText text = builderView.findViewById(R.id.editTextDialog);
            AlertDialog alert = builder.setTitle("Add a phone number to this contact")
                    .setMessage("choose a phone number from contacts or type one")
                    .setCancelable(true)
                    .setView(builderView).create();
            alert.show();
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alert.dismiss();
                }
            });
            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String phoneNumber = text.getText().toString();
                    if (phoneNumber.isEmpty())
                        Toast.makeText(ConversationActivity2.this, "phone number is missing", Toast.LENGTH_SHORT).show();
                    else {
                        recipients.get(0).setPhoneNumber(phoneNumber);
                        updateUser(recipients.get(0));
//                        userModel.updateUser(recipients.get(0));
                    }
                }
            });
            fromContacts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent addPhoneNumberIntent = new Intent(Intent.ACTION_PICK);
                    addPhoneNumberIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                    if (addPhoneNumberIntent.resolveActivity(getPackageManager()) != null)
                        addPhoneNumber.launch(addPhoneNumberIntent);
                    alert.dismiss();
                }
            });
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
                sendIntent.putExtra(Intent.EXTRA_TEXT, selectedMessage.getContent());
                sendIntent.setType("text/plain");
                Intent shareIntent = Intent.createChooser(sendIntent, null);
                startActivity(shareIntent);
            }
        } else if (item.getItemId() == R.id.messageInfo) {
            if (selectedMessage != null) {
                BackdropFragment backdropFragment = BackdropFragment.newInstance();
                Bundle backDropBundle = new Bundle();
                final String conversationType = "conversationType";
                backDropBundle.putSerializable("message", selectedMessage);
                backDropBundle.putInt(conversationType, this.conversationType.ordinal());
                backdropFragment.setArguments(backDropBundle);
                backdropFragment.show(getSupportFragmentManager(), BOTTOM_SHEET_TAG);
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
                ClipData clipData = ClipData.newPlainText("message", selectedMessage.getContent());
                if (clipboardManager != null) {
                    clipboardManager.setPrimaryClip(clipData);
                    onShowToastMessage("Copied message");
                }
            }
        } else if (item.getItemId() == R.id.addNewMember) {
            Intent intent = new Intent(ConversationActivity2.this, NewConversationActivity.class);
            intent.putExtra("recipients", (ArrayList<User>) recipients);
            newRecipients.launch(intent);
        } else if (item.getItemId() == R.id.leaveGroup) {
            onInteractionMessage(-1, MessageAction.leave_group);
            Log.d(CONVERSATION_ACTIVITY, "leave group");
        }
        else if (item.getItemId() == R.id.edit)
        {
            messageText.setText(selectedMessage.getContent());
            editMessage = selectedMessage;
            Log.d(CONVERSATION_ACTIVITY, "edit msg");
        }
        else if (item.getItemId() == R.id.delete)
        {
            onInteractionMessage(selectedMessage.getMessageID(), MessageAction.delete_message);
            Log.d(CONVERSATION_ACTIVITY, "delete msg");
        }
        else
            Log.e(ERROR_CASE, "menu error");

        return super.onOptionsItemSelected(item);
    }

    private void callPhone() {
        Log.d(CONVERSATION_ACTIVITY, "call phone was selected");
        Intent callRecipientIntent = null;
        if (directCall) //preferences option
        {
            Log.d(CONVERSATION_ACTIVITY, "direct call was chosen");
            if (askPermission(MessageType.callPhone))
                callRecipientIntent = new Intent(Intent.ACTION_CALL);
        }
        else {
            Log.d(CONVERSATION_ACTIVITY, "dial call was chosen");
            callRecipientIntent = new Intent(Intent.ACTION_DIAL);
        }
        if (callRecipientIntent != null) {
            callRecipientIntent.setData(Uri.parse("tel:0547671248"));
//            callRecipientIntent.setData(Uri.parse("tel:" + recipients.get(0).getPhoneNumber()));
            if (callRecipientIntent.resolveActivity(getPackageManager()) != null)
                startActivity(callRecipientIntent);
        }
    }

    private void InsertToDataBase(Message message) {
        model.isMessageExists(message).observe(this, new Observer<Boolean>() {
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

    private void fullMessageProcess(MessageType type)
    {
        Log.d(CONVERSATION_ACTIVITY, "full conversation msg");
        boolean fileMessage = type == MessageType.fileMessage || type == MessageType.imageMessage || type == MessageType.photoMessage || type == MessageType.voiceMessage || type == MessageType.videoMessage;
        Message message = prepareMessage(type);
        if (!fileMessage)
            sendMessage2(message);
        initNewConversation(message);
        showMessageOnScreen(message, message.getMessageAction());
        saveMessage(message);
        setPreviewMessageType(MessageType.undefined);
    }

    private void sendMessage2(@NonNull Message message) {
        Log.d(CONVERSATION_ACTIVITY, "sendMessage2");
        if (message.getMessageType() == MessageType.sms.ordinal()) {
            sendSMS(message);
        } else {
            if (online) {
                String token = getMyToken();
                message.setSenderToken(token);
                List<String> recipientsTokens = getRecipientsTokens();
                message.setMessageAction(MessageAction.new_message.ordinal());
                messageSender.sendMessage(message, recipientsTokens);
            } else {
                message.setMessageStatus(MessageStatus.WAITING.ordinal());
            }
        }
        setPreviewMessageType(MessageType.undefined);
    }

    private void initNewConversation(Message message)
    {
        Log.d(CONVERSATION_ACTIVITY, "init conversation");
        if (chatAdapter.getItemCount() == 0)
        {
            createNewConversation(message);
        }
    }

    private void showMessageOnScreen(Message message, int action) {
        MessageAction messageAction = MessageAction.values()[action];
        switch (messageAction) {
            case new_message:
                chatAdapter.addNewMessage(message);
                recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                updateConversation(message);
                break;
            case edit_message:
                chatAdapter.updateMessage(message);
                updateMessage(message);
                break;
            case delete_message:
                deleteMessage(message.getMessageID());
                break;
            case activity_start:
                chatAdapter.addNewMessage(message);
                recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                break;
        }
        if (!smsConversation && !message.getSenderID().equals(currentUserID) && message.getMessageStatus() != MessageStatus.READ.ordinal())
            onInteractionMessage(message.getMessageID(), MessageAction.read);
    }

    private void initDB()
    {
        model.getRecipients(conversationID).observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                if (users!=null) {
                    Log.d(CONVERSATION_ACTIVITY, "get recipients from database");
                    if (!users.isEmpty())
                        setRecipients(users);
                    else
                        Log.d(CONVERSATION_ACTIVITY, "no users in db");
                }
                else
                {
                    Log.e(CONVERSATION_ACTIVITY, "loading users from db, user list is null");
                }
                model.getRecipients(conversationID).removeObservers(ConversationActivity2.this);
            }
        });
        LiveData<List<Message>>mld = model.getMessages(conversationID);
        mld.observe(ConversationActivity2.this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {
                Log.d(CONVERSATION_ACTIVITY, "loading messages from database");
                for (Message message: messages)
                {
                    if (message.getMessageStatus() == MessageStatus.WAITING.ordinal())
                    {
                        Log.d(CONVERSATION_ACTIVITY, "sending unsent messages to recipients");
                        sendMessage2(message);
                    }
                    showMessageOnScreen(message, MessageAction.activity_start.ordinal());
                }
                mld.removeObservers(ConversationActivity2.this);
                model.getNewMessage(currentUserID, conversationID).observe(ConversationActivity2.this, new Observer<Message>() {
                    @Override
                    public void onChanged(Message message) {
                        if (message != null) {
                            if (!chatAdapter.isMessageExists(message.getMessageID())) {
                                Log.d(CONVERSATION_ACTIVITY, "loaded new message");
                                initNewConversation(message);
                                showMessageOnScreen(message, MessageAction.new_message.ordinal());
                            }
                            else
                            {
                                Log.e(CONVERSATION_ACTIVITY, "getNewMessage - msg already exist");
                            }

                        }
                        else
                        {
                            Log.e(CONVERSATION_ACTIVITY, "loading new message, msg is null");
                        }
                    }
                });
            }
        });
        LiveData<Conversation> cld = model.getConversation(conversationID);
        cld.observe(this, new Observer<Conversation>() {
            @Override
            public void onChanged(Conversation conversation) {
                Log.d(CONVERSATION_ACTIVITY, "loading conversation object from db");
                if (conversation != null)
                {
                    setConversation(conversation);
                    initConversationLook();
                }
                else
                {
                    Log.e(CONVERSATION_ACTIVITY, "conversation object from db is null");
                }
                cld.removeObservers(ConversationActivity2.this);
            }
        });
//        if (conversationType != ConversationType.sms)
//            model.getNewMessage(currentUserID, conversationID).observe(this, new Observer<Message>() {
//                @Override
//                public void onChanged(Message message) {
//                    if (message != null) {
//                        if (!chatAdapter.isMessageExists(message.getMessageID())) {
//                            Log.d(CONVERSATION_ACTIVITY, "loaded new message");
//                            initNewConversation(message);
//                            showMessageOnScreen(message, MessageAction.new_message.ordinal());
//                        }
//                        else
//                        {
//                            Log.e(CONVERSATION_ACTIVITY, "getNewMessage - msg already exist");
//                        }
//
//                    }
//                    else
//                    {
//                        Log.e(CONVERSATION_ACTIVITY, "loading new message, msg is null");
//                    }
//                }
//            });
        LiveData<User>currentUser = userModel.loadUserByID(currentUserID);
        currentUser.observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                Log.d(CONVERSATION_ACTIVITY, "loading current user");
                if (user != null)
                {
                    setCurrentUserID(user);
                }
                else
                {
                    Log.e(CONVERSATION_ACTIVITY, "loaded null user");
                    userModel.setOnUserDownloadedListener(new Server.onUserDownload() {
                        @Override
                        public void downloadedUser(User user) {
                            Log.d(CONVERSATION_ACTIVITY, "downloading current user");
                            setCurrentUserID(user);
                        }
                    });
                    userModel.downloadUser(currentUserID);
                    Log.i(USER_SERVICE, "downloading user");
                }
            }
        });
    }

    private void initBackend()
    {
        Log.d(CONVERSATION_ACTIVITY, "init backend");
        model = new ViewModelProvider(this).get(ConversationVM.class);
        userModel = new ViewModelProvider(this).get(UserVM.class);
    }

    @SuppressWarnings("unchecked")
    private void initConversation()
    {
        Log.d(CONVERSATION_ACTIVITY, "init conversation");
        Intent initIntent = getIntent();
        conversationID = initIntent.getStringExtra("conversationID");
        LiveData<Boolean> isConversationExist = model.isConversationExists(conversationID);
        isConversationExist.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (!aBoolean)
                {
                    Conversation conversation = new Conversation(conversationID);
                    List<User> recipients = (List<User>) initIntent.getSerializableExtra("recipients");
                    if (recipients != null) {
                        if (!recipients.isEmpty()) {
                            setRecipients(recipients);
                            ConversationType type = ConversationType.single;
                            if (initIntent.hasExtra("phoneNumber")) {
                                String number = initIntent.getStringExtra("phoneNumber");
                                type = ConversationType.sms;
                                conversation.setConversationName(recipients.get(0).getName());
                                conversation.setRecipientPhoneNumber(number);
                            } else if (recipients.size() > 1) {
                                type = ConversationType.group;
                                conversation.setConversationName(initIntent.getStringExtra("conversationName"));
                            } else if (recipients.size() == 1) {
                                conversation.setConversationName(recipients.get(0).getName());
                            } else {
                                Log.e(CONVERSATION_ACTIVITY, "initConversation - no recipients");
                            }
                            for (User recipient : recipients) {
                                String id = recipient.getUserUID();
                                conversation.addRecipient(id);
                                conversation.addToken(recipient.getToken());
                            }
                            conversation.setUnreadMessages(0);
                            conversation.setBlocked(false);
                            conversation.setMuted(false);
                            conversation.setConversationType(type.ordinal());
                            new Handler(getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    setConversation(conversation);
                                }
                            });
                            initConversationLook();

                        } else
                            Log.e(CONVERSATION_ACTIVITY, "init conversation - no recipients in list");
                    } else
                        Log.e(CONVERSATION_ACTIVITY, "init conversation - recipients are null");
                }
                isConversationExist.removeObservers(ConversationActivity2.this);
            }
        });
    }

    private void initConversationLook()
    {
        Map<ButtonType, Integer> sendBtnImages = new HashMap<>();
        sendBtnImages.put(ButtonType.sendMessage, R.drawable.ic_baseline_send_white);
        switch (conversationType)
        {
            case sms:
                actionBtn.setVisibility(View.GONE);
                sendMessageBtn.setButtonTypeImages(sendBtnImages);
                sendMessageBtn.setCurrentButtonType(ButtonType.sendMessage);

                toolbar.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark, getTheme()));
                toolbar.setPopupTheme(R.style.sms);
                break;
            case group:case single:
                actionBtn.setVisibility(View.VISIBLE);
                Map<ButtonType, Integer>buttonImages = new HashMap<>();
                buttonImages.put(ButtonType.location, R.drawable.ic_baseline_location_on_24);
                buttonImages.put(ButtonType.attachFile, R.drawable.ic_baseline_attach_file_white);
                buttonImages.put(ButtonType.camera, R.drawable.ic_baseline_camera_alt_white);
                buttonImages.put(ButtonType.gallery, R.drawable.ic_baseline_photo_24);
                buttonImages.put(ButtonType.delay, R.drawable.ic_baseline_access_time_white);
                buttonImages.put(ButtonType.video, R.drawable.ic_baseline_videocam_white);
                buttonImages.put(ButtonType.play, R.drawable.ic_baseline_play_circle_outline_white);
                buttonImages.put(ButtonType.pause,R.drawable.ic_baseline_pause_circle_outline_white);
                buttonImages.put(ButtonType.cancel, R.drawable.ic_baseline_cancel_24);
                buttonImages.put(ButtonType.joke, R.drawable.cns);
                buttonImages.put(ButtonType.poll,R.drawable.ic_baseline_poll_24);
                buttonImages.put(ButtonType.contact, R.drawable.ic_baseline_contacts_24);
                actionBtn.setButtonTypeImages(buttonImages);
                actionBtn.setCurrentButtonType(ButtonType.location);

                sendBtnImages.put(ButtonType.microphone, R.drawable.ic_baseline_mic_black);
                sendMessageBtn.setButtonTypeImages(sendBtnImages);
                sendMessageBtn.setCurrentButtonType(ButtonType.microphone);

                if (conversationType == ConversationType.group)
                {
                    toolbar.setBackgroundColor(getResources().getColor(android.R.color.holo_purple, getTheme()));
                    toolbar.setPopupTheme(R.style.group);
                }
                else
                {
                    toolbar.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light, getTheme()));
                    toolbar.setPopupTheme(R.style.single);
                }
                break;
            default:
                Log.e(CONVERSATION_ACTIVITY, "unsupported conversation type");
        }
        invalidateOptionsMenu();
    }

    private void saveUser(User user)
    {
        Log.d(CONVERSATION_ACTIVITY, "save user");
        userModel.saveUser(user);
    }

    private void createNewConversation(Message message) {
        Log.d(CONVERSATION_ACTIVITY, "createNewConversation id: " + message.getConversationID());
        model.createConversation(message, conversationType, getRecipientsIDs());
        for (User user: recipients)
            saveUser(user);
    }

    private void updateConversation(Message message) {
        Log.d(CONVERSATION_ACTIVITY, "updateConversation id: " + message.getConversationID());
        if (message.getMessageType() == MessageType.gpsMessage.ordinal())
            message.setContent(message.getAddress());
        model.updateConversation(message);
    }

    private void updateMessage(@NonNull Message message) {
        Log.d(CONVERSATION_ACTIVITY, "update message id: " + message.getMessageID());
        LiveData<Message>messageLiveData = model.getMessage(message.getMessageID());
        messageLiveData.observe(ConversationActivity2.this, new Observer<Message>() {
            @Override
            public void onChanged(Message message1) {
                Log.d(CONVERSATION_ACTIVITY, "update msg, check if update is needed id: " + message.getMessageID());
                if (message1 != message)
                {
                    Log.d(CONVERSATION_ACTIVITY, "msg update is needed");
                    if (chatAdapter.getMessage(chatAdapter.getItemCount()-1).getMessageID()==message.getMessageID())
                        updateConversation(message);
                    model.updateMessage(message);
                    chatAdapter.updateMessage(message);
                }
                else
                    Log.d(CONVERSATION_ACTIVITY, "msg update was not needed");
            }
        });
    }

    private void saveMessage(@NonNull Message message)
    {
        Log.d(CONVERSATION_ACTIVITY, "save message id: " + message.getMessageID());
        LiveData<Boolean>isMessageExist = model.isMessageExists(message);
        isMessageExist.observe(ConversationActivity2.this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (!aBoolean)
                {
                    Log.d(CONVERSATION_ACTIVITY, "saving msg to db");
                    model.saveMessage(message);
                }
                else
                {
                    Log.d(CONVERSATION_ACTIVITY, "message already exists - sending to updateMessage");
                    updateMessage(message);
                }
                isMessageExist.removeObservers(ConversationActivity2.this);
            }
        });
    }

    private void deleteMessage(long messageID) {
        Log.d(CONVERSATION_ACTIVITY, "delete message id: " + messageID);
        Message message = chatAdapter.getMessage(chatAdapter.getItemCount()-1);
        if (message.getMessageID() == messageID)
        {
            message.setContent("deleted");
            updateConversation(message);
        }
        model.deleteMessage(messageID);
        chatAdapter.deleteMessage(messageID);
    }

    private void onConversationMessage(ConversationMessageKind conversationMessageKind, String data)
    {
        ConversationMessage conversationMessage = new ConversationMessage(conversationID);
        conversationMessage.setConversationMessageKind(conversationMessageKind.ordinal());
        switch (conversationMessageKind)
        {
            case nameChange:
                conversationMessage.setConversationName(data);
                break;
            case addRecipient:
            case removeRecipient:
                conversationMessage.setRecipientsId(data);
                break;
        }
        messageSender.sendMessage(conversationMessage, getRecipientsTokens());
    }

    private void onInteractionMessage(long messageID, MessageAction action) {
        if (conversationType != ConversationType.sms) {
            Message message = new Message();
            message.setConversationID(conversationID);
            message.setSenderToken(getMyToken());
            message.setMessageAction(action.ordinal());
            switch (action) {
                case read: {
                    message.setMessageID(messageID);
                    message.setMessageStatus(MessageStatus.READ.ordinal());
                    break;
                }
                case delete_message: {
                    message.setMessageID(messageID);
                    onDeselectMessage();
                    deleteMessage(messageID);
                    break;
                }
                case edit_message: {
                    if (messageText.getText() != null) {
                        message = editMessage;
                        message.setMessageID(messageID);
                        message.setContent(getMessage());
                        message.setEditTime(System.currentTimeMillis());
                        message.setMessageStatus(MessageStatus.WAITING.ordinal());
//                        model.addMessageHistory(new MessageHistory(message));
                        model.updateConversationLastMessage(conversationID, getMessage());
                        updateMessage(message);
                        onClearEditMessage();
                        onDeselectMessage();
                        Log.d(CONVERSATION_ACTIVITY, "interaction - edit msg");
                    }
                    break;
                }
                default:
                    Log.d(CONVERSATION_ACTIVITY, "standard interaction msg");
            }
            Log.d(CONVERSATION_ACTIVITY,"interaction msg: " + action.name());
            messageSender.sendMessage(message, getRecipientsTokens());
        }
    }

    private void onClearEditMessage()
    {
        editMessage = null;
        Log.d(CONVERSATION_ACTIVITY, "edit msg was cleared");
    }

    private void onDeselectMessage()
    {
        int msgIndex = chatAdapter.findMessageLocation(null, 0, -1, selectedMessage.getMessageID());
        if (msgIndex != -1) {
            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(msgIndex);
            if (holder != null) {
                holder.itemView.findViewById(R.id.messageTextLayout).setSelected(false);
            }
        }
        messageLongPress = false;
        selectedMessage = null;
        invalidateOptionsMenu();
    }

    private List<String> getRecipientsNames() {
        List<String>recipientsNames = new ArrayList<>();
        for (int i = 0; i < recipients.size(); i++)
            recipientsNames.add(recipients.get(i).getName());
        return recipientsNames;
    }

    private List<String> getRecipientsIDs() {
        List<String>recipientsIds = new ArrayList<>();
        for (int i = 0; i < recipients.size(); i++)
            recipientsIds.add(recipients.get(i).getUserUID());
        return recipientsIds;
    }

    private List<String> getRecipientsTokens() {
        List<String>recipientsTokens = new ArrayList<>();
        for (int i = 0; i < recipients.size(); i++)
            recipientsTokens.add(recipients.get(i).getToken());
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

    private void getRecipientToken(String uid) {
        if (uid != null) {
            userModel.setOnTokenDownloadedListener(new Server.onTokenDownloaded() {
                @Override
                public void tokenDownloaded(String uid, String token) {
                    if (token != null) {
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
                    Log.e(NULL_ERROR, message);
                }
            });
            userModel.getUserToken(uid);
        } else
            Log.e(NULL_ERROR, "getRecipientToken - recipientUID is null");
    }

    private User getRecipientByID(String uid) {
        for (User user : recipients)
            if (user.getUserUID().equals(uid))
                return user;
        return null;
    }

    @Override
    public void onGifClick(Gif gif) {
        this.gif = gif;
        fullMessageProcess(MessageType.gif);
    }

    private void resetQuote() {
        quoteText.setText("");
        quoteSender.setText("");
        quoteLayout.setVisibility(View.GONE);
    }

    private void updateUser(User user)
    {
        Log.d(CONVERSATION_ACTIVITY, "update user");
        userModel.updateUser(user);
    }
}