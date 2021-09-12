package com.example.cerberus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;

import com.example.cerberus.Modules.NetworkCallback;

/*
Shows up when the device is not connected to the internet.
Keeps each activity's intents as they were passed.
 */
public class NoInternetActivity extends AppCompatActivity {
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private Class activityFrom = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);
        if (getIntent() != null) {
            activityFrom = (Class) getIntent().getSerializableExtra(NetworkCallback.ACTIVITY_FROM);
        }
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        networkCallback = new NetworkCallback(NoInternetActivity.this, activityFrom);
        connectivityManager.registerDefaultNetworkCallback(networkCallback);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}