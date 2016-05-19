package com.znl.define;

/**
 * Created by Administrator on 2015/10/22.
 */
public class  ProtocolModuleDefine {
    /**
     * *登录相关模块***
     */
    public static final int NET_M1 = 1;
    /**
     * *心跳***
     */
    public static final int NET_M1_C8888 = 8888;
    /**
     * *异地登陆***
     */
    public static final int NET_M1_C9998 = 9998;
    /**
     * *登录网关***
     */
    public static final int NET_M1_C9999 = 9999;
    /**
     * *登陆协议***
     */
    public static final int NET_M1_C10000 = 10000;
    /**
     * 创建角色*
     */
    public static final int NET_M1_C10001 = 10001;

    /**
     * 事件日志请求
     */
    public static final int NET_M1_C10002 = 10002;

    /////////////////////
    /**
     * 角色相关模块**
     */
    public static final int NET_M2 = 2;
    /**
     * 角色信息**
     */
    public static final int NET_M2_C20000 = 20000;
    /**
     * 军衔升级**
     */
    public static final int NET_M2_C20001 = 20001;
    /**
     * power修改发送**
     */
    public static final int NET_M2_C20002 = 20002;
    /**
     * 元宝购买繁荣度/恢复**
     */
    public static final int NET_M2_C20003 = 20003;
    /**
     * 统帅等级升级**
     */
    public static final int NET_M2_C20004 = 20004;
    /**
     * 授勋领取声望**
     */
    public static final int NET_M2_C20005 = 20005;
    /**
     * 事件日志请求
     *
     */
    public static final int NET_M2_C20006 = 20006;
    /**
     * 发送各种背包刷新**
     */
    public static final int NET_M2_C20007 = 20007;
    /**
     * 角色创建*
     */
    public static final int NET_M2_C20008 = 20008;
    /**
     * 打开领取声望**
     */
    public static final int NET_M2_C20010 = 20010;
    /**
     * 元宝购买体力**
     */
    public static final int NET_M2_C20011 = 20011;
    /**
     * 设置头像，挂件**
     */
    public static final int NET_M2_C20012 = 20012;
    /**
     * 请求是否可以购买体力**
     */
    public static final int NET_M2_C20013 = 20013;
    /**
     * 设置玩家坐标**
     */
    public static final int NET_M2_C20014 = 20014;
    /**
     * 30天登录奖励**
     */
    public static final int NET_M2_C20015 = 20015;
    /**
     * 每日登陆抽奖**
     */
    public static final int NET_M2_C20016 = 20016;

    /**
     * 每日领取声望**
     */
    public static final int NET_M2_C20017 = 20017;
    /**
     * 系统提示**
     */
    public static final int NET_M2_C20200 = 20200;

    /**
     * 通知完成**
     */
    public static final int NET_M2_C20300 = 20300;
    /**
     * 推送军团名字改变**
     */
    public static final int NET_M2_C20201 = 20201;
    /**
     * 推送军团名字改变**
     */
    public static final int NET_M2_C20301 = 20301;

    /**
     * 获取最近信息**
     */
    public static final int NET_M2_C20400 = 20400;

    /**
     * 繁荣度时间校验**
     */
    public static final int NET_M2_C20500 = 20500;
    /**
     * 体力时间校验**
     */
    public static final int NET_M2_C20501 = 20501;

    /**
     * 系统相关模块**
     */
    public static final int NET_M3 = 3;
    /**
     * 系统定时器模块**
     */
    public static final int NET_M3_C30000 = 30000;
    /**
     * 触发事件event**
     */
    public static final int NET_M3_C30001 = 30001;
    /**
     * 获取缓存信息**
     */
    public static final int NET_M3_C30100 = 30100;
    /**
     * 更新缓存信息event**
     */
    public static final int NET_M3_C30101 = 30101;
    /**
     * 充值成功**
     */
    public static final int NET_M3_C30102 = 30102;
    /**
     * 每天4点好友祝福刷新
     */
    public static final int NET_M3_C30103 =30103;
    /**
     * 佣兵相关模块**
     */
    public static final int NET_M4 = 4;
    /**
     * 刷新佣兵**
     */
    public static final int NET_M4_C40000 = 40000;
    /**
     * 战损佣兵列表**
     */
    public static final int NET_M4_C40001 = 40001;
    /**
     * 修复佣兵**
     */
    public static final int NET_M4_C40002 = 40002;

