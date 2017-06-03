package com.lga.util.net;

import com.lga.dailyread.bean.Article;

import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Jay on 2017/5/31.
 */

public class NetListener {

    public interface VolleyListener {

        void onResponse(JSONObject jsonObj);

        void onErrorResponse(int type, Object obj);
    }

    public interface OkhttpListener {

        void onResponse(okhttp3.Call call, okhttp3.Response response);

        void onFailure(okhttp3.Call call, IOException e);
    }

    public interface RetrofitListener {

        void onResponse(Article article);

        void onFailure(Throwable e);
    }
}
