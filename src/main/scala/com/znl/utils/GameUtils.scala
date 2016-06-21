package com.znl.utils

import java.io.{ObjectInputStream, ByteArrayInputStream, ObjectOutputStream, ByteArrayOutputStream}
import java.net.InetSocketAddress
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util
import java.util.{Date, Calendar, Random}

import akka.actor.{ActorLogging, ActorSelection, ActorContext, ActorRef}
import akka.event.LoggingAdapter
import com.znl.GameMainServer
import com.znl.base.{BaseSetDbPojo, BaseDbPojo}
import com.znl.core.{PlayerCache, PlayerTeam, PlayerTroop, SimplePlayer}
import com.znl.define.{PlayerPowerDefine, DataDefine, SoldierDefine, ActorDefine}
import com.znl.framework.http.{HttpRequestMessage, HttpMessage}
import com.znl.msg.GameMsg.{getTeamDate}
import com.znl.pojo.db.set.TeamDateSetDb
import com.znl.pojo.db.{Player}
import com.znl.proxy.{ConfigDataProxy, DbProxy}
import org.apache.mina.core.session.IoSession
import org.json.JSONObject
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask
import akka.serialization.SerializationExtension
import akka.util.{Timeout, ByteString}
import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._

/**
  * Created by woko on 2015/10/7.
  */
object GameUtils {

  var gameId = 0
  def getGameId(): Int ={
    gameId
  }
  def setGameId(gameId : Int): Unit ={
    this.gameId = gameId
  }

  var centerServerIp = ""
  def getCenterServerIp(): String ={
    centerServerIp
  }
  def setCenterServerIp(centerServerIp : String): Unit ={
    this.centerServerIp = centerServerIp
  }

  var openServer = true

  var serverType = 1
  def getServerType(): Int ={
    serverType
  }
  def setServerType(serverType : Int): Unit ={
    this.serverType = serverType
  }
  //TODO  java.util.ConcurrentModificationException
  def set2str[T](set: java.util.Set[T]) = {
    val sb = StringBuilder.newBuilder
    set.foreach(e => {
      sb.append(e + ",")
    })

    sb.toString()
  }

  //TODO 先只处理 set Long数据
  def str2set(str: String): java.util.Set[java.lang.Long] = {
    val set: java.util.Set[java.lang.Long] = new java.util.HashSet[java.lang.Long]()
    if (str.equals("")) {
      set
    } else {
      val ary = str.split(",")
      ary.foreach(e => {
        set.add(java.lang.Long.parseLong(e))
      })

      set
    }
  }

  def list2str[T](list: java.util.List[T]) = {
    val sb = StringBuilder.newBuilder
    list.foreach(e => {
      sb.append(e + ",")
    })
    sb.toString()
  }

  def str2list(str: String) = {
    val list = new java.util.ArrayList[java.lang.Integer]()
    if (str.equals("")) {
      list
    } else {
      val ary = str.split(",")
      ary.foreach(e => {
        list.add(java.lang.Integer.parseInt(e))
      })

      list
    }
  }

  def getPowerMap(cls: Class[_], starWord: String) = {
    var map: Map[String, Integer] = Map()

    cls.getFields.foreach(f => {
      if (f.getName.startsWith(starWord)) {
        val name: String = f.getName.replace(starWord, "")
        val value: Integer = f.get(null).asInstanceOf[Integer]
        map += (name -> value)
      }
    })

    map
  }

  def getDataDefine(cls: Class[_]) = {
    var map: Map[String, AnyRef] = Map()

    cls.getFields.foreach(f => {
      val name: String = f.getName
      val value: AnyRef = f.get(null).asInstanceOf[AnyRef]
      map += (name -> value)
    })

    map
  }

  //慎用！
  def futureAsk[T](actor: ActorSelection, msg: AnyRef, s: Int = 10) = {
    implicit val timeout = Timeout(30 seconds)
    val future = actor ? msg
    val result = Await.result(future, timeout.duration).asInstanceOf[T]
    result
  }

