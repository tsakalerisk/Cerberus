package com.example.cerberus.TwitterServices;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SearchHashtagsService {
    @GET("/i/search/typeahead.json")
    Call<ResponseBody> searchHashtags(@Query("q") String query);
}
