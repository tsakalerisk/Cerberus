package com.example.cerberus.Modules.Responses.TwitterResponses;

public class SearchResponse {
    public Integer num_results;
    public Topic[] topics;

    public class Topic {
        public String topic;
    }
}
