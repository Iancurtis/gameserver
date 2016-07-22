package com.znl.define;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;

/**
 * Created by Administrator on 2015/10/16.
 */
public class ActorDefine {




    public static final String APP_GAME_NAME = "game";

    public static final String ROOT_GAME_NAME = "root";
    public static final String ROOT_GAME_PATH = "/user/" + ROOT_GAME_NAME;

    public static final String DB_SERVER_NAME = "dbServer";
    public static final String DB_SERVER_PATH = "/user/" + ROOT_GAME_NAME + "/" + ActorDefine.DB_SERVER_NAME;

    public static final String GATE_SERVER_NAME = "gateServer";
    public static final String GATE_SERVER_PATH = "/user/" + ROOT_GAME_NAME + "/" + ActorDefine.GATE_SERVER_NAME;

    public static final String ADMIN_SERVER_NAME = "adminServer";
    public static final String ADMIN_SERVER_PATH = "/user/" + ROOT_GAME_NAME + "/" + ActorDefine.ADMIN_SERVER_NAME;

    public static final String TRIGGER_SERVER_NAME = "TriggerServer";
    public static final String Trigger_SERVER_PATH = "/user/" + ROOT_GAME_NAME + "/" + ActorDefine.TRIGGER_SERVER_NAME;

    public static final String PUSH_SERVER_NAME = "pushServer";

    public static final String AREA_SERVER_PRE_NAME = "areaServer";
    public static final String AREA_SERVER_PRE_PATH = "/user/" + ROOT_GAME_NAME + "/" + ActorDefine.AREA_SERVER_PRE_NAME;

    public static final String LOG_SERVER_NAME = "logServer";
    public static final String LOG_SERVER_PATH = "/user/" + ROOT_GAME_NAME + "/" + ActorDefine.LOG_SERVER_NAME;

    public static final String MYSQL_ACTOR_NAME = "MySqlActor";

    //--------------------ADMIN_SERVER_NAME-------cmd-------管理行为操作的Name值，需要跟传过来的Http内容一一对应，不然无法发送成功----------
    public static final String ADMIN_CHARGE_NAME = "change_money";  //充值
    public static final String ADMIN_NEWS_BROADCAST = "news_broadcast";  //消息广播接口
    public static final String ADMIN_SEND_MAIL = "send_mail";  //发送邮件
    public static final String ADMIN_SEND_GIFT = "send_gift";  //发送道具
    public static final String ADMIN_BAN = "ban"; //封禁类接口
    public static final String ADMIN_KICK = "kick_user"; //踢人接口
    public static final String ADMIN_USER_INFO_LIST = "user_info_list";
    public static final String ADMIN_INSTRUCTOR = "instructor";

    //-----------------------------------------

    public static final String PLAYER_SERVICE_NAME = "playerService";
    public static final String TRIGGER_SERVICE_NAME = "TriggerService";
    public static final String CHAT_SERVICE_NAME = "chatService";
    public static final String MAIL_SERVICE_NAME = "mailService";
    public static final String FRIEND_SERVICE_NAME = "friendService";
    public static final String BATTLE_REPORT_SERVICE_NAME = "BattleReportService";
    public static final String ARENA_SERVICE_NAME = "arenaService";
    public static final String ADMIN_LOG_SERVICE_NAME = "adminLogService";
    public static final String CHARGE_SERVICE_NAME = "chargeService";
    public static final String POWERRANKS_SERVICE_NAME = "powerRanksService";
    public static final String WORLD_SERVICE_NAME = "WorldService";
    public static final String PUSH_SERVICE_NAME = "PushService";


    public static final String ARMYGROUP_SERVICE_NAME = "armyGroupService";
    public static final Integer ADMIN_LOG_ACTION_INSERT = 1;

    public static final Integer ADMIN_LOG_ACTION_UPDATE = 2;
    public static final String PLAYER_ACTOR_NAME_KEY = "playerActorName";
    public static final String PLAYER_AREA_ID_KEY = "playerAreaId"; //
    public static final String PLAYER_IS_LOGIN_SUCCESS_KEY = "playerIsLoginSuccess"; //session 玩家是否登录成功

