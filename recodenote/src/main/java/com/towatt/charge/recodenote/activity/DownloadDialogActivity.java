package com.towatt.charge.recodenote.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.towatt.charge.recodenote.R;
import com.towatt.charge.recodenote.bean.UpdateInfoBean;
import com.towatt.charge.recodenote.service.DownLoadService;
import com.towatt.charge.recodenote.utils.CacheUtils;
import com.towatt.charge.recodenote.utils.CommentUtils;
import com.towatt.charge.recodenote.utils.NetUrl;

import org.xutils.common.util.DensityUtil;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

@ContentView(R.layout.activity_download_dialog)
public class DownloadDialogActivity extends AppCompatActivity implements View.OnClickListener {
    @ViewInject(R.id.tv_version)
    private TextView tv_version;
    @ViewInject(R.id.tv_size)
    private TextView tv_size;
    @ViewInject(R.id.tv_content)
    private TextView tv_content;

    @ViewInject(R.id.btn_cancel)
    private Button btn_cancel;

    @ViewInject(R.id.btn_confirm)
    private Button btn_confirm;
    private UpdateInfoBean.DataBean data;
    private ImageOptions imageOptions;

    @ViewInject(R.id.iv_icon)
    private ImageView iv_icon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        x.view().inject(this);

        initData();
    }

    private void initData() {

        //图片大小
        //ImageView圆角半径
        // 如果ImageView的大小不是定义为wrap_content, 不要crop.
        //加载中默认显示图片
        //设置使用缓存
        //加载失败后默认显示图片
        imageOptions = new ImageOptions.Builder()
//                .setSize(DensityUtil.dip2px(40), DensityUtil.dip2px(40))//图片大小
                .setRadius(DensityUtil.dip2px(0))//ImageView圆角半径
                .setCrop(true)// 如果ImageView的大小不是定义为wrap_content, 不要crop.
                .setImageScaleType(ImageView.ScaleType.CENTER_CROP)
                .setLoadingDrawableId(R.mipmap.ic_launcher)//加载中默认显示图片
                .setUseMemCache(true)//设置使用缓存
                .setFailureDrawableId(R.mipmap.ic_launcher)//加载失败后默认显示图片
                .build();

        String result = CacheUtils.getInstance(this).getValue(NetUrl.updateUrl, "");
        if(result.contains("data")){
            UpdateInfoBean updateInfoBean = CommentUtils.getGson().fromJson(result, UpdateInfoBean.class);
            data = updateInfoBean.getData();
            String replace = data.getInformation().replace("\\n", "\n");
            tv_content.setText(replace);
            tv_size.setText(data.getSize()+"M");
            tv_version.setText("v"+ data.getVersion());

            x.image().bind(iv_icon,data.getImg(),imageOptions);

        }

        btn_confirm.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_cancel:
                finish();
                break;
            case R.id.btn_confirm:
                checkStorePromission();
                break;
        }
    }


    private void checkStorePromission() {
        Dexter.checkPermission(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                Intent intent=new Intent(DownloadDialogActivity.this,DownLoadService.class);
                intent.putExtra("download_url",data.getUrl());
                startService(intent);
                finish();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                CommentUtils.showToast(DownloadDialogActivity.this,"没有给予访问内存卡的权限，不能下载！");
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
}
