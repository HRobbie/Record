package com.towatt.charge.recodenote.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.RemoteException;

import com.towatt.charge.recodenote.IMusicPlayerService;
import com.towatt.charge.recodenote.bean.RecordBean;
import com.towatt.charge.recodenote.db.DBManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/12/25.
 */
public class MusicPlayerService extends Service {


    /**
     * 当打开一个视频成功的时候，也就是播放起来了的时候，发这个广播
     */
    public static final String OPENAUDIOCOMPLETE = "com.atguigu.mobilepalyer_openaudiocomplete";
    /**
     * 音频列表
     */
//    private ArrayList<MediaItem> mediaItems;

    /**
     * 顺序播放
     */
    public static  final int REPEATE_NORMAL = 1;

    /**
     * 单曲循环
     */
    public static  final int REPEATE_SINGLE = 2;

    /**
     * 全部播放
     */
    public static  final int REPEATE_ALL = 3;

    /**
     * 播放模式，默认是顺序播放
     */
    private int playmode = REPEATE_NORMAL;

    private ArrayList<RecordBean> mMusicList=new ArrayList<>();
    private IMusicPlayerService.Stub mIBinder = new IMusicPlayerService.Stub(){
        MusicPlayerService service = MusicPlayerService.this;

        @Override
        public void start() throws RemoteException {
            service.start();

        }

        @Override
        public void pause() throws RemoteException {
            service.pause();
        }

        @Override
        public void openAudio(int position) throws RemoteException {
            service.openAudio(position);
        }

        @Override
        public void openAudioFile(String path) throws RemoteException {
            service.openAudioFile(path);
        }

        @Override
        public void next() throws RemoteException {
            service.next();
        }

        @Override
        public void pre() throws RemoteException {
            service.pre();
        }

//        @Override
        public int getPlaymode() throws RemoteException {
//            return service.getPlaymode();
            return 0;
        }

        @Override
        public void setPlaymode(int playmode) throws RemoteException {
            service.setPlaymode(playmode);
        }

        @Override
        public String getAudioName() throws RemoteException {
            return service.getAudioName();
        }

        @Override
        public String getArtist() throws RemoteException {
            return service.getArtist();
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return service.isPlaying();
        }

        @Override
        public int getDuration() throws RemoteException {
            return service.getDuration();
        }

        @Override
        public int getCurrentPosition() throws RemoteException {
            return service.getCurrentPosition();
        }

        @Override
        public void seekTo(int position) throws RemoteException {
            service.seekTo(position);
        }

        @Override
        public void nofityChang(String action,String flag) throws RemoteException {
            service.nofityChang(action,flag);
        }

        @Override
        public String getAudioPath() throws RemoteException {
            return service.getAudioPath();
        }
    };

    /**
     * 得到音频的播放地址
     * @return
     */
    private String getAudioPath() {
//        if(mediaItem != null){
//            return  mediaItem.getData();
//        }
        return "";
    }

