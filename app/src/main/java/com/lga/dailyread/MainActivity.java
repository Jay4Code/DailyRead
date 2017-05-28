package com.lga.dailyread;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.lga.cache.CacheConfig;
import com.lga.cache.CacheUtil;
import com.lga.security.AESEncryptor;
import com.lga.security.SecurityConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, RadioGroup.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener {

    private final String TAG = "MainActivity";
    private static final String FILE_NAME = "dailyread.xml";
    private static final String KEY_IS_RANDOM_URL = "is_random_url";
    private static final String KEY_CURR_URL = "curr_url";
    private static final String KEY_ARTICLE_SIZE_INDEX = "article_size_index";
    private static final String KEY_BG_COLOR_INDEX = "bg_color_index";

    private int[] ARTICLE_SIZES;
    private int[] BG_COLORS;

    private ArticleApi mApi;

    private SharedPreferences mPreferences;
    private String mCurrUrl;
    /**
     * 退出前保存的url是不是RANDOM_URL
     */
    private boolean isRandomUrl;
    private int mArticleSizeIndex;
    private int mBgColorIndex;

    private RequestQueue mQueue;

    private ViewGroup mLayoutArticle;
    private NavigationView mNavigationView;
    private ProgressBar mProgressBar;
    private Toolbar mToolbar;
    private FloatingActionButton mFab;
    private DrawerLayout mDrawer;
    private ScrollView mScrollView;
    private TextView mTvAuthor;
    private TextView mTvContent;
    private TextView mTvWords;
    private RadioGroup mRgSize;
    private RadioGroup mRgBg;
    private Switch mSwitcher;

    private Article mArticle;
    /**
     * 当前日期
     */
    private String mCurrDate;

    private Handler mHandler;

    private CacheUtil mCacheUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preprocess();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        // 解决在缓存数据后，打开app不显示标题的bug；
        // bug原因未知（能在parseJson方法中mToolbar.setTitle方法前读取到数据，
        // mToolbar.setTitle方法后通过mToolbar.getTitle方法也能取得数据）
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFab.setVisibility(View.GONE);
                loadData(mCurrUrl);
            }
        });

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        initView();

        loadData(mCurrUrl);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = mPreferences.edit();

        if (mArticle != null) {
            if (mCurrUrl.contains(ArticleApi.RANDOM)) {
                mCacheUtil.cacheObject(mCurrUrl, mArticle, JSON.toJSONString(mArticle));

                editor.putBoolean(KEY_IS_RANDOM_URL, true);
            }
        }

//        Log.e("kelly", "save url:" + mCurrUrl);

        String encode = AESEncryptor.encrypt(SecurityConfig.KEY, mCurrUrl);
//        Log.e("kelly", "加密");
//        Log.e("kelly", "密文：" + encode);
//        Log.e("kelly", "原文：" + mCurrUrl);
        editor.putString(KEY_CURR_URL, encode);
        editor.putInt(KEY_ARTICLE_SIZE_INDEX, mArticleSizeIndex);
        editor.putInt(KEY_BG_COLOR_INDEX, mBgColorIndex);
        editor.apply();
