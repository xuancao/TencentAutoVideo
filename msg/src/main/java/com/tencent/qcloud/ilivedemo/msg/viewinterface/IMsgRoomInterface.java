package com.tencent.qcloud.ilivedemo.msg.viewinterface;

import com.tencent.TIMMessage;

/**
 * Created by Administrator on 2018/4/15.
 */

public interface IMsgRoomInterface {
    // 进入房间成功
    void onEnterRoom();

    // 进房间失败
    void onEnterRoomFailed(String module, int errCode, String errMsg);

    // 退出房间成功
    void onQuitRoomSuccess();

    // 退出房间失败
    void onQuitRoomFailed(String module, int errCode, String errMsg);

    // 房间断开
    void onRoomDisconnect(int errCode, String errMsg);

    //发送消息成功
    void onSendMsgSuccess(TIMMessage message);

    // 发送消息失败
    void onSendMsgFailed(String module, int errCode, String errMsg);

    //处理异常事件
    void onException(int exceptionId, int errCode, String errMsg);

    //视频请求失败
    void onVedioRequestErr(int result, String errMsg);

}
