package com.znl.service.actor

import java.io.IOException
import java.sql._
import java.util
import java.util.Properties
import java.util.concurrent.atomic.AtomicBoolean

import akka.actor.{Props, ActorLogging, Actor}
import akka.actor.Actor.Receive
import com.mchange.v2.c3p0.ComboPooledDataSource
import com.znl.core.DbOper
import com.znl.define.ActorDefine
import com.znl.log.CustomerLogger
import com.znl.msg.GameMsg._
import com.znl.server.actor.QueueLoaderActor
import com.znl.utils.GameUtils
import org.json.JSONObject

import scala.collection.JavaConversions._
import scala.concurrent.duration._

/**
 * Created by Administrator on 2015/11/18.
 */

object MySqlActor{
  def props(mysql_ip : String, mysql_db : String, mysql_user : String, mysql_pwd : String,p:Properties)
  = Props(classOf[MySqlActor], mysql_ip, mysql_db, mysql_user, mysql_pwd, p)
}

class MySqlActor(mysql_ip : String, mysql_db : String, mysql_user : String, mysql_pwd : String, p:Properties) extends Actor with ActorLogging{

  val game_db_server_ip = p.getProperty("game_db_server_ip")
  val game_db_server_port = p.getProperty("game_db_server_port")

  //通过用C3P0 来做mysql的连接池，如果单一连接的话，会出现空闲超时异常
//  val cpds = new ComboPooledDataSource()
//  cpds.setDriverClass("com.mysql.jdbc.Driver")
//  cpds.setJdbcUrl("jdbc:mysql://%s/%s?useUnicode=true&characterEncoding=UTF-8".format(mysql_ip, mysql_db))
//  cpds.setUser(mysql_user)
//  cpds.setPassword(mysql_pwd)
//
//  cpds.setMinPoolSize(5)
//  cpds.setAcquireIncrement(5)
//  cpds.setMaxPoolSize(20)

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
      val loader = context.actorOf(QueueLoaderActor.props(index,mysql_ip, mysql_db, mysql_user, mysql_pwd, p), LOAD_NAME+index)
      context.watch(loader)
      loadList.add(true)
      index = index +1
    }
    println("服务器启动读取节点完毕LOAD_LIST_SIZE="+LOAD_LIST_SIZE)
  }



//  val jdbc = "jdbc:mysql://%s/%s?user=%s&password=%s".format(mysql_ip, mysql_db, mysql_user, mysql_pwd)
//  val conn = DriverManager.getConnection(jdbc)

  override def postStop() ={
    log.info("======MySqlActor=====postStop=====")
  }

//  val gameDbServer = context.actorSelection("akka.tcp://GameDBServer@%s:%s/user/root/mysqlServer".format(game_db_server_ip, game_db_server_port))

  //TODO 可能要做一下池处理，获取移动到另一个进程去(关服逻辑 )
  override def receive: Receive = {
    case InsertToMysql(table : String, id : Long, logAreaId : Int) =>
//      gameDbServer ! InsertToMysql(table, id)
      onInsertToMysql(table, id,logAreaId)
    case UpdateToMysql(table : String, id : Long, json : String, logAreaId : Int) =>
//      gameDbServer ! UpdateToMysql(table, id, json)
      onUpdateToMysql(table, id, json,logAreaId)
    case DelToMysql(table : String, id : Long, logAreaId : Int) =>
//      gameDbServer ! onDelToMysql(table, id)
      onDelToMysql(table, id,logAreaId)
    case QueryToMysql(sql : String) =>
      sender() ! onQuery(sql)
    case TriggerExecuteToMysql() =>
      onTriggerExecuteToMysql()  //TODO 待优化
    case IsDbQueueEmpty() =>
      sender() ! isDbQueueEmpty()
    case LoadDone(id : Int) =>
      setLoaderDone(id)
    case _=>

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
//  val sqlQueue = new util.LinkedList[String]()

  def isDbQueueEmpty() ={
    var rs = queueSize
    if(rs == 0){
      //遍历所有的loadList，看看是不是都好了
      for (done : Boolean <- loadList){
        if (done == false){
          rs = rs +1
        }
      }
    }
    rs
  }


  def pushQueue(table : String,sql : String) ={
//    sqlQueue.synchronized{
//      sqlQueue.offer(sql)
//    }

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

  def onInsertToMysql(table : String, id : Long, logAreaId : Int) ={
    val formatStr = "replace into %s(id, data) values (%d, '{\"id\" : %d}');"
    val sql : String = formatStr.format("GcolGame"+logAreaId+"."+table, id, id)
    pushQueue(table,sql)
//    executeUpdate(sql)a
  }

  def onUpdateToMysql(table : String, id : Long, json : String, logAreaId : Int) ={
//    val formatStr = "update %s set data='%s' where json_extract(data, '$.id') = %d;"
//    val sql : String = formatStr.format(table, json, id)
    val formatStr = "replace into %s(id, data) values (%d, '%s');"
    val sql : String = formatStr.format("GcolGame"+logAreaId+"."+table, id, json)
//    executeUpdate(sql)
    pushQueue(table,sql)
  }

  def onDelToMysql(table : String, id : Long, logAreaId : Int) ={
    val formatStr = "delete from %s where id = %d;"
    val sql : String = formatStr.format("GcolGame"+logAreaId+"."+table, id)
    pushQueue(table,sql)
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

      }
    }
    val unLockKeys: util.HashSet[String] = new util.HashSet[String]
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


  def setLoaderDone(id : Int): Unit ={
    loadList.set(id,true)
    println("load读取完毕"+id)
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
