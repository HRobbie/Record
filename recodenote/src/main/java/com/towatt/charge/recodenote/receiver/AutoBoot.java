package com.towatt.charge.recodenote.receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.towatt.charge.recodenote.bean.RecordBean;
import com.towatt.charge.recodenote.db.DBManager;
import com.towatt.charge.recodenote.service.BootScheduleService;
import com.towatt.charge.recodenote.service.NotificationService;

import java.util.List;

/**
 * Created by HRobbie on 2016/9/25.
 */
public class AutoBoot extends BroadcastReceiver {
    private Context context;
    private Intent intent;
    @Override
    public void onReceive(final Context context1, Intent intent1) {
        this.context=context1;
        this.intent=intent1;
        new Thread(){
            public void run(){
                boolean isServiceRunning = false;

//                Log.e("TAG", "isServiceRunning");

                if (intent.getAction().equals(Intent.ACTION_TIME_TICK)||Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {

                    //检查Service状态

                    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                        if ("com.towatt.charge.recodenote.service.BootScheduleService".equals(service.service.getClassName())||"com.towatt.charge.recodenote.service.BootScheduleServiceCopy".equals(service.service.getClassName()))

                        {
//                            Log.e("TAG", "AutoBoot BootScheduleServiceService");
                            isServiceRunning = true;
                        }

                    }
                    if (!isServiceRunning) {
                        Intent i = new Intent(context, BootScheduleService.class);
                        context.startService(i);
                    }
                }

            }
        }.start();

        new Thread(){
            public void run(){
                if (intent.getAction().equals(Intent.ACTION_TIME_TICK)||Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                    DBManager dbManager = new DBManager(context1);
                    List<RecordBean> recordBeen = dbManager.queryOldClock1();
                    if (recordBeen.size() >= 1) {
                        Intent i = new Intent(context, NotificationService.class);
                        context.startService(i);
                    }
                }
            }
        }.start();

//        Toast.makeText(context,"自启动",Toast.LENGTH_LONG).show();

        notifyOA();
    }

    private void notifyOA(){
        //应用1，发送拉起服务的广播
        Intent intent = new Intent();
        intent.setAction("com.action.keepLive");
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        context.sendBroadcast(intent);
    }
}
