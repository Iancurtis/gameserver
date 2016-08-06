package com.znl

import java.io._
import java.util.concurrent.ConcurrentHashMap
import java.util.{Date, Properties}

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import com.znl.define.{ActorDefine, GameDefine, PlayerPowerDefine, SoldierDefine}
import com.znl.framework.http.base.GameHttpServer
import com.znl.framework.socket.base.GameNetServer
import com.znl.hotReplace.HotReplaceClass
import com.znl.log.CustomerLogger
import com.znl.msg.GameMsg._
import com.znl.proxy.ConfigDataProxy
import com.znl.server._
import com.znl.service.WorldService
import com.znl.utils.{GameUtils, Hooker}

import scala.collection.JavaConversions._


/**
 * Created by woko on 2015/10/7.
 */
object GameMainServer extends App{
  val userDir: String = System.getProperty("user.dir")
  val confPath: String = userDir + File.separator + "properties" + File.separator + "application.conf"
  val file = new File(confPath)

   implicit var system = ActorSystem(ActorDefine.APP_GAME_NAME, ConfigFactory.parseFile(file))
   val gameNetServer : GameNetServer = new GameNetServer
  val gameHttpServer : GameHttpServer = new GameHttpServer

  def setSystem(system : ActorSystem) ={  //仅供测试使用
    this.system = system
  }

  var protectTime = 10
  val maxAreaServerNum = 5
  var areaKeyMap : ConcurrentHashMap[Int, String] = new ConcurrentHashMap[Int, String]()
  var openServerDateMap : ConcurrentHashMap[Int, Date] = new ConcurrentHashMap[Int, Date]()
  val gamePropertiesPath: String = userDir + File.separator + "properties" + File.separator + "game.properties"
  val p: Properties = new Properties
  try {
    val inputStream: InputStream = new FileInputStream(gamePropertiesPath)
    p.load(inputStream)
    val port: Int = p.getProperty("port").toInt
    GameUtils.setServerType(p.getProperty("server_type").toInt)
    GameUtils.setCenterServerIp(p.getProperty("center_server_ip"))
    GameUtils.setGameId(p.getProperty("game_id").toInt)
    GameUtils.test=p.getProperty("test").toBoolean
    GameUtils.redisExpireTime=p.getProperty("redisExpireTime").toInt
    GameUtils.mysqlDbName=p.getProperty("mysql_db")
    val http_port : Int = p.getProperty("http_port").toInt
    protectTime = p.getProperty("protect_time").toInt
    p.propertyNames().foreach(v => {
      val name = v.toString
      if(name.toString.indexOf("areaServer") >= 0){
        val areaLoginId = name.replace("areaServer", "").toInt
        val areaKey : String = p.getProperty(name)
        areaKeyMap += ( areaLoginId -> areaKey)
      }
    })
    p.propertyNames().foreach(v => {
      val name = v.toString
      if(name.toString.indexOf("server_open_date") >= 0){
        val areaLoginId = name.replace("server_open_date", "").toInt
        val dateStr : String = p.getProperty(name)
        openServerDateMap += ( areaLoginId -> GameUtils.getDateFromStr(dateStr))
      }
    })

    starServer()
    startGameRootSystem()
    Thread.sleep(1000) //TODO 这里要先确保DB先连上

    startGameNetServer(port)
    startGameHttpServer(http_port)

    Runtime.getRuntime.addShutdownHook(new Hooker())

  }
  catch {
    case e: IOException => {
      e.printStackTrace
    }
  }

  def reloadGameProperties(): Unit ={
    try {
      //目前只重读开服时间，其他因功能性上锁定，需要重启服务器才能生效
      val inputStream: InputStream = new FileInputStream(gamePropertiesPath)
      p.load(inputStream)
      openServerDateMap.clear()
      GameUtils.test = p.getProperty("test").toBoolean
      p.propertyNames().foreach(v => {
        val name = v.toString
        if(name.toString.indexOf("server_open_date") >= 0){
          val areaLoginId = name.replace("server_open_date", "").toInt
          val dateStr : String = p.getProperty(name)
          openServerDateMap += ( areaLoginId -> GameUtils.getDateFromStr(dateStr))
        }
      })
    }
    catch {
      case e: IOException => {
        e.printStackTrace
      }
    }
  }

