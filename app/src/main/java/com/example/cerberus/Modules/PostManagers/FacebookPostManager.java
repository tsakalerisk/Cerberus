package com.example.cerberus.Modules.PostManagers;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cerberus.FeedActivity;
import com.example.cerberus.LogInActivity;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

public class FacebookPostManager {
    private final FeedActivity feedActivity;
    private final AccessToken pageAccessToken;
    private Bitmap bitmap;
    private final TextView textView;
    public static final String TAG = FeedActivity.TAG;

    public FacebookPostManager(FeedActivity feedActivity) {
        this.feedActivity = feedActivity;
        textView = feedActivity.postTextView;
        pageAccessToken = feedActivity.getIntent().getParcelableExtra(LogInActivity.FB_PAGE_TOKEN_LITERAL);
    }

    public void post() {
        if (feedActivity.loadedPhotoInfo != null) {
            bitmap = feedActivity.loadedPhotoInfo.bitmap
                    .copy(feedActivity.loadedPhotoInfo.bitmap.getConfig(), true);
            postPhoto();
        } else {
            postText();
        }
    }

    private void postPhoto() {
        GraphRequest graphRequest = GraphRequest.newUploadPhotoRequest(pageAccessToken,
                pageAccessToken.getUserId() + "/photos",
                bitmap,
                textView.getText().toString(),
                null,
                response -> {
                    bitmap.recycle();
                    try {
                        if (response.getConnection().getResponseCode() == HttpURLConnection.HTTP_OK) {
                            Log.d(TAG, "Posted image on Facebook successfully.");
                            Toast.makeText(feedActivity, "Posted on Facebook!", Toast.LENGTH_SHORT).show();
                        } else
                            Log.d(TAG, "Failed to upload image on Facebook.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        graphRequest.executeAsync();
    }

    private void postText() {
        try {
            JSONObject post = new JSONObject();
            post.put("message", textView.getText().toString());
            GraphRequest graphRequest = GraphRequest.newPostRequest(pageAccessToken,
                    pageAccessToken.getUserId() + "/feed",
                    post,
                    response -> {
                        try {
                            if (response.getConnection().getResponseCode() == HttpURLConnection.HTTP_OK) {
                                Log.d(TAG, "Posted text on Facebook successfully.");
                                Toast.makeText(feedActivity, "Posted on Facebook!", Toast.LENGTH_SHORT).show();
                            } else
                                Log.d(TAG, "Failed to post text on Facebook.");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            );
            graphRequest.executeAsync();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
