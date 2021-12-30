package Audio;

import android.content.Context;
import android.widget.SeekBar;

import androidx.annotation.NonNull;

import java.util.HashMap;

public class AudioManager {

    private HashMap<String,AudioPlayer>players;
    private HashMap<String,AudioRecorder>recorders;
    private static AudioManager manager;

    public static AudioManager getInstance() {
        if (manager == null)
            manager = new AudioManager();
        return manager;
    }

    private AudioManager()
    {
        if (players == null)
            players = new HashMap<>();
        if (recorders == null)
            recorders = new HashMap<>();
    }

    public AudioPlayer getAudioPlayer(@NonNull String dataSource, SeekBar seekBar)
    {
        if (players.containsKey(dataSource))
            return players.get(dataSource);
        AudioPlayer audioPlayer;
        if (seekBar!=null)
        {
            audioPlayer = new AudioPlayer(dataSource, seekBar);
        }
        else
        {
            audioPlayer = new AudioPlayer(dataSource);
        }
        players.put(dataSource,audioPlayer);
        return audioPlayer;
    }

    public void releasePlayer(String dataSource)
    {
        if (players.containsKey(dataSource))
        {
            AudioPlayer player =  players.get(dataSource);
            if (player!=null)
            {
                player.stop();
                player.release();
            }
            players.remove(dataSource);
        }
    }

    public AudioRecorder getAudioRecorder(@NonNull String conversationID, Context context)
    {
        if (recorders.containsKey(conversationID))
            return recorders.get(conversationID);
        AudioRecorder recorder = new AudioRecorder(context);
        recorders.put(conversationID,recorder);
        return recorder;
    }

    public void releaseRecorder(String conversationID)
    {
        if (recorders.containsKey(conversationID))
        {
            AudioRecorder recorder = recorders.get(conversationID);
            if (recorder!=null)
            {
                recorder.stop();
                recorder.release();
            }
            recorders.remove(conversationID);
        }
    }
}
