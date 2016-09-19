/*
 * Copyright 2016 Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.towatt.charge.recodenote.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.towatt.charge.recodenote.ClockActivity;
import com.towatt.charge.recodenote.R;
import com.towatt.charge.recodenote.bean.FolderBean;
import com.towatt.charge.recodenote.bean.RecordBean;
import com.towatt.charge.recodenote.db.DBManager;
import com.towatt.charge.recodenote.listener.OnItemClickListener;
import com.towatt.charge.recodenote.manager.MediaManager;
import com.towatt.charge.recodenote.utils.CommentUtils;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuAdapter;

import java.io.File;
import java.util.List;

/**
 * Created by YOLANDA on 2016/7/22.
 */
public class MenuAdapter extends SwipeMenuAdapter{

    private Context mContext;
    private List<RecordBean> recordBeanList;
    private OnItemClickListener mOnItemClickListener;
    private boolean cbVisibility;
    private int selectedPosition;

    private View lastView;

    public boolean isCbVisibility() {
        return cbVisibility;
    }

    public void setCbVisibility(boolean cbVisibility) {
        this.cbVisibility = cbVisibility;
        Log.e("TAG", "cbVisibility="+cbVisibility);
        notifyDataSetChanged();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    private int position;

    private DBManager dbManager;
    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
    public MenuAdapter(Context mContext, List<RecordBean> recordBeanList) {
        this.mContext = mContext;
        this.recordBeanList = recordBeanList;

        dbManager=new DBManager(mContext);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        final int finalPosition=position;
        final RecordHolder recordHolder= (RecordHolder) holder;
        RecordBean recordBean = recordBeanList.get(position);
        final RecordBean finalRecordBean=recordBean;
        recordHolder.tv_create_time.setText(CommentUtils.longToYMDHMS(recordBean.getCreateDate()));
        recordHolder.tv_record_name.setText(recordBean.getName());
        recordHolder.id_time.setText(CommentUtils.longToHMS(recordBean.getDuration())+ "\"");
        long clockTime = recordBean.getClockTime();
        if(clockTime!=0){
            if(clockTime>System.currentTimeMillis()&&recordBean.getIsAlert()!=0){
                recordHolder.tv_clock_time.setTextColor(mContext.getResources().getColor(R.color.red));
            }else{
                recordHolder.tv_clock_time.setTextColor(mContext.getResources().getColor(R.color.gray));
            }

            recordHolder.tv_clock_time.setText (CommentUtils.longToYMDHMS(recordBean.getClockTime()));
        }else{
            recordHolder.tv_clock_time.setTextColor(mContext.getResources().getColor(R.color.gray));
            recordHolder.tv_clock_time.setText ("未设置");
        }

        if(cbVisibility){
            recordHolder.cb_select.setVisibility(View.VISIBLE);
            recordHolder.cb_select.setChecked(recordBean.isCheck());
        }else{
            recordHolder.cb_select.setVisibility(View.GONE);
        }
        Animation alphaAnimation1 = AnimationUtils.loadAnimation(
                mContext, R.anim.myanim);
        alphaAnimation1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                recordBeanList.get(finalPosition).setShake(false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        if(recordBean.isShake()){

            recordHolder.itemView.setAnimation(alphaAnimation1);
            alphaAnimation1.start();

//                recordBean.setShake(false);
            recordBeanList.set(position,recordBean);
        }else{
            recordHolder.itemView.clearAnimation();
        }


        final RecordHolder finalHolder = recordHolder;
        recordHolder.id_recorder_length.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lastView!=null){
                    lastView.clearAnimation();
                    lastView.setBackgroundResource(R.drawable.adj);
                }

                finalHolder.mAnimView.setBackgroundResource(R.drawable.play_anim);
                lastView =finalHolder.mAnimView;
                AnimationDrawable anim = (AnimationDrawable) finalHolder.mAnimView.getBackground();
                anim.start();
                     /* 得到被点击的文件 */
                File playfile = new File(finalRecordBean.getStorePosition());
                    /* 播放 */
                //播放音频
                MediaManager.playSound(playfile.getAbsolutePath() , new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        finalHolder.mAnimView.clearAnimation();
                        finalHolder.mAnimView.setBackgroundResource(R.drawable.adj);
                    }
                });
            }
        });

        recordHolder.itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                    selectedPosition=position;
                    menu.add(0, 1, 0, "播放");
//                    menu.add(0,2,0,"删除");
                    menu.add(0, 2, 0, "重命名");
                    menu.add(0,3,0,"取消提醒");
                    menu.add(0,4,0,"批量删除");
                }
        });
        recordHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mOnItemClickListener.onItemClick(finalPosition);


                if(cbVisibility){

                    finalHolder.cb_select.toggle();
                    Log.e("TAG", "viewHolder.cb_select.isChecked()="+finalHolder.cb_select.isChecked());
                    if(finalHolder.cb_select.isChecked()){
                        recordBeanList.get(finalPosition).setCheck(true);
                    }else{
                        recordBeanList.get(finalPosition).setCheck(false);
                    }
                }else{

                        Intent intent = new Intent(mContext, ClockActivity.class);
                        intent.putExtra("createName",recordBeanList.get(finalPosition).getCreateName());
                        FolderBean folderBean = dbManager.queryFolderByWhich(recordBeanList.get(finalPosition).getWhichFolder());
                        intent.putExtra("folderName",folderBean.getFolderName());
                        mContext.startActivity(intent);
                    }
            }
        });
    }

    @Override
    public int getItemCount() {
        return recordBeanList == null ? 0 : recordBeanList.size();
    }

    @Override
    public View onCreateContentView(ViewGroup parent, int viewType) {
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_record, parent, false);
    }

    @Override
    public RecyclerView.ViewHolder onCompatCreateViewHolder(View realContentView, int viewType) {
        return new RecordHolder(realContentView);
    }


    class RecordHolder extends RecyclerView.ViewHolder{
        public TextView tv_record_name;
        public TextView tv_create_time;
        public TextView tv_duration;
        public TextView id_time;
        public ImageView iv_clock;

        TextView tv_clock_time;
        FrameLayout id_recorder_length;
        View mAnimView;//播放声音的动画


        CheckBox cb_select;


        public RecordHolder(View itemView) {
            super(itemView);
            tv_create_time= (TextView) itemView.findViewById(R.id.tv_create_time);
            tv_record_name= (TextView) itemView.findViewById(R.id.tv_record_name);
            tv_clock_time= (TextView) itemView.findViewById(R.id.tv_clock_time);
            id_time= (TextView) itemView.findViewById(R.id.id_time);
            id_recorder_length= (FrameLayout) itemView.findViewById(R.id.id_recorder_length);
            mAnimView= itemView.findViewById(R.id.id_recorder_anim);

            cb_select= (CheckBox) itemView.findViewById(R.id.cb_select);

        }
    }

}
