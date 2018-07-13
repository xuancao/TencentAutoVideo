package com.tencent.qcloud.ilivedemo.msg.utils;

import com.tencent.qcloud.ilivedemo.msg.DemoApplication;
import com.tencent.qcloud.ilivedemo.msg.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * 时间转换工具
 */
public class TimeUtil {


    private TimeUtil(){}

    /**
     * 时间转化为显示字符串
     *
     * @param timeStamp 单位为秒
     */
    public static String getTimeStr(long timeStamp){
        if (timeStamp==0) return "";
        Calendar inputTime = Calendar.getInstance();
        inputTime.setTimeInMillis(timeStamp*1000);
        Date currenTimeZone = inputTime.getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        if (calendar.before(inputTime)){
            //今天23:59在输入时间之前，解决一些时间误差，把当天时间显示到这里
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy" + DemoApplication.getApplication().getResources().getString(R.string.time_year)+"MM"+DemoApplication.getApplication().getResources().getString(R.string.time_month)+"dd"+DemoApplication.getApplication().getResources().getString(R.string.time_day));
            return sdf.format(currenTimeZone);
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.before(inputTime)){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            return sdf.format(currenTimeZone);
        }
        calendar.add(Calendar.DAY_OF_MONTH,-1);
        if (calendar.before(inputTime)){
            return DemoApplication.getApplication().getResources().getString(R.string.time_yesterday);
        }else{
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            if (calendar.before(inputTime)){
                SimpleDateFormat sdf = new SimpleDateFormat("M"+DemoApplication.getApplication().getResources().getString(R.string.time_month)+"d"+DemoApplication.getApplication().getResources().getString(R.string.time_day));
                return sdf.format(currenTimeZone);
            }else{
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy" + DemoApplication.getApplication().getResources().getString(R.string.time_year)+"MM"+DemoApplication.getApplication().getResources().getString(R.string.time_month)+"dd"+DemoApplication.getApplication().getResources().getString(R.string.time_day));
                return sdf.format(currenTimeZone);

            }

        }

    }

    /**
     * 时间转化为聊天界面显示字符串
     *
     * @param timeStamp 单位为秒
     */
    public static String getChatTimeStr(long timeStamp){
        if (timeStamp==0) return "";
        Calendar inputTime = Calendar.getInstance();
        inputTime.setTimeInMillis(timeStamp*1000);
        Date currenTimeZone = inputTime.getTime();
        Calendar calendar = Calendar.getInstance();
        if (!calendar.after(inputTime)){
            //当前时间在输入时间之前
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy" + DemoApplication.getApplication().getResources().getString(R.string.time_year)+"MM"+DemoApplication.getApplication().getResources().getString(R.string.time_month)+"dd"+DemoApplication.getApplication().getResources().getString(R.string.time_day));
            return sdf.format(currenTimeZone);
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.before(inputTime)){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            return sdf.format(currenTimeZone);
        }
        calendar.add(Calendar.DAY_OF_MONTH,-1);
        if (calendar.before(inputTime)){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            return DemoApplication.getApplication().getResources().getString(R.string.time_yesterday)+" "+sdf.format(currenTimeZone);
        }else{
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            if (calendar.before(inputTime)){
                SimpleDateFormat sdf = new SimpleDateFormat("M"+DemoApplication.getApplication().getResources().getString(R.string.time_month)+"d"+DemoApplication.getApplication().getResources().getString(R.string.time_day)+" HH:mm");
                return sdf.format(currenTimeZone);
            }else{
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy"+DemoApplication.getApplication().getResources().getString(R.string.time_year)+"MM"+DemoApplication.getApplication().getResources().getString(R.string.time_month)+"dd"+DemoApplication.getApplication().getResources().getString(R.string.time_day)+" HH:mm");
                return sdf.format(currenTimeZone);
            }

        }

    }
}
