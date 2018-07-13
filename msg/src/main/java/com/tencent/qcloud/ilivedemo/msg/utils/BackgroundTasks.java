/**
 * Created by valexhuang on 2018/3/26.
 */
package com.tencent.qcloud.ilivedemo.msg.utils;

import android.os.Handler;

public class BackgroundTasks {

    private Handler mHandler = new Handler();


    public void runOnUiThread(Runnable runnable) {
        mHandler.post(runnable);
    }


    private static BackgroundTasks instance;

    public static BackgroundTasks getInstance() {
        return instance;
    }


    // 需要在主线程中初始化
    public static void initInstance() {
        instance = new BackgroundTasks();
    }


}
