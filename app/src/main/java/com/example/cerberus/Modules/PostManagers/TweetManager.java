package com.example.cerberus.Modules.PostManagers;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.example.cerberus.FeedActivity;
import com.example.cerberus.Modules.CustomTwitterApiClient;
import com.example.cerberus.Modules.PhotoLoader.PhotoInfo;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Media;
import com.twitter.sdk.android.core.models.Tweet;

import java.io.ByteArrayOutputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;

public class TweetManager {
    private static final String TAG = "TAG";
    public static final int QUALITY = 100;
    private final FeedActivity feedActivity;
    private final CustomTwitterApiClient twitterClient;
    private PhotoInfo loadedPhotoInfo = null;
    private String status = null;

    public TweetManager(FeedActivity feedActivity) {
        this.feedActivity = feedActivity;
        twitterClient = (CustomTwitterApiClient) TwitterCore.getInstance().getApiClient();
    }

    public void post() {
        loadedPhotoInfo = feedActivity.loadedPhotoInfo;
        //Save status because it will be erased from the TextView
        status = feedActivity.postTextView.getText().toString();
        if (loadedPhotoInfo != null) {
            new Thread(this::tweetWithImage).start();
        }
        else tweetStatus(null);
    }

    //Computationally heavy
    private void tweetWithImage() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        loadedPhotoInfo.bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, stream);
        byte[] byteArray = stream.toByteArray();
        //Easy way
        Call<Media> mediaCall = twitterClient.getMediaService().upload(RequestBody
            .create(MediaType.parse("image/*"), byteArray), null, null);
        mediaCall.enqueue(new Callback<Media>() {
            @Override
            public void success(Result<Media> result) {
                Log.d(TAG, "Uploaded image to Twitter successfully.");
                tweetStatus(result.data.mediaIdString);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d(TAG, "Failed to upload image to Twitter.");
            }
        });

        //Hard way
        /*try {
            //Initialize media upload
            byte[][] chunks = divideArray(byteArray, MAX_UPLOAD_SIZE);
            Call<UploadResponse> uploadCall = twitterClient.getUploadMediaService().initUpload(byteArray.length);
            Response<UploadResponse> uploadResponse = uploadCall.execute();
            if (uploadResponse.isSuccessful()) {
                String mediaId = uploadResponse.body().media_id_string;
                //Send media chunks
                for (int i = 0; i < chunks.length; i++) {
                    Log.d(TAG, "Sending chunk");
                    Call<ResponseBody> appendCall = twitterClient.getUploadMediaService().appendUpload(
                            mediaId, RequestBody.create(MediaType.parse("image/*"), chunks[i]), i);
                    Response<ResponseBody> appendResponse = appendCall.execute();
                    while (!appendResponse.isSuccessful()) {
                        Log.d(TAG, "Failed chunk, retrying");
                        Log.d(TAG, appendResponse.errorBody().string());
                        appendResponse = appendCall.clone().execute();
                    }
                }
                //Finalize upload
                Call<UploadResponse> finalizeCall = twitterClient.getUploadMediaService().finalizeUpload(mediaId);
                Response<UploadResponse> finalizeResponse = finalizeCall.execute();
                if (finalizeResponse.isSuccessful()) {
                    tweetStatus(mediaId);
                }
                else {
                    Log.d(TAG, "Failed to finalize media upload.");
                    Log.d(TAG, finalizeResponse.errorBody().string());
                }
            }
            else {
                Log.d(TAG, "Failed to initialize upload.");
                Log.d(TAG, uploadResponse.errorBody().string());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    private void tweetStatus(String mediaIdString) {
        Call<Tweet> call = twitterClient.getStatusesService().update(status,null,
                null, null, null, null, null,
                null, mediaIdString);
        call.enqueue(new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                Log.d(TAG, "Posted on Twitter successfully");
                Toast.makeText(feedActivity, "Posted on Twitter!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d(TAG, "Failed to post on Twitter");
                Log.d(TAG, exception.getMessage());
            }
        });
    }
}
