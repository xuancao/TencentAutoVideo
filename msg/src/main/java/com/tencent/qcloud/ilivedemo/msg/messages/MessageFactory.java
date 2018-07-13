package com.tencent.qcloud.ilivedemo.msg.messages;


import com.tencent.TIMMessage;

/**
 * 消息工厂
 */
public class MessageFactory {

    private MessageFactory() {}

    /**
     * 消息工厂方法
     */
    public static Message getMessage(TIMMessage message){
        switch (message.getElement(0).getType()){
            case Text:
            case Face:
                return new TextMessage(message);
            case Image:
                return new ImageMessage(message);
//            case Sound:
//                return new VoiceMessage(message);
//            case Video:
//                return new VideoMessage(message);
//            case GroupTips:
//                return new GroupTipMessage(message);
//            case File:
//                return new FileMessage(message);
            case Custom:
                return new CustomMessage(message);
//            case UGC:
//                return new UGCMessage(message);
            default:
                return null;
        }
    }



}
