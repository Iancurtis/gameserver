package com.znl.define;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2016/1/16.
 */
public class ActivityDefine {
    public static final int ACTIVITY_CONDITION_TYPE_FIRST_CHARGE = 101;//条件：首次充值
    public static final int ACTIVITY_CONDITION_TYPE_FIRST_ONLINETIME = 102;//条件：建号当日在线时间
    public static final int ACTIVITY_CONDITION_TYPE_AFTER_LONGIN = 103;//条件：建号第几天登陆游戏
    public static final int ACTIVITY_CONDITION_TYPE_VIP_COST = 104;//条件：VIP x以上的玩家花费金币购买
    public static final int ACTIVITY_CONDITION_TYPE_BUY_COSTCOIN = 105;//条件：花费金币购买
    public static final int ACTIVITY_CONDITION_TYPE_COM_LEVEL = 106;//条件：指挥官达到指定等级
    public static final int ACTIVITY_CONDITION_TYPE_EVERY_DAY_CHARGE = 107;//条件：每日充值满x金币
    public static final int ACTIVITY_CONDITION_TYPE_ITEM_ONSALE = 108;//条件：物品打折
    public static final int ACTIVITY_CONDITION_TYPE_BUY_TIMES = 109;//条件：限购买次数
    public static final int ACTIVITY_CONDITION_TYPE_BUY_TIMES_REST = 110;//条件：限购买次数，每日重置
    public static final int ACTIVITY_CONDITION_TYPE_EVERY_CHARGE_FIRST = 111;//条件：每个充值档次的首次充值
    public static final int ACTIVITY_CONDITION_TYPE_PUTON_PURPLE_EQUIP_NUM = 112;//条件：穿戴满x件紫色装备
    public static final int ACTIVITY_CONDITION_TYPE_LEVEL_PURPLE_EQUIP_NUM = 113;//升级x件紫色装备到y级（包括仓库的和已穿戴的）
    public static final int ACTIVITY_CONDITION_TYPE_BUILD_LEVEL = 114;//条件：x建筑等级到达y级
    public static final int ACTIVITY_CONDITION_TYPE_CAPITY_RANK = 115;//条件：结算时战力排行进全服x名
    public static final int ACTIVITY_CONDITION_TYPE_GUANQIA_RANK = 116;//条件：结算时关卡排名进全服x名
    public static final int ACTIVITY_CONDITION_TYPE_HONOR_RANK = 117;//条件：结算时荣誉排名进全服x名
    public static final int ACTIVITY_CONDITION_TYPE_LEVEL_RANK = 118;//条件：结算时等级排名进全服x名
    public static final int ACTIVITY_CONDITION_TYPE_VIPPEOPLE_NUM= 119;//条件：全服指定vip等级玩家达到x数量
    public static final int ACTIVITY_CONDITION_TYPE_VIP_GETREWARD= 120;//条件：对应vip等级的玩家领取
    public static final int ACTIVITY_CONDITION_TYPE_BEAR_WORLD_TIMES= 121;//条件：世界地图攻打玩家x次
    public static final int ACTIVITY_CONDITION_TYPE_BEAR_ARNENA_WINTITIMES_EVERYDAY= 122;//条件：每天竞技场胜利x次
    public static final int ACTIVITY_CONDITION_TYPE_RESOUCE_GETNUM= 123;//条件：采集x数量的y资源
    public static final int ACTIVITY_CONDITION_CHARGE_EVERYDAY_ATWILL= 124;//条件：每天任意充值
    public static final int ACTIVITY_CONDITION_CHARGE_ATWILL= 125;//条件：任意充值
    public static final int ACTIVITY_CONDITION_CHARGE_CONTIUNES= 126;//条件：连续x天充值
    public static final int ACTIVITY_CONDITION_ADVANCE_GENEL= 127;//条件：进阶x个指定品质将领
    public static final int ACTIVITY_CONDITION_RANK_LEGION= 128;//条件：结算时全服军团战力排名前x的军团，军团里战力前y名的玩家可以领取
    public static final int ACTIVITY_CONDITION_ONLINE_TIME= 129;//条件：在线x时间
    public static final int ACTIVITY_CONDITION_DONVATE_IN_HALL_RESOURCE= 130;//条件：军团大厅捐献x次资源
    public static final int ACTIVITY_CONDITION_DONVATE_IN_SCIENCE_RESOURCE= 131;//条件：军团科技捐献x次资源
    public static final int ACTIVITY_CONDITION_DONVATE_IN_HALL_COIN= 132;//条件：军团大厅捐献x次金币
    public static final int ACTIVITY_CONDITION_DONVATE_IN_SCIENCE_COIN= 133;//条件：军团科技捐献x次金币
    public static final int ACTIVITY_CONDITION_DONVATE_IN_HALL= 134;//条件：军团大厅捐献
    public static final int ACTIVITY_CONDITION_DONVATE_IN_SCIENCE= 135;//条件：军团科技捐献
    public static final int ACTIVITY_CONDITION_DONVATE_RANK= 136;//条件：军团捐献排名前x名
    public static final int ACTIVITY_CONDITION_LEGION_DUNGON_RANK= 137;//条件：全服军团副本击杀进度排名前x军团
    public static final int ACTIVITY_CONDITION_LEGIONPLAYER_DUNGON_RANK= 138;//条件：全服军团副本击杀进度排名前x军团所有成员
    public static final int ACTIVITY_CONDITION_HIT_GUANQIA_TIMES= 139;//条件：攻打关卡x次
    public static final int ACTIVITY_CONDITION_ZHENGFU_GUANQIA_TIMES= 140;//条件：攻打征服关卡
    public static final int ACTIVITY_CONDITION_HAVE_LEGION= 141;//条件：加入军团
    public static final int ACTIVITY_CONDITION_ENERY_EVERYDAY= 142;//条件：每天领取军令

