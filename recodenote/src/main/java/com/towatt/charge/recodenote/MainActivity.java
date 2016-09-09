package com.towatt.charge.recodenote;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.towatt.charge.recodenote.adapter.RecordAdapter;
import com.towatt.charge.recodenote.bean.RecordBean;
import com.towatt.charge.recodenote.db.DBManager;
import com.towatt.charge.recodenote.manager.MediaManager;
import com.towatt.charge.recodenote.receiver.AlarmReceiver;
import com.towatt.charge.recodenote.service.BootScheduleService;
import com.towatt.charge.recodenote.service.NotificationService;
import com.towatt.charge.recodenote.utils.CommentUtils;
import com.towatt.charge.recodenote.view.SwipeView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.jar.Manifest;

public class  MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener , SwipeView.OnSwipeStatusChangeListener {

    private RecyclerView lv_record;
//    private Button start_record;
//    private Button end_record;
    private File mRecAudioFile;        // 录制的音频文件
    private File mRecAudioPath;        // 录制的音频文件路徑
    private MediaRecorder mMediaRecorder;// MediaRecorder对象
    private String strTempFile = "record";// 零时文件的前缀
    private List<RecordBean> mMusicList = new ArrayList<RecordBean>();// 录音文件列表
    private List<String> mRecordList = new ArrayList<String>();// 录音文件列表

    private int fileCount=0;//新录音（数字）的数字
    private RecordAdapter adapter;

    private DBManager dbManager;
    private Calendar mCalendar;
//    private DialogManager mDialogManager;//录音的过程动画
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
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    //    private Button btn_pause;

    private AppBarLayout appbar;

    private Toolbar toolbar;
    private CoordinatorLayout coordinator_layout;

    private MultiplePermissionsListener allPermissionsListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collapsing_demo);

        initView();

        initData();


    }

    private void initData() {
        startRemind();

        stopNotificationMusic();

        checkStorePromission();

        dbManager=new DBManager(this);




        registerForContextMenu(lv_record);

        Intent intent = new Intent(this, BootScheduleService.class);
        startService(intent);





//        ll_voice.setVisibility(View.GONE);


//        chronometer.setBase(SystemClock.elapsedRealtime());//计时器清零
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
        lv_record = (RecyclerView)findViewById(R.id.lv_record);
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

        appbar = (AppBarLayout)findViewById(R.id.appbar);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);
        mCollapsingToolbarLayout.setTitle("语音记事本");

        toolbar = (Toolbar)findViewById(R.id.toolbar);

//        mCollapsingToolbarLayout.setCollapsedTitleTextColor(R.color.white);

        chronometer = (Chronometer)findViewById(R.id.chronometer);
        btn_cancel.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
//        tv_record_finish.setOnClickListener(this);

        fab_play.setOnClickListener(this);


        lv_record.setLayoutManager(new LinearLayoutManager(this));




        lv_record.setItemAnimator(new DefaultItemAnimator());
//        mDialogManager = new DialogManager(this);
        adapter = new RecordAdapter(this,mMusicList,cbVisibility);

        lv_record.setAdapter(adapter);


        mToolbar = (Toolbar) findViewById(R.id.toolbar);
//        mToolbar.inflateMenu(R.menu.zhihu_toolbar_menu);

//        mToolbar.setNavigationIcon(R.mipmap.white_record);

//        mToolbar.setTitle("语音记事本");
//        mToolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

               if (menuItemId == R.id.action_settings) {
//                    Toast.makeText(MainActivity.this, R.string.menu_settings, Toast.LENGTH_SHORT).show();
                        finish();
//                } else if (menuItemId == R.id.action_about) {
//                    Toast.makeText(MainActivity.this, R.string.menu_about_us, Toast.LENGTH_SHORT).show();
//
                }
                return true;
            }
        });

