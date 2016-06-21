package com.znl.service

import java.net.URLEncoder
import java.sql.SQLException
import java.util
import java.util.Properties
import java.util.concurrent.atomic.AtomicBoolean

import akka.actor.{Props, ActorLogging, Actor}
import com.mchange.v2.c3p0.ComboPooledDataSource
import com.znl.base.BaseLog
import com.znl.core.DbOper
import com.znl.define.ActorDefine
import com.znl.log.CustomerLogger
import com.znl.msg.GameMsg._
import com.znl.server.actor.QueueLoaderActor
import com.znl.utils.GameUtils
import org.json.JSONObject
import scala.concurrent.duration._
/**
 * serverPlat android_cn 服务器平台标识
 * dbAreaKey db区key， 合区规则为 2服合到1服 ，只会有1
 * log_mysql_ip : String, log_mysql_user : String, log_mysql_pwd : String, serverPlat : String
 * Created by Administrator on 2015/12/16.
 * areaLogicId 逻辑ID
 */

object LogService {
  def props(p: Properties, areaLogicId : Int,areaKey : String) = Props(classOf[LogService], p,areaLogicId,areaKey )
}

class LogService(p: Properties, areaLogicId : Int,areaKey : String) extends Actor with ActorLogging{
  val serverPlat = p.getProperty("serverPlat")
  val log_mysql_ip = p.getProperty("log_mysql_ip")
  val log_mysql_user = p.getProperty("log_mysql_user")
  val log_mysql_pwd = p.getProperty("log_mysql_password")
  val logAreaKey =  p.getProperty("logAreaKey" + areaLogicId)
  val mysql_db = "log_%s_s%s".format(serverPlat, logAreaKey)
  val log_db_server_ip = p.getProperty("log_db_server_ip")
  val log_db_server_port = p.getProperty("log_db_server_port")


//  val cpds = new ComboPooledDataSource()

//  cpds.setDriverClass("com.mysql.jdbc.Driver")
//  cpds.setJdbcUrl("jdbc:mysql://%s/%s?useUnicode=true&amp;characterEncoding=UTF8".format(log_mysql_ip, mysql_db))
//  cpds.setUser(log_mysql_user)
//  cpds.setPassword(log_mysql_pwd)
//
//  cpds.setMinPoolSize(5)
//  cpds.setAcquireIncrement(5)
//  cpds.setMaxPoolSize(20)

  val cpds = new com.alibaba.druid.pool.DruidDataSource()
  cpds.setDriverClassName("com.mysql.jdbc.Driver")
  cpds.setUrl("jdbc:mysql://%s/%s?useUnicode=true&amp;characterEncoding=UTF8".format(log_mysql_ip, mysql_db))
  cpds.setUsername(log_mysql_user)
  cpds.setPassword(log_mysql_pwd)

  cpds.setMaxActive(20)
  cpds.setMinIdle(1)
  cpds.setMaxWait(60000)
  cpds.setInitialSize(1)


  cpds.addFilters("stat,log4j")

  def getConnection() ={
    try{
      cpds.getConnection
    }catch{
      case e:SQLException =>
        log.error(e.getCause, "getConnection error")
        null
    }
  }

  val loadList = new util.ArrayList[Boolean]()
  val LOAD_LIST_SIZE = 20
  val LOAD_NAME = "LOADER"
  override def preStart() = {
    import context.dispatcher
    context.system.scheduler.schedule(50 milliseconds, 50 milliseconds,context.self, TriggerExecuteToMysql())
    var index = 0
    //创建读取节点
    while (index < LOAD_LIST_SIZE){
      val loader = context.actorOf(QueueLoaderActor.props(index,log_mysql_ip, mysql_db, log_mysql_user, log_mysql_pwd, p), LOAD_NAME+index)
      context.watch(loader)
      loadList.add(true)
      index = index +1
    }
    val loader = context.actorOf(QueueLoaderActor.props(-1,log_mysql_ip, mysql_db, log_mysql_user, log_mysql_pwd, p), LOAD_NAME+"online")
    context.watch(loader)
    loader ! WriteServerOpen(areaKey)
    println("服务器启动读取节点完毕LOAD_LIST_SIZE="+LOAD_LIST_SIZE)
  }