    // 增益
    public static final int ACTIVITY_CONDITION_LEGION_DERONATE_RETRUEN=201;//军团大厅采用金币贡献，返还x%金币
    public static final int ACTIVITY_CONDITION_LEGION_SCIENCE_GOLD_RETURN=202;//军团科技采用金币贡献，返还x%金币
    public static final int ACTIVITY_CONDITION_EUIP_ADVANCE_ADDTIMES=203;//每日增加x次装备探险次数
    public static final int ACTIVITY_CONDITION_ORDANCE_ADVANCE_ADDTIMES=204;//每日增加x次配件探险次数
    public static final int ACTIVITY_CONDITION_ORDANCE_ADVACE_BUYTIMES=205;//配件关卡额外次数购买返x%金币
    public static final int ACTIVITY_CONDITION_ORDANCE_DAMAGE_ADD=206;//配件关卡伤害加成x%
    public static final int ACTIVITY_CONDITION_EQUIP_BUYADDVACES_RETURN_GOLD=207;//装备关卡额外次数购买返x%金币
    public static final int ACTIVITY_CONDITION_EQUIP_DAMAGE_ADD=208;//装备关卡伤害加成x%
    public static final int ACTIVITY_CONDITION_ORDANCE_STRENGTH=209;//配件强化失败，仅扣除x%宝石
    public static final int ACTIVITY_CONDITION_ORDANCE_remou=210;//改造不降低强化等级
    public static final int ACTIVITY_CONDITION_EQUIP_LEVEL_UP_ADDEXP=211;//升级装备时装备卡提供的经验增加x%
    public static final int ACTIVITY_CONDITION_EQUIP_LOTTER_ONCE=212;//顶级单抽打折
    public static final int ACTIVITY_CONDITION_EQUIP_LOTTER_TEN=213;//顶级9连抽打折
    public static final int ACTIVITY_CONDITION_LOTTER_GENEL_GOLD=214;//招募将领花费金币优惠
    public static final int ACTIVITY_CONDITION_LOTTER_GENEL_BAOSHI=215;//招募将领花费宝石优惠
    public static final int ACTIVITY_CONDITION_BUILD_LEVEL_REDUCE=216;//升级建筑资源消耗减少x%
    public static final int ACTIVITY_CONDITION_BUILD_LEVEL_SPEED=217;//升级建筑速度增加x%
    public static final int ACTIVITY_CONDITION_CREATE_TANKE_SPEED=218;//生产坦克速度增加x%
    public static final int ACTIVITY_CONDITION_CHANGE_TANK_SPEED=219;//改造坦克速度增加x%
    public static final int ACTIVITY_CONDITION_RESOUCE_ADD=220;//所有资源基础产量增加x%
    public static final int ACTIVITY_CONDITION_LEVEL_TONGSHUAI_SUCCESS=221;//统率升级成功率增加x%基础值=【成功率50%，成功率 = 50%*（1+10%） = 55%】
    public static final int ACTIVITY_CONDITION_FIGHT_GUANQIA_ADDEXP=222;//攻打关卡增加x%基础经验
    public static final int ACTIVITY_CONDITION_FIGHT_WORLD_ADDEXP=223;//攻打世界资源点增加x%基础经验
    public static final int ACTIVITY_CONDITION_FIGHT_WORLD_REWARD_RATE=224;//攻打世界资源点道具掉落概率增加x%
    public static final int ACTIVITY_CONDITION_RESOURCE_SCIECE_SPEED=225;//资源类科技研发速度提升x%
    public static final int ACTIVITY_CONDITION_RESOURCE_SCIENCE_NEEEDRESOURCE=226;//资源类科技研发只需x%资源

