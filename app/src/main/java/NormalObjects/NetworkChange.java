package NormalObjects;

public interface NetworkChange {
    void onNetwork();
    void onNoNetwork();
    void onNetworkLost();
    void onChangedNetworkType();
}