  def getServerPlatName()={
    p.getProperty("serverPlat")
  }

  //启动整个游戏服务
  def startGameRootSystem() ={
    system.actorOf(RootGameSystem.props(p), ActorDefine.ROOT_GAME_NAME)
  }

  def startGameNetServer(port : Int) ={
    this.gameNetServer.startSocketServer(port)
  }

  def stopGameNetServer() ={
    this.gameNetServer.stop()
  }

  def startGameHttpServer(port : Int) ={
    gameHttpServer.startHttpServer(port)
  }

  def stopGameHttpServer() ={
    gameHttpServer.stop()
  }


  //通过玩家的区 ID，获得对应的areaKey
  def getAreaKeyByAreaId(areaId : Int) ={
    var areaKey = ""
    for (value <- areaKeyMap.values()){
      val areaStrs = value.split(GameDefine.AREA_SPLIT)
      for (areaStr <- areaStrs){
        if (areaStr.toInt == areaId){
          areaKey = value
        }
      }
    }
    areaKey
  }

  def getAreaKeyByLogicAreaId(logicAreaId : Int) ={
    areaKeyMap.get(logicAreaId)
  }

  def getLogicAreaIdByAreaId(areaId : Int) ={
    var logicAreaId = -1
    for (key <- areaKeyMap.keys()){
      val value = areaKeyMap.get(key)
      val areaStrs = value.split(GameDefine.AREA_SPLIT)
      for (areaStr <- areaStrs){
        if (areaStr.toInt == areaId){
          logicAreaId = key
        }
      }
    }
    logicAreaId
  }

  def checkAreaIdInAreaKey(areaKey : String,areaId : Int): Boolean ={
    var res = false
    val areaStrs = areaKey.split(GameDefine.AREA_SPLIT)
    for (areaStr <- areaStrs){
      if (areaStr.toInt == areaId){
        res = true
      }
    }
    res
  }

  def getLogAreaIdByAreaKey(areaKey : String): Int ={
    val value = areaKeyMap.find( e => {
      if(e._2.equals(areaKey)){
        true
      }else{
        false
      }
    })
    val logicId = value.get._1
    val logAreaKey =  p.getProperty("logAreaKey" + logicId).toInt
    logAreaKey
  }

  def getLogicAreaIdByAreaKey(areaKey : String): Int ={
    import scala.collection.JavaConversions._
    var logicAreaId = -1
    for (id <- areaKeyMap.keySet) {
      if (areaKeyMap.get(id).equals(areaKey)){
        logicAreaId = id
      }
    }
    logicAreaId
  }

  def getOpenServerDateByAreaKey(areaKey : String): Date ={
    val logicAreaId = getLogicAreaIdByAreaKey(areaKey)
    if(openServerDateMap.get(logicAreaId)==null) {
      new Date()
    }else{
      openServerDateMap.get(logicAreaId)
    }
  }

  def getOpenServerDateByAreaId(areaId : Int): Date ={
    val logicAreaId = getLogicAreaIdByAreaId(areaId)
    if(openServerDateMap.get(logicAreaId)==null) {
      new Date()
    }else{
      openServerDateMap.get(logicAreaId)
    }
  }