    private void seekTo(int position) {
        if(mediaPlayer != null){
            mediaPlayer.seekTo(position);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mIBinder;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getData();
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        playmode= CacheUtils.getPlaymode(this,"playmode");
//        getData();

    }

    /**
     * 得到视频的数据
     */
    private void getData() {

//        new Thread() {
//            @Override
//            public void run() {
//                super.run();
                DBManager dbManager = new DBManager(getApplicationContext());
                List<RecordBean> recordBeanList = dbManager.queryAll();
                for (int i = 0; i < recordBeanList.size(); i++) {
//                    mMusicList.add(recordBeanList.get(i));
                    mMusicList.addAll(recordBeanList);
                }




//            }
//        }.start();
    }

    /**
     * 播放音乐
     */
    private void start(){
        mediaPlayer.start();
//
//        int icon = R.drawable.notification_music_playing;
//        String title = "正在播放："+getAudioName();
//
//        long systemTim = System.currentTimeMillis();
//        //弹出状态
//        Notification notification = new Notification(icon,title,systemTim);
//        notification.flags = Notification.FLAG_ONGOING_EVENT;//设置属性，点击不消失
//
//        //点击的时候启动AudioPlayerActivity
//        Intent intent = new Intent(this,AudioPlayerActivity.class);
//        intent.putExtra("notification",true);//标识从状态进入音乐播放器
//
//
//        PendingIntent contentIntent = PendingIntent.getActivity(this,0,intent,0);
//        notification.setLatestEventInfo(this,"321音乐",title,contentIntent);
//
//        startForeground(1,notification);




    }

    /**
     * 暂停音乐
     */
    private void pause(){
        mediaPlayer.pause();
//        stopForeground(true);//把音乐播放状态栏消掉
    }

//    private  MediaItem mediaItem;
    private int position;
    private MediaPlayer mediaPlayer;

    /**
     * 根据位置播放对应的音频
     * @param audioPosition
     */
    private void openAudio(int audioPosition){
//        mediaItem =  mediaItems.get(audioPosition);
        position = audioPosition;
//
//
        if(mediaPlayer != null){
            mediaPlayer.reset();
            mediaPlayer = null;
        }
//
        mediaPlayer = new MediaPlayer();
//        mediaPlayer = MediaPlayer.create(this, R.raw.live);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
//                start();
                nofityChang(OPENAUDIOCOMPLETE,"prepare");
            }
        });
//        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
//            @Override
//            public boolean onError(MediaPlayer mp, int what, int extra) {
//                next();
//                return true;
//            }
//        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
//                next();
                nofityChang(OPENAUDIOCOMPLETE,"complete");
            }
        });
//
//
        try {
            mediaPlayer.setDataSource(mMusicList.get(position).getStorePosition());
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
//
//        setInsetPlaymode();
    }
    /**
     * 根据位置播放对应的音频
     * @param
     */
    private void openAudioFile(String path){
//        mediaItem =  mediaItems.get(audioPosition);

//
//
        if(mediaPlayer != null){
            mediaPlayer.reset();
            mediaPlayer = null;
        }
//
        mediaPlayer = new MediaPlayer();
//        mediaPlayer = MediaPlayer.create(this, R.raw.live);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
//                start();
                nofityChang(OPENAUDIOCOMPLETE,"prepare");
            }
        });
//        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
//            @Override
//            public boolean onError(MediaPlayer mp, int what, int extra) {
//                next();
//                return true;
//            }
//        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
//                next();
                nofityChang(OPENAUDIOCOMPLETE,"complete");
            }
        });
