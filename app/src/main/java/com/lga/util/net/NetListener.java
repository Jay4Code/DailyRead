package com.lga.util.net;

import org.json.JSONObject;

/**
 * Created by Jay on 2017/5/29.
 */

public interface NetListener {

    void onResponse(JSONObject jsonObj);

    void onErrorResponse(int type, Object obj);
}
