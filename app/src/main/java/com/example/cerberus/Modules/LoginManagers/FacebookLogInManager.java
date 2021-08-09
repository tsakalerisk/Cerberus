package com.example.cerberus.Modules.LoginManagers;

import static com.example.cerberus.LogInActivity.AUTO_LOG_OUT;
import static com.example.cerberus.LogInActivity.TAG_FACEBOOK;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.cerberus.LogInActivity;
import com.example.cerberus.R;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class FacebookLogInManager {
    private final LogInActivity logInActivity;
    private CallbackManager callbackManager = null;
    private AccessToken userAccessToken = null;
    private AccessToken pageAccessToken = null;
    private AccessTokenTracker accessTokenTracker = null;

    private static final String PERMISSIONS = "public_profile," +
            " pages_show_list," +
            " pages_manage_posts," +
            " pages_read_engagement," +
            " instagram_basic," +
            " instagram_content_publish," +
            " instagram_manage_comments," +
            " user_posts";

    public FacebookLogInManager(LogInActivity logInActivity) {
        this.logInActivity = logInActivity;
        checkIfLoggedIn();
        setFacebookCallback();
    }

    private void setFacebookCallback() {
        accessTokenTracker = createFacebookAccessTokenTracker();
        logInActivity.fbLoginButton.setPermissions(PERMISSIONS);
        callbackManager = CallbackManager.Factory.create();
        logInActivity.fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG_FACEBOOK, "Success!");
                accessTokenTracker.startTracking();
                userAccessToken = loginResult.getAccessToken();
                requestFacebookInfo();
            }

            @Override
            public void onCancel() {
                Log.e(TAG_FACEBOOK, "Cancelled!");
            }

            @Override
            public void onError(FacebookException error) {
                error.printStackTrace();
                Log.e(TAG_FACEBOOK, "Failed!");
            }
        });
    }

    public void requestFacebookInfo() {
        GraphRequest userGraphRequest = GraphRequest.newMeRequest(userAccessToken,
                (object, response) -> displayFacebookUserInfo(object));
        Bundle userParameters = new Bundle();
        userParameters.putString("fields", "first_name, last_name, picture.type(large)");
        userGraphRequest.setParameters(userParameters);
        userGraphRequest.executeAsync();

        GraphRequest pageGraphRequest = GraphRequest.newGraphPathRequest(userAccessToken,
                "me/accounts", this::displayFacebookPageInfo);
        Bundle pageParameters = new Bundle();
        pageParameters.putString("fields", "name, picture, access_token, id, instagram_business_account");
        pageGraphRequest.setParameters(pageParameters);
        pageGraphRequest.executeAsync();
    }

    private void displayFacebookPageInfo(GraphResponse response) {
        try {
            JSONObject pageData = response.getJSONObject().getJSONArray("data").getJSONObject(0);
            pageAccessToken = new AccessToken(pageData.getString("access_token"),
                    logInActivity.getResources().getString(R.string.facebook_app_id), pageData.getString("id"),
                    null, null, null, null,
                    null, null, null);
            logInActivity.fbPageName.setText(pageData.getString("name"));
            String imageUrl = pageData.getJSONObject("picture").getJSONObject("data").getString("url");
            Picasso.with(logInActivity).load(imageUrl).into(logInActivity.fbPageImage);
            requestInstagramInfo(pageData.getJSONObject("instagram_business_account").getString("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void requestInstagramInfo(String id) {
        GraphRequest instaUserRequest = GraphRequest.newGraphPathRequest(userAccessToken, id, this::displayInstagramUserInfo);
        Bundle instaParameters = new Bundle();
        instaParameters.putString("fields", "name, profile_picture_url, username");
        instaUserRequest.setParameters(instaParameters);
        instaUserRequest.executeAsync();
    }

    @SuppressLint("SetTextI18n")
    private void displayInstagramUserInfo(GraphResponse graphResponse) {
        try {
            JSONObject data = graphResponse.getJSONObject();
            logInActivity.instaName.setText(data.getString("name"));
            logInActivity.instaUsername.setText("@" + data.getString("username"));
            Picasso.with(logInActivity).load(data.getString("profile_picture_url")).into(logInActivity.instaImage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void displayFacebookUserInfo(JSONObject object) {
        String firstName, lastName;
        try {
            firstName = object.getString("first_name");
            lastName = object.getString("last_name");
            String full_name = firstName + " " + lastName;
            logInActivity.fbName.setText(full_name);
            String imageUrl = object.getJSONObject("picture").getJSONObject("data").getString("url");
            Picasso.with(logInActivity).load(imageUrl).into(logInActivity.fbImage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public AccessTokenTracker createFacebookAccessTokenTracker() {
        return new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    Log.d(TAG_FACEBOOK, "Logged out");

                    //Ew, hardcoded strings
                    logInActivity.fbName.setText("Δεν είστε συνδεδεμένοι");
                    logInActivity.fbImage.setImageResource(R.drawable.user_icon);

                    logInActivity.fbPageName.setText("Δεν συνδέθηκε σελίδα");
                    logInActivity.fbPageImage.setImageResource(R.drawable.page_icon);

                    logInActivity.instaName.setText("Δεν είστε συνδεδεμένοι");
                    logInActivity.instaUsername.setText("");
                    logInActivity.instaImage.setImageResource(R.drawable.user_icon);
                }
            }
        };
    }

    private void checkIfLoggedIn() {
        if (AUTO_LOG_OUT) {
            LoginManager.getInstance().logOut();
        }
        else {
            userAccessToken = AccessToken.getCurrentAccessToken();
            if (userAccessToken != null) {
                requestFacebookInfo();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void onDestroy() {
        accessTokenTracker.stopTracking();
    }

    public boolean isLoggedIn() {return AccessToken.getCurrentAccessToken() != null;}

    public AccessToken getUserAccessToken() {
        return userAccessToken;
    }

    public AccessToken getPageAccessToken() {
        return pageAccessToken;
    }
}