    /**
     * 战斗相关模块**
     */
    public static final int NET_M5 = 5;
    /**
     * 战斗开始**
     */
    public static final int NET_M5_C50000 = 50000;
    /**
     * 战斗结束**
     */
    public static final int NET_M5_C50001 = 50001;
    /**
     * 战斗出战队列生成（在50000协议之前发）**
     */
    public static final int NET_M5_C50002 = 50002;

    /**
     * 副本相关模块**
     */
    public static final int NET_M6 = 6;
    /**
     * 获取副本列表信息**
     */
    public static final int NET_M6_C60000 = 60000;
    /**
     * 获取副本详细信息**
     */
    public static final int NET_M6_C60001 = 60001;
    /**
     * 挑战关卡询问**
     */
    public static final int NET_M6_C60002 = 60002;
    /**
     * 开启宝箱**
     */
    public static final int NET_M6_C60003 = 60003;
    /**
     * VIP购买冒险次数**
     */
    public static final int NET_M6_C60004 = 60004;
    /**
     * 挂机**
     */
    public static final int NET_M6_C60005 = 60005;
    /**
     * 更新副本列表**
     */
    public static final int NET_M6_C60006 = 60006;

    /**
     * 极限挑战信息
     */
    public static final int NET_M6_C60100 = 60100;
    /**
     * 重置极限挑战
     */
    public static final int NET_M6_C60101 = 60101;

    /**
     * 开始扫荡
     */
    public static final int NET_M6_C60102 = 60102;

    /**
     * 停止扫荡
     */
    public static final int NET_M6_C60103 = 60103;

    /**
     * 开启新副本
     */
    public static final int NET_M6_C60104 = 60104;


    /**
     * 极限副本扫荡倒计时
     */
    public static final int NET_M6_C60105 = 60105;


    /**
     * 冒险副本次数请求4点刷新
     */
    public static final int NET_M6_C60106 = 60106;

    /**
     * 军队模块**
     */
    public static final int NET_M7 = 7;
    /**
     * 获取阵型信息**
     */
    public static final int NET_M7_C70000 = 70000;
    /**
     * 设置阵型**
     */
    public static final int NET_M7_C70001 = 70001;

    /**
     * 世界地图相关模块**
     */
    public static final int NET_M8 = 8;
    /**
     * 查看坐标周围的格子信息**
     */
    public static final int NET_M8_C80000 = 80000;
    /**
     * 攻打世界地图**
     */
    public static final int NET_M8_C80001 = 80001;
    /**
     * 侦查世界地图**
     */
    public static final int NET_M8_C80002 = 80002;
    /**
     * 任务部队列表**
     */
    public static final int NET_M8_C80003 = 80003;
    /**
     * 任务部队购买加速**
     */
    public static final int NET_M8_C80004 = 80004;
    /**
     * 定点迁移**
     */
    public static final int NET_M8_C80005 = 80005;
    /**
     * 查找玩家坐标**
     */
    public static final int NET_M8_C80006 = 80006;
    /**
     * 部队图标提示**
     */
    public static final int NET_M8_C80007 = 80007;

    /**
     * 添加收藏**
     */
    public static final int NET_M8_C80008 = 80008;
    /**
     *删除收藏**
     */
    public static final int NET_M8_C80009 = 80009;
    /**
     * 查看收藏信息*
     */
    public static final int NET_M8_C80010 = 80010;
    /**
     * 随机迁城令*
     *
     */
    public static final int NET_M8_C80011 = 80011;
    /**
     * 获得前往驻守时间*
     */
    public static final int NET_M8_C80012 = 80012;
    /**
     * 前往驻守*
     */
    public static final int NET_M8_C80013 = 80013;
    /**
     * 改变防守队伍*
     */
    public static final int NET_M8_C80014 = 80014;
    /**
     * 放大镜
     */
    public static final int NET_M8_C80015 = 80015;

    /**
     *刷新保护罩
     */
    public static final int NET_M8_C80016= 80016;
    /**
     * 驻军信息校验删除
     */
    public static final int NET_M8_C80103 = 80103;
    /**
     * 刷新新增驻军任务
     */
    public static final int NET_M8_C80104 = 80104;
    /**
     * 校验被攻击的通知并删除
     */
    public static final int NET_M8_C80107 = 80107;
    /**
     * 刷新与新增被攻击通知
     */
    public static final int NET_M8_C80108 = 80108;


