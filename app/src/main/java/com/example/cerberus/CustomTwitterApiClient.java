package com.example.cerberus;

import com.example.cerberus.TwitterServices.SearchHashtagsService;
import com.example.cerberus.TwitterServices.TrendsService;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;

import retrofit2.Retrofit;

public class CustomTwitterApiClient extends TwitterApiClient {
    public CustomTwitterApiClient(TwitterSession session) {
        super(session);
    }

    public TrendsService getTrendsService() {
        return getService(TrendsService.class);
    }

    public SearchHashtagsService getSearchHashtagsService() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://twitter.com/").build();
        return retrofit.create(SearchHashtagsService.class);
    }
}
