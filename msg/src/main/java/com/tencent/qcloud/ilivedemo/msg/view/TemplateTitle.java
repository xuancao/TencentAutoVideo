package com.tencent.qcloud.ilivedemo.msg.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.qcloud.ilivedemo.msg.R;


/**
 * 标题控件
 */
public class TemplateTitle extends RelativeLayout {

    private String titleText;
    private boolean canBack;
    private String backText;
    private String moreText;
    private int moreImg;
    private TextView tvMore;


    public TemplateTitle(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.title, this);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TemplateTitle, 0, 0);
        try {
            titleText = ta.getString(R.styleable.TemplateTitle_titleText);
            canBack = ta.getBoolean(R.styleable.TemplateTitle_canBack, false);
            backText = ta.getString(R.styleable.TemplateTitle_backText);
            moreImg = ta.getResourceId(R.styleable.TemplateTitle_moreImg, 0);
            moreText = ta.getString(R.styleable.TemplateTitle_moreText);
            setUpView();
        } finally {
            ta.recycle();
        }
    }

    private void setUpView(){
        TextView tvTitle = (TextView) findViewById(R.id.title);
        tvTitle.setText(titleText);
        LinearLayout backBtn = (LinearLayout) findViewById(R.id.title_back);
        backBtn.setVisibility(canBack ? VISIBLE : INVISIBLE);
        if (canBack){
            TextView tvBack = (TextView) findViewById(R.id.txt_back);
            tvBack.setText(backText);
            backBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Activity) getContext()).finish();
                }
            });
        }
        if (moreImg != 0){
            ImageView moreImgView = (ImageView) findViewById(R.id.img_more);
            moreImgView.setImageDrawable(getContext().getResources().getDrawable(moreImg));
        }
        tvMore = (TextView) findViewById(R.id.txt_more);
        tvMore.setText(moreText);
    }


    /**
     * 标题控件
     *
     * @param titleText 设置标题文案
     */
    public void setTitleText(String titleText){
        this.titleText = titleText;
        TextView tvTitle = (TextView) findViewById(R.id.title);
        tvTitle.setText(titleText);
    }

    /**
     * 标题更多按钮
     *
     * @param img 设置更多按钮
     */
    public void setMoreImg(int img){
        moreImg = img;
        ImageView moreImgView = (ImageView) findViewById(R.id.img_more);
        moreImgView.setImageDrawable(getContext().getResources().getDrawable(moreImg));
    }


    /**
     * 设置更多按钮事件
     *
     * @param listener 事件监听
     */
    public void setMoreImgAction(View.OnClickListener listener){
        ImageView moreImgView = (ImageView) findViewById(R.id.img_more);
        moreImgView.setOnClickListener(listener);
    }



    /**
     * 设置更多按钮事件
     *
     * @param listener 事件监听
     */
    public void setMoreTextAction(View.OnClickListener listener){
        tvMore.setOnClickListener(listener);
    }


    /**
     * 设置更多文字内容
     * @param text 更多文本
     */
    public void setMoreTextContext(String text){
        tvMore.setText(text);
    }



    /**
     * 设置返回按钮事件
     *
     * @param listener 事件监听
     */
    public void setBackListener(OnClickListener listener){
        if (canBack){
            LinearLayout backBtn = (LinearLayout) findViewById(R.id.title_back);
            backBtn.setOnClickListener(listener);
        }
    }






}