//        btn_pause = (Button)findViewById(R.id.btn_pause);
//        btn_pause.setOnClickListener(this);
//        mToolbar.setVisibility(View.VISIBLE);

        lv_record.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState== RecyclerView.SCROLL_STATE_DRAGGING){
                    if (unClosedSwipeView.size() > 0) {
                        closeAllOpenedSwipeView();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
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
                adapter.setCbVisibility(cbVisibility);
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
                                    adapter.setCbVisibility(cbVisibility);
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
                            long duration=sumRange-470;
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
                            RecordBean recordBean = new RecordBean(1, name, System.currentTimeMillis(), duration, mRecAudioFile.getName(),mRecAudioFile.getAbsolutePath());
                            dbManager.add(recordBean);



                            if (mRecAudioFile != null) {
//                                    mMusicList.add(name);
                                updateListData();
//                                dialog.dismiss();
                                noCloseDialog(dialog,true);
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
        mMusicList.clear();
        mRecordList.clear();
        List<RecordBean> recordBeanList = dbManager.queryAll();
        for (int i = 0; i < recordBeanList.size(); i++) {
            mMusicList.add(recordBeanList.get(i));
            mRecordList.add(recordBeanList.get(i).getCreateName());
        }

        adapter.notifyDataSetChanged();
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        /* 得到被点击的文件 */
//        File playfile = new File(mRecAudioPath.getAbsolutePath() + File.separator
//                + mRecordList.get(position));
//        /* 播放 */
//        playMusic(playfile);
        Log.e("TAG", "onItemClick=");
        if(cbVisibility){
            ViewHolder viewHolder = (ViewHolder) view.getTag();
            viewHolder.cb_select.toggle();
            Log.e("TAG", "viewHolder.cb_select.isChecked()="+viewHolder.cb_select.isChecked());
            if(viewHolder.cb_select.isChecked()){
                mMusicList.get(position).setCheck(true);
            }else{
                mMusicList.get(position).setCheck(false);
            }
        }else{

            Intent intent = new Intent(this, ClockActivity.class);
            intent.putExtra("createName",mRecordList.get(position));
            startActivity(intent);

//            Intent intent = new Intent(this, PlayActivity.class);
//            intent.putExtra("position",position);
//            intent.putExtra("recordName",mMusicList.get(position).getName());
//
//            startActivity(intent);
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
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED))// 手机有SD卡的情况
        {
            // 在这里我们创建一个文件，用于保存录制内容
            mRecAudioPath = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/data/files/");
            mRecAudioPath.mkdirs();// 创建文件夹
        } else// 手机无SD卡的情况
        {
            mRecAudioPath = this.getCacheDir();
        }

        // 取得指定位置的文件设置显示到播放列表
        File home = mRecAudioPath;
        if (home.listFiles(new MusicFilter()).length > 0)
        {
//            for (File file : home.listFiles(new MusicFilter()))
//            {
//                mMusicList.add(file.getName());
//            }
            File[] files = home.listFiles(new MusicFilter());
            Log.e("TAG", "1");

//            for(int i=0;i<files.length;i++){
//                String name = files[i].getName();
//                if(("newrecord"+(i+1)+".amr").equals(name)){
//                    fileCount=i+1;
//                }


//                mMusicList.add(name);
//            }

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
                        mMusicList.add(recordBean);
                        mRecordList.add(recordBean.getCreateName());
                        have=true;
                        break;
                    }
                }
                if(!have){
                    dbManager.delete(recordBean.getCreateName());
                }

            }
            Intent intent = getIntent();
            String name = intent.getStringExtra("name");
            Log.e("TAG", "musicList name="+name);
            int position=-1;
            for (int i = 0; i < mMusicList.size(); i++) {
                if(mMusicList.get(i).getName().equalsIgnoreCase(name)){
                    position=i;
                    break;
                }
            }
//            mMusicList.get(position).setShake(true);
//            adapter.notifyDataSetChanged();
//            lv_record.setSelection(position);

            if(position!=-1){
                appbar.setExpanded(false);
                mMusicList.get(position).setShake(true);
                adapter.notifyDataSetChanged();

                lv_record.scrollToPosition(position);
            }else{
                adapter.notifyDataSetChanged();
            }
        }else{
            dbManager.deleteAll();
        }
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
    /* 得到被点击的文件 */
        final File playfile = new File(mRecAudioPath.getAbsolutePath() + File.separator
                + mRecordList.get(position));
        final String createName = mRecordList.get(position);
//        new AlertDialog.Builder(this)
//                .setTitle("确定要删除吗？")
//                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
                        playfile.delete();
                        dbManager.delete(createName);

                        mMusicList.remove(position);

                        adapter.notifyDataSetChanged();
