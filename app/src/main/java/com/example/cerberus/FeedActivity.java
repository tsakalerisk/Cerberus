package com.example.cerberus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cerberus.Modules.CustomLinearLayoutManager;
import com.example.cerberus.Modules.CustomTwitterApiClient;
import com.example.cerberus.Modules.NetworkCallback;
import com.example.cerberus.Modules.PhotoLoader;
import com.example.cerberus.Modules.PhotoLoader.PhotoInfo;
import com.example.cerberus.Modules.PostManagers.FacebookPostManager;
import com.example.cerberus.Modules.PostManagers.InstagramPostManager;
import com.example.cerberus.Modules.PostManagers.TweetManager;
import com.example.cerberus.Modules.CustomViews.PostToggleButton;
import com.example.cerberus.Modules.SearchManager;
import com.example.cerberus.Modules.Adapters.TweetAdapter;
import com.example.cerberus.Modules.TimelineAnimationManager;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;

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
    private RecyclerView timelineRecyclerView = null;
    public ProgressBar progressBar = null;
    public SwipeRefreshLayout swipeLayout = null;

    private final PhotoLoader photoLoader = new PhotoLoader(this);
    public PhotoInfo loadedPhotoInfo = null;

    private FacebookPostManager facebookPostManager;
    private TweetManager tweetManager;
    private InstagramPostManager instagramPostManager;

    public static final String TAG = "TAG";
    public static final boolean DISABLE_POST = false;
    private ConnectivityManager connectivityManager;
    private NetworkCallback networkCallback;

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

        //Create post listeners
        facebookPostManager = new FacebookPostManager(FeedActivity.this);
        tweetManager = new TweetManager(FeedActivity.this);
        instagramPostManager = new InstagramPostManager(FeedActivity.this);

        //Init timeline adapter
        TweetAdapter tweetAdapter = new TweetAdapter();
        timelineRecyclerView.setAdapter(tweetAdapter);
        tweetAdapter.getTimeline(FeedActivity.this);
        timelineRecyclerView.setLayoutManager(new CustomLinearLayoutManager(FeedActivity.this));

        TimelineAnimationManager.init(FeedActivity.this, timelineRecyclerView);
        swipeLayout.setOnRefreshListener(() -> tweetAdapter.getTimeline(FeedActivity.this));

        checkForNetwork();
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
        timelineRecyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        swipeLayout = findViewById(R.id.swipeLayout);
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
                    alert(getResources().getString(R.string.choose_instagram_image));
                }
                else {
                    if (fbPostToggle.isChecked()) {
                        facebookPostManager.post();
                    }
                    if (twPostToggle.isChecked()) {
                        tweetManager.post();
                    }
                    if (instaPostToggle.isChecked()) {
                        instagramPostManager.post();
                    }
                    clearPostViews();
                }
            }
            else
                alert(getResources().getString(R.string.posts_disabled));
        });
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

    private void clearPostViews() {
        hideKeyboard();
        postTextView.setText("");
        deletePhoto();
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(FeedActivity.this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void alert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(FeedActivity.this);
        builder.setMessage(message)
                .setPositiveButton(getResources().getString(R.string.ok), null)
                .create()
                .show();
    }

    //Change views if a photo is unloaded
    private void deletePhoto() {
        if (loadedPhotoInfo != null) {
            photoImageView.setImageBitmap(null);
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

    //Change views if a photo is loaded
    public void onPhotoReturned(PhotoInfo photoInfo) {
        photoButtonLayout.setVisibility(View.VISIBLE);
        addPhotoButton.setVisibility(View.INVISIBLE);
        loadedPhotoInfo = photoInfo;
        photoImageView.setImageBitmap(loadedPhotoInfo.bitmap);
        photoNameTextView.setText(loadedPhotoInfo.name);
    }

    private void checkForNetwork() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getActiveNetworkInfo() == null) {
            Intent intent = new Intent(this, NoInternetActivity.class);
            intent.putExtra(NetworkCallback.ACTIVITY_FROM, getClass());
            intent.putExtras(getIntent().getExtras());
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}