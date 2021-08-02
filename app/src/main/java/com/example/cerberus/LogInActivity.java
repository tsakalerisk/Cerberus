package com.example.cerberus;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.cerberus.LoginManagers.FacebookLogInManager;
import com.example.cerberus.LoginManagers.TwitterLogInManager;
import com.example.cerberus.R;
import com.facebook.login.widget.LoginButton;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

public class LogInActivity extends AppCompatActivity {

    public static final boolean AUTO_LOG_OUT = false;

    public static final String TAG_FACEBOOK = "Facebook";
    public static final String TAG_TWITTER = "Twitter";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        twitterLogInManager = new TwitterLogInManager(this);
        setContentView(R.layout.activity_log_in);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        findAllViews();
        facebookLogInManager = new FacebookLogInManager(this);
        twitterLogInManager.init();
        buttonNext.setOnClickListener(v -> startActivity(new Intent(this, FeedActivity.class)));
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
}