package com.example.cerberus;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.cerberus.Modules.LoginManagers.FacebookLogInManager;
import com.example.cerberus.Modules.LoginManagers.TwitterLogInManager;
import com.example.cerberus.Modules.NetworkCallback;
import com.facebook.login.widget.LoginButton;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

/*
Manages user log in
 */
public class LogInActivity extends AppCompatActivity {

    public static final boolean AUTO_LOG_OUT = false;
    public static final boolean AUTO_CONTINUE = false;

    public static final String TAG_FACEBOOK = "Facebook";
    public static final String TAG_TWITTER = "Twitter";
    public static final String FB_USER_TOKEN_LITERAL = "FB_USER_TOKEN";
    public static final String FB_PAGE_TOKEN_LITERAL = "FB_PAGE_TOKEN";
    public static final String INSTA_USER_ID = "INSTA_USER_ID";

    public LoginButton fbLoginButton = null;
    public ImageView fbImage = null;
    public TextView fbName = null;
    public ImageView fbPageImage = null;
    public TextView fbPageName = null;

    public ImageView instaImage = null;
    public TextView instaName = null;
    public TextView instaUsername = null;

    public TwitterLoginButton twLogInButton = null;
    public Button twLogOutButton = null;
    public ImageView twImage = null;
    public TextView twName = null;
    public TextView twScreenName = null;

    public Button buttonNext = null;

    private FacebookLogInManager facebookLogInManager = null;
    private TwitterLogInManager twitterLogInManager = null;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        twitterLogInManager = new TwitterLogInManager(this);
        setContentView(R.layout.activity_log_in);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        findAllViews();

        facebookLogInManager = new FacebookLogInManager(this);
        twitterLogInManager.init();

        if (AUTO_CONTINUE && facebookLogInManager.isLoggedIn() && twitterLogInManager.isLoggedIn()) {
            continueToFeed();
        }

        buttonNext.setOnClickListener(v -> {
            if (facebookLogInManager.isLoggedIn() && twitterLogInManager.isLoggedIn()) {
                continueToFeed();
            }
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.login_to_continue)
                        .setPositiveButton(R.string.ok, null)
                        .create().show();
            }
        });

        checkForNetwork();
    }

    private void checkForNetwork() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getActiveNetworkInfo() == null) {
            Intent intent = new Intent(this, NoInternetActivity.class);
            intent.putExtra(NetworkCallback.ACTIVITY_FROM, getClass());
            startActivity(intent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        networkCallback = new NetworkCallback(this);
        connectivityManager.registerDefaultNetworkCallback(networkCallback);
    }

    private void continueToFeed() {
        Intent intent = new Intent(this, FeedActivity.class);
        intent.putExtra(FB_USER_TOKEN_LITERAL, facebookLogInManager.getUserAccessToken());
        intent.putExtra(FB_PAGE_TOKEN_LITERAL, facebookLogInManager.getPageAccessToken());
        intent.putExtra(INSTA_USER_ID, facebookLogInManager.getInstaUserId());
        startActivity(intent);
    }

    protected void findAllViews() {
        fbLoginButton = findViewById(R.id.fbButton);
        fbImage = findViewById(R.id.fbImage);
        fbName = findViewById(R.id.fbName);
        fbPageImage = findViewById(R.id.fbPageImage);
        fbPageName = findViewById(R.id.fbPageName);

        twLogInButton = findViewById(R.id.twButton);
        twLogOutButton = findViewById(R.id.twLogOutButton);
        twImage = findViewById(R.id.twImage);
        twName = findViewById(R.id.twName);
        twScreenName = findViewById(R.id.twScreenName);

        instaImage = findViewById(R.id.instaImage);
        instaName = findViewById(R.id.instaName);
        instaUsername = findViewById(R.id.instaUsername);

        buttonNext = findViewById(R.id.buttonNext);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        facebookLogInManager.onActivityResult(requestCode, resultCode, data);
        twitterLogInManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        facebookLogInManager.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}