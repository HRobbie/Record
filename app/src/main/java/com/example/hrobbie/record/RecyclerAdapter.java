package com.example.hrobbie.record;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

/**
 * Description:RecyclerView 适配器
 * User: xjp
 * Date: 2015/6/8
 * Time: 10:15
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {


    private Context context;
    private List<Bean> list;
    private Resources res;
    private OnItemClickListener listener;
    private boolean cbVisibility;

    public boolean isCbVisibility() {
        return cbVisibility;
    }

    public void setCbVisibility(boolean cbVisibility) {
        this.cbVisibility = cbVisibility;
        notifyDataSetChanged();
    }

    public RecyclerAdapter(Context context, List<Bean> list) {
        this.context = context;
        this.list = list;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Bean bean = list.get(position);
        holder.text.setText(bean.getText()+"");
        if(cbVisibility){
            holder.checkbox.setVisibility(View.VISIBLE);
        }else{
            holder.checkbox.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != listener)
                    listener.onItemClick(position, bean);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size() ;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        private CheckBox checkbox;
        private TextView text;

        public MyViewHolder(View view) {
            super(view);

            checkbox= (CheckBox) view.findViewById(R.id.checkbox);
            text= (TextView) view.findViewById(R.id.text);
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
