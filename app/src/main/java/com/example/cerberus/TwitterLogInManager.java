package com.example.cerberus;

import static com.example.cerberus.LogInActivity.AUTO_LOG_OUT;
import static com.example.cerberus.LogInActivity.TAG_TWITTER;

import android.app.AlertDialog;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.webkit.CookieManager;

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
    private LogInActivity logInActivity = null;

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
            }
            else {
                setLogOutVisibility(true);
                requestUserInfo();
            }
        }
        else
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
            builder.setMessage("Έχετε συνδεθεί ως: " + logInActivity.twName.getText())
                    .setCancelable(true)
                    .setPositiveButton("ΑΠΟΣΥΝΔΕΣΗ", (dialog, which) -> logOutAndClearViews())
                    .setNegativeButton("ΑΚΥΡΩΣΗ", null);
            builder.create().show();
        });
    }

    private void logOutAndClearViews() {
        logOut();
        logInActivity.twName.setText("Δεν είστε συνδεδεμένοι");
        logInActivity.twScreenName.setText("");
        logInActivity.twImage.setImageResource(R.drawable.user_icon);
        setLogOutVisibility(false);
    }

    private boolean isLoggedIn() {
        return TwitterCore.getInstance().getSessionManager().getActiveSession() != null;
    }

    private void logOut() {
        CookieManager.getInstance().removeSessionCookies(value -> {});
        TwitterCore.getInstance().getSessionManager().clearActiveSession();
        Log.d(TAG_TWITTER, String.valueOf(isLoggedIn()));
    }

    private void requestUserInfo() {
        Call<User> user = TwitterCore.getInstance().getApiClient().getAccountService()
                .verifyCredentials(false, false, false);
        user.enqueue(new Callback<User>() {
            @Override
            public void success(Result<User> result) {
                logInActivity.twName.setText(result.data.name);
                logInActivity.twScreenName.setText("@" + result.data.screenName);
                //Replaces normal size with original
                String imageUrl = result.data.profileImageUrl.replace("_normal", "");
                Picasso.get().load(imageUrl).into(logInActivity.twImage);
            }

            @Override
            public void failure(TwitterException exception) { }
        });
    }

    private void setLogOutVisibility(boolean b) {
        if (b)
            logInActivity.twLogOutButton.setVisibility(View.VISIBLE);
        else
            logInActivity.twLogOutButton.setVisibility(View.GONE);
    }
}
