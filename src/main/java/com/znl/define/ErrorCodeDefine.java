package com.znl.define;

/**
 * Created by Administrator on 2015/11/3.
 */
public class ErrorCodeDefine {

    public static final int M10000_1 = -1;//没有角色
    public static final int M10000_2 = -2;//被禁号了

    public static final int M9998_1 = 1; //被顶号了
    public static final int M9998_2 = 2; //被封号了

    public static final int M20001_1 = -1;//获取配置信息失败
    public static final int M20001_2 = -2;//现在已是最高级军衔，不能升级
    public static final int M20001_3 = -3;//银两（宝石）不足，不能升级军衔
    public static final int M20001_4 = -4;//指挥官等级不够，不能升级军衔

    public static final int M20003_1 = -1;//获取配置信息失败
    public static final int M20003_2 = -2;//当前繁荣度不足,自降一级
    public static final int M20003_3 = -3;//当前繁荣等级已是最高级，不能升级
    public static final int M20003_4 = -4;//购买繁荣度，金币不足

    public static final int M20004_1 = -1;//统帅等级不能比指挥官高
    public static final int M20004_2 = -2;//统帅升级成功率太低
    public static final int M20004_3 = -3;//统帅书不足
    public static final int M20004_4 = -4;//金币不足
    public static final int M20004_5 = -5;//统帅等级已满级，不能升级

    public static final int M20005_1 = -1; //今天的声望已经领过了
    public static final int M20005_2 = -2; //获取配置信息失败
    public static final int M20005_3 = -3; //银两不足，领取失败
    public static final int M20005_4 = -4; //金币不足，领取失败

    public static final int M20006_1 = -1;  //今日好友祝福10已经上限

    public static final int M40002_1 = -1;  //获取佣兵失败
    public static final int M40002_2 = -2;  //金币不足，无法修复
    public static final int M40002_3 = -3;  //宝石不足，无法修复

    public static final int M20008_1 = -1;  //角色名称重复
    public static final int M20008_2 = -2;  //中文角色名称长度为2~6个汉字
    public static final int M20008_3 = -3;  //英文角色名称长度为4~12个字母
    public static final int M20008_4 = -4;  //角色名称过长
    public static final int M20008_5 = -5;  //已经设置过名字了
    public static final int M20008_6 = -6;  //角色名称不能出现逗号、冒号、单引号、双引号、斜杠、反斜杠、星号
    public static final int M20010_1 = -1; ////今天的声望已经领过了

    public static final int M20011_1 = -1; //今日的购买体力次数已经用完了
    public static final int M20011_2 = -2; //金币不足

    public static final int M20013_1 = -1; //今日的购买体力次数已经用完了
    public static final int M20013_2 = -2; //金币不足

    public static final int M20012_1 = -1;//vip等级不够
    public static final int M20012_2 = -2;//获取头像失败
    public static final int M20012_3 = -3;//获取挂件失败
    public static final int M20012_4 = -4;//获取配置表失败

    public static final int M20015_1 = -1;//今天已经领取过奖励
    public static final int M20015_2 = -2;//没有奖励可领
    public static final int M20015_3 = -3;//获取事件配置失败
    public static final int M20015_4 = -4;//领取过的了
    public static final int M20015_5 = -5;//角色等级未达到30天登录奖励模块开放要求等级
    public static final int M20015_6 = -6;//装备背包满了

    public static final int M20016_1 = -1;//明天再来吧
    public static final int M20016_2 = -2;//该功能还没开启

    public static final int M20301_1 = -1;//你已领取过新手礼包了

    //M6
    public static final int M60001_1 = -1;//该章节尚未开启

    public static final int M60002_1 = -1;//获取事件配置失败
    public static final int M60002_2 = -2;//该章节尚未开启
    public static final int M60002_3 = -3;//军衔不足，无法挑战
    public static final int M60002_4 = -4;//该关卡未开启，无法挑战
    public static final int M60002_5 = -5;//体力不足，无法挑战
    public static final int M60002_6 = -6;//剩余不足，无法挑战
    public static final int M60002_13 = -13;//极限挑战扫荡中不能挑战
    public static final int M60002_14 = -14;//该关卡尚未开启不能挑战
    public static final int M60002_15 = -15;//你不在该关卡中
    public static final int M60002_16 = -16;//没有挑战次数了
    public static final int M60002_18 = -18;//将军府空间不足，无法挑战
    public static final int M60002_19 = -19;//军械空间不足，无法挑战
    public static final int M60002_20 = -20;//装备背包满了不能
    public static final int M60002_21 = -21;//装备背包满了不能

    public static final int M60003_1 = -1;//该章节尚未开启
    public static final int M60003_2 = -2;//该宝箱尚未开启或者已经领取过了

    public static final int M60004_1 = -1;//提升VIP等级可增加每天的购买次数
    public static final int M60004_2 = -2;//达到最大购买次数,请明天再来
    public static final int M60004_3 = -3;//金币不足

