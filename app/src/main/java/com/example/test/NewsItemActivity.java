package com.example.test;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

/**
 * Created by user on 06.10.2016.
 */
public class NewsItemActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_news_item);
        if(savedInstanceState != null){
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            intent.putExtra("isClosedByUser",false);
            finish();
        }


        ProgressBar pageLoading = (ProgressBar)findViewById(R.id.progress);
        WebView webview = (WebView) findViewById(R.id.webview);

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
            }
        });

        Intent startIntent = getIntent();

        String url = startIntent.getStringExtra("url");
        String title = startIntent.getStringExtra("title");
        if(!url.equals(""))
            webview.loadUrl(url);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        if(!title.equals(""))
            actionBar.setTitle(title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            intent.putExtra("isClosedByUser",true);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle saveBundle)
    {
        saveBundle.putBoolean("lala",true);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        intent.putExtra("isClosedByUser",true);
    }
}
