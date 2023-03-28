package Adapters;

import android.annotation.SuppressLint;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.woofmeow.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.PipedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Audio.Audio;
import Audio.AudioManager2;
import Audio.AudioPlayer3;
import Consts.ButtonType;
import Consts.MessageStatus;
import Consts.MessageType;
import Consts.Messaging;
import Fragments.ImageFragment;
import NormalObjects.ImageButtonPlus;
import NormalObjects.ImageButtonState;
import NormalObjects.Message;
import NormalObjects.PlayAudioButton;
import NormalObjects.Web;
import Time.TimeFormat;


@SuppressWarnings("Convert2Lambda")
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Message> messages = new ArrayList<>();
    private String currentUserUID;
    private final String ERROR = "CHAT_ADAPTER_ERROR";
    private final String CHAT_ADAPTER = "CHAT_ADAPTER";
    private float textSize = 30;
    private final int[] colors = {R.color.red, R.color.blue, R.color.green, R.color.yellow, R.color.colorAccent, R.color.colorPrimary};
    private int colorIterator = 0;
    private Map<String, Integer> matchedColors;
    private AudioPlayer3 audioPlayer;
    private final int PLAY = 0;
    private final int PAUSE = 1;
    private final int STOP = 2;
    public interface MessageInfoListener {
        void onMessageClick(Message message);

        void onMessageLongClick(Message message);

        String onImageDownloaded(Bitmap bitmap, Message message);

        String onVideoDownloaded(File file, Message message);
        //void onUpdateMessageStatus(Message message);
    }

    public ChatAdapter() {
    }

    private MessageInfoListener callback;

    public void setCurrentUserUID(String currentUserUID) {
        this.currentUserUID = currentUserUID;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public void addNewMessage(Message message) {
        if (messages == null)
            messages = new ArrayList<>();
        messages.add(message);
        notifyItemInserted(getItemCount() - 1);
    }

    public void deleteMessage(long messageID) {
        int index = findMessage(messages, 0, messages.size() - 1, messageID);
        if (index != -1) {
            messages.remove(index);
            notifyItemRemoved(index);
        }
    }

    public void changeExistingMessage(Message message) {
        if (messages != null) {
            int messageIndex = findMessage(messages, 0, messages.size() - 1, message.getMessageID());
            if (messageIndex != -1) {
                messages.set(messageIndex, message);
                notifyItemChanged(messageIndex);
            } else Log.e(ERROR, "messageIndex is -1");
        }
    }

    public void updateMessage(Message message) {
        int index = findMessage(messages, 0, messages.size() - 1, message.getMessageID());
        if (index != -1) {
            messages.set(index, message);
            notifyItemChanged(index);
        }
    }

    public void updateMessageStatus(long messageID, MessageStatus status, String time) {
        Log.d("messageStatus", "updating message status");
        int index = findMessage(messages, 0, messages.size() - 1, messageID);
        if (index != -1) {
            Message message = messages.get(index);
            message.setMessageStatus(status.ordinal());
            message.setReadingTime(Long.parseLong(time));
            notifyItemChanged(index);
        } else
            Log.e("MESSAGE_ID ERROR", "didn't find message in messages");
    }

    public void updateMessageImage(long messageID) {
        int index = findMessage(messages, 0, messages.size() - 1, messageID);
        if (index != -1) {
            notifyItemChanged(index);
        }
    }

    public void setListener(MessageInfoListener listener) {
        callback = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Messaging messageType = Messaging.values()[viewType];
        View view = null;
        switch (messageType) {
            case incoming: {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.incoming_message2, parent, false);
                break;
            }
            case outgoing: {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.outgoing_message2, parent, false);
                break;
            }
            default:
                Log.e(ERROR, "error in creating viewHolder in chatAdapter");
                break;
        }
        return new ChatAdapter.ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final Message message = messages.get(position);
        holder.message.setText(message.getContent());
        if (!message.getSenderID().equals(currentUserUID)) {
            holder.messageSender.setVisibility(View.VISIBLE);
            holder.messageSender.setText(message.getSenderName());
        }
