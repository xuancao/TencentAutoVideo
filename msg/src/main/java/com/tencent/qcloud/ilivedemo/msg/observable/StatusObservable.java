package com.tencent.qcloud.ilivedemo.msg.observable;

/**
 * Created by valexhuang on 2018/4/10.
 */

import com.tencent.ilivesdk.core.ILiveLoginManager;

import java.util.LinkedList;

/**
 * 状态观察者
 */
public class StatusObservable implements ILiveLoginManager.TILVBStatusListener {

    // 消息监听链表
    private LinkedList<ILiveLoginManager.TILVBStatusListener> listObservers = new LinkedList<>();
    // 句柄
    private static StatusObservable instance;


    public static StatusObservable getInstance() {
        if (null == instance) {
            synchronized (StatusObservable.class) {
                if (null == instance) {
                    instance = new StatusObservable();
                }
            }
        }
        return instance;
    }


    // 添加观察者
    public void addObserver(ILiveLoginManager.TILVBStatusListener listener) {
        if (!listObservers.contains(listener)) {
            listObservers.add(listener);
        }
    }

    // 移除观察者
    public void deleteObserver(ILiveLoginManager.TILVBStatusListener listener) {
        listObservers.remove(listener);
    }

    // 获取观察者数量
    public int getObserverCount() {
        return listObservers.size();
    }

    @Override
    public void onForceOffline(int error, String message) {
        // 拷贝链表
        LinkedList<ILiveLoginManager.TILVBStatusListener> tmpList = new LinkedList<>(listObservers);
        for (ILiveLoginManager.TILVBStatusListener listener : tmpList) {
            listener.onForceOffline(error, message);
        }
    }
}