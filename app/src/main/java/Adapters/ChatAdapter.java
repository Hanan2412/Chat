package Adapters;

import android.annotation.SuppressLint;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
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
import com.example.woofmeow.ConversationActivity;
import com.example.woofmeow.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Audio.AudioManager2;
import Audio.AudioPlayer2;
import Consts.MessageType;
import Consts.Messaging;
import Audio.AudioHelper;
import NormalObjects.FileManager;
import NormalObjects.Message;
import NormalObjects.PlayAudioButton;
import NormalObjects.Web;
import Time.TimeFormat;


@SuppressWarnings("Convert2Lambda")
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private ArrayList<Message> messages = new ArrayList<>();
    private String currentUserUID;
    private final String ERROR = "CHAT_ADAPTER_ERROR";
    private float textSize = 30;
    private final int[] colors = {R.color.red,R.color.blue,R.color.green,R.color.yellow,R.color.colorAccent,R.color.colorPrimary};
    private int colorIterator = 0;
    private Map<String,Integer>matchedColors;


    public interface MessageInfoListener {
        void onMessageClick(Message message, View view, int viewType);
        void onMessageLongClick(Message message, View view, int viewType);
        void onPreviewMessageClick(Message message);
        void onDeleteMessageClick(Message message);
        void onEditMessageClick(Message message);
        void onRadioBtnClick(RadioGroup group, int radioBtnPosition);
        String onImageDownloaded(Bitmap bitmap, Message message);
        String onVideoDownloaded(File file, Message message);
        void onVideoClicked(Uri uri);
        void onRetrySending(String messageID, String imagePath);
        //void onUpdateMessageStatus(Message message);
    }

    public ChatAdapter() {
    }

    private MessageInfoListener callback;

    public void setCurrentUserUID(String currentUserUID) {
        this.currentUserUID = currentUserUID;
    }
    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }
    public void addNewMessage(Message message) {
        if (messages == null)
            messages = new ArrayList<>();
        messages.add(message);
        notifyItemInserted(getItemCount() - 1);
    }

    public void deleteMessage(String messageID) {
        int index = findMessage(messages, 0, messages.size() - 1, messageID);
        if (index!=-1) {
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
    public void updateMessage(Message message)
    {
        int index = findMessage(messages,0,messages.size()-1,message.getMessageID());
        if (index!=-1)
        {
            messages.set(index,message);
            notifyItemChanged(index);
        }
    }

    public void updateMessageStatus(String id, String status, String time) {
        Log.d("messageStatus", "updating message status");
        int index = findMessage(messages, 0, messages.size() - 1, id);
        if (index != -1) {
            Message message = messages.get(index);
            message.setMessageStatus(status);
            message.setReadAt(Long.parseLong(time));
            notifyItemChanged(index);
        } else
            Log.e("MESSAGE_ID ERROR", "didn't find message in messages");
    }

    public void UpdateMessageImage(String messageID) {
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
        holder.message.setText(message.getMessage());
        if (!message.getSender().equals(currentUserUID))
        {
            holder.messageSender.setVisibility(View.VISIBLE);
            holder.messageSender.setText(message.getSenderName());
        }
//        if (message.isStar()) {
//            holder.message.setCompoundDrawablesRelativeWithIntrinsicBounds(android.R.drawable.star_on, 0, 0, 0);
//        }
        if (message.getMessageType() == MessageType.textMessage.ordinal())
        {
            holder.playRecordingLayout.setVisibility(View.GONE);
            holder.messageTextLayout.setVisibility(View.VISIBLE);
            holder.message.setVisibility(View.VISIBLE);
            holder.gpsImageIndicator.setVisibility(View.GONE);
            holder.contactLayout.setVisibility(View.GONE);
            holder.linkMessageLayout.setVisibility(View.GONE);
            holder.imageLayout.setVisibility(View.GONE);
        }
        else if (message.getMessageType() == MessageType.gpsMessage.ordinal()) {
            {
                holder.playRecordingLayout.setVisibility(View.GONE);
                holder.messageTextLayout.setVisibility(View.VISIBLE);
                holder.gpsImageIndicator.setVisibility(View.VISIBLE);
                holder.contactLayout.setVisibility(View.GONE);
                holder.message.setVisibility(View.VISIBLE);
                holder.linkMessageLayout.setVisibility(View.GONE);
                holder.imageLayout.setVisibility(View.GONE);
            }
        } else if (message.getMessageType() == MessageType.contact.ordinal()) {
            holder.contactLayout.setVisibility(View.VISIBLE);
            holder.contactName.setText(message.getContactName());
            holder.contactPhone.setText(message.getContactPhone());
            holder.messageTextLayout.setVisibility(View.GONE);
            holder.playRecordingLayout.setVisibility(View.GONE);
            holder.gpsImageIndicator.setVisibility(View.GONE);
            holder.linkMessageLayout.setVisibility(View.GONE);
            holder.imageLayout.setVisibility(View.GONE);
        } else if (message.getMessageType() == MessageType.photoMessage.ordinal() || message.getMessageType() == MessageType.imageMessage.ordinal()) {
            holder.imageLayout.setVisibility(View.VISIBLE);
            holder.previewImage.setVisibility(View.VISIBLE);
            holder.contactLayout.setVisibility(View.GONE);
            holder.linkMessageLayout.setVisibility(View.GONE);
            holder.gpsImageIndicator.setVisibility(View.GONE);
            holder.playRecordingLayout.setVisibility(View.GONE);
            if (message.getMessage() == null || message.getMessage().equals(""))
                holder.message.setVisibility(View.GONE);
            else {
                holder.message.setVisibility(View.VISIBLE);
                holder.messageTextLayout.setVisibility(View.VISIBLE);
            }
            Log.i("imageMessage", "path: " + message.getImagePath());
            if (message.getImagePath() != null) {
                holder.imageStatusLayout.setVisibility(View.GONE);
                if (message.getImagePath().startsWith("content")) {
                    Uri uri = Uri.parse(message.getImagePath());
                    String uriPath = uri.getPath();
                    Picasso.get().load(Uri.parse(message.getImagePath())).resize(300, 300).into(holder.previewImage);
                }
                else Picasso.get().load(new File(message.getImagePath())).resize(300,300).into(holder.previewImage);
            }
            else
            {
                holder.imageStatusLayout.setVisibility(View.VISIBLE);
                if (holder.reUpload != null)
                    holder.reUpload.setVisibility(View.VISIBLE);
                if (holder.reDownload !=null)
                    holder.reDownload.setVisibility(View.VISIBLE);
                if (holder.refresh != null)
                    holder.refresh.setVisibility(View.GONE);
                holder.showImageProgress.setVisibility(View.GONE);
            }
        } else if (message.getMessageType() == MessageType.voiceMessage.ordinal()) {
            holder.messageTextLayout.setVisibility(View.GONE);
            holder.playRecordingLayout.setVisibility(View.VISIBLE);
            holder.gpsImageIndicator.setVisibility(View.GONE);
            holder.message.setVisibility(View.GONE);
            holder.contactLayout.setVisibility(View.GONE);
            holder.linkMessageLayout.setVisibility(View.GONE);
            holder.imageLayout.setVisibility(View.GONE);
        } else if (message.getMessageType() == MessageType.webMessage.ordinal()) {
            holder.linkMessageLayout.setVisibility(View.VISIBLE);
            holder.contactLayout.setVisibility(View.GONE);
            holder.playRecordingLayout.setVisibility(View.GONE);
            holder.gpsImageIndicator.setVisibility(View.GONE);
            holder.imageLayout.setVisibility(View.GONE);
            if (message.getMessage() == null || message.getMessage().equals(""))
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
                            holder.message.setText(message.getMessage());
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
            web.downloadWebPreview(message.getMessage());
        } else if (message.getMessageType() == MessageType.videoMessage.ordinal()) {
            if (message.getRecordingPath() != null) {
                Uri videoUri = Uri.parse(message.getRecordingPath());
                File file = new File(message.getRecordingPath());
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
                            callback.onVideoClicked(videoUri);
                        else
                            Toast.makeText(holder.itemView.getContext(), "something went wrong, try again later", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }else if (message.getMessageType() == MessageType.gif.ordinal()) {
            holder.imageLayout.setVisibility(View.VISIBLE);
            holder.contactLayout.setVisibility(View.GONE);
            holder.previewImage.setScaleType(ImageView.ScaleType.FIT_XY);
            holder.imageStatusLayout.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext()).load(message.getMessage()).placeholder(R.drawable.ic_baseline_gif_24).addListener(new RequestListener<Drawable>() {
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
            time =  Long.parseLong(message.getMessageID());
        } else {
            time = Long.parseLong(message.getArrivingTime());
        }
        String finalDate = timeFormat.getFormattedDate(time);
        if (message.getConversationID().startsWith("G"))
        {
            String nameAndTime = message.getSenderName() + " " + finalDate;
            holder.timeReceived.setText(nameAndTime);

            if (!message.getSender().equals(currentUserUID)) {
                if (matchedColors == null)
                    matchedColors = new HashMap<>();
              if (matchedColors.containsKey(message.getSender()))
              {
                  int colorCode = matchedColors.get(message.getSender());
                  holder.timeReceived.setTextColor(ResourcesCompat.getColor(holder.itemView.getContext().getResources(),colorCode,holder.itemView.getContext().getTheme()));
              }
              else
              {
                  matchedColors.put(message.getSender(),colors[colorIterator]);
                  holder.timeReceived.setTextColor(ResourcesCompat.getColor(holder.itemView.getContext().getResources(),colors[colorIterator],holder.itemView.getContext().getTheme()));
                  colorIterator++;
                  if (colorIterator > colors.length)
                      colorIterator = 0;
              }
            }
        }
        else
        {
            holder.timeReceived.setText(finalDate);
        }
        if (message.getMessageStatus().equals(ConversationActivity.MESSAGE_WAITING))
            System.out.println();
        if (holder.statusTv != null && message.getMessageStatus() != null) {
            switch (message.getMessageStatus()) {
                case ConversationActivity.MESSAGE_SEEN:
                    holder.statusTv.setImageResource(R.drawable.ic_baseline_done_all_24);
                    break;
                case ConversationActivity.MESSAGE_DELIVERED:
                    holder.statusTv.setImageResource(R.drawable.ic_baseline_done_24);
                    break;
                case ConversationActivity.MESSAGE_WAITING:
                    holder.statusTv.setImageResource(R.drawable.ic_baseline_access_time_black);
                    break;
                default:
                    holder.statusTv.setImageResource(R.drawable.message_sent);
            }
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
            holder.quoteLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onMessageClick(message,v, getItemViewType(holder.getAdapterPosition()));
                }
            });
        } else
        {
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

    public String getMessageID(int position) {
        return messages.get(position).getMessageID();
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @SuppressWarnings("Convert2Lambda")
    public class ChatViewHolder extends RecyclerView.ViewHolder {
        com.vanniktech.emoji.EmojiTextView message;
        TextView timeReceived,messageSender, edit, delete, quote,quoteSenderName, linkTitle, linkContent, contactName, contactPhone, recordingTime, pollVotes, pollCreator;
        ImageView previewImage, linkImage, gpsImageIndicator;
        LinearLayout extraOptionsLayout, playRecordingLayout, videoLayout, imageStatusLayout, quoteLayout, messageTextLayout, linkMessageLayout, innerPaneLayout, pollLayout;
        RelativeLayout imageLayout, contactLayout;
        ImageView statusTv;
        SeekBar voiceSeek;
        ImageButton  playVideoBtn, reUpload, reDownload,refresh;
        NormalObjects.PlayAudioButton playPauseBtn;
        ProgressBar showImageProgress, linkProgressBar;
        ShapeableImageView contactImage;
        Button saveContactBtn, hidePoll;
        ConstraintLayout rootLayout;
        RadioGroup pollGroup;
        @SuppressLint("SetJavaScriptEnabled")
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
//            playPauseBtn = itemView.findViewById(R.id.play_pause_btn);
            videoLayout = itemView.findViewById(R.id.videoLayout);
            playVideoBtn = itemView.findViewById(R.id.playVideoBtn);
            showImageProgress = itemView.findViewById(R.id.imageProgressBar);
            statusTv = itemView.findViewById(R.id.messageStatus);
            reUpload = itemView.findViewById(R.id.reUpload);
            reDownload = itemView.findViewById(R.id.reDownload);
            extraOptionsLayout = itemView.findViewById(R.id.extraOptions);
            edit = itemView.findViewById(R.id.editBtn);
            delete = itemView.findViewById(R.id.deleteBtn);
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
            gpsImageIndicator = itemView.findViewById(R.id.gpsMessageIndicator);
            timeReceived = itemView.findViewById(R.id.messageTime);
            innerPaneLayout = itemView.findViewById(R.id.innerPaneLayout);
            rootLayout = itemView.findViewById(R.id.rootLayout);
            hidePoll = itemView.findViewById(R.id.hidePoll);
            pollLayout = itemView.findViewById(R.id.pollLayout);
            pollVotes = itemView.findViewById(R.id.pollVotes);
            pollCreator = itemView.findViewById(R.id.pollCreatorName);
            pollGroup = itemView.findViewById(R.id.radioGroup);
            hidePoll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (hidePoll.getVisibility() == View.VISIBLE)
                    {
                        pollLayout.setVisibility(View.GONE);
                    }
                }
            });
            pollGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    callback.onRadioBtnClick(radioGroup, i);
                }
            });
            if (edit != null && delete != null) {

                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        callback.onEditMessageClick(messages.get(getAdapterPosition()));
                    }
                });
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        callback.onDeleteMessageClick(messages.get(getAdapterPosition()));
                    }
                });
            }
            message.setTextSize(textSize);

            message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (messages.get(getAdapterPosition()).getMessageType() == MessageType.gpsMessage.ordinal())
                        callback.onPreviewMessageClick(messages.get(getAdapterPosition()));
                    //show options only for messages that i have sent
                    else if (ChatAdapter.this.getItemViewType(getAdapterPosition()) == Messaging.outgoing.ordinal()) {
                        if (extraOptionsLayout.getVisibility() == View.GONE)
                            extraOptionsLayout.setVisibility(View.VISIBLE);
                        else if (extraOptionsLayout.getVisibility() == View.VISIBLE)
                            extraOptionsLayout.setVisibility(View.GONE);
                    }
                    callback.onMessageClick(messages.get(getAdapterPosition()), v, getItemViewType());
                }
            });
            message.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    messageTextLayout.setSelected(!messageTextLayout.isSelected());
                    callback.onMessageLongClick(messages.get(getAdapterPosition()), v, getItemViewType());
                    return true;
                }
            });

            innerPaneLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    v.setSelected(!v.isSelected());
                    callback.onMessageLongClick(messages.get(getAdapterPosition()), v, getItemViewType());
                    return true;
                }
            });
            linkMessageLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onMessageClick(messages.get(getAdapterPosition()), v, getItemViewType());
                }
            });
            linkMessageLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    callback.onMessageLongClick(messages.get(getAdapterPosition()), v, getItemViewType());
                    return true;
                }
            });