//        if (message.isStar()) {
//            holder.message.setCompoundDrawablesRelativeWithIntrinsicBounds(android.R.drawable.star_on, 0, 0, 0);
//        }
        if (message.getMessageType() == MessageType.textMessage.ordinal()) {
            holder.playRecordingLayout.setVisibility(View.GONE);
            holder.messageTextLayout.setVisibility(View.VISIBLE);
            holder.message.setVisibility(View.VISIBLE);
            holder.specialMsgIndicator.setVisibility(View.GONE);
            holder.contactLayout.setVisibility(View.GONE);
            holder.linkMessageLayout.setVisibility(View.GONE);
            holder.imageLayout.setVisibility(View.GONE);
        } else if (message.getMessageType() == MessageType.gpsMessage.ordinal()) {
            holder.playRecordingLayout.setVisibility(View.GONE);
            holder.messageTextLayout.setVisibility(View.VISIBLE);
            holder.specialMsgIndicator.setVisibility(View.VISIBLE);
            holder.contactLayout.setVisibility(View.GONE);
            holder.message.setVisibility(View.VISIBLE);
            holder.linkMessageLayout.setVisibility(View.GONE);
            holder.imageLayout.setVisibility(View.GONE);
            holder.message.setText(message.getAddress());
        } else if (message.getMessageType() == MessageType.contact.ordinal()) {
            holder.contactLayout.setVisibility(View.VISIBLE);
            holder.contactName.setText(message.getContactName());
            holder.contactPhone.setText(message.getContactNumber());
            holder.messageTextLayout.setVisibility(View.GONE);
            holder.playRecordingLayout.setVisibility(View.GONE);
            holder.specialMsgIndicator.setVisibility(View.GONE);
            holder.linkMessageLayout.setVisibility(View.GONE);
            holder.imageLayout.setVisibility(View.GONE);
        } else if (message.getMessageType() == MessageType.photoMessage.ordinal() || message.getMessageType() == MessageType.imageMessage.ordinal()) {
            Log.i("imageMessage", "path: " + message.getFilePath());
            holder.imageLayout.setVisibility(View.VISIBLE);
            holder.previewImage.setVisibility(View.VISIBLE);
            holder.contactLayout.setVisibility(View.GONE);
            holder.linkMessageLayout.setVisibility(View.GONE);
            holder.specialMsgIndicator.setVisibility(View.GONE);
            holder.playRecordingLayout.setVisibility(View.GONE);
            if (message.getContent() == null || message.getContent().equals(""))
                holder.message.setVisibility(View.GONE);
            else {
                holder.message.setVisibility(View.VISIBLE);
                holder.messageTextLayout.setVisibility(View.VISIBLE);
            }

            if (message.getFilePath() != null) {
                holder.imageStatusLayout.setVisibility(View.GONE);
                if (message.getFilePath().startsWith("content")) {
                    Uri uri = Uri.parse(message.getFilePath());
                    String uriPath = uri.getPath();
                    Picasso.get().load(Uri.parse(message.getFilePath())).resize(300, 300).into(holder.previewImage);
                } else
                    Picasso.get().load(new File(message.getFilePath())).resize(300, 300).into(holder.previewImage);
            } else {
                holder.imageStatusLayout.setVisibility(View.VISIBLE);
                if (holder.reUpload != null)
                    holder.reUpload.setVisibility(View.VISIBLE);
                if (holder.reDownload != null)
                    holder.reDownload.setVisibility(View.VISIBLE);
                if (holder.refresh != null)
                    holder.refresh.setVisibility(View.GONE);
                holder.showImageProgress.setVisibility(View.GONE);
            }
        } else if (message.getMessageType() == MessageType.voiceMessage.ordinal()) {
            holder.messageTextLayout.setVisibility(View.GONE);
            holder.playRecordingLayout.setVisibility(View.VISIBLE);
            holder.specialMsgIndicator.setVisibility(View.GONE);
            holder.message.setVisibility(View.GONE);
            holder.contactLayout.setVisibility(View.GONE);
            holder.linkMessageLayout.setVisibility(View.GONE);
            holder.imageLayout.setVisibility(View.GONE);
            if (audioPlayer == null)
            {
                audioPlayer = new AudioPlayer3();
                audioPlayer.setListener(new Audio() {
                    @Override
                    public void onLoad(long duration) {
                        holder.voiceSeek.setMax((int)duration);
                    }

                    @Override
                    public void onUnLoad() {

                    }

                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onPause() {

                    }

                    @Override
                    public void onResume(long progress) {

                    }

                    @Override
                    public void onStopped(String fileName) {

                    }

                    @Override
                    public void onFinished(long fileDuration) {
                        audioPlayer.stopPlaying();
//                        audioPlayer.releasePlayer();
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                holder.recordingTime.setText(holder.itemView.getContext().getResources().getString(R.string.zero_time));
                                holder.voiceSeek.setProgress(0);
                                holder.playPauseBtn.nextState();

                            }
                        });
                        Thread thread = new Thread()
                        {
                            @Override
                            public void run() {
                                audioPlayer.releasePlayer();
                            }
                        };
                        thread.start();
                    }

                    @Override
                    public void onFailed(String msg) {
                        Log.e("CHAT_ADAPTER","failed to load voice: " + msg);
                    }

                    @Override
                    public void onProgressChange(long progress) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                holder.voiceSeek.setProgress((int)progress);
                                String time = new TimeFormat().getFormattedTime(progress);
                                holder.recordingTime.setText(time);
                            }
                        });

                    }
                });
            }
        } else if (message.getMessageType() == MessageType.webMessage.ordinal()) {
            holder.linkMessageLayout.setVisibility(View.VISIBLE);
            holder.contactLayout.setVisibility(View.GONE);
            holder.playRecordingLayout.setVisibility(View.GONE);
            holder.specialMsgIndicator.setVisibility(View.GONE);
            holder.imageLayout.setVisibility(View.GONE);
            if (message.getContent() == null || message.getContent().equals(""))
                holder.message.setVisibility(View.GONE);
            else {
                holder.message.setVisibility(View.VISIBLE);
                holder.messageTextLayout.setVisibility(View.VISIBLE);
            }
            Web web = new Web();
            web.setListener(new Web.onWebDownload() {
                @Override
                public void onMetaDataDownload(String description, String title) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            holder.linkContent.setText(description);
                            holder.linkTitle.setText(title);
                            holder.message.setText(message.getContent());
                        }
                    });
                }

                @Override
                public void onWebImageSuccess(Bitmap bitmap) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            holder.linkProgressBar.setVisibility(View.GONE);
                            holder.linkImage.setImageBitmap(bitmap);
                            holder.linkImage.setScaleType(ImageView.ScaleType.FIT_XY);
                        }
                    });

                }

                @Override
                public void onFailed() {
                    new Handler(holder.itemView.getContext().getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(holder.itemView.getContext(), "failed showing message", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            });
            web.downloadWebPreview(message.getContent());
        } else if (message.getMessageType() == MessageType.videoMessage.ordinal()) {
            callback.onMessageClick(message);
            if (message.getFilePath() != null) {
                Uri videoUri = Uri.parse(message.getFilePath());
                File file = new File(message.getFilePath());
                holder.videoLayout.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    holder.itemView.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Size size = new Size(holder.playVideoBtn.getWidth(), holder.playVideoBtn.getHeight());
                                Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(file, size, new CancellationSignal());
                                BitmapDrawable bitmapDrawable = new BitmapDrawable(holder.itemView.getResources(), thumbnail);
                                holder.playVideoBtn.setBackground(bitmapDrawable);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Images.Thumbnails.MINI_KIND);
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(holder.itemView.getResources(), thumbnail);
                    holder.playVideoBtn.setBackground(bitmapDrawable);
                }
                holder.playVideoBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!file.exists())
                            Toast.makeText(holder.itemView.getContext(), "file doesn't exists! can't play video", Toast.LENGTH_SHORT).show();
                        else if (videoUri != null)
