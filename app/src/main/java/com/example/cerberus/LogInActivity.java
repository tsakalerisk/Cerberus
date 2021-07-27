package com.example.cerberus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.Twitter;
import twitter4j.auth.RequestToken;

public class LogInActivity extends AppCompatActivity {

    public static final String TAG_TWITTER = "Twitter";
    public static final String TAG_FACEBOOK = "Facebook";
    private LoginButton fbLoginButton = null;
    private ImageView fbImage = null;
    private TextView fbName = null;
    private Button twLoginButton = null;
    private ImageView twImage = null;
    private TextView twName = null;
    private CallbackManager callbackManager = null;
    private Twitter twitter = null;
    private RequestToken requestToken = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_log_in);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        findAllViews();
        setFacebookCallback();
    }

    private void setFacebookCallback() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        FacebookSdk.setApplicationId(BuildConfig.FacebookAppId);
        fbLoginButton.setPermissions("email", "public_profile");
        callbackManager = CallbackManager.Factory.create();
        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG_FACEBOOK, "Success!");
                /*String userId = loginResult.getAccessToken().getUserId();
                GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        displayFacebookInfo(object);
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "first_name, last_name, picture.type(large)");
                graphRequest.setParameters(parameters);
                graphRequest.executeAsync();*/
            }

            @Override
            public void onCancel() {
                Log.e(TAG_FACEBOOK, "Cancelled");
            }

            @Override
            public void onError(FacebookException error) {
                error.printStackTrace();
                Log.e(TAG_FACEBOOK, error.toString());
            }
        });
    }

    protected void findAllViews() {
        fbLoginButton = findViewById(R.id.fbButton);
        fbImage = findViewById(R.id.fbImage);
        fbName = findViewById(R.id.fbName);

        //twLoginButton = findViewById(R.id.twButton);
        twImage = findViewById(R.id.twImage);
        twName = findViewById(R.id.twName);
    }

    public void displayFacebookInfo(JSONObject object) {
        String firstName, lastName;
        firstName = "";
        lastName = "";
        try {
            firstName = object.getString("first_name");
            lastName = object.getString("last_name");
            String imageUrl = object.getJSONObject("picture").getJSONObject("data").getString("url");
            Picasso.get().load(imageUrl).into(fbImage);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        fbName.setText(firstName + " " + lastName);
    }
}