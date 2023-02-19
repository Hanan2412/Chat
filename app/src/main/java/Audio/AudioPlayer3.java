package Audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

@SuppressWarnings("Convert2Lambda")
public class AudioPlayer3 extends Thread {

    private Audio listener;
    private int playTime;
    private MediaPlayer player;
    private boolean isPlaying;
    private String dataSource;
    private final String NULL_TAG = "NULL";
    private final String AUDIO_PLAYER3 = "AudioPlayer3";
    private boolean paused;

    public AudioPlayer3() {
        changePlayingStatus(false);
        changePausedIndicator(false);
    }

    public void playPause() {
        if (!isPlaying())
            startPlaying();
        else pausePlaying();
    }

    public void onResume() {
        changePausedIndicator(false);
        player.start();
        if (listener != null)
        {
            listener.onResume(player.getCurrentPosition());
            Log.d(AUDIO_PLAYER3, "onResume was called");
        }
        startThread();
    }

    private void startThread()
    {
        Log.d(AUDIO_PLAYER3, "starting timeElapsed thread");
        changePlayingStatus(true);
        Thread timeElapsedThread = new Thread(this);
        timeElapsedThread.setName("timeElapsedThread");
        timeElapsedThread.start();
    }

    public void changePausedIndicator(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public void pause() {
        pausePlaying();
    }

    public void play() {
        startPlaying();
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    private void startPlaying() {
        if (dataSource == null) {
            if (listener != null)
            {
                listener.onFailed("dataSource is null, cannot play audio");
                Log.d(AUDIO_PLAYER3, "onFailed was called");
            }
        } else if (isPaused())
            onResume();
        else {
            player.start();
            if (listener != null)
            {
                listener.onStart();
                Log.d(AUDIO_PLAYER3, "onStart was called");
            }
            startThread();
        }
    }

    private void pausePlaying() {
        changePlayingStatus(false);
        changePausedIndicator(true);
        player.pause();
        if (listener != null)
        {
            listener.onPause();
            Log.d(AUDIO_PLAYER3, "onPaused was called");
        }
        updatePlayTime();
    }

    public void stopPlaying() {
        changePlayingStatus(false);
        changePausedIndicator(false);
        player.stop();
        if (listener != null)
        {
            listener.onStopped(dataSource);
            Log.d(AUDIO_PLAYER3, "onStopped was called");
        }
    }

    private void changePlayingStatus(boolean newStatus) {
        isPlaying = newStatus;
        if (isPlaying)
            changePausedIndicator(false);
    }

    public void setListener(Audio listener) {
        this.listener = listener;
        Log.d(AUDIO_PLAYER3, "listener was set");
    }

    public int getDuration() {
        return player.getDuration();
    }

    public int getPlayTime() {
        return playTime;
    }

    private void updatePlayTime() {
        playTime = player.getCurrentPosition();
        if (listener != null)
        {
            listener.onProgressChange(getPlayTime());
            Log.d(AUDIO_PLAYER3, "onProgressChange was called");
        }
    }

    @Override
    public synchronized void run() {
        while (isPlaying()) {
            try {
                wait(1000);
                updatePlayTime();
                if (getPlayTime() == 0)
                {
                    if (listener != null) {
                        listener.onFinished(getDuration());
                    }
                    Log.d(AUDIO_PLAYER3, "onFinished from thread was called");
                    Log.d(AUDIO_PLAYER3, "play time and duration: (" + getPlayTime() + "," + getDuration() + ")");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (listener != null)
                {
                    listener.onFailed("time thread has failed");
                    Log.d(AUDIO_PLAYER3, "onFailed was called");
                }
            }

        }
    }

    public void seekTo(int seekTime) {
        if (isPlaying())
            pausePlaying();
        player.seekTo(seekTime);
    }

    public void releasePlayer() {
        if (player!=null) {
            if (isPlaying())
                stopPlaying();
            player.release();
            if (listener != null) {
                listener.onUnLoad();
                Log.d(AUDIO_PLAYER3, "onUnLoad was called");
            }
            setDataSource(null);
        }
        else Log.d(AUDIO_PLAYER3, "player was null in releasePlayer");
    }

    public void setDataSource(String dataSource) {
        player = new MediaPlayer();
        if (dataSource != null) {
            try {
                this.dataSource = dataSource;
                player.setDataSource(dataSource);
                player.setAudioAttributes(new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build());
                player.prepare();
                player.setVolume(1.0f, 1.0f);
                if (listener != null)
                {
                    listener.onLoad(getDuration());
                    Log.d(AUDIO_PLAYER3, "onLoad was called: " + getDuration());
                }
                changePlayingStatus(false);
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        changePlayingStatus(false);
                        player.seekTo(0);
                        if (listener != null) {
                            {
                                listener.onFinished(getDuration());
                                Log.d(AUDIO_PLAYER3, "onFinished was called");
                            }
                        }
                    }
                });
                player.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                    @Override
                    public void onSeekComplete(MediaPlayer mediaPlayer) {
                        updatePlayTime();
                    }
                });
                updatePlayTime();
            } catch (IOException e) {
                e.printStackTrace();
                if (listener != null)
                {
                    listener.onFailed("failed preparing audio source");
                    Log.d(AUDIO_PLAYER3, "onFailed was called");
                }
            }
        } else Log.i(NULL_TAG, "dataset is null");
    }


    public AudioPlayer3(Context context, int id) {
        MediaPlayer player = MediaPlayer.create(context, id);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        });
        player.start();
    }
}