//                            callback.onVideoClicked(videoUri);
                            callback.onMessageClick(message);
                        else
                            Toast.makeText(holder.itemView.getContext(), "something went wrong, try again later", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else if (message.getMessageType() == MessageType.gif.ordinal()) {
            holder.imageLayout.setVisibility(View.VISIBLE);
            holder.contactLayout.setVisibility(View.GONE);
            holder.previewImage.setScaleType(ImageView.ScaleType.FIT_XY);
            holder.imageStatusLayout.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext()).load(message.getContent()).placeholder(R.drawable.ic_baseline_gif_24).addListener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    holder.refresh.setVisibility(View.VISIBLE);
                    holder.reUpload.setVisibility(View.GONE);
                    holder.showImageProgress.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    holder.imageStatusLayout.setVisibility(View.GONE);
                    return false;
                }
            }).into(holder.previewImage);
            holder.message.setVisibility(View.GONE);
        }

        // }
        TimeFormat timeFormat = new TimeFormat();
        long time;
        //show the time of the messages i sent
        if (holder.getItemViewType() == Messaging.outgoing.ordinal()) {
            time = message.getMessageID();
        } else {
            time = message.getArrivingTime();
        }
        String finalDate = timeFormat.getFormattedDate(time);
        if (message.getConversationID().startsWith("G")) {
            String nameAndTime = message.getSenderName() + " " + finalDate;
            holder.timeReceived.setText(nameAndTime);

            if (!message.getSenderID().equals(currentUserUID)) {
                if (matchedColors == null)
                    matchedColors = new HashMap<>();
                if (matchedColors.containsKey(message.getSenderID())) {
                    int colorCode = matchedColors.get(message.getSenderID());
                    holder.timeReceived.setTextColor(ResourcesCompat.getColor(holder.itemView.getContext().getResources(), colorCode, holder.itemView.getContext().getTheme()));
                } else {
                    matchedColors.put(message.getSenderID(), colors[colorIterator]);
                    holder.timeReceived.setTextColor(ResourcesCompat.getColor(holder.itemView.getContext().getResources(), colors[colorIterator], holder.itemView.getContext().getTheme()));
                    colorIterator++;
                    if (colorIterator > colors.length)
                        colorIterator = 0;
                }
            }
        } else {
            holder.timeReceived.setText(finalDate);
        }
        if (holder.statusTv != null) {
            holder.statusTv.changeBtnState(message.getMessageStatus());
        }
        if (message.getQuoteMessage() != null && !message.getQuoteMessage().equals("")) {
            holder.messageTextLayout.setVisibility(View.VISIBLE);
            holder.playRecordingLayout.setVisibility(View.GONE);
            holder.contactLayout.setVisibility(View.GONE);
            holder.imageLayout.setVisibility(View.GONE);
            holder.quoteLayout.setVisibility(View.VISIBLE);
            holder.quote.setVisibility(View.VISIBLE);
            holder.quote.setText(message.getQuoteMessage());
            holder.quoteSenderName.setText(message.getSenderName());
        } else {
            holder.quoteLayout.setVisibility(View.GONE);
        }
    }

    public List<Message> getMessages() {
        return messages;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        AudioManager2 manager = AudioManager2.getInstance();
        manager.releasePlayer(manager.getCurrentDataSource());
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public long getMessageID(int position) {
        return messages.get(position).getMessageID();
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @SuppressWarnings("Convert2Lambda")
    public class ChatViewHolder extends RecyclerView.ViewHolder {
        com.vanniktech.emoji.EmojiTextView message;
        TextView timeReceived, messageSender, quote, quoteSenderName, linkTitle, linkContent, contactName, contactPhone, recordingTime, pollVotes, pollCreator;
        ImageView previewImage, linkImage, specialMsgIndicator;
        LinearLayout playRecordingLayout, videoLayout, imageStatusLayout, quoteLayout, messageTextLayout, linkMessageLayout, innerPaneLayout;//, pollLayout;
        RelativeLayout imageLayout, contactLayout;
        ImageButtonState statusTv;
        SeekBar voiceSeek;
        ImageButton playVideoBtn, reUpload, reDownload, refresh;
//        NormalObjects.PlayAudioButton playPauseBtn;
        ImageButtonState playPauseBtn;
        ProgressBar showImageProgress, linkProgressBar;
        ShapeableImageView contactImage;
        Button saveContactBtn;//, hidePoll;
        ConstraintLayout rootLayout, mainConstraintLayout;

        //        RadioGroup pollGroup;
        @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            recordingTime = itemView.findViewById(R.id.recordingTime);
            messageSender = itemView.findViewById(R.id.senderName);
            contactLayout = itemView.findViewById(R.id.contactLayout);
            contactImage = itemView.findViewById(R.id.contactImage);
            contactName = itemView.findViewById(R.id.contactName);
            contactPhone = itemView.findViewById(R.id.contactPhone);
            saveContactBtn = itemView.findViewById(R.id.saveContactBtn);
            quoteLayout = itemView.findViewById(R.id.quoteLayout);
            quoteSenderName = itemView.findViewById(R.id.quoteSenderName);
            messageTextLayout = itemView.findViewById(R.id.messageTextLayout);
            playRecordingLayout = itemView.findViewById(R.id.playRecordingLayout);
            voiceSeek = itemView.findViewById(R.id.voiceSeek);
            playPauseBtn = itemView.findViewById(R.id.play_pause_btn);
            videoLayout = itemView.findViewById(R.id.videoLayout);
            playVideoBtn = itemView.findViewById(R.id.playVideoBtn);
            showImageProgress = itemView.findViewById(R.id.imageProgressBar);
            statusTv = itemView.findViewById(R.id.messageStatus);
            reUpload = itemView.findViewById(R.id.reUpload);
            reDownload = itemView.findViewById(R.id.reDownload);
            refresh = itemView.findViewById(R.id.refresh);
            quote = itemView.findViewById(R.id.quoteText);
            message = itemView.findViewById(R.id.message);
            previewImage = itemView.findViewById(R.id.previewImage);
            linkMessageLayout = itemView.findViewById(R.id.linkMessage);
            linkProgressBar = itemView.findViewById(R.id.linkProgressBar);
            linkTitle = itemView.findViewById(R.id.linkTitle);
            linkImage = itemView.findViewById(R.id.linkImage);
            linkContent = itemView.findViewById(R.id.linkContent);
            imageLayout = itemView.findViewById(R.id.imageLayout);
            imageStatusLayout = itemView.findViewById(R.id.imageStatusLayout);
            specialMsgIndicator = itemView.findViewById(R.id.specialMessageIndicator);
            timeReceived = itemView.findViewById(R.id.messageTime);
            innerPaneLayout = itemView.findViewById(R.id.innerPaneLayout);
            rootLayout = itemView.findViewById(R.id.rootLayout);
            mainConstraintLayout = itemView.findViewById(R.id.mainConstraintLayout);
            List<Integer>playAudioImages = new ArrayList<>();
            playAudioImages.add(R.drawable.ic_baseline_play_circle_outline_white);
            playAudioImages.add(R.drawable.ic_baseline_pause_circle_outline_white);
            playAudioImages.add(R.drawable.ic_baseline_play_circle_outline_white);
            playPauseBtn.setImages(playAudioImages);
            List<Integer>playBtnStates = new ArrayList<>();
            playBtnStates.add(PAUSE);
            playBtnStates.add(PLAY);
            playBtnStates.add(STOP);
            playPauseBtn.setBtnStates(playBtnStates);
            playPauseBtn.setListener(new ImageButtonPlus.ImageButtonListener() {
                @Override
                public void onImageAdded(int image) {

                }

                @Override
                public void onImageSet() {

                }

                @Override
                public void onImageChanged(int image) {

                }

                @Override
                public void onImageMissing(ButtonType type) {

                }

                @Override
                public void onError(String msg) {
                    Log.e(CHAT_ADAPTER, "an error has happened while changing playAudioBtnState");
                }

                @Override
                public void onButtonStateChange(int buttonState) {
                    if (buttonState == PAUSE)
                    {
                        if (audioPlayer.getPlayTime() == 0)
                        {
                            playPauseBtn.nextState();
                        }
                        else if (audioPlayer!=null)
                            audioPlayer.playPause();
                    }
                    else if (buttonState == PLAY)
                    {
                        if (audioPlayer == null)
                            audioPlayer = new AudioPlayer3();
                        if (audioPlayer.getDataSource() == null)
                            audioPlayer.setDataSource(messages.get(getAdapterPosition()).getFilePath());
                        audioPlayer.playPause();
                    }
                    else if (buttonState == STOP)
                    {

                    }
                }

                @Override
                public void onButtonTypeChange(ButtonType type) {

                }
            });
//            hidePoll = itemView.findViewById(R.id.hidePoll);
//            pollLayout = itemView.findViewById(R.id.pollLayout);
//            pollVotes = itemView.findViewById(R.id.pollVotes);
//            pollCreator = itemView.findViewById(R.id.pollCreatorName);
//            pollGroup = itemView.findViewById(R.id.radioGroup);
//            hidePoll.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (hidePoll.getVisibility() == View.VISIBLE)
//                    {
//                        pollLayout.setVisibility(View.GONE);
//                    }
//                }
//            });
//            pollGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(RadioGroup radioGroup, int i) {
//                    callback.onRadioBtnClick(radioGroup, i);
//                }
//            });
            message.setTextSize(textSize);
//            message.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (messages.get(getAdapterPosition()).getMessageType() == MessageType.gpsMessage.ordinal())
//                        callback.onPreviewMessageClick(messages.get(getAdapterPosition()));
//                    callback.onMessageClick(messages.get(getAdapterPosition()), v, getItemViewType());
//                }
//            });
//            message.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    messageTextLayout.setSelected(!messageTextLayout.isSelected());
//                    callback.onMessageLongClick(messages.get(getAdapterPosition()));
//                    return true;
//                }
//            });

//            innerPaneLayout.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    v.setSelected(!v.isSelected());
//                    callback.onMessageLongClick(messages.get(getAdapterPosition()));
//                    return true;
//                }
//            });
//            linkMessageLayout.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    callback.onMessageClick(messages.get(getAdapterPosition()), v, getItemViewType());
//                }
//            });
//            linkMessageLayout.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    callback.onMessageLongClick(messages.get(getAdapterPosition()));
//                    return true;
//                }
//            });
//            previewImage.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    callback.onPreviewMessageClick(messages.get(getAdapterPosition()));
//                }
//            });
            List<Integer> statusStates = new ArrayList<>();
            statusStates.add(MessageStatus.WAITING.ordinal());
            statusStates.add(MessageStatus.SENT.ordinal());
            statusStates.add(MessageStatus.DELIVERED.ordinal());
            statusStates.add(MessageStatus.READ.ordinal());
            List<Integer> statusImages = new ArrayList<>();
            statusImages.add(R.drawable.ic_baseline_waiting);
            statusImages.add(R.drawable.message_sent);
            statusImages.add(R.drawable.ic_baseline_done_24);
            statusImages.add(R.drawable.ic_baseline_done_all_24);
            statusTv.setImages(statusImages);
            statusTv.setBtnStates(statusStates);
            statusTv.setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return true;
                }
            });
        }

        public void bind(Message message) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (callback != null) {
                        callback.onMessageClick(message);
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (callback != null) {
                        callback.onMessageLongClick(message);
                    }
                    innerPaneLayout.setSelected(!innerPaneLayout.isSelected());
                    return true;
                }
            });
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getSenderID().equals(currentUserUID))
            return Messaging.outgoing.ordinal();
        else
            return Messaging.incoming.ordinal();
    }

    public int findMessageIndex(long msgID) {
        Log.d("CHAT_ADAPTER", "find message index");
        return findMessage(this.messages, 0, this.messages.size(), msgID);
    }

    public int findMessageLocation(List<Message> messages, int min, int max, long key) {
        if (max == -1)
            max = this.messages.size();
        return findMessage(this.messages, min, max, key);
    }

    public ArrayList<Integer> SearchMessage(String searchQuery) {
        ArrayList<Integer> searchQueryIndexes = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getContent() != null)
                if (messages.get(i).getContent().contains(searchQuery)) {
                    searchQueryIndexes.add(i);
                }
        }
        return searchQueryIndexes;
    }

    public void UpdateMessageStar(long messageID, boolean star) {
        int index = findMessage(messages, 0, messages.size() - 1, messageID);//findCorrectMessage(messages,0,messages.size()-1,Long.parseLong(messageID));
        Message message = messages.get(index);
        message.setStar(star);
        notifyItemChanged(index);
    }

    public int updateMessageEdit(long messageID, String content, long time) {
        int index = findMessage(messages, 0, messages.size() - 1, messageID);//findCorrectMessage(messages,0,messages.size()-1,Long.parseLong(messageID));
        Message message = messages.get(index);
        message.setContent(content);
        message.setEditTime(time);
        notifyItemChanged(index);
        return index;
    }

    public Message getMessage(int index) {
        if (index < getItemCount() && index >= 0)
            return messages.get(index);
        else return null;
    }

    private int findMessage(List<Message> messages, int min, int max, long key) {
        if (messages.size() == 0)
            return -1;
        if (max >= min) {
            int mid = min + (max - min) / 2;
            if (messages.get(mid).getMessageID() == key)
                return mid;
            if (messages.get(mid).getMessageID() > key)
                return findMessage(messages, min, mid - 1, key);
            else
                return findMessage(messages, mid + 1, max, key);
        }
        return -1;
    }

    public boolean isMessageExists(long messageID) {
        int index = findMessage(messages, 0, messages.size() - 1, messageID);
        return index != -1;
    }
}
