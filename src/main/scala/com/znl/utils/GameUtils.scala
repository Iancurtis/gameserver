package com.znl.utils

import java.io.{ObjectInputStream, ByteArrayInputStream, ObjectOutputStream, ByteArrayOutputStream}
import java.net.InetSocketAddress
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util
import java.util.{UUID, Date, Calendar, Random}

import akka.actor.{ActorLogging, ActorSelection, ActorContext, ActorRef}
import akka.event.LoggingAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.znl.GameMainServer
import com.znl.base.{BaseSetDbPojo, BaseDbPojo}
import com.znl.core._
import com.znl.define._
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


  /**
   * mysql数据库
   */
  var mysqlDbName=""

  def getMysqlDbName:String={
    mysqlDbName
  }

  /**
   * reids玩家数据过期时间
   */
  var redisExpireTime=0

  def getRedisExpireTime:Int={
    redisExpireTime
  }
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
    val newSet = new util.HashSet[T](set)//创建一个镜像避免出现遍历过程中被修改的异常
    val sb = StringBuilder.newBuilder
    newSet.foreach(e => {
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
    val temp = new util.ArrayList[T](list)
    val sb = StringBuilder.newBuilder
    temp.foreach(e => {
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

  def setTest(value:Boolean): Unit ={
    test=value
  }
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
    var boomConfigtemp: JSONObject = null
    for (define <- list) {
      if (boom >= define.getInt("numneed") && boom <= define.getInt("nummax")) {
        boomConfig = define
      }
      boomConfigtemp=define
    }
    if(boomConfig==null){
      return boomConfigtemp
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

  /**
   * 检查到期时间是否正确
   *
   * 时间戳（秒）
   * @return 是否可以
   */
  def checkTime(time : Int): Boolean ={
    if (getServerTime() <= TimerDefine.TOLERNACE_TIME +time ){
      true
    }else{
      false
    }
  }

  /**
   * 获取两个时间差的天数
   * @param small 小一点的时间
   * @param big 大一点的时间
   * @return 如果small比big要打 返回0
   */
  def getDaysBetween(small: Int, big: Int): Int = {
    if (small >= big || small == 0 || big == 0) {
      return 0
    }
    val add: Int = (big - small) % (24 * 60 * 60)
    val a: Int = (big - small) / (24 * 60 * 60) + (if (add > 0) 1 else 0)
    return a
  }


  /**
   * 是否已做重置操作
   * @param resetTime 上一次重置时间
   * @param checkTime 要检测的时间,例如凌晨：0 ；4点：4
   * @return true 否 false 是
   */
  def hasNotResetDataHandler(resetTime:Int,checkTime:Int):Boolean={
    if(resetTime<=0)return true;
    val betweenHour: Int = (getServerTime() - resetTime) / ( 60 * 60)
    return betweenHour>=24
  }


  /**
   * 获取今天指定的的时间点
   * @param time 要检测的时间,例如凌晨：0 ；4点：4
   * @return 零时以long类型返回的数值
   *
   */
  def getTodayTimeForHourInt(time:Int): Int = {
    val calendar: Calendar = Calendar.getInstance()
    calendar.setTime(GameUtils.getServerDate())
    calendar.set(Calendar.HOUR_OF_DAY,time)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return (calendar.getTimeInMillis / 1000).asInstanceOf[Int]
  }

  val LONG_MAX_VALUE: Long = Math.pow(2, 63).toLong

  /** 生成一个唯一id **/
  def createOnlyRanId: Long = {
    val uuid: UUID = UUID.randomUUID
    uuid.getLeastSignificantBits + LONG_MAX_VALUE
  }

  //将对象转化为JSON格式的字符串
  def objectToJsonStr(obj: Object): String ={
    val gson :Gson = new Gson
    gson.toJson(obj)
  }

  //将JSON格式的字符串转换为对象
  def jsonStrToObject[T](str:String,trunClass:Class[T]): T ={
    val gson :Gson = new Gson
    gson.fromJson(str,new TypeToken[T](){}.getType())
  }

  def encodePlayerTeam(list: util.List[PlayerTeam],encodeType:Int): String ={
    val sb =  new StringBuffer()

    for (pt <- list ){
      var powerMap = pt.basePowerMap
      if(encodeType > 0){
        powerMap = pt.powerMap
      }
      sb.append(encodePowerMapToString(powerMap))
      sb.append("&&&")
    }
    sb.toString
  }

  def decodePlayerTeam(baseStr:java.lang.String,powerStr:java.lang.String,playerId : Long): util.List[PlayerTeam] ={
    val list: util.List[PlayerTeam] = new util.ArrayList[PlayerTeam]()
    val basicIndexMap  = new util.HashMap[Integer,util.HashMap[Integer, AnyRef]]()//[index,basicPowerMap]
    val powerIndexMap  = new util.HashMap[Integer,util.HashMap[Integer, AnyRef]]()//[index,powerMap]
    for (teamStr <- baseStr.split("&&&")){
      val  basePowerMap : util.HashMap[Integer, AnyRef]  = new util.HashMap[Integer, AnyRef]
      pushStrToMap(teamStr,basePowerMap)
      val index = basePowerMap.get(SoldierDefine.NOR_POWER_INDEX).asInstanceOf[Integer]
      basicIndexMap.put(index,basePowerMap)
    }

    for (teamStr <- powerStr.split("&&&")){
      val  powerMap : util.HashMap[Integer, AnyRef]  = new util.HashMap[Integer, AnyRef]
      pushStrToMap(teamStr,powerMap)
      val index = powerMap.get(SoldierDefine.NOR_POWER_INDEX).asInstanceOf[Integer]
      powerIndexMap.put(index,powerMap)
    }

    for (key <- basicIndexMap.keySet()){
      val basePowerMap = basicIndexMap.get(key)
      val pt = new PlayerTeam(basePowerMap,playerId)
      pt.powerMap = powerIndexMap.get(key)
      list.add(pt)
    }
    list
  }

  def pushStrToMap(teamStr:java.lang.String,map : util.HashMap[Integer, AnyRef]): Unit ={
    for (powerStr <- teamStr.split(",")){
      val tempAtt = powerStr.split("=")
      val power = Integer.parseInt(tempAtt(0))
      if(tempAtt.length > 1){
        val value = powerStr.split("=")(1)
        if (power == SoldierDefine.NOR_POWER_NAME){
          map.put(power,value)
        }else if(value.indexOf("_") >= 0){
          val list = new util.ArrayList[java.lang.Integer]()
          for (attStr <- value.split("_")){
            val att : Integer = Integer.parseInt(attStr)
            list.add(att)
          }
          map.put(power,list)
        }else{
          val valueInt:Integer = Integer.parseInt(value)
          map.put(power,valueInt)
        }
      }else{
        val list = new util.ArrayList[java.lang.Integer]()
        map.put(power,list)
      }
    }
  }

  def encodePowerMapToString(powerMap : util.HashMap[Integer, AnyRef]): String ={
    val sb =  new StringBuffer()
    for (key : Integer <- powerMap.keySet()){
      val value = powerMap.get(key)
      if (value.isInstanceOf[Integer]
        //          || value.isInstanceOf[java.lang.Long]
        || value.isInstanceOf[java.lang.String]){
        sb.append(key)
        sb.append("=")
        sb.append(value)
        sb.append(",")
      }else if(value.isInstanceOf[util.List[Integer]]){
        val list: util.List[Integer] = value.asInstanceOf[util.List[Integer]]
        sb.append(key)
        sb.append("=")
        for (id <- list){
          sb.append(id)
          sb.append("_")
        }
        sb.append(",")
      }else{
        println("！！！！！！！！解析的时候出现未知的类型"+key)
      }
    }
    sb.toString
  }


  def encodeIntegerMapToString(powerMap : util.HashMap[Integer, Integer]): String ={
    val sb =  new StringBuffer()
    for (key : Integer <- powerMap.keySet()){
      sb.append(key)
      sb.append("=")
      val value = powerMap.get(key)
      sb.append(value)
      sb.append(",")
    }
    sb.toString
  }

  def decodeStringToIntegerMap(teamStr:String): util.HashMap[Integer,Integer] ={
    val map = new util.HashMap[Integer,Integer]()
    if(teamStr.length <= 0){
      return map
    }
    for (powerStr <- teamStr.split(",")){
      val tempAtt = powerStr.split("=")
      val power = Integer.parseInt(tempAtt(0))
      val value = Integer.parseInt(tempAtt(1))
      map.put(power,value)
    }
    map
  }

  def isNumeric(str: String): Boolean = {
    var i: Int = 0
    while (i < str.length) {
      System.out.println(str.charAt(i))
      if (!Character.isDigit(str.charAt(i))) {
        return false
      }
      i += 1
    }
    return true
  }



}