    public static final int M60005_1 = -1;//获取事件配置失败
    public static final int M60005_2 = -2;//该章节尚未开启
    public static final int M60005_3 = -3;//军衔不足，无法挑战
    public static final int M60005_4 = -4;//该关卡未开启，无法挑战
    public static final int M60005_5 = -5;//体力不足，无法挑战
    public static final int M60005_6 = -6;//剩余不足，无法挑战
    public static final int M60005_7 = -7;//出战单位有误，请重新选择
    public static final int M60005_8 = -8;//出战位置重复，请重新选择
    public static final int M60005_9 = -9;//出战位置有错误，请重新选择
    public static final int M60005_10 = -10;//出战单位不能为空
    public static final int M60005_11 = -11;//出战槽位未开启
    public static final int M60005_12 = -12;//要满3星才可以挂机
    public static final int M60005_13 = -13;//极限挑战扫荡中不能挑战
    public static final int M60005_14 = -14;//该关卡尚未开启不能挑战
    public static final int M60005_15 = -15;//你不在该关卡中
    public static final int M60005_16 = -16;//没有挑战次数了
    public static final int M60005_17 = -17;//vip等级不够，无法挂机
    public static final int M60005_18 = -18;//将军府空间不足，无法挂机
    public static final int M60005_19 = -19;//军械空间不足，无法挂机
    public static final int M60005_20 = -20;//有伤兵未治疗，无法挂机
    public static final int M60005_21 = -21;//装备背包已经满了

    public static final int M60100_1 = -1;//等级不足
    public static final int M60101_1 = -1;//重置次数上限
    public static final int M60101_2 = -2;//体力不足5点不能重置
    public static final int M60102_1 = -1;//正在扫荡不能扫荡
    public static final int M60102_2 = -2;//没有可以扫荡的关卡
    public static final int M60103_1 = -1;//没有在扫荡

    public static final int M50000_1 = -1;//出战单位有误，请重新选择
    public static final int M50000_2 = -2;//出战位置重复，请重新选择
    public static final int M50000_3 = -3;//出战位置有错误，请重新选择
    public static final int M50000_4 = -4;//出战单位不能为空
    public static final int M50000_5 = -5;//出战槽位未开启
    public static final int M50000_6 = -6;//出战数量超过上限
    public static final int M50001_1 = -1;//战斗异常，找不到该场战斗的数据

    public static final int M70001_1 = -1;//佣兵不足，无法设置
    public static final int M70001_2 = -2;//数量出现小于0了
    public static final int M70001_3 = -3;//带兵量超过上限
    public static final int M70001_4 = -4;//队伍是空的
    public static final int M70001_5 = -5;//设置队伍的位置出错了

    public static final int M80000_1 = -1;//角色等级未达到野外基地开放要求等级

    public static final int M80001_1 = -1;//出战单位有误，请重新选择
    public static final int M80001_2 = -2;//出战位置重复，请重新选择
    public static final int M80001_3 = -3;//出战位置有错误，请重新选择
    public static final int M80001_4 = -4;//出战单位不能为空
    public static final int M80001_5 = -5;//出战槽位未开启
    public static final int M80001_6 = -6;//出战数量超过上限
    public static final int M80001_7 = -7;//获取敌方地图坐标出错
    public static final int M80001_8 = -8;//获取自己地图坐标出错
    public static final int M80001_9 = -9;//任务队列已满
    public static final int M80001_10 = -10;//不能攻打自己
    public static final int M80001_11 = -11;//对方尚在保护中，无法攻打
    public static final int M80001_12 = -12;//该据点正在被自己占领，不能攻击
    public static final int M80001_13 = -13;//体力不足，无法攻打
    public static final int M80001_14 = -14;//对方与你在同一个军团中，无法攻打
    public static final int M80001_15 = -15;//角色等级未达到可攻打其他玩家要求等级

    public static final int M80002_1 = -1;//玩家处于免战状态无法侦察
    public static final int M80002_2 = -2;//侦查失败请重试
    public static final int M80002_3 = -3;//侦查失败，银两不足
    public static final int M80002_4 = -4;//不能侦查自己

    public static final int M80004_1 = -1;//队伍已经过时或不存在了
    public static final int M80004_2 = -2;//金币不足，无法购买加速
    public static final int M80004_3 = -3;//还没到达无法操作

    public static final int M80005_1 = -1;//迁城失败，金币不足
    public static final int M80005_2 = -2;//迁城失败，只能迁移到空地
    public static final int M80005_3 = -3;//迁城失败，有部队执行任务的时候是不能执行迁移的

    public static final int M80006_1 = -1;//定位失败，道具不足
    public static final int M80006_9 = -9;//定位失败，没有该玩家
    public static final int M80006_3 = -3;//定位失败，没数据异常
    public static final int M80006_4 = 4;//道具类型错误
    public static final int M80006_5 = 5;//该玩家没有矿点可以勘察的

    public static final int M80008_1 = -1;//数据异常
    public static final int M80008_2 = -2;//空地布恩那个收藏

