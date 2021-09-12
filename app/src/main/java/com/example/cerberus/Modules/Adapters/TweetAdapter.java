package com.example.cerberus.Modules.Adapters;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cerberus.FeedActivity;
import com.example.cerberus.Modules.CustomViews.CustomTweetView;
import com.example.cerberus.R;
import com.example.cerberus.Fragments.TwitterSearchFragment;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.models.Tweet;

import java.util.List;

import retrofit2.Call;

/*
Adapter used to display Tweets.
Also contains methods to get them.
 */
public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.ViewHolder> {
    public static final int MAX_SEARCH_TWEETS = 100;
    public static final int MAX_TIMELINE_TWEETS = 200;
    private List<Tweet> postList;
    private static final String TAG = "TAG";

    /*
    Gets tweets based on a specified query.
    Uses this endpoint: https://developer.twitter.com/en/docs/twitter-api/v1/tweets/search/api-reference/get-search-tweets
     */
    public void getSearchResults(TwitterSearchFragment twitterSearchFragment, String q) {
        Call<Search> getSearchResults = TwitterCore.getInstance().getApiClient().getSearchService()
                .tweets(q, null, null, null, null, MAX_SEARCH_TWEETS,
                        null, null, null, null);
        getSearchResults.enqueue(new Callback<Search>() {
            @Override
            public void success(Result<Search> result) {
                setData(result.data.tweets);
                twitterSearchFragment.progressBar.setVisibility(View.GONE);
                twitterSearchFragment.swipeLayout.setRefreshing(false);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d(TAG, "Failed searching tweets for: " + q);
                exception.printStackTrace();
                twitterSearchFragment.swipeLayout.setRefreshing(false);
            }
        });
    }

    /*
    Gets tweets from the home timeline of a logged in user.
    Uses this endpoint: https://developer.twitter.com/en/docs/twitter-api/v1/tweets/timelines/api-reference/get-statuses-home_timeline
     */
    public void getTimeline(FeedActivity feedActivity) {
        Call<List<Tweet>> getTimeline = TwitterCore.getInstance().getApiClient().getStatusesService()
                .homeTimeline(MAX_TIMELINE_TWEETS, null, null, null,
                        null, null, null);
        getTimeline.enqueue(new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> result) {
                setData(result.data);
                feedActivity.progressBar.setVisibility(View.GONE);
                feedActivity.swipeLayout.setRefreshing(false);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d(TAG, "Failed getting tweet timeline");
                exception.printStackTrace();
                feedActivity.swipeLayout.setRefreshing(false);
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<Tweet> postList) {
        this.postList = postList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TweetAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tweet_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TweetAdapter.ViewHolder holder, int position) {
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
            ViewGroup layout = itemView.findViewById(R.id.layout);
            tweetView = new CustomTweetView(itemView.getContext(), (Tweet) null, R.style.tw__TweetLightWithActionsStyle);
            layout.addView(tweetView);
        }

        public CustomTweetView getTweetView() {
            return tweetView;
        }
    }
}
