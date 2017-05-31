package com.lga.dailyread;

/**
 * Created by Jay on 2017/5/26.
 */
public class ArticleApi {

    public final String BASE_URL = "https://interface.meiriyiwen.com/article/";
    // 可以使用URL_OTHER + 当天日期代替
//    public final String URL_CURR = "https://interface.meiriyiwen.com/article/today?dev=1";
    public final String URL_OTHER = "https://interface.meiriyiwen.com/article/day?dev=1&date=";
    public final String URL_RANDOM = "https://interface.meiriyiwen.com/article/random?dev=1";

    /**
     * URL_RANDOM的标识，URL_RANDOM所指向的内容不能缓存（url不唯一）
     */
    public static final String RANDOM = "random";
}
