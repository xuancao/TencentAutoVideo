package com.tencent.qcloud.ilivedemo.msg.viewinterface;

/**
 * Created by valexhuang on 2018/3/27.
 */

public interface ILoginInterface {

    // 更新登录态
    void updateLoginState(boolean state);

    // 登录成功
    void onLoginSDKSuccess();

    // 登录失败
    void onLoginSDKFailed(String module, int errCode, String errMsg);


    // 登录成功
    void onLogoutSDKSuccess();

    // 登录失败
    void onLogoutSDKFailed(String module, int errCode, String errMsg);


}