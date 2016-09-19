package com.towatt.charge.recodenote.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.towatt.charge.recodenote.R;
import com.towatt.charge.recodenote.bean.FolderBean;
import com.towatt.charge.recodenote.db.DBManager;

import java.util.List;

/**
 * user:HRobbie
 * Date:2016/8/26
 * Time:11:50
 * 邮箱：hwwyouxiang@163.com
 * Description:Page Function.
 */
public class FolderAdapter extends RecyclerView.Adapter {
    private Context mContext;
    private List<FolderBean> folderBeanList;
    private OnItemClickListener listener;

    private int selectedPosition;

    public int getPosition() {
        return selectedPosition;
    }

    public void setPosition(int position) {
        this.selectedPosition = position;
    }




    public FolderAdapter(Context mContext, List<FolderBean> folderBeanList) {
        this.mContext = mContext;
        this.folderBeanList=folderBeanList;


        dbManager=new DBManager(mContext);
    }

    private DBManager dbManager;
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(mContext).inflate(R.layout.item_card_folder, parent, false);
        return new FolderHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final int finalPosition=position;
        FolderHolder folderHolder= (FolderHolder) holder;
        final FolderBean folderBean = folderBeanList.get(position);

        folderHolder.tv_folder_time.setText(folderBean.getFolderName());


        /**
         * 调用接口回调
         */
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listener)
                    listener.onItemClick(finalPosition, folderBean);
            }
        });
        holder.itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                selectedPosition=finalPosition;
                //1.通过手动添加来配置上下文菜单选项
                contextMenu.add(0, 1, 0, "删除");
                contextMenu.add(0, 2, 0, "重命名");
            }
        });
    }

    @Override
    public int getItemCount() {
        return folderBeanList.size();
    }


    class FolderHolder extends RecyclerView.ViewHolder{
        public ImageView iv_folder;

        TextView tv_folder_time;




        public FolderHolder(View itemView) {
            super(itemView);
            tv_folder_time= (TextView) itemView.findViewById(R.id.tv_folder_time);
            iv_folder= (ImageView) itemView.findViewById(R.id.iv_folder);







        }
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


}
