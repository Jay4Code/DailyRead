package com.lga.util.net;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Jay on 2017/5/30.
 */

public interface ArticleService {

    @GET("day?dev=1&date=")
    Call<ResponseBody> getArticle(@Query("date") String date);

    @GET("random?dev=1")
    Call<ResponseBody> getRandomArticle();
}
