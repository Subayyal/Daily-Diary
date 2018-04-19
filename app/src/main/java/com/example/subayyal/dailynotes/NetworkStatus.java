package com.example.subayyal.dailynotes;

import android.net.NetworkInfo;

/**
 * Created by subayyal on 4/9/2018.
 */

public interface NetworkStatus {
    void onDisconnect();
    void onConnect();
}