    /**
     * 道具相关模块**
     */
    public static final int NET_M9 = 9;
    public static final int NET_M9_C90000 = 90000;
    /**
     * 道具使用**
     */
    public static final int NET_M9_C90001 = 90001;
    /**
     * 道具信息
     */
    public static final int NET_M9_C90002 = 90002;
    /**
     * 道具信息变化**
     */
    public static final int NET_M9_C90003 = 90003;   /*** 道具Buff效果加成变化***/
    public static final int NET_M9_C90004 = 90004;   /***发红包  改名***/
    public static final int NET_M9_C90005 = 90005;   /***外观道具使用***/
    public static final int NET_M9_C90006 = 90006;   /***外观道具使用***/
    public static final int NET_M9_C90007 = 90007;   /***军团贡献道具***/

    /**
     * 建筑相关模块**
     */
    public static final int NET_M10 = 10;
    public static final int NET_M10_C100000 = 100000;
    /**
     * 建筑初始化信息**
     */
    public static final int NET_M10_C100001 = 100001;
    /**
     * 建筑升级**
     */
    public static final int NET_M10_C100003 = 100003;
    /**
     * 取消建筑升级**
     */
    public static final int NET_M10_C100004 = 100004;
    /**
     * 加速生产加速升级**
     */
    public static final int NET_M10_C100005 = 100005;
    /**
     * 拆除建筑**
     */
    public static final int NET_M10_C100006 = 100006;
    /**
     * 生产**
     */
    public static final int NET_M10_C100007 = 100007;
    /**
     * 道具购买使用**
     */
    public static final int NET_M10_C100008 = 100008;
    /**
     * 商店购买道具**
     */
    public static final int NET_M10_C100009 = 100009;
    /**
     * 请求购买建筑位 **
     */
    public static final int NET_M10_C100010 = 100010;   /*** vip购买建筑位 ***/


    public static final int NET_M10_C100011 = 100011;   /***自动升级购买***/
    public static final int NET_M10_C100012 = 100012;   /***自动升级开启与关闭***/

    /**
     * 科技馆相关模块 **
     */
    public static final int NET_M11 = 11;
    public static final int NET_M11_C110000 = 110000;
    /**
     * 科技初始化 **
     */
    public static final int NET_M11_C110001 = 110001;  /*** 科技升级 ***/

    /**
     * 技能相关模块 **
     */
    public static final int NET_M12 = 12;
    public static final int NET_M12_C120000 = 120000;
    /**
     * 技能初始化 **
     */
    public static final int NET_M12_C120001 = 120001;
    /**
     * 技能升级 **
     */
    public static final int NET_M12_C120002 = 120002; /*** 技能重置 ***/


    /**
     * 装备相关模块 **
     */
    public static final int NET_M13 = 13;
    public static final int NET_M13_C130000 = 130000;
    public static final int NET_M13_C130001 = 130001;//装备升级
    public static final int NET_M13_C130002 = 130002;//穿戴装备
    public static final int NET_M13_C130003 = 130003;//卸下装备
    public static final int NET_M13_C130004 = 130004;//
    public static final int NET_M13_C130005 = 130005;//装备出售
    public static final int NET_M13_C130006 = 130006;//装备曹调换位置
    public static final int NET_M13_C130007 = 130007;//装备背包扩充

    public static final int NET_M13_C130100 = 130100;//军械碎片信息
    public static final int NET_M13_C130101 = 130101;//军械信息
    public static final int NET_M13_C130102 = 130102;//军械碎片合成军械
    public static final int NET_M13_C130103 = 130103;//军械碎片分解
    public static final int NET_M13_C130104 = 130104;//穿上军械
    public static final int NET_M13_C130105 = 130105;//卸下
    public static final int NET_M13_C130106 = 130106;//军械分解
    public static final int NET_M13_C130107 = 130107;//强化军械
    public static final int NET_M13_C130108 = 130108;//改造军械
    public static final int NET_M13_C130109 = 130109;//军械进化

    /**
     * 聊天相关模块 **
     */
    public static final int NET_M14 = 14;
    public static final int NET_M14_C140000 = 140000;//聊天
    public static final int NET_M14_C140001 = 140001;//查看信息
    public static final int NET_M14_C140002 = 140002;//私聊
    public static final int NET_M14_C140003 = 140003;//收到私聊信息
    public static final int NET_M14_C140004 = 140004;//私聊请求
    public static final int NET_M14_C140005 = 140005;//添加屏蔽
    public static final int NET_M14_C140006 = 140006;//屏蔽列表
    public static final int NET_M14_C140007 = 140007;//移除屏蔽列表
    public static final int NET_M14_C140008 = 140008;//喇叭
    public static final int NET_M14_C140009 = 140009;//获取玩家类型
    /**
     * 抽奖模块 **
     */
    public static final int NET_M15 = 15;
    public static final int NET_M15_C150000 = 150000;//装备抽奖信息
    public static final int NET_M15_C150001 = 150001;//装备抽奖
    public static final int NET_M15_C150002 = 150002;//淘宝抽奖
    public static final int NET_M15_C150003 = 150003;//淘宝购买幸运币