    public static final int M80011_1 = -1;//迁城失败，道具不足
    public static final int M80011_2 = -2;//迁城失败，还有部队在野外没回来

    public static final int M80012_1 = -1;//驻守失败，你还没有加入军团
    public static final int M80012_2 = -2;//驻守失败，不能驻守你自己
    public static final int M80012_3 = -3;//驻守失败，对方不存在
    public static final int M80012_4 = -4;//驻守失败，对方和你不是用一个军团了


    public static final int M80013_1 = -1;//驻守失败，你还没有加入军团
    public static final int M80013_2 = -2;//驻守失败，不能驻守你自己
    public static final int M80013_3 = -3;//驻守失败，对方不存在
    public static final int M80013_4 = -4;//驻守失败，对方和你不是用一个军团了
    public static final int M80013_5 = -5;//体力不足
    public static final int M80013_6 = -6;//该玩家能容纳的数量上限
    public static final int M80013_7 = -7;//任务队列已满

    public static final int M80014_1 = -1;//没有该任务了
    public static final int M80014_2 = -2;//不是驻军部队
    public static final int M80014_3 = -3;//驻军还没到达不能操作

    public static final int M80103_1 = -1;//任务列表清空
    public static final int M80103_2 = -2;//时间校验失败,发送剩余时间

    public static final int M80107_1=-1;//被攻击通知列表清空
    public static final int M80107_2=-2;//时间校验出错，发送剩余时间

    public static final int M90001_1 = -1;//道具不存在
    public static final int M90001_2 = -2;//获取配置信息失败
    public static final int M90001_3 = -3;//该道具不能使用
    public static final int M90001_4 = -4;//道具不足
    public static final int M90001_5 = -5;//道具不足需要消耗的道具不足
    public static final int M90001_6 = -6;//道具不足需要消耗的金币不足
    public static final int M90001_7 = -7;//造句足够无需用金币
    public static final int M90001_8 = -8;//不存在的玩家
    public static final int M90001_9 = -9;//不正确的使用方式

    public static final int M90002_1 = -1;//buffer持续时间还有，无法执行删除
    public static final int M90002_2 = -2;//buffer不存在

    public static final int M90006_1 = -1;//到据不存在
    public static final int M90006_2 = -2;//喇叭长度太长
    public static final int M90006_3 = -3;//不是喇叭
    public static final int M90006_4 = -4;//道具不足

    public static final int M90007_1 = -1;//道具类型错误
    public static final int M90007_2 = -2;//道具不足
    public static final int M90007_3 = -3;//你未加入军团
    public static final int M90007_4 = -4;//道具不足

    public static final int M90004_1 = -1;//道具不存在
    public static final int M90004_2 = -2;//获取配置信息失败
    public static final int M90004_3 = -3;//该道具不能使用
    public static final int M90004_4 = -4;//道具不足
    public static final int M90004_5 = -5;//道具不足需要消耗的道具不足
    public static final int M90004_6 = -6;//你没有加入军团
    public static final int M90004_7 = -7;//造句足够无需用金币
    public static final int M90004_8 = -8;//改道具发不了红包
    public static final int M90004_9 = -9;//不存在的玩家
    public static final int M90004_10 = -10;//名字已经存在了
    public static final int M90004_11 = -11;//长度不对
    public static final int M90004_12 = -12;//你不是团长不可以使用
    public static final int M90004_13 = -13;//不能给自己发红包
    public static final int M90004_14 = -14;//角色名称不能出现逗号、冒号、单引号、双引号、斜杠、反斜杠、星号
    public static final int M90004_15 = -15;//军团名称不能出现逗号、冒号、单引号、双引号、斜杠、反斜杠、星号

    public static final int M100001_1 = -1;//获取配置信息失败
    public static final int M100001_2 = -2;//该建筑已经在升级了
    public static final int M100001_3 = -3;//该建筑已经满级了
    public static final int M100001_4 = -4;//建筑升级所需资源不足
    public static final int M100001_5 = -5;//建筑升级需要的指挥所等级不够
    public static final int M100001_6 = -6;//提升VIP等级,开放购买更多建筑位
    public static final int M100001_7 = -7;//资源够不需要使用金币
    public static final int M100001_8 = -8;//建筑升级所需金币不足
    public static final int M100001_9 = -9;//该空地还未开启不能建造
    public static final int M100001_10 = -10;//改地方不可一建造此类型的建筑
    public static final int M100001_11 = -11;//请先拆除后建造
    public static final int M100001_12 = -12;//请求的数据有误，请求升级的等级要大于自己建筑的等级

    public static final int M100003_1 = -1;//建筑不存在
    public static final int M100003_2 = -2;//建筑没有在升级中
    public static final int M100003_3 = -2;//没有东西在生产


    public static final int M100004_1 = -1;//建筑不存在
    public static final int M100004_2 = -2;//建筑没有在升级中
    public static final int M100004_3 = -3;//金币不足
    public static final int M100004_4 = -4;//道具不足
    public static final int M100004_8 = -8;//没有在生产中

