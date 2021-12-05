package Adapters;

import android.annotation.SuppressLint;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
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
import androidx.recyclerview.widget.RecyclerView;
import com.example.woofmeow.ConversationActivity;
import com.example.woofmeow.R;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.HttpsURLConnection;
import Consts.MessageType;
import Consts.Messaging;
import NormalObjects.FileManager;
import NormalObjects.Message;


@SuppressWarnings("Convert2Lambda")
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private ArrayList<Message> messages = new ArrayList<>();
    private String currentUserUID;
    private boolean playing = false;
    private MediaPlayer player = null;
    private final String ERROR = "CHAT_ADAPTER_ERROR";
    private float textSize = 30;
    private HashMap<Integer, MediaPlayer> players = new HashMap<>();
    private HashMap<Integer, SeekBar> seeks = new HashMap<>();
    private HashMap<Integer, TextView> playBackTimes = new HashMap<>();


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

    public void DeleteMessage(String messageID)
    {
        int index = findMessage(messages,0,messages.size()-1,messageID);
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

    public void UpdateMessageStatus(String id,String status,String time)
    {
        Log.d("messageStatus","updating message status");
        int index = findMessage(messages,0,messages.size()-1,id);
        if(index!=-1) {
            Message message = messages.get(index);
            message.setMessageStatus(status);
            message.setReadAt(Long.parseLong(time));
            notifyItemChanged(index);
        }  else
            Log.e("MESSAGE_ID ERROR","didn't find message in messages");
    }

    public void UpdateMessageImage(String messageID)
    {
        int index = findMessage(messages,0,messages.size()-1,messageID);
        if(index!=-1)
        {
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
                Log.e(ERROR,"error in creating viewHolder in chatAdapter");
                break;
        }
        return new ChatAdapter.ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final Message message = messages.get(position);
       /* if (!message.getMessageStatus().equals(ConversationActivity.MESSAGE_SEEN))
            callback.onUpdateMessageStatus(message);*/
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
        } else if (message.getMessageType() == MessageType.photoMessage.ordinal()) {
            holder.previewImage.setVisibility(View.VISIBLE);
            if (message.getImagePath()!=null) {
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                Callable<Bitmap> bitmapCallable = new Callable<Bitmap>() {
                    @Override
                    public Bitmap call() {
                        Bitmap bitmap = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            ContentResolver resolver = holder.itemView.getContext().getApplicationContext().getContentResolver();
                            try {
                                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, Uri.parse(message.getImagePath())));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else
                            bitmap = BitmapFactory.decodeFile(message.getImagePath());
                        if (bitmap != null) {
                            float ratio = (float) bitmap.getWidth() / (float) bitmap.getHeight();
                            int width = 540;
                            int height;
                            if (bitmap.getWidth() > bitmap.getHeight())
                                height = (int) (width / ratio);
                            else
                                height = (int) (width * ratio);

                            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
                        }
                        return bitmap;
                    }
                };
                Future<Bitmap> bitmapFuture = executorService.submit(bitmapCallable);
                Thread doneThread = new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        while(!bitmapFuture.isDone())
                        {
                            /*
                            * waiting for the picture to load
                            * since get method blocks and makes the app freeze until the image is loaded
                            * noticeable with multiple images
                            * */
                        }
                        try {
                            Bitmap bitmap = bitmapFuture.get();
                            new Handler(holder.itemView.getContext().getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    holder.previewImage.setImageBitmap(bitmap);
                                }
                            });
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            executorService.shutdown();
                        }

                    }
                };
                doneThread.setName("doneThread");
                doneThread.start();
                /*try {
                    Bitmap bitmap = bitmapFuture.get();
                    holder.previewImage.setImageBitmap(bitmap);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    executorService.shutdown();
                }*/


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
            Thread downloadWeb = new Thread() {
                @Override
                public void run() {
                    super.run();

                    try {
                        if (holder.linkMessage != null) {

                            Document doc = Jsoup.connect(message.getMessage()).userAgent("Mozilla").get();
                            String title = doc.title();
                            // Elements meta = doc.select("meta[property=og:url]");
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
                                        holder.linkMessage.setVisibility(View.VISIBLE);
                                        holder.linkImage.setImageBitmap(bitmap);
                                        holder.linkImage.setScaleType(ImageView.ScaleType.FIT_XY);
                                        holder.linkContent.setText(description);
                                        holder.linkTitle.setText(title);
                                        holder.linkMessage.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                callback.onMessageClick(message, v, message.getMessageType());
                                            }
                                        });
                                        holder.message.setVisibility(View.GONE);
                                    }
                                });

                                inputStream.close();
                                httpsURLConnection.disconnect();
                            }
                            httpsURLConnection.disconnect();

                        }


                    } catch (IOException | IllegalArgumentException e) {
                        System.out.println("error in getting link image");
                    }
                }
            };
            downloadWeb.setName("linkMessage");
            downloadWeb.start();
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
                                    Size size = new Size(holder.playVideoBtn.getWidth(),holder.playVideoBtn.getHeight());
                                    Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(file, size, new CancellationSignal());
                                    BitmapDrawable bitmapDrawable = new BitmapDrawable(holder.itemView.getResources(),thumbnail);
                                    holder.playVideoBtn.setBackground(bitmapDrawable);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    else
                    {
                        Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Images.Thumbnails.MINI_KIND);
                        BitmapDrawable bitmapDrawable = new BitmapDrawable(holder.itemView.getResources(),thumbnail);
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
                if(message.getSendingTime() != null)
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
                    if (playing) {
                        Log.d("playing voice message", "voice message is paused");
                        holder.playPauseBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_white);
                        //onPlay(false, position);
                        onPlay1(false,position, holder.voiceSeek, holder.message, holder.playPauseBtn);
                    } else {
                        Log.d("playing voice message","voice message is playing");
                        holder.playPauseBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_white);
                        //onPlay(true, position);
                        onPlay1(true,position,holder.voiceSeek, holder.message,holder.playPauseBtn);
                    }
                }
            });
        }
    }

    public void setTextSize(float textSize){this.textSize = textSize;}
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


    private void onPlay(boolean start, int position) {

        playing = !playing;

        if (start)
            startPlaying(position);
        else
            pausePlaying(position);
    }

    private void onPlay1(boolean start,int position,SeekBar seek,TextView textView,ImageButton playPauseBtn)
    {
        playing = !playing;

        if (start)
            startPlaying1(position,seek,textView,playPauseBtn);
        else
            pausePlaying1();
    }

    private void startPlaying1(int position,SeekBar seek,TextView text,ImageButton playPauseBtn)
    {
        if(player!=null)
        {
            //player.stop();
            player.release();

        }
        player = new MediaPlayer();
       String recordingPath = messages.get(position).getRecordingPath();
        if(recordingPath!=null) {
            try {
                player.setDataSource(recordingPath);
                player.prepare();
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        playPauseBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_white);
                        text.setText(R.string.zero_time);
                        seek.setProgress(0);
                        player.stop();
                        player.release();
                        playing = !playing;
                    }
                });
                if(seek.getProgress() != 0)
                    player.seekTo(seek.getProgress());
                else
                    seek.setProgress(0);
                seek.setMax(player.getDuration());
                text.setText(R.string.zero_time);
                text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                text.setGravity(Gravity.CENTER);
                seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        if (seekBar.getProgress() / 1000 < 10) {
                            String progress = "00:0" + seekBar.getProgress() / 1000;
                            text.setText(progress);
                        } else {
                            String progress = "00:" + seekBar.getProgress() / 1000;
                            text.setText(progress);
                        }
                    }
                });
                player.start();
                Thread seekThread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        while (player.isPlaying()) {
                            int currentPosition = player.getCurrentPosition();
                            seek.setProgress(currentPosition);
                            String post;
                            if (currentPosition / 1000 < 10) {
                                post = "00:0" + currentPosition / 1000;
                            } else {
                                post = "00:" + currentPosition / 1000;
                            }
                            text.setText(post);
                        }
                    }
                };
                seekThread.setName("playing recording thread");
                seekThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            Log.e("NULL","voice recording path is null");
            Toast.makeText(text.getContext(), "and error happened while trying to play the recording, try again later", Toast.LENGTH_SHORT).show();
        }
    }

    private void pausePlaying1()
    {
        if(player!=null)
            player.pause();
    }

    private void startPlaying(int position) {

        MediaPlayer player = players.get(position);
        SeekBar seek = seeks.get(position);
        TextView playback = playBackTimes.get(position);
        if (player != null && seek != null && playback != null) {
            player.start();
            playback.setGravity(Gravity.CENTER);
            playback.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            Thread thread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    while (player.isPlaying()) {
                        int currentPosition = player.getCurrentPosition();
                        seek.setProgress(currentPosition);
                        String post;
                        if (currentPosition / 1000 < 10) {
                            post = "00:0" + currentPosition / 1000;
                        } else {
                            post = "00:" + currentPosition / 1000;
                        }
                        playback.setText(post);

                    }
                }
            };
            thread.setName("playing recording thread");
            thread.start();
        }

    }


    private void pausePlaying(int position) {

        MediaPlayer player = players.get(position);
        if (player != null)
            player.pause();

    }

    public int findQuotedMessageLocation(ArrayList<Message> messages, int min, int max, long key) {
        return findMessage(this.messages,min,max,String.valueOf(key));
        //return findCorrectMessage(this.messages,min,max,key);
    }

    public ArrayList<Integer> SearchMessage(String searchQuery) {
        ArrayList<Integer> searchQueryIndexes = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getMessage().contains(searchQuery)) {
                searchQueryIndexes.add(i);
            }
        }
        return searchQueryIndexes;
    }

    public void UpdateMessageStar(String messageID,boolean star)
    {
        int index = findMessage(messages,0,messages.size()-1,messageID);//findCorrectMessage(messages,0,messages.size()-1,Long.parseLong(messageID));
        Message message = messages.get(index);
        message.setStar(star);
        notifyItemChanged(index);
    }

    public int UpdateMessageEdit(String messageID,String content,String time)
    {
        int index = findMessage(messages,0,messages.size()-1,messageID);//findCorrectMessage(messages,0,messages.size()-1,Long.parseLong(messageID));
        Message message = messages.get(index);
        message.setMessage(content);
        message.setEditTime(time);
        notifyItemChanged(index);
        return index;
    }

    public Message getMessage(int index)
    {
        if(index < getItemCount())
            return messages.get(index);
        else return null;
    }

    private int findMessage(ArrayList<Message> messages, int min, int max, String key)
    {
        if(max >= min)
        {
            int mid = min + (max-min)/2;
            if(messages.get(mid).getMessageID().equals(key))
                return mid;
            if (Long.parseLong(messages.get(mid).getMessageID()) > Long.parseLong(key))
                return findMessage(messages,min,mid-1,key);
            else
                return findMessage(messages,mid+1,max,key);
        }
        return -1;
    }

    public boolean isMessageExists(String messageID)
    {
        int index = findMessage(messages,0,messages.size()-1,messageID);
        return index != -1;
    }
}
