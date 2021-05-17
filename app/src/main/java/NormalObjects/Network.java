package NormalObjects;

import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.util.Log;

import androidx.annotation.NonNull;

public class Network extends ConnectivityManager.NetworkCallback {

    private NetworkChange callback;

    public void setListener(NetworkChange listener){
        callback = listener;
    }

    String NETWORK_CLASS = "network111";
    public Network() {
        super();
    }

    @Override
    public void onAvailable(@NonNull android.net.Network network) {
        super.onAvailable(network);
        callback.onNetwork();
        Log.i(NETWORK_CLASS,"onAvailable");
    }

    @Override
    public void onLosing(@NonNull android.net.Network network, int maxMsToLive) {
        super.onLosing(network, maxMsToLive);
        Log.i(NETWORK_CLASS,"onLosing");
    }

    @Override
    public void onLost(@NonNull android.net.Network network) {
        super.onLost(network);
        callback.onNetworkLost();
        Log.i(NETWORK_CLASS,"onLost");
    }

    @Override
    public void onUnavailable() {
        super.onUnavailable();
        callback.onNoNetwork();
        Log.i(NETWORK_CLASS,"onUnavailable");
    }

    @Override
    public void onCapabilitiesChanged(@NonNull android.net.Network network, @NonNull NetworkCapabilities networkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities);
        callback.onChangedNetworkType();
        Log.i(NETWORK_CLASS,"onCapabilitiesChange");
    }
}
