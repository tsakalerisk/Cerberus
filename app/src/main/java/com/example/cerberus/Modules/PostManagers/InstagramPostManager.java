package com.example.cerberus.Modules.PostManagers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import com.example.cerberus.BuildConfig;
import com.example.cerberus.FeedActivity;
import com.example.cerberus.LogInActivity;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.DELETE;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public class InstagramPostManager {
    private final FeedActivity feedActivity;
    private Bitmap bitmap;
    private String text;
    private ImgurApi imgurApi;
    private final AccessToken userAccessToken;
    private final String instaUserId;
    public static final String TAG = FeedActivity.TAG;

    public InstagramPostManager(FeedActivity feedActivity) {
        this.feedActivity = feedActivity;
        userAccessToken = feedActivity.getIntent().getParcelableExtra(LogInActivity.FB_USER_TOKEN_LITERAL);
        instaUserId = feedActivity.getIntent().getStringExtra(LogInActivity.INSTA_USER_ID);
    }

    public void post() {
        text = feedActivity.postTextView.getText().toString();
        bitmap = feedActivity.loadedPhotoInfo.bitmap;
        bitmap = createSquaredBitmap(bitmap);

        new Thread(() -> {
            final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .writeTimeout(90, TimeUnit.SECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.imgur.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
            imgurApi = retrofit.create(ImgurApi.class);
            try {
                ImgurResponse imgurResponse = postToImgur();
                if (imgurResponse != null) {
                    String id = createMedia(imgurResponse);
                    if (id != null) {
                        if (publishPost(id)) {
                            feedActivity.runOnUiThread(() -> Toast.makeText(feedActivity,
                                    "Posted on Instagram!",
                                    Toast.LENGTH_SHORT)
                                    .show());
                        }
                    }
                    deleteFromImgur(imgurResponse.data.deletehash);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            bitmap.recycle();
        }).start();
    }

    private Bitmap createSquaredBitmap(Bitmap srcBmp) {
        int dim = Math.max(srcBmp.getWidth(), srcBmp.getHeight());
        Bitmap dstBmp = Bitmap.createBitmap(dim, dim, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(dstBmp);
        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(srcBmp,
                (dim - srcBmp.getWidth()) >> 1,
                (dim - srcBmp.getHeight()) >> 1,
                null);

        return dstBmp;
    }

    private void deleteFromImgur(String deleteHash) throws IOException {
        Response<ResponseBody> response = imgurApi.deleteImage(deleteHash).execute();
        if (response.isSuccessful())
            Log.d(TAG, "Deleted from Imgur successfully.");
        else
            Log.d(TAG, "Failed to delete from Imgur.");
    }

    private boolean publishPost(String id) throws JSONException, IOException {
        JSONObject body = new JSONObject();
        body.put("creation_id", id);
        GraphRequest publishPost = GraphRequest.newPostRequest(userAccessToken,
                instaUserId + "/media_publish",
                body,
                null);
        GraphResponse response = publishPost.executeAndWait();
        if (response.getConnection().getResponseCode() == HttpURLConnection.HTTP_OK) {
            Log.d(TAG, "Successfully posted on Instagram.");
            return true;
        }
        else {
            Log.d(TAG, "Failed to post on Instagram.");
            Log.d(TAG, response.getError().getErrorMessage());
            return false;
        }
    }

    private String createMedia(ImgurResponse imgurResponse) throws JSONException, IOException {
        JSONObject body = new JSONObject();
        body.put("caption", text);
        body.put("image_url", imgurResponse.data.link);
        GraphRequest createMedia = GraphRequest.newPostRequest(userAccessToken,
                instaUserId + "/media",
                body,
                null);
        GraphResponse response = createMedia.executeAndWait();
        if (response.getConnection().getResponseCode() == HttpURLConnection.HTTP_OK) {
            Log.d(TAG, "Successfully created Instagram media.");
            return response.getJSONObject().getString("id");
        }
        else {
            Log.d(TAG, "Failed to create Instagram media.");
            Log.d(TAG, response.getError().getErrorMessage());
            return null;
        }
    }

    private ImgurResponse postToImgur() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), byteArray);
        Call<ImgurResponse> call = imgurApi.uploadImage(requestBody);
        Response<ImgurResponse> response = call.execute();
        if (response.isSuccessful()) {
            Log.d(TAG, "Posted on Imgur successfully. ");
            Log.d(TAG, "Link: " + Objects.requireNonNull(response.body()).data.link);
            Log.d(TAG, "Delete hash: " + response.body().data.deletehash);
            return response.body();
        }
        else {
            Log.d(TAG, "Failed to post on Imgur.");
            Log.d(TAG, Objects.requireNonNull(response.errorBody()).string());
            return null;
        }
    }

    public interface ImgurApi {
        @Multipart
        @POST("/3/image")
        @Headers("Authorization: Client-ID " + BuildConfig.ImgurClientId)
        Call<ImgurResponse> uploadImage(@Part("image") RequestBody image);

        @DELETE("/3/image/{deletehash}")
        @Headers("Authorization: Client-ID " + BuildConfig.ImgurClientId)
        Call<ResponseBody> deleteImage(@Path("deletehash") String deleteHash);
    }

    public static class ImgurResponse {
        public Data data;
        public static class Data {
            public String link;
            public String deletehash;
        }
    }
}
