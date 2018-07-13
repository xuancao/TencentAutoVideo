package com.tencent.qcloud.ilivedemo.msg.presenter;

import android.util.Log;

import com.tencent.TIMMessage;
import com.tencent.av.sdk.AVView;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.ILiveConstants;
import com.tencent.ilivesdk.ILiveMemStatusLisenter;
import com.tencent.ilivesdk.ILiveSDK;
import com.tencent.ilivesdk.adapter.CommonConstants;
import com.tencent.ilivesdk.core.ILivePushOption;
import com.tencent.ilivesdk.core.ILiveRoomManager;
import com.tencent.ilivesdk.core.ILiveRoomOption;
import com.tencent.ilivesdk.data.ILivePushRes;
import com.tencent.ilivesdk.data.ILivePushUrl;
import com.tencent.ilivesdk.view.AVRootView;
import com.tencent.qcloud.ilivedemo.msg.utils.UIUtils;
import com.tencent.qcloud.ilivedemo.msg.viewinterface.IMsgRoomInterface;

import java.util.LinkedList;


/**
 * Created by valexhuang on 2018/3/27.
 */
public class RoomHelper implements ILiveRoomOption.onExceptionListener, ILiveRoomOption.onRoomDisconnectListener,ILiveRoomOption.onRequestViewListener,ILiveMemStatusLisenter {

    private static final String TAG = RoomHelper.class.getSimpleName();

    private IMsgRoomInterface roomView;

    private AVRootView avRootView;

    private LinkedList<String> identifiers = new LinkedList<>();

    private static final int FRONT_CAMERA = 0;
    private static final int BACK_CAMERA = 1;
    private Boolean isOpenCamera = false;
    private boolean isMicOpen = false;
    private boolean mIsFrontCamera = true;
    private boolean isBakCameraOpen, isBakMicOpen;      // 切后台时备份当前camera及mic状态

    public RoomHelper(IMsgRoomInterface view) {
        roomView = view;
    }

    public boolean isMicOpen() {
        return isMicOpen;
    }

    public boolean isOpenCamera(){
        return isOpenCamera;
    }

    public boolean isFrontCamera() {
        return mIsFrontCamera;
    }

    //初始化AVRootView 设置渲染控件
    public void setRootView(AVRootView avRootView) {
        this.avRootView = avRootView;
        this.avRootView.setLocalFullScreen(false);
        ILiveRoomManager.getInstance().initAvRootView(avRootView);
    }

    /**
     * 进入房间，为方便测试提供创建房间和加入房间两个操作
     * @param createFlag true:创建房间，false加入房间
     */
    public void enterRoom(String roomNum,boolean createFlag) {
        if (LoginHelper.isLogin()) {
            int roomId = 0;
            try {
                roomId = Integer.parseInt(roomNum);
            } catch (Exception e) {

            }
            if (roomId > 0) {
                if (createFlag)
                    createRoom(roomId);
                else
                    joinEventRoom(roomId);
            } else
                UIUtils.toastLongMessage("请输入合法的房间ID（1~10000000）");
        } else {
            UIUtils.toastLongMessage("您还未登录");
        }
    }


