package com.example.cerberus;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.facebook.login.widget.LoginButton;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;

import retrofit2.Call;

public class LogInActivity extends AppCompatActivity {

    public static final boolean AUTO_LOG_OUT = false;

    public static final String TAG_FACEBOOK = "Facebook";
    public static final String TAG_TWITTER = "Twitter";

    public LoginButton fbLoginButton = null;
    public ImageView fbImage = null;
    public TextView fbName = null;
    public ImageView fbPageImage = null;
    public TextView fbPageName = null;
    private FacebookLogInManager facebookLogInManager = null;

    public TwitterLoginButton twLogInButton = null;
    public Button twLogOutButton = null;
    public ImageView twImage = null;
    public TextView twName = null;
    public TextView twScreenName = null;
    private TwitterLogInManager twitterLogInManager = null;

    private Button instaLoginButton = null;
    private ImageView instaImage = null;
    private TextView instaName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        twitterLogInManager = new TwitterLogInManager(this);
        setContentView(R.layout.activity_log_in);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        findAllViews();
        facebookLogInManager = new FacebookLogInManager(this);
        twitterLogInManager.init();
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

        instaLoginButton = findViewById(R.id.instaButton);
        instaImage = findViewById(R.id.instaImage);
        instaName = findViewById(R.id.instaName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        facebookLogInManager.onActivityResult(requestCode, resultCode, data);
        twLogInButton.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        facebookLogInManager.onDestroy();
    }
}