    public static final int M100005_1 = -1;//建筑不存在
    public static final int M100005_2 = -2;//该类型建筑不可以拆除

    public static final int M100006_1 = -1;//佣兵生产数量太大
    public static final int M100006_2 = -2;//资源不足
    public static final int M100006_3 = -3;//道具不足
    public static final int M100006_4 = -4;//生产队列上限
    public static final int M100006_5 = -5;//司令部等级不足
    public static final int M100006_6 = -6;//建筑等级不够
    public static final int M100006_7 = -7;//单个队列生产数量超过上限
    public static final int M100006_8 = -8;//读取配置数据失败
    public static final int M100006_9 = -9;//生产队列上限
    public static final int M100006_10 = -10;//司令部等级不足
    public static final int M100006_11 = -11;//资源不足
    public static final int M100006_12 = -12;//资源不足
    public static final int M100006_13 = -13;//兵种改造数量上限
    public static final int M100006_14 = -14;//读取配置数据失败
    public static final int M100006_15 = -15;//士兵数不足
    public static final int M100006_16 = -16;//资源不足
    public static final int M100006_17 = -17;//道具不足
    public static final int M100006_27 = -27;//兵营等级不足
    public static final int M100006_28 = -28;//指挥官等级不足


    public static final int M100006_18 = -18;//科技馆等级不够
    public static final int M100006_19 = -19;//声望等级不够
    public static final int M100006_20 = -20;//已是最高级
    public static final int M100006_21 = -21;//银两不足
    public static final int M100006_22 = -22;//铁锭不足
    public static final int M100006_23 = -23;//木材不足
    public static final int M100006_24 = -24;//石料不足
    public static final int M100006_25 = -25;//粮食不足
    public static final int M100006_26 = -26;//该科技正在升级

    public static final int M100004_5 = -5;//读取配置数据失败
    public static final int M100004_6 = -6;//已经拥有该道具不需要购买
    public static final int M100004_7 = -7;//购买道具所需金币不足

    public static final int M100007_5 = -5;//读取配置数据失败
    public static final int M100007_6 = -6;//已经拥有该道具不需要购买
    public static final int M100007_7 = -7;//购买道具所需金币不足


    public static final int M100008_1 = -1;//读取配置数据失败
    public static final int M100008_2 = -2;//购买数量超过上限
    public static final int M100008_3 = -3;//金币不足

    public static final int M100009_1 = -1;//可购买次数已用完
    public static final int M100009_2 = -2;//提升VIP等级,开放购买更多建筑位
    public static final int M100009_3 = -3;//金币不足

    public static final int M100011_1 = -1;//金币不足
    public static final int M100012_1 = -1;//没有自动升级

    public static final int M110001_1 = -1;//佣兵生产数量太大
    public static final int M110001_2 = -2;//资源不足
    public static final int M110001_3 = -3;//道具不足
    public static final int M110001_4 = -4;//生产队列上限
    public static final int M110001_5 = -5;//司令部等级不足
    public static final int M110001_6 = -6;//建筑等级不够
    public static final int M110001_7 = -7;//单个队列生产数量超过上限
    public static final int M110001_8 = -8;//读取配置数据失败
    public static final int M110001_9 = -9;//生产队列上限
    public static final int M110001_10 = -10;//生产队列上限


    public static final int M120001_1 = -1; //获取配置信息失败
    public static final int M120001_2 = -2; //指挥官等级不足
    public static final int M120001_3 = -3; //技能书不足
    public static final int M120001_4 = -4; //现在技能等级已是最高级
    public static final int M120001_5 = -5; //升级技能，金币不足
    public static final int M120002_1 = -1; //技能等级全部为0，无需重置
    public static final int M120002_2 = -2; //重置技能，金币不足


    public static final int M130001_1 = -1;//装备不存在
    public static final int M130001_2 = -2;//该类型装备不能升级
    public static final int M130001_3 = -3;//已经满级不能升级
    public static final int M130001_4 = -4;//有不存在的装备
    public static final int M130001_5 = -5;//升到满级不需要那么多
    public static final int M130001_6 = -6;//武将的等级不能超过主公等级


    public static final int M130002_1 = -1;//装备不存在
    public static final int M130002_2 = -2;//该装备类型不能穿戴
    public static final int M130002_3 = -3;//该装备不能穿戴在这里
    public static final int M130002_4 = -3;//该槽位未开启

    public static final int M130003_1 = -1;//装备不存在
    public static final int M130003_2 = -2;//仓库容量满了

    public static final int M130005_1 = -1;//装备不存在
    public static final int M130005_2 = -2;//穿在身上不能卖

    public static final int M130006_1 = -1;//不能跟背包调换位置
    public static final int M130006_2 = -2;//该槽位未开启

    public static final int M130007_1 = -1;//装备背包已经上限了
    public static final int M130007_2 = -2;//身上金币不足

    public static final int M130102_1 = -1;//军械碎片不存在
    public static final int M130102_2 = -2;//读取配置数据失败
    public static final int M130102_3 = -3;//军械碎片数量不足不能合成
    public static final int M130102_4 = -4;//该军械碎片不能合成军械

