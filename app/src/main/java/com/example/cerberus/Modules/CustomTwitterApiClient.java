package com.example.cerberus.Modules;

import com.example.cerberus.TwitterServices.SearchHashtagsService;
import com.example.cerberus.TwitterServices.TrendsService;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.internal.network.OkHttpClientHelper;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CustomTwitterApiClient extends TwitterApiClient {
    public CustomTwitterApiClient(TwitterSession session) {
        super(session);
    }

    public TrendsService getTrendsService() {
        return getService(TrendsService.class);
    }

    public SearchHashtagsService getSearchHashtagsService() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://twitter.com/")
                .addConverterFactory(GsonConverterFactory.create()).build();
        return retrofit.create(SearchHashtagsService.class);
    }
}