  var test = true

  var time = (System.currentTimeMillis / 1000).toInt

  def getServerTime():Int = {
    if (test){
      time
    }else{
      (System.currentTimeMillis / 1000).toInt
    }
  }

  def setServerTime(value: Int) = {
    time = value
  }

  def getServerDateStr() = {
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    var lt: Long = 0l
    if (test){
      lt = time.toLong * 1000
    }else{
      lt = System.currentTimeMillis
    }
    format.format(lt)
  }

  def getServerDate() = {
    if (test){
      new Date(time * 1000L)
    }else{
      new Date((getServerTime * 1000l))
    }
  }

  def getDateFromStryyyyMMdd(dateStr: String): Date ={
    val format = new SimpleDateFormat("yyyyMMdd")
    val date = format.parse(dateStr)
    date
  }

  def getStryyyyMMddFromDate(date: Date) : String ={
    val format = new SimpleDateFormat("yyyyMMdd")
    format.format(date)
  }

  def getDateFromStr(dateStr: String): Date ={
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val date = format.parse(dateStr)
    date
  }

  def setServerDate(dateStr: String) = {
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val date = format.parse(dateStr)
    time = (date.getTime / 1000).toInt
    Calendar.getInstance().setTimeInMillis(date.getTime)
  }

  val random = new SecureRandom()

  //Random() //
  def getRandomValueByRange(bound: Int): Int = {
    random.nextInt(bound)
  }

  def getRandomValueByInterval(min: Int, max: Int): Int = {
    random.nextInt(max) % (max - min + 1) + min
  }

  def getCallStatckString() = {
    val strBuilding = StringBuilder.newBuilder
    val ex = new Throwable();
    val stackElements = ex.getStackTrace
    if (stackElements != null) {
      stackElements.foreach(ele => {
        strBuilding.++=(ele.getClassName + "\n")
        strBuilding.++=(ele.getFileName + "\n")
        strBuilding.++=(ele.getLineNumber + "\n")
        strBuilding.++=(ele.getMethodName + "\n")
        strBuilding.++=("-----------------------------------\n")
      })
    }
    strBuilding.toString()
  }

  def stackTrackLog(log : LoggingAdapter, cause : Throwable) ={
    val stackElements = cause.getStackTrace
    if (stackElements != null) {
      val strBuilding = StringBuilder.newBuilder
      strBuilding.++(cause.getMessage + "\n")
      stackElements.foreach(ele => {
        strBuilding.++=("\t" + ele.getClassName + ".")
        strBuilding.++=(ele.getMethodName + "")
        strBuilding.++=( "(" + ele.getFileName + ":")
        strBuilding.++=(ele.getLineNumber + ")\n")
      })
      val errorStr = strBuilding.toString()
      log.error(errorStr)
    }
  }

  def getClassName(obj: AnyRef): String = {
    return obj.getClass.getName.replace(obj.getClass.getPackage.getName + ".", "")
  }

  def getClassName(pojoClass: Class[_]): String = {
    return pojoClass.getName.replace(pojoClass.getPackage.getName + ".", "")
  }

