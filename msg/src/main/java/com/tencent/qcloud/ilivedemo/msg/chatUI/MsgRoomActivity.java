package com.tencent.qcloud.ilivedemo.msg.chatUI;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.TIMMessage;
import com.tencent.TIMMessageListener;
import com.tencent.TIMMessageStatus;
import com.tencent.ilivesdk.core.ILiveLoginManager;
import com.tencent.ilivesdk.view.AVRootView;
import com.tencent.qcloud.ilivedemo.msg.R;
import com.tencent.qcloud.ilivedemo.msg.messages.CustomMessage;
import com.tencent.qcloud.ilivedemo.msg.messages.ImageMessage;
import com.tencent.qcloud.ilivedemo.msg.messages.Message;
import com.tencent.qcloud.ilivedemo.msg.messages.MessageFactory;
import com.tencent.qcloud.ilivedemo.msg.messages.TextMessage;
import com.tencent.qcloud.ilivedemo.msg.observable.MessageObservable;
import com.tencent.qcloud.ilivedemo.msg.presenter.RoomHelper;
import com.tencent.qcloud.ilivedemo.msg.utils.FileUtil;
import com.tencent.qcloud.ilivedemo.msg.utils.PermissionUtils;
import com.tencent.qcloud.ilivedemo.msg.utils.UIUtils;
import com.tencent.qcloud.ilivedemo.msg.view.ChatInput;
import com.tencent.qcloud.ilivedemo.msg.view.LoginView;
import com.tencent.qcloud.ilivedemo.msg.viewinterface.ChatInterface;
import com.tencent.qcloud.ilivedemo.msg.viewinterface.IMsgRoomInterface;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MsgRoomActivity extends Activity implements IMsgRoomInterface, ChatInterface, View.OnClickListener,ILiveLoginManager.TILVBStatusListener,TIMMessageListener {
    private EditText mRoomIdText;
    private Button mCreateBtn, mJoinBtn, mQuitBtn;
    private TextView mMicBtn,mCameraBtn;

    private List<Message> mMsgDataList = new ArrayList<>();
    private ChatAdapter mMessageAdapter;
    private ListView mMsgList;

    private ChatInput input;
    private static final int IMAGE_STORE = 200;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int IMAGE_PREVIEW = 400;
    private Uri fileUri;

    private RoomHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   // 不锁屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
        setContentView(R.layout.activity_msg_room);
        PermissionUtils.checkPermission(this);
        MessageObservable.getInstance().addObserver(this); //监听房间内的群里消息
        initView();
        setListener();
        initData();
    }

    private void initView() {
        LoginView ctrPanel = (LoginView) findViewById(R.id.ilive_ctr_panel);
        mRoomIdText = ctrPanel.findViewById(R.id.room_id_text);
        mCreateBtn = ctrPanel.findViewById(R.id.create_btn);
        mJoinBtn = ctrPanel.findViewById(R.id.join_btn);
        mQuitBtn = ctrPanel.findViewById(R.id.quit_btn);
        mMicBtn  = ctrPanel.findViewById(R.id.mic_btn);
        mCameraBtn  = ctrPanel.findViewById(R.id.switch_cam);
        mMsgList = findViewById(R.id.member_list);
        input = (ChatInput) findViewById(R.id.input_panel);
        input.setChatView(this);
    }

    private void setListener(){
        mCreateBtn.setOnClickListener(this);
        mJoinBtn.setOnClickListener(this);
        mQuitBtn.setOnClickListener(this);
        mMicBtn.setOnClickListener(this);
        mCameraBtn.setOnClickListener(this);
        mMsgList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        input.setInputMode(ChatInput.InputMode.NONE);
                        break;
                }
                return false;
            }
        });
    }

    private void initData(){
        mMessageAdapter = new ChatAdapter(this, R.layout.item_message, mMsgDataList);
        mMsgList.setAdapter(mMessageAdapter);
        mMsgList.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        helper = new RoomHelper(this);
        // 获取渲染控件
        AVRootView avRootView = findViewById(R.id.av_root_view);
        // 设置渲染控件
        helper.setRootView(avRootView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_btn:
                helper.enterRoom(mRoomIdText.getText().toString(),true);
                break;
            case R.id.join_btn:
                helper.enterRoom(mRoomIdText.getText().toString(),false);
                break;
            case R.id.quit_btn:
                helper.quitRoom();
                break;
            case R.id.mic_btn:
                if (helper.isMicOpen()) {
                    mMicBtn.setBackgroundResource(R.drawable.icon_mic_close);
                    helper.closeMic();
                } else {
                    mMicBtn.setBackgroundResource(R.drawable.icon_mic_open);
                    helper.openMic();
                }
                break;
            case R.id.switch_cam:
                if (helper.isOpenCamera()){
                    if (helper.isFrontCamera()){
                        helper.switchCamera();
                    }else {
                        helper.closeCamera();
                    }
                }else {
                    helper.openCamera();
                }
                break;
        }
    }

    //进入房间成功回调
    @Override
    public void onEnterRoom() {
        UIUtils.toastShortMessage("加入房间成功");

    }

    //退出房间成功回调
    @Override
    public void onQuitRoomSuccess() {
        UIUtils.toastShortMessage("退出房间成功");
        //退出房间时取消消息监听
        MessageObservable.getInstance().deleteObserver(this);
        mMsgDataList.clear();
        mMessageAdapter.notifyDataSetChanged();
    }


    /**
     * 群组消息监听回调，群组有接受到消息后会触发此回调
     * @param list
     */
    @Override
    public boolean onNewMessages(List<TIMMessage> list) {
        showMessage(list);
        return false;
    }


    /**
     * 消息发送成功回调处理
     * @param message
     */
    @Override
    public void onSendMsgSuccess(TIMMessage message) {
        showMessage(message);
    }

    /**
     * 消息发送失败回调处理
     * @param module
     * @param errCode
     * @param errMsg
     */
    @Override
    public void onSendMsgFailed(String module, int errCode, String errMsg) {
        UIUtils.toastLongMessage("发送消息失败：" + errCode + "::::" + errMsg);
//        long id = message.getMsgUniqueId();
//        for (Message msg : messageList){
//            if (msg.getMessage().getMsgUniqueId() == id){
//                switch (code){
//                    case 80001:
//                        //发送内容包含敏感词
//                        msg.setDesc(getString(R.string.chat_content_bad));
//                        adapter.notifyDataSetChanged();
//                        break;
//                }
//            }
//        }
//
//        adapter.notifyDataSetChanged();
    }


    public void onEnterRoomFailed(String module, int errCode, String errMsg) {
        UIUtils.toastLongMessage("加入房间失败：" + errCode + "::::" + errMsg);
    }

    public void onQuitRoomFailed(String module, int errCode, String errMsg) {
        UIUtils.toastLongMessage("退出房间失败：" + errCode + "::::" + errMsg);
    }


    public void onRoomDisconnect(int errCode, String errMsg) {
        UIUtils.toastLongMessage("连接断开：" + errCode + "::::" + errMsg);
    }

    @Override
    public void onException(int exceptionId, int errCode, String errMsg) {
        UIUtils.toastLongMessage("异常：" + errCode + "::::" + errMsg);
    }

    @Override
    public void onVedioRequestErr(int result, String errMsg) {
        UIUtils.toastLongMessage("请求失败：" + result + "::::" + errMsg);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        //退出房间时注销群组消息的监听
        MessageObservable.getInstance().deleteObserver(this);
        helper.quitRoom();
    }

    @Override
    protected void onResume() {
        super.onResume();
        helper.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        helper.onPause();
    }

    /**
     * 被强制下线回调方法
     * @param error
     * @param message
     */
    @Override
    public void onForceOffline(int error, String message) {
        Toast.makeText(this,"账号异地登录",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginView.class);
        startActivity(intent);
        finish();
    }

    /***----- chatView start ---------------  **/


    /**
     * 显示消息
     * @param message
     */
    @Override
    public void showMessage(TIMMessage message) {
        if (message == null) {
            mMessageAdapter.notifyDataSetChanged();
        } else {
            Message mMessage = MessageFactory.getMessage(message);
            if (mMessage != null) {
                if (mMsgDataList.size()==0){
                    mMessage.setHasTime(null);
                }else{
                    mMessage.setHasTime(mMsgDataList.get(mMsgDataList.size()-1).getMessage());
                }
                mMsgDataList.add(mMessage);
                mMessageAdapter.notifyDataSetChanged();
                mMsgList.setSelection(mMessageAdapter.getCount()-1);
            }
        }
    }

    /**
     * 显示消息
     * @param messages
     */
    @Override
    public void showMessage(List<TIMMessage> messages) {
        int newMsgNum = 0;
        for (int i = 0; i < messages.size(); ++i){
            Message mMessage = MessageFactory.getMessage(messages.get(i));
            if (mMessage == null || messages.get(i).status() == TIMMessageStatus.HasDeleted) continue;
            if (mMessage instanceof CustomMessage && (((CustomMessage) mMessage).getType() == CustomMessage.Type.TYPING ||
                    ((CustomMessage) mMessage).getType() == CustomMessage.Type.INVALID)) continue;
            ++newMsgNum;
            if (i != messages.size() - 1){
                mMessage.setHasTime(messages.get(i+1));
                mMsgDataList.add( mMessage);
            }else{
                mMessage.setHasTime(null);
                mMsgDataList.add( mMessage);
            }
        }
        mMessageAdapter.notifyDataSetChanged();
        mMsgList.setSelection(newMsgNum);
    }

    /**
     * 清除所有消息，等待刷新
     */
    @Override
    public void clearAllMessage() {
        mMsgDataList.clear();
    }

    @Override
    public void sendText() {
        if (!TextUtils.isEmpty(input.getText())){
            Message message = new TextMessage(input.getText());
            helper.sendGroupMsg(message.getMessage());
            input.setText("");
        }
    }

    @Override
    public void sendImage() {
        Intent intent_album = new Intent("android.intent.action.GET_CONTENT");
        intent_album.setType("image/*");
        startActivityForResult(intent_album, IMAGE_STORE);
    }

    @Override
    public void sendPhoto() {
        Intent intent_photo = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent_photo.resolveActivity(getPackageManager()) != null) {
            File tempFile = FileUtil.getTempFile(FileUtil.FileType.IMG);
            if (tempFile != null) {
                fileUri = Uri.fromFile(tempFile);
            }
            intent_photo.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(intent_photo, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK && fileUri != null) {
                showImagePreview(fileUri.getPath());
            }
        } else if (requestCode == IMAGE_STORE) {
            if (resultCode == RESULT_OK && data != null) {
                showImagePreview(FileUtil.getFilePath(this, data.getData()));
            }

        } else if (requestCode == IMAGE_PREVIEW){
            if (resultCode == RESULT_OK) {
                boolean isOri = data.getBooleanExtra("isOri",false);
                String path = data.getStringExtra("path");
                File file = new File(path);
                if (file.exists()){
                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(path, options);
                    if (file.length() == 0 && options.outWidth == 0) {
                        Toast.makeText(this, getString(R.string.chat_file_not_exist),Toast.LENGTH_SHORT).show();
                    }else {
                        if (file.length() > 1024 * 1024 * 10){
                            Toast.makeText(this, getString(R.string.chat_file_too_large),Toast.LENGTH_SHORT).show();
                        }else{
                            Message message = new ImageMessage(path,isOri);
                            helper.sendGroupMsg(message.getMessage());
                        }
                    }
                }else{
                    Toast.makeText(this, getString(R.string.chat_file_not_exist),Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    /** 拍照、选择图片调用 */
    private void showImagePreview(String path){
        if (path == null) return;
        Intent intent = new Intent(this, ImagePreviewActivity.class);
        intent.putExtra("path", path);
        startActivityForResult(intent, IMAGE_PREVIEW);
    }


}
