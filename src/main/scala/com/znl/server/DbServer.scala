package com.znl.server

import java.util
import java.util.{Properties, Set}
import java.util.concurrent.ConcurrentHashMap

import akka.actor.SupervisorStrategy.Resume
import akka.actor._
import com.google.protobuf.GeneratedMessage
import com.znl.GameMainServer
import com.znl.base.{BaseSetDbPojo, BaseDbPojo}
import com.znl.core.{PlayerTroop, PlayerTeam, ArenaRank}
import com.znl.define.ActorDefine
import com.znl.msg.GameMsg._
import com.znl.pojo.db.Player
import com.znl.server.action.{DbActionType, DbAction}
import com.znl.service.actor.MySqlActor
import com.znl.utils.{ZipUtils, GameUtils}
import org.json.JSONObject
import redis.clients.jedis._

import scala.collection.JavaConversions._
import scala.concurrent.duration._

object DbServer {

  val jedisClusterNodes: util.Set[HostAndPort] = new util.HashSet[HostAndPort]

  var jc: JedisCluster = null

  def props(redisIp: String, redisPort: Int, mysql_ip: String, mysql_db: String, mysql_user: String, mysql_pwd: String,  p:Properties) ={
    jedisClusterNodes.add(new HostAndPort(redisIp, redisPort))
    jc = new JedisCluster(jedisClusterNodes)
    Props(classOf[DbServer], redisIp, redisPort, mysql_ip, mysql_db, mysql_user, mysql_pwd,p)
  }

  def createDbPojo(pojoClass: Class[_],areaId : Int) = {
    val time = System.currentTimeMillis()

    val pojo: Option[BaseDbPojo] = Some(pojoClass.newInstance().asInstanceOf[BaseDbPojo])
    val key = pojo.get.getGroupIdKey
    var id = 0L
    try {
      id = jc.incr(key)

      //      val setKey = pojo.getGroupIdSetKey
      //      jc.sadd(setKey, id.toString)  //创建的KEY保存在set里面
      pojo.get.setId(id)
//      pojo.get.setLogAreaId(areaId)

    } finally {

    }

    val mysqlActor = getMysqlActor
//    println("！！！！！创建数据的时候就不发到mysqlActor了")
//    mysqlActor ! InsertToMysql(pojo.get.getClassName, id, areaId)
    pojo

  }

  val dataMap: util.Map[String, Option[BaseDbPojo]] = new java.util.concurrent.ConcurrentHashMap[String, Option[BaseDbPojo]]
  val offlineDataMap: util.Map[String, Option[BaseDbPojo]] = new java.util.concurrent.ConcurrentHashMap[String, Option[BaseDbPojo]]
  private[this] val saveDataMap: util.Map[String, Option[BaseDbPojo]] = new java.util.concurrent.ConcurrentHashMap[String, Option[BaseDbPojo]]

  def pushIntoSaveMap(pojo :BaseDbPojo): Unit ={
    val dataKey = getDataKey(pojo.getId, GameUtils.getClassName(pojo))
    saveDataMap.put(dataKey, Some(pojo))
  }

