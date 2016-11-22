package com.towatt.charge.recodenote;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.towatt.charge.recodenote.adapter.MenuAdapter;
import com.towatt.charge.recodenote.bean.RecordBean;
import com.towatt.charge.recodenote.db.DBManager;
import com.towatt.charge.recodenote.listener.OnItemClickListener;
import com.towatt.charge.recodenote.manager.MediaManager;
import com.towatt.charge.recodenote.receiver.AlarmReceiver;
import com.towatt.charge.recodenote.service.BootScheduleService;
import com.towatt.charge.recodenote.service.NotificationService;
import com.towatt.charge.recodenote.utils.CommentUtils;
import com.yanzhenjie.recyclerview.swipe.Closeable;
import com.yanzhenjie.recyclerview.swipe.OnSwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class  MainActivity extends AppCompatActivity implements View.OnClickListener  {

    private SwipeMenuRecyclerView lv_record;
//    private Button start_record;
//    private Button end_record;
    private File mRecAudioFile;        // 录制的音频文件
    private File mRecAudioPath;        // 录制的音频文件路徑
    private MediaRecorder mMediaRecorder;// MediaRecorder对象
    private String strTempFile = "record";// 零时文件的前缀
    private List<RecordBean> mMusicList = new ArrayList<RecordBean>();// 录音文件列表
    private List<String> mRecordList = new ArrayList<String>();// 录音文件列表

    private int fileCount=0;//新录音（数字）的数字

    private DBManager dbManager;
    private Calendar mCalendar;
    private View mAnimView;//播放声音的动画

//    private LinearLayout ll_voice;
    private Toolbar mToolbar;

    private boolean cbVisibility;//checkbox是否可见

    private LinearLayout ll_batch;

    private Button btn_cancel;
    private Button btn_delete;


//    private TextView tv_record_finish;
    private FloatingActionButton fab_play;
    private boolean inRecording;//是不是在录音中


    private Chronometer chronometer;
    private int selectedPosition;

    /**记录需要合成的几段amr语音文件**/
    private ArrayList<String> list=new ArrayList<String>();;
    private long rangeTime;//上一次暂停经历的时间

    private long sumRange=rangeTime;


    private Toolbar toolbar;

    private RelativeLayout rl_record;
    private MultiplePermissionsListener allPermissionsListener;
    private String whichFolder;
    private View lastAnimView;
    private String createName;
    private String folderName;
    private MenuAdapter menuAdapter;

    private Activity mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collapsing_demo);

        mContext=this;
        initView();

        initData();


    }

    private void initData() {

        Intent intent = getIntent();
        createName = intent.getStringExtra("createName");
        folderName = intent.getStringExtra("folderName");
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED))// 手机有SD卡的情况
        {
            // 在这里我们创建一个文件，用于保存录制内容
            mRecAudioPath = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/data/files/");
        } else// 手机无SD卡的情况
        {
            mRecAudioPath = this.getCacheDir();
        }
        toolbar.setTitle(folderName);
        whichFolder = intent.getStringExtra("whichFolder");

        Log.e("TAG", "MainActivity whichFolder="+whichFolder);
//        startRemind();

        stopNotificationMusic();

        checkStorePromission();

        dbManager=new DBManager(this);


