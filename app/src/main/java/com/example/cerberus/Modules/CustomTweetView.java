package com.example.cerberus.Modules;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.utils.widget.ImageFilterView;
import androidx.core.content.ContextCompat;

import com.example.cerberus.R;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.internal.UserUtils;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.TweetView;


public class CustomTweetView extends TweetView {
    private ImageFilterView avatarView;
    private View actionBar;
    private static final String TAG = "TAG";
    private TextView textRetweets;
    private TextView textFavorites;
    private ImageButton replyButton;
    private ImageButton retweetButton;
    private ImageButton favoriteButton;

    private Tweet displayTweet;

    public static final int RETWEETED = R.id.retweeted;
    public static final int FAVORITED = R.id.favorited;

    public CustomTweetView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTweetView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomTweetView(Context context, Tweet tweet, int styleResId) {
        super(context, tweet, styleResId);

        ImageView oldAvatarView = findViewById(R.id.tw__tweet_author_avatar);
        ViewGroup parent = (ViewGroup) oldAvatarView.getParent();
        int avatarIndex = parent.indexOfChild(oldAvatarView);

        avatarView = new ImageFilterView(parent.getContext());
        avatarView.setImageDrawable(oldAvatarView.getDrawable());
        avatarView.setRoundPercent(1);
        avatarView.setLayoutParams(oldAvatarView.getLayoutParams());
        avatarView.setId(oldAvatarView.getId());

        parent.removeView(oldAvatarView);
        parent.addView(avatarView, avatarIndex);

        ViewGroup oldActionBar = findViewById(R.id.tw__tweet_action_bar);
        actionBar = LayoutInflater.from(context)
                .inflate(R.layout.twitter_action_bar_layout, null, false);
        actionBar.setId(oldActionBar.getId());
        actionBar.setLayoutParams(oldActionBar.getLayoutParams());

        parent.removeView(oldActionBar);
        parent.addView(actionBar);

        //TextView textReplies = actionBar.findViewById(R.id.text_replies);
        textRetweets = findViewById(R.id.text_retweets);
        textFavorites = findViewById(R.id.text_favorites);

        replyButton = findViewById(R.id.button_reply);
        retweetButton = findViewById(R.id.button_retweet);
        favoriteButton = findViewById(R.id.button_favorite);

        /*EditText editText = new EditText(context);
        editText.setImeOptions(EditorInfo.IME_ACTION_SEND);
        editText.setVisibility(GONE);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.connect(editText.getId(), ConstraintSet.BOTTOM, getRootView().getId(), ConstraintSet.BOTTOM);
        constraintSet.applyTo((ConstraintLayout) getRootView());
        ((ViewGroup)getRootView()).addView(editText);

        replyButton.setOnClickListener(v -> {

        });*/
    }





    @Override
    public void setTweet(Tweet tweet) {
        super.setTweet(tweet);
        String url;
        if (tweet != null) {
            displayTweet = getDisplayTweet();

            if (displayTweet.user != null) {
                url = UserUtils.getProfileImageUrlHttps(displayTweet.user,
                        UserUtils.AvatarSize.REASONABLY_SMALL);
                Picasso.with(getContext()).load(url).into(avatarView);
            }

            //setRetweetCounter(displayTweet.retweetCount);
            setUpFavoriteButton(getContext());
            setUpRetweetButton(getContext());

            //tHiS ObJeCt iS OnLy aVaIlAbLe wItH ThE PrEmIuM AnD EnTeRpRiSe tIeR PrOdUcTs.
            /*textReplies.setText(displayTweet.replyCount != 0 ?
                    String.valueOf(displayTweet.replyCount) : "");*/
        }
    }

