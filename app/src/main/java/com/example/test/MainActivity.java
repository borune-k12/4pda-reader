package com.example.test;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cantrowitz.rxbroadcast.RxBroadcast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class MainActivity extends AppCompatActivity implements AutoLoadingAdapter.OnActionListener{

    private NewsModel newsfeed;
    private PublishSubject<Boolean> subject = PublishSubject.create();

    RecyclerView list;
    WebView webview;
    private int mOrientation;
    SwipeRefreshLayout mRefresh;
    AutoLoadingAdapter adapter = new AutoLoadingAdapter(this);

    TextView selectNews;
    ProgressBar pageLoading;

    View contentLayout;
    TextView no_news_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mOrientation = getResources().getConfiguration().orientation;
        (getSupportActionBar()).hide();

        newsfeed = new NewsModel(this);
        list = (RecyclerView) findViewById(R.id.news_list);
        contentLayout = findViewById(R.id.content);
        no_news_text = (TextView) findViewById(R.id.no_news_text);

        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            webview = (WebView) findViewById(R.id.webview);
            webview.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    pageLoading.setVisibility(View.VISIBLE);
                    webview.setVisibility(View.INVISIBLE);
                    selectNews.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    pageLoading.setVisibility(View.INVISIBLE);
                    webview.setVisibility(View.VISIBLE);
                    selectNews.setVisibility(View.INVISIBLE);
                }
            });
            selectNews = (TextView) findViewById(R.id.selectNewsText);
            pageLoading = (ProgressBar) findViewById(R.id.progress);
        }

        list.setLayoutManager(new LinearLayoutManager(this));

        list.setAdapter(adapter);
        adapter.setListener(this);

        mRefresh = (SwipeRefreshLayout) findViewById(R.id.contentView);

        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               // System.out.println("onNext from refresh");
                subject.onNext(true);
            }
        });

        if (savedInstanceState != null) {
            int index = savedInstanceState.getInt("index");
            adapter.setSelectedIndex(index);
            if (index >= 0) {
                String url = newsfeed.getNews().get(index).url;
                String title = newsfeed.getNews().get(index).title;
                if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    Intent intent = new Intent(MainActivity.this, NewsItemActivity.class);
                    intent.putExtra("url", url);
                    intent.putExtra("title", title);
                    startActivityForResult(intent, 1);
                } else if (mOrientation == Configuration.ORIENTATION_LANDSCAPE)
                    webview.loadUrl(url);
            }

        }
        subject = PublishSubject.create();
        subscribeSubject();
        //System.out.println("onNext from start");
        if (newsfeed.getNews().size() == 0) {
            subject.onNext(true);
        } else {
            no_news_text.setVisibility(View.INVISIBLE);
            contentLayout.setVisibility(View.VISIBLE);
            adapter.setContents(newsfeed.getNews());

        }
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

        RxBroadcast.fromBroadcast(this, filter)
                .subscribe(new Action1<Intent>() {
                    @Override
                    public void call(Intent intent) {
                        NetworkInfo info = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);

                        if (info != null && info.isConnectedOrConnecting()) {
                            if (newsfeed.getNews().size() == 0) {
                                //subscribeSubject();
                                System.out.println("onNext from intent");
                                subject.onNext(false);
                            }
                        }

                    }
                });

    }

    private PublishSubject<Boolean> testErrorSubject = PublishSubject.create();

    private void subscribeSubject(){
            subject
                .debounce(1, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .flatMap(b -> newsfeed.getNewsfeed(b))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(e -> {
                    if (e instanceof UnknownHostException) {
                        Toast.makeText(MainActivity.this, R.string.no_inet, Toast.LENGTH_SHORT).show();
                        mRefresh.setRefreshing(false);
                    }

                })
                .retry()
                .subscribe(items -> {
                    if(items != null) {
                        no_news_text.setVisibility(View.INVISIBLE);
                        contentLayout.setVisibility(View.VISIBLE);
                        adapter.setContents(items);
                        mRefresh.setRefreshing(false);
                    }
                });
    }

    @Override
    public void onSaveInstanceState(Bundle saveBundle)
    {
        saveBundle.putInt("index",adapter.getSelectedIndex());
    }

    @Override
    public void onItemClicked(String url, String title) {
        if(mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            Intent intent = new Intent(MainActivity.this, NewsItemActivity.class);
            intent.putExtra("url", url);
            intent.putExtra("title",title);

            startActivityForResult(intent,1);
        }
        else {
            selectNews.setVisibility(View.INVISIBLE);
            pageLoading.setVisibility(View.VISIBLE);
            webview.loadUrl("http:"+url);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == 1 && resultCode == RESULT_OK) {
            if(data.getBooleanExtra("isClosedByUser",false))
                adapter.setSelectedIndex(-1);
        }
    }

    @Override
    public void needMoreData() {
        Log.d("tt","more");
        System.out.println("onNext from adapter");
        subject.onNext(false);
    }

    public int getOrientation(){
        return mOrientation;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webview.canGoBack()) {
                        webview.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }
}
