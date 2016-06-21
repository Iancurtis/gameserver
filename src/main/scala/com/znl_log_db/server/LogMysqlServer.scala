package com.znl.service

import java.net.URLEncoder
import java.sql.SQLException
import java.util
import java.util.Properties

import akka.actor.{Props, ActorLogging, Actor}
import com.mchange.v2.c3p0.ComboPooledDataSource
import com.znl.base.BaseLog
import com.znl.define.ActorDefine
import com.znl.msg.GameMsg.{IsDbQueueEmpty, TriggerExecuteToMysql, SendAdminLog}
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

object LogMysqlServer {
  def props(p: Properties) = Props(classOf[LogMysqlServer], p )
}

class LogMysqlServer(p: Properties) extends Actor with ActorLogging{
  val serverPlat = p.getProperty("serverPlat")
  val log_mysql_ip = p.getProperty("log_mysql_ip")
  val log_mysql_user = p.getProperty("log_mysql_user")
  val log_mysql_pwd = p.getProperty("log_mysql_password")
//  val logAreaKey =  p.getProperty("logAreaKey" + areaLogicId)
//  val mysql_db = "log_%s_s%s".format(serverPlat, logAreaKey)
  val cpds = new ComboPooledDataSource()
  cpds.setDriverClass("com.mysql.jdbc.Driver")
  cpds.setJdbcUrl("jdbc:mysql://%s?useUnicode=true&amp;characterEncoding=UTF8".format(log_mysql_ip))
  cpds.setUser(log_mysql_user)
  cpds.setPassword(log_mysql_pwd)

  cpds.setMinPoolSize(5)
  cpds.setAcquireIncrement(5)
  cpds.setMaxPoolSize(20)

  def getConnection() ={
    try{
      cpds.getConnection
    }catch{
      case e:SQLException =>
        log.error(e.getCause, "getConnection error")
        null
    }
  }

  def dbNameByAreaKey(logAreaKey : String): String ={
    val mysql_db = "log_%s_s%s".format(serverPlat, logAreaKey)
    mysql_db
  }

  override def preStart() ={
    log.error("start log db server")
  }

  import context.dispatcher
  context.system.scheduler.schedule(2 seconds, 2 seconds,context.self, TriggerExecuteToMysql())

  override def receive: Receive = {
    case SendAdminLog(log, actionType, key , value, logAreaKey) =>
      onSendAdminLog(log, actionType, key : String, value : Long, logAreaKey : String)
    case TriggerExecuteToMysql() =>
      onTriggerExecuteToMysql()
    case IsDbQueueEmpty() =>
      sender() ! isDbQueueEmpty()
    case _=>
  }

  val batchSize = 100
  //sql列表
  val sqlQueue = new util.LinkedList[String]()
  def onTriggerExecuteToMysql() ={
    sqlQueue.synchronized{
      val size = this.sqlQueue.size()
      if(size > 0){

        val time = System.currentTimeMillis()

        val conn = getConnection()
        val statement = conn.createStatement()
        for( i <- 0 until batchSize){
          val size = this.sqlQueue.size()
          if (size > 0){
            val sql = this.sqlQueue.poll()
            statement.addBatch(sql)
          }

        }

        statement.executeBatch()
        statement.close()
        conn.close()

        log.error("存储日志时间：" + (System.currentTimeMillis() - time)  )
      }
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

  def isDbQueueEmpty() ={
    //    var isEmpty = true
    var count = 0
    sqlQueue.synchronized{
      //      isEmpty = sqlQueue.size() <= 0
      count = sqlQueue.size()
    }

    //    isEmpty
    count
  }

  //actionType 1插入 2更新
  //默认只会插入
  def onSendAdminLog(obj : BaseLog, actionType : Int, key : String, value : Long, logAreaKey : String) ={
    var sql = ""
    if(actionType == ActorDefine.ADMIN_LOG_ACTION_INSERT){
      sql = getInsertSql(obj, logAreaKey)
    }else if(actionType == ActorDefine.ADMIN_LOG_ACTION_UPDATE){
      sql = getUpdateSql(obj, key, value, logAreaKey)
    }

    log.info(sql)
    val logType = GameUtils.getClassName(obj)
    if(logType.equals("tbllog_online")){
      log.error(sql)
      onExecuteToMysql(sql)
    }else{
      sqlQueue.offer(sql)
    }

  }

  def getInsertSql(obj : BaseLog, logAreaKey : String) ={
    val json: JSONObject = new JSONObject(obj)
    val iter = json.keys

    val logType = GameUtils.getClassName(obj)
    val db = dbNameByAreaKey(logAreaKey)

    var sql: String = "insert into %s.%s set ".format(db, logType)
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
          longValue =  (System.currentTimeMillis() / 1000).toInt
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

  def getUpdateSql(obj : BaseLog, key : String, value : Long, logAreaKey : String) ={
    val json: JSONObject = new JSONObject(obj)
    val iter = json.keys

    val logType = GameUtils.getClassName(obj)
    val db = dbNameByAreaKey(logAreaKey)

    var firstFlag = true
    var sql: String = "update %s.%s set ".format(db, logType)
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
