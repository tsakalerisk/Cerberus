package com.example.cerberus;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

import com.twitter.sdk.android.core.TwitterCore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchManager {
    public static final int AREA_CODE_GREECE = 23424833;
    public static final int AREA_CODE_WORLD = 1;
    public static final String TWITTER_TAG = "Twitter";
    public static final String TREND_NAME_LITERAL = "trendName";
    public static final String TWEET_NUM_LITERAL = "tweetNum";
    public static final int TREND_REQUEST_COOLDOWN_SECONDS = 300;
    private static final String RESULT_NAME_LITERAL = "ResultName";

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
        trendAdapter = new TrendCursorAdapter(feedActivity,
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
        searchAdapter = new TrendCursorAdapter(feedActivity,
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
        Call<ResponseBody> call = getTwitterClient().getSearchHashtagsService().searchHashtags(query);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        JSONArray topics = new JSONObject(response.body().string()).getJSONArray("topics");
                        final MatrixCursor c = new MatrixCursor(new String[]{ BaseColumns._ID, RESULT_NAME_LITERAL});
                        for (int i = 0; i < topics.length(); i++) {
                            c.addRow(new Object[] {i, topics.getJSONObject(i).getString("topic")});
                        }
                        searchAdapter.changeCursor(c);
                    } else Log.d(TWITTER_TAG, response.errorBody().string());
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TWITTER_TAG, "Failed getting trends");
                t.printStackTrace();
            }
        });
    }

    private static void getTrends() {
        Call<ResponseBody> call = getTwitterClient().getTrendsService().getTrends(AREA_CODE_GREECE);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        String resultString = response.body().string();
                        JSONArray trends = new JSONArray(resultString).getJSONObject(0).getJSONArray("trends");

                        //Initialize suggestions
                        final MatrixCursor c = new MatrixCursor(new String[]{ BaseColumns._ID, TREND_NAME_LITERAL, TWEET_NUM_LITERAL});
                        for (int i = 0; i < trends.length(); i++) {
                            String trendName = trends.getJSONObject(i).getString("name");
                            //See if tweet number is available
                            Integer tweetNum;
                            if (!trends.getJSONObject(i).isNull("tweet_volume"))
                                tweetNum = (Integer) trends.getJSONObject(i).opt("tweet_volume");
                            else
                                tweetNum = null;
                            c.addRow(new Object[] {i, trendName, tweetNum != null ?  String.format(Locale.getDefault(), "%,d", tweetNum) + " tweets": ""});
                        }
                        trendAdapter.changeCursor(c);
                    }
                    else Log.d(TWITTER_TAG, response.errorBody().string());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TWITTER_TAG, "Failed getting trends");
                t.printStackTrace();
            }
        });
    }

    private static CustomTwitterApiClient getTwitterClient() {
        return (CustomTwitterApiClient) TwitterCore.getInstance().getApiClient();
    }
}
