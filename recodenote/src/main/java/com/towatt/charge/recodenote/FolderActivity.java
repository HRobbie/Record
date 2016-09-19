package com.towatt.charge.recodenote;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.towatt.charge.recodenote.adapter.FolderAdapter;
import com.towatt.charge.recodenote.bean.FolderBean;
import com.towatt.charge.recodenote.bean.RecordBean;
import com.towatt.charge.recodenote.db.DBManager;
import com.towatt.charge.recodenote.service.BootScheduleService;
import com.towatt.charge.recodenote.utils.CommentUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class FolderActivity extends AppCompatActivity implements View.OnClickListener{

    private RecyclerView lv_record;
    private List<String> mRecordList = new ArrayList<String>();// 录音文件列表

    private FolderAdapter adapter;

    private Toolbar mToolbar;

    private FloatingActionButton fab_play;

    private DBManager dbManager;

    private File mRecAudioFile;        // 录制的音频文件
    private File mRecAudioPath;        // 录制的音频文件路徑
    private MediaRecorder mMediaRecorder;// MediaRecorder对象
    private String strTempFile = "record";// 零时文件的前缀

    private long rangeTime;//上一次暂停经历的时间

    private long sumRange=rangeTime;
    private Chronometer chronometer;

    private ArrayList<FolderBean> folderList=new ArrayList<>();
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    //    private Button btn_pause;

    private MultiplePermissionsListener allPermissionsListener;

    private boolean inRecording;

    private int folderCount=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        initView();

        initData();


    }

    private void initData() {
        startRemind();

        checkStorePromission();

        dbManager=new DBManager(this);

        checkFolderCount();

        registerForContextMenu(lv_record);

        int hour = (int) ((SystemClock.elapsedRealtime() - chronometer.getBase()) / 1000 / 60);
        chronometer.setFormat("0"+String.valueOf(hour)+":%s");
        chronometer.setBase(SystemClock.elapsedRealtime());//计时器清零



    }

    private void checkFolderCount() {
        new Thread(){
            public void run(){
                List<FolderBean> folderBeen = dbManager.queryFolderAll();
                folderList.clear();
                folderList.addAll(folderBeen);
//                countFolder();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }.start();

    }


    @Override
    protected void onStart() {
        super.onStart();

    }

    private void initView() {
        lv_record = (RecyclerView)findViewById(R.id.lv_record);


        fab_play = (FloatingActionButton) findViewById(R.id.fab_play);

        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);
//        mCollapsingToolbarLayout.setTitle("语音记事本");

        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mToolbar.setTitle("语音记事本");

        chronometer = (Chronometer)findViewById(R.id.chronometer);

        fab_play.setOnClickListener(this);


        lv_record.setLayoutManager(new LinearLayoutManager(this));




        lv_record.setItemAnimator(new DefaultItemAnimator());
        adapter = new FolderAdapter(this,folderList);

        lv_record.setAdapter(adapter);

        registerForContextMenu(lv_record);



        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        adapter.setOnItemClickListener(new FolderAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, Object object) {
                FolderBean folderBean = folderList.get(position);
                String folderName = folderBean.getFolderName();
                String whichFolder = folderBean.getWhichFolder();

                Intent intent = new Intent(FolderActivity.this, MainActivity.class);
                intent.putExtra("whichFolder",whichFolder);
                intent.putExtra("folderName",folderName);
//                intent.putExtra("createName",mRecAudioFile.getName());
                intent.putExtra("mRecAudioPath",mRecAudioPath.getAbsolutePath());
                Log.e("TAG", "FolderActivity whichFolder="+whichFolder);
                startActivity(intent);
            }
        });


        mToolbar.inflateMenu(R.menu.zhihu_toolbar_menu);

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
//                    Toast.makeText(FolderActivity.this, R.string.menu_settings, Toast.LENGTH_SHORT).show();

