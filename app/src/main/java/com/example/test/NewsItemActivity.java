package com.example.test;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebBackForwardList;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

/**
 * Created by user on 06.10.2016.
 */
public class NewsItemActivity extends AppCompatActivity {

    private WebView webview;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_news_item);


        ProgressBar pageLoading = (ProgressBar)findViewById(R.id.progress);
        webview = (WebView) findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        if(savedInstanceState != null){
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            intent.putExtra("isClosedByUser",false);

            intent.putExtra("history",savedInstanceState);
            finish();
        }

        webview.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon){
                pageLoading.setVisibility(View.VISIBLE);
                webview.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onPageFinished(WebView view, String url){
                pageLoading.setVisibility(View.INVISIBLE);
                webview.setVisibility(View.VISIBLE);
                actionBar.setTitle(webview.getTitle());
            }
        });

        Intent startIntent = getIntent();
        Bundle history = startIntent.getBundleExtra("history");
        if(history != null)
            webview.restoreState(history);
        else {
            String url = "http:" + startIntent.getStringExtra("url");
            if (!url.equals(""))
                webview.loadUrl(url);
        }

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            if (webview.canGoBack()) {
                webview.goBack();
            } else {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                intent.putExtra("isClosedByUser", true);
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle saveBundle)
    {
        WebBackForwardList history = webview.copyBackForwardList();
        webview.saveState(saveBundle);
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