//                    }
//                })
//                .setNegativeButton("取消", null)
//                .show();
    }

    /* 过滤文件类型 */
    class MusicFilter implements FilenameFilter
    {
        public boolean accept(File dir, String name)
        {
            return (name.endsWith(".amr"));
        }
    }

    class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mMusicList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder=null;
            if(convertView==null){
                holder=new ViewHolder();
                convertView=View.inflate(MainActivity.this,R.layout.item,null);
                holder.tv_create_time= (TextView) convertView.findViewById(R.id.tv_create_time);
                holder.tv_record_name= (TextView) convertView.findViewById(R.id.tv_record_name);
                holder.tv_clock_time= (TextView) convertView.findViewById(R.id.tv_clock_time);
                holder.id_time= (TextView) convertView.findViewById(R.id.id_time);
                holder.id_recorder_length= (FrameLayout) convertView.findViewById(R.id.id_recorder_length);
                holder.mAnimView= convertView.findViewById(R.id.id_recorder_anim);
                holder.cb_select= (CheckBox) convertView.findViewById(R.id.cb_select);



                holder.delete = (TextView) convertView.findViewById(R.id.delete);
                holder.swipeView = (SwipeView) convertView.findViewById(R.id.swipeView);
                holder.ll_content= (LinearLayout) convertView.findViewById(R.id.ll_content);
//                holder.sb_record= (SeekBar) convertView.findViewById(R.id.sb_record);
//                holder.tv_start_time= (TextView) convertView.findViewById(R.id.tv_start_time);
//                holder.tv_end_start= (TextView) convertView.findViewById(R.id.tv_end_start);
//                holder.btn_start= (ImageView) convertView.findViewById(R.id.btn_start);
//                holder.btn_stop= (ImageView) convertView.findViewById(R.id.btn_stop);
//                holder.ll_bottom= (LinearLayout) convertView.findViewById(R.id.ll_bottom);


                convertView.setTag(holder);

            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            /**
             * 说话的动画
             */
//            if(mAnimView != null)
//            {
//                mAnimView.setBackgroundResource(R.drawable.adj);
//                mAnimView = null;
//            }
//            mAnimView=convertView.findViewById(R.id.id_recorder_anim);

            final int lastPosition=position;
            final RecordBean recordBean = mMusicList.get(position);
            holder.tv_create_time.setText(CommentUtils.longToYMDHMS(recordBean.getCreateDate()));
            holder.tv_record_name.setText(recordBean.getName());
            holder.id_time.setText(CommentUtils.longToHMS(recordBean.getDuration())+ "\"");
            long clockTime = recordBean.getClockTime();
            if(clockTime!=0){
                if(clockTime>System.currentTimeMillis()&&recordBean.getIsAlert()!=0){
                    holder.tv_clock_time.setTextColor(getResources().getColor(R.color.red));
                }else{
                    holder.tv_clock_time.setTextColor(getResources().getColor(R.color.gray));
                }
                holder.tv_clock_time.setText (CommentUtils.longToYMDHMS(recordBean.getClockTime()));
            }else{
                holder.tv_clock_time.setTextColor(getResources().getColor(R.color.gray));
                holder.tv_clock_time.setText ("未设置");
            }
            if(cbVisibility){
                holder.cb_select.setVisibility(View.VISIBLE);
                holder.cb_select.setChecked(recordBean.isCheck());
            }else{
                holder.cb_select.setVisibility(View.GONE);
            }
//            AlphaAnimation alphaAnimation1 = new AlphaAnimation(0.1f, 1.0f);
//            alphaAnimation1.setDuration(1000);
//            alphaAnimation1.setRepeatCount(5);
//            alphaAnimation1.setRepeatMode(Animation.REVERSE);
            Animation alphaAnimation1 = AnimationUtils.loadAnimation(
                    MainActivity.this, R.anim.myanim);
            alphaAnimation1.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    recordBean.setShake(false);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            if(recordBean.isShake()){

                convertView.setAnimation(alphaAnimation1);
                alphaAnimation1.start();

//                recordBean.setShake(false);
                mMusicList.set(position,recordBean);
            }else{
                convertView.clearAnimation();
            }
            final ViewHolder finalHolder = holder;
            final View finalView=convertView;
            holder.id_recorder_length.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finalView.clearAnimation();
                    finalHolder.mAnimView.setBackgroundResource(R.drawable.play_anim);
                    AnimationDrawable anim = (AnimationDrawable) finalHolder.mAnimView.getBackground();
                    anim.start();
                     /* 得到被点击的文件 */
                    File playfile = new File(mRecAudioPath.getAbsolutePath() + File.separator
                            + recordBean.getCreateName());
                    /* 播放 */
                    //播放音频
                    MediaManager.playSound(playfile.getAbsolutePath() , new MediaPlayer.OnCompletionListener() {

                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            finalHolder.mAnimView.setBackgroundResource(R.drawable.adj);
                        }
                    });
                }
            });



            holder.swipeView.setOnSwipeStatusChangeListener(MainActivity.this);

            holder.swipeView.fastClose();


            holder.delete.setText("删除");

            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteRecordFile(lastPosition);
                }
            });

            holder.ll_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(cbVisibility){

                        finalHolder.cb_select.toggle();
                        Log.e("TAG", "viewHolder.cb_select.isChecked()="+finalHolder.cb_select.isChecked());
                        if(finalHolder.cb_select.isChecked()){
                            mMusicList.get(position).setCheck(true);
                        }else{
                            mMusicList.get(position).setCheck(false);
                        }
                    }else{
                        if (unClosedSwipeView.size() > 0) {
                            closeAllOpenedSwipeView();
                        } else {
                            Intent intent = new Intent(MainActivity.this, ClockActivity.class);
                            intent.putExtra("createName",mRecordList.get(lastPosition));
                            startActivity(intent);
                        }
                    }


                }
            });

            holder.ll_content.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                    if (unClosedSwipeView.size() > 0) {
                        closeAllOpenedSwipeView();
                    } else {
                        selectedPosition=lastPosition;
                        menu.add(0, 1, 0, "播放");
                        menu.add(0, 2, 0, "重命名");
                        menu.add(0,3,0,"取消提醒");
                        menu.add(0,4,0,"批量删除");
                    }

                }
            });

            return convertView;
        }


    }

   static class ViewHolder{
        TextView tv_record_name;
        TextView tv_create_time;
        TextView tv_duration;
        TextView id_time;
        ImageView iv_clock;

       TextView tv_clock_time;
        FrameLayout id_recorder_length;
       View mAnimView;//播放声音的动画
       CheckBox cb_select;

       SwipeView swipeView;
       TextView delete;
        LinearLayout ll_content;

//       SeekBar sb_record;
//       TextView tv_start_time;
//       TextView tv_end_start;
//       ImageView btn_start;
//       ImageView btn_stop;
//       LinearLayout ll_bottom;
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
        stopNotificationMusic();

        setIntent(intent);
        String name = intent.getStringExtra("name");
        Log.e("TAG", "onnewIntent name="+name);
        int position=-1;
        mMusicList.clear();
        mRecordList.clear();
        List<RecordBean> recordBeanList = dbManager.queryAll();
        for (int i = 0; i < recordBeanList.size(); i++) {
            mMusicList.add(recordBeanList.get(i));
            mRecordList.add(recordBeanList.get(i).getCreateName());
            if(recordBeanList.get(i).getName().equalsIgnoreCase(name)){
                position=i;
            }
        }
        if(position!=-1){
            appbar.setExpanded(false);
            mMusicList.get(position).setShake(true);
            adapter.notifyDataSetChanged();

            lv_record.scrollToPosition(position);
        }else{
            adapter.notifyDataSetChanged();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        MediaManager.resume();
    }



    /**
     * 创建上下文菜单选项
     */
//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v,
//                                    ContextMenu.ContextMenuInfo menuInfo) {
//
//        //1.通过手动添加来配置上下文菜单选项
//        menu.add(0, 1, 0, "删除");
//        menu.add(0, 2, 0, "重命名");
//        menu.add(0,3,0,"取消提醒");
//        menu.add(0,4,0,"批量删除");
//        //2.通过xml文件来配置上下文菜单选项
////        MenuInflater mInflater = getMenuInflater();
////        mInflater.inflate(R.menu.cmenu, menu);
//
////        super.onCreateContextMenu(menu, v, menuInfo);
//
//        super.onCreateContextMenu(menu,v,menuInfo);
//    }

    /**
     * 当菜单某个选项被点击时调用该方法
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
//        final int selectedPosition = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;

       selectedPosition= adapter.getSelectedPosition();
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
                                        adapter.notifyDataSetChanged();
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
                adapter.notifyDataSetChanged();
                break;
            case 4:
                cbVisibility=true;
                ll_batch.setVisibility(View.VISIBLE);
//                toolbar.setVisibility(View.GONE);
                adapter.setCbVisibility(cbVisibility);
                break;

        }
        return super.onContextItemSelected(item);
    }

    private void stopNotificationMusic(){
        if(NotificationService.mMediaPlayer!=null){
            NotificationService.mMediaPlayer.stop();
        }
    }

    private ArrayList<SwipeView> unClosedSwipeView = new ArrayList<>();

    @Override
    public void onOpen(SwipeView openedSwipeView) {

        for (int i = 0; i < unClosedSwipeView.size(); i++) {
            if (unClosedSwipeView.get(i) != openedSwipeView) {
                unClosedSwipeView.get(i).close();
            }
        }
        if (!unClosedSwipeView.contains(openedSwipeView)) {
            unClosedSwipeView.add(openedSwipeView);
        }
    }

    @Override
    public void onClose(SwipeView closedSwipeView) {
        unClosedSwipeView.remove(closedSwipeView);
    }

    @Override
    public void onSwiping(SwipeView swipingSwipeView) {
        if (!unClosedSwipeView.contains(swipingSwipeView)) {
//            closeAllOpenedSwipeView();
            unClosedSwipeView.add(swipingSwipeView);
        }

    }


    private void closeAllOpenedSwipeView() {
        for (int i = 0; i < unClosedSwipeView.size(); i++) {
            if (unClosedSwipeView.get(i).getSwipeStatus() != SwipeView.SwipeStatus.Close) {
                unClosedSwipeView.get(i).close();
            }
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


}