  def getDbPojo(id: Long, pojoClass: Class[_],pushDataMap : Boolean) = {

    val time = System.currentTimeMillis()
    var res : Option[BaseDbPojo] = null
    val dataKey = getDataKey(id, GameUtils.getClassName(pojoClass))
    //先读内存缓存
    if (dataMap.contains(dataKey)) {
      res = dataMap.get(dataKey) //需要做下线释放掉对应的数据
    }else if(saveDataMap.containsKey(dataKey)) {
      res = saveDataMap.get(dataKey)
    }else {
      //再读redis
      var pojo: Option[BaseDbPojo] = Some(pojoClass.newInstance().asInstanceOf[BaseDbPojo])
      pojo.get.setId(id)
      val key = pojo.get.getKey
      val map = jc.hgetAll(key) //new ConcurrentHashMap[String, String]() //
      if (map.size() == 0) {
        pojo = None
      } else {
        map.foreach(e => {
          val key = e._1
          val value = e._2
          pojo.get.setter(key, value)
        })
        dataMap.put(dataKey, pojo)
        if(pushDataMap == false){
//          offlineDataMap.put(dataKey, pojo)
        }
      }

      //      log.info("getDbPojo耗时xx:" + dataKey + ":" + ( System.currentTimeMillis() - time ) )
      //可能再读mysql
      //      if(map.size() == 0){
      //        val sql = "select data from %s where id = %d".format(GameUtils.getClassName(pojoClass), id)
      //        System.out.println(sql)
      //        val actor  = context.actorSelection( ActorDefine.MYSQL_ACTOR_NAME )
      //        val resultList :  util.ArrayList[String] = GameUtils.futureAsk(actor, QueryToMysql(sql), 30)
      //        if(resultList.length > 0){
      //          val result = resultList.get(0)
      //
      //          val jsonObject = new JSONObject(result)
      //          jsonObject.keySet().foreach( key => {
      //            val value = java.net.URLDecoder.decode(jsonObject.get(key).toString, "utf-8")
      //            pojo.get.setter(key, value)
      //          })
      //
      //          dataMap.put(dataKey, pojo)
      //        }else{
      //          pojo = None
      //        }
      //      }

      res = pojo
    }
    res
  }

  def clearOffLineDataMap(): Unit ={
    offlineDataMap.clear()
  }

  def removeSaveDataMap(dataKey :String): Unit ={
    saveDataMap.remove(dataKey)
  }

  //释放掉对应的db内存缓存
  def finalizeDbPojo(pojo: BaseDbPojo) = {
    val id = pojo.getId
    val dataKey = getDataKey(id, GameUtils.getClassName(pojo))
    if (dataMap.contains(dataKey)) {
      //      log.info("==========db缓存释放掉=======")
      dataMap.remove(dataKey)
    } else {
      //      log.warning("!!!警告！！这个数据没有被缓存起来！！")
    }
  }

  //这里只做Map的删除，具体还是通知Actor删除
  def delDbPojo(pojo: BaseDbPojo) = {
    val dataKey = getDataKey(pojo.getId, GameUtils.getClassName(pojo))
    //    jc.del(key)  //TODO 需要写一级日志

    dataMap.remove(dataKey)
  }


  def getDataKey(id: Long, pojoName: String) = {
    id.toString + pojoName
  }

  def getMysqlActor ={
    val system: ActorSystem = GameMainServer.system
    val mysqlActor: ActorSelection = system.actorSelection(ActorDefine.DB_SERVER_PATH + "/" + ActorDefine.MYSQL_ACTOR_NAME)
    mysqlActor
  }

  //全局保存一些协议包内容 这些数据默认只保存一个月
  def createProtoGeneratedMessage(cmd: Int, msg: GeneratedMessage, expire: Int = 30 * 24 * 60) = {
    val sourceBytes: Array[Byte] = msg.toByteArray
    val sourceStr: String = com.znl.framework.socket.websocket.Base64.encodeBytes(sourceBytes)
    val zipStr: String = ZipUtils.gzip(sourceStr)

    val id = jc.incr(ActorDefine.DB_PROTO_INC_ID_KEY)

    val key = ActorDefine.DB_PROTO_NAME_FORMAT.format(cmd, id)
    jc.set(key, zipStr)

    jc.expire(key, expire)

    id
  }


  def onInitSetDbPojo(setDbPojo: BaseSetDbPojo) ={
    val db_set_key = getSetDbPojoDbKey(setDbPojo)
    val set: util.Set[Tuple] = jc.zrangeWithScores(db_set_key, 0, 40000000000L)
    set.foreach(f => {
      setDbPojo.addKeyValue(f.getElement, f.getScore.toLong, true)
    })
    true
  }


  def getSetDbPojoDbKey(setDbPojo: BaseSetDbPojo) ={
    val areaKey = setDbPojo.getAreaKey
    val className = GameUtils.getClassName(setDbPojo)
    val db_set_key = "gset:[%s]:[%s]".format(className, areaKey)
    db_set_key
  }

}

