package com.lga.util.net;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

/**
 * Created by Jay on 2017/5/29.
 * 封装网络请求库
 */
public class NetUtil {

    public static final int VOLLEY_GET = Request.Method.GET;
    private RequestQueue mQueue;

    public NetUtil(Context context) {
        mQueue = Volley.newRequestQueue(context);
    }

    public void cancelAll(String tag) {
        if (mQueue != null) mQueue.cancelAll(tag);
    }

    public void getData(int method, String tag, String url, final NetListener netListener) {
        JsonObjectRequest jsr = new JsonObjectRequest(
                method,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObj) {
                        netListener.onResponse(jsonObj);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        netListener.onErrorResponse(0, null);
                    }
                });
        jsr.setTag(tag);
        mQueue.add(jsr);
    }
}
