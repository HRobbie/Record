package com.towatt.charge.recodenote.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.towatt.charge.recodenote.FolderActivity;
import com.towatt.charge.recodenote.R;

public class SplashActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView iv_icon;
    private static final int MESSAGE_DELAY = 1;
    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case MESSAGE_DELAY:
                    Intent intent = new Intent(SplashActivity.this, FolderActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initView();

        checkInternetPermission();

    }

    private void initView() {
        iv_icon = (ImageView)findViewById(R.id.iv_icon);
        iv_icon.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_icon:
//                Intent intent = new Intent();
//                intent.setClassName("com.mossle.android", "com.mossle.android.service.NotificationService");
//                startService(intent);
                break;
        }

    }


    private void checkInternetPermission() {

        Dexter.checkPermission(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                handler.sendEmptyMessageDelayed(MESSAGE_DELAY,1000);
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                finish();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }, Manifest.permission.INTERNET);
    }
}