//                        finish();
//                   folderCount++;
                   countFolder();
                   final EditText editText = new EditText(FolderActivity.this);
                   editText.setText("新建文件夹"+folderCount);
                   new AlertDialog.Builder(FolderActivity.this)
                           .setTitle("新建文件夹")
                           .setCancelable(false)
                           .setView(editText)
                           .setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
                               @Override
                               public void onClick(DialogInterface dialog, int which) {
                                   String name = editText.getText().toString().trim();
                                   if (!name.isEmpty()) {
                                       long timeMillis = System.currentTimeMillis();
                                       FolderBean folderBean = new FolderBean(1, timeMillis+"", name, timeMillis);
                                       dbManager.addFolder(folderBean);
                                       noCloseDialog(dialog,true);
                                       updateListData();
                                   } else {
                                       CommentUtils.showToast(FolderActivity.this,"名字不能为空！");
                                       noCloseDialog(dialog,false);
                                   }


                               }
                           })
                           .setNegativeButton(getString(R.string.give_up), new DialogInterface.OnClickListener() {
                               @Override
                               public void onClick(DialogInterface dialog, int which) {
                                   noCloseDialog(dialog,true);
//                                   folderCount--;
                               }
                           })
                           .show();
                }
//                  else if (menuItemId == R.id.action_about) {
//                    Toast.makeText(MainActivity.this, R.string.menu_about_us, Toast.LENGTH_SHORT).show();
////
//                }
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_play:
                if(inRecording){
                    inRecording=false;
                    fab_play.setImageResource(R.drawable.white_record);
                    endRecord();
//                    stopRecord();
                    lv_record.setEnabled(true);
                }else{
                    checkAudioPermission();
                }
                break;
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


        // TODO Auto-generated method stub
        if (mRecAudioFile != null)
        {
            stopRecord();


            renameFile(mRecAudioFile);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
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
        /**
         * 计算新建文件夹的个数
         */
        countFolder();

//        folderCount++;
        final long timeMillis = System.currentTimeMillis();
        View view = View.inflate(this, R.layout.two_edit_text, null);
        final EditText editText = (EditText) view.findViewById(R.id.editText);
        final EditText editText1 = (EditText) view.findViewById(R.id.editText1);
        editText.setText("新录音1");
        editText1.setText("新建文件夹"+(folderCount));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.save_record))
                .setCancelable(false)
                .setView(view)
                .setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = editText.getText().toString().trim();
                        String folderName = editText1.getText().toString().trim();
                        if (!name.isEmpty()&&!folderName.isEmpty()) {
                            long duration=sumRange-470;
                            Log.e("TAG", "duration=" + duration);

                            RecordBean recordBean = new RecordBean(1, name, timeMillis, duration, mRecAudioFile.getName(),mRecAudioFile.getAbsolutePath(),timeMillis+"");
                            dbManager.add(recordBean);
                            FolderBean folderBean = new FolderBean(1, timeMillis+"", folderName, timeMillis);
                            dbManager.addFolder(folderBean);

                            if (mRecAudioFile != null) {
                                noCloseDialog(dialog,true);
                                updateListData();
//                                createFolder(timeMillis);
                            }
                        } else {
                            CommentUtils.showToast(FolderActivity.this,"名字不能为空！");
                            noCloseDialog(dialog,false);
                        }


                    }
                })
                .setNegativeButton(getString(R.string.give_up), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRecAudioFile.delete();
                        noCloseDialog(dialog,true);
//                        folderCount--;
                    }
                })
                .show();

        chronometer.setBase(SystemClock.elapsedRealtime());//计时器清零

        rangeTime=0;
        fab_play.setImageResource(R.drawable.white_record);
    }

    private void countFolder() {
        for(int i=0;i<folderList.size();i++){
            boolean contain=false;
            for(int j=0;j<folderList.size();j++){
                FolderBean recordBean = folderList.get(j);
                if(recordBean.getFolderName().equalsIgnoreCase("新建文件夹"+(i+1))){
                    contain=true;
                    folderCount=i+1;
                    break;
                }
            }
            if(!contain){
                folderCount=i;
                break;
            }


        }
        folderCount++;

    }

    private void createFolder(final long timeMillis) {
        final EditText editText1 = new EditText(this);
        editText1.setText("新建文件夹"+(folderCount+1));
        new AlertDialog.Builder(this)
                .setTitle("为录音文件新建一个文件夹")
                .setView(editText1)
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String folderName = editText1.getText().toString().trim();
                        if (!folderName.isEmpty()) {
                            FolderBean folderBean = new FolderBean(1, timeMillis+"", folderName, timeMillis);
                            dbManager.addFolder(folderBean);
                            noCloseDialog(dialog, true);
                            updateListData();
                        } else {
                            CommentUtils.showToast(FolderActivity.this, "名字不能为空！");
                            noCloseDialog(dialog, false);
                        }
                    }
                })
                .show();
    }

    /**
     * 更新列表数据
     */
    private void updateListData() {
        new Thread(){
            public void run(){
                folderList.clear();
                List<FolderBean> recordBeanList = dbManager.queryFolderAll();
                folderList.addAll(recordBeanList);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
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
    private void checkStorePromission() {
        Dexter.checkPermission(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                if (Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED))// 手机有SD卡的情况
                {
                    // 在这里我们创建一个文件，用于保存录制内容
                    mRecAudioPath = new File(Environment.getExternalStorageDirectory()
                            .getAbsolutePath() + "/data/files/");
                    mRecAudioPath.mkdirs();// 创建文件夹
                } else// 手机无SD卡的情况
                {
                    mRecAudioPath = FolderActivity.this.getCacheDir();
                }

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



    /**
     * 开启提醒
     */
    private void startRemind(){
        Intent intent = new Intent(this, BootScheduleService.class);
        startService(intent);
    }


    /**
     * 当菜单某个选项被点击时调用该方法
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
//


        int selectedPosition = adapter.getPosition();

        final int finalSelectedPosition = selectedPosition;
        final FolderBean folderBean = folderList.get(selectedPosition);
        switch(item.getItemId()){
            case 2:
                final EditText editText = new EditText(FolderActivity.this);
                editText.setText(folderBean.getFolderName());
                new AlertDialog.Builder(this)
                        .setTitle("请输入新文件夹名称")
                        .setView(editText)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String name = editText.getText().toString().trim();
                                if(!name.isEmpty()){
                                    new Thread(){
                                        public void run(){
                                            dbManager.updateFolderName(name,folderBean.getWhichFolder());
                                        }
                                    }.start();
                                    folderBean.setFolderName(name);
                                    Log.e("TAG", "重命名finalSelectedPosition="+finalSelectedPosition);
                                    adapter.notifyItemChanged(finalSelectedPosition);
                                    CommentUtils.showToast(FolderActivity.this,"修改成功！");
//                                    updateListData();

                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();

                break;
            case 1:
                new Thread(){
                    public void run(){
                        final List<RecordBean> recordBeen = dbManager.queryAllRecord(folderBean.getWhichFolder());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(recordBeen.size()>0){
                                    CommentUtils.showToast(FolderActivity.this,"文件夹内有文件，不能删除文件夹！");
                                }else {
                                    new AlertDialog.Builder(FolderActivity.this)
                                                .setTitle("确定要删除吗？")
                                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        new Thread(){
                                                            public void run(){
                                                                dbManager.deleteFolder(folderBean.getWhichFolder());
                                                            }
                                                        }.start();
                                                        Log.e("TAG", "删除finalSelectedPosition="+finalSelectedPosition);
                                                        folderList.remove(finalSelectedPosition);
//                                                        adapter.notifyItemChanged(finalSelectedPosition);
                                                        adapter.notifyDataSetChanged();
                                                        CommentUtils.showToast(FolderActivity.this,"删除成功！");
//                                                        updateListData();

                                                    }
                                                })
                                                .setNegativeButton("取消", null)
                                                .show();


                                }
                            }
                        });

                    }
                }.start();

                break;

        }
        return super.onContextItemSelected(item);
    }




}
