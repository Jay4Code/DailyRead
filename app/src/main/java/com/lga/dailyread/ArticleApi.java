package com.lga.dailyread;

/**
 * Created by Jay on 2017/5/26.
 * 每日一文API：https://github.com/jokermonn/-Api/blob/master/OneArticle.md
 */
public class ArticleApi {

    // 可以使用URL_OTHER + 当天日期代替
//    public final String URL_CURR = "https://interface.meiriyiwen.com/article/today?dev=1";
    public final String URL_OTHER = "https://interface.meiriyiwen.com/article/day?dev=1&date=";
    public final String URL_RANDOM = "https://interface.meiriyiwen.com/article/random?dev=1";

    public final String DATA = "data";
    public final String AUTHOR = "author";
    public final String TITLE = "title";
    public final String DIGEST = "digest";
    public final String CONTENT = "content";
    public final String WC = "wc";
    public final String DATE = "date";
    public final String CURR = "curr";
    public final String PREV = "prev";
    public final String NEXT = "next";

    /**
     * URL_RANDOM的标识，URL_RANDOM所指向的内容不能缓存（url不唯一）
     */
    public static final String RANDOM = "random";
}
