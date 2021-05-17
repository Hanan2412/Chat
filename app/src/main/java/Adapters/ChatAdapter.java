package Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.media.MediaPlayer;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.woofmeow.ConversationActivity;
import com.example.woofmeow.R;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;


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

import javax.net.ssl.HttpsURLConnection;

import Consts.MessageType;
import Consts.Messaging;
import NormalObjects.Message;




@SuppressWarnings("Convert2Lambda")
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private ArrayList<Message> messages = new ArrayList<>();
    private String currentUserUID;
    private Context context;
    private Bitmap bitmap;
    private boolean playing = false;
    private MediaPlayer player = null;
    private String ERROR = "CHAT_ADAPTER_ERROR";
    private HashMap<Integer,String>paths = new HashMap<>();


    private ArrayList<Integer> voiceMessagesIndex = new ArrayList<>();
    private HashMap<Integer,MediaPlayer>players = new HashMap<>();
    private HashMap<Integer,SeekBar>seeks = new HashMap<>();
    private HashMap<Integer,TextView>playBackTimes = new HashMap<>();



    public interface MessageInfoListener {
        void onMessageClick(Message message, View view, int viewType);

        void onMessageLongClick(Message message, View view, int viewType);

        void onPreviewMessageClick(Message message);

        void onDeleteMessageClick(Message message);

        void onEditMessageClick(Message message);

        String onImageDownloaded(Bitmap bitmap,Message message);
    }

    private MessageInfoListener callback;

    public void setCurrentUserUID(String currentUserUID) {
        this.currentUserUID = currentUserUID;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public void addNewMessage(Message message){
        if(messages==null)
            messages = new ArrayList<>();
        messages.add(message);

    }


    public void changeExistingMessage(Message message,int position)
    {
        if(messages!=null)
        {
            int messageIndex = findCorrectMessage(messages,0,messages.size()-1,Long.parseLong(message.getMessageID()));
            if (messageIndex!=-1)
            {
                messages.set(messageIndex,message);
                notifyItemChanged(messageIndex);
            }
            else Log.e(ERROR,"messageIndex is -1");
        }
    }

    private int findCorrectMessage(ArrayList<Message> messages, int min, int max, long key)
    {

        int mid = (max + min) / 2;
        long midKey = Long.parseLong(messages.get(mid).getMessageID());
        if (midKey > key) {
            if (max == mid)
            {
                if (key == Long.parseLong(messages.get(min).getMessageID()))
                    return min;
            }
            max = mid;
            mid = findCorrectMessage(messages, min, max, key);
        } else if (midKey < key) {
            if (min == mid)
            {
                if (key == Long.parseLong(messages.get(max).getMessageID()))
                    return max;
            }
            min = mid;
            mid = findCorrectMessage(messages, min, max, key);
        }
        if (messages.get(mid).getMessageID().equals(String.valueOf(key)))
            return mid;
        return -1;
    }

    public void setListener(MessageInfoListener listener) {
        callback = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
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
                System.out.println("error in creating viewHolder in chatAdapter");
                break;
        }
        return new ChatAdapter.ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        final Message message = messages.get(position);
        holder.message.setText(message.getMessage());
        if (message.getMessageType() == MessageType.textMessage.ordinal())
        {
            holder.previewImage.setVisibility(View.GONE);
            holder.playRecordingLayout.setVisibility(View.GONE);
        }
        else if (message.getMessageType() == MessageType.gpsMessage.ordinal())
        {
            holder.previewImage.setVisibility(View.VISIBLE);
            holder.playRecordingLayout.setVisibility(View.GONE);
        }
        else if (message.getMessageType() == MessageType.photoMessage.ordinal()) {
            holder.previewImage.setVisibility(View.VISIBLE);
            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    ChatAdapter.this.bitmap = bitmap;
                   String path =  callback.onImageDownloaded(bitmap,message);
                   if (path!=null)
                   {
                       Bitmap bitmap1 = BitmapFactory.decodeFile(path);
                       holder.previewImage.setImageBitmap(bitmap1);
                       //Picasso.get().load(path).into(holder.previewImage);
                   }
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    e.printStackTrace();
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };
            Picasso.get().load(message.getImagePath()).into(target);

            //Picasso.get().load(message.getImagePath()).into(holder.previewImage);
            holder.playRecordingLayout.setVisibility(View.GONE);
        } else if (message.getMessageType() == MessageType.VoiceMessage.ordinal()) {

            holder.messageTextLayout.setVisibility(View.VISIBLE);
            holder.playRecordingLayout.setVisibility(View.VISIBLE);
            holder.previewImage.setVisibility(View.GONE);
            if(holder.statusTv!=null)
                 holder.statusTv.setVisibility(View.GONE);
            voiceMessagesIndex.add(position);
            if (message.getRecordingPath() != null) {
                StorageReference downloadAudioFile = FirebaseStorage.getInstance().getReferenceFromUrl(message.getRecordingPath());
                try {
                    File file = File.createTempFile("recording" + message.getMessageID(), ".3gpp");
                    downloadAudioFile.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                            player = new MediaPlayer();
                            String path = file.getAbsolutePath();
                            paths.put(position,path);
                            try {
                                player.setDataSource(path);
                                player.prepare();
                                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mp) {
                                        holder.playPauseBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_white);
                                        holder.voiceSeek.setProgress(0);
                                        holder.message.setText(R.string.zero_time);
                                    }
                                });
                                players.put(position,player);
                                holder.voiceSeek.setMax(player.getDuration());
                                holder.voiceSeek.setProgress(0);
                                holder.voiceSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                    @Override
                                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                                    }

                                    @Override
                                    public void onStartTrackingTouch(SeekBar seekBar) {

                                    }

                                    @Override
                                    public void onStopTrackingTouch(SeekBar seekBar) {
                                        if(seekBar.getProgress()/1000<10)
                                        {
                                            String progress = "00:0" + seekBar.getProgress()/1000;
                                            holder.message.setText(progress);
                                        }
                                        else
                                        {
                                            String progress = "00:" + seekBar.getProgress()/1000;
                                            holder.message.setText(progress);
                                        }
                                    }
                                });
                                seeks.put(position,holder.voiceSeek);
                                holder.message.setText(R.string.zero_time);
                                holder.message.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                holder.message.setGravity(Gravity.CENTER);
                                playBackTimes.put(position,holder.message);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            System.out.println("failed to download audio file");
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }else if(message.getMessageType() == MessageType.webMessage.ordinal())
        {
            Thread downloadWeb = new Thread()
            {
                @Override
                public void run() {
                    super.run();

                    try{
                        if(holder.linkMessage!=null){

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
                            if(responseCode == 200)
                            {
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
                                                callback.onMessageClick(message,v,message.getMessageType());
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


                    }catch (IOException  | IllegalArgumentException e){
                        System.out.println("error in getting link image");
                    }
                }
            };
            downloadWeb.setName("linkMessage");
            downloadWeb.start();
        }

        Calendar calendar = Calendar.getInstance();

        //show the time of the messages i sent
        if (holder.getItemViewType() == Messaging.outgoing.ordinal()) {
            try {
                long timeSent = Long.parseLong(message.getMessageTime());
                calendar.setTimeInMillis(timeSent);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

        } else {
            calendar.setTimeInMillis(message.getReadAt());
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
                        System.out.println("in playing is true");
                        holder.playPauseBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_white);
                         onPlay(false, position);
                    } else {
                        System.out.println("in playing is false");
                        holder.playPauseBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_white);
                        onPlay(true, position);
                    }
                }
            });
        }



    }

   public String getMessageID(int position)
   {
       return messages.get(position).getMessageID();
   }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @SuppressWarnings("Convert2Lambda")
    public class ChatViewHolder extends RecyclerView.ViewHolder {

        TextView message, timeReceived, edit, delete, quote, linkTitle,linkContent;
        ImageView previewImage,linkImage;
        LinearLayout extraOptionsLayout, bigPictureLayout, playRecordingLayout;
        RelativeLayout messageTextLayout,linkMessage;
        ImageView statusTv, bigPicture;
        SeekBar voiceSeek;
        ImageButton playPauseBtn;



        @SuppressLint("SetJavaScriptEnabled")
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            messageTextLayout = itemView.findViewById(R.id.messageTextLayout);
            playRecordingLayout = itemView.findViewById(R.id.playRecordingLayout);
            voiceSeek = itemView.findViewById(R.id.voiceSeek);
            playPauseBtn = itemView.findViewById(R.id.play_pause_btn);


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
                    callback.onMessageLongClick(messages.get(getAdapterPosition()),v,getItemViewType());
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

    private void startPlaying(int position)
    {

        MediaPlayer player = players.get(position);
        SeekBar seek = seeks.get(position);
        TextView playback = playBackTimes.get(position);
        if(player!=null && seek!=null && playback!=null)
        {
            player.start();
            playback.setGravity(Gravity.CENTER);
            playback.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            Thread thread = new Thread(){
                @Override
                public void run() {
                    super.run();
                    while(player.isPlaying())
                    {
                        int currentPosition = player.getCurrentPosition();
                        seek.setProgress(currentPosition);
                        if(currentPosition/1000 < 10)
                        {
                            String post = "00:0" + currentPosition/1000;
                            playback.setText(post);
                        }
                        else
                        {
                            String post = "00:" + currentPosition/1000;
                            playback.setText(post);
                        }

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

    public int findQuotedMessageLocation(ArrayList<Message>messages,int min,int max,long key)
    {
        if(messages == null)
            messages = this.messages;
        int mid = (max + min)/2;
        long midKey = Long.parseLong(messages.get(mid).getMessageID());
        if(midKey > key)
        {
            if(mid == max)
                return -1;
            max = mid;
            mid = findQuotedMessageLocation(messages,min,max,key);
        }
        else if (midKey < key)
        {
            if(mid == min)
                return -1;
            min = mid;
            mid = findQuotedMessageLocation(messages,min,max,key);
        }
        if(mid!=-1)
            if (messages.get(mid).getMessageID().equals(String.valueOf(key)))
             return mid;
        return -1;
    }

    public ArrayList<Integer> SearchMessage(String searchQuery)
    {
        ArrayList<Integer>searchQueryIndexes = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++)
        {
            if (messages.get(i).getMessage().contains(searchQuery))
            {
                searchQueryIndexes.add(i);
            }
        }
        return searchQueryIndexes;
    }
}
