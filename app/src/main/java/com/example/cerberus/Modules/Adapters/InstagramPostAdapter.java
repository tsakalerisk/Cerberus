package com.example.cerberus.Modules.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cerberus.Fragments.InstagramSearchFragment;
import com.example.cerberus.LogInActivity;
import com.example.cerberus.Modules.Responses.InstagramResponses.GetHashtagIdResponse;
import com.example.cerberus.Modules.Responses.InstagramResponses.GetMediaInfoResponse;
import com.example.cerberus.Modules.Responses.InstagramResponses.GetTopMediaResponse;
import com.example.cerberus.Modules.Responses.InstagramResponses.Media;
import com.example.cerberus.Modules.Responses.InstagramResponses.User;
import com.example.cerberus.R;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/*
Adapter used to display Instagram posts.
Also contains methods to get the posts.
 */
public class InstagramPostAdapter extends RecyclerView.Adapter<InstagramPostAdapter.ViewHolder>{
    private static final String TAG = "TAG";
    private final Activity activity;
    private final Gson gson = new Gson();
    private AccessToken token;
    private String userId;
    private List<Media> media;
    private List<User> users;

    public InstagramPostAdapter(Activity activity) {
        this.activity = activity;
    }

    /*
    Gets Instagram posts based on a query. The query must be in English
    and relatively popular, or an error will be returned. Due to Instagram's
    API limitations we can not retrieve the username or the profile picture
    of the users that made the posts, so they're replaced by placeholders instead.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void getSearchResults(InstagramSearchFragment instagramSearchFragment, String query) {
        token = activity.getIntent().getParcelableExtra(LogInActivity.FB_USER_TOKEN_LITERAL);
        userId = activity.getIntent().getStringExtra(LogInActivity.INSTA_USER_ID);

        //The process takes place sequentially on a separate thread so that there won't be any nested callbacks.
        new Thread(() -> {
            try {
                String hashtagId = getHashtagId(query);
                media = getTopMedia(hashtagId);
                /*users = new ArrayList<>();
                for (Media medium: media) {
                    users.add(getUser(medium));
                }*/
                activity.runOnUiThread(() -> {
                    notifyDataSetChanged();
                    instagramSearchFragment.progressBar.setVisibility(View.GONE);
                    instagramSearchFragment.swipeLayout.setRefreshing(false);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /*private User getUser(Media medium) throws IOException {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(medium.permalink)
                .addConverterFactory(GsonConverterFactory.create()).build();
        Call<GetMediaInfoResponse> getMediaInfoCall = retrofit.create(MediaInfo.class).getMediaInfo(token.getToken());
        Response<GetMediaInfoResponse> getMediaInfoResponse = getMediaInfoCall.execute();
        if (getMediaInfoResponse.isSuccessful() && getMediaInfoResponse.body() != null) {
            return getMediaInfoResponse.body().graphql.shortcode_media.owner;
        }
        else {
            return null;
        }
    }

    private interface MediaInfo {
        @GET("?__a=1")
        Call<GetMediaInfoResponse> getMediaInfo(@Query("access_token") String token);
    }

    private Media getMedia(String mediaId) throws IOException {
        GraphRequest getMediaRequest = GraphRequest.newGraphPathRequest(token, mediaId, null);
        getMediaRequest.setVersion("v11.0");
        Bundle getMediaParams = new Bundle();
        getMediaParams.putString("fields", "id");
        getMediaParams.putString("access_token", token.getToken());
        getMediaParams.putString("fields", "caption, comments_count, id, like_count, media_type, media_url, owner, permalink, timestamp, username");
        getMediaRequest.setParameters(getMediaParams);
        GraphResponse unparsedResponse = getMediaRequest.executeAndWait();
        if (unparsedResponse.getConnection().getResponseCode() == HttpURLConnection.HTTP_OK) {
            return gson.fromJson(unparsedResponse.getRawResponse(), Media.class);
        }
        else {
            return null;
        }
    }*/


    /*
    Gets the top posts containing the specified hashtagId, deserializes the JSON
    response into a list of Media objects using Gson, and returns the list.
    Uses this endpoint: https://developers.facebook.com/docs/instagram-api/reference/ig-hashtag/top-media
     */
    private List<Media> getTopMedia(String hashtagId) throws IOException {
        GraphRequest searchPostsRequest = GraphRequest.newGraphPathRequest(token, hashtagId + "/top_media", null);

        Bundle searchPostsParams = new Bundle();
        searchPostsParams.putString("user_id", userId);
        searchPostsParams.putString("fields", "caption, comments_count, id, like_count, media_type, media_url, permalink, timestamp");
        searchPostsRequest.setParameters(searchPostsParams);

        GraphResponse unparsedResponse = searchPostsRequest.executeAndWait(); //Runs on a separate thread

        if (unparsedResponse.getConnection().getResponseCode() == HttpURLConnection.HTTP_OK) {
            Log.d(TAG, "Successfully got top media from instagram.");
            GetTopMediaResponse getTopMediaResponse = gson.fromJson(unparsedResponse.getRawResponse(), GetTopMediaResponse.class);
            return getTopMediaResponse.data;
        }
        else {
            Log.d(TAG, "Failed to get top media from Instagram.");
            return null;
        }
    }

    /*
    Gets the hashtag id related to the specified query.
    Uses this endpoint: https://developers.facebook.com/docs/instagram-api/reference/ig-hashtag-search
     */
    private String getHashtagId(String query) throws IOException {
        GraphRequest getHashtagIdRequest = GraphRequest.newGraphPathRequest(token, "/ig_hashtag_search/", null);

        Bundle getHashtagIdParams = new Bundle();
        getHashtagIdParams.putString("user_id", userId);
        getHashtagIdParams.putString("q", query);
        getHashtagIdRequest.setParameters(getHashtagIdParams);

        GraphResponse unparsedResponse = getHashtagIdRequest.executeAndWait(); //Runs on a separate thread

        if (unparsedResponse.getConnection().getResponseCode() == HttpURLConnection.HTTP_OK) {
            Log.d(TAG, "Successfully got hashtag id from Instagram.");
            GetHashtagIdResponse getHashtagIdResponse = gson.fromJson(unparsedResponse.getRawResponse(), GetHashtagIdResponse.class);
            return getHashtagIdResponse.data[0].id;
        }
        else {
            Log.d(TAG, "Failed to get hashtag id from Instagram. " + unparsedResponse.getError().getErrorMessage());
            return null;
        }
    }

    @NonNull
    @Override
    public InstagramPostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.instagram_post_layout, parent, false);
        return new InstagramPostAdapter.ViewHolder(view);
    }

