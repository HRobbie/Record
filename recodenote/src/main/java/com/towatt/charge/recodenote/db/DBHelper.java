package com.towatt.charge.recodenote.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库的创建和更新
 * Created by xwh on 2015/8/26.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static String DB_NAME = "note.db";
    private static int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * 当第一次创建数据库时回调该方法
     *
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table record_note(_id integer primary key autoincrement,name varchar,createDate integer,duration integer,createName varchar,clockTime integer,isAlert integer,storePosition varchar,whichFolder integer,folderName varchar)";
        db.execSQL(sql);

    }

    /**
     * 当数据库版本更新时回调该方法
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        String sql="alter table record_note add column isAlert integer";
//        db.execSQL(sql);
    }
}
