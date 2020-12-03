package com.example.cerberus;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class LogInActivity extends AppCompatActivity {

    public static final String TAG = "Twitter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        (new GetData()).execute();
    }

    class GetData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
                    .setOAuthConsumerKey(BuildConfig.TwitterApiKey)
                    .setOAuthConsumerSecret(BuildConfig.TwitterApiKeySecret)
                    .setOAuthAccessToken(BuildConfig.TwitterAccessToken)
                    .setOAuthAccessTokenSecret(BuildConfig.TwitterAccessTokenSecret);
            TwitterFactory tf = new TwitterFactory(cb.build());
            Twitter twitter = tf.getInstance();
            Query query = new Query("pizza");
            QueryResult result = null;
            try {
                result = twitter.search(query);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            for (twitter4j.Status status : result.getTweets()) {
                Log.d(TAG,"@" + status.getUser().getScreenName() + ":" + status.getText());
            }
            return null;
        }
    }
}