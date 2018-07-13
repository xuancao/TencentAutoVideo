package com.tencent.qcloud.ilivedemo.msg.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.qcloud.ilivedemo.msg.R;
import com.tencent.qcloud.ilivedemo.msg.viewinterface.ChatInterface;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 聊天界面输入控件
 */
public class ChatInput extends RelativeLayout implements TextWatcher,View.OnClickListener {

    private static final String TAG = "ChatInput";

    private ImageButton btnAdd, btnSend, btnEmotion;
    private EditText editText;
    private boolean isSendVisible,isEmoticonReady;
    private InputMode inputMode = InputMode.NONE;
    private ChatInterface chatView;
    private LinearLayout morePanel;
    private LinearLayout emoticonPanel;
    private final int REQUEST_CODE_ASK_PERMISSIONS = 100;

    public ChatInput(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.chat_input, this);
        initView();
    }

    private void initView(){
        btnAdd = (ImageButton) findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(this);
        btnSend = (ImageButton) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(this);
        btnEmotion = (ImageButton) findViewById(R.id.btnEmoticon);
        btnEmotion.setOnClickListener(this);
        morePanel = (LinearLayout) findViewById(R.id.morePanel);
        LinearLayout BtnImage = (LinearLayout) findViewById(R.id.btn_photo);
        BtnImage.setOnClickListener(this);
        LinearLayout BtnPhoto = (LinearLayout) findViewById(R.id.btn_image);
        BtnPhoto.setOnClickListener(this);
        LinearLayout btnVideo = (LinearLayout) findViewById(R.id.btn_video);
        btnVideo.setOnClickListener(this);
        setSendBtn();
        editText = (EditText) findViewById(R.id.input);
        editText.addTextChangedListener(this);
        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    updateView(InputMode.TEXT);
                }
            }
        });
        isSendVisible = editText.getText().length() != 0;
        emoticonPanel = (LinearLayout) findViewById(R.id.emoticonPanel);

    }

    private void updateView(InputMode mode){
        if (mode == inputMode) return;
        leavingCurrentState();
        switch (inputMode = mode){
            case MORE:
                morePanel.setVisibility(VISIBLE);
                break;
            case TEXT:
                if (editText.requestFocus()){
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                }
                break;
            case EMOTICON:
                if (!isEmoticonReady) {
                    prepareEmoticon();
                }
                emoticonPanel.setVisibility(VISIBLE);
                break;
        }
    }

    private void leavingCurrentState(){
        switch (inputMode){
            case TEXT:
                View view = ((Activity) getContext()).getCurrentFocus();
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                editText.clearFocus();
                break;
            case MORE:
                morePanel.setVisibility(GONE);
                break;
            case EMOTICON:
                emoticonPanel.setVisibility(GONE);
        }
    }

    /**
     * 关联聊天界面逻辑
     */
    public void setChatView(ChatInterface chatView){
        this.chatView = chatView;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        isSendVisible = s!=null&&s.length()>0;
        setSendBtn();
//        if (isSendVisible){
//            chatView.sending();
//        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private void setSendBtn(){
        if (isSendVisible){
            btnAdd.setVisibility(GONE);
            btnSend.setVisibility(VISIBLE);
        }else{
            btnAdd.setVisibility(VISIBLE);
            btnSend.setVisibility(GONE);
        }
    }

    private void prepareEmoticon(){
        if (emoticonPanel == null) return;
        for (int i = 0; i < 5; ++i){
            LinearLayout linearLayout = new LinearLayout(getContext());
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
            for (int j = 0;j < 7; ++j){

                try{
                    AssetManager am = getContext().getAssets();
                    final int index = 7*i+j;
                    InputStream is = am.open(String.format("emoticon/%d.gif", index));
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    Matrix matrix = new Matrix();
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    matrix.postScale(3.5f, 3.5f);
                    final Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                            width, height, matrix, true);
                    ImageView image = new ImageView(getContext());
                    image.setImageBitmap(resizedBitmap);
                    image.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
                    linearLayout.addView(image);
                    image.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String content = String.valueOf(index);
                            SpannableString str = new SpannableString(String.valueOf(index));
                            ImageSpan span = new ImageSpan(getContext(), resizedBitmap, ImageSpan.ALIGN_BASELINE);
                            str.setSpan(span, 0, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            editText.append(str);
                        }
                    });
                    is.close();
                }catch (IOException e){

                }

            }
            emoticonPanel.addView(linearLayout);
        }
        isEmoticonReady = true;
    }

    @Override
    public void onClick(View v) {
        Activity activity = (Activity) getContext();
        int id = v.getId();
        if (id == R.id.btn_send){
            chatView.sendText();
        }
        if (id == R.id.btn_add){
            updateView(inputMode == InputMode.MORE ? InputMode.TEXT : InputMode.MORE);
        }
        if (id == R.id.btn_photo){
            if(activity!=null && requestCamera(activity)){
                chatView.sendPhoto();
            }
        }
        if (id == R.id.btn_image){
            if(activity!=null && requestStorage(activity)){
                chatView.sendImage();
            }
        }
        if (id == R.id.btn_video){

        }
        if (id == R.id.btnEmoticon){
            updateView(inputMode == InputMode.EMOTICON? InputMode.TEXT: InputMode.EMOTICON);
        }
    }

    /**
     * 获取输入框文字
     */
    public Editable getText(){
        return editText.getText();
    }

    /**
     * 设置输入框文字
     */
    public void setText(String text){
        editText.setText(text);
    }

    /**
     * 设置输入模式
     */
    public void setInputMode(InputMode mode){
        updateView(mode);
    }

    public enum InputMode{
        TEXT,
        EMOTICON,
        MORE,
        NONE,
    }

    private boolean requestCamera(Activity activity){
        if (afterM()){
            int hasPermission = activity.checkSelfPermission(Manifest.permission.CAMERA);
            if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(new String[]{Manifest.permission.CAMERA},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return false;
            }
        }
        return true;
    }

    private boolean requestStorage(Activity activity){
        if (afterM()){
            int hasPermission = activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return false;
            }
        }
        return true;
    }

    private boolean afterM(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

}
