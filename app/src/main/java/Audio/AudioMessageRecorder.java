package Audio;

import android.util.Log;

import java.io.IOException;

public class AudioMessageRecorder extends Thread{

    private long currentRecordingTime;
    private final String audioMessageRecorderTag = "messageRecorder";
    private AudioRecorder2 recorder;
    private Boolean recording;
    private String fileName;

    private Audio listener;
    public AudioMessageRecorder()
    {
        currentRecordingTime  = 0;
        recorder = new AudioRecorder2();
        changeRecordingIndicator(false);
    }

    public void startRecording(String fileName)
    {
        if (fileName == null)
        {
            Log.e(audioMessageRecorderTag, "fileName cant be null");
            if (listener != null)
                listener.onFailed("fileName cant be null");
        }
        else {
            try {
                Log.d(audioMessageRecorderTag, "started recording");
                this.fileName = fileName;
                recorder.startRecording(fileName);
                if (listener != null)
                    listener.onStart();
                startCountingThread();
            } catch (IOException e) {
                if (listener != null)
                    listener.onFailed(e.getMessage());
                Log.e(audioMessageRecorderTag, "start recording failed");
            }
        }
    }

    public void onStopRecording()
    {
        try {
            Log.d(audioMessageRecorderTag, "stopped recording");
            recorder.stopRecording();
            changeRecordingIndicator(false);
            if (listener != null) {
                listener.onStopped(fileName);
                listener.onFinished(currentRecordingTime);
                Log.d(audioMessageRecorderTag, "onFinished was called - currentRecordingTime: " + currentRecordingTime);

            }
            currentRecordingTime = 0;
        }catch (RuntimeException e )
        {
            Log.e(audioMessageRecorderTag, "stopped failed - possible reason button was pressed and released fast");
            e.printStackTrace();
            if (listener!=null)
                listener.onFailed("on stopped failed");
            changeRecordingIndicator(false);
        }
    }

    public void onResumeRecording()
    {
        Log.d(audioMessageRecorderTag, "resumed recording");
        recorder.resumeRecording();
        startCountingThread();
        if(listener != null)
            listener.onResume(currentRecordingTime);
    }

    public void onPauseRecording()
    {
        Log.d(audioMessageRecorderTag, "paused recording");
        recorder.pauseRecording();
        changeRecordingIndicator(false);
        if(listener != null)
            listener.onPause();
    }

    private void startCountingThread()
    {
        Log.d(audioMessageRecorderTag, "starting time thread");
        changeRecordingIndicator(true);
    }

    private void changeRecordingIndicator(boolean newState)
    {
       recording = newState;
       if(recording) {
           Thread recordingTimeThread = new Thread(this);
           recordingTimeThread.setName("recording time");
           recordingTimeThread.start();
       }

    }

    @Override
    public synchronized void run() {
        while (isRecording())
        {
            try {
                wait(1000);
                currentRecordingTime = currentRecordingTime + 1;
                if(listener != null)
                    listener.onProgressChange(currentRecordingTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void setListener(Audio listener)
    {
        Log.d(audioMessageRecorderTag, "listener was set");
        this.listener = listener;
    }

    public boolean isRecording() {
        return recording;
    }
}
