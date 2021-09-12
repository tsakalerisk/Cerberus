package com.example.cerberus.Modules.CustomViews;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.utils.widget.ImageFilterView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cerberus.Modules.CustomLinearLayoutManager;
import com.example.cerberus.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.internal.UserUtils;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.TweetView;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

/*
This class customises the TweetView provided by TwitterKit
adding things like round user icons, retweet and favorite
counters, and reaction buttons.
 */
public class CustomTweetView extends TweetView {
    private ImageFilterView avatarView;
    private static final String TAG = "TAG";
    private TextView textReplies;
    private TextView textRetweets;
    private TextView textFavorites;
    private ImageButton replyButton;
    private ImageButton retweetButton;
    private ImageButton favoriteButton;

    private Tweet displayTweet;

    public static final int RETWEETED = R.id.retweeted;
    public static final int FAVORITED = R.id.favorited;
    private LinearLayout replyLayout;
    private EditText replyEditText;
    private ImageButton replySendButton;
    private FloatingActionButton scrollUpButton;
    private RecyclerView recyclerView;
    private CustomLinearLayoutManager recyclerViewLayoutManager;

    public static final boolean DISABLE_APP_PERMALINK = true;

    public CustomTweetView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTweetView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomTweetView(Context context, Tweet tweet, int styleResId) {
        super(context, tweet, styleResId);

        //Replaces the square user picture with a round one
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

        //Replaces the old basic action bar with the new detailed one
        ViewGroup oldActionBar = findViewById(R.id.tw__tweet_action_bar);
        View actionBar = LayoutInflater.from(context)
                .inflate(R.layout.twitter_action_bar_layout, null, false);
        actionBar.setId(oldActionBar.getId());
        actionBar.setLayoutParams(oldActionBar.getLayoutParams());

        parent.removeView(oldActionBar);
        parent.addView(actionBar);

        textReplies = actionBar.findViewById(R.id.text_replies);
        textRetweets = findViewById(R.id.text_retweets);
        textFavorites = findViewById(R.id.text_favorites);

        replyButton = findViewById(R.id.button_reply);
        retweetButton = findViewById(R.id.button_retweet);
        favoriteButton = findViewById(R.id.button_favorite);
    }

    @Override
    public void setTweet(Tweet tweet) {
        super.setTweet(tweet);
        if (DISABLE_APP_PERMALINK) {
            //Disables posts from opening in the app on-click
            setOnClickListener(null);
        }
        String url;
        if (tweet != null) {
            displayTweet = getDisplayTweet();

            if (displayTweet.user != null) {
                url = UserUtils.getProfileImageUrlHttps(displayTweet.user,
                        UserUtils.AvatarSize.REASONABLY_SMALL);
                Picasso.with(getContext()).load(url).into(avatarView);
            }

            setUpRetweetButton(getContext());
            setUpFavoriteButton(getContext());
            setUpReplyButton();
        }
    }