    public static final String PLAYER_LOGOUT_TIME_KEY = "PlayerLogoutTime"; //玩家端口连接时间
    public static final String WORLD_BLOCK_PRE_NAME = "WorldBlockActor";


    public static final String ARMYGROUPNODE = "ArmyNode";

    ////////////////////////db//key////db的全局key/////////////////////////////////////
    /****************DB的角色名集合KEY****对应的分数则为其playerId*********************/
    public static final String DB_ROLE_NAME_KEY = "g:roleName";
    public static final String DB_ACCOUNT_NAME_KEY = "g:accountName";  //对应的账号名集合Key，分数则为PlayerId

    public static final String DB_PROTO_NAME_FORMAT = "proto:[%d]:[%d]"  ; //cmd, id,协议KEY 格式
    public static final String DB_PROTO_INC_ID_KEY = "g:proto:id";  //协议体的自增key

    public static final String DB_PLAYER_TEAM = "team:teamDate";//阵型id,id，保存出战的2进制队伍数据

    public static final String DB_RANK_GET_ARENA = "arena:ranks";
    public static final String DB_LAST_RANK_GET_ARENA = "arena:lastRank";

    public static final String DB_RANK = "r:rank";

    public static final String DB_ARM_GROUP = "armGroup";

    public static final String DB_BILL_ORDER = "bill:orderIds";

    public static final String ORDANCE_PROXY_NAME = "ordnanceProxy";
    public static final String PLAYER_PROXY_NAME = "playerProxy";
    public static final String SOLDIER_PROXY_NAME = "soldierProxy";
    public static final String DUNGEO_PROXY_NAME = "dungeoProxy";
    public static final String BUILDING_PROXY_NAME = "buildingProxy";
    public static final String BATTLE_PROXY_NAME = "battleProxy";
    public static final String ITEM_PROXY_NAME = "itemProxy";
    public static final String SYSTEM_PROXY_NAME = "systemProxy";
    public static final String TIMERDB_PROXY_NAME = "timerdbProxy";
    public static final String RESFUNBUILD_PROXY_NAME = "resFunBuildProxy";
    public static final String REWARD_PROXY_NAME = "rewardProxy";
    public static final String TECHNOLOGY_PROXY_NAME = "technologyProxy";
    public static final String FORMATION_PROXY_NAME = "formationProxy";
    public static final String SKILL_PROXY_NAME = "skillProxy";
    public static final String EQUIP_PROXY_NAME = "equipProxy";
    public static final String ORDANCEPIECE_PROXY_NAME = "ordnancePieceProxy";
    public static final String ITEMBUFF_PROXY_NAME = "itemBuffProxy";
    public static final String PERFORMTASKS_PROXY_NAME="performtasksProxy";
    public static final String LOTTER_PROXY_NAME = "lotterProxy";
    public static final String MAIL_PROXY_NAME = "mailProxy";
    public static final String TASK_PROXY_NAME = "taskProxy";
    public static final String FRIEND_PROXY_NAME = "friendProxy";
    public static final String VIP_PROXY_NAME = "vipProxy";
    public static final String ARENA_PROXY_NAME = "arenaProxy";
    public static final String ARMYGROUP_PROXY_NAME = "armygroupProxy";
    public static final String ACTIVITY_PROXY_NAME = "activityProxy";
    public static final String COLLECT_PROXY_NAME = "collectProxy";
    public static final String ADVISER_PROXY_NAME = "adsverProxy";
    public static final String NEW_BUILD_PROXY_NAME = "newBuildProxy";

    public static final int LOGIN_MODULE_ID = ProtocolModuleDefine.NET_M1;
    public static final String LOGIN_MODULE_NAME = "login";

    public static final int ROLE_MODULE_ID = ProtocolModuleDefine.NET_M2;
    public static final String ROLE_MODULE_NAME = "role";

    public static final int SYSTEM_MODULE_ID = ProtocolModuleDefine.NET_M3;
    public static final String SYSTEM_MODULE_NAME = "system";

