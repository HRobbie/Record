package com.towatt.charge.recodenote.receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.towatt.charge.recodenote.application.SampleApplication;
import com.towatt.charge.recodenote.service.BootScheduleService;

/**
 * Created by HRobbie on 2016/9/25.
 */
public class AutoBoot extends BroadcastReceiver {
    private Context context;
    private Intent intent;
    @Override
    public void onReceive(Context context1, Intent intent1) {
        this.context=context1;
        this.intent=intent1;
        new Thread(){
            public void run(){
                boolean isServiceRunning = false;

                Log.e("TAG", "isServiceRunning");

                if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {

                    //检查Service状态

                    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                        if ("com.towatt.charge.recodenote.service.BootScheduleService".equals(service.service.getClassName()))

                        {
                            Log.e("TAG", "AutoBoot BootScheduleServiceService");
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



//        Toast.makeText(context,"自启动",Toast.LENGTH_LONG).show();
    }
}
