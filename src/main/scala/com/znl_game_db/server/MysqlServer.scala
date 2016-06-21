package com.znl.service.actor

import java.io.IOException
import java.sql._
import java.util

import akka.actor.{Props, ActorLogging, Actor}
import akka.actor.Actor.Receive
import com.mchange.v2.c3p0.ComboPooledDataSource
import com.znl.msg.GameMsg._
import org.json.JSONObject

import scala.collection.JavaConversions._
import scala.concurrent.duration._

/**
 * Created by Administrator on 2015/11/18.
 */

object MySqlServer{
  def props(mysql_ip : String, mysql_db : String, mysql_user : String, mysql_pwd : String)
  = Props(classOf[MySqlServer], mysql_ip, mysql_db, mysql_user, mysql_pwd)
}

class MySqlServer(mysql_ip : String, mysql_db : String, mysql_user : String, mysql_pwd : String) extends Actor with ActorLogging{

  //通过用C3P0 来做mysql的连接池，如果单一连接的话，会出现空闲超时异常
  val cpds = new ComboPooledDataSource()
  cpds.setDriverClass("com.mysql.jdbc.Driver")
  cpds.setJdbcUrl("jdbc:mysql://%s/%s?useUnicode=true&characterEncoding=UTF-8".format(mysql_ip, mysql_db))
  cpds.setUser(mysql_user)
  cpds.setPassword(mysql_pwd)

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

  import context.dispatcher
  context.system.scheduler.schedule(2 seconds, 2 seconds,context.self, TriggerExecuteToMysql())


  //  val jdbc = "jdbc:mysql://%s/%s?user=%s&password=%s".format(mysql_ip, mysql_db, mysql_user, mysql_pwd)
  //  val conn = DriverManager.getConnection(jdbc)

  override def preStart() ={
    log.error("start game db server")
  }

  override def postStop() ={
    log.info("======MySqlActor=====postStop=====")
  }

  //TODO 可能要做一下池处理，获取移动到另一个进程去(关服逻辑 )
  override def receive: Receive = {
    case InsertToMysql(table : String, id : Long, logAreaId : Int) =>
      onInsertToMysql(table, id, logAreaId)
    case UpdateToMysql(table : String, id : Long, json : String, logAreaId : Int) =>
      onUpdateToMysql(table, id, json, logAreaId)
    case DelToMysql(table : String, id : Long, logAreaId : Int) =>
      onDelToMysql(table, id, logAreaId)
    case QueryToMysql(sql : String) =>
      sender() ! onQuery(sql)
    case TriggerExecuteToMysql() =>
      onTriggerExecuteToMysql()  //TODO 待优化
    case IsDbQueueEmpty() =>
      sender() ! isDbQueueEmpty()
    case _=>

  }

  val batchSize = 100
  //sql列表
  val sqlQueue = new util.LinkedList[String]()

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

  def pushQueue(sql : String) ={
    sqlQueue.synchronized{
      sqlQueue.offer(sql)
    }
  }

  def onInsertToMysql(table : String, id : Long, logAreaId : Int) ={
    val formatStr = "replace into %s(id, data) values (%d, '{\"id\" : %d}');"
    val sql : String = formatStr.format("GcolGame"+logAreaId+"."+table, id, id)
    pushQueue(sql)
    //    executeUpdate(sql)
  }

  def onUpdateToMysql(table : String, id : Long, json : String, logAreaId : Int) ={
    //    val formatStr = "update %s set data='%s' where json_extract(data, '$.id') = %d;"
    //    val sql : String = formatStr.format(table, json, id)
    val formatStr = "replace into %s(id, data) values (%d, '%s');"
    val sql : String = formatStr.format("GcolGame"+logAreaId+"."+table, id, json)
    //    executeUpdate(sql)
    pushQueue(sql)
  }

  def onDelToMysql(table : String, id : Long, logAreaId : Int) ={
    val formatStr = "delete from %s where id = %d;"
    val sql : String = formatStr.format("GcolGame"+logAreaId+"."+table, id)
    pushQueue(sql)
    //    executeUpdate(sql)
  }

  //进行查询数据 一般只有管理平台会用到
  def onQuery(sql : String) ={
    val queryList = new util.ArrayList[String]()

    val time = System.currentTimeMillis()
    System.out.println(time)
    try{
      val conn = getConnection()
      val pstmt  = conn.prepareStatement(sql)
      val rs = pstmt.executeQuery()
      val col = rs.getMetaData.getColumnCount


      while (rs.next()){
        val result = rs.getString(1)

        //        val jsonObj = new JSONObject(result)
        //      for(i <- 1 until col + 1){
        //        jsonObj.put(rs.getMetaData.getColumnName(i), rs.getString(i))
        //      }
        queryList.add(result)
      }

      pstmt.close()
      conn.close()
    }catch {
      case e: Exception => {
        e.printStackTrace
      }

    }

    queryList
  }

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

        log.error("存储时间：" + (System.currentTimeMillis() - time)  )
      }



    }

  }

  def executeUpdate(sql : String) ={
    var stmt : Statement = null
    var rs : ResultSet = null
    var conn : Connection = null
    try{
      val time = System.currentTimeMillis()

      conn = getConnection()
      stmt = conn.createStatement()
      stmt.executeUpdate(sql)


      log.info("存储时间：" + (System.currentTimeMillis() - time)  )

    }catch{
      case e : SQLException =>
        e.printStackTrace()
        log.error(e.getCause, "MySqlActor onExecutQuery:" + sql)
    }finally {
      if(rs != null){
        try{
          rs.close()
        }catch {
          case sqlEx : SQLException =>

        }
        rs = null
      }

      if(stmt != null){
        try{
          stmt.close()
        }catch {
          case sqlEx : SQLException =>

        }
        stmt = null
      }

      if(conn != null){
        conn.close()
      }
    }
  }
}
