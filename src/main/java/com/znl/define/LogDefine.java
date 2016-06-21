package com.znl.define;

/**
 * Created by Administrator on 2015/12/14.
 * 日志定义
 */
public class LogDefine {
    /****
     * 建筑操作类型定义
     ****/
    public static final int BUILDINGLEVEL = 1;//建筑升级
    public static final int BUILDINGLEVELSPEEDBYCOIN = 2;//建筑加速升级金币
    public static final int BUILDINGLEVELFINISH = 3;//建筑升级完成升级
    public static final int BUILDINGLEVELSPEEDBYITEM = 4;//建筑加速升级通过道具
    public static final int BUILDINGPRODUCT = 5;//生产
    public static final int BUILDINGPRODUCTSPEEDBYCOIN = 6;//使用金币生产加速完成生产
    public static final int BUILDINGPRODUCTSPEEDBYITEM = 7;//使用道具加速加速生产
    public static final int BUILDINGPRODUCTFINISH = 8;//完成生产

    /****
     * 装备日志类型定义
     ****/
    public static final int EQUIPLEVEL = 1;//装备升级
    public static final int EQUIPON = 2;//装备上装
    public static final int EQUIPOFF = 3;//装备下装
    public static final int EQUIPCHANGE = 4;//装备位置调换
    public static final int EQUIPLDROP = 5;//装备分解
    public static final int BUYEQUIPBAG = 6;//购买背包容量

    /****
     * 军械日志类型定义
     ****/
    public static final int ORDNANCECOMPL = 1;//军械碎片合成军械
    public static final int ORDNANCEPIECEDROP = 2;//军械碎片分解
    public static final int ORDNANCEON = 3;//穿上军械
    public static final int ORDNANCEOFF = 4;//卸下军械
    public static final int ORDNANCEDROP = 5;//分解军械
    public static final int ORDNANCESTRENGTH = 6;//强化军械
    public static final int ORDNANCESTRGAIZAO = 7;//改造军械
    public static final int ORDNANCESTRADVANCE = 7;//进化军械


