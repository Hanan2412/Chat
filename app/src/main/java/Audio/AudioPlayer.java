package Audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.widget.SeekBar;


import java.io.IOException;

public class AudioPlayer extends MediaPlayer implements Runnable,MediaPlayer.OnCompletionListener,MediaPlayer.OnSeekCompleteListener{

    private SeekBar seekBar = null;
    private AudioHelper callback;
    public AudioPlayer (String dataSource)
    {
        try {
            setDataSource(dataSource);
            setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .build());
            prepare();
            setOnCompletionListener(this);
            setOnSeekCompleteListener(this);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public AudioPlayer (String dataSource, SeekBar seekBar)
    {
        try {
            setDataSource(dataSource);
            setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .build());
            prepare();
            setOnCompletionListener(this);
            setOnSeekCompleteListener(this);
            this.seekBar = seekBar;
            this.seekBar.setMin(0);
            this.seekBar.setProgress(0);
            this.seekBar.setMax(getDuration());
            this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    String progressFormatted = formatProgress(progress);
                    callback.onProgressChange(progressFormatted, progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                   /* pause();
                    seekTo(seekBar.getProgress());
                    String progress = formatProgress(seekBar.getProgress()*1000);
                    callback.onProgressChange(progress, seekBar.getProgress());*/
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    /*String progress = formatProgress(seekBar.getProgress());
                    callback.onProgressChange(progress, seekBar.getProgress());
                    playPauseAudio();*/
                }
            });
        }catch (IOException | IllegalStateException e)
        {
            e.printStackTrace();
        }
    }

    //this constructor is to play sounds without any control from the user - like background music or feedback sound
    @SuppressWarnings("Convert2Lambda")
    public AudioPlayer(Context context, int id)
    {
        MediaPlayer player = create(context,id);
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
        {
            pause();
        }
        else
        {
            start();
            if (seekBar!=null)
                seekThread();
        }
    }

    @Override
    public void stop() throws IllegalStateException {
        super.stop();
        if (seekBar!=null)
            seekBar.setProgress(0);
        if (callback!=null)
            callback.onPlayingStatusChange(false);
    }

    public void setListener(AudioHelper listener)
    {
        callback = listener;
    }

    private void seekThread()
    {
        Thread seek = new Thread(this);
        seek.setName("voice seekbar thread");
        seek.start();
    }

    @Override
    public void run() {
        while (isPlaying()) {
            seekBar.setProgress(getCurrentPosition());
        }
    }

    private String formatProgress(int progress)
    {
        int currentPosition = progress/1000;
        if (currentPosition < 10)
            return "00:0" + currentPosition;
        else
            return "00:" + currentPosition;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (seekBar!=null)
            seekBar.setProgress(0);
        seekTo(0);
        if (callback!=null)
        {
            callback.onPlayingStatusChange(isPlaying());
            callback.onProgressChange("00:00",0);
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

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


}