    public static final int SOLDIER_MODULE_ID = ProtocolModuleDefine.NET_M4;
    public static final String SOLDIER_MODULE_NAME = "soldier";

    public static final int BATTLE_MODULE_ID = ProtocolModuleDefine.NET_M5;
    public static final String BATTLE_MODULE_NAME = "battle";

    public static final int DUNGEO_MODULE_ID = ProtocolModuleDefine.NET_M6;
    public static final String DUNGEO_MODULE_NAME = "dungeo";

    public static final int MAP_MODULE_ID = ProtocolModuleDefine.NET_M8;
    public static final String MAP_MODULE_NAME = "map";

    public static final int ITEM_MODULE_ID = ProtocolModuleDefine.NET_M9;
    public static final String ITEM_MODULE_NAME = "item";

    public static final int BUILD_MODULE_ID = ProtocolModuleDefine.NET_M10;
    public static final String  BUILD_MODULE_NAME = "build";

    public static final int EQUIP_MODULE_ID = ProtocolModuleDefine.NET_M13;
    public static final String  EQUIP_MODULE_NAME = "equip";

    public static final int TECHNOLOGY_MODULE_ID = ProtocolModuleDefine.NET_M11;
    public static final String TECHNOLOGY_MODULE_NAME = "technology";

    public static final int TROOP_MODULE_ID = ProtocolModuleDefine.NET_M7;
    public static final String TROOP_MODULE_NAME = "troop";

    public static final int SKILL_MODULE_ID = ProtocolModuleDefine.NET_M12;
    public static final String SKILL_MODULE_NAME = "skill";

    public static final int CHAT_MODULE_ID = ProtocolModuleDefine.NET_M14;
    public static final String CHAT_MODULE_NAME = "chat";

    public static final int LOTTER_MODULE_ID = ProtocolModuleDefine.NET_M15;
    public static final String LOTTER_MODULE_NAME = "lotter";

    public static final int MAIL_MODULE_ID = ProtocolModuleDefine.NET_M16;
    public static final String MAIL_MODULE_NAME = "mail";

    public static final int TASK_MODULE_ID = ProtocolModuleDefine.NET_M19;
    public static final String TASK_MODULE_NAME = "task";

    public static final int FRIEND_MODULE_ID = ProtocolModuleDefine.NET_M17;
    public static final String FRIEND_MODULE_NAME = "friend";

    public static final int ARENA_MODULE_ID = ProtocolModuleDefine.NET_M20;
    public static final String ARENA_MODULE_NAME = "arena";

    public static final int RANKS_MODULE_ID = ProtocolModuleDefine.NET_M21;
    public static final String RANKS_MODULE_NAME = "powerRanks";

    public static final int ARMYGROUP_MODULE_ID = ProtocolModuleDefine.NET_M22;
    public static final String ARMYGROUP_MODULE_NAME = "armygroup";

    public static final int ACTIVITY_MODULE_ID = ProtocolModuleDefine.NET_M23;
    public static final String ACTIVITY_MODULE_NAME = "activity";

    public static final int CDKEY_MODULE_ID = ProtocolModuleDefine.NET_M24;
    public static final String CDKEY_MODULE_NAME = "cdkey";

    public static final int SHARE_MODULE_ID = ProtocolModuleDefine.NET_M25;
    public static final String SHARE_MODULE_NAME = "share";

    public static final int ADVISER_MODULE_ID = ProtocolModuleDefine.NET_M26;
    public static final String ADVISER_MODULE_NAME = "adviser";

    public static final int NEW_BUILD_MODULE_ID = ProtocolModuleDefine.NET_M28;
    public static final String NEW_BUILD_MODULE_NAME = "newBuild";

    public static final int CAPACITY_MODULE_ID = ProtocolModuleDefine.NET_M50;
    public static final String CAPACITY_MODULE_NAME = "capacity";

    public static final int LEGION_DUNGEO_MODULE_ID = ProtocolModuleDefine.NET_M27;
    public static final String LEGION_DUNGEO_MODULE_NAME = "legiondungeo";

