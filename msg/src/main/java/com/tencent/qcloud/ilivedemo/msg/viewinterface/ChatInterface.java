package com.tencent.qcloud.ilivedemo.msg.viewinterface;

import com.tencent.TIMMessage;

import java.util.List;

/**
 * 聊天界面的接口
 */
public interface ChatInterface{

    // 显示消息
    void showMessage(TIMMessage message);

    // 显示消息
    void showMessage(List<TIMMessage> messages);

    // 发送图片消息
    void sendImage();

    //发送照片消息
    void sendPhoto();

    // 发送文字消息
    void sendText();

    // 清除所有消息(离线恢复),并等待刷新
    void clearAllMessage();


}
