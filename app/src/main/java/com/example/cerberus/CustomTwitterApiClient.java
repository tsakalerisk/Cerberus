package com.example.cerberus;

import com.example.cerberus.TwitterServices.TrendsService;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;

public class CustomTwitterApiClient extends TwitterApiClient {
    public CustomTwitterApiClient(TwitterSession session) {
        super(session);
    }

    public TrendsService getTrendsService() {
        return getService(TrendsService.class);
    }
}
