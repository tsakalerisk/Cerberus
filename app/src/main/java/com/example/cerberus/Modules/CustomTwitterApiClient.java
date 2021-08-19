package com.example.cerberus.Modules;

import com.example.cerberus.Modules.TwitterServices.SearchHashtagsService;
import com.example.cerberus.Modules.TwitterServices.TrendsService;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;

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

    /*public CustomTimelineService getCustomTimelineService() {
        Gson gson = new GsonBuilder().registerTypeAdapter(CustomTweet.class, new CustomTweet.CustomTweetDeserializer()).create();
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://api.twitter.com/")
                .client(OkHttpClientHelper.getOkHttpClient(
                        TwitterCore.getInstance().getSessionManager().getActiveSession(),
                        TwitterCore.getInstance().getAuthConfig()))
                .addConverterFactory(GsonConverterFactory.create(gson)).build();
        return retrofit.create(CustomTimelineService.class);
        //return getService(CustomTimelineService.class);
    }*/
}
