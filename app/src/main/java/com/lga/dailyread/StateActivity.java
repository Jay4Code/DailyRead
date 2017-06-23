package com.lga.dailyread;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class StateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_state);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // 解决在缓存数据后，打开app不显示标题的bug；
        // bug原因未知（能在parseJson方法中mToolbar.setTitle方法前读取到数据，
        // mToolbar.setTitle方法后通过mToolbar.getTitle方法也能取得数据）
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        //设置toolbar后调用setDisplayHomeAsUpEnabled
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
