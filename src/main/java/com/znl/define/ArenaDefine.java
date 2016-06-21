package com.znl.define;

/**
 * Created by Administrator on 2015/11/20.
 */
public class ArenaDefine {
    public final static  String CMD_CHANGE_RANK="2";//改变排名
    public final static  String CMD_CHANGE_WINTIMES="3";//改变连胜次数
    public final static  String CMD_ADD_PROTIME="6";//添加保护时间
    public final static  String CMD_ASK_FIGHT="7";//请求战斗
    public final static  String CMD_REFRESHCAPITY="8";//刷新战力
    public final static  String CMD_DELTIME="9";//消除时间


    public final static  int FIGHTTIME=5;//挑战次数
    public final static  int ARENA_TIME_WAIT=10*60*1000;//等待时间
    public final static  int ARENA_TIME_PROTECT=1000;//保护时间

    public final static int ARENA_SERVER_REPORT_SIZE = 20;//服务器战报容量

    public final static  int ROBOTGETTYPR1=1;//按照排名获取机器人方式
    public final static  int ROBOTGETTYPR2=2;//按照id获取机器人方式
}
