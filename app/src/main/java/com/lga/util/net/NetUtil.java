package com.lga.util.net;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.lga.dailyread.ArticleApi;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;

/**
 * Created by Jay on 2017/5/29.
 * 封装网络请求库
 */
public class NetUtil {

    // ------------volley------------
    public static final int VOLLEY_GET = Request.Method.GET;
    private RequestQueue mQueue;

    /**
     * volley专用
     * @param context
     */
    public NetUtil(Context context) {
        mQueue = Volley.newRequestQueue(context);
    }

    public void getData(int method, String tag, String url, final NetListener.VolleyListener listener) {
        JsonObjectRequest jsr = new JsonObjectRequest(
                method,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObj) {
                        listener.onResponse(jsonObj);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        listener.onErrorResponse(0, null);
                    }
                });
        jsr.setTag(tag);
        mQueue.add(jsr);
    }

    public void cancelAll(String tag) {
        if (mQueue != null) mQueue.cancelAll(tag);
    }

    // ------------okhttp------------
    private OkHttpClient mClient;
//    private okhttp3.Call mCall;

    /**
     * okhttp专用
     */
    public NetUtil() {
        mClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    public void getData(String url, final NetListener.OkhttpListener listener) {
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .build();
        okhttp3.Call call = mClient.newCall(request);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                listener.onResponse(call, response);
            }

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                listener.onFailure(call, e);
            }
        });
    }

    /*public void cancel() {
        if(mCall != null) mCall.cancel();
    }*/

    // ------------retrofit2------------
    private ArticleService mService;
    private retrofit2.Call<ResponseBody> mCall;

    public NetUtil(String baseUrl) {
        mService = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .build()
                .create(ArticleService.class);
    }

    public void getData(String url, final NetListener.RetrofitListener listener) {
        if (url.contains(ArticleApi.RANDOM)) {
            mCall = mService.getRandomArticle();
        } else {
            mCall = mService.getArticle(url.substring(url.length() - 8, url.length()));
        }

        mCall.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                listener.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                listener.onFailure(call, t);
            }
        });
    }

    public void cancel() {
        if (mCall != null) mCall.cancel();
    }

    // ----------------------
}
