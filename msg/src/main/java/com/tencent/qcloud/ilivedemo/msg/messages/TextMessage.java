package com.tencent.qcloud.ilivedemo.msg.messages;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.TypedValue;
import android.widget.TextView;

import com.tencent.TIMElem;
import com.tencent.TIMElemType;
import com.tencent.TIMFaceElem;
import com.tencent.TIMMessage;
import com.tencent.TIMMessageDraft;
import com.tencent.TIMTextElem;
import com.tencent.qcloud.ilivedemo.msg.DemoApplication;
import com.tencent.qcloud.ilivedemo.msg.R;
import com.tencent.qcloud.ilivedemo.msg.chatUI.ChatAdapter;
import com.tencent.qcloud.ilivedemo.msg.utils.EmoticonUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 文本消息数据
 */
public class TextMessage extends Message {

    public TextMessage(TIMMessage message){
        this.message = message;
    }

    public TextMessage(String s){
        message = new TIMMessage();
        TIMTextElem elem = new TIMTextElem();
        elem.setText(s);
        message.addElement(elem);
    }

    public TextMessage(TIMMessageDraft draft){
        message = new TIMMessage();
        for (TIMElem elem : draft.getElems()){
            message.addElement(elem);
        }
    }

    private List<ImageSpan> sortByIndex(final Editable editInput, ImageSpan[]array){
        ArrayList<ImageSpan> sortList = new ArrayList<>();
        for (ImageSpan span : array){
            sortList.add(span);
        }
        Collections.sort(sortList, new Comparator<ImageSpan>() {
            @Override
            public int compare(ImageSpan lhs, ImageSpan rhs) {
                return editInput.getSpanStart(lhs) - editInput.getSpanStart(rhs);
            }
        });

        return sortList;
    }

    public TextMessage(Editable s){
        message = new TIMMessage();
        ImageSpan[] spans = s.getSpans(0, s.length(), ImageSpan.class);
        List<ImageSpan> listSpans = sortByIndex(s, spans);
        int currentIndex = 0;
        for (ImageSpan span : listSpans){
            int startIndex = s.getSpanStart(span);
            int endIndex = s.getSpanEnd(span);
            if (currentIndex < startIndex){
                TIMTextElem textElem = new TIMTextElem();
                textElem.setText(s.subSequence(currentIndex, startIndex).toString());
                message.addElement(textElem);
            }
            TIMFaceElem faceElem = new TIMFaceElem();
            int index = Integer.parseInt(s.subSequence(startIndex, endIndex).toString());
            faceElem.setIndex(index);
            if (index < EmoticonUtil.emoticonData.length){
                faceElem.setData(EmoticonUtil.emoticonData[index].getBytes(Charset.forName("UTF-8")));
            }
            message.addElement(faceElem);
            currentIndex = endIndex;
        }
        if (currentIndex < s.length()){
            TIMTextElem textElem = new TIMTextElem();
            textElem.setText(s.subSequence(currentIndex, s.length()).toString());
            message.addElement(textElem);
        }

    }



    /**
     * 在聊天界面显示消息
     *
     * @param viewHolder 界面样式
     * @param context 显示消息的上下文
     */
    @Override
    public void showMessage(ChatAdapter.ViewHolder viewHolder, Context context) {
        clearView(viewHolder);
        if (checkRevoke(viewHolder)) return;
        boolean hasText = false;
        TextView tv = new TextView(DemoApplication.getApplication());
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tv.setTextColor(DemoApplication.getApplication().getResources().getColor(isSelf() ? R.color.white : R.color.black));
        List<TIMElem> elems = new ArrayList<>();
        for (int i = 0; i < message.getElementCount(); ++i){
            elems.add(message.getElement(i));
            if (message.getElement(i).getType() == TIMElemType.Text){
                hasText = true;
            }
        }
        SpannableStringBuilder stringBuilder = getString(elems, context);
        if (!hasText){
            stringBuilder.insert(0," ");
        }
        tv.setText(stringBuilder);
        getBubbleView(viewHolder).addView(tv);
        showStatus(viewHolder);
    }

    /**
     * 获取消息摘要
     */
    @Override
    public String getSummary() {
        String str = getRevokeSummary();
        if (str != null) return str;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i<message.getElementCount(); ++i){
            switch (message.getElement(i).getType()){
                case Face:
                    TIMFaceElem faceElem = (TIMFaceElem) message.getElement(i);
                    byte[] data = faceElem.getData();
                    if (data != null){
                        result.append(new String(data, Charset.forName("UTF-8")));
                    }
                    break;
                case Text:
                    TIMTextElem textElem = (TIMTextElem) message.getElement(i);
                    result.append(textElem.getText());
                    break;
            }

        }
        return result.toString();
    }

    /**
     * 保存消息或消息文件
     */
    @Override
    public void save() {

    }

    private static int getNumLength(int n){
        return String.valueOf(n).length();
    }


    public static SpannableStringBuilder getString(List<TIMElem> elems, Context context){
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        for (int i = 0; i<elems.size(); ++i){
            switch (elems.get(i).getType()){
                case Face:
                    TIMFaceElem faceElem = (TIMFaceElem) elems.get(i);
                    int startIndex = stringBuilder.length();
                    try{
                        AssetManager am = context.getAssets();
                        InputStream is = am.open(String.format("emoticon/%d.gif", faceElem.getIndex()));
                        if (is == null) continue;
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        Matrix matrix = new Matrix();
                        int width = bitmap.getWidth();
                        int height = bitmap.getHeight();
                        matrix.postScale(2, 2);
                        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                                width, height, matrix, true);
                        ImageSpan span = new ImageSpan(context, resizedBitmap, ImageSpan.ALIGN_BASELINE);
                        stringBuilder.append(String.valueOf(faceElem.getIndex()));
                        stringBuilder.setSpan(span, startIndex, startIndex + getNumLength(faceElem.getIndex()), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        is.close();
                    }catch (IOException e){

                    }
                    break;
                case Text:
                    TIMTextElem textElem = (TIMTextElem) elems.get(i);
                    stringBuilder.append(textElem.getText());
                    break;
            }

        }
        return stringBuilder;
    }


}
