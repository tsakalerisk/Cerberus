package com.example.cerberus.Modules.TwitterServices;

import com.example.cerberus.Modules.Responses.TwitterResponses.SearchResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SearchHashtagsService {
    @GET("/i/search/typeahead.json")
    Call<SearchResponse> searchHashtags(@Query("q") String query);
}
