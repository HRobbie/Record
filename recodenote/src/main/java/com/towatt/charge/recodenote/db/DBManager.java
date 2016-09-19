package com.towatt.charge.recodenote.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.towatt.charge.recodenote.bean.FolderBean;
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
        String sql = "insert into record_note (_id,name,createDate,duration,createName,storePosition,whichFolder) values(null, ?, ?,?,?,?,?)";
        db.execSQL(sql, new Object[]{recordBean.getName(), recordBean.getCreateDate(),recordBean.getDuration(),recordBean.getCreateName(),recordBean.getStorePosition(),recordBean.getWhichFolder()});
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
            String whichFolder1 = cursor.getString(cursor.getColumnIndex("whichFolder"));
            RecordBean note = new RecordBean(_id, name, createDate, duration, createName,clockTime,isAlert,storePosition,whichFolder1);
            list.add(note);
        }
        cursor.close();
        return list;
    }
    /**
     * 根据文件夹查询其中的录音
     *
     * @return
     */
    public List<RecordBean> queryAllRecord(String whichFolder) {
        String strWhich=whichFolder+"";
        Log.e("TAG", "dbManager strWhich="+strWhich);
        String sql = "select * from record_note where whichFolder=? order by createDate desc";
        Cursor cursor = db.rawQuery(sql, new String[]{strWhich});
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
            String whichFolder1 = cursor.getString(cursor.getColumnIndex("whichFolder"));
            RecordBean note = new RecordBean(_id, name, createDate, duration, createName,clockTime,isAlert,storePosition,whichFolder1);

            list.add(note);

            Log.e("TAG", "dbmanager queryAllRecord");
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
                String whichFolder1 = cursor.getString(cursor.getColumnIndex("whichFolder"));
                RecordBean note = new RecordBean(_id, name, createDate, duration, createName,clockTime,isAlert,storePosition,whichFolder1);
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
                String whichFolder1 = cursor.getString(cursor.getColumnIndex("whichFolder"));
                RecordBean note = new RecordBean(_id, name, createDate, duration, createName,clockTime,isAlert,storePosition,whichFolder1);
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
            String whichFolder1 = cursor.getString(cursor.getColumnIndex("whichFolder"));
            note = new RecordBean(_id, name, createDate, duration, createName,clockTime,isAlert,storePosition,whichFolder1);
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

    /**
     * 添加一条数据
     *
     * @param folderBean
     */
    public void addFolder(FolderBean folderBean) {
        String sql = "insert into record_folder (_id,whichFolder,folderName,createDate) values(null, ?, ?,?)";
        db.execSQL(sql, new Object[]{folderBean.getWhichFolder(),folderBean.getFolderName(),folderBean.getCreateDate()});
        Log.e("TAG", "dbmanager db add");
    }


    /**
     * 删除若干条数据
     *
     * @param whichFolder
     */
    public void deleteFolder(String whichFolder) {
        String sql = "delete from record_folder where whichFolder = ?";
        db.execSQL(sql, new Object[]{whichFolder});
    }

    /**
     * 更新一条数据
     *
     * @param newName,createName
     */
    public void updateFolderName(String newName,String whichFolder) {
        String sql = "update record_folder set folderName = ? where whichFolder = ?";
        db.execSQL(sql, new Object[]{newName, whichFolder});
    }


    /**
     * 查询所有
     *
     * @return
     */
    public List<FolderBean> queryFolderAll() {
        String sql = "select * from record_folder order by _id desc";
        Cursor cursor = db.rawQuery(sql, null);
        List<FolderBean> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            int _id = cursor.getInt(cursor.getColumnIndex("_id"));
            String whichFolder = cursor.getString(cursor.getColumnIndex("whichFolder"));
            String folderName = cursor.getString(cursor.getColumnIndex("folderName"));
            long createDate = cursor.getLong(cursor.getColumnIndex("createDate"));
            FolderBean note = new FolderBean(_id, whichFolder, folderName, createDate);

            list.add(note);
        }
        cursor.close();
        return list;
    }
    /**
     * 查询所有
     *
     * @return
     */
    public FolderBean queryFolderByWhich(String whichFolder) {
        String sql = "select * from record_folder where whichFolder=?";
        Cursor cursor = db.rawQuery(sql, new String[]{whichFolder});
        while (cursor.moveToNext()) {
            int _id = cursor.getInt(cursor.getColumnIndex("_id"));
            String folderName = cursor.getString(cursor.getColumnIndex("folderName"));
            long createDate = cursor.getLong(cursor.getColumnIndex("createDate"));
            FolderBean note = new FolderBean(_id, whichFolder, folderName, createDate);

            return note;
        }
        return null;
    }
}