//        musicList();

        registerForContextMenu(lv_record);






        int hour = (int) ((SystemClock.elapsedRealtime() - chronometer.getBase()) / 1000 / 60);
        chronometer.setFormat("0"+String.valueOf(hour)+":%s");
        chronometer.setBase(SystemClock.elapsedRealtime());//计时器清零


    }

    private void checkStorePromission() {
        Dexter.checkPermission(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                musicList();// 更新所有录音文件到List中
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void initView() {
        lv_record = (SwipeMenuRecyclerView)findViewById(R.id.lv_record);
//        start_record = (Button)findViewById(R.id.start_record);
//        end_record = (Button)findViewById(R.id.end_record);
//
//        start_record.setOnClickListener(this);
//        end_record.setOnClickListener(this);
//        end_record.setEnabled(false);

//        lv_record.setOnItemLongClickListener(this);

//        ll_voice = (LinearLayout)findViewById(R.id.ll_voice);

        ll_batch = (LinearLayout)findViewById(R.id.ll_batch);

        btn_delete = (Button)findViewById(R.id.btn_delete);

        btn_cancel = (Button)findViewById(R.id.btn_cancel);

//        tv_record_finish = (TextView)findViewById(R.id.tv_record_finish);
        fab_play = (FloatingActionButton) findViewById(R.id.fab_play);


        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("语音文件");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.drawable.arrow_back);
//        mCollapsingToolbarLayout.setCollapsedTitleTextColor(R.color.white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        chronometer = (Chronometer)findViewById(R.id.chronometer);
        btn_cancel.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
//        tv_record_finish.setOnClickListener(this);

        fab_play.setOnClickListener(this);


        lv_record.setLayoutManager(new LinearLayoutManager(this));

        rl_record = (RelativeLayout)findViewById(R.id.rl_record);


        lv_record.setItemAnimator(new DefaultItemAnimator());

        menuAdapter = new MenuAdapter(this, mMusicList);

//        lv_record.setAdapter(adapter);
        // 为SwipeRecyclerView的Item创建菜单就两句话，不错就是这么简单：
        // 设置菜单创建器。
        lv_record.setSwipeMenuCreator(swipeMenuCreator);
        // 设置菜单Item点击监听。
        lv_record.setSwipeMenuItemClickListener(menuItemClickListener);
        lv_record.setAdapter(menuAdapter);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
//        mToolbar.inflateMenu(R.menu.zhihu_toolbar_menu);

//        mToolbar.setNavigationIcon(R.mipmap.white_record);

//        mToolbar.setTitle("语音记事本");
//        mToolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        setSupportActionBar(mToolbar);
//        toolbar.setNavigationIcon(R.drawable.arrow_back);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setSupportActionBar(toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int menuItemId = item.getItemId();
//                if (menuItemId == R.id.action_search) {
//                    Toast.makeText(MainActivity.this, R.string.menu_search, Toast.LENGTH_SHORT).show();
//
//                } else if (menuItemId == R.id.action_notification) {
//                    Toast.makeText(MainActivity.this, R.string.menu_notifications, Toast.LENGTH_SHORT).show();

//               if (menuItemId == R.id.action_settings) {
//                    Toast.makeText(MainActivity.this, R.string.menu_settings, Toast.LENGTH_SHORT).show();
//                        finish();
//                }
//                  else if (menuItemId == R.id.action_about) {
//                    Toast.makeText(MainActivity.this, R.string.menu_about_us, Toast.LENGTH_SHORT).show();
////
//                }
                return true;
            }
        });

//        btn_pause = (Button)findViewById(R.id.btn_pause);
//        btn_pause.setOnClickListener(this);
//        mToolbar.setVisibility(View.VISIBLE);

//        lv_record.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if(newState== RecyclerView.SCROLL_STATE_DRAGGING){
//                    if (unClosedSwipeView.size() > 0) {
//                        closeAllOpenedSwipeView();
//                    }
//                }
//            }
//
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//            }
//        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
//            case R.id.start_record://开始录音
//                startRecord();
//                break;
//            case R.id.end_record://技术录音
//                endRecord();
//                break;
            case R.id.btn_cancel:
                cbVisibility=false;
                ll_batch.setVisibility(View.GONE);

//                toolbar.setVisibility(View.VISIBLE);
                for(int i=0;i<mMusicList.size();i++){
                    mMusicList.get(i).setCheck(false);
                }
//                adapter.notifyDataSetChanged();
                menuAdapter.setCbVisibility(cbVisibility);
                break;
            case R.id.btn_delete:

                new AlertDialog.Builder(this)
                            .setTitle("确定要删除吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    for(int i=0;i<mMusicList.size();i++){
                                        RecordBean recordBean = mMusicList.get(i);
                                        Log.e("TAG", "recordBean.isCheck()"+recordBean.isCheck());
                                        if(recordBean.isCheck()){
                                            File file = new File(recordBean.getStorePosition());
                                            file.delete();
                                            dbManager.delete(recordBean.getCreateName());
                                            mRecordList.remove(i);
                                            mMusicList.remove(i);

                                            i--;
                                        }
                                    }
                                    cbVisibility=false;
                                    ll_batch.setVisibility(View.GONE);
//                                    toolbar.setVisibility(View.VISIBLE);
//                                    adapter.notifyDataSetChanged();
                                    menuAdapter.setCbVisibility(cbVisibility);
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();

                break;
            case R.id.fab_play:
                if(inRecording){
                    inRecording=false;
//                    list.add(mRecAudioFile.getAbsolutePath());
                    fab_play.setImageResource(R.drawable.white_record);
                    endRecord();
//                    stopRecord();
                    lv_record.setEnabled(true);
                    rl_record.setVisibility(View.GONE);
                }else{
                    checkAudioPermission();



//                    tv_record_finish.setVisibility(View.GONE);
                }
                break;