//        editor.commit();

        mCacheUtil.flushCache();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mQueue.cancelAll(TAG);

        mHandler.removeCallbacksAndMessages(null);
    }

    private void preprocess() {
        ARTICLE_SIZES = getResources().getIntArray(R.array.article_size);
        BG_COLORS = getResources().getIntArray(R.array.bg_articles);

        mApi = new ArticleApi();
        mCurrDate = getFormatDate();

        mPreferences = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        isRandomUrl = mPreferences.getBoolean(KEY_IS_RANDOM_URL, false);

        String encode = mPreferences.getString(KEY_CURR_URL, null);
        mCurrUrl = AESEncryptor.decrypt(
                SecurityConfig.KEY,
                encode
        );
//        Log.e("kelly", "解密");
//        Log.e("kelly", "密文：" + encode);
//        Log.e("kelly", "原文：" + mCurrUrl);
        if (mCurrUrl == null) {
            mCurrUrl = mApi.URL_OTHER + mCurrDate;
        }

        mArticleSizeIndex = mPreferences.getInt(KEY_ARTICLE_SIZE_INDEX, 1);
        mBgColorIndex = mPreferences.getInt(KEY_BG_COLOR_INDEX, 0);

        mQueue = Volley.newRequestQueue(this);

        mHandler = new Handler();

        mCacheUtil = new CacheUtil(this);
        mCacheUtil.openCache(
                mCacheUtil.setCacheDir(CacheConfig.CACHE_DIR),
                1,
                1,
                CacheConfig.MAX_SIZE
        );
    }

    private void initView() {
        mLayoutArticle = (ViewGroup) findViewById(R.id.layout_article);
        mLayoutArticle.setBackgroundColor(BG_COLORS[mBgColorIndex]);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mTvAuthor = (TextView) findViewById(R.id.tv_author);
        mTvContent = (TextView) findViewById(R.id.tv_content);
        mTvContent.setTextSize(ARTICLE_SIZES[mArticleSizeIndex]);
        mTvWords = (TextView) findViewById(R.id.tv_words);
    }

    private void loadData(String url) {
//        Log.e("kelly", "url:" + url);

        mProgressBar.setVisibility(View.VISIBLE);

        if (isRandomUrl) {                              // 上一次退出应用时的内容是RANDOM_URL则主动加载
            isRandomUrl = false;

            mArticle = (Article) mCacheUtil.getCachedObject(url, Article.class);
            if (mArticle != null) {
                updateUI(mArticle);
            } else {
                loadData(url, false);
            }
        } else if (url.contains(ArticleApi.RANDOM)) {   // 被动加载RANDOM_URL
            loadData(url, false);
        } else {                                        // 被动加载OTHER_URL
            mArticle = (Article) mCacheUtil.getCachedObject(url, Article.class);
            if (mArticle != null) {
                updateUI(mArticle);
            } else {
                loadData(url, true);
            }
        }
    }

    private void loadData(final String url, final boolean needCache) {
        JsonObjectRequest jsr = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObj) {
                        mArticle = parseJson(jsonObj);
                        if (mArticle == null) {
                            mProgressBar.setVisibility(View.GONE);
                            showError(R.string.net_busy);
                        } else {
                            if (needCache)
                                mCacheUtil.cacheObject(url, mArticle, JSON.toJSONString(mArticle));

                            updateUI(mArticle);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        mProgressBar.setVisibility(View.GONE);

                        if (mArticle == null) {
                            mFab.setVisibility(View.VISIBLE);
                        }

                        showError(R.string.net_busy);
                    }
                });
        jsr.setTag(TAG);
        mQueue.add(jsr);
    }

    private Article parseJson(JSONObject jsonObj) {
        Article article;
        try {
            article = new Article();
            JSONObject dataRoot = jsonObj.getJSONObject(mApi.DATA);
            article.author = dataRoot.getString(mApi.AUTHOR);
            article.title = dataRoot.getString(mApi.TITLE);
            article.digest = dataRoot.getString(mApi.DIGEST);
            article.content = dataRoot.getString(mApi.CONTENT);
            article.wc = dataRoot.getInt(mApi.WC);

            JSONObject data = dataRoot.getJSONObject(mApi.DATE);
            article.curr = data.getString(mApi.CURR);
            article.prev = data.getString(mApi.PREV);
            article.next = data.getString(mApi.NEXT);

            updateUI(article);
        } catch (JSONException e) {
            e.printStackTrace();
            article = null;
        }
        return article;
    }

    private void updateUI(Article article) {
        if (mCurrDate == null) {
            mCurrDate = article.curr;
        }
        if (mCurrDate.compareTo(article.curr) > 0) {
            mNavigationView.getMenu().getItem(3).setEnabled(true);
            mNavigationView.getMenu().getItem(4).setEnabled(true);
        } else {
            mNavigationView.getMenu().getItem(3).setEnabled(false);
            mNavigationView.getMenu().getItem(4).setEnabled(false);
        }

        mScrollView.scrollTo(article.readPosition, 0);

        mToolbar.setTitle(article.title);
        mTvAuthor.setText(article.author);
        mTvContent.setText(article.getFormatedContent());
        mTvWords.setText(getString(R.string.words, article.wc));

        mProgressBar.setVisibility(View.GONE);
    }

    private void showError(int errorId) {
        Snackbar.make(mLayoutArticle, getString(errorId), Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
    }

    private String getFormatDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();

        return sdf.format(calendar.getTime());
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        // Handle navigation view item clicks here.
        DrawerLayout.DrawerListener listener = new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                int id = item.getItemId();
                if (id == R.id.nav_read_settings) {
                    showReadSettingsDialog();
                } else {
                    if (mArticle == null) {
                        showError(R.string.net_busy);
                        return;
                    }

                    if (id == R.id.nav_random) {
                        mCurrUrl = mApi.URL_RANDOM;
                    } else if (id == R.id.nav_prev) {
                        mCurrUrl = mApi.URL_OTHER + mArticle.prev;
                    } else if (id == R.id.nav_today) {
                        mCurrUrl = mApi.URL_OTHER + getFormatDate();
                    } else if (id == R.id.nav_next) {
                        mCurrUrl = mApi.URL_OTHER + mArticle.next;
                    }
                    loadData(mCurrUrl);
                }
                mDrawer.removeDrawerListener(this);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        };
        mDrawer.addDrawerListener(listener);
        mDrawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void showReadSettingsDialog() {
        Dialog dialog = new Dialog(this, R.style.ActionSheetDialogStyle);

        View view = LayoutInflater.from(this).inflate(R.layout.view_dialog, null);
        // init view
        mRgSize = (RadioGroup) view.findViewById(R.id.rg_size);
        ((RadioButton) mRgSize.getChildAt(mArticleSizeIndex)).setChecked(true);
        mRgSize.setOnCheckedChangeListener(this);
        mRgBg = (RadioGroup) view.findViewById(R.id.rg_bg);
        // mBgColorIndex * 2，是因为中间还夹了一个Space
        ((RadioButton) mRgBg.getChildAt(mBgColorIndex * 2)).setChecked(true);
        mRgBg.setOnCheckedChangeListener(this);
        mSwitcher = (Switch) view.findViewById(R.id.switcher);
        mSwitcher.setOnCheckedChangeListener(this);

        dialog.setContentView(view);
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = getResources().getDisplayMetrics().widthPixels;
        view.setLayoutParams(params);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.show();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        int id = group.getId();
        if (id == R.id.rg_size) {
            switch (checkedId) {
                case R.id.rb_small:
                    mArticleSizeIndex = 0;
                    break;
                case R.id.rb_middle:
                    mArticleSizeIndex = 1;
                    break;
                case R.id.rb_big:
                    mArticleSizeIndex = 2;
                    break;
            }
            mTvContent.setTextSize(ARTICLE_SIZES[mArticleSizeIndex]);
        } else if (id == R.id.rg_bg) {
            switch (checkedId) {
                case R.id.rb_c1:
                    mBgColorIndex = 0;
                    break;
                case R.id.rb_c2:
                    mBgColorIndex = 1;
                    break;
                case R.id.rb_c3:
                    mBgColorIndex = 2;
                    break;
                case R.id.rb_c4:
                    mBgColorIndex = 3;
                    break;
            }
            mLayoutArticle.setBackgroundColor(BG_COLORS[mBgColorIndex]);
            updateArticleColor(getReverseColor(BG_COLORS[mBgColorIndex]));
        }
    }

    private int getReverseColor(int color) {
        return Color.rgb(
                255 - Color.red(color),
                255 - Color.green(color),
                255 - Color.blue(color)
        );
    }

    private void updateArticleColor(int color) {
        mTvAuthor.setTextColor(color);
        mTvContent.setTextColor(color);
        mTvWords.setTextColor(color);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }
}
