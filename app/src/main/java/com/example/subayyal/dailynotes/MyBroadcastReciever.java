package com.example.subayyal.dailynotes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by subayyal on 4/9/2018.
 */

public class MyBroadcastReciever extends BroadcastReceiver {
    NetworkStatus networkStatus;

    public MyBroadcastReciever(NetworkStatus networkStatus) {
        this.networkStatus = networkStatus;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Bundle extras = intent.getExtras();

        NetworkInfo info = (NetworkInfo) extras
                .getParcelable("networkInfo");

        NetworkInfo.State state = info.getState();
        Log.d("Test", info.toString() + " "
                + state.toString());

        if (state == NetworkInfo.State.CONNECTED) {
            networkStatus.onConnect();
        } else {
            networkStatus.onDisconnect();
        }
    }
}
