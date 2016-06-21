package com.znl.define;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2015/11/16.
 */
public class ItemDefine {
    /*道具模版Id*/
    public static final int POWER_typeId = 1;
    /*数量*/
    public static final int POWER_num= 2;

    public static final int COMMANDBOOK_ID = 4013;  //统帅书ID
    public static final int SKILLBOOK_ID = 4012;  //技能书ID

    public static HashMap<Integer,String> NameMap = new HashMap<>();


    /**************道具类型***********/
    public final static List<Integer> ITEM_REWARD_DATE = Arrays.asList(1,16,27);//读取数据生成表
    public final static List<Integer> ITEM_REWARD_RATE= Arrays.asList(2);//读取数据生成表
    public final static List<Integer> ITEM_REWARD_NONE= Arrays.asList(5,6,7,17);//不能使用
    public static final int ITEM_REWARD_BUFFER=3;//buffer
    public static final int ITEM_REWARD_AVOID_WAR=4;//免战类
    public static final int ITEM_REWARD_BUILD_SPEED=5;//建筑升级加速类
    public static final int ITEM_SPEED_TAKEED_PRODUCTION=6;//坦克生产加速
    public static final int ITEM_SPEED_SCIENCE_PRODUCTION=7;//科技升级加速
    public static final int ITEM_SPEED_USE_NEED_ITEM=16;//使用需要消耗道具
    public static final int ITEM_CHANGE_ATTR=18;//改变外观，属性
    public static final int ITEM_CHANGE_RESCOUCE_OUTPUT=19;//外观资源产量
    public static final int ITEM_CHANGE_BUILD_POSITION=20;//外观临时创建位
    public static final int ITEM_CHANGE_FACADE=21;//改变外观
    public static final int ITEM_SEND_RED_EVEOPE=27;//发红包
    public static final int ITEM_CHANGE_SEARCH_POINT=29;//查找玩家基地坐标类
    public static final int ITEM_BASE_RANDOM=30;//基地随机搬迁
    public static final int ITEM_SEARCH_MINES=32;//查找玩家世界矿点
    public static final int ITEM_CHANGE_NAME=33;//玩家改名
    public static final int ITEM_TEMPORORY_BUILDING=34;//增加临时建造位类
    public static final int ITEM_CHANGE_UNION_NAME=35;//军团改名
    public static final int ITEM_SAY_HAI=36;//喇叭类
    public static final int ITEM_ADD_LEGION_SHARE=37;//增加军团贡献度

    /*******特殊道具id*********/
    public static final int MOVE_ITEM_ID = 3311;//定点迁城令
    public static final int MOVE_RANDOM_ITEM_ID = 3301;//随机迁城令
    public static final int CHECK_PLAYER_ITEM_ID = 3291;//定位仪
    public static final int ORE_ITEM_ID = 3321;//矿场勘察点

    public static final int GOLD_REPACEITEM_PRICE=50;//金币代替道具价格
}
