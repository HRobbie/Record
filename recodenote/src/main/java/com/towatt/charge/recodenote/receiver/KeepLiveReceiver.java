package com.towatt.charge.recodenote.receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.towatt.charge.recodenote.service.BootScheduleService;

/**
 * user:HRobbie
 * Date:2016/11/24
 * Time:13:49
 * 邮箱：hwwyouxiang@163.com
 * Description:和towattoa组成全家桶，相互拉起
 */
public class KeepLiveReceiver extends BroadcastReceiver{
    private Context context;
    private Intent intent;
    @Override
    public void onReceive(Context context1, Intent intent1) {
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
//                            Log.e("TAG", "AutoBoot NotificationService");
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
//        CommentUtils.showToast(context1,"语音记事本");
    }
}