  def stopServer(): Unit ={
//    println("服务器结束")
    //停服流程
    //1停止socket服务
    //2保存所有玩家数据
    //3数据全部保存完毕
    //4关闭程序

//
    CustomerLogger.error("！！！开始停服操作！！！！切记：不要强制关闭进程！")
    //停止socket、http服务
    CustomerLogger.error("第1步：停止socket、http服务")
    GameUtils.openServer = false
    stopGameNetServer()  //客户端的socket全部断开，触发gateServer的onCloseSession方法
    stopGameHttpServer()


    ////////////////////////////////////////////////////
    CustomerLogger.error("第2步：通知service保存！")
    Thread.sleep(2000)
    areaKeyMap.foreach( f => {
      val path = ActorDefine.AREA_SERVER_PRE_PATH+f._1+"/"+ActorDefine.ARMYGROUP_SERVICE_NAME
      system.actorSelection(path) ! saveDateBeforeStop()
    })
    areaKeyMap.foreach( f => {
      val path = ActorDefine.AREA_SERVER_PRE_PATH+f._1+"/"+ActorDefine.POWERRANKS_SERVICE_NAME
      system.actorSelection(path) ! saveDateBeforeStop()
    })
    areaKeyMap.foreach( f => {
      val path = ActorDefine.AREA_SERVER_PRE_PATH+f._1+"/"+ActorDefine.ARENA_SERVICE_NAME
      system.actorSelection(path) ! saveDateBeforeStop()
    })
    CustomerLogger.error("第2步：通知service保存完毕")
    ////////////////////////////////////////////////////

    CustomerLogger.error("第3步：关闭所有的野外世界节点！")
    WorldService.initRewardMap(areaKeyMap)
    Thread.sleep(2000)
    areaKeyMap.foreach( f => {
      val path = ActorDefine.AREA_SERVER_PRE_PATH+f._1+"/"+ActorDefine.WORLD_SERVICE_NAME
      system.actorSelection(path) ! StopAllNode()
    })
    Thread.sleep(2000)
    //检测节点是否已经全部关闭
    var worldNodeAllDone = true
    areaKeyMap.foreach( f => {
      val playerService = system.actorSelection(ActorDefine.AREA_SERVER_PRE_PATH+f._1+"/"+ActorDefine.WORLD_SERVICE_NAME)
      val size : Int = GameUtils.futureAsk(playerService, AllNodeTeamBack(), 20)
      if (size > 0){
        worldNodeAllDone = false
      }
    })
    var index = 1
    while (!worldNodeAllDone){
      worldNodeAllDone = true
      Thread.sleep(3000)
      CustomerLogger.error("第3步：第%d次检测关闭所有的野外世界节点开始执行".format(index))
      areaKeyMap.foreach( f => {
        val playerService = system.actorSelection(ActorDefine.AREA_SERVER_PRE_PATH+f._1+"/"+ActorDefine.WORLD_SERVICE_NAME)
        val size : Int = GameUtils.futureAsk(playerService, AllNodeTeamBack(), 20)
        if (size > 0){
          worldNodeAllDone = false
        }
        CustomerLogger.error("第3步：第%d次检测关闭所有的野外世界节点完毕".format(index))
      })
      index = index+1
    }
    CustomerLogger.error("第3步：野外奖励开始入库")
    WorldService.saveWorldCloseReward()
    CustomerLogger.error("第3步：关闭所有的野外世界节点完毕")
    index = 1
    ////////////////////////////////////////////////////
    //发送给GateServer通知，直接保存玩家 //onAutoClearOffPlayerData
    Thread.sleep(3000)
    CustomerLogger.error("第4步：通知GateServer直接保存所有玩家的数据")
    system.actorSelection(ActorDefine.GATE_SERVER_PATH) ! AutoClearOffPlayerData() //直接触发保存玩家数据了

    Thread.sleep(1000)
    CustomerLogger.error("第4步：通知GateServer直接保存所有玩家的数据完毕")
    ////////////////////////////////////////////////////
    //调用清除所有玩家的simplePlayer
    CustomerLogger.error("第5步：开始清除并保存所有玩家的缓存数据！")
    areaKeyMap.foreach( f => {
      val path = ActorDefine.AREA_SERVER_PRE_PATH+f._1+"/"+ActorDefine.PLAYER_SERVICE_NAME
      println(path)
      system.actorSelection(path) ! StopServerSaveSimplePlayer()
    })
    //检测simplePlayer是否已经保存完成
    var allDone : Boolean = true
    areaKeyMap.foreach( f => {
      val playerService = system.actorSelection(ActorDefine.AREA_SERVER_PRE_PATH+f._1+"/"+ActorDefine.PLAYER_SERVICE_NAME)
      val done : Boolean = GameUtils.futureAsk(playerService, CheckSaveSimplePlayerDone(), 20)
      if (done == false){
        allDone = false
      }
    })
    while (!allDone){
      allDone = true
      Thread.sleep(3000)
      CustomerLogger.error("第5步：第%d次检测清除并保存所有玩家的缓存数据开始执行".format(index))
      areaKeyMap.foreach( f => {
        val playerService = system.actorSelection(ActorDefine.AREA_SERVER_PRE_PATH+f._1+"/"+ActorDefine.PLAYER_SERVICE_NAME)
        val done : Boolean = GameUtils.futureAsk(playerService, CheckSaveSimplePlayerDone(), 20)
        if (done == false){
          allDone = false
        }
        CustomerLogger.error("第5步：第%d次检测清除并保存所有玩家的缓存数据完毕".format(index))
      })
    }
    CustomerLogger.error("第5步：清除并保存所有玩家的缓存数据完毕")
    index = 1
    //////////////////////////////////////////////////////
    //循环检测dbServer是否已经全部入库完毕了
    CustomerLogger.error("第6步：第1次检测redis是否全部入库完毕")

    val dbServer = system.actorSelection(ActorDefine.DB_SERVER_PATH)
    var count : Int = GameUtils.futureAsk(dbServer, IsDbQueueEmpty(), 20)
    while (count > 0){
      Thread.sleep(3000)
      count = GameUtils.futureAsk(dbServer, IsDbQueueEmpty(), 20)
      index = index + 1
      CustomerLogger.error("第6步：第%d次检测redis是否全部入库完毕:剩余数：%d".format(index, count))
    }

    CustomerLogger.error("第6步：redis全部入库完毕")

    //////////////////////////////////////////////////////
    CustomerLogger.error("第7步：第1次检测mysql是否全部入库完毕")
    index = 1
    //循环检测mysqlDb是否已经全部入库完毕了
    val mysqlServer = system.actorSelection(ActorDefine.DB_SERVER_PATH + "/" + ActorDefine.MYSQL_ACTOR_NAME)
    count  = GameUtils.futureAsk(mysqlServer, IsDbQueueEmpty(), 20)
    while (count > 0){
      Thread.sleep(3000)
      count = GameUtils.futureAsk(mysqlServer, IsDbQueueEmpty(), 20)
      index = index + 1
      CustomerLogger.error("第7步：第%d次检测mysql是否全部入库完毕:剩余数:%d".format(index, count))
    }
    CustomerLogger.error("第7步：mysql全部入库完毕")

    ////////////////////////////////////////////
    CustomerLogger.error("第8步：第1次检测日志是否全部入库完毕")
    var allEmpty = true
    index = 1
    while (!allEmpty){
      areaKeyMap.foreach( f => {
        val adminLogService = system.actorSelection(ActorDefine.AREA_SERVER_PRE_PATH+f._1 + "/" + ActorDefine.ADMIN_LOG_SERVICE_NAME)
        count = GameUtils.futureAsk(adminLogService, IsDbQueueEmpty(), 20)
        if(count >  0){
          allEmpty = false
        }
      })
      Thread.sleep(3000)
      CustomerLogger.error("第8步：第%d次检测日志是否全部入库完毕".format(index))
      index = index + 1
    }
    CustomerLogger.error("第8步：日志全部入库完毕")

    //////////////////////////////////////////////////////
    CustomerLogger.error("第9步：关闭system，关闭进程")
    system.shutdown()
    system.awaitTermination()
    CustomerLogger.error("服务器关闭结束")
    System.exit(0)

    //////////////////////////////////////////////////////////////

    system.registerOnTermination( new Runnable {
      override def run(): Unit = {
        println("registerOnTermination")
      }
    })

  }

