package com.znl.define;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2015/11/16.
 */
public class TaskDefine {
    /*任务读表类型*/
    public static final int TABLE_TASK_MAIN_LINE = 1;//主线任务
    public static final int TABLE_TASK_DAY = 2;//日常任务
    public static final int TABLE_TASK_ACTIVITY_DAY= 3;//日常活跃


    /*********任务类型**********/
    public static final int TASK_TYPE_SHENGWANG_LV=1;//声望等级
    public static final int TASK_TYPE_JUNXIAN_LV=2;//军衔等级
    public static final int TASK_TYPE_BEATGATE_TIMES=3;//征战关卡次数
    public static final int TASK_TYPE_BEATWORLD_TIMES=4;//攻打世界玩家次数
    public static final int TASK_TYPE_WINRESOURCE_LV=5;//攻打资源点等级
    public static final int TASK_TYPE_BUILDING_LV=6;//建筑物等级
    public static final int TASK_TYPE_BUILDING_NUM=7;//建筑物建造数
    public static final int TASK_TYPE_CREATESODIER_NUM=8;//生成兵种数量
    public static final int TASK_TYPE_RESOURCE_VALUE=9;//资源产量
    public static final int TASK_TYPE_WINGATE_ID=10;//战胜关卡
    public static final int TASK_TYPE_COSTGOLD_TIMES=11;//消耗金币次数
    public static final int TASK_TYPE_BUILDLEVEUP_TIMES=12;//升级建筑次数
    public static final int TASK_TYPE_SCIENCELV_TIMES=13;//研发科技次数
    public static final int TASK_TYPE_EQUIPLVUP_TIMES=14;//升级装备次数
    public static final int TASK_TYPE_ARENAFIGHT_TIMES=15;//竞技场挑战次数
    public static final int TASK_TYPE_EQUIPTANXIAN_TIMES=16;//装备探险次数
    public static final int TASK_TYPE_ORNDANCETANXIAN_TIMES=17;//配件探险次数
    public static final int TASK_TYPE_JIXIANTANXIAN_TIMES=18;//极限探险次数
    public static final int TASK_TYPE_FEIXUXUNBAO_TIMES=19;//废墟寻宝次数
    public static final int TASK_TYPE_BEATEWORLDRESOUCE_TIMES=20;//攻打世界资源点次数
    public static final int TASK_TYPE_UNIONCONTRIBUTE_TIMES=21;//军团捐献次数
    public static final int TASK_TYPE_UNIONARENA_LV=22;//军团试练场次数
    public static final int TASK_TYPE_GETUNIONTESTBOX_NUM=23;//领取军团试练箱个数
    public static final int TASK_TYPE_UNIOMCONVER_TIMES=24;//兑换军团商店道具次数
    public static final int TASK_TYPE_UNIONREST_TIMES=25;//军团驻扎次数
    public static final int TASK_TYPE_LOTTEREQUIP_TIEMS=26;//抽取长辈次数
    public static final int TASK_TYPE_ORNDANCESTENGTH_TIMES=27;//强化配件次数
    public static final int TASK_TYPE_ZHAOMUGENERAIS_TIMES=28;//招募将领次数
    public static final int TASK_TYPE_ADVANCEGENERAIS_TIMES=29;//进阶将领次数


    /************任务状态***********/
    public static final int TASK_STATUS_ACCEPT=1;//接受
    public static final int TASK_STATUS_UNACCEPT=0;//未接受

    /************任务完成状态***********/
    public static final int TASK_STATUS_UNFISH=0;//未完成
    public static final int TASK_STATUS_FINISH=1;//完成了
    public static final int TASK_STATUS_HASGET=2;//已经领取
    public static final int TASK_STATUS_DELETE=3;//删除


    /************任务定义**********/
    public static final int TASK_DAY_RESET_TIMES=3;

    /***
     * 执行任务状态
     ***/
   public static final int PERFORM_TASK_STATE_TODO = 1;//去执行
   public static final int PERFORM_TASK_STATE_DOING = 2;//执行中
   public static final int PERFORM_TASK_STATE_DONE = 3;//执行完回来

    /***
     * 执行任务对象
     ***/
    public static final int PERFORM_TASK_ATTACK = 1;//进攻
    public static final int PERFORM_TASK_DIGGING = 3;//挖矿
    public static final int PERFORM_TASK_RETURN =2;//返回
    public static final int PERFORM_TASK_GOHELP = 4;//出发驻军
    public static final int PERFORM_TASK_HELPBACK = 5;//驻防中
    public static final int PERFORM_TASK_OTHERHELPBACK = 6;//别人的驻军

    //提示状态
    public static final int NOTICE_TASK_ATTACK = 1;//进攻
    public static final int NOTICE_TASK_GOHELP = 2;//驻军中
    public static final int NOTICE_TASK_HELPCOMING = 3;//别人过来驻军

    public static final int WORLD_TASK_TIME_LIMIT = 187;//任务部队的起始时间
    public static final int WORLD_TASK_TIME_EACH = 8;//任务部队的单位时间




}
