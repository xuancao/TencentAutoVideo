package com.tencent.qcloud.ilivedemo.msg;

import android.app.Application;

import com.tencent.ilivesdk.ILiveSDK;
import com.tencent.ilivesdk.core.ILiveRoomConfig;
import com.tencent.ilivesdk.core.ILiveRoomManager;
import com.tencent.qalsdk.sdk.MsfSdkUtils;
import com.tencent.qcloud.ilivedemo.msg.observable.MessageObservable;
import com.tencent.qcloud.ilivedemo.msg.utils.BackgroundTasks;
import com.tencent.qcloud.ilivedemo.msg.utils.Constants;


/**
 * Created by valexhuang on 2018/3/26.
 */

public class DemoApplication extends Application {

    private static Application application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

        // 判断仅在主线程进行初始化
        if (MsfSdkUtils.isMainProcess(this)) {
            // 初始化iLiveSDK
            ILiveSDK.getInstance().initSdk(this, Constants.SDK_APPID, Constants.ACCOUNT_TYPE);
            // 初始化iLiveSDK房间管理模块并设置消息监听
            ILiveRoomManager.getInstance().init(new ILiveRoomConfig().messageListener(MessageObservable.getInstance()));
            //初始化UI处理线程
            BackgroundTasks.initInstance();
        }
    }

    public static Application getApplication(){
        return application;
    }
}
