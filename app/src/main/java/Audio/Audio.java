package Audio;

public interface Audio {

    void onLoad(long duration);
    void onUnLoad();
    void onStart();
    void onPause();
    void onResume(long progress);
    void onStopped(String fileName);
    void onFinished(long fileDuration);
    void onFailed(String msg);
    void onProgressChange(long progress);
}
