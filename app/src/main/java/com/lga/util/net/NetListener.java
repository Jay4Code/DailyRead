package com.lga.util.net;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;

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

        void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<okhttp3.ResponseBody> response);

        void onFailure(retrofit2.Call<ResponseBody> call, Throwable t);
    }
}