    /**按钮状态**/
    public static final int ACTIVITY_BUTTON_TYPE_BUY = 2;//购买
    public static final int ACTIVITY_BUTTON_TYPE_GET = 1;//领取

    /**特殊活动标志**/
    public static final int FIRST_CHARGE_UITYPE = 1;//首次充值
    public static final int INVESTMENT_UITYPE = 2;//投资计划
    public static final int CHARGE_EVERY_DAY_UITYPE = 4;//每日充值
    public static final int POWER_RANK_UITYPE = 5;//排行榜
    public static final int TEXT_UITYPE = 6;//文字类型

    public static final int ACTIVITY_STATE_GOING = 1;//进行中
    public static final int ACTIVITY_STATE_DONE = 2;//结束

    public static final int ACTIVITY_ACTION_ADD = 1;//增加
    public static final int ACTIVITY_ACTION_REMOVE = 2;//修改

    /****投资状态****/
    public static final int ACTIVITY_STATE_UNINVEST = 0;//未投资
    public static final int ACTIVITY_STATE_INVEST = 1;//投资

    public static final int INVESTMENT_PRICE = 500;//投资计划价格
    public static final int INVESTMENT_VIP = 2;//需要的vip等级

    /****计算连续充值天数****/
    public static final int  NO_CONTINUOUS = 0;//需要的vip等级
    public static final int CONTINUOUS = 1;//需要的vip等级

    /******限时活动*******/
    public static final int LABA_UITYPE=11;//拉霸活动
    public static final int LABA_FREE_LOTTER_TYPE=1;//拉霸抽奖类型（免费抽）
    public static final int LABA_ONE_LOTTER_TYPE=2;//拉霸抽奖类型（单抽）
    public static final int LABA_TEN_LOTTER_TYPE=10;//拉霸抽奖类型（10连抽）
    public static final int LIMIT_ACTION_LEGIONSHARE_ID=12;//限时活动有福同享活动uitypeId
    public static final int LIMIT_ACTION_LABA_ID=11;//限时活动拉霸uitypeId


    public final static List<Integer> rewardinEndTime = Arrays.asList(115,116,117,118,136,137,138);//时间到结算活动

}
