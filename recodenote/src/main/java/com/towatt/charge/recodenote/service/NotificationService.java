package com.towatt.charge.recodenote.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.towatt.charge.recodenote.MainActivity;
import com.towatt.charge.recodenote.R;
import com.towatt.charge.recodenote.bean.RecordBean;
import com.towatt.charge.recodenote.db.DBManager;

import java.io.IOException;
import java.util.List;

/**
 * user:HRobbie
 * Date:2016/8/24
 * Time:14:22
 * 邮箱：hwwyouxiang@163.com
 * Description:Page Function.
 */
public class NotificationService extends Service {
    int NOTIFYID_2 = 124;	//第二个通知的ID
    public static MediaPlayer mMediaPlayer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //获取alarm uri
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

//创建media player
        mMediaPlayer = new MediaPlayer();

        try {
            mMediaPlayer.setDataSource(this, alert);
            final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.setLooping(true);
                mMediaPlayer.prepare();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        MediaPlayer mp = new MediaPlayer();
//        try {
//            mp.setDataSource(this, RingtoneManager
//                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
//            mp.prepare();
//
//            mp.start();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        if(!mMediaPlayer.isPlaying()){
            mMediaPlayer.start();
        }
        new Thread(){
            public void run(){
                SystemClock.sleep(60000);
                if(mMediaPlayer!=null){
                    mMediaPlayer.stop();
                }

            }
        }.start();

        DBManager dbManager = new DBManager(this);
        List<RecordBean> recordList = dbManager.queryAllClock1();
        for(int i=0;i<recordList.size();i++){
            Log.e("TAG", "Notificationservice nstartCommand");
            RecordBean recordBean = recordList.get(i);
            Log.e("TAG", "onrecivie");
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            // 添加第二个通知
//            Notification notify1 = new Notification(R.drawable.advise2,
//                    "语音记事本提醒", System.currentTimeMillis());
//            notify1.flags|=Notification.FLAG_AUTO_CANCEL;	//打开应用程序后图标消失
//            notify1.defaults = Notification.DEFAULT_VIBRATE;	//设置默认声音、默认振动和默认闪光灯
//            notify1.flags |= Notification.FLAG_INSISTENT;
//            notify1.flags |= Notification.FLAG_ONGOING_EVENT;
            Intent intent1=new Intent(this,MainActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            Bundle bundle = new Bundle();
//            bundle.putString("name",recordBean.getName());
            intent1.putExtra("name",recordBean.getName());
//            intent.putExtras(bundle);
            PendingIntent pendingIntent=PendingIntent.getActivity(this, startId+i, intent1, PendingIntent.FLAG_CANCEL_CURRENT);
//            notify1.setLatestEventInfo(this, "通知",
//                    recordBean.getName()+"要收听了", pendingIntent);//设置事件信息
//            notificationManager.notify(startId+i, notify1); // 通过通知管理器发送通知


            Notification.Builder builder = new Notification.Builder(this);
            builder.setSmallIcon(R.drawable.advise2).setContentTitle("通知").setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_VIBRATE).setContentIntent(pendingIntent).setContentText(recordBean.getName()+"要收听了")
                    .setTicker("语音记事本提醒").setOngoing(true).setAutoCancel(true);
            Notification build = builder.build();
            notificationManager.notify(startId+i, build);


        }



        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
    }
}
