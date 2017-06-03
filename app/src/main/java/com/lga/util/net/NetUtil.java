package com.lga.util.net;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.lga.dailyread.bean.Article;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

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
     *
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
    private Observable<Article> mObservable;

    public NetUtil(String baseUrl) {
        mService = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ArticleService.class);

    }

    public void getData(String category, String dev, String date, final NetListener.RetrofitListener listener) {
        mObservable = mService.getArticle(category, dev, date);
        mObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Article>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onNext(@NonNull Article article) {
                        listener.onResponse(article);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        listener.onFailure(e);
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    public void cancel() {
        if (mObservable != null) mObservable.unsubscribeOn(Schedulers.io());
    }

    // ----------------------
}
