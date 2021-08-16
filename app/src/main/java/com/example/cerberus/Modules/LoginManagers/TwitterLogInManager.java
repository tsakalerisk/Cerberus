package com.example.cerberus.Modules.LoginManagers;

import static com.example.cerberus.LogInActivity.AUTO_LOG_OUT;
import static com.example.cerberus.LogInActivity.TAG_TWITTER;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.webkit.CookieManager;

import com.example.cerberus.BuildConfig;
import com.example.cerberus.Modules.CustomTwitterApiClient;
import com.example.cerberus.LogInActivity;
import com.example.cerberus.R;
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
import com.twitter.sdk.android.core.models.User;

import retrofit2.Call;

public class TwitterLogInManager {
    private final LogInActivity logInActivity;

    public TwitterLogInManager(LogInActivity logInActivity) {
        this.logInActivity = logInActivity;
        TwitterConfig config = new TwitterConfig.Builder(logInActivity)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(BuildConfig.TwitterApiKey, BuildConfig.TwitterApiKeySecret))
                .debug(true)
                .build();
        Twitter.initialize(config);
    }

    public void init() {
        logInActivity.twLogInButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        checkLogIn();
        setUpButtons();
    }

    private void checkLogIn() {
        if (isLoggedIn()) {
            if (AUTO_LOG_OUT) {
                logOut();
                setLogOutVisibility(false);
            } else {
                setLogOutVisibility(true);
                requestUserInfo();
            }
        } else
            setLogOutVisibility(false);
    }

    private void setUpButtons() {
        logInActivity.twLogInButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.d(TAG_TWITTER, "Success!");
                requestUserInfo();
                setLogOutVisibility(true);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.e(TAG_TWITTER, "Failed!");
                exception.printStackTrace();
            }
        });
        logInActivity.twLogOutButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(logInActivity);
            builder.setMessage(logInActivity.getResources().getString(R.string.logged_in_as) + logInActivity.twName.getText())
                    .setCancelable(true)
                    .setPositiveButton(logInActivity.getResources().getString(R.string.log_out), (dialog, which) -> logOutAndClearViews())
                    .setNegativeButton(logInActivity.getResources().getString(R.string.cancel), null)
                    .create().show();
        });
    }

    private void logOutAndClearViews() {
        logOut();
        logInActivity.twName.setText(logInActivity.getResources().getString(R.string.not_logged_in));
        logInActivity.twScreenName.setText("");
        logInActivity.twImage.setImageResource(R.drawable.user_icon);
        setLogOutVisibility(false);
    }

    public boolean isLoggedIn() {
        return getActiveSession() != null;
    }

    private TwitterSession getActiveSession() {
        return TwitterCore.getInstance().getSessionManager().getActiveSession();
    }

    private void logOut() {
        CookieManager.getInstance().removeSessionCookies(value -> {
        });
        TwitterCore.getInstance().getSessionManager().clearActiveSession();
        Log.d(TAG_TWITTER, String.valueOf(isLoggedIn()));
    }

    private void requestUserInfo() {
        //Add custom API Client before making any calls
        TwitterCore.getInstance().addApiClient(getActiveSession(), new CustomTwitterApiClient(getActiveSession()));
        Call<User> user = TwitterCore.getInstance().getApiClient().getAccountService()
                .verifyCredentials(false, false, false);
        user.enqueue(new Callback<User>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void success(Result<User> result) {
                logInActivity.twName.setText(result.data.name);
                logInActivity.twScreenName.setText("@" + result.data.screenName);
                //Replaces normal size with original
                String imageUrl = result.data.profileImageUrl.replace("_normal", "");
                Picasso.with(logInActivity).load(imageUrl).into(logInActivity.twImage);
            }

            @Override
            public void failure(TwitterException exception) {
            }
        });
    }

    private void setLogOutVisibility(boolean b) {
        if (b)
            logInActivity.twLogOutButton.setVisibility(View.VISIBLE);
        else
            logInActivity.twLogOutButton.setVisibility(View.GONE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        logInActivity.twLogInButton.onActivityResult(requestCode, resultCode, data);
    }
}
