package com.towatt.charge.recodenote.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * user:HRobbie
 * Date:2016/8/23
 * Time:13:25
 * 邮箱：hwwyouxiang@163.com
 * Description:Page Function.
 */
public class CommentUtils {
    /**
     * 时间戳转年月日时分秒
     * @param time
     * @return
     */
    public static String longToYMDHMS(long time){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(time));
    }

    /**
     * 时间戳转时分秒
     * @param time
     * @return
     */
    public static String longToHMS(long time){
        long second=0;
        long l = time / 1000;
        if(l<=0){
            second=1;
        }else{
            second=l;
        }
        return second+"";
    }

    /**
     * 弹出toast
     * @param context
     * @param msg
     */
    public static void showToast(Context context,String msg){
        Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
    }

    /**
     * 根据时间戳获得年份
     * @param time
     * @return
     */
    public static int getYear(long time){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        String year = sdf.format(new Date(time));
        return Integer.parseInt(year);
    }
    /**
     * 根据时间戳获得月份
     * @param time
     * @return
     */
    public static int getMonth(long time){
        SimpleDateFormat sdf = new SimpleDateFormat("MM");
        String year = sdf.format(new Date(time));
        return Integer.parseInt(year);
    }
    /**
     * 根据时间戳获得日
     * @param time
     * @return
     */
    public static int getDay(long time){
        SimpleDateFormat sdf = new SimpleDateFormat("dd");
        String year = sdf.format(new Date(time));
        return Integer.parseInt(year);
    }
    /**
     * 根据时间戳获得小时
     * @param time
     * @return
     */
    public static int getHour(long time){
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        String year = sdf.format(new Date(time));
        return Integer.parseInt(year);
    }
    /**
     * 根据时间戳获得分钟
     * @param time
     * @return
     */
    public static int getMinute(long time){
        SimpleDateFormat sdf = new SimpleDateFormat("mm");
        String year = sdf.format(new Date(time));
        return Integer.parseInt(year);
    }
    /**
     * 根据时间戳获得年月日
     * @param time
     * @return
     */
    public static String getYMD(long time){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String ymd = sdf.format(new Date(time));
        return ymd;
    }
    /**
     * 根据时间戳获得时分秒
     * @param time
     * @return
     */
    public static String getHSM(long time){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String hsm = sdf.format(new Date(time));
        return hsm;
    }

    public static String SecondToHSM(long time){
        long second = time / 1000;
        long minute = second / 60;
        long hour = minute / 60;
        String sSecond="00";
        String sMinute="00";
        String sHour="00";
        if(second<10){
            sSecond="0"+second;
        }else{
            sSecond=second+"";
        }
        if(hour<10){
            sHour="0"+hour;
        }else{
            sHour=hour+"";
        }
        if(minute<10){
            sMinute="0"+minute;
        }else{
            sMinute=minute+"";
        }
        return sHour+":"+sMinute+":"+sSecond;

    }


    /**
     * 发送post请求
     * @param <T>
     */
    public static <T> Callback.Cancelable Post(String url, Map<String,Object> map, Callback.CommonCallback<T> callback){
        RequestParams params=new RequestParams(url);


        if(null!=map){


            for(Map.Entry<String, Object> entry : map.entrySet()){
                params.addParameter(entry.getKey(), entry.getValue());
            }
        }
        Callback.Cancelable cancelable = x.http().post(params, callback);
        return cancelable;
    }

    /**
     * 发起网络get请求
     * url:请求的url
     * requestCallBack：回调
     */
    public static <T> Callback.Cancelable Get(String url, Map<String,String> map, Callback.CommonCallback<T> callback){
        RequestParams params=new RequestParams(url);
        if(null!=map){
            for(Map.Entry<String, String> entry : map.entrySet()){
                params.addQueryStringParameter(entry.getKey(), entry.getValue());
            }
        }
        Callback.Cancelable cancelable = x.http().get(params, callback);
        return cancelable;
    }

    public static Gson getGson(){
       return new Gson();
    }

    /**
     * 返回当前程序版本名
     */
    public static String getAppVersionName(Context context) {
        String versionName = "";
        String versionCode="";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
//            versionCode = pi.versionCode;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }
}
