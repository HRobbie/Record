package com.towatt.charge.recodenote.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.towatt.charge.recodenote.service.NotificationService;

/**
 * user:HRobbie
 * Date:2016/8/23
 * Time:14:05
 * 邮箱：hwwyouxiang@163.com
 * Description:Page Function.
 */
public class AlarmReceiver extends BroadcastReceiver {
//    int NOTIFYID_2 = 124;	//第二个通知的ID
    @Override
    public void onReceive(Context context, Intent intent) {
//        String name = intent.getStringExtra("name");
//        Log.e("TAG", "onrecivie");
//        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
//        // 添加第二个通知
//        Notification notify1 = new Notification(R.drawable.advise2,
//                "第二个通知", System.currentTimeMillis());
//        notify1.flags|=Notification.FLAG_AUTO_CANCEL;	//打开应用程序后图标消失
//        Intent intent1=new Intent(context,MainActivity.class);
//        PendingIntent pendingIntent=PendingIntent.getActivity(context, 0, intent1, 0);
//        notify1.setLatestEventInfo(context, name+"通知",
//                "查看详细内容", pendingIntent);//设置事件信息
//        notificationManager.notify(NOTIFYID_2, notify1); // 通过通知管理器发送通知

//        Intent intent1 = new Intent(context, MainActivity.class);
//        context.startActivity(intent1);

        Log.e("TAG", "alartReceiver");
        Intent intent1=new Intent(context, NotificationService.class);
        context.startService(intent1);
    }
}
