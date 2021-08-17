package com.example.cerberus;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class NetworkCallback extends ConnectivityManager.NetworkCallback {
    public static final String ACTIVITY_FROM = "ACTIVITY_FROM";
    private final Activity activityFrom;
    private final Class activityTo;

    public NetworkCallback(Activity activityFrom) {
        this.activityFrom = activityFrom;
        this.activityTo = null;
    }

    public NetworkCallback(Activity activityFrom, Class activityTo) {
        this.activityFrom = activityFrom;
        this.activityTo = activityTo;
    }

    @Override
    public void onAvailable(@NonNull Network network) {
        if (activityTo != null) {
            Intent intent = new Intent(activityFrom, activityTo);
            activityFrom.startActivity(intent);
        }
    }

    @Override
    public void onLost(@NonNull Network network) {
        Intent intent = new Intent(activityFrom, NoInternetActivity.class);
        intent.putExtra(ACTIVITY_FROM, activityFrom.getClass());
        activityFrom.startActivity(intent);
    }


}