    public static final int MAX_NUM = 2100000000;
    public static final int MAX_ENERGY = 20; //体力上限
    public static final int MAX_UPCOMMD_RATE = 1000; //统帅升级成功概率1000为底
    public static final int MAX_UPCOMMD_GOLD = 28; //统帅升级花费28金币
    public static final int MAX_FRIEND = 20; //好友上限20人
    public static final int MIN_BOOM = 18; //繁荣度初始值18
    public static final int MIN_BUY_ENERGY = 5;//初始购买体力花费5金币
    public static final int MIN_RESET_SKILL = 58;//重置技能需要58金币
    public static final int DEFINE_BOOM_NORMAL = 0; //繁荣度正常
    public static final int DEFINE_BOOM_RUINS = 1; //繁荣度废墟
    public static final int DEFINE_UPLV_CMBOOK = 0;  //统帅书升级统帅等级
    public static final int DEFINE_UPLV_CMGOLD = 1;  //金币升级统帅等级
    public static final int DEFINE_UPLV_SKILLBOOK = 0; //技能升级使用技能书
    public static final int DEFINE_UPLV_SKILLGOLD = 1; //金币升级技能
    public static final int DEFINE_MONEY_SKILLBOOK = 9; //每本技能书9元宝

    public static final int RUINS_ICON = 51;// 废墟图标

    public static final int DEFINE_GET_BOOM = 25; //攻打玩家基地，世界资源点获得繁荣度初始值
    public static final double MIN_BOOM_TAEL = 0.02; //购买繁荣度的元宝（0.02元宝/1繁荣度）
    public static final int MIN_DUNGEO_ID = 101;//其实副本id

    public static final int LOGIN_DAY_NUM = 30;//登录30天

    public static final String VIP_ISONHOOK = "isonhook";//vip挂机特权
    public static final String VIP_WAITQUEUE = "waitqueue";//vip等待队列数
    public static final String VIP_BOOMLOSS = "boomloss";//vip繁荣度扣除比例
    public static final String VIP_ENERGYBUY= "energybuy";//vip购买体力次数
    public static final String VIP_ARENABUY= "arenabuy";//vip购买竞技场次数
    public static final String VIP_MILITARYRESET= "militaryreset";//vip军工关卡重置次数
    public static final String VIP_FITRESET= "fitreset";//vip装备，配件关卡重置次数
    public static final String VIP_BULIDQUEUE= "bulidqueue";//vip建筑队列
    public static final String VIP_DAYQUESTRESET= "dayquestreset";//vip日常任务
    public static final String VIP_TROOPCOUNT= "troopCount";//vip出战队伍数
    public static final String VIP_REDBUILDTIME= "redBuildtime";//vip建筑加速
    public static final String VIP_REDSCIENCETIME= "redSciencetime";//vip科技加速
    public static final String VIP_SPEEDUPCOLLECTRES= "speedUpCollectRes";//vip世界资源点采集加速
    public static final String VIP_REDTANKPRO= "redTankpro";//vip坦克生成加速
    public static final String VIP_REDTANKREM= "redTankrem";//vip坦克改造加速
    public static final String VIP_SPEEDUPMARCH= "speedupMarch";//vip野外行军速度加速
    public static final String VIP_STRENGBASERATE= "StrengBaseRate";//vip强化配件成功率

    /*******设置********/
    public static final Integer SETTING_AUTO_ADD_DEFEND_TEAM_ON = 1;//自动补充防守阵型开启
    public static final Integer SETTING_AUTO_ADD_DEFEND_TEAM_OFF = 0;//自动补充防守阵型关闭

    /*****侦查类型*****/
    public static final Integer DETECT_TYPE_PRICE = 1;//询问价格
    public static final Integer DETECT_TYPE_DETECT = 2;//侦查

    /*********各种价格********/
    public static final Integer MOVE_PRICE = 88;//迁城价格