//            case R.id.tv_record_finish:
//                if(inRecording){
//                    stopRecord();
//
//                    if(list.size()>0){
//                        list.add(mRecAudioFile.getAbsolutePath());
//                        getInputCollection(list,true);
//                    }else{
//                        renameFile(mRecAudioFile);
//                    }
//                }else{
//                    if(list.size()>1){
//                        getInputCollection(list,true);
//                    }else{
//                        renameFile(mRecAudioFile);
//                    }
//                }
//                break;
        }

    }

    private void checkAudioPermission() {
        if (Dexter.isRequestOngoing()) {
            return;
        }
        Dexter.checkPermissions(new MultiplePermissionsListener() {
                                    @Override
                                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                                        if(!report.areAllPermissionsGranted()){
                                            Snackbar.make(fab_play,"权限不足，无法开启录音！",Snackbar.LENGTH_SHORT).show();
                                        }else{
                                            rl_record.setVisibility(View.VISIBLE);
                                            fab_play.setImageResource(R.drawable.ic_media_pause);
                                            startRecord();
                                            lv_record.setEnabled(false);
                                            inRecording=true;
                                        }
                                    }

                                    @Override
                                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                                        token.continuePermissionRequest();
                                    }
                                }, android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.RECORD_AUDIO);
    }


    private void endRecord() {
//        isRecording=false;
//        ll_voice.setVisibility(View.GONE);


        // TODO Auto-generated method stub
        if (mRecAudioFile != null)
        {
//            getInputCollection(list,true);
            stopRecord();


                    /* 按钮状态 */
//            start_record.setEnabled(true);
//            end_record.setEnabled(false);

//            mDialogManager.dimissDialog();
            renameFile(mRecAudioFile);
//            chronometer.setBase(SystemClock.elapsedRealtime());//计时器清零

            list.clear();
        }

    }

    /**
     * 停止录音
     */
    private void stopRecord() {
//        isRecording=false;
    /* ⑤停止录音 */
//            mMediaRecorder.setOnErrorListener(null);
        if(mMediaRecorder!=null){
            mMediaRecorder.stop();

            rangeTime=SystemClock.elapsedRealtime()-chronometer.getBase();
            chronometer.stop();
            sumRange=rangeTime;
            Log.e("TAG", "rangeTime="+rangeTime);
            Log.e("TAG", "sumRange="+sumRange);
            Log.e("TAG", "SystemClock.elapsedRealtime()="+SystemClock.elapsedRealtime());
            Log.e("TAG", "chronometer.getBase()="+chronometer.getBase());
          /* ⑥释放MediaRecorder */
//            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    private void renameFile(final File mRecAudioFile) {
        for(int i=0;i<mMusicList.size();i++){
            boolean contain=false;
            for(int j=0;j<mMusicList.size();j++){
                RecordBean recordBean = mMusicList.get(j);
                if(recordBean.getName().equalsIgnoreCase("新录音"+(i+1))){
                    contain=true;
                    fileCount=i+1;
                    break;
                }
            }
            if(!contain){
                fileCount=i;
                break;
            }


        }
        fileCount++;

        final EditText editText = new EditText(this);
        editText.setText("新录音"+(fileCount));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.save_record))
                .setCancelable(false)
                .setView(editText)
                .setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = editText.getText().toString().trim();
                        if (!name.isEmpty()) {
                            long duration=sumRange-450;
//                            MediaPlayer mp = MediaPlayer.create(MainActivity.this, Uri.parse(mRecAudioFile.getAbsolutePath()));
//                            try {
//                                mp.prepare();
//                                if (mp != null) {
//                                    duration = mp.getDuration();
//                                    mp.reset();
//                                    mp.release();
//                                    mp=null;
//                                }
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }



