package com.example.test;

/**
 * Created by user on 06.10.2016.
 */
public class NewsItem {
    String title,
            url,
            pic_url,
            date;

    public NewsItem(String title, String url, String pic_url, String date){
        this.title = title;
        this.url = url;
        this.pic_url = pic_url;
        this.date = date;
    }
}
