package com.znl.modules.login;

import akka.actor.Props;
import akka.japi.Creator;
import com.mchange.v2.c3p0.PooledDataSource;
import com.znl.GameMainServer;
import com.znl.base.BaseDbPojo;
import com.znl.base.BaseSetDbPojo;
import com.znl.base.BasicModule;
import com.znl.core.PlayerCache;
import com.znl.core.PlayerTask;
import com.znl.define.*;
import com.znl.framework.socket.Request;
import com.znl.log.CustomerLogger;
import com.znl.log.admin.tbllog_event;
import com.znl.log.admin.tbllog_login;
import com.znl.log.admin.tbllog_player;
import com.znl.msg.GameMsg;
import com.znl.pojo.db.Player;
import com.znl.pojo.db.set.AccountNameSetDb;
import com.znl.proto.M1;
import com.znl.proto.M2;
import com.znl.proto.M3;
import com.znl.proto.M8;
import com.znl.proxy.*;
import com.znl.service.PlayerService;
import com.znl.utils.GameUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2015/10/23.
 */
public class LoginModule extends BasicModule  {


    public static Props props(final int areaId){
        return Props.create(new Creator<LoginModule>(){
            private static final long serialVersionUID = 1L;
            @Override
            public LoginModule create() throws Exception {
                return new LoginModule(areaId) ;
            }
        });
    }

    private final int areaId;
    public LoginModule(int areaId){
        this.areaId = areaId;
        this.setModuleId(ProtocolModuleDefine.NET_M1);
    }

    //TODO 如果出现异常，这里也要把他赋值为true
    private boolean isInit = false;

    private void OnTriggerNet10000Event(Request request){
        //重连成功的话，playerActor已经创建完毕的了，不做创建角色操作
        //TODO 这里需要一个判断，是否已经初始化过了

        if(isInit){
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            if(playerProxy.isBanAct()){
                sendClientLoginResult(ErrorCodeDefine.M100001_2);
                loginFail(playerProxy.getPlayer());
            }else {
                sendClientLoginResult(0);
                //这里再直接通知
                sendModuleMsg(ActorDefine.ROLE_MODULE_NAME, new GameMsg. InitSendRoleInfo());
            }

        }else{
            this.onReceiveLoginReq(request);
        }

    }

    private void OnTriggerNet10001Event(Request request){
        this.onReceiveCreateRoleReq(request);
    }

    //事件日志请求
    public void OnTriggerNet10002Event(Request request) {
        M1.M10002.C2S c2s = request.getValue();

        int eventId = c2s.getEventId();

        tbllog_event eventLog = new tbllog_event();
        eventLog.setEvent_id(eventId);
        eventLog.setAccount_name(playerCache.getAccount());
        eventLog.setRole_id(playerCache.getPlayerId());
        eventLog.setPlatform(playerCache.getPlat_name());
        eventLog.setUser_ip(playerCache.getUser_ip());
        eventLog.setDid(playerCache.getImei());
        eventLog.setOs(playerCache.getOsName());
        eventLog.setDevice(playerCache.getModel());
        eventLog.setDevice_type(playerCache.getModel());
        eventLog.setScreen(playerCache.getScreen());
        eventLog.setMno(playerCache.getOperators());
        eventLog.setNm(playerCache.getNet());
        eventLog.setHappend_time(GameUtils.getServerTime());
        eventLog.setGame_version(playerCache.getGame_version());

        sendLog(eventLog);

        sendNetMsg(ProtocolModuleDefine.NET_M1, ProtocolModuleDefine.NET_M1_C10002, M1.M10002.S2C.newBuilder().setRs(0).build());
        sendMsg(ProtocolModuleDefine.NET_M1_C10002);
        sendPushNetMsgToClient(ProtocolModuleDefine.NET_M1_C10002);
    }

    //发送给客户端，登录成功
    private void sendClientLoginResult(int rs){
        M1.M10000.S2C s2c = M1.M10000.S2C.newBuilder().setRs(rs).build();  //直接告诉客户端登录成功了，客户端做相应的逻辑，等服务器正在登陆成功后，主动推送20000协议
        pushNetMsg(ProtocolModuleDefine.NET_M1, ProtocolModuleDefine.NET_M1_C10000, s2c); //不要用send
      //  sendPushNetMsgToClient();
        sendMsg(0);
    }