    public static final int M130103_1 = -1;//读取配置数据失败
    public static final int M130103_2 = -2;//没有可分解的军械

    public static final int M130104_1 = -1;//军械不存在
    public static final int M130104_2 = -2;//军械已经穿在身上了
    public static final int M130104_3 = -3;//该位置已经有军械穿上了
    public static final int M130104_4 = -4;//主公等级不足，不能穿戴


    public static final int M130105_1 = -1;//该军械不存在
    public static final int M130105_2 = -2;//该军械在背包上
    public static final int M130105_3 = -3;//仓库满了

    public static final int M130106_1 = -1;//该军械不存在
    public static final int M130106_2 = -2;//已经穿在身上的军械不能分解

    public static final int M130107_1 = -1;//该军械不存在
    public static final int M130107_2 = -2;//读取配置数据失败
    public static final int M130107_3 = -3;//该军械已经满级了
    public static final int M130107_4 = -4;//资源不足
    public static final int M130107_5 = -5;//你拥有的增加强化概率的强化道具不足
    public static final int M130107_6 = -6;//升级失败，幸运不够
    public static final int M130107_7 = -7;//不需要太多增加军械强化成功概率的道具


    public static final int M130108_1 = -1;//该军械不存在
    public static final int M130108_2 = -2;//改造已经满级了
    public static final int M130108_3 = -3;//道具不足
    public static final int M130108_4 = -4;//你拥有的增加强化概率的强化道具不足
    public static final int M130108_5 = -5;//强化等级不足

    public static final int M130109_1 = -1;//该军械不存在
    public static final int M130109_2 = -2;//该军械属于不能进化的类型
    public static final int M130109_3 = -3;//碎片不足
    public static final int M130109_4 = -4;//改造等级不足

    //M14
    public static final int M140001_1 = -1;//找不到该玩家数据
    public static final int M140002_1 = -1;//找不到该玩家数据
    public static final int M140002_2 = -2;//发送聊天失败，请重试
    public static final int M140002_3 = -3;//无法私聊不在线的玩家
    public static final int M140002_4 = -4;//禁言中无法聊天
    public static final int M140004_1 = -1;//该玩家不存在
    public static final int M140004_2 = -2;//该玩家不在线
    public static final int M140005_1 = -1;//该玩家已经在你的屏蔽列表中了

    //M15
    public static final int M150000_1 = -1;//角色等级未达到武将招募开放要求等级
    public static final int M150001_1 = -1;//读取配置数据失败
    public static final int M150001_2 = -2;//金币不足
    public static final int M150001_3 = -3;//装备仓库已满，不能继续抽了
    public static final int M150002_1 = -1;//读取配置数据失败
    public static final int M150002_2 = -2;//道具不足
    public static final int M150003_1 = -1;//读取配置数据失败
    public static final int M150003_2 = -2;//道具已足无需购买
    public static final int M150003_3 = -3;//金币不足
    public static final int M150003_4 = -4;//角色等级未达到探宝开放要求等级


    //M16
    public static final int M160001_1 = -1;//该邮件不存在
    public static final int M160003_1 = -1;//操作错误，请稍后重试
    public static final int M160003_2 = -2;//不存在的玩家，发送失败
    public static final int M160005_1 = -1;//邮件不存在了
    public static final int M160005_2 = -2;//该邮件不是战报邮件
    public static final int M160005_3 = -3;//该战报没有战斗可回放
    public static final int M160006_1 = -1;//该邮件没有附件
    public static final int M160006_2 = -2;//该邮件的附件已经被提取过了
    public static final int M160006_3 = -3;//装备背包已经满了
    public static final int M160006_4 = -4;//军师府已满

    public static final int M170001_1 = -1; //该玩家不存在
    public static final int M170001_2 = -2; //加自己为好友

    public static final int M170001_3 = -3; //该玩家已是你的好友
    public static final int M170001_4 = -4; //超过好友上限
    public static final int M170002_1 = -1; //搜索的玩家不存在
    public static final int M170003_1 = -1; //玩家不是好友
    public static final int M170004_1 = -1; //该好友已经祝福过了
    public static final int M170006_1 = -1; //重复领取奖励
    public static final int M170006_2 = -2; //今天没有可领取的次数了


    public static final int M190001_1 = -1;//任务不存在
    public static final int M190001_2 = -2;//任务还不能领取


    public static final int M190002_1 = -1;//任务不存在
    public static final int M190002_2 = -2;//不能接受任务了请重置
    public static final int M190002_3 = -3;//有任务在接受中，不能重置
    public static final int M190002_4 = -4;//金币不足不能重置
    public static final int M190002_5 = -5;//还可以完成任务，不需要重置
    public static final int M190002_6 = -6;//任务重置数次上限
    public static final int M190002_7 = -7;//有任务在接受中不能进行刷新
    public static final int M190002_8 = -8;//不能进行刷新，请重置
    public static final int M190002_9 = -9;//刷新任务所需的金币不足
    public static final int M190002_10 = -10;//任务不存在
    public static final int M190002_11 = -11;//金币不足不能快速完成
    public static final int M190002_12 = -12;//该任务没有被接受不能快速完成
    public static final int M190003_13 = -13;//没有可领的了
    public static final int M190003_14 = -14;//读取配置数据错误
    public static final int M190003_15 = -15;//活跃读不足
    public static final int M190002_16 = -16;//该状态不能放弃

