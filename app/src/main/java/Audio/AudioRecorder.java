package Audio;

import android.content.Context;
import android.media.MediaRecorder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AudioRecorder extends MediaRecorder implements Runnable {

    private AudioHelper callback;
    private boolean recording;
    private int seconds = 0,minutes = 0;
    private final String fileName;
    public AudioRecorder(Context context)
    {
        setAudioSource(MediaRecorder.AudioSource.MIC);
        setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        fileName = context.getExternalCacheDir().getAbsolutePath() + "/audioRecording_" + System.currentTimeMillis() + ".3pg";
        setOutputFile(fileName);
        setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            prepare();
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
        }
    }
    public String getRecordingPath()
    {
        return fileName;
    }

    public void recordStopAudio()
    {
        if (isRecording())
        {
            recording = false;
            pause();
        }
        else
        {
            recording = true;
            start();
            Thread timeCount = new Thread(this);
            timeCount.setName("recording time");
            timeCount.start();
        }
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
        if (callback!=null)
        {
            callback.onPlayingStatusChange(isRecording());
        }
    }

    @Override
    public void pause() throws IllegalStateException {
        super.pause();
        if (callback!=null)
        {
            callback.onPlayingStatusChange(isRecording());
        }
    }

    public boolean isRecording()
    {
        return recording;
    }

    public void setListener(AudioHelper listener)
    {
        callback = listener;
    }

    private String formatProgress()
    {
        if (seconds == 60) {
            seconds = 0;
            minutes++;
        }
        if (minutes == 60) {
            minutes = 0;
        }
        String minuteStr;
        if (minutes < 10)
            minuteStr = "0" + minutes;
        else minuteStr = minutes + "";
        String secondsStr;
        if (seconds < 10)
            secondsStr = "0" + seconds;
        else
            secondsStr = seconds + "";
        return minuteStr + ":" + secondsStr;
    }

    @Override
    public void run() {
        while (isRecording())
        {
            callback.onProgressChange(formatProgress(),minutes*60+seconds);
            try {
                TimeUnit.SECONDS.sleep(1);
                seconds++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