    /*********角色名称********/
    public static final Integer ROLE_CHINESENAME_LENGTH_MAX= 6;//角色中文名称最大长度
    public static final Integer ROLE_CHINESENAME_LENGTH_MIN= 2;//角色中文名称最小长度
    public static final Integer ROLE_ENGlISHNAME_LENGTH_MAX= 12;//角色英文名称最大长度
    public static final Integer ROLE_ENGlISHNAME_LENGTH_MIN= 4;//角色英文名称最小长度

    /****开启模块功能****/
    public static final int OPEN_MAP_ID = 1;//野外基地模块开启id
    public static final int OPEN_LOTTERY_EQUIP_ID = 2;//武将招募模块开启id
    public static final int OPEN_TREASURE_MODULE_ID = 3;//淘宝模块开启id
    public static final int OPEN_ACTIVITY_ID = 4;//活动模块开启id
    public static final int OPEN_THIRTY_LOGIN_AWARD_ID = 5;//30天登录奖励模块开启id

    /******封禁状态*****/
    public static final Integer BAN_STATUS_NORMAL = 0;//解禁，正常状态
    public static final Integer BAN_STATUS_BAN = 1;//封禁状态

    /******系统公告*****/
    public static final Integer VIP_NOTICE_TYPE = 1;//VIP公告
    public static final Integer CLEARANCE_NOTICE_TYPE = 2;//通关公告
    public static final Integer WIN_STEAK_NOTICE_TYPE = 3;//演武场连胜公告
    public static final Integer GENERAL_RECRUIT_NOTICE_TYPE = 4;//将领招募公告
    public static final Integer GENERAL_UPGRADE_NOTICE_TYPE = 5;//将领升级公告
    public static final Integer COMMAND_UPGRADE_NOTICE_TYPE = 6;//统率升级公告
    public static final Integer RANK_UPGRADE_NOTICE_TYPE = 7;//军衔升级公告
    public static final Integer SEEKING_TREASURES_NOTICE_TYPE = 8;//探宝获取公告
    public static final Integer USE_ITERM_NOTICE_TYPE = 9;//使用道具公告
    public static final Integer CONTEST_FIELD_FIRST_CHANGE_NOTICE_TYPE = 10;//演武场第一换人公告
    public static final Integer RESOURCE_POINT_UPGRADE_NOTICE_TYPE = 11;//战胜演武场前10名正在采集的资源点公告
    public static final Integer LIMIT_EXPLORE_NOTICE_TYPE = 12;//极限探险公告
    public static final Integer SYSTEM_NOTICE_HEAD_TYPE = 9999;//系统公告头像

    public static final Integer CONDITION_TWO = 0;//公告type为1,2,3,4,6,7,10,11,12时condition2的值为0,type10两个条件读为0
    public static final Integer RANK_FRIST_ONE = 1;//演武场排名第一  或   武将部位为一
    public static final Integer CHAT_TYPE = 4;//chat类型
    public static final Integer CHAT_TYPE_12 = 12;//chat类型12
    public static final Integer CHAT_TYPE_7 = 7;//chat类型7
    public static final Integer GENERAL_QUALITY = 4;//武将为紫

    /******新手礼包*****/
    public static final Integer NEW_PLAYER_REWARD_ID = 100;

    public static final long REMOVE_CHECK_MAX_TIME = 20*60*1000;//移除检查

    /**
     * 角色名称校验时用到的参数**
     */
    public static final int CHINESE_ROLE_NAME = 1;//中文角色名称长度不规范
    public static final int ENGLISH_ROLE_NAME = 2;//英文角色名称长度不规范
    public static final int CHINESE_ENGLISH_ROLE_NAME = 3;//中英混合角色名称长度不规范
    public static final int CHARS_ROLE_NAME = 4;//角色名称包含 " , ; ' / \等字符

    /******世界icon******/
    public static final int WORLD_RESOURCE_ICON_MAX = 50;


    /******繁荣度******/
    public static final int BOOM_MAX_POOR = 600;//繁荣度废墟界限

    /******体力******/
    public static int  ENERGY_LIMIT = 20;//体力上限（不包含元宝购买）

   /******军团副本******/
    public static final int LEGION_DUNGEO_CHANGE_TIME = 5;//军团副本挑战次数
}
