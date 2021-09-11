package com.example.cerberus.Modules;

import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

import com.example.cerberus.FeedActivity;
import com.example.cerberus.Modules.Adapters.SearchViewAdapter;
import com.example.cerberus.R;
import com.example.cerberus.SearchPostsActivity;
import com.example.cerberus.Modules.Responses.TwitterResponses.SearchResponse;
import com.example.cerberus.Modules.Responses.TwitterResponses.TrendResponse;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;

public class SearchManager {
    public static final int AREA_CODE_GREECE = 23424833;
    public static final int AREA_CODE_WORLD = 1;
    public static final String TAG = "TAG";
    public static final String TREND_NAME_LITERAL = "trendName";
    public static final String TWEET_NUM_LITERAL = "tweetNum";
    public static final int TREND_REQUEST_COOLDOWN_SECONDS = 300;
    private static final String RESULT_NAME_LITERAL = "ResultName";
    public static final String QUERY_LITERAL = "QUERY";

    private static SimpleCursorAdapter trendAdapter;
    private static SimpleCursorAdapter searchAdapter;
    private static long lastTrendCall = 0;

    public static void setUpSearchView(FeedActivity feedActivity) {
        SearchView searchView = feedActivity.searchView;

        //Remove bar under text
        int searchPlateId = feedActivity.getResources()
                .getIdentifier("android:id/search_plate", null, null);
        View searchPlate = feedActivity.findViewById(searchPlateId);
        searchPlate.setBackgroundResource(0);

        //Set auto-complete threshold to 0 characters
        int autoCompleteTextViewID = feedActivity.getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        AutoCompleteTextView searchAutoCompleteTextView = searchView.findViewById(autoCompleteTextViewID);
        searchAutoCompleteTextView.setThreshold(0);

        //Set trend adapter
        final String[] from = new String[] {TREND_NAME_LITERAL, TWEET_NUM_LITERAL};
        final int[] to = new int[] {R.id.trendName, R.id.tweetNum};
        trendAdapter = new SearchViewAdapter(feedActivity,
                R.layout.trend_layout,
                null,
                from,
                to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        searchView.setSuggestionsAdapter(trendAdapter);
        getTrends();

        //Set search adapter
        final String[] from2 = new String[] {RESULT_NAME_LITERAL};
        final int[] to2 = new int[] {R.id.resultName};
        searchAdapter = new SearchViewAdapter(feedActivity,
                R.layout.search_layout,
                null,
                from2,
                to2,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            boolean isQueryEmpty = searchView.getQuery().toString().isEmpty();
            boolean isOnCooldown = System.currentTimeMillis() - lastTrendCall <= TREND_REQUEST_COOLDOWN_SECONDS * 1000;
            if (hasFocus && isQueryEmpty && !isOnCooldown) {
                getTrends();
                lastTrendCall = System.currentTimeMillis();
            }
        });

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
                String txt = cursor.getString(1);
                searchView.setQuery(txt, true);
                return true;
            }

            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Intent intent = new Intent(feedActivity, SearchPostsActivity.class);
                intent.putExtra(QUERY_LITERAL, s);
                intent.putExtras(feedActivity.getIntent().getExtras());
                feedActivity.startActivity(intent);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.isEmpty()) {
                    searchView.setSuggestionsAdapter(trendAdapter);
                    getTrends();
                }
                else {
                    searchView.setSuggestionsAdapter(searchAdapter);
                    searchHashtags(s);
                }
                return false;
            }
        });
    }

    private static void searchHashtags(String query) {
        Call<SearchResponse> call = getTwitterClient().getSearchHashtagsService().searchHashtags(query);
        call.enqueue(new Callback<SearchResponse>() {
            @Override
            public void success(Result<SearchResponse> result) {
                final MatrixCursor c = new MatrixCursor(new String[]{ BaseColumns._ID, RESULT_NAME_LITERAL});
                SearchResponse.Topic[] topics = result.data.topics;
                for (int i = 0; i < topics.length; i++) {
                    c.addRow(new Object[] {i, topics[i].topic});
                }
                searchAdapter.changeCursor(c);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d(TAG, "Failed getting trends");
                exception.printStackTrace();
            }
        });
    }

    private static void getTrends() {
        Call<List<TrendResponse>> call = getTwitterClient().getTrendsService().getTrends(AREA_CODE_GREECE);
        call.enqueue(new Callback<List<TrendResponse>>() {
            @Override
            public void success(Result<List<TrendResponse>> result) {
                TrendResponse.Trend[] trends = result.data.get(0).trends;
                //Initialize suggestions
                final MatrixCursor c = new MatrixCursor(
                        new String[]{ BaseColumns._ID, TREND_NAME_LITERAL, TWEET_NUM_LITERAL});
                for (int i = 0; i < trends.length; i++) {
                    //See if tweet number is available
                    String tweetNum = trends[i].tweet_volume != null ?
                            String.format(Locale.getDefault(), "%,d", trends[i].tweet_volume) + " tweets": "";
                    c.addRow(new Object[] {i, trends[i].name, tweetNum});
                }
                trendAdapter.changeCursor(c);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d(TAG, "Failed getting trends");
                exception.printStackTrace();
            }
        });
    }

    private static CustomTwitterApiClient getTwitterClient() {
        return (CustomTwitterApiClient) TwitterCore.getInstance().getApiClient();
    }
}