//                            long duration= SystemClock.elapsedRealtime()-chronometer.getBase()+sumRange;
//                            Log.e("TAG", "SystemClock.elapsedRealtime()="+SystemClock.elapsedRealtime());
//                            Log.e("TAG", "chronometer.getBase()="+chronometer.getBase());
//                            Log.e("TAG", "sumRange="+sumRange);
                            Log.e("TAG", "duration=" + duration);
                            RecordBean recordBean = new RecordBean(1, name, System.currentTimeMillis(), duration, mRecAudioFile.getName(),mRecAudioFile.getAbsolutePath(),whichFolder);
                            dbManager.add(recordBean);



                            if (mRecAudioFile != null) {
//                                    mMusicList.add(name);
                                updateListData();
//                                dialog.dismiss();
                                noCloseDialog(dialog,true);

//                                inRecording=false;
                            }
                        } else {
                            CommentUtils.showToast(MainActivity.this,"名字不能为空！");
                            noCloseDialog(dialog,false);
                        }
                    }
                })
                .setNegativeButton(getString(R.string.give_up), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRecAudioFile.delete();
//                        dialog.dismiss();
                        noCloseDialog(dialog,true);
//                        inRecording=false;
                    }
                })
                .show();

        chronometer.setBase(SystemClock.elapsedRealtime());//计时器清零

        list.clear();
        rangeTime=0;
//        sumRange=rangeTime;
//        tv_record_finish.setVisibility(View.GONE);
        fab_play.setImageResource(R.drawable.white_record);
    }

    /**
     * 更新列表数据
     */
    private void updateListData() {
        new Thread(){
            public void run(){
                mMusicList.clear();
                mRecordList.clear();
                List<RecordBean> recordBeanList = dbManager.queryAllRecord(whichFolder);
                for (int i = 0; i < recordBeanList.size(); i++) {
                    mMusicList.add(recordBeanList.get(i));
                    mRecordList.add(recordBeanList.get(i).getCreateName());
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        menuAdapter.notifyDataSetChanged();
                    }
                });
            }
        }.start();



    }

    /**
     * dialog是否能關閉
     * @param dialog
     * @param canClose
     */
    private void noCloseDialog(DialogInterface dialog,boolean canClose) {
        try {
            Field field = dialog.getClass().getSuperclass().getDeclaredField( "mShowing" );
            field.setAccessible( true );
            field.set( dialog, canClose );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startRecord() {
        if(mRecAudioPath==null){
            checkStorePromission();
        }
//        isRecording=true;
        try
        {
                    /* ①Initial：实例化MediaRecorder对象 */
            mMediaRecorder = new MediaRecorder();

            /* ②setAudioSource/setVedioSource*/
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//设置麦克风
                    /* ②设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default
                     * THREE_GPP(3gp格式，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
                     * */
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    /* ②设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default */
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    /* ②设置输出文件的路径 */
            try
            {
                mRecAudioFile = File.createTempFile(strTempFile, ".amr", mRecAudioPath);

            } catch (Exception e)
            {
                e.printStackTrace();
            }
            mMediaRecorder.setOutputFile(mRecAudioFile.getAbsolutePath());
                    /* ③准备 */
            mMediaRecorder.prepare();
                    /* ④开始 */
            mMediaRecorder.start();
                    /*按钮状态*/
//            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.setBase(SystemClock.elapsedRealtime()-rangeTime);
            chronometer.start();
//            start_record.setEnabled(false);
//            end_record.setEnabled(true);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }


    /* 播放录音文件 */
    private void playMusic(File file)
    {
//        Intent intent = new Intent();
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.setAction(android.content.Intent.ACTION_VIEW);
//        /* 设置文件类型 */
//        intent.setDataAndType(Uri.fromFile(file), "audio");
//        startActivity(intent);
        MediaManager.playSound(file.getAbsolutePath(), new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {

            }
        });
    }

    /* 播放列表 */
    public void musicList()
    {

//        if (Environment.getExternalStorageState().equals(
//                Environment.MEDIA_MOUNTED))// 手机有SD卡的情况
//        {
//            // 在这里我们创建一个文件，用于保存录制内容
//            mRecAudioPath = new File(Environment.getExternalStorageDirectory()
//                    .getAbsolutePath() + "/data/files/");
//            mRecAudioPath.mkdirs();// 创建文件夹
//        } else// 手机无SD卡的情况
//        {
//            mRecAudioPath = this.getCacheDir();
//        }

        new Thread(){
            public void run(){
                // 取得指定位置的文件设置显示到播放列表
                File home = mRecAudioPath;
                if (home.listFiles(new MusicFilter()).length > 0)
                {
                    File[] files = home.listFiles(new MusicFilter());
                    Log.e("TAG", "1");


                    mMusicList.clear();
                    mRecordList.clear();

                    List<RecordBean> recordBeen = dbManager.queryAll();
                    for(int i=0;i<recordBeen.size();i++){
                        RecordBean recordBean = recordBeen.get(i);

//                if(("新录音"+(i+1)).equalsIgnoreCase(recordBean.getName())){
//                    fileCount=i+1;
//                }
                        Log.e("TAG", "fileCount="+fileCount);
                        boolean have=false;
                        for(int j=0;j<files.length;j++){
                            if(files[j].getName().equalsIgnoreCase(recordBean.getCreateName())){
//                        if(recordBean.getWhichFolder()==whichFolder){


//                        }
                                have=true;
                                break;
                            }
                        }
                        if(!have){
                            dbManager.delete(recordBean.getCreateName());
                        }

                    }
                    List<RecordBean> recordBeen1 = dbManager.queryAllRecord(whichFolder);
                    for(int i=0;i<recordBeen1.size();i++){
                        mMusicList.add(recordBeen1.get(i));
                        mRecordList.add(recordBeen1.get(i).getCreateName());
                    }
                    Log.e("TAG", "musicList name="+createName);
                    int position=-1;
                    for (int i = 0; i < mMusicList.size(); i++) {
                        if(mMusicList.get(i).getCreateName().equalsIgnoreCase(createName)){
                            position=i;
                            break;
                        }
                    }
//            mMusicList.get(position).setShake(true);
//            adapter.notifyDataSetChanged();
//            lv_record.setSelection(position);

                    if(position!=-1){
//                        dbManager.updateIsAlert(createName,0);
                        mMusicList.get(position).setShake(true);
                        final int finalPosition = position;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                menuAdapter.notifyDataSetChanged();

                                lv_record.scrollToPosition(finalPosition);
                            }
                        });

                    }else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                menuAdapter.notifyDataSetChanged();
                            }
                        });

                    }
                }else{
                    dbManager.deleteAll();
                }
            }
        }.start();

    }


