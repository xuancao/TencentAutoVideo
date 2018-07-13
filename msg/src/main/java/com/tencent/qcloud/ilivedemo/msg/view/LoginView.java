package com.tencent.qcloud.ilivedemo.msg.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tencent.qcloud.ilivedemo.msg.presenter.LoginHelper;
import com.tencent.qcloud.ilivedemo.msg.utils.UIUtils;
import com.tencent.qcloud.ilivedemo.msg.viewinterface.ILoginInterface;
import com.tencent.qcloud.ilivedemo.msg.R;

/**
 * Created by valexhuang on 2018/4/12.
 *
 * 登录与创建房间集成控件，此处不做过多说明，关于登录和创建房间等具体可参考前三章节
 */

public class LoginView extends LinearLayout implements ILoginInterface, View.OnClickListener {


    private EditText accoutText;
    private Button loginBtn;
    private ImageView ctrBtn;
    private TextView loginSate;
    private View loginGroup;
    LoginHelper helper;

    public LoginView(Context context) {
        super(context);
        initView();
    }

    public LoginView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public LoginView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }


    private void initView() {
        helper = new LoginHelper(this);
        LayoutInflater.from(getContext()).inflate(R.layout.layout_login_panel, this);

        accoutText = findViewById(R.id.panel_account_text);
        loginGroup = findViewById(R.id.ctrl_group);

        ctrBtn = findViewById(R.id.panel_ctrl);
        ctrBtn.setOnClickListener(this);

        loginBtn = findViewById(R.id.panel_login_btn);
        loginBtn.setOnClickListener(this);

        loginSate = findViewById(R.id.panel_login_state);

        if (LoginHelper.isLogin()) {
            accoutText.setText(LoginHelper.getCurrentAccount());
            updateLoginState(true);
        }


    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.panel_ctrl:
                boolean flag = loginGroup.getVisibility() == View.VISIBLE;
                if (flag){
                    loginGroup.setVisibility(View.VISIBLE);
                }else {
                    loginGroup.setVisibility(View.GONE);
                }
                break;
            case R.id.panel_login_btn:
                String userSig;
                if ("steel".equals(getAccount())){
                    userSig = "eJxlz0FvgjAUwPE7n6LhvCwtpZMt2UFFUwRB45zx1BRaSNUhllpnzL77MmYykr3r7--y8m4OAMB9S1aPvCiO59owc22kC16AC92HP2waJRg3DGvxD*Vno7RkvDRSd4gIIR6E-UYJWRtVqnvRGikPPW7FnnU3fvd9CBEcYPzUT1TV4XyyHkfLMJ*dk2wb8AsO19TXeW5pHG-SQ3GxIqZ6mKWLGfbRFC*GUTWupnaD6DuncZnZ7X5UjQihIqKpSeJijk6rcHddngblbvLaO2nUh7w-hCF5JgEOemqlbtWx7gIPIoI8DH-Gdb6cb*POXJI_";
                }else if("gust1".equals(getAccount())){
                    userSig = "eJxlj01Pg0AURff8CsIWI2*YUoOJi2kLRlKtCqapG4IwwGv5mMLUUE3-eyM2cRLf9pybe9*3puu6ES3D6yRN20MjY3kU3NBvdQOMqz8oBGZxImPaZf8gHwR2PE5yybsREsdxbADVwYw3EnO8GMWhl0TBfbaLx47f-ASAwA2lU1XBYoSP3tv8gfnVU2DKwP2ymPiokZhLr4gYtapwmEsImuHV3bNyFrk2Q49tn2Xth3BvrtrJusdyEZZe*r7Oj9H2ZbURVSuC-WLjWzN3d6dUSqz55SEKjjsFUAd98q7HthkFG4hDbAo-Z2gn7QyvdVx2";
                }else {
                    userSig = "eJxlz1FLwzAQwPH3foqS14lcm6Q6wYdtmdCRUYubDl9KXZKabuuyJlWH*N1l3cCA9-r7H8d9B2EYogV-ui7X633XuMIdjUThXYgAXf2hMVoUpStwK-6h-DK6lUWpnGx7jCilMYDfaCEbp5W*FFVnXeyxFZuiv3HeJwAR3GCc*ImuepxPl5M0Z9OMJSWjg0dCpDBH3r3EuX239SLL*EpRshyp*sCjdph9phVLHkbz3bNkbsZvD9u6smwwy0mzBSLH-HUzzpSi9G01Eem9d9Lpnbw8hIEOEyDY0w-ZWr1v*iCGiEYxhtOg4Cf4BZUAXAw_";                }
                helper.login(getAccount(),userSig);
                break;
        }
    }


    public void updateLoginState(boolean state) {
        String textLable = state ? "已登录" : "未登陆";
        int color = state ? Color.GREEN : Color.RED;
        loginSate.setText(textLable);
        loginSate.setTextColor(color);
        String buttonLabel = state ? "注销" : "登录";
        loginBtn.setText(buttonLabel);
    }

    @Override
    public void onLoginSDKSuccess() {
        updateLoginState(true);
    }

    @Override
    public void onLoginSDKFailed(String module, int errCode, String errMsg) {
        UIUtils.toastLongMessage("登录失败" + ":::" + errCode + "=" + errMsg);
    }

    @Override
    public void onLogoutSDKSuccess() {
        updateLoginState(false);
    }

    @Override
    public void onLogoutSDKFailed(String module, int errCode, String errMsg) {
        UIUtils.toastLongMessage("注销失败" + ":::" + errCode + "=" + errMsg);
    }

    public String getAccount() {
        return accoutText.getText().toString().trim();
    }

}
