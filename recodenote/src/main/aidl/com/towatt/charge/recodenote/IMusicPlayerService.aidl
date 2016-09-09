// IMusicPlayerService.aidl
package com.towatt.charge.recodenote;

// Declare any non-default types here with import statements

interface IMusicPlayerService {
 /**
         * 播放音乐
         */
         void start();

        /**
         * 暂停音乐
         */
         void pause();

        /**
         * 根据位置播放对应的音频
         * @param position
         */
         void openAudio(int position);


         void openAudioFile(String path);

        /**
         * 播放下一首歌曲
         */
         void next();


        /**
         * 播放上一首
         */
         void pre();

        /**
         * 得到播放模式
         * @return
         */
         int getPlaymode();


        /**
         * 设置播放模式
         * @param playmode
         */
         void setPlaymode(int playmode);

        /**
         * 得到音频的名称
         * @return
         */
         String getAudioName();

        /**
         * 得到艺术家的名称
         * @return
         */
         String getArtist();

        /**
         * 是否在播放
         * @return
         */
         boolean isPlaying();


        /**
         * 得到播放时长
         * @return
         */
         int getDuration();

        /**
         * 得到当前播放进度
         * @return
         */
         int getCurrentPosition();

          /**
              根据传入的位置，定位到对应音频的位置
              */
         void seekTo(int position);

         void nofityChang(String action,String flag);

         String getAudioPath();
}