    /**
     * * 邮件模块 ***
     */
    public static final int NET_M16 = 16;
    public static final int NET_M16_C160000 = 160000;//邮件列表
    public static final int NET_M16_C160001 = 160001;//查看邮件信息
    public static final int NET_M16_C160002 = 160002;//新邮件通知
    public static final int NET_M16_C160003 = 160003;//发送邮件
    public static final int NET_M16_C160004 = 160004;//删除邮件
    public static final int NET_M16_C160005 = 160005;//战斗播报
    public static final int NET_M16_C160006 = 160006;//邮件提取
    public static final int NET_M16_C160007 = 160007;//新邮件增加
    /**
     * ******好友模块************
     */
    public static final int NET_M17 = 17;
    public static final int NET_M17_C170000 = 170000; //好友信息列表
    public static final int NET_M17_C170001 = 170001; //添加好友
    public static final int NET_M17_C170002 = 170002; //搜索玩家
    public static final int NET_M17_C170003 = 170003; //删除好友
    public static final int NET_M17_C170004 = 170004; //请求祝福
    public static final int NET_M17_C170005 = 170005; //祝福通知
    public static final int NET_M17_C170006 = 170006; //请求领取祝福

    /**
     * ******任务模块************
     */
    public static final int NET_M19 = 19;
    public static final int NET_M19_C190000 = 190000; //任务初始化信息
    public static final int NET_M19_C190001 = 190001; //领取任务奖励
    public static final int NET_M19_C190002 = 190002; //日常任务操作
    public static final int NET_M19_C190003 = 190003; //领取日常任务活跃

    /**
     * ******竞技场模块************
     */
    public static final int NET_M20 = 20;
    public static final int NET_M20_C200000 = 200000;//竞技场信息
    public static final int NET_M20_C200001 = 200001;//请求战斗
    public static final int NET_M20_C200002 = 200002;//重置防守队伍
    public static final int NET_M20_C200003 = 200003;//购买挑战次数
    public static final int NET_M20_C200004 = 200004;//竞技场商店购买
    public static final int NET_M20_C200005 = 200005;//竞技场上次排名领取
    public static final int NET_M20_C200006 = 200006;//加速竞技场
    public static final int NET_M20_C200100 = 200100;//竞技场战报列表
    public static final int NET_M20_C200101 = 200101;//查看战报详细信息,-------之后推送200100
    public static final int NET_M20_C200102 = 200102;//删除个人战报,------之后推送200100
    public static final int NET_M20_C200104 = 200104;//新增个人战报
    public static final int NET_M20_C200105 = 200105;//更新全服战报
    public static final int NET_M20_C200106 = 200106;//竞技场时间验证

    /**
     * ******排行榜模块************
     */
    public static final int NET_M21 = 21;
    public static final int NET_M21_C210000 = 210000;//
    /**
     * ******军团模块************
     */
    public static final int NET_M22 = 22;
    public static final int NET_M22_C220000 = 220000;//军团等级信息
    public static final int NET_M22_C220002 = 220002;//军团商店物品兑换
    public static final int NET_M22_C220100 = 220100;//军团列表
    public static final int NET_M22_C220101 = 220101;//查询军团详细信息
    public static final int NET_M22_C220102 = 220102;//军团申请 取消申请
    public static final int NET_M22_C220103 = 220103;//创建军团
    public static final int NET_M22_C220104 = 220104;//军团搜索
    public static final int NET_M22_C220200 = 220200;//查看军团信息
    public static final int NET_M22_C220201 = 220201;//军团成员操作 军团操作 踢人 ，转让 退出
    public static final int NET_M22_C220202 = 220202 ;//查看审批列表
    public static final int NET_M22_C220203 = 220203 ;//同意加入军团
    public static final int NET_M22_C220204 = 220204 ;//清空申请列
    public static final int NET_M22_C220205 = 220205 ;//推送申请条数
    public static final int NET_M22_C220210 = 220210 ;//军团编辑军团宣言
    public static final int NET_M22_C220211 = 220211 ;//军团公告
    public static final int NET_M22_C220220 = 220220 ;//职位编辑
    public static final int NET_M22_C220221 = 220221 ;//军团升职 团长任命
    public static final int NET_M22_C220007 = 220007 ;//军团大厅升级
    public static final int NET_M22_C220008 = 220008 ;//军团大厅金币,资源捐献
    public static final int NET_M22_C220009 = 220009 ;//军团科技金币,资源捐献
    public static final int NET_M22_C220010 = 220010 ;//军团科技大厅升级
    public static final int NET_M22_C220012 = 220012 ;//请求福利院信息
    public static final int NET_M22_C220013 = 220013 ;//军团福利院日常福利 >升级 领取
    public static final int NET_M22_C220015 = 220015 ;//军团福利院活跃，资源福利领取
    public static final int NET_M22_C220300 = 220300 ;// 情报站
    public static final int NET_M22_C220400 = 220400 ;// 军团招募

