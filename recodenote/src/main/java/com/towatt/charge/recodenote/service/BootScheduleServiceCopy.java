package com.towatt.charge.recodenote.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.towatt.charge.recodenote.FolderActivity;
import com.towatt.charge.recodenote.R;
import com.towatt.charge.recodenote.bean.RecordBean;
import com.towatt.charge.recodenote.db.DBManager;

import java.util.List;

/**
 * user:HRobbie
 * Date:2016/8/24
 * Time:14:47
 * 邮箱：hwwyouxiang@163.com
 * Description:Page Function.
 */
public class BootScheduleServiceCopy extends Service {

    private DBManager dbManager;
    private final static int MESSAGE_SEND=0;
    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case MESSAGE_SEND:
                    long clockTime = msg.getData().getLong("clockTime");
                    sendBoardCase(clockTime);
                break;

            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        Notification notification = new Notification();
//        startForeground(200,notification);
        new Thread(){
            public void run(){
                Intent intent = new Intent(BootScheduleServiceCopy.this, FolderActivity.class);
                PendingIntent pendingIntent=PendingIntent.getActivity(BootScheduleServiceCopy.this, 200, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                Notification.Builder builder = new Notification.Builder(BootScheduleServiceCopy.this);
                builder.setSmallIcon(R.mipmap.ic_launcher).setContentTitle("语音记事本")
                        .setDefaults(Notification.DEFAULT_LIGHTS).setContentText("语音记事本在后台运行")
                        .setTicker("语音记事本提醒").setOngoing(true).setAutoCancel(true).setContentIntent(pendingIntent);
                Notification build = builder.build();
                Notification notification = new Notification();
                startForeground(200,build);
            }
        }.start();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("TAG", "bootScheduleservice onstartCommend");
        new Thread(){
            public void run(){
                dbManager = new DBManager(BootScheduleServiceCopy.this);
                List<RecordBean> recordList = dbManager.queryAllClock();
//        stopRemind();
                if(recordList.size()>=1) {
                    sendBoardCase(recordList.get(0).getClockTime());
                    for(int i=1;i<recordList.size();i++){
                        long duration =0;
                        duration = recordList.get(i).getClockTime() - recordList.get(i-1).getClockTime();
                        if(duration>=0){
                            long clockTime = recordList.get(i).getClockTime();
                            Message obtain = Message.obtain();
                            obtain.what=MESSAGE_SEND;
                            Bundle bundle = new Bundle();
                            bundle.putLong("clockTime",clockTime);
                            obtain.setData(bundle);
                            long delay = clockTime - System.currentTimeMillis()+40000;
                            handler.sendMessageDelayed(obtain,delay);
                        }
                    }
                }else{
                    stopRemind();
                }

            }
        }.start();

        return START_STICKY;
    }

    private void sendBoardCase(long time) {
        Intent intent1 = new Intent(this, NotificationService.class);
//        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent1, 0);
        PendingIntent pi=PendingIntent.getService(this,0,intent1,0);
        //得到AlarmManager实例
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);

        //**********注意！！下面的两个根据实际需求任选其一即可*********

        /**
         * 单次提醒
         * mCalendar.getTimeInMillis() 上面设置的13点25分的时间点毫秒值
         */
        am.set(AlarmManager.RTC_WAKEUP, time, pi);

        /**
         * 重复提醒
         * 第一个参数是警报类型；下面有介绍
         * 第二个参数网上说法不一，很多都是说的是延迟多少毫秒执行这个闹钟，但是我用的刷了MIUI的三星手机的实际效果是与单次提醒的参数一样，即设置的13点25分的时间点毫秒值
         * 第三个参数是重复周期，也就是下次提醒的间隔 毫秒值 我这里是一天后提醒
         */
//        am.setRepeating(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), (1000 * 20  ), pi);
    }


    /**
     * 关闭提醒
     */
    private void stopRemind(){

//        Intent intent = new Intent(this, AlarmReceiver.class);
//        PendingIntent pi = PendingIntent.getBroadcast(this, 0,
//                intent, 0);
        Intent intent1 = new Intent(this, NotificationService.class);
//        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent1, 0);
        PendingIntent pi=PendingIntent.getService(this,0,intent1,0);
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        //取消警报
        am.cancel(pi);
//        Toast.makeText(this, "关闭了提醒", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        Intent intent = new Intent(BootScheduleServiceCopy.this, BootScheduleService.class);
        startService(intent);
        super.onDestroy();
    }
}
