package NormalObjects;

import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.util.Log;

import androidx.annotation.NonNull;

public class Network2 extends ConnectivityManager.NetworkCallback {


    private NetworkChange callback;

    public void setListener(NetworkChange listener){
        callback = listener;
    }

    String NETWORK_CLASS = "network111";
    private static Network2 network2 = null;
    public static Network2 getInstance()
    {
        if (network2 == null)
            network2 = new Network2();
        return network2;
    }
    private Network2() {
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
