package com.example.weatherapp.utils.networkutil;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import androidx.annotation.NonNull;

public class NetworkConnectionObserver {

    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private NetworkStatusListener listener;

    public NetworkConnectionObserver(Context context, NetworkStatusListener listener) {

        this.listener = listener;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        networkCallback = new ConnectivityManager.NetworkCallback() {

            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                listener.onNetworkAvailable();
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                listener.onNetworkLost();
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                listener.onNetworkLost();
            }
        };
    }


    public void registerCallback() {

       connectivityManager.registerDefaultNetworkCallback(networkCallback);

//        NetworkRequest networkRequest = new NetworkRequest.Builder()
//                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//                .build();
//
//        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);


    }

    public  void unregisterCallback() {
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }

    public void checkNetworkConnection() {
        Network network = connectivityManager.getActiveNetwork();
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);

        boolean isNetworkAvailable = networkCapabilities != null &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

        if (!isNetworkAvailable) {
            listener.onNetworkLost();
        }
    }


}