    /***
     * 获得方式
     ****/
    public static final int GET_RESET_SKILL = 1;//重置技能
    public static final int GET_DROP_ORNDANCEPIECE = 2;//军械碎片分解
    public static final int GET_BUY_LOTTERTAOBAO = 3;// 淘宝购买幸运币
    public static final int GET_LOTTEREQUIP = 4;//装备抽奖
    public static final int GET_LOTTER_TAOBAO = 5;//掏宝
    public static final int GET_USE_ITEM = 6;//使用道具
    public static final int GET_DROP_ORNDANCE = 7;//分解军械
    public static final int GET_ORNDANCE_ADVANCE = 8;//军械进化
    public static final int GET_CHAT = 9;//作弊
    public static final int GET_INITITEM = 10;// 初始化资源
    public static final int GET_BUYITEMANDUSE = 11;//物品购买使用
    public static final int GET_SHOP_BUYITEM = 12;//商店购买获得道具
    public static final int GET_BUILD_PRODUCTION = 13;//建筑生产获取
    public static final int GET_GUANQIA = 14;//关卡挑战结束逻辑
    public static final int GET_OPEN_DUNGON_BOX = 15;//打开副本宝箱
    public static final int GET_MAIN_TASK_GETREWARD = 16;//主线任务
    public static final int GET_DAYLIY_TASK_GETREWARD = 17;//日常任务
    public static final int GET_DAYLIY_TASK_DAYACTIVITY = 18;//日常活跃
    public static final int GET_REQUEST_WISH = 19;//请求祝福
    public static final int GET_PICETOORNDANCE = 20;//碎片合成军械
    public static final int GET_REPAIRE_SOLDIER = 21;//修复佣兵
    public static final int GET_ADD_BAGSIZE = 22;// 背包扩充
    public static final int GET_SALE_EQUIP = 23;// 装备出售
    public static final int GET_WORLD_TASK_FINISH = 24;//任务执行完成，队伍回来
    public static final int GET_JUNXIAN_LV = 25;// 军衔升级
    public static final int GET_ATTACK_PLAYER_HOME = 26;// 攻击玩家基地
    public static final int GET_ATTACK_WOLDER_RES = 27;//攻打世界资源点
    public static final int GET_BUILDLEVE_UP = 28;//建筑升级
    public static final int GET_BUYBOOM = 29;//元宝购买繁荣度
    public static final int GET_ENERGY = 30;//元宝购买体力
    public static final int GET_FRIEND_WISH = 31;//好友祝福
    public static final int GET_LOGIN_EVERDAY = 32;//每日登陆
    public static final int GET_COMMOND_LVUP = 34;//统帅升级
    public static final int GET_PRESURE_LVUP = 35;//声望升级
    public static final int GET_BUY_GLOD = 36;// 购买金币
    public static final int GET_CANCEL_BUILD_LEVELUP = 37;// 取消建筑升级，生产
    public static final int GET_CANCEL_PRODUCTION = 38;// 取消生产
    public static final int GET_BUY_BUILD_POSITION = 39;//购买建筑位
    public static final int GET_RESOURCE_REGET = 40;//自动恢复
    public static final int GET_MADEL = 41;//授勋
    public static final int GET_MAIL = 42;//邮件提取
    public static final int GET_CHARGE = 43;//充值
    public static final int GET_ARENA_SHOP = 44;//竞技场商城
    public static final int GET_ARENA_WIN = 45;//竞技场胜利
    public static final int GET_ARENA_LASTREWARD = 46;//竞技场胜利
    public static final int GET_WORLD = 47;//世界
    public static final int GET_WORLD_FIGHT_TEAM_RETURN = 48;//世界队伍返回
    public static final int GET_AREAN_FIGHT = 49;//竞技场挑战完成
    public static final int GET_ARMYGROUP_SHOP = 50;//军团贡献值兑换
    public static final int GET_ARMYGROUP_CONTRIBUTE = 51;//军团捐献
    public static final int GET_ARMYGROUP_WELFAREREWARD = 52;//军团福利领取奖励
    public static final int GET_LIMITCHANGE_MOP = 53;//极限挑战扫荡
    public static final int GET_ACTIVITY_GET = 54;//活动领取
    public static final int GET_DAYLOTTERY = 55;//转盘
    public static final int GET_CDKEY = 56;//cdkey领取
    public static final int GET_THREETYREWARD = 57;//30天奖励
    public static final int GET_NEW_PLAYER = 58;//新手礼包
    public static final int GET_TAST_ACTIVITY = 59;//日常活跃领取
    public static final int GET_CLOSE_WORLD = 60;//关服后的登陆领取世界的补偿
    public static final int  GET_LEGIONSHARE_RECHARGE = 61;//获得有福同享的充值奖励
    public static final int  GET_LEGIONSHARE_SHARE = 62;//获得有福同享的分享奖励
    public static final int GET_LABA_LOTTER = 63;//限时活动获得拉霸奖励
    public static final int GET_ADVISER_LV = 64;//军师升级
    public static final int GET_ADVISER_DROP = 65;//军师分解
    public static final int GET_ADVISER_JINJIE = 66;//军师进阶
    public static final int GET_ADVISER_LOTTERY = 67;//军师抽奖
    public static final int GET_WORLD_ATTACK_FAIL = 68;//世界战斗超过30回合的佣兵返还
    /***获得方式****/

