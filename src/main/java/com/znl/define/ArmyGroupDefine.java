package com.znl.define;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2015/11/20.
 */
public class ArmyGroupDefine {

    public final static  int CRETE_COST_GOLD=50;//创建军团消耗金币
    public final static Map<Integer,Integer> mapneed=new HashMap<Integer,Integer>(){
        {
            put(201,300000);
            put(202,300000);
            put(203,300000);
            put(204,300000);
            put(205,300000);
        }
    };

    public final static  int JOIN_TYPE1=1;//直接通过
    public final static  int JOIN_TYPE2=2;//需要审核

    /*****操作类型******/
    public final static  int OPERATE_APPLY=1;//申请
    public final static  int OPERATE_CANCELAPPLY=2;//取消申请
    public final static  int OPERATE_CLEARAPPLYLIST=3;//清空申请


    public final static  int OPERATE_KICK=1;//踢人
    public final static  int OPERATE_transfer=2;//转让
    public final static  int OPERATE_Level=3;// 退出

    /*****获取所有军团信心操作*****/
    public final static  int OPERATE_GETALLGROUP_SEND220100=1;//发送军团信息
    public final static  int OPERATE_GETALLGROUP_SEND220104=2;//军团搜索
    /*****获取单个军团信息操作*****/
    public final static  int OPERATE_GETGROUP_SEND220101=1;//获取军团详细信息

 public final static int ARMYGROUP_FIXSHOP = 1;//军团物品固定商店
    public final static int ARMYGROUP_RANDSHOP = 2;//军团珍品不固定商店

    /*****军团职位*****/
    public final static  int JOB_SELF1=1;//自定义职位1
    public final static  int JOB_SELF2=2;//自定义职位2
    public final static  int JOB_SELF3=3;//自定义职位3
    public final static  int JOB_SELF4=4;//自定义职位4
    public final static  int JOB_NORMAL=5;//普通成员
    public final static  int JOB_MIN_MANGER=6;//副军长团长
    public final static  int JOB_MANGER=7;//军团长

    /*****最多申请个数*****/
    public final static  int MAX_APPLYNUM=5;

    /*****军团捐献次数定时器otherType****/
    public final static  int CONTRUBUTE_HALL=1;//军团大厅
    public final static  int CONTRUBUTE_TECH=2;//军团科技

    /*****军团各升级操作****/
    public final static  int UP_REQ=1;//请求升级
    public final static  int UP_OPT=2;//操作

    /*****军团活跃人物类型otherType****/
    public final static  int MESSIONTYPE1=1;//捐赠
    public final static  int MESSIONTYPE2=2;//打副本
    public final static  int MESSIONTYPE3=3;//兑换
    public final static  int MESSIONTYPE4=4;//驻军
    public final static  int MESSIONTYPE5=5;//团战


    public final static  int JUANZENGTIME=60;//捐赠次数基数
    public final static  int MESSIONTYPENUM1=500;//捐赠
    public final static  int MESSIONTYPENUM3=30;//兑换

    //情报站分类
    public final static  int SITUATION_ARMY=1;//军情
    public final static  int SITUATION_PEOPLE=2;//民情


    public final static  int SITUATION_MAX=100;//情报上限

    //民情分类
    public final static  int PEOPLE_TEST=1;//试炼场
    public final static  int PEOPLE_REWARD=2;//团长发奖
    public final static  int PEOPLE_JOIN=3;//加入军团
    public final static  int PEOPLE_LEVEL=4;//退出军团
    public final static  int PEOPLE_KICK=5;//踢出
    public final static  int PEOPLE_CHANGE_MASTER=6;//换团长
    public final static  int PEOPLE_NAME=7;//任命
    public final static  int BUILD_UP=8;//建筑升级

   //军团招募时间间隔
    public final static  int ENDLIST_TIME=30*60*1000;

    //军团加入多少时间才能领取资源
    public final static  long needTime=24*60*60*1000;
}

