package com.sinnus.zhihupaper.activity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.TextHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sinnus.zhihupaper.R;
import com.sinnus.zhihupaper.db.NewsWebDataDBHelper;
import com.sinnus.zhihupaper.model.News;
import com.sinnus.zhihupaper.model.Story;
import com.sinnus.zhihupaper.util.Constant;
import com.sinnus.zhihupaper.util.HttpUtil;
import com.sinnus.zhihupaper.view.RevealBackgroundView;

import org.apache.http.Header;

public class NewsDetailActivity extends AppCompatActivity {

    private WebView mWebView;
    private Story mStory;
    private ImageView mImageView;
    private AppBarLayout mAppBarLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NewsWebDataDBHelper newsWebDataDBHelper;
    private News news;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_latest_content);
        newsWebDataDBHelper = new NewsWebDataDBHelper(this, 1);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        mStory = (Story) getIntent().getSerializableExtra("story");
        mImageView = (ImageView) findViewById(R.id.iv);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout_detail);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);
        collapsingToolbarLayout.setTitle(mStory.getTitle());
        collapsingToolbarLayout.setBackgroundColor(getResources().getColor(R.color.color_primary));
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setDatabaseEnabled(true);
        mWebView.getSettings().setAppCacheEnabled(true);

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        setSwipeRefreshLayoutRefreshing(coordinatorLayout);
        loadData();

    }
    public void loadData() {
        if (HttpUtil.netWorkConnected(this)) {
            HttpUtil.get(Constant.CONTENT + mStory.getId(), new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    swipeRefreshLayout.setRefreshing(false);
                    swipeRefreshLayout.setEnabled(false);
                    Toast.makeText(NewsDetailActivity.this, "服务器故障", Toast.LENGTH_LONG).show();

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    SQLiteDatabase db = newsWebDataDBHelper.getWritableDatabase();
                    swipeRefreshLayout.setRefreshing(false);
                    swipeRefreshLayout.setEnabled(false);
                    responseString = responseString.replaceAll("'", "''");
                    db.execSQL("replace into Cache(newsId,json) values(" + mStory.getId() + ",'" + responseString + "')");
                    db.close();
                    parseJson(responseString);
                    //显示出来
                }
            });
        }
        else {
            SQLiteDatabase db = newsWebDataDBHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery("select * from Cache where newsId = " + mStory.getId(), null);
            if (cursor.moveToFirst()) {
                String json = cursor.getString(cursor.getColumnIndex("json"));
                parseJson(json);
            }
            cursor.close();
            db.close();
        }

    }
    public void parseJson(String data){
        Gson gson =new Gson();
        news = gson.fromJson(data, News.class);
        final ImageLoader imageloader = ImageLoader.getInstance();
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        imageloader.displayImage(news.getImage(), mImageView, options);
        System.out.println("image:"+news.getImage());

        String css = "<link rel=\"stylesheet\" href=\"file:///android_asset/css/news.css\" type=\"text/css\">";
        String html = "<html><head>" + css + "</head><body>" + news.getBody() + "</body></html>";
        html = html.replace("<div class=\"img-place-holder\">", "");
        mWebView.loadDataWithBaseURL("x-data://base", html, "text/html", "UTF-8", null);
    }

    public void setSwipeRefreshLayoutRefreshing(final View root){
        root.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        root.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        swipeRefreshLayout.setRefreshing(true);
                    }
                }
        );
    }
}
