package com.znl.base;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import com.znl.GameMainServer;
import com.znl.define.ActorDefine;
import com.znl.define.DataDefine;
import com.znl.define.FunctionIdDefine;
import com.znl.define.PlayerPowerDefine;
import com.znl.log.CustomerLogger;
import com.znl.log.admin.tbllog_function;
import com.znl.msg.GameMsg;
import com.znl.msg.ShareMsg;
import com.znl.pojo.db.Equip;
import com.znl.pojo.db.Report;
import com.znl.proto.M25;
import com.znl.proxy.ConfigDataProxy;
import com.znl.proxy.GameProxy;
import com.znl.proxy.PlayerProxy;
import com.znl.utils.GameUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2015/10/27.
 */


abstract public class BasicProxy{


  private GameProxy gameProxy = null;
  protected String areaKey;

  /** *
    * 释放掉对应的BD POJO
    */
  @Override
  public void finalize() throws Throwable {
    shutDownProxy();
    super.finalize();
  }

  public abstract void shutDownProxy();

  public GameProxy getGameProxy() {
    return gameProxy;
  }

  public void setGameProxy(GameProxy gameProxy) {
    this.gameProxy = gameProxy;
  }

  public<T> T getProxy(String name) {
    return (T)gameProxy.getProxy(name);
  }

  protected abstract void init();

  protected void refurceExpandPowerMap(){
    ConcurrentHashMap<Integer,Long> map  = new ConcurrentHashMap<>(expandPowerMap);
    init();
    List<JSONObject> sendPowers = ConfigDataProxy.getConfigAllInfo(DataDefine.RESOURCE);

    //比较重置前和重置后的两个map，取出有不一样的通知到playerProxy
    for (JSONObject key :sendPowers){
      int power = key.getInt("ID");
      PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
      if (expandPowerMap.containsKey(power) && !map.containsKey(power)){
        playerProxy.addPowerToChangePower(power);
      }else if(!expandPowerMap.containsKey(power) && map.containsKey(power)){
        playerProxy.addPowerToChangePower(power);
      }else  if (expandPowerMap.containsKey(power) && map.containsKey(power)){
        if(map.get(power) != expandPowerMap.get(power).longValue()){
          playerProxy.addPowerToChangePower(power);
        }
      }
    }
  }

  private Queue<BaseDbPojo> dbPojoQueue = new LinkedList<>();
  //保存要入库的数据
  public void saveDbPojo(){
    synchronized (dbPojoQueue) {
      while (dbPojoQueue.size() > 0){
        BaseDbPojo pojo = dbPojoQueue.poll();
        pojo.save();
      }
    }
  }

  //预保存
  protected void offerDbPojo(BaseDbPojo pojo){
    dbPojoQueue.offer(pojo);
  }

  protected ConcurrentHashMap<Integer,Long> expandPowerMap  = new ConcurrentHashMap<>();

  public long getExpandPowerValue(int power){
    long value = 0;
    if(expandPowerMap.containsKey(power)){
      value = expandPowerMap.get(power);
    }
    return value;
  }


  protected void sendPorxyLog(BaseLog baseLog){
    try{
      String path = baseLog.getClass().getCanonicalName();
      if (path.indexOf("admin") > -1){
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        int logicAreaId = GameMainServer.getLogicAreaIdByAreaId(playerProxy.getAreaId());
        ActorSelection logServer = GameMainServer.system().actorSelection(ActorDefine.AREA_SERVER_PRE_PATH+logicAreaId+"/"+ActorDefine.ADMIN_LOG_SERVICE_NAME);
        GameMsg.SendAdminLog msg = new GameMsg.SendAdminLog(baseLog,ActorDefine.ADMIN_LOG_ACTION_INSERT, "", 0, "");
        logServer.tell(msg, ActorRef.noSender());
      }else {
        ActorSelection logServer = GameMainServer.system().actorSelection(ActorDefine.LOG_SERVER_PATH);
        GameMsg.SendLog msg = new GameMsg.SendLog(baseLog);
        logServer.tell(msg, ActorRef.noSender());
      }
    }catch (Exception e){
      CustomerLogger.error("发送平台日志出错", e);
      e.printStackTrace();
    }

  }