//            playPauseBtn.setListener(new PlayAudioButton.onPlayAudio() {
//                @Override
//                public void playAudio() {
//                    AudioManager2 manager = AudioManager2.getInstance();
//                    Message audioMessage = messages.get(getAdapterPosition());
//                    if (audioMessage.getRecordingPath() != null) {
//                        AudioPlayer2 player = manager.getAudioPlayer(audioMessage.getRecordingPath());
//                        if (voiceSeek.getMax() != player.getDuration() / 1000) {
//                            voiceSeek.setMax(player.getDuration() / 1000);
//                            voiceSeek.setMin(0);
//                        }
//                        TimeFormat format = new TimeFormat();
//                        player.setAudioListener(new AudioHelper() {
//                            @Override
//                            public void onProgressChange(String formattedProgress, int progress) {
//                                String time = formattedProgress + "/" + format.getFormattedTime(player.getDuration());
//                                new Handler(Looper.getMainLooper()).post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        recordingTime.setText(time);
//                                        voiceSeek.setProgress(progress, true);
//                                        if (progress == player.getDuration() / 1000)
//                                            playPauseBtn.resetClicks();
//                                    }
//                                });
//                                manager.updateProgress(audioMessage.getRecordingPath(), progress);
//                            }
//
//                            @Override
//                            public void onPlayingStatusChange(boolean isPlaying) {
//
//                            }
//                        });
//                        int progress = manager.getProgress(audioMessage.getRecordingPath());
//                        player.seekTo(progress * 1000);
//                        voiceSeek.setProgress(progress);
//                        player.playPauseAudio();
//                    }
//                }
//
//                @Override
//                public void pauseAudio() {
//                    AudioManager2 manager = AudioManager2.getInstance();
//                    Message audioMessage = messages.get(getAdapterPosition());
//                    if (audioMessage.getRecordingPath() != null) {
//                        AudioPlayer2 player = manager.getAudioPlayer(audioMessage.getRecordingPath());
//                        player.playPauseAudio();
//                    }
//                }
//            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getSender().equals(currentUserUID))
            return Messaging.outgoing.ordinal();
        else
            return Messaging.incoming.ordinal();
    }

    public int findMessageLocation(ArrayList<Message> messages, int min, int max, long key) {
        return findMessage(this.messages, min, max, String.valueOf(key));
        //return findCorrectMessage(this.messages,min,max,key);
    }

    public ArrayList<Integer> SearchMessage(String searchQuery) {
        ArrayList<Integer> searchQueryIndexes = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getMessage() != null)
                if (messages.get(i).getMessage().contains(searchQuery)) {
                    searchQueryIndexes.add(i);
                }
        }
        return searchQueryIndexes;
    }

    public void UpdateMessageStar(String messageID, boolean star) {
        int index = findMessage(messages, 0, messages.size() - 1, messageID);//findCorrectMessage(messages,0,messages.size()-1,Long.parseLong(messageID));
        Message message = messages.get(index);
        message.setStar(star);
        notifyItemChanged(index);
    }

    public int updateMessageEdit(String messageID, String content, String time) {
        int index = findMessage(messages, 0, messages.size() - 1, messageID);//findCorrectMessage(messages,0,messages.size()-1,Long.parseLong(messageID));
        Message message = messages.get(index);
        message.setMessage(content);
        message.setEditTime(time);
        notifyItemChanged(index);
        return index;
    }

    public Message getMessage(int index) {
        if (index < getItemCount() && index >= 0)
            return messages.get(index);
        else return null;
    }

    private int findMessage(ArrayList<Message> messages, int min, int max, String key) {
        if (max >= min) {
            int mid = min + (max - min) / 2;
            if (messages.get(mid).getMessageID().equals(key))
                return mid;
            if (Long.parseLong(messages.get(mid).getMessageID()) > Long.parseLong(key))
                return findMessage(messages, min, mid - 1, key);
            else
                return findMessage(messages, mid + 1, max, key);
        }
        return -1;
    }

    public boolean isMessageExists(String messageID) {
        int index = findMessage(messages, 0, messages.size() - 1, messageID);
        return index != -1;
    }
}
