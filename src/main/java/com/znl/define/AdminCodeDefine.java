package com.znl.define;

/**
 * 管理平台宏定义
 * Created by Administrator on 2015/12/21.
 */
public class AdminCodeDefine {

    /****
     *返回类型
     *100~199(表示参数错误，游戏方自定义)	返回具体错误参数，例子1
     * 200~299(表示flag标志错误，游戏方自定义)	验证失败，例子2
     * 300~399(表示数据失败，游戏方自定义)	用于返回失败的用户列表，例子3，此类型必须返回有数据的desc和data
     * 0(表示成功)
     *
     */
    public static final Integer ACTION_SUCCESS = 0; //成功
    public static final Integer ACTION_FAIL = -1; //未知失败

    public static final Integer ACTION_PARAMETER_FAIL = 101; //参数不全
    public static final Integer ACTION_UNKNOWN_FAIL = -3; //行为操作未知错误

    public static final Integer CHARGE_ORDER_ID_REPEAT = -4;
    public static final Integer CHARGE_CAN_NOT_FIND_PLAYER = -5;
    public static final Integer CHARGE_UNKOWNEN_TYPE = -6;//未知的充值类型

    public static final Integer CHARGE_TYPE_NORMAL = 0;//普通充值
    public static final Integer CHARGE_TYPE_MONTH_CARD = 1;//月卡充值
}
