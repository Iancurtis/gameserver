package com.znl.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/11/19.
 */
public class RandomUtil {

    /******** 0 - num 中获取一个数***************/
    public static int random(int Num)
    {
        return (int)(Math.random() * Num);
    }

    /***********0 - num 中获取一个数 times次数 pro/Num*****/
    public static int randomTimes(int Num, int times,int proba) {
        int n = 0;
        while (times>0) {
            times--;
            int num = random(Num);
            if(num<proba){
                n++;

            }
        }
        return n;
    }

    /********抽牌随机***************/
    public static int randomByShuffleCards(int size) {
        List<Integer> num = new ArrayList<Integer>(size);
        for (int i = 0; i < size; i++) {
            num.add(i);
        }
        List<Integer> num2 = new ArrayList<Integer>(size);
        // 将num的元素取随机加到num2中
        while (num.size() > 0) {
            int ran = (int) (Math.random() * num.size());
            num2.add(num.get(ran));
            num.remove(ran);
        }
        List<Integer> num3 = new ArrayList<Integer>(size);
        // 再来一次
        while (num2.size() > 0) {
            int ran = (int) (Math.random() * num2.size());
            num3.add(num2.get(ran));
            num2.remove(ran);
        }
        int index =  (int)(Math.random() * size);
        return num3.get(index);
    }

    // min - max 获取一个数
    public static int random(int Min, int Max)
    {
        int tmp;
        double Num = (Math.random()*((double)Max - (double)Min));
        if(Num - Math.floor(Num) >= 0.5)
            tmp = (int)Math.ceil(Num);
        else
            tmp = (int)Math.floor(Num);

        return Min + tmp;
    }

}
