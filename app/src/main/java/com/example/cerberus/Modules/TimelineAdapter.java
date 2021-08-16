package com.example.cerberus.Modules;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cerberus.FeedActivity;
import com.example.cerberus.R;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.TweetView;

import java.util.List;

import retrofit2.Call;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ViewHolder> {
    private List<Tweet> postList;
    private static final String TAG = "TAG";

    public TimelineAdapter(FeedActivity feedActivity) {
        Call<List<Tweet>> getTimeline = TwitterCore.getInstance().getApiClient().getStatusesService().homeTimeline(200,
                null, null, null, null, null, null);
        getTimeline.enqueue(new Callback<List<Tweet>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void success(Result<List<Tweet>> result) {
                setData(result.data);
                feedActivity.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d(TAG, "Failed getting tweet timeline");
                exception.printStackTrace();
            }
        });
    }

    /*public TimelineAdapter(List<Tweet> postList) {
        setData(postList);
    }*/

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<Tweet> postList) {
        this.postList = postList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TimelineAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.timeline_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineAdapter.ViewHolder holder, int position) {
        //Log.d(TAG, "Binding timeline item number " + position);
        holder.getTweetView().setTweet(postList.get(position));
    }

    @Override
    public int getItemCount() {
        return postList == null ? 0 : postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CustomTweetView tweetView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            //itemView.setOnClickListener(v -> {});
            ViewGroup layout = itemView.findViewById(R.id.layout);
            tweetView = new CustomTweetView(itemView.getContext(), (Tweet) null, R.style.tw__TweetLightWithActionsStyle);
            layout.addView(tweetView);
        }

        public TweetView getTweetView() {
            return tweetView;
        }
    }
}
