package com.example.cerberus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.login.widget.LoginButton;

import twitter4j.Twitter;
import twitter4j.auth.RequestToken;

public class LogInActivity extends AppCompatActivity {

    public static final boolean AUTO_LOG_OUT = true;

    public static final String TAG_FACEBOOK = "Facebook";
    public static final String TAG_TWITTER = "Twitter";

    public LoginButton fbLoginButton = null;
    public ImageView fbImage = null;
    public TextView fbName = null;
    public ImageView fbPageImage = null;
    public TextView fbPageName = null;
    private FacebookLogInManager facebookLogInManager = null;

    private Button twLoginButton = null;
    private ImageView twImage = null;
    private TextView twName = null;
    private Twitter twitter = null;

    private Button instaLoginButton = null;
    private ImageView instaImage = null;
    private TextView instaName = null;

    private RequestToken requestToken = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        findAllViews();
        facebookLogInManager = new FacebookLogInManager(this);
    }

    protected void findAllViews() {
        fbLoginButton = findViewById(R.id.fbButton);
        fbImage = findViewById(R.id.fbImage);
        fbName = findViewById(R.id.fbName);
        fbPageImage = findViewById(R.id.fbPageImage);
        fbPageName = findViewById(R.id.fbPageName);

        twLoginButton = findViewById(R.id.twButton);
        twImage = findViewById(R.id.twImage);
        twName = findViewById(R.id.twName);

        instaLoginButton = findViewById(R.id.instaButton);
        instaImage = findViewById(R.id.instaImage);
        instaName = findViewById(R.id.instaName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        facebookLogInManager.callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        facebookLogInManager.accessTokenTracker.stopTracking();
    }
}