  protected void sendFunctionLog(int functionId,long expand1,long expand2,long expand3,String expandStr){
    if (gameProxy != null){
      PlayerProxy playerProxy  = getProxy(ActorDefine.PLAYER_PROXY_NAME);
      tbllog_function log   = new tbllog_function();
      log.setAccount_name(playerProxy.getAccountName());
      log.setRole_id(playerProxy.getPlayerId());
      log.setPlatform(playerProxy.getPlayerCache().getPlat_name());
      log.setDim_level(playerProxy.getLevel());
      log.setAction_id(functionId);
      log.setStatus(FunctionIdDefine.ACCOMPLISH);
      log.setExpand1(expand1);
      log.setExpand2(expand2);
      log.setExpand3(expand3);
      log.setExpandstr(expandStr);
      int now = GameUtils.getServerTime();
      log.setLog_time(now);
      log.setHappend_time(now);
      sendPorxyLog(log);
    }
  }

  protected void sendFunctionLog(int functionId,long expand1,long expand2,long expand3){
    if (gameProxy != null){
      PlayerProxy playerProxy  = getProxy(ActorDefine.PLAYER_PROXY_NAME);
      tbllog_function log   = new tbllog_function();
      log.setAccount_name(playerProxy.getAccountName());
      log.setRole_id(playerProxy.getPlayerId());
      log.setPlatform(playerProxy.getPlayerCache().getPlat_name());
      log.setDim_level(playerProxy.getLevel());
      log.setAction_id(functionId);
      log.setStatus(FunctionIdDefine.ACCOMPLISH);
      log.setExpand1(expand1);
      log.setExpand2(expand2);
      log.setExpand3(expand3);
      int now = GameUtils.getServerTime();
      log.setLog_time(now);
      log.setHappend_time(now);
      sendPorxyLog(log);
    }
  }

  public void sendUpdateLog(BaseLog baseLog){
    try{
      PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
      ActorSelection logServer = GameMainServer.system().actorSelection(ActorDefine.AREA_SERVER_PRE_PATH+GameMainServer.getLogicAreaIdByAreaId(playerProxy.getAreaId())+"/"+ActorDefine.ADMIN_LOG_SERVICE_NAME);
      GameMsg.SendAdminLog msg = new GameMsg.SendAdminLog(baseLog,ActorDefine.ADMIN_LOG_ACTION_UPDATE, "role_id", playerProxy.getPlayerId(), "");
      logServer.tell(msg, ActorRef.noSender());
    }catch (Exception e){
      CustomerLogger.error("发送平台日志出错", e);
      e.printStackTrace();
    }
  }

  protected void sendRankServiceMsg(Object msg){
    PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
    String path = ActorDefine.AREA_SERVER_PRE_PATH+GameMainServer.getLogicAreaIdByAreaId(playerProxy.getAreaId())+"/"+ActorDefine.POWERRANKS_SERVICE_NAME;
    ActorSelection arenaService = GameMainServer.system().actorSelection(path);
    arenaService.tell(msg, ActorRef.noSender());
  }

  protected void sendMailServiceMsg(Object msg){
    PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
    String path = ActorDefine.AREA_SERVER_PRE_PATH+GameMainServer.getLogicAreaIdByAreaId(playerProxy.getAreaId())+"/"+ActorDefine.MAIL_SERVICE_NAME;
    ActorSelection arenaService = GameMainServer.system().actorSelection(path);
    arenaService.tell(msg, ActorRef.noSender());
  }

  protected void sendSystemChatToPlayerService(M25.M250000.S2C msg){
    PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
    String path = ActorDefine.AREA_SERVER_PRE_PATH+GameMainServer.getLogicAreaIdByAreaId(playerProxy.getAreaId())+"/"+ActorDefine.PLAYER_SERVICE_NAME;
    ActorSelection arenaService = GameMainServer.system().actorSelection(path);
    arenaService.tell(new ShareMsg(msg,0), ActorRef.noSender());
  }

  /**
   * 通知军团node
   * @param msg
   * @param id id 军团id
   */
  protected void sendArmygroupNodeMsg(Object msg,Long id){
    PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
    String path = ActorDefine.AREA_SERVER_PRE_PATH+GameMainServer.getLogicAreaIdByAreaId(playerProxy.getAreaId())+"/"+ActorDefine.ARMYGROUP_SERVICE_NAME+ "/" + ActorDefine.ARMYGROUPNODE+id;
    ActorSelection armyGroupNode = GameMainServer.system().actorSelection(path);
    armyGroupNode.tell(msg, ActorRef.noSender());
  }

  /**
   * 4点钟刷新事件
   * 需要处理的记得重写此方法
   */
  public void fixedTimeEventHandler(){

  }

  /**
   * 每天零点事件
   */
  public void zeroTimerEventHandler(){

  }

  /**
   * 每次成功登录之后的事件处理
   */
  public  void afterLoginEvent(){

  }


}