    //竞技场
    public static final int M200000_1 = -1;//先设置防守队伍

    public static final int M200001_1 = -1;//对手不存在
    public static final int M200001_2 = -2;//玩家处于保护时间
    public static final int M200001_3 = -3;//先设置防守队伍
    public static final int M200001_4 = -4;//没有挑战次数了
    public static final int M200001_5 = -5;//还在冷去中
    public static final int M200001_6 = -6;//数据异常
    public static final int M200001_7 = -7;//数据异常


    public static final int M200004_1 = -1;//数据异常
    public static final int M200004_2 = -2;//资源不足

    public static final int M200005_1 = -1;//今天已经领取过了
    public static final int M200005_2 = -2;//还未上榜

    public static final int M200101_1 = -1;//邮件不存在


    public static final int M200006_1 = -1;//可以挑战无需加速
    public static final int M200006_2 = -2;//加速所需金币不足

    //充值http錯誤碼

    public static final int M200003_1 = -1;//购买次数上限
    public static final int M200003_2 = -2;//购买竞技场挑战所需金币不足
    public static final int M200003_3 = -3;//还有挑战次数无需购买


    //M22
    public static final int M220002_1 = -1;//军团贡献值太低
    public static final int M220002_2 = -2;//军团等级太低
    public static final int M220002_3 = -3;//兑换上限
    public static final int M220002_4 = -4;//军团兑换上限
    public static final int M220002_5 = -5;//没有该珍品
    public static final int M220002_6 = -6;//军团等级不足

    public static final int M220007_1 = -1;//没有权限
    public static final int M220007_2 = -2;//建设度不足
    public static final int M220007_3 = -3;//军团等级上限
    public static final int M220007_4 = -4;//你还没有加入任何军团

    public static final int M220008_1 = -1;//您的VIP等级未达到1级，不能捐献
    public static final int M220008_2 = -2;//您的捐献次数上限了
    public static final int M220008_3 = -3;//金币不足
    public static final int M220008_4 = -4;//资源不足
    public static final int M220008_5 = -5;//读取配置数据错误

    public static final int M220009_1 = -1;//您的VIP等级未达到1级，不能捐献
    public static final int M220009_2 = -2;//您的捐献次数上限了
    public static final int M220009_3 = -3;//金币不足
    public static final int M220009_4 = -4;//资源不足
    public static final int M220009_5 = -5;//读取配置数据错误
    public static final int M220009_6 = -6;//请升级军团科技大厅再来捐献
    public static final int M220009_7 = -7;//该科技未开启

    public static final int M220010_1 = -1;//没有权限
    public static final int M220010_2 = -2;//建设度不足
    public static final int M220010_3 = -3;//军团科技大厅等级上限
    public static final int M220010_4 = -4;//军团科技大厅等级不能超过军团大厅等级


    public static final int M220013_1 = -1;//没有权限
    public static final int M220013_2 = -2;//建设度不足
    public static final int M220013_3 = -3;//军团福利院等级不能超过军团大厅等级
    public static final int M220013_4 = -4;//军团福利院等级上限
    public static final int M220013_5 = -5;//读取配置数据错误
    public static final int M220013_6 = -6;//贡献度不足
    public static final int M220013_7 = -7;//今天的福利已经领过了


    public static final int M220015_1 = -1;//加入军团还没有超过24小时

    public static final int M220102_1 = -1;//自己的申请列表已满
    public static final int M220102_2 = -2;//你战力不符合军团的要求
    public static final int M220102_3 = -3;//你的等级不符合军团的要求
    public static final int M220102_4 = -4;//军团的人数已经上限
    public static final int M220108_8 = -8;//你已经加入了军团了

    public static final int M220103_1 = -1;//金币不足
    public static final int M220103_2 = -2;//银两不足
    public static final int M220103_3 = -3;//名字已经存在了
    public static final int M220103_4 = -4;//字符不合法
    public static final int M220103_5 = -5;//已经拥有军团了
    public static final int M220103_6 = -6;//自身等级不够
    public static final int M220103_8 = -8;//铁定不足
    public static final int M220103_9 = -9;//木材不足
    public static final int M220103_10 = -10;//石料不足
    public static final int M220103_11 = -11;//粮食不足
    public static final int M220103_12 = -12;//军团名称不能出现逗号、冒号、单引号、双引号、斜杠、反斜杠、星号


    public static final int M220200_1 = -1;//你还没有加入军团


