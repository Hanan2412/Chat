package Adapters;

import android.annotation.SuppressLint;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.woofmeow.ConversationActivity;
import com.example.woofmeow.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import Consts.MessageType;
import Consts.Messaging;
import Audio.AudioHelper;
import Audio.AudioManager;
import Audio.AudioPlayer;
import NormalObjects.FileManager;
import NormalObjects.Message;
import NormalObjects.Web;


@SuppressWarnings("Convert2Lambda")
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private ArrayList<Message> messages = new ArrayList<>();
    private String currentUserUID;
    private final String ERROR = "CHAT_ADAPTER_ERROR";
    private float textSize = 30;


    public interface MessageInfoListener {
        void onMessageClick(Message message, View view, int viewType);

        void onMessageLongClick(Message message, View view, int viewType);

        void onPreviewMessageClick(Message message);

        void onDeleteMessageClick(Message message);

        void onEditMessageClick(Message message);

        String onImageDownloaded(Bitmap bitmap, Message message);

        String onVideoDownloaded(File file, Message message);

        void onVideoClicked(Uri uri);

        //void onUpdateMessageStatus(Message message);
    }

    private MessageInfoListener callback;

    public void setCurrentUserUID(String currentUserUID) {
        this.currentUserUID = currentUserUID;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public void addNewMessage(Message message) {
        if (messages == null)
            messages = new ArrayList<>();
        messages.add(message);
        notifyItemInserted(getItemCount() - 1);
    }

    public void DeleteMessage(String messageID) {
        int index = findMessage(messages, 0, messages.size() - 1, messageID);
        messages.remove(index);
        notifyItemRemoved(index);
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

    public void UpdateMessageStatus(String id, String status, String time) {
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
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.incoming_message, parent, false);
                break;
            }
            case outgoing: {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.outgoing_message, parent, false);
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
        if (message.isStar()) {
            holder.message.setCompoundDrawablesRelativeWithIntrinsicBounds(android.R.drawable.star_on, 0, 0, 0);
        }
        if (message.getMessageType() == MessageType.textMessage.ordinal()) {
            holder.previewImage.setVisibility(View.GONE);
            holder.playRecordingLayout.setVisibility(View.GONE);
        } else if (message.getMessageType() == MessageType.gpsMessage.ordinal()) {
            holder.previewImage.setVisibility(View.VISIBLE);
            holder.playRecordingLayout.setVisibility(View.GONE);
        } else if (message.getMessageType() == MessageType.contact.ordinal()) {
            holder.linkMessage.setVisibility(View.VISIBLE);
            holder.linkImage.setImageResource(R.drawable.ic_baseline_contacts_24);
            holder.linkImage.setBackground(ResourcesCompat.getDrawable(holder.itemView.getContext().getResources(), R.drawable.conversation_cell_not_selected, holder.itemView.getContext().getTheme()));
            holder.linkContent.setText(message.getContactName());
            holder.linkTitle.setText(message.getContactPhone());
            holder.message.setVisibility(View.GONE);
        } else if (message.getMessageType() == MessageType.photoMessage.ordinal()) {
            holder.previewImage.setVisibility(View.VISIBLE);
            if (message.getImagePath() != null) {
                FileManager fm = FileManager.getInstance();
                fm.setListener(new FileManager.onLoadingImage() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {//the call is happening from a different thread so handler is a must
                        new Handler(holder.itemView.getContext().getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                holder.previewImage.setScaleType(ImageView.ScaleType.FIT_XY);
                                holder.previewImage.setImageBitmap(bitmap);
                            }
                        });
                    }

                    @Override
                    public void onFailed() {
                        new Handler(holder.itemView.getContext().getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(holder.itemView.getContext(), "Failed loading an image", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                fm.readImageMessage(message.getImagePath(), holder.itemView.getContext());
            }
            holder.playRecordingLayout.setVisibility(View.GONE);
        } else if (message.getMessageType() == MessageType.VoiceMessage.ordinal()) {

            holder.messageTextLayout.setVisibility(View.VISIBLE);
            holder.playRecordingLayout.setVisibility(View.VISIBLE);
            holder.previewImage.setVisibility(View.GONE);
            holder.message.setText(R.string.zero_time);
            holder.message.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            holder.message.setGravity(Gravity.CENTER);

        } else if (message.getMessageType() == MessageType.webMessage.ordinal()) {
            holder.linkMessage.setVisibility(View.VISIBLE);
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
                        }
                    });
                }

                @Override
                public void onWebImageSuccess(Bitmap bitmap) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            holder.linkImage.setImageBitmap(bitmap);
                            holder.linkImage.setScaleType(ImageView.ScaleType.FIT_XY);
                            holder.message.setVisibility(View.GONE);
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
        }
        // }

        Calendar calendar = Calendar.getInstance();

        //show the time of the messages i sent
        if (holder.getItemViewType() == Messaging.outgoing.ordinal()) {
            try {
                long timeSent;
                if (message.getSendingTime() != null)
                    timeSent = Long.parseLong(message.getSendingTime());
                else
                    timeSent = Long.parseLong(message.getMessageID());
                calendar.setTimeInMillis(timeSent);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

        } else {
            calendar.setTimeInMillis(Long.parseLong(message.getArrivingTime()));
        }
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);

        String minuteW = minute + "", hourW = hour + "", secondsW = seconds + "", monthW = month + 1 + "";
        if (minute < 10)
            minuteW = "0" + minute + "";
        if (hour < 10)
            hourW = "0" + hour;
        if (seconds < 10)
            secondsW = "0" + seconds;
        if (month < 10)
            monthW = "0" + month;
        String time = hourW + ":" + minuteW + ":" + secondsW;
        String date = day + "/" + monthW + "/" + year;
        String finalDate = date + " " + time;
        holder.timeReceived.setText(finalDate);

        if (holder.extraOptionsLayout != null)
            holder.extraOptionsLayout.setVisibility(View.GONE);
        if (holder.statusTv != null && message.getMessageStatus() != null)
            switch (message.getMessageStatus()) {
                case ConversationActivity.MESSAGE_SEEN:
                    holder.statusTv.setImageResource(R.drawable.ic_baseline_done_all_24);
                    break;
                case ConversationActivity.MESSAGE_DELIVERED:
                    holder.statusTv.setImageResource(R.drawable.ic_baseline_done_24);
                    break;
                case ConversationActivity.MESSAGE_WAITING:
                    holder.statusTv.setImageResource(R.drawable.ic_baseline_done_outline_24);
                    break;
                default:
                    holder.statusTv.setImageResource(R.drawable.ic_baseline_cast_connected_24);
            }

        if (message.getQuoteMessage() != null && !message.getQuoteMessage().equals("")) {
            holder.quote.setVisibility(View.VISIBLE);
            holder.quote.setText(message.getQuoteMessage());
        } else
            holder.quote.setVisibility(View.GONE);


        if (holder.playPauseBtn != null) {
            holder.playPauseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AudioManager manager = AudioManager.getInstance();
                    if (messages.get(position).getRecordingPath()!=null) {
                        AudioPlayer audioPlayer = manager.getAudioPlayer(messages.get(position).getRecordingPath(), holder.voiceSeek);
                        audioPlayer.setListener(new AudioHelper() {
                            @Override
                            public void onProgressChange(String formattedProgress, int progress) {
                                holder.message.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                holder.message.setGravity(Gravity.CENTER);
                                holder.message.setText(formattedProgress);
                            }

                            @Override
                            public void onPlayingStatusChange(boolean isPlaying) {
                                if (isPlaying)
                                    holder.playPauseBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_white);
                                else
                                    holder.playPauseBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_white);
                            }
                        });
                        audioPlayer.playPauseAudio();
                    }
                    else
                        Toast.makeText(holder.itemView.getContext(), "error accrued while trying to play recording", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        AudioManager manager = AudioManager.getInstance();
        for (Message message: messages)
        {
            if (message.getMessageType() == MessageType.VoiceMessage.ordinal())
                manager.releasePlayer(message.getRecordingPath());
        }
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

        TextView message, timeReceived, edit, delete, quote, linkTitle, linkContent;
        ImageView previewImage, linkImage;
        LinearLayout extraOptionsLayout, bigPictureLayout, playRecordingLayout, videoLayout;
        RelativeLayout messageTextLayout, linkMessage;
        ImageView statusTv, bigPicture;
        SeekBar voiceSeek;
        ImageButton playPauseBtn, playVideoBtn;


        @SuppressLint("SetJavaScriptEnabled")
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            messageTextLayout = itemView.findViewById(R.id.messageTextLayout);
            playRecordingLayout = itemView.findViewById(R.id.playRecordingLayout);
            voiceSeek = itemView.findViewById(R.id.voiceSeek);
            playPauseBtn = itemView.findViewById(R.id.play_pause_btn);

            videoLayout = itemView.findViewById(R.id.videoLayout);
            playVideoBtn = itemView.findViewById(R.id.playVideoBtn);

            statusTv = itemView.findViewById(R.id.messageStatus);

            extraOptionsLayout = itemView.findViewById(R.id.extraOptions);
            edit = itemView.findViewById(R.id.editBtn);
            delete = itemView.findViewById(R.id.deleteBtn);


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
            message = itemView.findViewById(R.id.message);
            message.setTextSize(textSize);
            timeReceived = itemView.findViewById(R.id.messageTime);
            message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //show options only for messages that i sent
                    if (ChatAdapter.this.getItemViewType(getAdapterPosition()) == Messaging.outgoing.ordinal()) {
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
                    callback.onMessageLongClick(messages.get(getAdapterPosition()), v, getItemViewType());
                    return true;
                }
            });

            previewImage = itemView.findViewById(R.id.previewImage);
            previewImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (messages.get(getAdapterPosition()).getMessageType() == MessageType.gpsMessage.ordinal())
                        callback.onPreviewMessageClick(messages.get(getAdapterPosition()));

                }
            });
            bigPicture = itemView.findViewById(R.id.bigPicture);
            bigPictureLayout = itemView.findViewById(R.id.bigPictureLayout);
            quote = itemView.findViewById(R.id.quoteText);

            linkMessage = itemView.findViewById(R.id.linkMessage);
            linkMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onMessageClick(messages.get(getAdapterPosition()), v, getItemViewType());
                }
            });
            linkContent = itemView.findViewById(R.id.linkContent);
            linkTitle = itemView.findViewById(R.id.linkTitle);
            linkImage = itemView.findViewById(R.id.linkImage);
            linkMessage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    callback.onMessageLongClick(messages.get(getAdapterPosition()), v, getItemViewType());
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getSender().equals(currentUserUID))
            return Messaging.outgoing.ordinal();
        else
            return Messaging.incoming.ordinal();
    }

    public int findQuotedMessageLocation(ArrayList<Message> messages, int min, int max, long key) {
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

    public int UpdateMessageEdit(String messageID, String content, String time) {
        int index = findMessage(messages, 0, messages.size() - 1, messageID);//findCorrectMessage(messages,0,messages.size()-1,Long.parseLong(messageID));
        Message message = messages.get(index);
        message.setMessage(content);
        message.setEditTime(time);
        notifyItemChanged(index);
        return index;
    }

    public Message getMessage(int index) {
        if (index < getItemCount())
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
