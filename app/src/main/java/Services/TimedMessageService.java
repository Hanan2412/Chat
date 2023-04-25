package Services;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.woofmeow.ConversationActivity2;

import java.math.BigInteger;
import java.util.ArrayList;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import Backend.ChatDao;
import Backend.ChatDataBase;

import Model.MessageSender;
import NormalObjects.Message;


@SuppressWarnings({"Convert2Lambda", "AnonymousHasLambdaAlternative"})
public class TimedMessageService extends Service{

    private int notificationID;
    private final String FOREGROUND_SERVICE = "TimedMessageService";
    private AlarmManager.OnAlarmListener alarmListener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressWarnings("unchecked")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (!intent.getBooleanExtra("stop", false)) {
            String ChannelID = CreateChannelID();
            Message message = (Message) intent.getSerializableExtra("message");
            if (message != null) {
                Log.d(FOREGROUND_SERVICE, "stated foreground service - timed message");
                long x = message.getMessageID();
                BigInteger bigInteger = BigInteger.valueOf(x);
                notificationID = bigInteger.intValue();
                List<String> tokens = (List<String>)intent.getSerializableExtra("tokens");
                long time = intent.getLongExtra("time",-1);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(time);
                Date date1 = calendar.getTime();
                Log.d("time11111", date1.toString());
                Intent conversationIntent = new Intent(this, ConversationActivity2.class);
                conversationIntent.putExtra("conversationID", message.getConversationID());
                conversationIntent.putExtra("recipient", message.getConversationName());
                conversationIntent.putExtra("recipientToken", (ArrayList<String>) tokens);

                PendingIntent alarmPendingIntent = PendingIntent.getActivity(this, 120, conversationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ChannelID);
                Intent cancelIntent = new Intent(this, TimedMessageService.class);
                cancelIntent.putExtra("stop", true);
                cancelIntent.putExtra("notificationID", notificationID);
                PendingIntent cancelPendingIntent = PendingIntent.getForegroundService(this.getApplicationContext(), notificationID, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                NotificationCompat.Action action = new NotificationCompat.Action.Builder(android.R.drawable.ic_media_play, "cancel", cancelPendingIntent)
                        .build();
                builder.setSmallIcon(android.R.drawable.star_on)
                        .setContentTitle("waiting to send message")
                        .setContentText("message: " + message.getContent() + " will be sent to: " + message.getConversationName())
                        .setSubText("will be sent at: " + calendar.getTime())
                        .setContentIntent(alarmPendingIntent).addAction(action);
                alarmListener = new AlarmManager.OnAlarmListener() {
                    @Override
                    public void onAlarm() {
                        if(tokens!=null) {
                            MessageSender sender = MessageSender.getInstance();
                            ChatDataBase chatDataBase = ChatDataBase.getInstance(TimedMessageService.this);
                            ChatDao chatDao = chatDataBase.chatDao();
                            message.setSendingTime(System.currentTimeMillis());
                            sender.sendMessage(message, tokens);
                            Thread thread = new Thread(){
                                @Override
                                public void run() {
                                    chatDao.insertNewMessage(message);
                                    chatDao.updateConversationLastMessage(message.getConversationID(), message.getContent());
                                }
                            };
                            thread.setName("Timed msg save msg");
                            thread.start();
                            Intent forgroundServiceIntent = new Intent("delayedMessage");
                            forgroundServiceIntent.putExtra("message", message);
                            LocalBroadcastManager.getInstance(TimedMessageService.this).sendBroadcast(forgroundServiceIntent);
                            //DBActive dbActive = DBActive.getInstance(TimedMessageService.this);
                            //dbActive.saveMessage(message);
                            Log.d(FOREGROUND_SERVICE, "stopped foreground service - onAlarm");
                        }
                        else
                            Log.e("NULL-ERROR","token is null in TimedService");
                        stopForeground(true);
                    }
                };
                if (manager != null) {
                    Log.d("time11111", time+"");
                    manager.setExact(AlarmManager.RTC_WAKEUP, time, "alarm manager - send delay message", alarmListener, new Handler());
                }
                startForeground(notificationID, builder.build());

            }
        }
        else
        {
            int notificationID = intent.getIntExtra("notificationID",-1);
            if(notificationID == this.notificationID) {
                Log.d(FOREGROUND_SERVICE, "service is stopped");
                manager.cancel(alarmListener);
                stopForeground(true);
            }
            else if(notificationID == -1)
                Log.e(FOREGROUND_SERVICE,"notificationID is -1");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private String CreateChannelID() {
        CharSequence channelName = "Timed Message";
        String description = "send a message at the time you picked";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        String CHANNEL_ID = "Timed Message";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        } else
            Log.e("notification channel", "notification manager in foregroundService is null");
        return CHANNEL_ID;
    }
}