    private void setUpRetweetButton(Context context) {
        retweetButton.setTag(RETWEETED, displayTweet.retweeted);
        updateRetweetViews(context, displayTweet.retweeted);
        retweetButton.setOnClickListener(v -> {
            if (!(boolean) retweetButton.getTag(RETWEETED)) {
                TwitterCore.getInstance().getApiClient().getStatusesService()
                        .retweet(displayTweet.id, null).enqueue(new Callback<Tweet>() {
                    @Override
                    public void success(Result<Tweet> result) {
                        Log.d(TAG, "Successfully retweeted.");
                        retweetButton.setTag(RETWEETED, true);
                        updateRetweetViews(context, true);
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        Log.d(TAG, "Failed to retweet.");
                    }
                });
            }
            else {
                TwitterCore.getInstance().getApiClient().getStatusesService()
                        .unretweet(displayTweet.id, null).enqueue(new Callback<Tweet>() {
                    @Override
                    public void success(Result<Tweet> result) {
                        Log.d(TAG, "Successfully un-retweeted.");
                        retweetButton.setTag(RETWEETED, false);
                        updateRetweetViews(context, false);
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        Log.d(TAG, "Failed to un-retweet.");
                    }
                });
            }
        });
    }

    private void setUpFavoriteButton(Context context) {
        favoriteButton.setTag(FAVORITED, displayTweet.favorited);
        updateFavoriteViews(context, displayTweet.favorited);
        favoriteButton.setOnClickListener(v -> {
            if (! (boolean) favoriteButton.getTag(FAVORITED)) {
                TwitterCore.getInstance().getApiClient().getFavoriteService()
                        .create(displayTweet.id, null).enqueue(new Callback<Tweet>() {
                    @Override
                    public void success(Result<Tweet> result) {
                        Log.d(TAG, "Successfully favorited.");
                        favoriteButton.setTag(FAVORITED, true);
                        updateFavoriteViews(context, true);
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        Log.d(TAG, "Failed to favorite.");
                    }
                });
            }
            else {
                TwitterCore.getInstance().getApiClient().getFavoriteService()
                        .destroy(displayTweet.id, null).enqueue(new Callback<Tweet>() {
                    @Override
                    public void success(Result<Tweet> result) {
                        Log.d(TAG, "Successfully un-favorited.");
                        favoriteButton.setTag(FAVORITED, false);
                        updateFavoriteViews(context, false);
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        Log.d(TAG, "Failed to un-favorite.");
                    }
                });
            }
        });
    }

    private void updateRetweetViews(Context context, boolean isRetweeted) {
        boolean wasRetweetedWhenLoaded = displayTweet.retweeted;

        if (isRetweeted) {
            retweetButton.setColorFilter(ContextCompat.getColor(context, R.color.retweet));
            textRetweets.setTextColor(ContextCompat.getColor(context, R.color.retweet));
            setRetweetCounter(displayTweet.retweetCount + (wasRetweetedWhenLoaded ? 0 : 1));
        }
        else {
            retweetButton.setColorFilter(null);
            textRetweets.setTextColor(ContextCompat.getColor(context, android.R.color.secondary_text_light));
            setRetweetCounter(displayTweet.retweetCount - (wasRetweetedWhenLoaded ? 1 : 0));
        }
    }

    private void updateFavoriteViews(Context context, boolean isFavorited) {
        boolean wasFavoritedWhenLoaded = displayTweet.favorited;

        if (isFavorited) {
            favoriteButton.setColorFilter(ContextCompat.getColor(context, R.color.favorite));
            textFavorites.setTextColor(ContextCompat.getColor(context, R.color.favorite));
            setFavoriteCounter(displayTweet.favoriteCount + (wasFavoritedWhenLoaded ? 0 : 1));
        }
        else {
            favoriteButton.setColorFilter(null);
            textFavorites.setTextColor(ContextCompat.getColor(context, android.R.color.secondary_text_light));
            setFavoriteCounter(displayTweet.favoriteCount - (wasFavoritedWhenLoaded ? 1 : 0));
        }
    }

    private void setFavoriteCounter(int favoriteCount) {
        textFavorites.setText(favoriteCount != 0 ?
                String.valueOf(favoriteCount) : "");
    }

    private void setRetweetCounter(int retweetCount) {
        textRetweets.setText(retweetCount != 0 ?
                String.valueOf(retweetCount) : "");
    }

    private Tweet getDisplayTweet() {
        if (getTweet().retweetedStatus != null)
            return getTweet().retweetedStatus;
        else if (getTweet().quotedStatus != null)
            return getTweet().quotedStatus;
        else
            return getTweet();
    }
}
