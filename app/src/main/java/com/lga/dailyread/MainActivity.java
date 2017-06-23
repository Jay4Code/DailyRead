package com.lga.dailyread;

import android.app.Dialog;
import android.content.Intent;
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
import com.lga.dailyread.bean.Article;
import com.lga.util.cache.CacheConfig;
import com.lga.util.cache.CacheUtil;
import com.lga.util.date.DateUtil;
import com.lga.util.net.NetListener;
import com.lga.util.net.NetUtil;
import com.lga.util.security.AESEncryptor;
import com.lga.util.security.SecurityConfig;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;

import static com.lga.util.date.DateUtil.getFormatDate;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, RadioGroup.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener {

    private static final long EXIT_INTERVAL = 2000L;
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

    private NetUtil mNetUtil;

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

    private long mFirstTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preprocess();

        initView();

        loadData(mCurrUrl);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);

        // save data
        SharedPreferences.Editor editor = mPreferences.edit();
        if (mArticle != null) {
            if (mCurrUrl.contains(ArticleApi.RANDOM)) {
                mCacheUtil.cacheObject(mCurrUrl, mArticle, JSON.toJSONString(mArticle));

                editor.putBoolean(KEY_IS_RANDOM_URL, true);
            }
        }

        String encode = AESEncryptor.encrypt(SecurityConfig.KEY, mCurrUrl);
        editor.putString(KEY_CURR_URL, encode);
        editor.putInt(KEY_ARTICLE_SIZE_INDEX, mArticleSizeIndex);
        editor.putInt(KEY_BG_COLOR_INDEX, mBgColorIndex);
        editor.apply();

        mCacheUtil.flushCache();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNetUtil.cancelAll(TAG);

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

        mArticleSizeIndex = mPreferences.getInt(KEY_ARTICLE_SIZE_INDEX, 1);
        mBgColorIndex = mPreferences.getInt(KEY_BG_COLOR_INDEX, 0);

        mNetUtil = new NetUtil(this);

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

        mLayoutArticle = (ViewGroup) findViewById(R.id.layout_article);
        mLayoutArticle.setBackgroundColor(BG_COLORS[mBgColorIndex]);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mTvAuthor = (TextView) findViewById(R.id.tv_author);
        mTvContent = (TextView) findViewById(R.id.tv_content);
        mTvContent.setTextSize(ARTICLE_SIZES[mArticleSizeIndex]);
        mTvWords = (TextView) findViewById(R.id.tv_words);
    }

    /**
     * 加载数据
     * @param url url
     */
    private void loadData(String url) {
        mProgressBar.setVisibility(View.VISIBLE);

        if (url == null) {
            url = mApi.URL_OTHER + mCurrDate;
        }

        if (isRandomUrl) {           // 上一次退出应用时的内容是RANDOM_URL则主动加载
            isRandomUrl = false;

            loadDataWithCache(url, false);
        } else if (url.contains(ArticleApi.RANDOM)) {   // 被动加载RANDOM_URL
            loadData(url, false);
        } else {                                        // 被动加载OTHER_URL
            loadDataWithCache(url, true);
        }
    }

    /**
     * 加载数据。如果缓存中没有，从服务器中读取
     * @param url url
     * @param needCache 从服务器读取的数据是否需要缓存
     */
    private void loadDataWithCache(String url, boolean needCache) {
        Article article = (Article) mCacheUtil.getCachedObject(url, Article.class);
        if (article != null) {
            mCurrUrl = url;

            updateUI(article);
        } else {
            loadData(url, needCache);
        }
    }

    /**
     * 从服务器读取数据，并缓存
     * @param url url
     * @param needCache 从服务器读取的数据是否需要缓存
     */
    private void loadData(final String url, final boolean needCache) {
        mNetUtil.getData(NetUtil.VOLLEY_GET, TAG, url, new NetListener() {
            @Override
            public void onResponse(JSONObject jsonObj) {
                if(jsonObj == null) {
                    onErrorResponse(0, null);
                    return;
                }

                Article article = JSON.parseObject(jsonObj.toString(), Article.class);
                if (article == null) {
                    mProgressBar.setVisibility(View.GONE);
                    showAlter(R.string.net_busy);
                } else {
                    mCurrUrl = url;

                    if (needCache)
                        mCacheUtil.cacheObject(url, article, JSON.toJSONString(article));

                    updateUI(article);
                }
            }

            @Override
            public void onErrorResponse(int type, Object obj) {
                mProgressBar.setVisibility(View.GONE);

                mFab.setVisibility(mArticle == null ? View.VISIBLE : View.GONE);
                showAlter(R.string.net_busy);
            }
        });
    }

    private void updateUI(Article article) {
        mArticle = article;

        if (mCurrDate == null) {
            mCurrDate = article.getCurr();
        }
        if (mCurrDate.compareTo(article.getCurr()) > 0) {
            mNavigationView.getMenu().getItem(3).setEnabled(true);
            mNavigationView.getMenu().getItem(4).setEnabled(true);
        } else {
            mNavigationView.getMenu().getItem(3).setEnabled(false);
            mNavigationView.getMenu().getItem(4).setEnabled(false);
        }

        mScrollView.scrollTo(article.getReadPosition(), 0);

        mToolbar.setTitle(article.getTitle());
        mTvAuthor.setText(article.getAuthro());
        mTvContent.setText(article.getFormatedContent());
        mTvWords.setText(getString(R.string.words, article.getWc()));

        mProgressBar.setVisibility(View.GONE);
    }

    private void showAlter(int alterId) {
        Snackbar.make(mLayoutArticle, getString(alterId), Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            if (System.currentTimeMillis() - mFirstTime > EXIT_INTERVAL) {
                showAlter(R.string.exit);
                mFirstTime = System.currentTimeMillis();
            } else {
                /*
                // 返回桌面
                Intent home = new Intent(Intent.ACTION_MAIN);
                home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                home.addCategory(Intent.CATEGORY_HOME);
                startActivity(home);
                */
                super.onBackPressed();
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        // Handle navigation view item clicks here.
        DrawerLayout.DrawerListener listener = new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {}

            @Override
            public void onDrawerOpened(View drawerView) {}

            @Override
            public void onDrawerClosed(View drawerView) {
                int id = item.getItemId();
                if (id == R.id.nav_read_settings) {
                    showReadSettingsDialog();
                } else if (id == R.id.nav_state) {
                    startActivity(new Intent(MainActivity.this, StateActivity.class));
                } else {
                    if (mArticle == null) {
                        showAlter(R.string.net_busy);
                        return;
                    }

                    String url = null;
                    switch (id) {
                        case R.id.nav_random:
                            url = mApi.URL_RANDOM;
                            break;
                        case R.id.nav_prev:
                            url = mApi.URL_OTHER + mArticle.getPrev();
                            break;
                        case R.id.nav_today:
                            url = mApi.URL_OTHER + DateUtil.getFormatDate();
                            break;
                        case R.id.nav_next:
                            url = mApi.URL_OTHER + mArticle.getNext();
                            break;
                    }
                    loadData(url);
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
