package com.towatt.charge.recodenote.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.support.design.widget.AppBarLayout;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.towatt.charge.recodenote.ClockActivity;
import com.towatt.charge.recodenote.R;
import com.towatt.charge.recodenote.bean.RecordBean;
import com.towatt.charge.recodenote.db.DBManager;
import com.towatt.charge.recodenote.manager.MediaManager;
import com.towatt.charge.recodenote.utils.CommentUtils;
import com.towatt.charge.recodenote.view.SwipeView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * user:HRobbie
 * Date:2016/8/26
 * Time:11:50
 * 邮箱：hwwyouxiang@163.com
 * Description:Page Function.
 */
public class RecordAdapter extends RecyclerView.Adapter{
    private Context mContext;
    private List<RecordBean> recordBeanList;
    private OnItemClickListener listener;
    private ArrayList<SwipeView> unClosedSwipeView = new ArrayList<>();
    private boolean cbVisibility;
    private int selectedPosition;

    private AppBarLayout appbar;
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

    public RecordAdapter(Context mContext, List<RecordBean> recordBeanList, boolean cbVisibility) {
        this.mContext = mContext;
        this.recordBeanList = recordBeanList;


        dbManager=new DBManager(mContext);
    }
    private int position;

    private DBManager dbManager;
    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View view;
//        if(cbVisibility){
//            view= LayoutInflater.from(mContext).inflate(R.layout.item, parent, false);
//        }else{
//
//            view= LayoutInflater.from(mContext).inflate(R.layout.item1, parent, false);
//        }

//        View view = View.inflate(mContext, R.layout.item, null);

        View view= LayoutInflater.from(mContext).inflate(R.layout.item, parent, false);
        return new RecordHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final int finalPosition=position;
        RecordHolder recordHolder= (RecordHolder) holder;
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
//            AlphaAnimation alphaAnimation1 = new AlphaAnimation(0.1f, 1.0f);
//            alphaAnimation1.setDuration(1000);
//            alphaAnimation1.setRepeatCount(5);
//            alphaAnimation1.setRepeatMode(Animation.REVERSE);
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
                finalHolder.itemView.clearAnimation();


                finalHolder.mAnimView.setBackgroundResource(R.drawable.play_anim);
                AnimationDrawable anim = (AnimationDrawable) finalHolder.mAnimView.getBackground();
                anim.start();
                     /* 得到被点击的文件 */
                File playfile = new File(finalRecordBean.getStorePosition());
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



        recordHolder.swipeView.setOnSwipeStatusChangeListener(new SwipeView.OnSwipeStatusChangeListener() {
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
        });

        recordHolder.swipeView.fastClose();


        recordHolder.delete.setText("删除");

        recordHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unClosedSwipeView.clear();
                deleteRecordFile(finalPosition);
            }
        });

        recordHolder.ll_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cbVisibility){

                    finalHolder.cb_select.toggle();
                    Log.e("TAG", "viewHolder.cb_select.isChecked()="+finalHolder.cb_select.isChecked());
                    if(finalHolder.cb_select.isChecked()){
                        recordBeanList.get(finalPosition).setCheck(true);
                    }else{
                        recordBeanList.get(finalPosition).setCheck(false);
                    }
                }else{
                    if (unClosedSwipeView.size() > 0) {
                        closeAllOpenedSwipeView();
                    } else {
                        Intent intent = new Intent(mContext, ClockActivity.class);
                        intent.putExtra("createName",recordBeanList.get(finalPosition).getCreateName());
                        mContext.startActivity(intent);
                    }
                }


            }
        });

        recordHolder.ll_content.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                if (unClosedSwipeView.size() > 0) {
                    closeAllOpenedSwipeView();
                } else {
                    selectedPosition=finalPosition;
                    menu.add(0, 1, 0, "播放");
                    menu.add(0, 2, 0, "重命名");
                    menu.add(0,3,0,"取消提醒");
                    menu.add(0,4,0,"批量删除");
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return recordBeanList.size();
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

        SwipeView swipeView;
        TextView delete;
        LinearLayout ll_content;

        public RecordHolder(View itemView) {
            super(itemView);
            tv_create_time= (TextView) itemView.findViewById(R.id.tv_create_time);
            tv_record_name= (TextView) itemView.findViewById(R.id.tv_record_name);
            tv_clock_time= (TextView) itemView.findViewById(R.id.tv_clock_time);
            id_time= (TextView) itemView.findViewById(R.id.id_time);
            id_recorder_length= (FrameLayout) itemView.findViewById(R.id.id_recorder_length);
            mAnimView= itemView.findViewById(R.id.id_recorder_anim);

            cb_select= (CheckBox) itemView.findViewById(R.id.cb_select);
            swipeView= (SwipeView) itemView.findViewById(R.id.swipeView);
            delete= (TextView) itemView.findViewById(R.id.delete);
            ll_content= (LinearLayout) itemView.findViewById(R.id.ll_content);


//            if(cbVisibility){
//                cb_select.setVisibility(View.GONE);
//            }else{
//                cb_select.setVisibility(View.VISIBLE);
//            }


        }

//        @Override
//        public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
//            //1.通过手动添加来配置上下文菜单选项
//            menu.add(0, 1, 0, "删除");
//            menu.add(0, 2, 0, "重命名");
//            menu.add(0,3,0,"取消提醒");
//        }



    }

    /**
     * 内部接口回调方法
     */
    public interface OnItemClickListener {
        void onItemClick(int position, Object object);
    }

    /**
     * 设置监听方法
     *
     * @param listener
     */
    public void setOnItemClickListener(OnItemClickListener listener) {

        this.listener = listener;
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        holder.itemView.setOnLongClickListener(null);
        super.onViewRecycled(holder);
    }


    /**
     * 删除录音文件
     * @param position
     */
    private void deleteRecordFile(final int position) {
    /* 得到被点击的文件 */
        final File playfile = new File(recordBeanList.get(position).getStorePosition());
        final String createName = recordBeanList.get(position).getCreateName();
//        new AlertDialog.Builder(this)
//                .setTitle("确定要删除吗？")
//                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
        playfile.delete();
        dbManager.delete(createName);

        recordBeanList.remove(position);

        notifyDataSetChanged();
//                    }
//                })
//                .setNegativeButton("取消", null)
//                .show();
    }


    private void closeAllOpenedSwipeView() {
        for (int i = 0; i < unClosedSwipeView.size(); i++) {
            if (unClosedSwipeView.get(i).getSwipeStatus() != SwipeView.SwipeStatus.Close) {
                unClosedSwipeView.get(i).close();
            }
        }
    }



}
