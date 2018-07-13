package com.tencent.qcloud.ilivedemo.msg.observable;

/**
 * Created by valexhuang on 2018/4/10.
 */

import com.tencent.TIMMessage;
import com.tencent.TIMMessageListener;

import java.util.LinkedList;
import java.util.List;

/**
 * 消息观察者
 */
public class MessageObservable implements TIMMessageListener {
    // 消息监听链表
    private LinkedList<TIMMessageListener> listObservers = new LinkedList<>();
    // 句柄
    private static MessageObservable instance;


    public static MessageObservable getInstance() {
        if (null == instance) {
            synchronized (MessageObservable.class) {
                if (null == instance) {
                    instance = new MessageObservable();
                }
            }
        }
        return instance;
    }

    // 添加观察者
    public void addObserver(TIMMessageListener listener) {
        if (!listObservers.contains(listener)) {
            listObservers.add(listener);
        }
    }

    // 移除观察者
    public void deleteObserver(TIMMessageListener listener) {
        listObservers.remove(listener);
    }

    @Override
    public boolean onNewMessages(List<TIMMessage> list) {
        LinkedList<TIMMessageListener> tmpList = listObservers;
        // 拷贝链表
//        LinkedList<TIMMessageListener> tmpList = new LinkedList<>(listObservers);
        for (TIMMessageListener listener : tmpList) {
            listener.onNewMessages(list);
        }
        return false;
    }
}