/**
  * 数据库服务
  * 用来提供持久化的数据
  * 数据落地策略
  * Created by woko on 2015/10/7.
  */
class DbServer(redisIp: String, redisPort: Int, mysql_ip: String, mysql_db: String, mysql_user: String, mysql_pwd: String, p:Properties) extends Actor with ActorLogging {

  override val supervisorStrategy = OneForOneStrategy() {
    case e: Exception => {
      e.printStackTrace()
      log.error(e.fillInStackTrace(), e.getMessage)
      Resume
    }
    case _ => Resume
  }

  val jedisClusterNodes: util.Set[HostAndPort] = new util.HashSet[HostAndPort]
  jedisClusterNodes.add(new HostAndPort(redisIp, redisPort))
  val jc: JedisCluster = new JedisCluster(jedisClusterNodes)

//  val config: JedisPoolConfig = new JedisPoolConfig
//  config.setMaxIdle(200)
//  config.setMaxWaitMillis(10000)
//  config.setTestOnBorrow(true)
//  val jedisPool: JedisPool = new JedisPool(config, "192.168.198.133", 15600, 10000, "123456")
//  val jc =  jedisPool.getResource


  val mysqlActor = context.actorOf(MySqlActor.props(mysql_ip, mysql_db, mysql_user, mysql_pwd,p), ActorDefine.MYSQL_ACTOR_NAME)
  context.watch(mysqlActor)

  override def preStart() = {
    log.info("dbService start")

    //    val isValid = ActorPath.isValidPathElement("dbService")
    //    println(isValid)
  }

  import context.dispatcher

  context.system.scheduler.schedule(1 seconds, 1 seconds, context.self, TriggerDBAction())
  context.system.scheduler.schedule(10 seconds, 10 seconds, context.self, TriggerClearOfflineData())
  override def receive: Receive = {
    case SaveDBPojo(pojo) =>
      saveDbPojo(pojo)
    case DelDBPojo(pojo) =>
      delDbPojo(pojo)
    case TriggerDBAction() =>
      onTriggerDBAction()
    case FinalizeDbPojo(pojo: BaseDbPojo) =>
      finalizeDbPojo(pojo)
    case CreateDBPojo(pojoClass) =>
      val pojo = createDbPojo(pojoClass)
      sender().tell(pojo, self)
    case GetDBPojo(id, pojoClass) =>
      val pojo = getDbPojo(id, pojoClass)
      sender().tell(pojo, self)
    case IsDbQueueEmpty() =>
      sender() ! isDbQueueEmpty()
//    case  InitSetDbPojo(setDbPojo: BaseSetDbPojo) =>
//      sender() ! onInitSetDbPojo(setDbPojo)
    case UpdateSetDbPojoElement(setDbPojo: BaseSetDbPojo, key : String, value : Long) =>
      onUpdateSetDbPojoElement(setDbPojo, key, value)
    case DelSetDbPojoElement(setDbPojo: BaseSetDbPojo, key : String) =>
      onDelSetDbPojoElement(setDbPojo, key)
    case CreateProtoGeneratedMessage(cmd : Int, msg : GeneratedMessage, expire : Int) =>
      sender() ! createProtoGeneratedMessage(cmd,msg,expire)
    case GetProtoGeneratedMessage(cmd : Int, id : Long) =>
      sender() ! getProtoGeneratedMessage(cmd,id)
    case TriggerClearOfflineData() =>
      DbServer.clearOffLineDataMap()
    case _ =>
      log.warning("========un handle====event========")
  }

  //保存拿取数据的缓存，用来确保不会多次去DB拿数据
  val dataMap: util.Map[String, Option[BaseDbPojo]] = new java.util.concurrent.ConcurrentHashMap[String, Option[BaseDbPojo]]

  //先保存即将要入库的数据，入库完毕后再删除，确保未入库时就再去DB拿数据
//  val saveDataMap: util.Map[String, Option[BaseDbPojo]] = new java.util.concurrent.ConcurrentHashMap[String, Option[BaseDbPojo]]

