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
import com.towatt.charge.recodenote.bean.FolderBean;
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
    private List<RecordBean> recordList;
    private DBManager dbManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

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
        dbManager = new DBManager(this);



        findReordList(startId);

        notifyOldClock(startId+300);

        return START_NOT_STICKY;
    }

    /**
     * 提醒所有未提醒的语音
     */
    private void notifyOldClock(final int startId) {
        new Thread(){
            public void run(){
                recordList = dbManager.queryOldClock1();
                if(recordList.size()>=1){
                    playMusic();
                }
                for(int i = 0; i< recordList.size(); i++){

                    Log.e("TAG", "Notificationservice nstartCommand");
                    RecordBean recordBean = recordList.get(i);
                    FolderBean folderBean = dbManager.queryFolderByWhich(recordBean.getWhichFolder());

                    Log.e("TAG", "onrecivie");
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    // 添加第二个通知
//            Notification notify1 = new Notification(R.drawable.advise2,
//                    "语音记事本提醒", System.currentTimeMillis());
//            notify1.flags|=Notification.FLAG_AUTO_CANCEL;	//打开应用程序后图标消失
//            notify1.defaults = Notification.DEFAULT_VIBRATE;	//设置默认声音、默认振动和默认闪光灯
//            notify1.flags |= Notification.FLAG_INSISTENT;
//            notify1.flags |= Notification.FLAG_ONGOING_EVENT;
                    Intent intent1=new Intent(NotificationService.this,MainActivity.class);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            Bundle bundle = new Bundle();
//            bundle.putString("name",recordBean.getName());
                    intent1.putExtra("createName",recordBean.getCreateName());
                    intent1.putExtra("whichFolder",recordBean.getWhichFolder());
                    intent1.putExtra("folderName",folderBean.getFolderName());
//            intent.putExtras(bundle);
                    PendingIntent pendingIntent=PendingIntent.getActivity(NotificationService.this, startId+i, intent1, PendingIntent.FLAG_CANCEL_CURRENT);
//            notify1.setLatestEventInfo(this, "通知",
//                    recordBean.getName()+"要收听了", pendingIntent);//设置事件信息
//            notificationManager.notify(startId+i, notify1); // 通过通知管理器发送通知


                    Notification.Builder builder = new Notification.Builder(NotificationService.this);
                    builder.setSmallIcon(R.drawable.advise2).setContentTitle("通知").setWhen(System.currentTimeMillis())
                            .setDefaults(Notification.DEFAULT_ALL).setContentIntent(pendingIntent).setContentText(recordBean.getName()+"要收听了")
                            .setTicker("语音记事本提醒").setOngoing(true).setAutoCancel(true);
                    Notification build = builder.build();

                    notificationManager.notify(startId+i, build);

                    dbManager.updateIsAlert(recordBean.getCreateName(),0);
                }
            }
        }.start();
    }
    private void playMusic() {
        new Thread(){
            public void run(){
                //创建media player
                mMediaPlayer = new MediaPlayer();
                //获取alarm uri
                Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                try {
                    mMediaPlayer.setDataSource(NotificationService.this, alert);
                    final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                    if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                        mMediaPlayer.setLooping(true);
                        mMediaPlayer.prepare();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
            }
        }.start();
    }

    private void findReordList(final int startId) {
        new Thread(){
            public void run(){
                recordList = dbManager.queryAllClock1();
                if(recordList.size()>=1){
                    playMusic();
                }
                for(int i = 0; i< recordList.size(); i++){

                    Log.e("TAG", "Notificationservice nstartCommand");
                    RecordBean recordBean = recordList.get(i);
                    FolderBean folderBean = dbManager.queryFolderByWhich(recordBean.getWhichFolder());

                    Log.e("TAG", "onrecivie");
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    // 添加第二个通知
//            Notification notify1 = new Notification(R.drawable.advise2,
//                    "语音记事本提醒", System.currentTimeMillis());
//            notify1.flags|=Notification.FLAG_AUTO_CANCEL;	//打开应用程序后图标消失
//            notify1.defaults = Notification.DEFAULT_VIBRATE;	//设置默认声音、默认振动和默认闪光灯
//            notify1.flags |= Notification.FLAG_INSISTENT;
//            notify1.flags |= Notification.FLAG_ONGOING_EVENT;
                    Intent intent1=new Intent(NotificationService.this,MainActivity.class);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            Bundle bundle = new Bundle();
//            bundle.putString("name",recordBean.getName());
                    intent1.putExtra("createName",recordBean.getCreateName());
                    intent1.putExtra("whichFolder",recordBean.getWhichFolder());
                    intent1.putExtra("folderName",folderBean.getFolderName());
//            intent.putExtras(bundle);
                    PendingIntent pendingIntent=PendingIntent.getActivity(NotificationService.this, startId+i, intent1, PendingIntent.FLAG_CANCEL_CURRENT);
//            notify1.setLatestEventInfo(this, "通知",
//                    recordBean.getName()+"要收听了", pendingIntent);//设置事件信息
//            notificationManager.notify(startId+i, notify1); // 通过通知管理器发送通知


                    Notification.Builder builder = new Notification.Builder(NotificationService.this);
                    builder.setSmallIcon(R.drawable.advise2).setContentTitle("通知").setWhen(System.currentTimeMillis())
                            .setDefaults(Notification.DEFAULT_ALL).setContentIntent(pendingIntent).setContentText(recordBean.getName()+"要收听了")
                            .setTicker("语音记事本提醒").setOngoing(true).setAutoCancel(true);
                    Notification build = builder.build();

                    notificationManager.notify(startId+i, build);

                    dbManager.updateIsAlert(recordBean.getCreateName(),0);
                }
            }
        }.start();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mMediaPlayer!=null){
            mMediaPlayer.release();
        }
    }
}