//    @Override
//    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, final long id) {
//        deleteRecordFile(position);
//
//
//        return true;
//    }

    /**
     * 删除录音文件
     * @param position
     */
    private void deleteRecordFile(final int position) {

        new Thread(){
            public void run(){
                /* 得到被点击的文件 */

                final File playfile = new File(mRecAudioPath.getAbsolutePath() + File.separator
                        + mRecordList.get(position));
                final String createName = mRecordList.get(position);
                playfile.delete();
                dbManager.delete(createName);
            }
        }.start();


        mMusicList.remove(position);

        menuAdapter.notifyItemChanged(position);
//                    }
//                })
    }

    /* 过滤文件类型 */
    class MusicFilter implements FilenameFilter
    {
        public boolean accept(File dir, String name)
        {
            return (name.endsWith(".amr"));
        }
    }


    @Override
    public void onBackPressed() {
//        super.onBackPressed();

        if(inRecording){
            inRecording=false;
//                    list.add(mRecAudioFile.getAbsolutePath());
            fab_play.setImageResource(R.drawable.white_record);
            endRecord();
//                    stopRecord();
            lv_record.setEnabled(true);
            rl_record.setVisibility(View.GONE);
        }else{
            backFolderActivity();
        }
    }

    private void backFolderActivity() {
        Intent intent = new Intent(this, FolderActivity.class);
        startActivity(intent);
        finish();
    }




    /**
     * 开启提醒
     */
    private void startRemind(){
        Intent intent = new Intent(this, BootScheduleService.class);
        startService(intent);
    }

    /**
     * 关闭提醒
     */
    private void stopRemind(){

        Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(MainActivity.this, 0,
                intent, 0);
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        //取消警报
        am.cancel(pi);
        Toast.makeText(this, "关闭了提醒", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onPause() {
        super.onPause();
        MediaManager.pause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        final Intent finalIntent=intent;


        new Thread(){
            public void run(){
                stopNotificationMusic();
                folderName = finalIntent.getStringExtra("folderName");
                Log.e("TAG", "Mainactivity folderName="+folderName);
                String createName = finalIntent.getStringExtra("createName");
                Log.e("TAG", "onnewIntent createName="+createName);
                whichFolder = finalIntent.getStringExtra("whichFolder");


                int position=-1;
                mMusicList.clear();
                mRecordList.clear();
                List<RecordBean> recordBeanList = dbManager.queryAllRecord(whichFolder);
                for (int i = 0; i < recordBeanList.size(); i++) {
                    mMusicList.add(recordBeanList.get(i));
                    mRecordList.add(recordBeanList.get(i).getCreateName());
                    if(recordBeanList.get(i).getCreateName().equalsIgnoreCase(createName)){
                        position=i;
                    }
                }
                if(position!=-1){
                    mMusicList.get(position).setShake(true);
//                    dbManager.updateIsAlert(createName,0);
                    final int finalPosition = position;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            menuAdapter.notifyDataSetChanged();

                            lv_record.scrollToPosition(finalPosition);
                        }
                    });

                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toolbar.setTitle(folderName);
                            menuAdapter.notifyDataSetChanged();
                        }
                    });

                }
            }
        }.start();



    }

    @Override
    protected void onResume() {
        super.onResume();
        MediaManager.resume();
    }

    /**
     * 当菜单某个选项被点击时调用该方法
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
//        final int selectedPosition = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;

       selectedPosition= menuAdapter.getSelectedPosition();
        final RecordBean recordBean = mMusicList.get(selectedPosition);
        switch(item.getItemId()){
            case 2:
                final EditText editText = new EditText(this);
                editText.setText(recordBean.getName());
                new AlertDialog.Builder(this)
                            .setTitle("修改录音名字")
                            .setView(editText)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String name = editText.getText().toString().trim();
                                    if(!name.isEmpty()){
                                        dbManager.update(name,recordBean.getCreateName());
                                        recordBean.setName(name);
                                        mMusicList.set(selectedPosition,recordBean);
//                                        menuAdapter.notifyDataSetChanged();
                                        menuAdapter.notifyItemChanged(selectedPosition);
                                    }
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();


                break;
            case 1:
                Intent intent1 = new Intent(this, PlayActivity.class);
                intent1.putExtra("position",selectedPosition);
                intent1.putExtra("path",mMusicList.get(selectedPosition).getStorePosition());
                 intent1.putExtra("recordName",mMusicList.get(selectedPosition).getName());
//
                 startActivity(intent1);
                break;
            case 3:
                dbManager.updateIsAlert(recordBean.getCreateName(),0);
                Intent intent = new Intent(this, BootScheduleService.class);
                startService(intent);
                CommentUtils.showToast(this,"取消成功！");
                recordBean.setIsAlert(0);
                mMusicList.set(selectedPosition,recordBean);
//                menuAdapter.notifyDataSetChanged();
                menuAdapter.notifyItemChanged(selectedPosition);
                break;
            case 4:
                cbVisibility=true;
                ll_batch.setVisibility(View.VISIBLE);
//                toolbar.setVisibility(View.GONE);
                menuAdapter.setCbVisibility(cbVisibility);
                break;
//            case 2:
//                deleteRecordFile(selectedPosition);
//                break;

        }
        return super.onContextItemSelected(item);
    }

    private void stopNotificationMusic(){
        if(NotificationService.mMediaPlayer!=null){
            NotificationService.mMediaPlayer.stop();
        }
    }




    /**
     *  @param isAddLastRecord 是否需要添加list之外的最新录音，一起合并
     *  @return 将合并的流用字符保存
     */
    public  void getInputCollection(List list,boolean isAddLastRecord){



        // 创建音频文件,合并的文件放这里
        File file1=new File(mRecAudioPath,System.currentTimeMillis()+".amr");
        FileOutputStream fileOutputStream = null;

        if(!file1.exists()){
            try {
                file1.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            fileOutputStream=new FileOutputStream(file1);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //list里面为暂停录音 所产生的 几段录音文件的名字，中间几段文件的减去前面的6个字节头文件




        for(int i=0;i<list.size();i++){
            File file=new File((String) list.get(i));
            Log.d("list的长度", list.size()+"");
            try {
                FileInputStream fileInputStream=new FileInputStream(file);
                byte  []myByte=new byte[fileInputStream.available()];
                //文件长度
                int length = myByte.length;

                //头文件
                if(i==0){
                    while(fileInputStream.read(myByte)!=-1){
                        fileOutputStream.write(myByte, 0,length);
                    }
                }

                //之后的文件，去掉头文件就可以了
                else{
                    while(fileInputStream.read(myByte)!=-1){

                        fileOutputStream.write(myByte, 0, length);
                    }
                }

                fileOutputStream.flush();
                fileInputStream.close();
                System.out.println("合成文件长度："+file1.length());

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }



        }
        //结束后关闭流
        try {
            fileOutputStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //加上当前正在录音的这一段
//			if(isAddLastRecord){
//
//
//				//刚刚录音的
//				try {
//					FileInputStream fileInputStream=new FileInputStream(myRecAudioFile);
//					byte  []myByte=new byte[fileInputStream.available()];
//					System.out.println(fileInputStream.available()+"");
//					while(fileInputStream.read(myByte)!=-1){
//						//outputStream.
//						fileOutputStream.write(myByte, 6, (fileInputStream.available()-6));
//					}
//
//					fileOutputStream.flush();
//					fileInputStream.close();
//					fileOutputStream.close();
//					System.out.println("合成文件长度："+file1.length());
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//			}


        //合成一个文件后，删除之前暂停录音所保存的零碎合成文件
        deleteListRecord(isAddLastRecord);
        //
        renameFile(file1);

    }

    private void deleteListRecord(boolean isAddLastRecord){
        for(int i=0;i<list.size();i++){
            File file=new File((String) list.get(i));
            if(file.exists()){
                file.delete();
            }
        }
        //正在暂停后，继续录音的这一段音频文件
        if(isAddLastRecord){
            mRecAudioFile.delete();
        }
    }

    /**
     * 菜单创建器。在Item要创建菜单的时候调用。
     */
    private SwipeMenuCreator swipeMenuCreator = new SwipeMenuCreator() {
        @Override
        public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
            int height = getResources().getDimensionPixelSize(R.dimen.item_height);

            // MATCH_PARENT 自适应高度，保持和内容一样高；也可以指定菜单具体高度，也可以用WRAP_CONTENT。
            int width = getResources().getDimensionPixelSize(R.dimen.item_width);


            // 添加右侧的，如果不添加，则右侧不会出现菜单。
            {
                SwipeMenuItem deleteItem = new SwipeMenuItem(mContext)
                        .setBackgroundDrawable(R.drawable.selector_red)
                        .setText("删除") // 文字，还可以设置文字颜色，大小等。。
                        .setTextColor(Color.WHITE)
                        .setWidth(width)
                        .setHeight(height);
                swipeRightMenu.addMenuItem(deleteItem);// 添加一个按钮到右侧侧菜单。
            }
        }
    };

    /**
     * 菜单点击监听。
     */
    private OnSwipeMenuItemClickListener menuItemClickListener = new OnSwipeMenuItemClickListener() {
        /**
         * Item的菜单被点击的时候调用。
         * @param closeable       closeable. 用来关闭菜单。
         * @param adapterPosition adapterPosition. 这个菜单所在的item在Adapter中position。
         * @param menuPosition    menuPosition. 这个菜单的position。比如你为某个Item创建了2个MenuItem，那么这个position可能是是 0、1，
         * @param direction       如果是左侧菜单，值是：SwipeMenuRecyclerView#LEFT_DIRECTION，如果是右侧菜单，值是：SwipeMenuRecyclerView#RIGHT_DIRECTION.
         */
        @Override
        public void onItemClick(Closeable closeable, int adapterPosition, int menuPosition, int direction) {
            closeable.smoothCloseMenu();// 关闭被点击的菜单。

//            if (direction == SwipeMenuRecyclerView.RIGHT_DIRECTION) {
//                Toast.makeText(mContext, "list第" + adapterPosition + "; 右侧菜单第" + menuPosition, Toast.LENGTH_SHORT).show();
//            } else if (direction == SwipeMenuRecyclerView.LEFT_DIRECTION) {
//                Toast.makeText(mContext, "list第" + adapterPosition + "; 左侧菜单第" + menuPosition, Toast.LENGTH_SHORT).show();
//            }

            // TODO 如果是删除：推荐调用Adapter.notifyItemRemoved(position)，不推荐Adapter.notifyDataSetChanged();
            if (menuPosition == 0) {// 删除按钮被点击。
//                mMusicList.remove(adapterPosition);
//                menuAdapter.notifyItemRemoved(adapterPosition);
                deleteRecordFile(adapterPosition);
            }
        }
    };


    private OnItemClickListener onItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
//            Toast.makeText(mContext, "我是第" + position + "条。", Toast.LENGTH_SHORT).show();
            if(menuAdapter.isCbVisibility()){
                menuAdapter.setCbVisibility(false);
            }else{
                menuAdapter.setCbVisibility(true);
            }
        }
    };

}