  def getDataKey(id: Long, pojoName: String) = {
    id.toString + pojoName
  }

  //释放掉对应的db内存缓存
  def finalizeDbPojo(pojo: BaseDbPojo) = {
    val id = pojo.getId
    val dataKey = getDataKey(id, GameUtils.getClassName(pojo))
    if (dataMap.contains(dataKey)) {
      //      log.info("==========db缓存释放掉=======")
      dataMap.remove(dataKey)
    } else {
      //      log.warning("!!!警告！！这个数据没有被缓存起来！！")
    }

//    if (saveFieldMap.containsKey(dataKey)) {
//      DbServer.removeSaveDataMap(dataKey)
//      saveFieldMap.remove(dataKey)
//    }
  }

  def getDbPojo(id: Long, pojoClass: Class[_]) = {

    val time = System.currentTimeMillis()

    val dataKey = getDataKey(id, GameUtils.getClassName(pojoClass))
    //先读内存缓存
    if (dataMap.contains(dataKey)) {
      dataMap.get(dataKey) //需要做下线释放掉对应的数据
//    } else if (DbServer.saveDataMap.containsKey(dataKey)) {
//      DbServer.saveDataMap.get(dataKey)
    } else {
      //再读redis
      var pojo: Option[BaseDbPojo] = Some(pojoClass.newInstance().asInstanceOf[BaseDbPojo])
      pojo.get.setId(id)
      val key = pojo.get.getKey
      val map = jc.hgetAll(key) //new ConcurrentHashMap[String, String]() //
      if (map.size() == 0) {
        pojo = None
      } else {
        map.foreach(e => {
          val key = e._1
          val value = e._2
          pojo.get.setter(key, value)
        })

        dataMap.put(dataKey, pojo)
      }

      //      log.info("getDbPojo耗时xx:" + dataKey + ":" + ( System.currentTimeMillis() - time ) )
      //可能再读mysql
//      if(map.size() == 0){
//        val sql = "select data from %s where id = %d".format(GameUtils.getClassName(pojoClass), id)
//        System.out.println(sql)
//        val actor  = context.actorSelection( ActorDefine.MYSQL_ACTOR_NAME )
//        val resultList :  util.ArrayList[String] = GameUtils.futureAsk(actor, QueryToMysql(sql), 30)
//        if(resultList.length > 0){
//          val result = resultList.get(0)
//
//          val jsonObject = new JSONObject(result)
//          jsonObject.keySet().foreach( key => {
//            val value = java.net.URLDecoder.decode(jsonObject.get(key).toString, "utf-8")
//            pojo.get.setter(key, value)
//          })
//
//          dataMap.put(dataKey, pojo)
//        }else{
//          pojo = None
//        }
//      }

      pojo
    }

  }

  def createDbPojo(pojoClass: Class[_]) = {
    val time = System.currentTimeMillis()

    val pojo: Option[BaseDbPojo] = Some(pojoClass.newInstance().asInstanceOf[BaseDbPojo])
    val key = pojo.get.getGroupIdKey
    var id = 0L
    try {
      id = jc.incr(key)

      //      val setKey = pojo.getGroupIdSetKey
      //      jc.sadd(setKey, id.toString)  //创建的KEY保存在set里面
      pojo.get.setId(id)


    } finally {

    }

    mysqlActor ! InsertToMysql(pojo.get.getClassName, id,pojo.get.getLogAreaId)

    log.info("createDbPojo耗时xx:" + (System.currentTimeMillis() - time))

    pojo

  }

  val batchSize = 400
  val dbQueue = new util.LinkedList[DbAction]()

  def isDbQueueEmpty() = {
//    var isEmpty = true
    var size = 0
    dbQueue.synchronized {
//      isEmpty = dbQueue.size() <= 0
      size = dbQueue.size()
    }

    size
  }