//
//
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
//
//        setInsetPlaymode();
    }

    /**
     * 发送广播
     * @param action
     */
    private void nofityChang(String action,String flag) {
        Intent intent = new Intent();
        intent.putExtra("flag",flag);
        intent.setAction(action);
        sendBroadcast(intent);
    }

    /**
     * 播放下一首歌曲
     */
    private void next(){
        setNextPosition();//设置位置
        setNextUrl();

    }

    /**
     * 根据位置播放对应的音频
     */
    private void setNextUrl() {
//        int playmode = getPlaymode();
//
//        mediaPlayer.setLooping(false);
//
//        if(playmode == MusicPlayerService.REPEATE_NORMAL){
//
//            if(position <= mediaItems.size()-1){
//                openAudio(position);//最后一个可以播放
//            }else{
//                position = mediaItems.size()-1;
//            }
//
//        }else if(playmode ==MusicPlayerService.REPEATE_SINGLE){
//            mediaPlayer.setLooping(true);
////            openAudio(position);//最后一个可以播放
//            if(position <= mediaItems.size()-1){
//                openAudio(position);//最后一个可以播放
//            }else{
//                position = mediaItems.size()-1;
//            }
//
//        }else if(playmode ==MusicPlayerService.REPEATE_ALL){
//            openAudio(position);//最后一个可以播放
//        }else{
//            if(position <= mediaItems.size()-1){
//                openAudio(position);//最后一个可以播放
//            }else{
//                position = mediaItems.size()-1;
//            }
//        }
    }

    /**
     * 设置下一个的播放位置
     */
    private void setNextPosition() {
//        int playmode = getPlaymode();
//
//        if(playmode == MusicPlayerService.REPEATE_NORMAL){
//            position ++;
//
//        }else if(playmode ==MusicPlayerService.REPEATE_SINGLE){
//            mediaPlayer.setLooping(true);
//            //不用做任何设置
//            position ++;
//        }else if(playmode ==MusicPlayerService.REPEATE_ALL){
//            position ++;
//            if(position >mediaItems.size()-1){
//                position = 0;
//            }
//
//        }else{
//            position ++;
//        }
    }

    private void setInsetPlaymode() {
//        int playmode = getPlaymode();
//        mediaPlayer.setLooping(false);
//        if(playmode == MusicPlayerService.REPEATE_NORMAL){
//
//        }else if(playmode ==MusicPlayerService.REPEATE_SINGLE){
//            mediaPlayer.setLooping(true);
//            //不用做任何设置
//        }else if(playmode ==MusicPlayerService.REPEATE_ALL){
//
//        }else{
//        }
    }


    /**
     * 播放上一首
     */
    private void pre(){
//        setPrePosition();//设置位置
//        setPreUrl();
    }

    private void setPreUrl() {
//        int playmode = getPlaymode();
//        mediaPlayer.setLooping(false);
//
//        if(playmode == MusicPlayerService.REPEATE_NORMAL){
//
//            if(position >= 0){
//                openAudio(position);//最后一个可以播放
//            }else{
//                position = 0;
//            }
//
//        }else if(playmode ==MusicPlayerService.REPEATE_SINGLE){
//            mediaPlayer.setLooping(true);
////            openAudio(position);//最后一个可以播放
//            if(position >= 0){
//                openAudio(position);//最后一个可以播放
//            }else{
//                position = 0;
//            }
//        }else if(playmode ==MusicPlayerService.REPEATE_ALL){
//            openAudio(position);//最后一个可以播放
//        }else{
//            if(position >= 0){
//                openAudio(position);//最后一个可以播放
//            }else{
//                position = 0;
//            }
//        }
    }

    private void setPrePosition() {
//        int playmode = getPlaymode();
//
//        if(playmode == MusicPlayerService.REPEATE_NORMAL){
//            position --;
//
//        }else if(playmode ==MusicPlayerService.REPEATE_SINGLE){
//            //不用做任何设置
//            position --;
//        }else if(playmode ==MusicPlayerService.REPEATE_ALL){
//            position --;
//            if(position <0){
//                position = mediaItems.size()-1;
//            }
//
//        }else{
//            position --;
//        }
    }

    /**
     * 得到播放模式
     * @return
     */
//    private int getPlaymode(){
//        return playmode;
//    }


    /**
     * 设置播放模式
     * @param playmode
     */
    private void setPlaymode(int playmode){
//        this.playmode = playmode;
//        CacheUtils.setPlaymode(this,"playmode",playmode);

    }

    /**
     * 得到音频的名称
     * @return
     */
    private String getAudioName(){
//        if(mediaItem != null){
//            return mediaItem.getTitle();
//        }
        return "";
    }

    /**
     * 得到艺术家的名称
     * @return
     */
    private String getArtist(){
//        if(mediaItem != null){
//            return mediaItem.getArtist();
//        }
        return "";
    }

    /**
     * 是否在播放
     * @return
     */
    private boolean isPlaying(){
        if(mediaPlayer != null){
            return mediaPlayer.isPlaying();
        }
        return false;
    }


    /**
     * 得到播放时长
     * @return
     */
    private int getDuration(){
        if(mediaPlayer != null){
            return  mediaPlayer.getDuration();
        }
        return 0;
    }

    /**
     * 得到当前播放进度
     * @return
     */
    private int getCurrentPosition(){
        if(mediaPlayer != null){
            return  mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void onDestroy() {
        mMusicList.clear();
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer=null;
        super.onDestroy();

    }
}