    private void setUpReplyButton() {
        //tHiS ObJeCt iS OnLy aVaIlAbLe wItH ThE PrEmIuM AnD EnTeRpRiSe tIeR PrOdUcTs.
        /*textReplies.setText(displayTweet.replyCount != 0 ?
                String.valueOf(displayTweet.replyCount) : "");*/

        replyButton.setOnClickListener(v -> {
            //Sets up the reply UI and locks scrolling while the user types a reply
            replyLayout = getRootView().findViewById(R.id.replyLayout);
            replyEditText = getRootView().findViewById(R.id.replyEditText);
            replySendButton = getRootView().findViewById(R.id.replySendButton);
            scrollUpButton = getRootView().findViewById(R.id.scrollUpButton);
            recyclerView = getRootView().findViewById(R.id.recyclerView);
            recyclerViewLayoutManager = (CustomLinearLayoutManager) recyclerView.getLayoutManager();

            int scrollUpButtonVisibility;
            replyLayout.setVisibility(VISIBLE);
            scrollUpButtonVisibility = scrollUpButton.getVisibility();
            scrollUpButton.setVisibility(GONE);
            if (recyclerViewLayoutManager != null) {
                recyclerViewLayoutManager.setScrollEnabled(false);
            }
            if (replyEditText.requestFocus()) {
                //Opens keyboard
                InputMethodManager inputMethodManager = (InputMethodManager) getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInputFromWindow(getApplicationWindowToken(),
                        InputMethodManager.SHOW_IMPLICIT, 0);
            }

            replySendButton.setOnClickListener(new ReplyButtonListener());

            //If the user closes the keyboard the reply UI is hidden and scrolling is unlocked
            KeyboardVisibilityEvent.setEventListener((Activity) getContext(), isOpen -> {
                if (!isOpen) {
                    if (replyEditText != null) {
                        if (replyEditText.getVisibility() == VISIBLE) {
                            replyEditText.setText("");
                            replyLayout.setVisibility(GONE);
                            scrollUpButton.setVisibility(scrollUpButtonVisibility);
                            if (recyclerViewLayoutManager != null) {
                                recyclerViewLayoutManager.setScrollEnabled(true);
                            }
                        }
                    }
                }
            });
        });
    }

    /*
    Posts a reply to the tweet displayed.
    Uses this endpoint: https://developer.twitter.com/en/docs/twitter-api/v1/tweets/post-and-engage/api-reference/post-statuses-update
     */
    private class ReplyButtonListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            //The reply text needs to be preceded by the original poster's @username.
            TwitterCore.getInstance().getApiClient().getStatusesService()
                    .update("@" + displayTweet.user.screenName + " "
                                    + replyEditText.getText().toString(), displayTweet.id,
                            null, null, null, null,
                            null, null, null)
                    .enqueue(new Callback<Tweet>() {
                        @Override
                        public void success(Result<Tweet> result) {
                            Log.d(TAG, "Replied to tweet successfully.");
                            Toast.makeText(getContext(), getResources().getString(R.string.reply_posted), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void failure(TwitterException exception) {
                            Log.d(TAG, "Failed to reply to tweet.");
                        }
                    });
            //The keyboard listener handles the closing of the reply views.
            hideKeyboard();
        }
    }

    private void setUpRetweetButton(Context context) {
        retweetButton.setTag(RETWEETED, displayTweet.retweeted);
        updateRetweetViews(context, displayTweet.retweeted);

        retweetButton.setOnClickListener(v -> {
            if (!(boolean) retweetButton.getTag(RETWEETED)) {
                //Retweet displayed tweet
                //Uses this endpoint: https://developer.twitter.com/en/docs/twitter-api/v1/tweets/post-and-engage/api-reference/post-statuses-retweet-id
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
            } else {
                //Un-retweet displayed tweet
                //Uses this endpoint: https://developer.twitter.com/en/docs/twitter-api/v1/tweets/post-and-engage/api-reference/post-statuses-unretweet-id
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
            if (!(boolean) favoriteButton.getTag(FAVORITED)) {
                //Favorite displayed tweet
                //Uses this endpoint: https://developer.twitter.com/en/docs/twitter-api/v1/tweets/post-and-engage/api-reference/post-favorites-create
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
            } else {
                //Un-favorite displayed tweet
                //Uses this endpoint: https://developer.twitter.com/en/docs/twitter-api/v1/tweets/post-and-engage/api-reference/post-favorites-destroy
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
        } else {
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
        } else {
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

    /*
    If the current tweet is a retweet then displayTweet will be
    the tweet being retweeted. Otherwise, they will be the same.
     */
    private Tweet getDisplayTweet() {
        if (getTweet().retweetedStatus != null)
            return getTweet().retweetedStatus;
        else
            return getTweet();
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = ((Activity) getContext()).getCurrentFocus();
        if (view == null) {
            view = new View(getContext());
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
