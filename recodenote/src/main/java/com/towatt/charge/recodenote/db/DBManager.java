package com.towatt.charge.recodenote.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.towatt.charge.recodenote.bean.RecordBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库的“增删改查”
 * Created by xwh on 2015/8/26.
 */
public class DBManager {

    private SQLiteDatabase db;
    private RecordBean note;

    public DBManager(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        db = dbHelper.getReadableDatabase();
    }

    /**
     * 添加一条数据
     *
     * @param recordBean
     */
    public void add(RecordBean recordBean) {
        String sql = "insert into record_note (_id,name,createDate,duration,createName,storePosition) values(null, ?, ?,?,?,?)";
        db.execSQL(sql, new Object[]{recordBean.getName(), recordBean.getCreateDate(),recordBean.getDuration(),recordBean.getCreateName(),recordBean.getStorePosition()});
        Log.e("TAG", "dbmanager db add");
    }

    /**
     * 删除若干条数据
     *
     * @param createName
     */
    public void delete(String createName) {
            String sql = "delete from record_note where createName = ?";
            db.execSQL(sql, new Object[]{createName});
    }
    /**
     * 删除所有数据
     *
     *
     */
    public void deleteAll() {
            String sql = "delete from record_note";
            db.execSQL(sql);
    }

    /**
     * 更新一条数据
     *
     * @param newName,createName
     */
    public void update(String newName,String createName) {
        String sql = "update record_note set name = ? where createName = ?";
        db.execSQL(sql, new Object[]{newName, createName});
    }

    /**
     * 修改提醒时间
     */
    public void updateClockTime(String createName,long clockTime){
        String sql="update record_note set clockTime =?  where createName = ?";
        db.execSQL(sql, new Object[]{clockTime, createName});
    }
    /**
     * 修改是否提醒
     */
    public void updateIsAlert(String createName,int isAlert){
        String sql="update record_note set isAlert =?  where createName = ?";
        db.execSQL(sql, new Object[]{isAlert, createName});
    }

    /**
     * 查询所有
     *
     * @return
     */
    public List<RecordBean> queryAll() {
        String sql = "select * from record_note order by createDate desc";
        Cursor cursor = db.rawQuery(sql, null);
        List<RecordBean> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            int _id = cursor.getInt(cursor.getColumnIndex("_id"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            long createDate = cursor.getLong(cursor.getColumnIndex("createDate"));
            long duration = cursor.getLong(cursor.getColumnIndex("duration"));
            String createName = cursor.getString(cursor.getColumnIndex("createName"));
            long clockTime = cursor.getLong(cursor.getColumnIndex("clockTime"));
            int isAlert = cursor.getInt(cursor.getColumnIndex("isAlert"));
            String storePosition = cursor.getString(cursor.getColumnIndex("storePosition"));
            RecordBean note = new RecordBean(_id, name, createDate, duration, createName,clockTime,isAlert,storePosition);

            list.add(note);
        }
        cursor.close();
        return list;
    }
    /**
     * 查询所有含有通知并且通知超过当前日期的类,且需要提醒
     *
     * @return
     */
    public List<RecordBean> queryAllClock() {
        String sql = "select * from record_note where clockTime!=0 and isAlert!=0 order by clockTime asc";
        Cursor cursor = db.rawQuery(sql, null);
        List<RecordBean> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            long clockTime = cursor.getLong(cursor.getColumnIndex("clockTime"));
            if(clockTime>System.currentTimeMillis()){
                int _id = cursor.getInt(cursor.getColumnIndex("_id"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                long createDate = cursor.getLong(cursor.getColumnIndex("createDate"));
                long duration = cursor.getLong(cursor.getColumnIndex("duration"));
                String createName = cursor.getString(cursor.getColumnIndex("createName"));
                int isAlert = cursor.getInt(cursor.getColumnIndex("isAlert"));
                String storePosition = cursor.getString(cursor.getColumnIndex("storePosition"));
                RecordBean note = new RecordBean(_id, name, createDate, duration, createName,clockTime,isAlert,storePosition);
                list.add(note);
            }
        }
        cursor.close();
        return list;
    }
    /**
     * 查询所有含有通知且通知在一分钟以内
     *
     * @return
     */
    public List<RecordBean> queryAllClock1() {
        String sql = "select * from record_note where clockTime!=0 and isAlert!=0 order by clockTime asc";
        Cursor cursor = db.rawQuery(sql, null);
        List<RecordBean> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            long clockTime = cursor.getLong(cursor.getColumnIndex("clockTime"));
            if(clockTime>System.currentTimeMillis()-60000&&clockTime<=System.currentTimeMillis()){
                int _id = cursor.getInt(cursor.getColumnIndex("_id"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                long createDate = cursor.getLong(cursor.getColumnIndex("createDate"));
                long duration = cursor.getLong(cursor.getColumnIndex("duration"));
                String createName = cursor.getString(cursor.getColumnIndex("createName"));
                int isAlert = cursor.getInt(cursor.getColumnIndex("isAlert"));
                String storePosition = cursor.getString(cursor.getColumnIndex("storePosition"));
                RecordBean note = new RecordBean(_id, name, createDate, duration, createName,clockTime,isAlert,storePosition);

                list.add(note);
            }
        }
        cursor.close();
        return list;
    }
    /**
     * 查询一条数据
     *
     * @return
     */
    public RecordBean queryByCreateName(String CreName) {
        String sql = "select * from record_note where createName = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{CreName});
        RecordBean note=null;
        while (cursor.moveToNext()) {
            int _id = cursor.getInt(cursor.getColumnIndex("_id"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            long createDate = cursor.getLong(cursor.getColumnIndex("createDate"));
            long duration = cursor.getLong(cursor.getColumnIndex("duration"));
            String createName = cursor.getString(cursor.getColumnIndex("createName"));
            long clockTime = cursor.getLong(cursor.getColumnIndex("clockTime"));
            int isAlert = cursor.getInt(cursor.getColumnIndex("isAlert"));
            String storePosition = cursor.getString(cursor.getColumnIndex("storePosition"));
            note = new RecordBean(_id, name, createDate, duration, createName,clockTime,isAlert,storePosition);

        }
        cursor.close();
        return note;
    }


    public void closeDB() {
        if (db != null && db.isOpen()) {
            db.close();
        }
        Log.e("TAG", "数据库关了！！！");
    }
}
