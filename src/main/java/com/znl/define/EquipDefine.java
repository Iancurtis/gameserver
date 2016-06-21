package com.znl.define;

/**
 * Created by Administrator on 2015/11/27.
 */
public class EquipDefine {
    /****装备类型定义*****/
    public static final int EQUIP_TYPE_EQUOP=1;//使用的装备
    public static final int EQUIP_TYPE_EXP=2;//装备经验卡
    public static final int EQUIP_TYPE_RANDOM=3;//随机装备

    /****装备背包最大容量*****/
    public static final int EQUIP_MAX_SIZE=300;
    /****装备背包扩大价格*****/
    public static final int EQUIP_ADD_BAG_PRICR=50;
    /****装备背包扩大大小*****/
    public static final int EQUIP_ADD_BAG_SIZE=20;
    /****军械最大容量*****/
    public static final int ORDANCE_MAX_SIZE=70;



    /********军械碎片分解获得道具id*****/
    public static final int DROP_ORDANC_RETURN_ITEM=4018;

    /********配件强度计算底值攻击生命*****/
    public static final int ORDANC_STRENTH_TYPE1=800;

    /********配件强度计算底值穿刺，防护*****/
    public static final int ORDANC_STRENTH_TYPE2=20;

    /********军械强化使用的道具id****/
    public static final int ORDNANCE_STRENGTH_ADDRATE_ITEM=4019;

    /********军械强化使用的道具增加概率****/
    public static final int ORDNANCE_STRENGTH_ADDRATE_NUM=50;



    /********军械改造使用道具id****/
    public static final int ORDNANCE_REMOULD_ADDRATE_NUM=4020;

    /********军械改造需要的强化等级****/
    public static final int ORDNANCE_REMOULD_NEED_STRENGTH_LEVEL=5;

    /********万能碎片Id****/
    public static final int SUPER_ORDNANCEPICE=301;

    /********淘宝连抽次数****/
    public static final int TAOBAO_NUM=10;


    /********可出战的军师类型****/
    public static final int FIGHT_ADVISER_TYPE=1;

    /********军师进阶所需数量****/
    public static final int ADVISER_ADVANCE_NUM=6;

    /********军师府容量*******/
    public static final int ADVISER_JUNSHIFU_MAXNUM=200;
}
