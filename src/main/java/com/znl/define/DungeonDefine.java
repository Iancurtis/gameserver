package com.znl.define;

/**
 * Created by Administrator on 2015/11/18.
 */
public class DungeonDefine {

    public static final int EXTREME_ADVENTRUE = 4;

    /*关卡1星满足条件*/
    public static final int ONE_STAR_CONDITION = 70;
    /*关卡2星满足条件*/
    public static final int TWO_STAR_CONDITION = 90;
    /*关卡3星满足条件*/
    public static final int THREE_STAR_CONDITION = 100;

    /*购买冒险次数,消耗元宝初始值:98*/
    public static final int BUY_ADVANCE_TIMES_EXPEND = 98;
    /*购买冒险次数,购买次数达到5次后固定花费:298*/
    public static final int BUY_ADVANCE_TIMES_FIX_EXPEND = 298;
    /*购买冒险次数,逐步增加元宝:50*/
    public static final int BUY_ADVANCE_TIMES_EXPEND_ADD = 50;
    /*购买冒险次数,每次购买增加数:5*/
    public static final int ADVANCE_TIMES = 5;

    /*购买类型，1:获取元宝的数目 2:购买请求*/
    public static final int BUY_ADVANCE_TIMES_TYPE_ASK = 1;
    public static final int BUY_ADVANCE_TIMES_TYPE_BUY = 2;

    /*西域远征*/
    public static final int DEOGEO_LIMIT_REST = 1;//重置默认次数
    public static final int DEOGEO_LIMIT_CHANGE = 3;//挑战次数默认
    public static final int DEOGEO_LIMIT_MOP = 1;//扫荡次数默认
    // /*军团副本*/
    public static final int LEGION_DUNGEO = 5;//军团副本相关操作参数
}
