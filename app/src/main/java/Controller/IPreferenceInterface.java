package Controller;

public interface IPreferenceInterface {
    void onPreferenceChange(String path,boolean data);
    void onPreferenceDelete(String path);
}
