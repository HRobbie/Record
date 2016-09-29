package com.towatt.charge.recodenote;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.hrobbie.timepickerlibrary.pickerview.TimePickerDialog;
import com.hrobbie.timepickerlibrary.pickerview.data.Type;
import com.hrobbie.timepickerlibrary.pickerview.listener.OnDateSetListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.towatt.charge.recodenote.bean.RecordBean;
import com.towatt.charge.recodenote.db.DBManager;
import com.towatt.charge.recodenote.receiver.AlarmReceiver;
import com.towatt.charge.recodenote.service.BootScheduleService;
import com.towatt.charge.recodenote.utils.CommentUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ClockActivity1 extends AppCompatActivity implements View.OnClickListener, OnDateSetListener {

    private Calendar mCalendar;

    private EditText et_clock_time;
    TimePickerDialog mDialogAll;


    private DBManager dbManager;
    private String createName;
    private RecordBean recordBean;

    private String folderName;
    private long selectTime;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock1);

        initView();

        initData();
    }

    private void initData() {
        Calendar calendar=Calendar.getInstance();

        dbManager = new DBManager(this);
        Intent intent = getIntent();
        createName = intent.getStringExtra("createName");
        folderName = intent.getStringExtra("folderName");
        recordBean = dbManager.queryByCreateName(createName);

        toolbar.setTitle(recordBean.getName());
        long clockTime = recordBean.getClockTime();
        if(clockTime!=0){
            if(recordBean.getIsAlert()!=0){
                et_clock_time.setTextColor(getResources().getColor(R.color.red));
                et_clock_time.setTextColor(getResources().getColor(R.color.red));
            }else{
                et_clock_time.setTextColor(getResources().getColor(R.color.gray));
                et_clock_time.setTextColor(getResources().getColor(R.color.gray));
            }
//            et_clock_time.setText(CommentUtils.longToYMDHMS(clockTime));
        }


        
    }

    private void initView() {
        et_clock_time = (EditText)findViewById(R.id.et_clock_time);


        toolbar = (Toolbar) findViewById(R.id.toolbar);

//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("设置提醒");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.drawable.arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        /**
         * 把toolbar的黑色回退键改成白色
         */
//        Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
//        upArrow.setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
//        getSupportActionBar().setHomeAsUpIndicator(upArrow);




        et_clock_time.setOnClickListener(this);


        long tenYears = 10L * 365 * 1000 * 60 * 60 * 24L;
        mDialogAll = new TimePickerDialog.Builder()
                .setCallBack(this)
                .setCancelStringId("取消")
                .setSureStringId("确定")
                .setTitleStringId("选择时间")
                .setYearText("年")
                .setMonthText("月")
                .setDayText("日")
                .setHourText("时")
                .setMinuteText("分")
                .setCyclic(false)
                .setMinMillseconds(System.currentTimeMillis())
                .setMaxMillseconds(System.currentTimeMillis() + tenYears)
                .setCurrentMillseconds(System.currentTimeMillis())
                .setThemeColor(getResources().getColor(R.color.timepicker_dialog_bg))
                .setType(Type.ALL)
                .setWheelItemTextNormalColor(getResources().getColor(R.color.timetimepicker_default_text_color))
                .setWheelItemTextSelectorColor(getResources().getColor(R.color.timepicker_toolbar_bg))
                .setWheelItemTextSize(12)
                .build();

        mDialogAll.show(getSupportFragmentManager(), "all");
    }



    public void save(View v) {
        String time = et_clock_time.getText().toString().trim();
        if(!time.isEmpty()){


            checkAlertPermission(time);

        }else{
            CommentUtils.showToast(this,"请选择日期和时间！");
        }


    }

    private void checkAlertPermission(final String time) {
        if (Dexter.isRequestOngoing()) {
            return;
        }
        Dexter.checkPermissions(new MultiplePermissionsListener() {
                                    @Override
                                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                                        if(!report.areAllPermissionsGranted()){
                                            CommentUtils.showToast(ClockActivity1.this,"权限不足，无法开启提醒！");
                                        }else{
                                            String dateTime = time;
                                            startRemind();
                                        }
                                    }

                                    @Override
                                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                                        token.continuePermissionRequest();
                                    }
                                }, android.Manifest.permission.VIBRATE,
                android.Manifest.permission.WAKE_LOCK);
    }
    /**
     * 开启提醒
     */
    private void startRemind(){

        //得到日历实例，主要是为了下面的获取时间
        mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(System.currentTimeMillis());

        //获取当前毫秒值
        long systemTime = System.currentTimeMillis();

        //是设置日历的时间，主要是让日历的年月日和当前同步
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        // 这里时区需要设置一下，不然可能个别手机会有8个小时的时间差
        mCalendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
//        //设置哪一年提醒
//        mCalendar.set(Calendar.YEAR,setYear);
//        //设置那一月提醒
//        mCalendar.set(Calendar.MONTH,setMonth);
//        //设置那一日提醒
//        mCalendar.set(Calendar.DAY_OF_MONTH,setDay);
//        //设置在几点提醒  设置的为13点
//        mCalendar.set(Calendar.HOUR_OF_DAY, setHour);
//        //设置在几分提醒  设置的为25分
//        mCalendar.set(Calendar.MINUTE, setMinute);
//        Log.e("TAG", "setMinute="+setMinute);
        //下面这两个看字面意思也知道
//        mCalendar.set(Calendar.SECOND, 0);
//        mCalendar.set(Calendar.MILLISECOND, 0);
//
//        //上面设置的就是13点25分的时间点
//
//        //获取上面设置的13点25分的毫秒值
//        long selectTime = mCalendar.getTimeInMillis();
//        Log.e("TAG", "selectime="+selectTime);
//        Log.e("TAG", "systemTime"+systemTime);

        // 如果当前时间大于设置的时间，那么就从第二天的设定时间开始
        if(systemTime > selectTime) {
//            mCalendar.add(Calendar.DAY_OF_MONTH, 1);
            CommentUtils.showToast(this,"提醒时间小于当前时间，请重新设置提醒时间！");
            return;
        }
        //修改数据库的设定时间
        dbManager.updateClockTime(createName, selectTime);
        dbManager.updateIsAlert(createName,1);
        Intent intent = new Intent(this, BootScheduleService.class);
        startService(intent);
//        if(MainActivity.mainActivity!=null){
//            MainActivity.mainActivity.finish();
//        }

        CommentUtils.showToast(this,"保存成功，将于"+CommentUtils.longToYMDHMS(selectTime)+"提醒");
        et_clock_time.setTextColor(getResources().getColor(R.color.red));
        Intent intent1 = new Intent(this, MainActivity.class);
        intent1.putExtra("whichFolder",recordBean.getWhichFolder());
        Log.e("TAG", "clockActivity folderName="+folderName);
        intent1.putExtra("folderName", folderName);
//        intent1.putExtra("createName", recordBean.getCreateName());
        startActivity(intent1);
        finish();

        //AlarmReceiver.class为广播接受者
//        Intent intent = new Intent(ClockActivity.this, AlarmReceiver.class);
//        intent.putExtra("name",recordBean.getName());
//        Log.e("TAG", "name="+recordBean.getName());
//        PendingIntent pi = PendingIntent.getBroadcast(ClockActivity.this, 0, intent, 0);
//        //得到AlarmManager实例
//        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
//
//        //**********注意！！下面的两个根据实际需求任选其一即可*********
//
//        /**
//         * 单次提醒
//         * mCalendar.getTimeInMillis() 上面设置的13点25分的时间点毫秒值
//         */
//        am.set(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), pi);

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

        Intent intent = new Intent(ClockActivity1.this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(ClockActivity1.this, 0,
                intent, 0);
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        //取消警报
        am.cancel(pi);
        Toast.makeText(this, "关闭了提醒", Toast.LENGTH_SHORT).show();

    }


    /**
     * 按住home键退出
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.et_clock_time:
                mDialogAll.show(getSupportFragmentManager(), "all");
                break;
        }
    }

    @Override
    public void onDateSet(TimePickerDialog timePickerView, long millseconds) {
        selectTime=millseconds;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = simpleDateFormat.format(new Date(millseconds));
        et_clock_time.setText(time);
    }
}