  val refSimplePlayerMap : util.HashSet[Integer] = new util.HashSet[Integer]
  initRefSimplePlayerMap
  def initRefSimplePlayerMap(): Unit ={
    refSimplePlayerMap.add(PlayerPowerDefine.POWER_boom)
    refSimplePlayerMap.add(PlayerPowerDefine.POWER_level)
    refSimplePlayerMap.add(PlayerPowerDefine.POWER_icon)
    refSimplePlayerMap.add(PlayerPowerDefine.POWER_vipLevel)
    refSimplePlayerMap.add(PlayerPowerDefine.POWER_boomUpLimit)
    refSimplePlayerMap.add(PlayerPowerDefine.POWER_tael)
    refSimplePlayerMap.add(PlayerPowerDefine.POWER_iron)
    refSimplePlayerMap.add(PlayerPowerDefine.POWER_wood)
    refSimplePlayerMap.add(PlayerPowerDefine.POWER_stones)
    refSimplePlayerMap.add(PlayerPowerDefine.POWER_food)
    refSimplePlayerMap.add(PlayerPowerDefine.POWER_atklv)
    refSimplePlayerMap.add(PlayerPowerDefine.POWER_critlv)
    refSimplePlayerMap.add(PlayerPowerDefine.POWER_dogelv)
    refSimplePlayerMap.add(PlayerPowerDefine.NOR_POWER_highestCapacity)
    refSimplePlayerMap.add(PlayerPowerDefine.NOR_POWER_depotprotect)
    refSimplePlayerMap.add(PlayerPowerDefine.NOR_POWER_facade)
    refSimplePlayerMap.add(PlayerPowerDefine.NOR_POWER_protect_date)
  }

  def countFirstHandValue(teams : util.List[PlayerTeam]) : Int ={
    import scala.collection.JavaConversions._
    var value = 0
    for (team : PlayerTeam <- teams) {
      value = value + team.basePowerMap.get(SoldierDefine.POWER_initiative).asInstanceOf[Int]
    }
    value
  }

  //玩家数据转成SimplePlayer数据
  def player2SimplePlayer(player: Player, simplePlayer : SimplePlayer): SimplePlayer = {
//
    simplePlayer.setId(player.getId)
    simplePlayer.setName(player.getName)
    simplePlayer.setAccountName(player.getAccountName)
    simplePlayer.setBoom(player.getBoom.intValue())
    simplePlayer.setBoomUpLimit(player.getBoomUpLimit.intValue())
    simplePlayer.setCapacity(player.getCapacity)
    simplePlayer.setIconId(player.getIcon)
    simplePlayer.setMilitaryRank(player.getMilitaryRank)
    simplePlayer.setCreateTime(GameUtils.getServerTime)
    simplePlayer.setLevel(player.getLevel)
    simplePlayer.setProtectNum(player.getDepotprotect)
    simplePlayer.setTael(player.getTael)
    simplePlayer.setIron(player.getIron)
    simplePlayer.setStones(player.getStones)
    simplePlayer.setFood(player.getFood)
    simplePlayer.setWood(player.getWood)
    simplePlayer.setArmygrouid(player.getArmygroupId)
    simplePlayer.setPost(player.getPost)
//    simplePlayer.setAreaKey(GameMainServer.getLogicAreaIdByAreaId(player.getAreaId))
    simplePlayer.setAtklv(player.getAtklv)
    simplePlayer.setCritlv(player.getCritlv)
    simplePlayer.setDogelv(player.getDogelv)
    simplePlayer.setX(player.getWorldTileX)
    simplePlayer.setY(player.getWorldTileY)
    simplePlayer.setBoomLevel(player.getBoomLevel)
    simplePlayer.setLoginOut(player.getLoginOutTime)
    simplePlayer.setLogintime(player.getLoginTime.toLong)
    simplePlayer.setProtectOverDate(player.getProtectOverDate)
    simplePlayer.setLegionName(player.getLegionName)
    simplePlayer.setGetLimitChangeId(player.getGetLimitChangeId)
    simplePlayer.setBoomState(player.getBoomState)
    simplePlayer.setBoomRefTime(player.getBoomRefTime)
    simplePlayer.setFaceIcon(player.getFaceIcon)
    simplePlayer.setFacadeendTime(player.getFacadeendTime)
    simplePlayer.setPlatform(player.getPlatName)
    simplePlayer.setHelpId(player.getUsedefine)
    simplePlayer.setAutobuild(player.getAutoBuild)
    simplePlayer.setAutobuildendtime(player.getAutoBuildendTime)
    simplePlayer.setRemianset(player.getRemianset)
    simplePlayer.setGardNum(player.getGardNum)
    simplePlayer.setEnery(player.getEnergy)
    simplePlayer.setPendant(player.getPendant)
    simplePlayer.setVipLevel(player.getVipLevel)
    simplePlayer.setAppArmylist(player.getApplyArmylist)
    try {
      simplePlayer.setAreaKey(GameMainServer.getAreaKeyByAreaId(player.getAreaId))
    } catch {
      case e: Exception => {
        System.err.println(e)
      }
    }
    simplePlayer.setSettingAutoAddDefendlist(player.getSettingAutoAddDefendlist)
    if (simplePlayer.getArenaTroop == null){
      val arena: Object = BaseSetDbPojo.getSetDbPojo(classOf[TeamDateSetDb], simplePlayer.getAreaKey).getTeamData(player.getId, SoldierDefine.FORMATION_ARENA)
//      val arena: Object =  DbProxy.ask(getTeamDate(simplePlayer.getAreaKey , player.getId, SoldierDefine.FORMATION_ARENA), 100)
      if (arena != None) {
        val troop: PlayerTroop = arena.asInstanceOf[PlayerTroop]
        simplePlayer.setArenaTroop(troop)
      }
    }
    if (simplePlayer.getDefendTroop == null){
      val defend: Object = BaseSetDbPojo.getSetDbPojo(classOf[TeamDateSetDb], simplePlayer.getAreaKey).getTeamData(player.getId, SoldierDefine.FORMATION_DEFEND)
//      val defend: Object = DbProxy.ask(getTeamDate(simplePlayer.getAreaKey, player.getId, SoldierDefine.FORMATION_DEFEND), 100)
      if (defend != None && defend != null) {
        val troop: PlayerTroop = defend.asInstanceOf[PlayerTroop]
        simplePlayer.setDefendTroop(troop)
      }else{
        val troop: PlayerTroop = new PlayerTroop
        simplePlayer.setDefendTroop(troop)
      }
    }
    simplePlayer
  }

