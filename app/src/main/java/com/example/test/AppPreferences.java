package com.example.test;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by user on 06.10.2016.
 */
public class AppPreferences {

    public static final String PREF_NAME = "preferences";
    public static final String PREF_NEWS = "news";

    public static void setNewsModel(Context ctx, String modelString){
        SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(PREF_NEWS, modelString).commit();
    }

    public static String getNewsModel(Context ctx){
        SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(PREF_NEWS,"");
    }
}