  def onTriggerDBAction() = {
    dbQueue.synchronized {
      val size = dbQueue.size()
      if (size > 0) {
        for (i <- 0 until batchSize) {
          val size = dbQueue.size()
          if (size > 0) {
            val action = dbQueue.poll()
            if (action.getType.equals(DbActionType.SAVE)) {
              val time = System.currentTimeMillis()
              //保存行为
              val key = action.getKey
              val map = action.getMap
              jc.hmset(key, map) //入库
              if (action.getExpireAt > 0) {
                jc.expireAt(key, action.getExpireAt * 1000L)
              }
              val dataKey = getDataKey(action.getId, action.getPojoClassName)
//              dataMap.remove(dataKey)
              DbServer.removeSaveDataMap(dataKey)
              log.error("-----------------SaveDB data----------------------:" + key + " time:" + " value:" + map + " " + (System.currentTimeMillis() - time))
              log.info("-----------------SaveDB data----------------------:" + key + " time:" + " value:" + map + " " + (System.currentTimeMillis() - time))
            } else if (action.getType.equals(DbActionType.DEL)) {
              val key = action.getKey
              jc.del(key)
//              log.info("-----------------DelDB data----------------------:" + key)
            }
          }
        }
      }
    }
  }

  //缓存map 统一用 getDataKey(pojo.getId, GameUtils.getClassName(pojo)) 做key
  val saveFieldMap: util.Map[String, util.Map[String, String]] = new ConcurrentHashMap[String, util.Map[String, String]]()

  //TODO 每次保存时缓存当前值，比较最新值的 diffMap
  def saveDbPojo(pojo: BaseDbPojo) = {
    val time = System.currentTimeMillis()

    val key = pojo.getKey
    val fields = pojo.getFieldNameList
    val dataKey = getDataKey(pojo.getId, GameUtils.getClassName(pojo))

    val map: java.util.Map[String, String] = new java.util.HashMap() //保存要入库的数据
    //    val valueMap: java.util.Map[String, String] = new java.util.HashMap()  //所有的值保存起来，以便比较
    val saveMap: util.Map[String, Object] = new java.util.HashMap()

    var oldFielMap = saveFieldMap.get(dataKey)
    if (oldFielMap == null) {
      oldFielMap = new java.util.HashMap()
    }

    fields.foreach(
      field => {
        val value = pojo.getter(field)

        var setStr: String = ""
        if (value.isInstanceOf[util.Set[Any]]) {
          setStr = GameUtils.set2str(value.asInstanceOf[util.Set[Any]])
        } else if (value.isInstanceOf[util.List[Any]]) {
          setStr = GameUtils.list2str(value.asInstanceOf[util.List[Any]])
        } else if (value != null && value.getClass.getName.equals("[B")) {
          //[B
          setStr = com.znl.framework.socket.websocket.Base64.encodeBytes(value.asInstanceOf[Array[Byte]])
        }
        else if (value == null) {
          if (value.isInstanceOf[Integer]) {
            setStr = "0"
          } else if (value.isInstanceOf[java.lang.Long]) {
            setStr = "0"
          } else {
            setStr = ""

          }
          pojo.setter(field, setStr) //随便把默认值附上去
        } else {
          setStr = value.toString
        }

        saveMap.put(field, setStr)

        //        valueMap.put(field, setStr)

        //        if(oldFielMap != null){
        val oldValue = oldFielMap.get(field)
        if ((oldValue != null) && (!oldFielMap.get(field).equals(setStr))) {
          //
          //            log.info("===key:%s==field:%s====oldValue:%s=====newValue:%s==========".format(key, field, oldValue, setStr))
          map.put(field, setStr)
        } else if (oldValue == null) {
          //            log.info("===key:%s==field:%s====oldValue:%s=====newValue:%s==========".format(key, field, oldValue, setStr))
          map.put(field, setStr)
        }
        oldFielMap.put(field, setStr) //替换成新值
        //        }else{
        //          map.put(field, setStr)
        //        }
      }
    )

    map.put("id", pojo.getId().toString)

    saveFieldMap.put(dataKey, oldFielMap)

    //TODO 这里可能要设置一个比较长的过期时间，然后在get的地方拿不到，则到mysql拿
    saveMap.put("id", pojo.getId())

    //    jc.hmset(key, map)
    //
    //    if(pojo.getExpireAt > 0 ){  //设置了过期的时间戳
    //      jc.expireAt(key, pojo.getExpireAt * 1000L)
    //    }

    if (map.size() > 1) {
      //没有值改变，那就不入库了，减少IO
      val action = new DbAction
      action.setId(pojo.getId)
      action.setKey(key)
      action.setMap(map)
      action.setPojoClassName(pojo.getClassName)
      action.setExpireAt(pojo.getExpireAt)
      action.setType(DbActionType.SAVE)

      //log.info("==dbQueue.push:=key:%s=====newValue:%s==========".format(key, map))


      dbQueue.offer(action) //TODO 入不了就写日志 警报

      //TODO 如果入库队列里面又出现相同的Key，则会出现问题
//      DbServer.saveDataMap.put(dataKey, Some(pojo))
      //发送到别的进程，进行mysql数据备份 TODO
      val json = new JSONObject(saveMap)
      if(pojo.getClassName.equals("Player")){
//        log.info("UpdateToMysql:[]" + pojo.getClassName + ":" + json.toString)
      }
      mysqlActor ! UpdateToMysql(pojo.getClassName, pojo.getId, json.toString,pojo.getLogAreaId)
    }
    //    log.info("saveDbPojo耗时:" + key + " " + ( System.currentTimeMillis() - time ) )
  }

