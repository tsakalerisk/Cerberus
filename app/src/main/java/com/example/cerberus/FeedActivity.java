package com.example.cerberus;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.cerberus.Modules.CustomTwitterApiClient;
import com.example.cerberus.Modules.PhotoLoader;
import com.example.cerberus.Modules.PhotoLoader.PhotoInfo;
import com.example.cerberus.Modules.PostManagers.FacebookPostManager;
import com.example.cerberus.Modules.PostToggleButton;
import com.example.cerberus.Modules.SearchManager;
import com.example.cerberus.Modules.PostManagers.TweetManager;
import com.facebook.AccessToken;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.TweetView;

import java.util.List;

import retrofit2.Call;

public class FeedActivity extends AppCompatActivity {

    public SearchView searchView = null;
    public TextView postTextView = null;
    private PostToggleButton fbPostToggle = null;
    private ToggleButton twPostToggle = null;
    private ToggleButton instaPostToggle = null;
    private Button addPhotoButton = null;
    private Button postButton = null;
    private ConstraintLayout photoButtonLayout = null;
    private ImageView photoImageView = null;
    private TextView photoNameTextView = null;
    private Button deletePhotoButton = null;

    private final PhotoLoader photoLoader = new PhotoLoader(this);
    public PhotoInfo loadedPhotoInfo = null;

    public static final String TAG = "TAG";
    public static final boolean DISABLE_POST = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Maybe
        Twitter.initialize(FeedActivity.this);
        TwitterSession activeSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
        TwitterCore.getInstance().addApiClient(activeSession, new CustomTwitterApiClient(activeSession));

        setContentView(R.layout.activity_feed);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        findAllViews();
        SearchManager.setUpSearchView(FeedActivity.this);
        setUpButtonListeners();

        //Get first tweet
        Call<List<Tweet>> getTimeline = TwitterCore.getInstance().getApiClient().getStatusesService().homeTimeline(null,
                null, null, null, null, null, null);
        getTimeline.enqueue(new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> result) {
                TweetView tweetView = findViewById(R.id.tweetView);
                tweetView.setTweet(result.data.get(0));
                //Stop view from opening Twitter on click
                tweetView.setOnClickListener(v -> {});
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d(TAG, "Failed getting first tweet");
                exception.printStackTrace();
            }
        });
    }

    private void findAllViews() {
        searchView = findViewById(R.id.searchView);
        postTextView = findViewById(R.id.postTextView);
        fbPostToggle = findViewById(R.id.fbPostToggle);
        twPostToggle = findViewById(R.id.twPostToggle);
        instaPostToggle = findViewById(R.id.instaPostToggle);
        addPhotoButton = findViewById(R.id.addPhotoButton);
        postButton = findViewById(R.id.postButton);
        photoButtonLayout = findViewById(R.id.photoButtonLayout);
        photoImageView = findViewById(R.id.photoImageView);
        photoNameTextView = findViewById(R.id.photoName);
        deletePhotoButton = findViewById(R.id.deletePhotoButton);
    }

    private void setUpButtonListeners() {
        fbPostToggle.setOnClickListener(v -> updatePostButton());
        twPostToggle.setOnClickListener(v -> updatePostButton());
        instaPostToggle.setOnClickListener(v -> updatePostButton());

        addPhotoButton.setOnClickListener(v -> photoLoader.init());

        deletePhotoButton.setOnClickListener(v -> deletePhoto());

        postButton.setOnClickListener(v -> {
            if (!DISABLE_POST) {
                if (instaPostToggle.isChecked() && loadedPhotoInfo == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(FeedActivity.this);
                    builder.setMessage("Επιλέξτε μία εικόνα για να ανεβάσετε στο Instagram.")
                            .setPositiveButton("OK", null)
                            .create().show();
                }
                else {
                    if (fbPostToggle.isChecked()) {
                        new FacebookPostManager(this).post();
                        clearPostViews();
                    }
                    if (twPostToggle.isChecked()) {
                        new TweetManager(this).post();
                        clearPostViews();
                    }
                    if (instaPostToggle.isChecked()) {

                    }
                }
            }
        });
    }

    private void clearPostViews() {
        postTextView.setText("");
        deletePhoto();
    }

    private void deletePhoto() {
        if (loadedPhotoInfo != null) {
            loadedPhotoInfo.bitmap.recycle();
            loadedPhotoInfo = null;
        }
        photoButtonLayout.setVisibility(View.GONE);
        addPhotoButton.setVisibility(View.VISIBLE);
    }


    public void updatePostButton() {
        //If at least one toggle is checked, set active
        if (fbPostToggle.isChecked() || twPostToggle.isChecked() || instaPostToggle.isChecked())
            postButton.setEnabled(true);
        else if (!fbPostToggle.isChecked() && !twPostToggle.isChecked() && !instaPostToggle.isChecked())
            postButton.setEnabled(false);
    }

    public void onPhotoReturned(PhotoInfo photoInfo) {
        photoButtonLayout.setVisibility(View.VISIBLE);
        addPhotoButton.setVisibility(View.INVISIBLE);
        loadedPhotoInfo = photoInfo;
        photoImageView.setImageBitmap(loadedPhotoInfo.bitmap);
        photoNameTextView.setText(loadedPhotoInfo.name);
    }
}