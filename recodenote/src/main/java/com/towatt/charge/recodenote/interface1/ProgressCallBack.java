package com.towatt.charge.recodenote.interface1;

import org.xutils.common.Callback;

/**
 * user:HRobbie
 * Date:2016/11/25
 * Time:13:33
 * 邮箱：hwwyouxiang@163.com
 * Description:Page Function.
 */
public abstract class ProgressCallBack<ResultType> implements Callback.ProgressCallback<ResultType>{
    @Override
    public void onWaiting() {

    }

    @Override
    public void onStarted() {

    }

    @Override
    public void onLoading(long l, long l1, boolean b) {

    }

    @Override
    public void onSuccess(ResultType resultType) {

    }

    @Override
    public void onError(Throwable throwable, boolean b) {

    }

    @Override
    public void onCancelled(CancelledException e) {

    }

    @Override
    public void onFinished() {

    }
}
