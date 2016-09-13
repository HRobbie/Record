package com.towatt.charge.recodenote;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class ClockActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_select_time;
    private DatePicker dp_date;
    private TimePicker tp_time;
    private TextView tv_clock_date;
    private TextView tv_clock_time;
    private Calendar mCalendar;


    private int setYear;
    private int setMonth;
    private int setDay;
    private int setHour;
    private int setMinute;
    private int SetSecond;
    private DBManager dbManager;
    private String createName;
    private RecordBean recordBean;

    private TextView tv_reset;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock);

        initView();

        initData();
    }

    private void initData() {
        Calendar calendar=Calendar.getInstance();
        int year=calendar.get(Calendar.YEAR);
        int monthOfYear=calendar.get(Calendar.MONTH);
        int dayOfMonth=calendar.get(Calendar.DAY_OF_MONTH);
        Log.e("TAG", "monthOfYear="+monthOfYear);
        Log.e("TAG", "dayOfMonth="+dayOfMonth);

        dbManager = new DBManager(this);
        Intent intent = getIntent();
        createName = intent.getStringExtra("createName");
        recordBean = dbManager.queryByCreateName(createName);
        long clockTime = recordBean.getClockTime();
        if(clockTime!=0){
            year=CommentUtils.getYear(clockTime);
            monthOfYear=CommentUtils.getMonth(clockTime)-1;
            dayOfMonth=CommentUtils.getDay(clockTime);
            tp_time.setCurrentHour(CommentUtils.getHour(clockTime));
            tp_time.setCurrentMinute(CommentUtils.getMinute(clockTime));
            if(clockTime>System.currentTimeMillis()&&recordBean.getIsAlert()!=0){
                tv_clock_date.setTextColor(getResources().getColor(R.color.red));
                tv_clock_time.setTextColor(getResources().getColor(R.color.red));
            }else{
                tv_clock_time.setTextColor(getResources().getColor(R.color.gray));
                tv_clock_date.setTextColor(getResources().getColor(R.color.gray));
            }
            tv_clock_date.setText(CommentUtils.getYMD(clockTime));
            tv_clock_time.setText(CommentUtils.getHSM(clockTime));
        }

        dp_date.init(year, monthOfYear, dayOfMonth, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {
                setYear=i;
                setMonth=i1;
                setDay=+i2;

                String month=i1+"";
                String day=i2+"";
                if(i1+1<10){
                    month="0"+(i1+1);
                }
                if(i2<10){
                    day="0"+i2;
                }
                tv_clock_date.setText(i+"-"+month+"-"+day);
                dp_date.setVisibility(View.GONE);
                tp_time.setVisibility(View.VISIBLE);
            }
        });

        tp_time.setIs24HourView(true);
        tp_time.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                setHour=i;
                setMinute=i1;

                String hour=i+"";
                String second=i1+"";
                if(i<10){
                    hour="0"+i;
                }
                if(i1<10){
                    second="0"+i1;
                }
                tv_clock_time.setText(hour+":"+second);
            }
        });

        
    }

    private void initView() {
        tv_select_time = (TextView)findViewById(R.id.tv_select_time);
        dp_date = (DatePicker)findViewById(R.id.dp_date);
        tp_time = (TimePicker)findViewById(R.id.tp_time);
        tv_clock_date = (TextView)findViewById(R.id.tv_clock_date);
        tv_clock_time = (TextView)findViewById(R.id.tv_clock_time);
        tv_reset = (TextView)findViewById(R.id.tv_reset);

        Toolbar toolbar =  (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.arrow_back);
        toolbar.setTitle("设置提醒");
        toolbar.setTitleTextColor(Color.WHITE);

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



        tv_reset.setOnClickListener(this);
    }


    public void save(View v) {
        String date = tv_clock_date.getText().toString().trim();
        String time = tv_clock_time.getText().toString().trim();
        if(!date.isEmpty()&&!time.isEmpty()){


            checkAlertPermission(date,time);

        }else{
            CommentUtils.showToast(this,"请选择日期和时间！");
        }


    }

    private void checkAlertPermission(final String date, final String time) {
        if (Dexter.isRequestOngoing()) {
            return;
        }
        Dexter.checkPermissions(new MultiplePermissionsListener() {
                                    @Override
                                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                                        if(!report.areAllPermissionsGranted()){
                                            CommentUtils.showToast(ClockActivity.this,"权限不足，无法开启提醒！");
                                        }else{
                                            String dateTime = date + " " + time;
                                            startRemind();
                                            Log.e("TAG", "dateTime="+dateTime);
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
        //设置哪一年提醒
        mCalendar.set(Calendar.YEAR,setYear);
        //设置那一月提醒
        mCalendar.set(Calendar.MONTH,setMonth);
        //设置那一日提醒
        mCalendar.set(Calendar.DAY_OF_MONTH,setDay);
        //设置在几点提醒  设置的为13点
        mCalendar.set(Calendar.HOUR_OF_DAY, setHour);
        //设置在几分提醒  设置的为25分
        mCalendar.set(Calendar.MINUTE, setMinute);
        Log.e("TAG", "setMinute="+setMinute);
        //下面这两个看字面意思也知道
        mCalendar.set(Calendar.SECOND, 0);
        mCalendar.set(Calendar.MILLISECOND, 0);

        //上面设置的就是13点25分的时间点

        //获取上面设置的13点25分的毫秒值
        long selectTime = mCalendar.getTimeInMillis();
        Log.e("TAG", "selectime="+selectTime);
        Log.e("TAG", "systemTime"+systemTime);

        // 如果当前时间大于设置的时间，那么就从第二天的设定时间开始
        if(systemTime > selectTime) {
//            mCalendar.add(Calendar.DAY_OF_MONTH, 1);
            CommentUtils.showToast(this,"提醒时间小于当前时间，请重新设置提醒时间！");
            return;
        }
        //修改数据库的设定时间
        dbManager.updateClockTime(createName,selectTime);
        dbManager.updateIsAlert(createName,1);
        Intent intent = new Intent(this, BootScheduleService.class);
        startService(intent);
//        if(MainActivity.mainActivity!=null){
//            MainActivity.mainActivity.finish();
//        }

        CommentUtils.showToast(this,"保存成功，将于"+CommentUtils.longToYMDHMS(selectTime)+"提醒");
        tv_clock_time.setTextColor(getResources().getColor(R.color.red));
        tv_clock_date.setTextColor(getResources().getColor(R.color.red));
        Intent intent1 = new Intent(this, MainActivity.class);
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

        Intent intent = new Intent(ClockActivity.this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(ClockActivity.this, 0,
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
            case R.id.tv_reset:
                tp_time.setVisibility(View.GONE);
                dp_date.setVisibility(View.VISIBLE);
                break;
        }
    }
}