    public static final int M220201_1 = -1;//没有加入军团
    public static final int M220201_2 = -2;//权限不够
    public static final int M220201_3 = -3;//不存在的成员
    public static final int M220201_4 = -4;//你不是团长不能做转让团长
    public static final int M220201_5 = -5;//没有人给你转让
    public static final int M220201_6 = -6;//自己退了就没人，我不允许
    public static final int M220201_7 = -7;//团长不能退啊

    public static final int M220203_1 = -1;//你的权限不够
    public static final int M220203_2 = -2;//军团满人了无法加入
    public static final int M220203_3 = -3;//重复申请
    public static final int M220203_4 = -4;//已经不在申请列表了
    public static final int M220203_5 = -5;//已经有军团的人了

    public static final int M220204_1 = -1;//你的权限不够

    public static final int M220210_1 = -1;//你不是团长
    public static final int M220210_2 = -2;//加入类型设置有误
    public static final int M220210_3 = -3;//宣言太长

    public static final int M220211_1 = -1;//你不是团长
    public static final int M220211_2 = -2;//公告太长
    public static final int M220211_3 = -3;//军团公告不能出现逗号、冒号、单引号、双引号、斜杠、反斜杠、星号

    public static final int M220220_1 = -1;//不是团长
    public static final int M220220_2 = -2;//军团等级不够
    public static final int M220220_3 = -3;//职位长度不对

    public static final int M220221_1 = -1;//不是团长不能任职
    public static final int M220221_2 = -2;//军团等级不够
    public static final int M220221_3 = -3;//该职位的人数已满
    public static final int M220221_4 = -4;//团长不能给自己任职
    public static final int M220221_5 = -5;//团长不能升职
    public static final int M220221_6 = -6;//没有人离线7天
    public static final int M220221_7 = -7;//你的贡献值不是最大的

    public static final int M220300_1 = -1;//你妹又加入军团不能查看军情

    public static final int M220400_1 = -1;//你不是团长没有招募的权限
    public static final int M220400_2 = -2;//招募时间间隔太短
    public static final int M220400_3 = -3;//你没有加入军团

    public static final int M230001_1 = -1;//活动领取失败，该活动尚未开启或者已经过期
    public static final int M230001_2 = -2;//活动领取失败，尚未到达领取条件
    public static final int M230001_3 = -3;//活动购买失败，购买次数不足
    public static final int M230001_4 = -4;//活动购买失败，所需资源不足，无法购买
    public static final int M230001_5 = -5;//活动领取失败，尚未达到领取条件，不能领取
    public static final int M230001_6 = -6;//已经购买过了投资计划了就不能重复购买了
    public static final int M230001_7 = -7;//金币不足，无法购买投资计划
    public static final int M230001_8 = -8;//vip等级不足，无法购买投资计划
    public static final int M230000_9 = -9;//角色等级未达到活动开放要求等级

    public static final int M230003_10 = -10;//金币不足，无法执行拉霸抽奖
    public static final int M230003_11 = -11;//活动不存在或者限时活动已过期


    public static final int M240000_1 = -1;//cdkey领取失败，领取次数上限
    public static final int M240000_2 = -2;//cdkey领取失败，读取配置数据失败
    public static final int M240000_3 = -3;//cdkey领取失败，cdkey解析失败
    public static final int M240000_4 = -4;//cdkey领取失败，cdkey不存在或者已经被领取了
    public static final int M240000_5 = -5;//cdkey领取失败，领取过于频繁，请稍后重试

    public static final int M250000_1 = -1;//分享失败，无法找到该佣兵
    public static final int M250000_2 = -2;//分享失败，无法找到该竞技场战斗记录
    public static final int M250000_3 = -3;//分享失败，无法找到该战报
    public static final int M250000_4 = -4;//分享失败，还未加入军团，无法分享到军团
    public static final int M250000_5 = -5;//30秒内不可再次分享

    public static final int M2300006_1 = -1;//有福同享活动未开启或者已经过期
    public static final int M2300006_2 = -2;//领取有福同享宝箱失败，宝箱id不存在或已经过期


    public static final int M260001_1 = -1;//数量不够
    public static final int M260001_2 = -2;//所需军师不足
    public static final int M260001_3 = -3;//金币不足
    public static final int M260001_4 = -4;//不同品质不能进阶
    public static final int M260001_5 = -5;//该品质不能进阶
    public static final int M260001_6 = -6;//配置数据错误


    public static final int M260002_1 = -1;//没有可升级的军师了
    public static final int M260002_2 = -2;//读取配置失败
    public static final int M260002_3 = -3;//元宝不足
    public static final int M260002_4 = -4;//道具不足

    public static final int M260003_1 = -1;//该军师没有可以分解的000


    public static final int M260005_1 = -1;//抽奖所需资源不足
    public static final int M260005_2 = -2;//读取配置失败
    public static final int M260005_3 = -3;//军师府上限

    public static final int M270002_1 = -1;//请先通关上一一关
    public static final int M270002_2 = -2;//该副本正在被挑战中
    public static final int M270002_3 = -3;//挑战次数不足

    public static final int M270003_1 = -3;//宝箱已领取

