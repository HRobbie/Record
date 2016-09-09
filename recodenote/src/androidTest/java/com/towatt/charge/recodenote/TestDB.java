package com.towatt.charge.recodenote;

import android.content.Context;
import android.test.InstrumentationTestCase;

import com.towatt.charge.recodenote.bean.RecordBean;
import com.towatt.charge.recodenote.db.DBManager;

/**
 * user:HRobbie
 * Date:2016/8/22
 * Time:19:58
 * 邮箱：hwwyouxiang@163.com
 * Description:Page Function.
 */
public class TestDB extends InstrumentationTestCase {
    private Context context;
    private void test1(){
        DBManager dbManager = new DBManager(context);
        RecordBean recordBean = new RecordBean(1, "1", 1, 1, "1");
        dbManager.add(recordBean);
    }
}