  //sql列表
  val queuesMap : util.HashMap[String,util.LinkedList[DbOper]] = new util.HashMap[String,util.LinkedList[DbOper]]()//[tableName,sqlStr]
  val keys = new util.LinkedList[String]()
  var keyIndex = 0
  val userHandleLock : util.HashMap[String, AtomicBoolean] = new util.HashMap[String, AtomicBoolean]//保护原子锁
  val batchCommitSize = 100;// 每次批量提交的条目数
  val userRemoveCheck : util.HashMap[String,Long] = new util.HashMap[String,Long]()
  private var allReceiveSize: Int = 0
  private var allDoneSize: Int = 0
  private var queueSize: Int = 0

//  val logDbServer = context.actorSelection("akka.tcp://LogDBServer@%s:%s/user/root/logMysqlServer".format(log_db_server_ip, log_db_server_port))

  override def receive: Receive = {
    case SendAdminLog(log, actionType, key , value, xx) =>
//      logDbServer ! SendAdminLog(log, actionType, key , value, logAreaKey)
      onSendAdminLog(log, actionType, key : String, value : Long)
    case TriggerExecuteToMysql() =>
      onTriggerExecuteToMysql()
    case IsDbQueueEmpty() =>
      sender() ! isDbQueueEmpty()
    case LoadDone(id : Int) =>
      setLoaderDone(id)
    case _=>
  }

  //sql列表
  def onTriggerExecuteToMysql() : Unit ={
    if (keys.size() == 0){
      return
    }
    val datas = getMysqlQueues()
    if(datas.size()==0){
      return
    }
    //分发到子actor去保存
    loadList.synchronized{
      var loaderIndex = -1
      var index = 0
      while (index < LOAD_LIST_SIZE && loaderIndex == -1){
        if (loadList.get(index)){
          loaderIndex = index
        }
        index = index+1
      }
      if (loaderIndex >= 0){
        loadList.set(loaderIndex,false)
        context.actorSelection(LOAD_NAME+loaderIndex) ! LoadMysql(datas)
      }else{
        println("error！！！！logService的存储池满了！！！")
      }
    }
    val unLockKeys: util.HashSet[String] = new util.HashSet[String]
    import scala.collection.JavaConversions._
    for (dbOper <- datas) {
      if(dbOper != null){
        val key = dbOper.table
        if(unLockKeys.contains(key) == false){//还未解过的
        val lock = getLockByKey(key)
          if(lock!=null){
            lock.set(false)
            unLockKeys.add(key)
          }
        }
      }
    }

  }

  /***获得读取的数据队列***/
  def getMysqlQueues(): util.List[DbOper] ={
    val rs :util.List[DbOper] = new util.ArrayList[DbOper]()
    queuesMap.synchronized{
      var count: Int = 0
      var getNum: Int = 0
      val keysSize: Int = keys.size
      val needRemove: util.List[String] = new util.ArrayList[String]
      var break : Boolean = false
      while(break == false){
        if (keyIndex >= keysSize){
          keyIndex = 0//队列越界了，从头起
        }
        count = count +1
        val key: String = keys.get(keyIndex)
        if(key!=null){
          val queue : util.LinkedList[DbOper] = queuesMap.get(key)
          if(queue!=null){
            if(queue.size()>0){
              val lock :AtomicBoolean= getLockByKey(key)
              if(!lock.get()){
                lock.set(true)
                getNum+=queue.size()
                rs.addAll(queue)
                queue.clear()
                val time = GameUtils.getServerDate().getTime
                userRemoveCheck.put(key,time)
              }
            }else{
              val time = userRemoveCheck.get(key)
              if((GameUtils.getServerDate().getTime-time)> ActorDefine.REMOVE_CHECK_MAX_TIME){
                needRemove.add(key)
              }
            }
          }else{
            CustomerLogger.error("key="+key+"没有对应的Queue")
          }
        }else{
          CustomerLogger.error("readPoint="+keyIndex+"没有对应的key")
        }

        if(count>=keysSize){
          break = true
        }else if(getNum>=batchCommitSize){
          break = true
        }else{
          keyIndex = keyIndex +1
        }
      }
      queueSize = queueSize - getNum
      allDoneSize+=getNum
      import scala.collection.JavaConversions._
      for (k <- needRemove) {
        removeKey(k)
      }
      if(needRemove.size()>0){
        CustomerLogger.error("移除key"+needRemove.size()+"个!")
      }
    }
    rs
  }

  private def removeKey(key: String): Boolean = {
    val queue: util.LinkedList[DbOper] = queuesMap.get(key)
    if (queue != null && queue.size > 0) {
      false
    }else{
      keys.remove(key)
      queuesMap.remove(key)
      userRemoveCheck.remove(key)
      userHandleLock.remove(key)
      true
    }
  }