  final val OPEN_SERVER_TIME = GameUtils.getServerTime()

  def starServer(): Unit ={
    ConfigDataProxy.loadAllConfig()
    //生成PlayerPowerDefine的名字库
    var map = GameUtils.getPowerMap(classOf[PlayerPowerDefine],"POWER_")
    map.foreach(f => {
      val powerName : String = f._1
      if (powerName.equals("NameMap") == false){
        val powerValue : Integer = f._2
        PlayerPowerDefine.NameMap += (powerValue  -> powerName)
      }

    })
    CustomerLogger.info("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&NameMap加载完毕&&&&&&&&&&&&&&")
    //生成扩展角色属性的名字库
    map = GameUtils.getPowerMap(classOf[PlayerPowerDefine],"NOR_POWER_")
    map.foreach(f => {
      val powerName : String = f._1
      if (powerName.equals("NameMap") == false && powerName.equals("TOTAL_FIGHT_POWER") == false){
        val powerValue : Integer = f._2
        PlayerPowerDefine.ExtendPowerMap += (powerValue  -> powerName)
      }
    })

    //生成SoldierDefine的名字库
    GameUtils.getPowerMap(classOf[SoldierDefine],"POWER_").foreach(f => {
      val powerName : String = f._1
      if (powerName.equals("NameMap") == false && powerName.equals("TOTAL_FIGHT_POWER") == false){
        val powerValue : Integer = f._2
        SoldierDefine.NameMap += (powerValue  -> powerName)
      }
    })
  }

