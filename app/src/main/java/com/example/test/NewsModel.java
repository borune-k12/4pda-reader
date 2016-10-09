package com.example.test;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by borune on 05.10.16.
 */
public class NewsModel {

    private static final String TAG = "NewsModel";
    private static final String url = "http://4pda.ru/";
    private static final int news_on_page = 30;

    private List<NewsItem> mNews;
    private Context mContext;

    private Subscriber< ? super List<NewsItem>> subscriber_;
    public Observable< List<NewsItem> > getNewsfeed(boolean refresh) {
        return Observable.create(new Observable.OnSubscribe<List<NewsItem>>() {
            @Override
            public void call(Subscriber<? super List<NewsItem>> subscriber) {
                try {
                    subscriber.onNext(parseNews(refresh));
                } catch (IOException ex){
                    subscriber.onError(ex);
                }
            }
        });
    }

    public List<NewsItem> parseNews(boolean refresh) throws IOException
    {
         {
            String page = url;
            if(!refresh)
                page += "page/"+String.valueOf(mNews.size() / news_on_page + 1);

            else mNews.clear();

            Log.d(TAG,"loading "+page);
            Document doc = Jsoup.connect(page).get();

            Elements articleElems = doc.select("article.post:has(h1.list-post-title)");

            for(Element article:articleElems)
            {
                Element post = article.select("h1.list-post-title").first();
                Element visual = article.select("div.visual").first();

                NewsItem item = new NewsItem(post.select("a").attr("title"),
                                             post.select("a").attr("href"),
                                             visual.select("img").attr("src"),
                                             visual.select("em.date").first().ownText());
                mNews.add(item);
            }
            AppPreferences.setNewsModel(mContext,serialize());

        }

        Log.d(TAG,"loaded "+mNews.size() + " news");
        return mNews;
    }

    public String serialize(){
        JSONArray newsArray = new JSONArray();

        for(NewsItem item : mNews){

            JSONObject jsonItem = new JSONObject();
            try {
                jsonItem.put("title",item.title);
                jsonItem.put("url",item.url);
                jsonItem.put("pic_url",item.pic_url);
                jsonItem.put("date",item.date);

                newsArray.put(jsonItem);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return newsArray.toString();
    }

    public void deserialize(String newsString){
        try {
            JSONArray newsArray = new JSONArray(newsString);
            for(int iter=0; iter<newsArray.length(); ++iter){
                JSONObject jsonItem = newsArray.getJSONObject(iter);

                mNews.add(new NewsItem(jsonItem.optString("title"),
                                       jsonItem.optString("url"),
                                       jsonItem.optString("pic_url"),
                                       jsonItem.optString("date")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int size(){
        return mNews.size();
    }

    public List<NewsItem> getNews(){
        return mNews;
    }

    public NewsModel (Context ctx){
        mContext = ctx;
        mNews = new ArrayList<>();
        deserialize(AppPreferences.getNewsModel(ctx));
    }

}
