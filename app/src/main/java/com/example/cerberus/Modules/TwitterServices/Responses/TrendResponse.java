package com.example.cerberus.Modules.TwitterServices.Responses;

public class TrendResponse {
    public Trend[] trends;
    public String as_of;
    public String created_at;
    public Location[] locations;

    public class Trend {
        public String name;
        public String url;
        public String query;
        public Integer tweet_volume;
    }

    public class Location {
        public String name;
        public Integer woeid;
    }
}
