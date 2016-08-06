package com.znl.define;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2015/11/6.
 */
public class ResFunBuildDefine {
 /*******定时器类型**************/
 public static final int BUILDE_TYPE_RESOUCE = 1;//野外建筑
 public static final int BUILDE_TYPE_FUNTION= 2;//基地建筑

 public final static List<Integer> RESOUCETYPELIST = Arrays.asList(2,3,4,5,6);//资源建筑
 public final static List<Integer> FUNCTIONTYPELIST = Arrays.asList(8,9,10,11);//功能建筑
 public final static List<Integer> REMOVEBUILDLIST = Arrays.asList(3,4,5);//拆除野外铜、铁、油三类建筑时，扣除相应的繁荣度
 public final static List<Integer> SPEEDBUILDLEVELUP = Arrays.asList(3131,3132,3133);//建筑升级加速道具
 public final static List<Integer> BASEBUILDLIST = Arrays.asList(1,9,10,13,14,15,16,8,7,17,11);//基地建筑
 public final static List<Integer> PRODUCTBUILD = Arrays.asList(8,9,10,11);//生产建筑
 public final static List<Integer> RESOURCESCIENCE = Arrays.asList(1,4,7,10,14);//资源科技
 public final static List<Integer> NO_NEED_SAVE_FUNCTION = Arrays.asList(6,7,8,13,14);//不需要保存的建筑

 public static final int BUILDE_TYPE_COMMOND = 1;//司令部
 public static final int BUILDE_TYPE_GEM_PROCESS= 2;//宝石加工
 public static final int BUILDE_TYPE_CUPROPROCESS= 3;//铜矿场
 public static final int BUILDE_TYPE_IRON_PROCESS= 4;//铁矿场
 public static final int BUILDE_TYPE_OIL_WELL= 5;//油井
 public static final int BUILDE_TYPE_SI_PROCESS = 6;//硅矿场
 public static final int BUILDE_TYPE_DEPOT = 7;//仓库
 public static final int BUILDE_TYPE_SCIENCE =8;//科技馆
 public static final int BUILDE_TYPE_TANK= 9;//战车工厂
 public static final int BUILDE_TYPE_RREFIT= 10;//改装工厂
 public static final int BUILDE_TYPE_CREATEROOM = 11;//制造车间

 public final static int MAX_RESOURCE_TYPE = BUILDE_TYPE_DEPOT;

/************等*待*队*列*初始值*************/
public static final int MIN_WAITQUEUE = 1;

/***************建筑升级返还比例***********/
public static final int CANCEL_LEVEL_RETURN = 70;


 /***************建筑升级最大队列***********/
 public static final int BUILD_LEVING_MAX = 3;

 public static final int MIN_BUILD_SIZE = 2; //初始建筑位2
 public static final int MIN_BUY_BUILD_GOlD = 30; //初始建筑位2时，购买第三个建筑位需30金币,之后递增30，
 public static final int BUY_BUILD_SIZE_GOLD = 120; //购买建筑位花费金币120

/******生产队列状态******/
 public static final int PRODUCTION_STATE_WORKING = 1;//生产中
 public static final int PRODUCTION_STATE_WAITING = 2;//等待中





}
