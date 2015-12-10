package com.sinnus.zhihupaper.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sinnus on 2015/11/16.
 */
public class NewsWebDataDBHelper extends SQLiteOpenHelper{
    public NewsWebDataDBHelper(Context context, int version){
        super(context, "webCache.db", null, version);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists Cache (id INTEGER primary key autoincrement,newsId INTEGER unique,json text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