  def delDbPojo(pojo: BaseDbPojo) = {
//
    //    jc.del(key)  //TODO 需要写一级日志
    val dataKey = getDataKey(pojo.getId, GameUtils.getClassName(pojo))
    saveFieldMap.remove(dataKey)

    val key = pojo.getKey
    val action = new DbAction
    action.setType(DbActionType.DEL)
    action.setKey(key)

    dbQueue.offer(action)


    mysqlActor ! DelToMysql(pojo.getClassName, pojo.getId, pojo.getLogAreaId)
  }

  //全局保存一些协议包内容 这些数据默认只保存一个月
  def createProtoGeneratedMessage(cmd: Int, msg: GeneratedMessage, expire: Int = 30 * 24 * 60) = {
    val sourceBytes: Array[Byte] = msg.toByteArray
    val sourceStr: String = com.znl.framework.socket.websocket.Base64.encodeBytes(sourceBytes)
    val zipStr: String = ZipUtils.gzip(sourceStr)

    val id = jc.incr(ActorDefine.DB_PROTO_INC_ID_KEY)

    val key = ActorDefine.DB_PROTO_NAME_FORMAT.format(cmd, id)
    jc.set(key, zipStr)

    jc.expire(key, expire)

    Some(id)
  }


  //拿到的，自己去解析
  def getProtoGeneratedMessage(cmd: Int, id: Long) = {
    val key = ActorDefine.DB_PROTO_NAME_FORMAT.format(cmd, id)
    val zipStr: String = jc.get(key)

    if (zipStr == null) {
      None
    } else {
      val unzipStr: String = ZipUtils.gunzip(zipStr)
      val sourceBytes: Array[Byte] = com.znl.framework.socket.websocket.Base64.decode(unzipStr)

      Some(sourceBytes)
    }
  }

  /////////////////////////////////////////区共用集合逻辑处理//////////////////////////////////////////////////////////////////////////


  def onUpdateSetDbPojoElement(setDbPojo: BaseSetDbPojo, key : String, value : Long): Unit ={
    val db_set_key = DbServer.getSetDbPojoDbKey(setDbPojo)
    jc.zadd(db_set_key, value, key)

    //保存到mysql
    mysqlActor ! UpdateSetToMysql(setDbPojo.getClassName, key, value,setDbPojo.getLogAreaId)
  }

  def onDelSetDbPojoElement(setDbPojo: BaseSetDbPojo, key : String): Unit ={
    val db_set_key = DbServer.getSetDbPojoDbKey(setDbPojo)
    jc.zrem(db_set_key, key)
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  def isTrue = {
    true
  }

}