  /***获得原子锁***/
  def getLockByKey(key: String): AtomicBoolean = {
    if (userHandleLock.containsKey(key)) {
      return userHandleLock.get(key)
    }
    else {
      val ab: AtomicBoolean = new AtomicBoolean(false)
      userHandleLock.put(key, ab)
      return ab
    }
  }

  def isDbQueueEmpty() ={
    queueSize
  }

  def setLoaderDone(id : Int): Unit ={
    if(id >= 0){
      loadList.set(id,true)
    }
    println("load读取完毕"+id)
  }

  def pushQueue(table : String,sql : String) ={
    queuesMap.synchronized{
      var queue = queuesMap.get(table)
      if (queue == null){
        queue = new util.LinkedList[DbOper]()
        queuesMap.put(table,queue)
        keys.add(table)
      }
      if(queue.size()!=0 && queue.size()%100==0){
        println("MySqlActor的table="+table+"的队列长度="+queue.size());
      }
      val rs : Boolean = queue.offer(new DbOper(table,sql))
      queuesMap.put(table,queue)
      if(rs){
        queueSize = queueSize + 1
        allReceiveSize = allReceiveSize + 1
        if(queueSize>1000 && queueSize%100==0){
          println("MySqlActor的queueSize数量很多了=== "+queueSize)
          CustomerLogger.error("MySqlActor的queueSize数量很多了=== "+queueSize)
        }
      }
    }

  }

  //actionType 1插入 2更新
  //默认只会插入
  def onSendAdminLog(obj : BaseLog, actionType : Int, key : String, value : Long) ={
    var sql = ""

    if(actionType == ActorDefine.ADMIN_LOG_ACTION_INSERT){
      sql = getInsertSql(obj)
    }else if(actionType == ActorDefine.ADMIN_LOG_ACTION_UPDATE){
      sql = getUpdateSql(obj, key, value)
    }

    log.info(sql)
    val logType = GameUtils.getClassName(obj)
    if(logType.equals("tbllog_online") || logType.equals("tbllog_pay")){
      context.actorSelection(LOAD_NAME+"online") ! LoadMysqlDic(new DbOper(logType,sql))
    }else{
      pushQueue(logType,sql)
    }
  }

  //直接插队入库 在线统计等
  def onExecuteToMysql(sql : String) ={
    val conn = getConnection()
    val statement = conn.createStatement()
    statement.addBatch(sql)
    statement.executeBatch()
    statement.close()
    conn.close()
  }

  def getInsertSql(obj : BaseLog) ={
    val json: JSONObject = new JSONObject(obj)
    val iter = json.keys

    val logType = GameUtils.getClassName(obj)

    var sql: String = "insert into %s set ".format(mysql_db+"."+logType)
    while (iter.hasNext) {
      val key: String = iter.next
      val value: Any = json.get(key)
      if (value.isInstanceOf[String]) {
        if (iter.hasNext) {
          sql = sql + "%s='%s', ".format( key, value)
        }
        else {
          sql = sql + "%s='%s'".format( key, value)
        }
      }
      else {
        var longValue =  json.getLong(key)
        if(key.equals("log_time")){
          longValue = GameUtils.getServerTime
        }
        if (iter.hasNext) {
          sql = sql + "%s=%d, ".format( key, longValue)
        }
        else {
          sql = sql + "%s=%d ".format( key, longValue)
        }
      }
    }
    sql = sql + ";"
    sql
  }

  def getUpdateSql(obj : BaseLog, key : String, value : Long) ={
    val json: JSONObject = new JSONObject(obj)
    val iter = json.keys

    val logType = GameUtils.getClassName(obj)

    var firstFlag = true
    var sql: String = "update %s set ".format(mysql_db+"."+logType)
    while (iter.hasNext) {
      val key: String = iter.next
      val value: AnyRef = json.get(key)
      if (value.isInstanceOf[String]) {
        if(!value.equals("")){
          if (iter.hasNext && firstFlag) {
            sql = sql + "%s='%s' ".format( key, value)
            firstFlag = false
          }
          else {
            sql = sql + ",%s='%s'".format( key, value)
          }
        }
      }
      else {
        if(json.getLong(key) != 0){
          if (iter.hasNext  && firstFlag) {
            sql = sql + "%s=%d ".format( key, json.getLong(key))
            firstFlag = false
          }
          else {
            sql = sql + ",%s=%d ".format( key, json.getLong(key))
          }
        }
      }
    }
    sql = sql + " where %s=%d;".format(key, value)
    sql
  }
}
