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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.woofmeow.ConversationActivity;

import java.math.BigInteger;
import java.util.Calendar;

import DataBase.DBActive;
import Model.MessageSender;
import NormalObjects.Message;

@SuppressWarnings("Convert2Lambda")
public class TimedMessageService extends Service{

    private int notificationID;
    private final String FOREGROUND_SERVICE = "TimedMessageService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (!intent.getBooleanExtra("stop", false)) {
            String ChannelID = CreateChannelID();
            Message message = (Message) intent.getSerializableExtra("message");
            if (message != null) {
                Log.d(FOREGROUND_SERVICE, "stated foreground service - timed message");
                long x = Long.parseLong(message.getMessageID());
                BigInteger bigInteger = BigInteger.valueOf(x);
                notificationID = bigInteger.intValue();
                String[] token = intent.getStringArrayExtra("token");
                String time = intent.getStringExtra("time");
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(Long.parseLong(time));
                Intent conversationIntent = new Intent(this, ConversationActivity.class);
                conversationIntent.putExtra("conversationID", message.getConversationID());
                conversationIntent.putExtra("recipient", message.getGroupName());
                conversationIntent.putExtra("recipientToken", token);
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
                        .setContentText("message: " + message.getMessage() + "will be sent to: " + message.getGroupName())
                        .setSubText("message will be sent at: " + calendar.getTime().toString())
                        .setContentIntent(alarmPendingIntent).addAction(action);
                if (manager != null) {
                    manager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), "alarm manager - send delay message", new AlarmManager.OnAlarmListener() {
                        @Override
                        public void onAlarm() {
                            if(token!=null) {
                                MessageSender sender = MessageSender.getInstance();
                                sender.sendMessage(message, token);
                                DBActive dbActive = DBActive.getInstance(TimedMessageService.this);
                                dbActive.saveMessage(message);
                                Log.d(FOREGROUND_SERVICE, "stopped foreground service - onAlarm");
                            }
                            else
                                Log.e("NULL-ERROR","token is null in TimedService");
                            stopForeground(true);
                        }
                    }, new Handler());
                }
                startForeground(notificationID, builder.build());

            }
        }
        else
        {
            int notificationID = intent.getIntExtra("notificationID",-1);
            if(notificationID == this.notificationID) {
                Log.d(FOREGROUND_SERVICE, "service is stopped");
                manager.cancel(new AlarmManager.OnAlarmListener() {
                    @Override
                    public void onAlarm() {
                        Toast.makeText(TimedMessageService.this, "message will not be sent", Toast.LENGTH_SHORT).show();
                        Log.d(FOREGROUND_SERVICE,"cancelled alarm");
                    }
                });
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
