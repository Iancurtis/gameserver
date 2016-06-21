package com.znl.utils;

import scala.tools.cmd.gen.AnyVals;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Administrator on 2015/11/20.
 */
public class DateUtil {
    /******
     * time1当前时间
     * time2比较的时间
     * hour 对比时间点
     *********/
    public static boolean isCanGet(long time1, long time2, int hour) {
        Calendar c2 = Calendar.getInstance();
        c2.setTime(new Date(time2));
        int time2hour = c2.get(Calendar.HOUR_OF_DAY);
        if (time2hour >= hour) {
            c2.add(Calendar.DATE, 1);
        }
        c2.set(Calendar.HOUR_OF_DAY, hour);
        c2.set(Calendar.MINUTE, 0);
        c2.set(Calendar.SECOND, 0);
        if (time1 >= c2.getTime().getTime()) {
            return true;
        }
        return false;
    }

    //获取当前游戏时间 多少秒后的时间戳
    public int getTimeStampAfterSecond(int second) {
        return GameUtils.getServerTime() + second;
    }

    /******
     * time1当前时间
     * hour下个时间点
     *********/
    public static long getNextHour(long time1, int hour) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(new Date(time1));
        int nhour = c1.get(Calendar.HOUR_OF_DAY);
        if (hour <= nhour) {
            c1.set(Calendar.HOUR_OF_DAY, hour);
            c1.add(Calendar.DAY_OF_YEAR, 1);
            c1.set(Calendar.MINUTE, 0);
            c1.set(Calendar.SECOND, 0);
        } else {
            c1.set(Calendar.HOUR_OF_DAY, hour);
            c1.set(Calendar.MINUTE, 0);
            c1.set(Calendar.SECOND, 0);
        }
        return c1.getTime().getTime();
    }

    //获得当前时间小时跟分钟组成的一个数字
    public static int getNowHoureMinitetoInt(){
        Calendar c1 = Calendar.getInstance();
        c1.setTime(GameUtils.getServerDate());
        int hour=c1.get(Calendar.HOUR_OF_DAY);
        int min=c1.get(Calendar.MINUTE);
        return hour*100+min;
    }


    public static int getDaysBetweenTowTime(Date timeBefore, Date timeAfter) {
        long dateBefore = timeBefore.getTime() / 1000 / 60 / 60 / 24;
        long dateAfter = timeAfter.getTime() / 1000 / 60 / 60 / 24;
        long dayDiff =dateBefore- dateAfter;
        return (int) (dayDiff);
    }

    public static String getDateFormt(Long time) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date date = new Date(time);
            return simpleDateFormat.format(date);
        } catch (Exception e) {
            System.out.println("erro" + e);
        }
        return "";
    }

    public static int getDayIntwoTime(Long time1,Long time2){
        long days = 0;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date d1 = new Date(time1);
            Date d2 = new Date(time2);
            d1=format.parse(format.format(d1));
            d2=format.parse(format.format(d2));
            days = (d2.getTime() - d1.getTime()) / (24*3600*1000);
            if(days==0){
                days=1;
            }
            return (int)Math.abs(days);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return (int)Math.abs(days);
    }
}