  def reload(): Unit ={
    ConfigDataProxy.loadAllConfig()
    areaKeyMap.foreach( f => {
      val path = ActorDefine.AREA_SERVER_PRE_PATH+f._1+"/"+ActorDefine.PLAYER_SERVICE_NAME
      system.actorSelection(path) ! Reload()
    })
  }



  var br : BufferedReader = new BufferedReader(new InputStreamReader(System.in))
  while(true){
    println("输入命令:<start/stop/openServer(了解更多指令)>")
    try {
      var str : String = br.readLine().trim().toString
      if (str.equals("start")) {
        GameUtils.openServer = true
        println("服务器正在运行")
      }else if(str.startsWith("reload")){
        reload()
      }else if(str.equals("stop")){
        system.actorSelection(ActorDefine.ROOT_GAME_PATH) ! StopGame()
//        this.stopServer()
      }else if(str.equals("serverTime")){
        val dateStr = GameUtils.getServerDateStr()
        println(dateStr)
      }else if(str.startsWith("test")){
        val ary : Array[String] = str.split("=")
        val test = java.lang.Boolean.parseBoolean(ary(1))
        GameUtils.test = test
        println("当前的作弊指令是"+GameUtils.test)
      }else if(str.indexOf("setServerDate") >= 0){//setServerDate=2016-04-21 03:59:30
        val ary : Array[String] = str.split("=")
        GameUtils.setServerDate(ary(1))
        println("设置时间成功当前时间为："+GameUtils.getServerDate())
      }else if(str.equals("showTime")){
        println("设置时间成功当前时间为："+GameUtils.getServerDate())
      }else if(str.equals("properitesReload")){
        reloadGameProperties()
        println("重新读取服务器配置表完毕："+GameUtils.getServerDate())
      }else if(str.startsWith("hotReplace")){//hotReplace=com.znl.modules.friend=hotReplace/FriendModule.class
        //hotReplace=com.znl.proxy=hotReplace/OrdnanceProxy.class
        val ary : Array[String] = str.split("=")
        val path = ary(1)
        val classFile = System.getProperty("user.dir")+File.separator+ary(2)
        HotReplaceClass.hotReplaceClass(path,classFile)
      }else if(str.startsWith("loadCloseReward")){
        val ary : Array[String] = str.split("=")
        val areaKey = ary(1)
        WorldService.loadAllWorldRewardSetDb(areaKey)
      }

    }catch {
      case e: Exception => {
        e.printStackTrace()
      }
    }
  }


}
