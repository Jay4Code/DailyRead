package com.lga.util.net;

import com.lga.dailyread.bean.Article;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Jay on 2017/5/30.
 */

public interface ArticleService {

    @GET("{category}")
    Observable<Article> getArticle(@Path("category") String category, @Query("dev") String dev, @Query("date") String date);
}
