/*
 * Copyright (C) 2015 Karumi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.towatt.charge.recodenote.application;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;

import com.karumi.dexter.Dexter;
import com.towatt.charge.recodenote.receiver.AutoBoot;

import org.xutils.BuildConfig;
import org.xutils.x;

/**
 * Sample application that initializes the Dexter library.
 */
public class SampleApplication extends Application {

  @Override public void onCreate() {
    super.onCreate();
    Dexter.initialize(this);

//    new Thread(){
//        public void run(){
//          IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
//
//          AutoBoot receiver = new AutoBoot();
//          registerReceiver(receiver, filter);
//        }
//    }.start();
    IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);

    AutoBoot receiver = new AutoBoot();
    registerReceiver(receiver, filter);

    //注册亮屏广播
    IntentFilter filter1 = new IntentFilter();
    filter1.addAction(Intent.ACTION_SCREEN_ON);
    registerReceiver(receiver, filter1);


    //初始化XUtils3
    x.Ext.init(this);
    //设置debug模式
    x.Ext.setDebug(BuildConfig.DEBUG);
  }
}