    /***
     * 失去方式
     ****/
    public static final int LOST_USEITEM = 1001;//使用道具
    public static final int LOST_SKILL_LV = 1002;//技能升级
    public static final int LOST_UP_COMMOND_LV = 1003;// 统帅升级
    public static final int LOST_LOTTER_TAO = 1004;// 执行淘宝装备抽奖
    public static final int LOST_ORNDANCE_STRENGTH = 1005;//强化军械
    public static final int LOST_ORNDANCE_GAIZAO = 1006;//改造军械
    public static final int LOST_BUILDING_SPEEDLV = 1007;//建筑升级加速
    public static final int LOST_BUILDING_SPEEDPRODUCTION = 1008;//建筑生产加速
    public static final int LOST_UPEQUIP_LV = 1009;//装备升级
    public static final int LOST_EQUIP_SALE = 1010;//装备售出
    public static final int LOST_ORDANCE_DROP = 1011;//分解军械
    public static final int LOST_ORDANCE_ADVANCE = 1012;//军械进化
    public static final int LOST_DROP_DROP = 1013;//军械碎片分解
    public static final int LOST_FIGHT_REDUCE = 1015;//战损
    public static final int LOST_SOLDIER_GAIZAO = 1016;//兵种改造
    public static final int LOST_CHALLENGE_DUNGON = 1017;//挑战副本
    public static final int LOST_RISK_TIMES = 1018;//购买冒险次数
    public static final int LOST_ADD_BAGSIZE = 1019;//背包扩充
    public static final int LOST_BUY_LUCKYCOIN = 1020;// 淘宝购买幸运币
    public static final int LOST_LOTTER_EQUIP = 1021;// 装备抽奖
    public static final int LOST_DO_WORLD_TASK = 1022;// 执行世界任务
    public static final int LOST_MilitaryLevetUp = 1023;//军衔升级
    public static final int LOST_BUY_BOOLD = 1024;//购买荣誉
    public static final int LOST_BUYENERGY = 1025;//元宝购买体力
    public static final int LOST_PIECE_TO_ORNDANCE = 1026;//军械碎片合成军械
    public static final int LOST_BEAT_WORLD = 1027;//攻打世界
    public static final int LOST_medalPRESTIGE = 1028;//授勋获取声望
    public static final int LOST_PRESTIGE_LEVEUP = 1029; //声望升级
    public static final int LOST_TANK_PRODUCTION = 1030; //坦克生产
    public static final int LOST_BUID_LEVELUP = 1031; ////建筑升级
    public static final int LOST_BUY_BUILD_POSITION = 1032; ////购买建筑位
    public static final int LOST_ITEM_BUYANDUSE = 1033; ////物品购买是有
    public static final int LOST_MADE_PRODUTION = 1034;//制造车间生产
    public static final int LOST_SHOP_BUY = 1035; //商店购买
    public static final int LOST_SKILL_RESET = 1036; //重置技能
    public static final int LOST_REPAIR_SOLDIER = 1037; //修复佣兵
    public static final int LOST_FINISH_TASK_BYGOLD = 1038; //使用金币完成任务
    public static final int LOST_REFRESH_TASK = 1039; //刷新任务
    public static final int LOST_RESET_TASK = 1040; //重置任务
    public static final int LOST_SCIENCE_LEVEUP = 1041; //科技升级
    public static final int LOST_DAYLIY_REFRESH = 1042; //日常刷新
    public static final int LOST_BUY_MONTH_CARD = 1044;//购买月卡
    public static final int LOST_BUY_CHEAT = 1045;//作弊
    public static final int LOST_BUY_FIGHT_TIMES = 1046;//购买竞技场挑战次数
    public static final int LOST_ARENA_SHOP_BUY = 1047;//购买竞技场商店购买
    public static final int LOST_CREATE_ARMY = 1048;//创建军团
    public static final int LOST_SPEED_ARENA = 1049;//加速竞技场
    public static final int LOST_DETECT = 1050;//侦查
    public static final int LOST_WORLD_FIGHT = 1051;//世界战斗
    public static final int LOST_ARMYGROUP_SHOP = 1052;//军团商店贡献值兑换
    public static final int LOST_WORLD_BE_ATTACK = 1053;//被攻打
    public static final int LOST_TASK_BUY_QUICK = 1054;//购买加速完成任务队伍
    public static final int LOST_ARMYGROUP_WELFAREREWARD = 1055;//领取军团福利
    public static final int LOST_ARMYGROUP_CONTRIBUTE = 1056;//军团捐献
    public static final int LOST_MOVE_CITY = 1057;//迁城
    public static final int LOST_ACTIVITY_BUY = 1058;//活动购买
    public static final int LOST_DROP_BUILD = 1059;//拆除建筑
    public static final int LOST_BUY_AUTO = 1060;//购买自动升级
    public static final int LOST_LIMIT_CHANGE = 1061;//极限挑战
    public static final int LOST_LEGION_HELP = 1062;//军团驻军
    public static final int LOST_LABA_LOTTER = 1063;//用金币进行拉霸抽奖
    public static final int LOST_ADVISER_LV = 1064;//军师升级
    public static final int LOST_ADVISER_DROP = 1065;//军师分解
    public static final int LOST_ADVISER_JINJIE = 1066;//军师进阶
    public static final int LOST_ADVISER_LOTTERY = 1067;//军师抽奖
    public static final int LOST_WORLD_ATTACK = 1068;//派出攻打
    /***失去方式****/


    /****
     * 平台相关
     ****/
    public static final int ADMIN_BATTLE_ID_ARENA = 1;//竞技场
    public static final int ADMIN_BATTLE_ID_WORLD = 2;//野外基地对战
    public static final int ADMIN_BATTLE_ID_WORLD_RESOURCE = 3;//野外资源对战
    public static final int ADMIN_BATTLE_ID_WORLD_FIGHT = 4;//野外资源占领
}
