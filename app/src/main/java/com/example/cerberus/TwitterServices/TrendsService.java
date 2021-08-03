package com.example.cerberus.TwitterServices;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TrendsService {
    @GET("/1.1/trends/place.json")
    Call<ResponseBody> getTrends(@Query("id") int areaCode);
}