    /*
    Because Instagram doesn't allow getting information about the poster, such as username
    and profile picture, I am using placeholders here.
     */
    @Override
    public void onBindViewHolder(@NonNull InstagramPostAdapter.ViewHolder holder, int position) {
        //Picasso.with(activity).load(users.get(position).profile_pic_url).into(holder.userImageView);
        //holder.usernameTextView.setText(users.get(position).username);
        holder.userImageView.setImageResource(R.drawable.user_icon);
        holder.usernameTextView.setText("username");

        Picasso.with(activity).load(media.get(position).media_url).into(holder.mediaImageView);
        holder.likeTextView.setText(activity.getResources().getString(R.string.x_likes, media.get(position).like_count));

        //String captionHtml = "<b>" + users.get(position).username + "</b> " + media.get(position).caption;
        String captionHtml = "<b>username</b> " + media.get(position).caption;

        holder.captionTextView.setText(Html.fromHtml(captionHtml));
    }

    @Override
    public int getItemCount() {
        return media == null ? 0 : media.size();
    }

    /*
    As far as I know Instagram doesn't allow reactions on posts obtained
    via hashtag search (the id we get from the search is different than
    the id needed to comment), so the reaction buttons are currently unimplemented.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView userImageView;
        private final TextView usernameTextView;
        private final ImageView mediaImageView;
        private final ImageButton likeImageButton;
        private final ImageButton commentImageButton;
        private final ImageButton shareImageButton;
        private final ImageButton bookmarkImageButton;
        private final TextView likeTextView;
        private final TextView captionTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userImageView = itemView.findViewById(R.id.userImageView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            mediaImageView = itemView.findViewById(R.id.mediaImageView);
            likeImageButton = itemView.findViewById(R.id.likeImageButton);
            commentImageButton = itemView.findViewById(R.id.commentImageButton);
            shareImageButton = itemView.findViewById(R.id.shareImageButton);
            bookmarkImageButton = itemView.findViewById(R.id.bookmarkImageButton);
            likeTextView = itemView.findViewById(R.id.likeTextView);
            captionTextView = itemView.findViewById(R.id.captionTextView);
        }
    }
}
