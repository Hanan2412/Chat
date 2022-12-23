package Audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;

import java.io.IOException;

import Time.TimeFormat;

public class AudioPlayer2 extends MediaPlayer implements Runnable,MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener {

    private AudioHelper callback;
    private TimeFormat timeFormat;
    private int playingTime = 0;
    private String currentDataSource;

    public AudioPlayer2(String dataSource)
    {
        this.currentDataSource = dataSource;
        timeFormat = new TimeFormat();
        try {
            setDataSource(dataSource);
            setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .build());
            prepare();
            setVolume(1.0f,1.0f);
            setOnCompletionListener(this);
            setOnSeekCompleteListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCurrentDataSource()
    {
        return currentDataSource;
    }
    //this constructor is to play sounds without any control from the user - like background music or feedback sound
    @SuppressWarnings("Convert2Lambda")
    public AudioPlayer2(Context context, int id) {
        MediaPlayer player = create(context, id);
        player.start();
        player.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
                mp.release();
            }
        });
    }

    public void playPauseAudio()
    {
        if (isPlaying())
            pause();
        else
        {
            start();
            Thread playingTimeThread = new Thread(this);
            playingTimeThread.setName("playing time");
            playingTimeThread.start();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        seekTo(0);
        if (callback!=null)
        {
            callback.onPlayingStatusChange(isPlaying());
            callback.onProgressChange("00:00",0);
        }
    }

    @Override
    public void seekTo(int msec) throws IllegalStateException {
        super.seekTo(msec);
        playingTime = msec/1000;
        if (callback!=null)
            callback.onProgressChange(timeFormat.getFormattedTime(msec*1000L), playingTime);
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    @Override
    public synchronized void run() {
        while (isPlaying())
        {
            callback.onProgressChange(timeFormat.getFormattedTime(playingTime* 1000L), playingTime);
            try{
                wait(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                playingTime++;
            }
        }
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
        if (callback!=null)
            callback.onPlayingStatusChange(isPlaying());
    }

    @Override
    public void pause() throws IllegalStateException {
        super.pause();
        if (callback!=null)
            callback.onPlayingStatusChange(isPlaying());
    }

    public void setAudioListener(AudioHelper listener) {
        if (callback==null)
            callback = listener;
    }
}
