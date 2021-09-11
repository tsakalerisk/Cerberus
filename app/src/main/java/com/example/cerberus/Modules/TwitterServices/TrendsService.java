package com.example.cerberus.Modules.TwitterServices;

import com.example.cerberus.Modules.Responses.TwitterResponses.TrendResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TrendsService {
    @GET("/1.1/trends/place.json")
    Call<List<TrendResponse>> getTrends(@Query("id") int areaCode);
}
