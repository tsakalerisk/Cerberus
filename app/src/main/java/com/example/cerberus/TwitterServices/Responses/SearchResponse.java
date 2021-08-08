package com.example.cerberus.TwitterServices.Responses;

public class SearchResponse {
    public Integer num_results;
    public Topic[] topics;

    public class Topic {
        public String topic;
    }
}
