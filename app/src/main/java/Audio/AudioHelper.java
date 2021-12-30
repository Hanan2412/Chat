package Audio;

public interface AudioHelper {
    void onProgressChange(String formattedProgress,int progress);
    void onPlayingStatusChange(boolean isPlaying);
}
