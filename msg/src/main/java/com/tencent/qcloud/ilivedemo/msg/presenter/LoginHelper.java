package com.tencent.qcloud.ilivedemo.msg.presenter;


import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.core.ILiveLoginManager;
import com.tencent.qcloud.ilivedemo.msg.observable.StatusObservable;
import com.tencent.qcloud.ilivedemo.msg.utils.UIUtils;
import com.tencent.qcloud.ilivedemo.msg.viewinterface.ILoginInterface;


/**
 * Created by valexhuang on 2018/4/4.
 */

public class LoginHelper implements ILiveLoginManager.TILVBStatusListener {

    private ILoginInterface loginView;
    private static boolean mLoginState;
    private static String mCurrentAccount;

    public LoginHelper(ILoginInterface loginView) {
        this.loginView = loginView;
    }

    /**
     * 账号登录
     * @param account
     * @param userSig
     */
    public void login(final String account,String userSig) {
        //判断是否已登录，已登录则进行注销操作
        if (isLogin()) {
            imLogout(account);
        } else {
            imLogin(account,userSig);
        }
    }

    /**
     * 退出imsdk
     * 退出成功会调用退出AVSDK
     */
    public void imLogout(final String account) {
        //传入用户与当前登录用户相同时才进行注销
        if (account.equals(mCurrentAccount)) {
            ILiveLoginManager.getInstance().iLiveLogout(new ILiveCallBack() {
                @Override
                public void onSuccess(Object data) {
                    mLoginState = false;
                    mCurrentAccount = null;
                    loginView.onLogoutSDKSuccess();
                    //退出登录后注销账号状态监听
                    StatusObservable.getInstance().deleteObserver(LoginHelper.this);

                }

                @Override
                public void onError(String module, int errCode, String errMsg) {
                    loginView.onLogoutSDKFailed(module, errCode, errMsg);
                }
            });
        } else {
            UIUtils.toastLongMessage("请先注销" + mCurrentAccount);
        }
    }

    /**
     * 登录imsdk
     * @param account 用户id
     * @param userSig  用户签名
     */
    public void imLogin(final String account,String userSig) {
        ILiveLoginManager.getInstance().iLiveLogin(account, userSig, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                mLoginState = true;
                mCurrentAccount = account;
                loginView.onLoginSDKSuccess();
                //登录成功后监听账号登录状态
                StatusObservable.getInstance().addObserver(LoginHelper.this);
                ILiveLoginManager.getInstance().setUserStatusListener(StatusObservable.getInstance());


            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                mLoginState = false;
                loginView.onLoginSDKFailed(module, errCode, errMsg);
            }
        });
    }


    /**
     * 账号异常退出处理
     *
     * @param error
     * @param message
     */
    @Override
    public void onForceOffline(int error, String message) {
        mLoginState = false;
        mCurrentAccount = null;
        loginView.updateLoginState(false);
    }

    public static boolean isLogin() {
        return mLoginState;
    }

    public static String getCurrentAccount() {
        return mCurrentAccount;
    }

}
