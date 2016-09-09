package com.towatt.charge.recodenote;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.towatt.charge.recodenote.service.MusicPlayerService;
import com.towatt.charge.recodenote.utils.CommentUtils;

public class PlayActivity extends Activity implements View.OnClickListener {
    private SeekBar sb_record;
    private TextView tv_start_time;
    private TextView tv_end_start;
    private ImageView btn_start;
    private ImageView btn_stop;

    private TextView tv_record_name;

//    MediaPlayer mMediaPlayer;


    private int position;
    /**
     * 服务的代理类
     */
    private IMusicPlayerService service;


    private ServiceConnection con = new ServiceConnection() {

        /**
         * 当Activity绑定服务成功的时候回调这个方法
         * @param name
         * @param ibinder
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder ibinder) {
            //拿到服务的引用
            service = IMusicPlayerService.Stub.asInterface(ibinder);

            try {
//                if(!notification){
//                    service.openAudio(position);
//                }else{
//
                service.openAudioFile(path);
//                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        /**
         * 当Activity和服务绑定断掉的时候回调
         * @param name
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    private final static int MESSAGE_POSITION=1;
    private Handler handler  = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case MESSAGE_POSITION:
//                   int currentPosition = mMediaPlayer.getCurrentPosition();
//                   tv_start_time.setText(currentPosition+"");
//                   sb_record.setProgress(currentPosition);
                    try {
                        int currentPosition = service.getCurrentPosition();
                        tv_start_time.setText(CommentUtils.SecondToHSM(currentPosition));
                        sb_record.setProgress(currentPosition);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    if(!isDestroyed){
                        removeMessages(MESSAGE_POSITION);
                        handler.sendEmptyMessageDelayed(MESSAGE_POSITION,100);
                    }
                    break;
            }

        }
    };
    private MyReceiver receiver;
    private String recordName;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //窗口对齐屏幕宽度
        Window win = this.getWindow();
        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //        lp.gravity = Gravity.TOP;//设置对话框置顶显示
        win.setAttributes(lp);

        setContentView(R.layout.activity_play);


        Intent intent = getIntent();
        position = intent.getIntExtra("position", 0);
        recordName = intent.getStringExtra("recordName");
        path = intent.getStringExtra("path");
        startAndBindService();
        initView();
        initData();

//        try {
//            service.openAudio(position);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
    }

    private void startAndBindService() {
        Intent intent  = new Intent(this,MusicPlayerService.class);
        bindService(intent, con, BIND_AUTO_CREATE);
        startService(intent);

    }
    private void initData() {
//        mMediaPlayer = MediaPlayer.create(this,R.raw.live);


//            sb_record.setMax(mMediaPlayer.getDuration());



//        tv_end_start.setText(mMediaPlayer.getDuration()+"");

        sb_record.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                tv_start_time.setText(progress+"");
//                mMediaPlayer.seekTo(progress);
                if(fromUser){
                    try {
                        service.seekTo(progress);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tv_record_name.setText(recordName);


        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicPlayerService.OPENAUDIOCOMPLETE);
        registerReceiver(receiver, filter);

    }

    private void initView() {
        sb_record= (SeekBar) findViewById(R.id.sb_record);
        tv_start_time= (TextView) findViewById(R.id.tv_start_time);
        tv_end_start= (TextView) findViewById(R.id.tv_end_start);
        btn_start= (ImageView) findViewById(R.id.btn_start);
        btn_stop= (ImageView) findViewById(R.id.btn_stop);

        tv_record_name = (TextView)findViewById(R.id.tv_record_name);
        btn_start.setOnClickListener(this);
        btn_stop.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_start:

//                if(mMediaPlayer.isPlaying()){
//                    btn_start.setImageResource(R.drawable.pause);
//                    mMediaPlayer.pause();
//                    handler.removeMessages(1);
//
//                }else{
//                    btn_start.setImageResource(R.drawable.play);
//                    try {
//                        mMediaPlayer.start();
//                        handler.sendEmptyMessage(1);
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
                try {
                    if(service.isPlaying()){
                        btn_start.setImageResource(R.drawable.pause);
                        service.pause();
                        handler.removeMessages(MESSAGE_POSITION);
                    }else{
                        btn_start.setImageResource(R.drawable.play);
                        service.start();
                        handler.removeMessages(MESSAGE_POSITION);
                        handler.sendEmptyMessage(MESSAGE_POSITION);
                    }

                    break;
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            case R.id.btn_stop:
//                mMediaPlayer.stop();
//                if(mMediaPlayer!=null){
//                    mMediaPlayer.reset();
//                    mMediaPlayer.release();
//                    handler.removeMessages(1);
//                }
                try {
                    if(service.isPlaying()){
                        service.pause();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                finish();
                break;
        }
    }

    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

//            try {
//                //得到当前播放的音频的名称，演唱者，
//                setViewData();
//                // 当前的音频的总长度
//                seekbar_music.setMax(service.getDuration());
//                setButtonInStatus();
//                //发消息
//                handler.sendEmptyMessage(PROGRESS);
//                showLyric();
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
            String flag = intent.getStringExtra("flag");

            try {
                int duration = service.getDuration();
                sb_record.setMax(duration);
                tv_end_start.setText(CommentUtils.SecondToHSM(duration));

                if("complete".equalsIgnoreCase(flag)){
                   btn_start.setImageResource(R.drawable.pause);
                }else if("prepare".equalsIgnoreCase(flag)){
                    if(!service.isPlaying()){
                        service.start();
                        btn_start.setImageResource(R.drawable.play);
                    }
                }



                handler.removeMessages(MESSAGE_POSITION);
                handler.sendEmptyMessage(MESSAGE_POSITION);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Activity是否销毁
     */
    private  boolean isDestroyed = false;
    @Override
    protected void onDestroy() {

        isDestroyed = true;
        if(con != null){
            unbindService(con);
            con= null;
        }

        if(receiver != null){
            unregisterReceiver(receiver);
            receiver = null;
        }
        handler.removeMessages(MESSAGE_POSITION);

        super.onDestroy();

    }
}