    //TODO 这个模块吃重启后，会出现异常
    private PlayerCache playerCache; //用来缓存玩家Cache数据，用来处理Event日志
    /**
     * 接受到登录请求
     *10000协议
     * @param request
     */
    private void onReceiveLoginReq(Request request){

        int rs = 0;
        CustomerLogger.info("======onReceiveLoginReq===========");
        CustomerLogger.info("##################################################################################################");
        CustomerLogger.info("##################################################################################################");
        CustomerLogger.info("##################################################################################################");
        CustomerLogger.info("##################################################################################################");
        CustomerLogger.info("##################################################################################################");
        CustomerLogger.info("##################################################################################################");
        M1.M10000.C2S c2s = request.getValue();


        String ip = GameUtils.getIpByIoSession(request.getSession());

        PlayerCache cache = new PlayerCache();
        cache.setAccount(c2s.getAccount());
        cache.setAreId(c2s.getAreId());
        cache.setFill_register_msg_times(c2s.getFillRegisterMsgTimes());
        cache.setImei(c2s.getImei());
        cache.setLocation(c2s.getLocation());
        cache.setModel(c2s.getModel());
        cache.setNet(c2s.getNet());
        cache.setOperators(c2s.getOperators());
        cache.setOs(c2s.getOs());
        cache.setPackage_name(c2s.getPackageName());
        cache.setPackage_size(c2s.getPackageSize());
        cache.setPlat_id(c2s.getPlatId());
        cache.setScreen(c2s.getScreen());
        cache.setStartup_times(c2s.getStartupTimes());
        cache.setUtma(c2s.getUtma());
        cache.setUser_ip(ip);
        cache.setGame_version(c2s.getGameVersion());
        cache.setPushChanelId(c2s.getPushChannelId());
        JSONObject platInfo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.PLAT,"platID",c2s.getPlatId());
        if(platInfo != null){
            cache.setPlat_name(platInfo.getString("platName"));
        }else{
            cache.setPlat_name("unknown");
        }

        if(c2s.getOs() == 1){
            cache.setOsName("Android");
        }else if(c2s.getOs() == 2){
            cache.setOsName("IOS");
        }else{
            cache.setOsName("both");
        }

        playerCache = cache;

        String accountName = c2s.getAccount();
        accountName = accountName + "_" + c2s.getAreId();  //跟Actor命名规则一致 TODO
        String areaKey = this.getAreaKey();

        AccountNameSetDb accountNameSetDb = BaseSetDbPojo.getSetDbPojo(AccountNameSetDb.class, areaKey);
        Player player = accountNameSetDb.getDbPojoByKey(accountName, Player.class);

