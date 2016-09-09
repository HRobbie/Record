package com.towatt.charge.recodenote.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.towatt.charge.recodenote.service.BootScheduleService;

/**
 * user:HRobbie
 * Date:2016/8/24
 * Time:14:34
 * 邮箱：hwwyouxiang@163.com
 * Description:Page Function.
 */
public class BootAlarm extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("TAG", "onReceive");
        Intent intent1=new Intent(context, BootScheduleService.class);
        context.startService(intent1);
    }
}