  def getBoomConfig(boom: Int): JSONObject = {
    var boomConfig: JSONObject = null
    val list: util.List[JSONObject] = ConfigDataProxy.getConfigAllInfo(DataDefine.BOOMLEVEL)
    import scala.collection.JavaConversions._
    for (define <- list) {
      if (boom >= define.getInt("numneed") && boom <= define.getInt("nummax")) {
        boomConfig = define
      }
    }
    return boomConfig
  }

  def getJsonFromMap(map: util.Map[java.lang.String, Object]) = {
    val json = new JSONObject(map)
    json.toString
  }

  //获取管理平台相关操作的json信息
  def getAdminStatusJsonMsg(ret: Int, msg: String) = {
    val json = new JSONObject()
    json.put("ret", ret.toString)
    json.put("msg", msg)
    json.toString
  }

  //给定的参数都不能为null
  def checkParameter(requestMessage: HttpRequestMessage, parameterList: util.List[java.lang.String]) = {
    val nullList = new util.ArrayList[String]()
    parameterList.foreach(parameter => {
      val value: String = requestMessage.getParameter(parameter)
      if (value == null) {
        nullList.add(parameter)
      } else if (value.length == 0) {
        nullList.add(parameter)
      }
    })
    nullList
  }

  def getIpByIoSession(session : IoSession) ={
    val obj = session.getRemoteAddress()
    if(obj == null ){
      "unknown"
    }else{
      val clientIP = session.getRemoteAddress().asInstanceOf[InetSocketAddress].getAddress().getHostAddress()
      if(clientIP == null){
        "unknown"
      }else{
        clientIP
      }
    }
  }

  //---------------------------------

  val preKey = "Game_"

  def getPojoKey(pojoClass: Class[_]): String = {
    val packageName = pojoClass.getPackage.getName
    val fullName = pojoClass.getName
    val key = fullName.replace(packageName, "")
    preKey + key
  }

