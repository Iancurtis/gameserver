package com.znl.server.actor

import java.sql.{Statement, SQLException}
import java.util
import java.util.Properties

import akka.actor.Actor.Receive
import akka.actor.{ActorLogging, Actor, Props}
import com.znl.core.DbOper
import com.znl.msg.GameMsg.{WriteServerOpen, LoadMysqlDic, LoadDone, LoadMysql}

/**
 * Created by Administrator on 2016/3/2.
 */

object QueueLoaderActor{
  def props(id : Int,mysql_ip : String, mysql_db : String, mysql_user : String, mysql_pwd : String,p:Properties)
  = Props(classOf[QueueLoaderActor], id,mysql_ip, mysql_db, mysql_user, mysql_pwd, p)
}

class QueueLoaderActor(id : Int,mysql_ip : String, mysql_db : String, mysql_user : String, mysql_pwd : String, p:Properties) extends Actor with ActorLogging{
  println("服务器启动读取节点完毕id="+id)
  val game_db_server_ip = p.getProperty("game_db_server_ip")
  val game_db_server_port = p.getProperty("game_db_server_port")

  val cpds = new com.alibaba.druid.pool.DruidDataSource()
  cpds.setDriverClassName("com.mysql.jdbc.Driver")
  cpds.setUrl("jdbc:mysql://%s/?useUnicode=true&characterEncoding=UTF-8".format(mysql_ip))
  cpds.setUsername(mysql_user)
  cpds.setPassword(mysql_pwd)

  cpds.setMaxActive(20)
  cpds.setMinIdle(1)
  cpds.setMaxWait(60000)
  cpds.setInitialSize(1)


  def getConnection() ={
    try{
      cpds.getConnection
    }catch{
      case e:SQLException =>
        log.error( "getConnection error",e)
        null
    }
  }

  override def receive: Receive = {

    case LoadMysql(data :util.List[DbOper]) =>
      import scala.collection.JavaConversions._
      val conn = getConnection()
      var statement: Statement = null
      try{
        println("mysql_db:"+mysql_db+" ;load开始读取"+id+"，读取长度="+data.size())

        val time = System.currentTimeMillis()
        statement = conn.createStatement()
        for (dbOper <- data) {
          statement.addBatch(dbOper.sql)
          //System.err.println("+++++++++++++保存的sql:"+dbOper.sql)
        }
        statement.executeBatch()
        log.info("存储时间：" + (System.currentTimeMillis() - time))
      }catch{
        case e:Exception =>
          if(data != null){
            for (dbOper <- data) {
              log.error(dbOper.sql)
            }
          }
          log.error("QueueLoaderActor error",e)
          e.printStackTrace()
      }finally {
        if(statement != null){
          statement.close()
        }
        if(conn != null){
          conn.close()
        }
        sender() ! LoadDone(id)
      }

    case LoadMysqlDic(dbOper : DbOper)=>
      val conn = getConnection()
      var statement: Statement = null
      try{
//        println("mysql_db:"+mysql_db+" ;load开始读取"+id+"这次录入操作是："+dbOper.sql)
        val time = System.currentTimeMillis()
        statement = conn.createStatement()
        statement.addBatch(dbOper.sql)
        statement.executeBatch()
        log.info("存储时间：" + (System.currentTimeMillis() - time))
      }catch{
        case e:Exception =>
          log.error( "QueueLoaderActor error",e)
          e.printStackTrace()
          println("loader出现错误，id="+id)
      }finally {
        if(statement != null){
          statement.close()
        }
        if(conn != null){
          conn.close()
        }
        sender() ! LoadDone(id)
      }
    case WriteServerOpen(areaKey : String) =>
      
    case _=>
  }


  def loadOper(): Unit ={

  }

  override def preStart() = {
    sender() ! LoadDone(id)
  }
}
