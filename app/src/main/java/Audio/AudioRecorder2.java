package Audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;

public class AudioRecorder2{

    private MediaRecorder recorder;


    public void startRecording(String fileName) throws IOException {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setAudioChannels(1);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.prepare();
        recorder.start();
    }

    public void stopRecording()
    {
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    public void pauseRecording()
    {
        recorder.pause();
    }

    public void resumeRecording()
    {
        recorder.resume();

    }
}