  def serialization(context: ActorContext, projo: AnyRef): String = {
    val serialization = SerializationExtension(context.system)
    val serializer = serialization.findSerializerFor(projo)
    val bytes = serializer.toBinary(projo)
    byte2hex(bytes)
  }

  def unserialization(context: ActorContext, pojoClass: Class[_], byteString: String) = {
    val serialization = SerializationExtension(context.system)
    val serializer = serialization.findSerializerFor(pojoClass)
    val bytes = hex2byte(byteString)
    serializer.fromBinary(bytes)
  }

  def byte2hex(bytes: Array[Byte]) = {
    var hs: String = ""
    var stmp: String = ""
    bytes.foreach(byte => {
      stmp = Integer.toHexString(byte & 0xFF)
      if (stmp.length() == 1) {
        hs += "0" + stmp
      } else {
        hs += stmp
      }
    }
    )
    hs
  }

  def hex2byte(str: String): Array[Byte] = {
    if (str == null)
      null

    val strValue = str.trim()
    val len = strValue.length()
    if (len == 0 || len % 2 == 1)
      null

    val bytes = new ArrayBuffer[Byte](len / 2)
    for (i <- 0 until len if i % 2 == 0) {
      val index = i / 2
      val value = Integer.decode("0x" + str.substring(i, i + 2)).intValue().toByte
      bytes += value
    }
    bytes.toArray
  }

  def objectToBytes(obj: AnyRef): Array[Byte] = {
    var bytes: Array[Byte] = new Array[Byte](1024)
    try {
      val bo: ByteArrayOutputStream = new ByteArrayOutputStream
      val oo: ObjectOutputStream = new ObjectOutputStream(bo)
      oo.writeObject(obj)
      bytes = bo.toByteArray
      bo.close
      oo.close
    }
    catch {
      case e: Exception => {
        System.out.println("translation" + e.getMessage)
        e.printStackTrace
      }
    }
    return (bytes)
  }

  def ByteToObject(bytes: Array[Byte]): AnyRef = {
    var obj: AnyRef = new AnyRef
    try {
      val bi: ByteArrayInputStream = new ByteArrayInputStream(bytes)
      val oi: ObjectInputStream = new ObjectInputStream(bi)
      obj = oi.readObject
      bi.close
      oi.close
    }
    catch {
      case e: Exception => {
        System.out.println("translation" + e.getMessage)
        e.printStackTrace
      }
    }
    return obj
  }

  def getCityIcon(value : Int):Int ={
    val jSONObject : JSONObject=ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.BOOMLEVEL,"boomlv",value)
    return  jSONObject.getInt("BaseLook")
  }

  def getPlayercace(player :Player): PlayerCache={
    var cache : PlayerCache=new PlayerCache()
    cache.setAccount(player.getAccountName)
    cache.setAreId(player.getAreaId)
    cache.setUtma("offline")
    cache.setImei("offline")
    cache.setScreen("offline")
    cache.setOs(1)
    cache.setOsName("offline")
    cache.setModel("offline")
    cache.setPlat_id(0)
    cache.setPlat_name(player.getPlatName)
    return cache
  }


  val slowWarnMap : util.HashMap[Integer,Integer] = new util.HashMap[Integer,Integer]()
  def addSlowWarnMap(cmd : Integer): Unit ={
    if(test){
      if(slowWarnMap.containsKey(cmd)){
        slowWarnMap.put(cmd,slowWarnMap.get(cmd)+1)
      }else{
        slowWarnMap.put(cmd,1)
      }
    }
  }

  /**
   * 功能描述：返回分
   *
     * 日期
   * @return 返回分钟
   */
  def getNowMinute(): Int = {
    val calendar = Calendar.getInstance
    calendar.setTime(getServerDate())
    return calendar.get(Calendar.MINUTE)
  }

  def printAllSlowMap(): Unit ={

  }

}