    /******活动*********/
    public static final int NET_M23 = 23;
    public static final int NET_M23_C230000 = 230000;//获取活动列表
    public static final int NET_M23_C230001= 230001;//领取、购买
    public static final int NET_M23_C230002= 230002;//获取限时活动列表
    public static final int NET_M23_C230003= 230003;//获取拉霸活动信息,以及抽奖
    public static final int NET_M23_C230005= 230005;//获取有福同享宝箱列表
    public static final int NET_M23_C230006= 230006;//领取有福同享宝箱奖励
    public static final int NET_M23_C230007= 230007;//刷新单个活动内容
    public static final int NET_M23_C230008= 230008;//删除活动
    public static final int NET_M23_C230009= 230009;//增加一个限时活动
    public static final int NET_M23_C230010= 230010;//检测一个普通活动是否开启
    public static final int NET_M23_C230011= 230011;//检测一个限时活动是否开启
    public static final int NET_M23_C230012= 230012;//检测一个在线礼包活动是否开启
    public static final int NET_M23_C230013= 230013;//推送排行活动排名
    /******cdkey*********/
    public static final int NET_M24 = 24;
    public static final int NET_M24_C240000 = 240000;//cdkey领取

    /******share*********/
    public static final int NET_M25 = 25;
    public static final int NET_M25_C250000 = 250000;//分享


    /******新的build模块*********/
    public static final int NET_M28 = 28;
    public static final int NET_M28_C280001 = 280001;//建筑请求升级 包括建造（0级升1级）
    public static final int NET_M28_C280002 = 280002;//请求完成升级 包括建造（0级升1级）完成成功后，客户端做对应的逻辑
    public static final int NET_M28_C280003 = 280003;//取消建筑升级
    public static final int NET_M28_C280004 = 280004;//建筑加速升级
    public static final int NET_M28_C280005 = 280005;//野外建筑拆除
    public static final int NET_M28_C280006 = 280006;//建筑生产 包括 兵营，校场，工匠坊，科技
    public static final int NET_M28_C280007 = 280007;//请求生产完成
    public static final int NET_M28_C280008 = 280008;//取消生产
    public static final int NET_M28_C280009 = 280009;//加速生产
    public static final int NET_M28_C280011 = 280011;//客户端自己判断是否可以购买VIP购买建筑位，且计算出价格
    public static final int NET_M28_C280012 = 280012;//购买自动升级建筑
    public static final int NET_M28_C280013 = 280013;//自动升级建筑开关
    public static final int NET_M28_C280014 = 280014;//自动升级结束
    public static final int NET_M28_C280015 = 280015;//资源产出校验
    /******计算战斗力独立模块*********/
    public static final int NET_M50 = 50;


    /******军师信息*********/
    public static final int NET_M26 = 26;
    public static final int NET_M26_C260000 = 260000;//军师信息
    public static final int NET_M26_C260001 = 260001;//军师进阶
    public static final int NET_M26_C260002 = 260002;//军师升级
    public static final int NET_M26_C260003 = 260003;//军师分解
    public static final int NET_M26_C260004 = 260004;//军师抽奖信息
    public static final int NET_M26_C260005 = 260005;//军师抽奖
    public static final int NET_M26_C260006 = 260006;//军师一键进阶

    /******军团副本*********/
    public static final int NET_M27 = 27;
    public static final int NET_M27_C270000 = 270000;//军团副本信息
    public static final int NET_M27_C270001 = 270001;//
    public static final int NET_M27_C270002 = 270002;//军团副本询问
    public static final int NET_M27_C270004 = 270004;//获得军团副本某一章节信息
    public static final int NET_M27_C270003 = 270003;//领取宝箱
 }

