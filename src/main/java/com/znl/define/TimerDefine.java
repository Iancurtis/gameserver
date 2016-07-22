package com.znl.define;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2015/11/6.
 */
public class  TimerDefine {
 /*******定时器类型**************/
 public static final int DEFAULT_ENERGY_RECOVER = 1;//恢复体力
 public static final int ADVENCE_REFRESH= 2;//副本探险次数
 public static final int BUILDING_LEVEL_UP= 3;//建筑升级
 public static final int DEFAULT_BOOM_RECOVER = 4;//恢复繁荣度
 public static final int DEFAULT_GET_PRESTIGE = 5;//登录领取声望
 public static final int TIMER_TYPE_RESOUCE = 6;//资源产出定时器
 public static final int FRIEND_BLESS = 7; //好友祝福获得体力
 public static final int BUY_ENERGY = 8; //购买体力
 public static final int BUILD_CREATE =10; //建筑生产
 public static final int TECHNOLOGY_LEVEL_UP = 11; //科技升级
 public static final int PERFORM_TASK = 12; //执行任务
 public static final int ITEM_BUFF = 13; //道具使用增加玩家buff
 public static final int DEFAULT_TODAYGET_PRESTIGE = 14;//今日授勋声望
 public static final int TIMIER_LOTTERY= 15;//武将抽奖次数//免费时间
 public static final int TIMIER_LOTTERY_TAOBAO= 16;//淘宝免费
 public static final int FRIEND_DAY_BLESS = 17; //每日的祝福可获取奖励的次数
 public static final int FRIEND_DAY_GET_BLESS = 18; //每日可领取祝福奖励的次数
 public static final int FRIEND_DAY_ACTIVITY = 19; //日常活跃
 public static final int FRIEND_DAY_MESSION= 20; //日常任务次数
 public static final int FRIEND_DAY_BE_BLESS = 21; //好友被祝福的定时器
 public static final int BUY_ADVANCE_TIMES = 22; //购买冒险次数
 public static final int ARENA_FIGHT = 23; //竞技场等待时间
 public static final int ARENA_TIMES = 24; //竞技场挑战次数
 public static final int ARENA_ADD_TIMES = 26; //竞技购买挑战次数
 public static final int TIMIER_LOTTERY_TODAY= 25;//武将抽奖免费今日抽过的次数
 public static final int LASTARENAREWAED= 27;//竞技场上期排名
 public static final int LOGIN_DAY_NUM = 28;//30天登录奖励
 public static final int LOGIN_LOTTERY = 29;//每日登录抽奖
 public static final int ARMYGROUP_SHOP = 30;//军团兑换商店
 public static final int ARMYGROUP_HALL_CONTIBUTE = 31;//军团大厅捐献金币,资源
 public static final int ARMYGROUP_TECH_CONTIBUTE = 32;//军团科技捐献金币,资源
 public static final int ARMYGROUP_WELFAREREWARD = 33;//军团福利院领取福利
 public static final int DAY_TASK_REST = 34;//日常任务重置次数
 public static final int LIMIT_CHANGET_TIMES = 35;//极限挑战次数
 public static final int LIMIT_CHANGET_REST = 36;//极限重置次数
 public static final int LIMIT_CHANGET_MOPPING = 37;//极限挑战扫荡
 public static final int ACTIVITY_REFRESH = 38;//活动每分钟刷新
 public static final int BUILD_AUTO_LEVLE_UP = 39;//建筑自动升级
 public static final int BUILD_DEGREE = 40;//繁荣度达到满值
 public static final int LEGION_ALLTIME_DONATE= 41;//总科技捐赠次数
 public static final int LABA_LOTTER_FREETIME= 42;//拉霸免费抽奖次数
 public static final int ACTIVITY_DEL_FREETIME= 43;//下一个活动删除刷新时间  0表示没活动删除了
 public static final int ACTIVITY_ADD_FREETIME= 44;//下一个活动要增加的的时间  0表示没活动增加了
 public static final int JUNSHILOTTERY= 45;//军师抽奖
 public static final int ARMYGROUP_REFRESH= 46;//军团副本挑战次数
 public static final int ARMYGROUP_DUNGEO_REFRESH= 47;//军团副本没日刷新
/*****手动充值的定时器类型*****/
 public final static List<Integer> TIMER_REFRESH_BYHAND = Arrays.asList(1,5,14,21);

 /**时间定义**/
 public static final int DEFAULT_TIME_RECOVER = 30*60*1000;//恢复体力所需时间:30分钟
 public static final int DEFAULT_TIME_BOOM = 60*1000;//恢复繁荣度所需时间:1分钟
 public static final int ONE_DAY = 60*60*24*1000;//一天的毫秒数
 public static final long BUFF_MSEL = 60*1000;//道具buff的开始结束时间转为毫秒数
 public static final int ARENA_FIGHT_RESHTIME = 10*60*1000;//竞技场刷新毫秒数

 public static final int DEFAULT_TIME_RESOUCE = 60;//资源校验时间

 /*******次数定义********/
 public static final int ARENA_FIGHT_TIMES=5;//每天初始次数


 /*******定时器刷新类型**************/
 public static final int TIMER_REFRESH_NONE = -1;//不用每天刷新
 public static final int TIMER_REFRESH_ZERO= 0;//0点刷新(后面全部改为4点)
 public static final int TIMER_REFRESH_TWTEEN=12;//12点刷新(后面全部改为4点)
 public static final int TIMER_REFRESH_FOUR= 4;//4点刷新

 /*******自动升级开关********/
 public static final int BUILDAUTOLEVEL_OPEN = 1;//开启自动升级
 public static final int BUILDAUTOLEVEL_OFF = 0;//自动升级关掉
 public static final int BUILDAUTOLEVELPRICE = 238;//自动升级价格
 public static final long BUILDAUTOLEVEL_ADDTIME=4*60*60*1000;//持续的时间

 public static final int RONCUOONEMINUTE=1*60*1000;//1分钟

 public static final int AUTLUBUILDLEVE=1;//建筑自动升级


 /*******容差时间*******/
 public static final int TOLERNACE_TIME = 30;//允许的前后端差距时间

 /******重置定时类型*******/
 public static final int CHANGE_TIMER_INIT_TYPE_LOGIN = 0;
 public static final int CHANGE_TIMER_INIT_TYPE_BUY_TIME = 3;

 /*********下午4点触发*********/
 public static final int TIMER_TEST = -1;
 public static final int TIMER_1M = 2;//每分钟触发
 public static final int TIMER_1H = 3;//每小时触发
 public static final int TIMER_4PM = 14;//每天下午四点
}