    public static final int M280001_1 = -1;//获取配置信息失败
    public static final int M280001_2 = -2;//该建筑已经在升级了
    public static final int M280001_3 = -3;//该建筑已经满级了
    public static final int M280001_4 = -4;//建筑升级所需资源不足
    public static final int M280001_5 = -5;//建筑升级需要的指挥所等级不够
    public static final int M280001_6 = -6;//提升VIP等级,开放购买更多建筑位
    public static final int M280001_7 = -7;//资源够不需要使用金币
    public static final int M280001_8 = -8;//建筑升级所需金币不足
    public static final int M280001_9 = -9;//该空地还未开启不能建造
    public static final int M280001_10 = -10;//改地方不可一建造此类型的建筑
    public static final int M280001_11 = -11;//请先拆除后建造
    public static final int M280001_12 = -12;//请求的数据有误，请求升级的等级要大于自己建筑的等级

    public static final int M260006_1 = -1;//数量不够
    public static final int M260006_2 = -2;//所需军师不足
    public static final int M260006_3 = -3;//道具不足
    public static final int M260006_4 = -4;//该品质不能进阶
    public static final int M260006_5 = -5;//配置数据错误


    public static final int M280002_1 = -1;//获取配置信息失败
    public static final int M280002_2 = -2;//该建筑并没有在升级中
    public static final int M280002_3 = -3;//升级时间未到


    public static final int M280003_1 = -1;//建筑不存在
    public static final int M280003_2 = -2;//建筑没有在升级中

    public static final int M280004_1 = -1;//建筑不存在
    public static final int M280004_2 = -2;//建筑没有在升级中
    public static final int M280004_3 = -3;//金币不足
    public static final int M280004_4 = -4;//道具不足
    public static final int M280004_5 = -5;//读取配置数据失败
    public static final int M280004_6 = -6;//已经拥有该道具不需要购买
    public static final int M280004_7 = -7;//购买道具所需金币不足
    public static final int M280004_8 = -8;//没有在生产中

    public static final int M280005_1 = -1;//建筑不存在
    public static final int M280005_2 = -2;//该类型建筑不可以拆除

    public static final int M280006_1 = -1;//佣兵生产数量太大
    public static final int M280006_2 = -2;//资源不足
    public static final int M280006_3 = -3;//道具不足
    public static final int M280006_4 = -4;//生产队列上限
    public static final int M280006_5 = -5;//司令部等级不足
    public static final int M280006_6 = -6;//建筑等级不够
    public static final int M280006_7 = -7;//单个队列生产数量超过上限
    public static final int M280006_8 = -8;//读取配置数据失败
    public static final int M280006_9 = -9;//生产队列上限
    public static final int M280006_10 = -10;//司令部等级不足
    public static final int M280006_11 = -11;//资源不足
    public static final int M280006_12 = -12;//资源不足
    public static final int M280006_13 = -13;//兵种改造数量上限
    public static final int M280006_14 = -14;//读取配置数据失败
    public static final int M280006_15 = -15;//士兵数不足
    public static final int M280006_16 = -16;//资源不足
    public static final int M280006_17 = -17;//道具不足
    public static final int M280006_18 = -18;//科技馆等级不够
    public static final int M280006_19 = -19;//声望等级不够
    public static final int M280006_20 = -20;//已是最高级
    public static final int M280006_21 = -21;//银两不足
    public static final int M280006_22 = -22;//铁锭不足
    public static final int M280006_23 = -23;//木材不足
    public static final int M280006_24 = -24;//石料不足
    public static final int M280006_25 = -25;//粮食不足
    public static final int M280006_26 = -26;//该科技正在升级
    public static final int M280006_27 = -27;//兵营等级不足
    public static final int M280006_28 = -28;//指挥官等级不足
    public static final int M280006_29 = -29;//获取建筑失败

    public static final int M280007_1 = -1;//不存在的生产队列
    public static final int M280007_2 = -2;//生产时间未到
    public static final int M280007_3 = -3;//未知的生产类型

    public static final int M280008_1 = -1;//建筑不存在
    public static final int M280008_2 = -2;//建筑没有在升级中
    public static final int M280008_3 = -2;//没有东西在生产

    public static final int M280009_1 = -1;//建筑不存在
    public static final int M280009_2 = -2;//建筑没有在升级中
    public static final int M280009_3 = -3;//金币不足
    public static final int M280009_4 = -4;//道具不足
    public static final int M280009_5 = -5;//读取配置数据失败
    public static final int M280009_6 = -6;//已经拥有该道具不需要购买
    public static final int M280009_7 = -7;//购买道具所需金币不足
    public static final int M280009_8 = -8;//没有在生产中

    public static final int M280011_1 = -1;//已经到了最大建筑位上限了
    public static final int M280011_2 = -2;//vip等级不够，不能购买建筑位
    public static final int M280011_3 = -3;//元宝不足，无法购买建筑位

    public static final int M280012_1 = -1;//金币不足
    public static final int M280013_1 = -1;//没有自动升级
    public static final int M280014_1 = -1;//还有时间呢，还没结束呢
}
