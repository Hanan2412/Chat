package Audio;

import java.util.HashMap;

public class AudioManager2 {

    private HashMap<String,Integer>progress;
    private AudioPlayer2 audioPlayer;
    private static AudioManager2 manager;

    public static AudioManager2 getInstance(){
        if (manager == null)
            manager = new AudioManager2();
        return manager;
    }

    private AudioManager2()
    {
        progress = new HashMap<>();
    }

    public AudioPlayer2 getAudioPlayer(String dataSource)
    {
        audioPlayer = new AudioPlayer2(dataSource);
        return audioPlayer;
    }

    public void releasePlayer(String dataSource)
    {
        if(audioPlayer != null)
        {
            audioPlayer.stop();
            audioPlayer.release();
        }
    }

    public int getProgress(String dataSource)
    {
        return progress.getOrDefault(dataSource, 0);
    }

    public void updateProgress(String dataSource, int currentProgress)
    {
        progress.put(dataSource,currentProgress);
    }
}