    // 对应IMRoomActivity中的加入房间,即具备首发消息的功能
    public int createRoom(int roomId) {
        ILiveRoomOption option = new ILiveRoomOption()
                .imsupport(true) // 开启IM功能
//                .autoRender(false)  //配置关闭自动渲染
                .groupType("AVChatRoom")    // 使用互动直播聊天室(默认)
                .exceptionListener(this)
                .roomDisconnectListener(this)
                .setRequestViewLisenter(this) // 监听视频请求回调
                .setRoomMemberStatusLisenter(this) // 监听房间内音视频事件
                .controlRole("user") //使用user角色
                .autoCamera(true) //进入房间自动打开摄像头
                .autoMic(true);//进入房间自动打开麦克
        return ILiveRoomManager.getInstance().createRoom(roomId, option, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                roomView.onEnterRoom();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                roomView.onEnterRoomFailed(module, errCode, errMsg);
            }
        });
    }

    // 对应IMRoomActivity中的加入房间,即具备首发消息的功能
    public int joinEventRoom(int roomId) {
        ILiveRoomOption option = new ILiveRoomOption()
                .imsupport(true)       // 开启IM功能
                .groupType("AVChatRoom")    // 使用互动直播聊天室(默认),与创建一致
                .exceptionListener(this)
                .roomDisconnectListener(this)
                .setRequestViewLisenter(this) // 监听视频请求回调
                .setRoomMemberStatusLisenter(this) // 监听房间内音视频事件
                .controlRole("user")//使用user角色,默认画质
                .autoCamera(true) //进入房间不打开摄像头
                .autoMic(true); //进入房间不连麦
        return ILiveRoomManager.getInstance().joinRoom(roomId, option, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                roomView.onEnterRoom();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                roomView.onEnterRoomFailed(module, errCode, errMsg);
            }
        });
    }

    /**
     * 退出房间
     * @return
     */
    public int quitRoom() {
        return ILiveRoomManager.getInstance().quitRoom(new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                closeCameraAndMic();
                roomView.onQuitRoomSuccess();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                roomView.onQuitRoomFailed(module, errCode, errMsg);
            }
        });
    }

    /**
     * 请求视频画面
     */
    @Override
    public boolean onEndpointsUpdateInfo(int eventid, String[] updateList) {
        switch (eventid){
            case ILiveConstants.TYPE_MEMBER_CHANGE_HAS_CAMERA_VIDEO:
                for (String identifier : updateList){
                    // 请求视频画面
                    ILiveSDK.getInstance().getContextEngine().requestUserVideoData(identifier,CommonConstants.Const_VideoType_Camera);
                    if (!identifiers.contains(identifier)){
                        identifiers.add(identifier);
                        Log.i("identifierChange","add identifier:"+identifier);
                    }
                }
                resetRenderLayout();
                break;
            case ILiveConstants.TYPE_MEMBER_CHANGE_NO_CAMERA_VIDEO:
                for (String identifier : updateList){
                    ILiveSDK.getInstance().getContextEngine().removeUserVideoData(identifier,CommonConstants.Const_VideoType_Camera);
                    avRootView.closeUserView(identifier,CommonConstants.Const_VideoType_Camera,true);
                    if(identifiers.remove(identifier)){
                        Log.i("identifierChange","remove identifier:"+identifier);
                    };

                }
                resetRenderLayout();
                break;
            case ILiveConstants.TYPE_MEMBER_CHANGE_HAS_SCREEN_VIDEO:
                for (String identifier : updateList){
                    // 请求视频画面
                    ILiveSDK.getInstance().getContextEngine().requestUserVideoData(identifier,CommonConstants.Const_VideoType_Screen);
                }
                break;
        }
        // 这里需要返回false
        return false;
    }

    /**
     * 渲染视频画面--在视频请求成功后，调用渲染控件的接口来进行渲染：
     * @param identifierList
     * @param viewList
     * @param count
     * @param result
     * @param errMsg
     */
    @Override
    public void onComplete(String[] identifierList, AVView[] viewList, int count, int result, String errMsg) {
        if (ILiveConstants.NO_ERR == result){
            for (int i=0; i<identifierList.length; i++){
                avRootView.renderVideoView(true, identifierList[i], viewList[i].videoSrcType, true);
            }
            resetRenderLayout();
        }else {
            roomView.onVedioRequestErr(result,errMsg);
        }
    }

    @Override
    public void onException(int exceptionId, int errCode, String errMsg) {
       roomView.onException(exceptionId,errCode,errMsg);
    }

    @Override
    public void onRoomDisconnect(int errCode, String errMsg) {
        // 处理房间中断(一般为断网或长时间无长行后台回收房间)
        roomView.onRoomDisconnect(errCode, errMsg);
    }

    // 处理Activity事件
    public void onPause() {
        ILiveRoomManager.getInstance().onPause();

        isBakCameraOpen = isOpenCamera;
        isBakMicOpen = isMicOpen;
        if (isBakCameraOpen || isBakMicOpen) {    // 若摄像头或Mic打开
            closeCameraAndMic();
        }
    }

    public void onResume() {
        ILiveRoomManager.getInstance().onResume();

        if (isBakCameraOpen || isBakMicOpen) {
            if (isBakCameraOpen) {
                openCamera();
            }
            if (isBakMicOpen) {
                openMic();
            }
        }
    }

    /**
     * 发送群组消息
     * @param message
     */
    public void sendGroupMsg(final TIMMessage message) {
        //填充发送者
        message.setSender(LoginHelper.getCurrentAccount());
        //发送消息
        ILiveRoomManager.getInstance().sendGroupMessage(message, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                // 处理发消息成功
                roomView.onSendMsgSuccess(message);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                // 处理发消息失败
                roomView.onSendMsgFailed(module, errCode, errMsg);
            }
        });
    }




    private void resetRenderLayout(){
        //在此判断视频数量
        int num = identifiers.size();
        Log.i("identifierChange","reset num:"+num);
        if (num==1){
            trViewSingle();
        }else if (num==2){
            trViewDouble();
        }else if (num<5){
            trViewQuarter();
        }else {
            trViewNonuple();
        }
    }

    //单视频布局
    private void trViewSingle(){
        avRootView.getViewByIndex(0).setPosLeft(0);
        avRootView.getViewByIndex(0).setPosTop(0);
        avRootView.getViewByIndex(0).setPosWidth(avRootView.getWidth());
        avRootView.getViewByIndex(0).setPosHeight(avRootView.getHeight());
        avRootView.getViewByIndex(0).autoLayout();
    }

    //二分布局
    private void trViewDouble(){
        avRootView.getViewByIndex(0).setPosLeft(0);
        avRootView.getViewByIndex(0).setPosTop(0);
        avRootView.getViewByIndex(0).setPosWidth(avRootView.getWidth());
        avRootView.getViewByIndex(0).setPosHeight(avRootView.getHeight()/2);
        avRootView.getViewByIndex(0).autoLayout();

        avRootView.getViewByIndex(1).setPosLeft(0);
        avRootView.getViewByIndex(1).setPosTop(avRootView.getHeight()/2);
        avRootView.getViewByIndex(1).setPosWidth(avRootView.getWidth());
        avRootView.getViewByIndex(1).setPosHeight(avRootView.getHeight()/2);
        avRootView.getViewByIndex(1).autoLayout();
    }

    //四分布局
    private void trViewQuarter(){
        // 计算视频画面的宽高
        int subWidth = avRootView.getWidth()/2;
        int subHeight = avRootView.getHeight()/2;

        // 设置视频画面左上角位置
        avRootView.getViewByIndex(0).setPosLeft(0);
        avRootView.getViewByIndex(0).setPosTop(0);

        avRootView.getViewByIndex(1).setPosLeft(subWidth);
        avRootView.getViewByIndex(1).setPosTop(0);

        avRootView.getViewByIndex(2).setPosLeft(0);
        avRootView.getViewByIndex(2).setPosTop(subHeight);

        avRootView.getViewByIndex(3).setPosLeft(subWidth);
        avRootView.getViewByIndex(3).setPosTop(subHeight);

        for (int i=0; i<4; i++){
            avRootView.getViewByIndex(i).setPosWidth(subWidth);
            avRootView.getViewByIndex(i).setPosHeight(subHeight);
            avRootView.getViewByIndex(i).autoLayout();
        }
    }

    //六宫布局
    private void trViewNonuple(){
        int subWidth = avRootView.getWidth()/2;
        int subHeight = avRootView.getHeight()/3;

        // 设置视频画面左上角位置
        avRootView.getViewByIndex(0).setPosLeft(0);
        avRootView.getViewByIndex(0).setPosTop(0);

        avRootView.getViewByIndex(1).setPosLeft(subWidth);
        avRootView.getViewByIndex(1).setPosTop(0);

        avRootView.getViewByIndex(2).setPosLeft(0);
        avRootView.getViewByIndex(2).setPosTop(subHeight);

        avRootView.getViewByIndex(3).setPosLeft(subWidth);
        avRootView.getViewByIndex(3).setPosTop(subHeight);

        avRootView.getViewByIndex(4).setPosLeft(0);
        avRootView.getViewByIndex(4).setPosTop(subHeight*2);

        avRootView.getViewByIndex(5).setPosLeft(subWidth);
        avRootView.getViewByIndex(5).setPosTop(subHeight*2);

        for (int i=0; i<6; i++){
            avRootView.getViewByIndex(i).setPosWidth(subWidth);
            avRootView.getViewByIndex(i).setPosHeight(subHeight);
            avRootView.getViewByIndex(i).autoLayout();
        }
    }

    /**
     * 开启摄像头和MIC
     */
    public void openCameraAndMic() {
        openCamera();
        openMic();
    }

    /**
     * 关闭摄像头和MIC
     */
    public void closeCameraAndMic() {
        closeCamera();
        closeMic();
    }


    /**
     * 开启Mic
     */
    public void openMic() {
        ILiveRoomManager.getInstance().enableMic(true);
        isMicOpen = true;
    }

    /**
     * 关闭Mic
     */
    public void closeMic() {
        ILiveRoomManager.getInstance().enableMic(false);
        isMicOpen = false;
    }

    /**
     * 打开摄像头
     */
    public void openCamera() {
        if (mIsFrontCamera) {
            enableCamera(FRONT_CAMERA, true);
        } else {
            enableCamera(BACK_CAMERA, true);
        }
    }

    /**
     * 关闭摄像头
     */
    public void closeCamera() {
        if (mIsFrontCamera) {
            enableCamera(FRONT_CAMERA, false);
        } else {
            enableCamera(BACK_CAMERA, false);
        }
    }

    /**
     * 转换前后摄像头
     *
     * @return
     */
    public void switchCamera() {
        ILiveRoomManager.getInstance().switchCamera(mIsFrontCamera ? BACK_CAMERA : FRONT_CAMERA);
        mIsFrontCamera = !mIsFrontCamera;
    }

    /**
     * 开关摄像头
     *
     * @param camera
     * @param isEnable
     */
    private void enableCamera(final int camera, boolean isEnable) {
        if (isEnable) {
            isOpenCamera = true;
        } else {
            isOpenCamera = false;
        }
        if (camera == FRONT_CAMERA) {
            mIsFrontCamera = true;
        } else {
            mIsFrontCamera = false;
        }
        //打开摄像头
        ILiveRoomManager.getInstance().enableCamera(camera, isEnable);
    }



}