        if(player == null){  //创建角色
            CustomerLogger.info("======没有角色===========");
            rs = 0;
           // sendClientLoginResult(rs);
            player = createEmptyPlayer(accountName,cache);
            sendClientLoginResult(rs);
        }else{
            //TODO 这里要做一层判断，是否被禁号了
            if(PlayerService.isBanIp(ip)){
                rs = ErrorCodeDefine.M10000_3;
            }
            if(player.getBanAct() == 1){
                if(player.getBanActDate() >= GameUtils.getServerTime()){
                    //还在被禁号
                    rs = ErrorCodeDefine.M10000_2;
                }else{
                    player.setBanAct(0);
                    player.setBanActDate(0);
                }
            }
  //          sendClientLoginResult(rs);  //直接就先判断，快速通知客户端状态

            if(rs < 0){  //登录失败
                loginFail(player);
            }else{
                GameProxy gameProxy = new GameProxy(player,cache);
                if(player.getName().equals("")){
                    CustomerLogger.info("======还没有创建名字===========");
                    rs = 0;
                }else{
                    rs = 0;
                }
                loginSuccess(player,gameProxy,cache);
            }
        }
        sendClientLoginResult(rs);  //直接就先判断，快速通知客户端状态
        if(rs >= ErrorCodeDefine.M10000_1) {  //登录成功
            cache.setPlayerId(player.getId());
            isInit = true;
            tellLogin();
            sendFuntctionLog(FunctionIdDefine.LOGIN_FUNCTION_ID);
        }
    }

    //第一次登陆，创建一个新的player
    private Player createEmptyPlayer(String accountName,PlayerCache cache) {
        String areaKey = this.getAreaKey();
        AccountNameSetDb accountNameSetDb = BaseSetDbPojo.getSetDbPojo(AccountNameSetDb.class, areaKey);

        Player player = accountNameSetDb.createDbPojo(accountName, Player.class);
        if (player == null){
            System.out.println("createDbPojo的时候Player是个空！！！！");
        }
        player.setAreaId(this.areaId);
        player.setAccountName(accountName);
        player.setDungeoId(ActorDefine.MIN_DUNGEO_ID);
        player.setRegTime(GameUtils.getServerTime());
//        player.setLevel(1);
//        player.setEnergy(ActorDefine.MAX_ENERGY);
//        player.setBoom((long) ActorDefine.MIN_BOOM);
        Set<Integer> setList=new HashSet<Integer>();
        setList.add(1);
        setList.add(2);
        setList.add(3);
        setList.add(4);
        player.setRemianset(setList);
        player.setPlatName(cache.getPlat_name());
        player.save();


//        DbProxy.tell(new GameMsg.AddPlayerByAccountName(accountName, playerId, areaKey), self());

        GameProxy gameProxy = new GameProxy(player,cache);
        this.setGameProxy(gameProxy); //  新加的
        SoldierProxy soldierProxy = gameProxy.getProxy(ActorDefine.SOLDIER_PROXY_NAME);

//        soldierProxy.creatSoldier(SoldierDefine.DEFAULT_SOLDIER_TYPE_ID,SoldierDefine.DEFAULT_SOLDIER_NUM,playerId);

        PlayerProxy playerProxy = gameProxy.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        playerProxy.setPlayerCache(cache);

        DungeoProxy dungeoProxy = gameProxy.getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        dungeoProxy.openNewDungeo(ActorDefine.MIN_DUNGEO_ID);
        //创建所有的冒险关卡
        List<JSONObject> list = ConfigDataProxy.getConfigAllInfo(DataDefine.ADVENTURE);
        for(JSONObject define : list){
            dungeoProxy.openNewDungeo(define.getInt("ID"));
        }

        initResouce(gameProxy);
        loginSuccess(player, gameProxy,cache);

        pushMsgToPlayerDevice(playerProxy.getPushChannelId(),"系统信息", "欢迎第一次登陆！", 0);
        //TODO 先默认创建玩家 就创建一个建筑
        //创建好之后 ，需要再通知客户端
//        sendServiceMsg(ActorDefine.WORLD_SERVICE_NAME, new GameMsg.AutoAddBuilding(player.getId(), accountName));

        player.setRegTime(GameUtils.getServerTime());
        player.setLastLoginTime(GameUtils.getServerTime());
        /**
         * tbllog_player
         */
        tbllog_player playerlog= new tbllog_player();
        playerlog.setPlatform(cache.getPlat_name());
        playerlog.setRole_id(player.getId());
        playerlog.setAccount_name(accountName);
        playerlog.setRole_name(player.getName());
        playerlog.setReg_time(GameUtils.getServerTime());
        playerlog.setReg_ip(cache.getUser_ip());
        playerlog.setLast_login_time(GameUtils.getServerTime());
        playerlog.setHappend_time(GameUtils.getServerTime());
//        playerlog.setFirst_pay_time(player.getFirstChargeTime());
//        playerlog.setLast_pay_time(player.getLastChargeTime());
        sendLog(playerlog);

        return player;
    }

    /**
     * 接受创建角色请求 修改名字 TODO
     *10001协议
     * @param request
     */
    private void onReceiveCreateRoleReq(Request request){

    }


    //登录失败 要把这个玩家的Actor Kill掉
    private void loginFail(Player player){
        context().parent().tell(new GameMsg.LoginFail(player), self());
    }

    private void loginSuccess(Player player, GameProxy gameProxy,PlayerCache cache){
       // TimerdbProxy timerdbProxy=gameProxy.getProxy(ActorDefine.TIMERDB_PROXY_NAME);
       // timerdbProxy.initTimer();
//        ResFunBuildProxy resFunBuildProxy=gameProxy.getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
//        resFunBuildProxy.initResFuBuild(new ArrayList<List<Integer>>());
        NewBuildProxy newBuildProxy = gameProxy.getProxy(ActorDefine.NEW_BUILD_PROXY_NAME);
        newBuildProxy.initResFuBuild(player);
        SoldierProxy soldierProxy = gameProxy.getProxy(ActorDefine.SOLDIER_PROXY_NAME);
        soldierProxy.initHighestCapacity();
        FormationProxy formationProxy = gameProxy.getProxy(ActorDefine.FORMATION_PROXY_NAME);
        formationProxy.initFormation();
        formationProxy.checkBaseDefendTroop(soldierProxy);
        PlayerProxy playerProxy = gameProxy.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        playerProxy.setPlayerCache(cache);
        SkillProxy skillProxy = gameProxy.getProxy(ActorDefine.SKILL_PROXY_NAME);
        skillProxy.initSkillInfo();
        TechnologyProxy technologyProxy = gameProxy.getProxy(ActorDefine.TECHNOLOGY_PROXY_NAME);
        technologyProxy.initTechnology();
        VipProxy vipProxy = gameProxy.getProxy(ActorDefine.VIP_PROXY_NAME);
        vipProxy.initVipData();
        TaskProxy taskProxy=gameProxy.getProxy(ActorDefine.TASK_PROXY_NAME);
        taskProxy.initTask();
        context().parent().tell(new GameMsg.LoginSuccess(player,gameProxy), self());
   //     player.setLoginTime(GameUtils.getServerTime());
        player.setLoginLevel(player.getLevel());
        playerProxy.loginDayNum();

        player.setLastLoginTime(GameUtils.getServerTime());

        /***
         * tbllog_login日志
         */
        tbllog_login loginlog = new tbllog_login();
        loginlog.setPlatform(cache.getPlat_name());
        loginlog.setRole_id(player.getId());
        loginlog.setAccount_name(player.getAccountName());
        loginlog.setDim_level(player.getLevel());
        loginlog.setUser_ip(cache.getUser_ip());
        loginlog.setLogin_map_id(0);
        loginlog.setDid(cache.getImei());
        loginlog.setGame_version(cache.getGame_version());
        loginlog.setOs(cache.getOsName());
        loginlog.setOs_version(cache.getModel());
        loginlog.setDevice(cache.getModel());
        loginlog.setDevice_type(cache.getModel());
        loginlog.setScreen(cache.getScreen());
        loginlog.setMno(cache.getOperators());
        loginlog.setNm(cache.getNet());
        loginlog.setHappend_time(GameUtils.getServerTime());

        sendLog(loginlog);

        tbllog_player playerLog = new tbllog_player();
        playerLog.setLast_login_time(GameUtils.getServerTime());
        playerLog.setHappend_time(GameUtils.getServerTime());
        playerProxy.sendUpdateLog(playerLog);  //登录成功后，更新玩家的最后登录时间

        this.setGameProxy(gameProxy);
    }



    @Override
    public void onReceiveOtherMsg(Object object){
        if (object instanceof GameMsg.CreateWorldBuild) {
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            sendServiceMsg(ActorDefine.WORLD_SERVICE_NAME, new GameMsg.AutoAddBuilding(playerProxy.getPlayerId(), playerProxy.getAccountName()));
        }
    }





    private String getAreaKey(){
        return GameMainServer.getAreaKeyByAreaId(this.areaId);
    }

    //初始化资源
    public void initResouce(GameProxy gameProxy){
        PlayerProxy playerProxy = gameProxy.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        SoldierProxy soldierProxy = gameProxy.getProxy(ActorDefine.SOLDIER_PROXY_NAME);
        ItemProxy itemProxy = gameProxy.getProxy(ActorDefine.ITEM_PROXY_NAME);
        List<JSONObject> list = ConfigDataProxy.getConfigAllInfo(DataDefine.INIT_POWER);
        for (JSONObject define : list){
            if(define.getInt("b_type") == PlayerPowerDefine.BIG_POWER_RESOURCE){
                if (define.getInt("typeid") == 120){
                    System.out.println(define.getInt("typeid")+","+define.getInt("num"));
                }
                playerProxy.addPowerValue(define.getInt("typeid"),define.getInt("num"),LogDefine.GET_INITITEM);
            }
        }
        //要先增加完资源属性之后才能增加其他的类型，不然会报错
        playerProxy.allTakeSoldierNum();
        for (JSONObject define : list){
            if(define.getInt("b_type") == PlayerPowerDefine.BIG_POWER_SOLDIER){
                soldierProxy.creatSoldier(define.getInt("typeid"), define.getInt("num"), playerProxy.getPlayerId());
            }else if(define.getInt("b_type") == PlayerPowerDefine.BIG_POWER_ITEM){
                itemProxy.addItem(define.getInt("typeid"), define.getInt("num"),LogDefine.GET_INITITEM);
            }
        }
        // 新号加玩家免战buff
//        M3.M30000.S2C.Builder builder = M3.M30000.S2C.newBuilder();
//        builder.setType(0);
//        pushNetMsg(ProtocolModuleDefine.NET_M3, ProtocolModuleDefine.NET_M3_C30000, builder.build());
//        sendPushNetMsgToClient();
        ItemBuffProxy itemBuffProxy=gameProxy.getProxy(ActorDefine.ITEMBUFF_PROXY_NAME);
        long overTime = (GameUtils.getServerDate().getTime())+(120*TimerDefine.BUFF_MSEL);
        itemBuffProxy.addItemBuff(3121,ItemDefine.ITEM_REWARD_AVOID_WAR, PlayerPowerDefine.NOR_POWER_protect_date,1,120,1);
        playerProxy.setProtectOverDate(overTime);
        playerProxy.savePlayer();
    }

    private void tellLogin(){
        PlayerProxy playerProxy =getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if(playerProxy.getArmGrouId()>0) {
            GameMsg.changeMenberlogintime mess=new GameMsg.changeMenberlogintime(playerProxy.getPlayerId());
            context().actorSelection("../../../" + ActorDefine.ARMYGROUP_SERVICE_NAME + "/" + ActorDefine.ARMYGROUPNODE +playerProxy.getArmGrouId()).tell(mess, self());
        }
    }

    /**
     * 重复协议请求处理
     * @param request
     */
    @Override
    public void repeatedProtocalHandler(Request request) {

    }
}
