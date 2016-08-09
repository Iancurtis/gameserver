package com.znl_game_clear.server

import java.io.{InputStreamReader, BufferedReader}
import java.sql.{Statement, SQLException}
import java.util

import akka.actor.ActorSystem
import com.znl.base.{BaseDbPojo, BaseSetDbPojo}
import com.znl.define.ActorDefine
import com.znl.msg.GameMsg.{GiveMeAPill, LoadDone, GMCommand, DelToMysql}
import com.znl.pojo.db.Player
import com.znl.pojo.db.set._
import com.znl.proxy.DbProxy
import com.znl.server.action.{DbActionType, DbAction}
import com.znl.utils.{GameUtils, GetClassUtil}
import redis.clients.jedis.{Tuple, JedisCluster, HostAndPort}
import scala.collection.JavaConversions._
/**
 * Created by Administrator on 2016/3/8.
 */
object RedisDataClear extends App{
  var redisIp = "192.168.10.190"
  var redisPort = 7001
  val jedisClusterNodes: util.Set[HostAndPort] = new util.HashSet[HostAndPort]
  jedisClusterNodes.add(new HostAndPort(redisIp, redisPort))
  val jc: JedisCluster = new JedisCluster(jedisClusterNodes)



  var serverIp = new util.HashMap[Integer,java.lang.String]()
  def initServer(): Unit ={
//    serverIp = new util.HashMap[Integer,java.lang.String]()
    serverIp.put(1,"10.251.28.13:4711")
    serverIp.put(2,"10.104.67.88:4711")
  }


  val mysql_ip = "203.195.140.103"

  val cpds = new com.alibaba.druid.pool.DruidDataSource()
  cpds.setDriverClassName("com.mysql.jdbc.Driver")
  cpds.setUrl("jdbc:mysql://%s/?useUnicode=true&characterEncoding=UTF-8".format(mysql_ip))
  cpds.setUsername("root")
  cpds.setPassword("3kwan123")

  cpds.setMaxActive(20)
  cpds.setMinIdle(1)
  cpds.setMaxWait(60000)
  cpds.setInitialSize(1)


  initServer()
  startToolServer()

  def getConnection() ={
    try{
      cpds.getConnection
    }catch{
      case e:SQLException =>
        e.printStackTrace();
        null
    }
  }

  def startToolServer():Unit = {
    var br : BufferedReader = new BufferedReader(new InputStreamReader(System.in))
    while(true){
      println("输入命令:<start/stop/openServer(了解更多指令)>")
      try {
        val str : String = br.readLine().trim().toString
        if (str.equals("start")) {
          println("服务器正在运行")
        }
        else if(str.startsWith("clearAll")) {
          val ary : Array[String] = str.split("=")
          val areaKey = ary(1)
          var index = 10
          while (index>0){
            println("clearAll 即将清除掉 areaKey="+areaKey+"的数据，误操作请赶紧CTRL+C--------------"+index)
            index = index-1
            Thread.sleep(1000)
          }
          RedisDataClear.clearDbData(areaKey)
          RedisDataClear.clearSetData(areaKey)
        }else if(str.startsWith("clearLegion")){
          val ary : Array[String] = str.split("=")
          val areaKey = ary(1)
          var index = 10
          while (index>0){
            println("clearAll 即将清除掉 areaKey="+areaKey+"的数据，误操作请赶紧CTRL+C--------------"+index)
            index = index-1
            Thread.sleep(1000)
          }

        }else if(str.startsWith("clear")){
          val ary : Array[String] = str.split("=")
          val id = ary(1).toLong
          val name =ary(2)
          var index = 5
          while (index>0){
            println("clear 即将清除掉 id="+id+"，name="+name+"的数据，误操作请赶紧CTRL+C--------------"+index)
            index = index-1
            Thread.sleep(1000)
          }
          clearOneDbData(id,name)
        }else if(str.startsWith("allRole")){
          val ary : Array[String] = str.split("=")
          val areaKey = ary(1)
          printAllPlayers(areaKey)
        }else if(str.startsWith("fixClose")){
          implicit val system = ActorSystem("Admin")
          val ary : Array[String] = str.split("=")
          val areaId:Int= ary(1).toInt
          var index = 10
          while (index>0){
            println("fixClose 即将关闭服务器 areaId="+areaId+"，误操作请赶紧CTRL+C--------------"+index)
            index = index-1
            Thread.sleep(1000)
          }
          val actor = system.actorSelection("akka.tcp://game@"+serverIp.get(areaId)+"/user/root/adminServer")
          actor ! GMCommand("stop")
        }else if(str.startsWith("pillNode")){//pillNode=2=42=51
          implicit val system = ActorSystem("Admin")
          val ary : Array[String] = str.split("=")
          val areaId:Int= ary(1).toInt
          val x:Int = ary(2).toInt
          val y:Int = ary(3).toInt
          var index = 5
          while (index>0){
            println("fixClose 即将服务器节点 areaId="+areaId+",x="+x+",y="+y+"，误操作请赶紧CTRL+C--------------"+index)
            index = index-1
            Thread.sleep(1000)
          }
          val actor = system.actorSelection("akka.tcp://game@"+serverIp.get(areaId)+"/user/root/"+ActorDefine.AREA_SERVER_PRE_NAME+"1")
          actor ! GiveMeAPill(x,y)
        }else if(str.startsWith("updateSetFromRedisToMysql")){//updateSetFromRedisToMysql=游戏服areaKey=数据库表id（GcolGame9992的9992）
          val ary : Array[String] = str.split("=")
          val areaKey:String= ary(1)
          val dbId :Int = ary(2).toInt
          updateAllDbSetToMysql(areaKey,dbId)
        }
      }catch {
        case e: Exception => {
          e.printStackTrace()
        }
      }
    }
  }

//
//  def editData(id : Long,name:String,editName:String,editValue:String): Unit ={
//    val key = String.format("%s:[%d]",name,id)
//
//  }

  def getSetDbPojo[T <: BaseSetDbPojo](pojoClass: Class[T], areaKey: String): T = {
    val className: String = GameUtils.getClassName(pojoClass)
    val key: String = className + areaKey
    val pojo = pojoClass.newInstance
    try {
      pojo.setAreaKey(areaKey)
      initSetDbPojo(pojo,areaKey)
    }
    catch {
      case e: Exception => {
        e.printStackTrace
      }
    }
    return pojo
  }


  def printAllPlayers(areaKey : String): Unit ={
    val roleIdList = new util.HashSet[java.lang.Long]()
    roleIdList.addAll(getSetDbPojo(classOf[AccountNameSetDb], areaKey).getAllValue)
    for (id <- roleIdList){
      println(id)
    }
  }

  def clearOneDbData(id : java.lang.Long,name : java.lang.String): Unit ={
    val key = String.format("%s:[%d]",name,id)
    jc.del(key)
    println("清理完成"+key)
  }

  def updateAllDbSetToMysql(areaKey : String,dbId : Int): Unit ={
    updateDbSetToMysql(areaKey,dbId,classOf[AccountNameSetDb])
    updateDbSetToMysql(areaKey,dbId,classOf[ArenaLastRankSetDb])
    updateDbSetToMysql(areaKey,dbId,classOf[ArenaRankSetDb])
    updateDbSetToMysql(areaKey,dbId,classOf[ArenaReportSetDb])
    updateDbSetToMysql(areaKey,dbId,classOf[ArmGroupSetDb])
    updateDbSetToMysql(areaKey,dbId,classOf[BillOrderSetDb])
    updateDbSetToMysql(areaKey,dbId,classOf[HelpTeamDateSetDb])
    updateDbSetToMysql(areaKey,dbId,classOf[LegionDungeoTeamSetDb])
    updateDbSetToMysql(areaKey,dbId,classOf[LimitDungeonFastSetDb])
    updateDbSetToMysql(areaKey,dbId,classOf[LimitDungeonNearSetDb])
    updateDbSetToMysql(areaKey,dbId,classOf[NoticeDateSetDb])
    updateDbSetToMysql(areaKey,dbId,classOf[PlayerRankSetDb])
    updateDbSetToMysql(areaKey,dbId,classOf[RoleNameSetDb])
    updateDbSetToMysql(areaKey,dbId,classOf[SituationDateSetDb])
    updateDbSetToMysql(areaKey,dbId,classOf[TeamDateSetDb])
    updateDbSetToMysql(areaKey,dbId,classOf[VipActSetDb])
    updateDbSetToMysql(areaKey,dbId,classOf[WorldCloseSaveReward])
    updateDbSetToMysql(areaKey,dbId,classOf[WorldRewardSetDb])
    updateDbSetToMysql(areaKey,dbId,classOf[WorldTileSetDb])
  }

  def updateDbSetToMysql[T <: BaseSetDbPojo](areaKey : String,dbId : Int,pojoClass: Class[T]): Unit ={
    val setDb = getSetDbPojo(pojoClass, areaKey)
    val table: String = GameUtils.getClassName(pojoClass)
    val keys = setDb.getAllKey
    val sqlList = new  util.ArrayList[String]()
    var index = 0
    val allKeySize = keys.size()//存储总数量
    println("================开始保存"+table+"！"+index+"/"+allKeySize)
    for (key : String <- keys){
      index = index+1
      val value =setDb.getValueByKey(key)
      val formatStr = "replace into %s(set_key, set_value) values (\"%s\", "+value+");"
      val sql : String = formatStr.format("GcolGame"+dbId+"."+table, key)
      sqlList.add(sql)
      if(index % 100 == 0){
        //每一百条保存一下
        loadToMysql(sqlList)
        sqlList.clear()
        println("当前保存进度："+index+"/"+allKeySize)
        Thread.sleep(100)
      }
    }
    //保存到mysql
    loadToMysql(sqlList)
    println("当前保存进度："+index+"/"+allKeySize)
    Thread.sleep(1000)
    println("================保存"+table+"完成！"+index+"/"+allKeySize)
  }

  //保存到mysql的方法
  def loadToMysql(sqlList :util.ArrayList[String]): Unit ={
    val conn = getConnection()
    var statement: Statement = null
    try{
      statement = conn.createStatement()
      for (sql <- sqlList){
        statement.addBatch(sql)
      }
      statement.executeBatch()
    }catch{
      case e:Exception =>
        e.printStackTrace()
    }finally {
      if(statement != null){
        statement.close()
      }
      if(conn != null){
        conn.close()
      }
    }
  }

  def clearLegionData(areaKey : String): Unit ={
    val roleIdList = new util.HashSet[java.lang.Long]()
    roleIdList.addAll(getSetDbPojo(classOf[AccountNameSetDb], areaKey).getAllValue)
    val removeKeys = new util.ArrayList[String]
    for(roleId : java.lang.Long <- roleIdList){
      val player : Player = getWithoutlogAreaId(roleId,classOf[Player],areaKey)
      if(player != null){

      }
    }
  }

  def clearDbData(areaKey : String): Unit ={
    //清除玩家的数据
    val roleIdList = new util.HashSet[java.lang.Long]()
    roleIdList.addAll(getSetDbPojo(classOf[AccountNameSetDb], areaKey).getAllValue)
    val removeKeys = new util.ArrayList[String]
    for(roleId : java.lang.Long <- roleIdList){
      val player : Player = getWithoutlogAreaId(roleId,classOf[Player],areaKey)
      if(player != null){
        val userCla: Class[_] = player.getClass
        val fs = userCla.getDeclaredFields
        var index = 0
        while (index < fs.length){
          val method = fs(index)
          method.setAccessible(true) //设置些属性是可以访问的
          val obj : Object  = method.get(player);//得到此属性的值
          val fType:String = method.getType().toString()
          if (fType.endsWith("Set")) {
            var name = method.getName()
            if (name.endsWith("Set")){
              name = name.replace("1","").replace("2","").replace("3","").replace("Set","")
              name = name.substring(0, 1).toUpperCase() + name.substring(1)
              val value :util.Set[java.lang.Long] = obj.asInstanceOf[util.Set[java.lang.Long]]
              for (id <- value ){
                val key = String.format("%s:[%d]", name, id)
                removeKeys.add(key)
              }
            }
          }
          index = index +1
        }
        val key = String.format("%s:[%d]", "Player", player.getId)
        removeKeys.add(key)
      }
    }

    if (removeKeys.size() > 0){
      for (key <- removeKeys){
        println(key)
        jc.del(key)
      }
    }
  }


  def clearSetData(areaKey : String): Unit ={
    val classSet : util.Set[Class[_]] = GetClassUtil.getClasses("com.znl.pojo.db.set")
    for (classValue <- classSet) {
      try{
        removeAllDataByClass(classValue.asSubclass(classOf[BaseSetDbPojo]),areaKey)
      }catch {
        case e : Exception=>
          println(classValue.getSimpleName+" "+e)
      }
    }
    println("清除areaKey="+areaKey+" redis 数据完成！")
  }

  def removeAllDataByClass[T <: BaseSetDbPojo](pojoClass: Class[T], areaKey: String) {
    try {
      val className: String = GameUtils.getClassName(pojoClass)
      val key: String = className + areaKey
      var pojo = pojoClass.newInstance
      pojo.setAreaKey(areaKey)
      initSetDbPojo(pojo,areaKey)
      for (key <- pojo.getAllKey){
        val db_set_key = getSetDbPojoDbKey(pojo,areaKey)
        jc.zrem(db_set_key, key)
      }
    }
    catch {
      case e: Exception => {
        e.printStackTrace
      }
    }
  }

  def initSetDbPojo(setDbPojo: BaseSetDbPojo,areaKey :String) ={
    val db_set_key = getSetDbPojoDbKey(setDbPojo,areaKey)
    val set: util.Set[Tuple] = jc.zrangeWithScores(db_set_key, 0, 40000000000L)
    set.foreach(f => {
      setDbPojo.addKeyValue(f.getElement, f.getScore.toLong, true)
    })
  }

  def getSetDbPojoDbKey(setDbPojo: BaseSetDbPojo,areaKey :String) ={
    val className = GameUtils.getClassName(setDbPojo)
    val db_set_key = "gset:[%s]:[%s]".format(className, areaKey)
    db_set_key
  }

  /** ***为清库开的后门 ******/
  def getWithoutlogAreaId[T <: BaseDbPojo](id: Long, pojoClass: Class[T], areaKey: String): T = {
//    val result = DbProxy.getDbPojo(id, pojoClass).asInstanceOf[T]
    var pojo: Option[BaseDbPojo] = Some(pojoClass.newInstance().asInstanceOf[BaseDbPojo])
    pojo.get.setId(id)
    val key = pojo.get.getKey
    val map = jc.hgetAll(key)
    if (map.size() == 0) {
      pojo = None
    } else {
      map.foreach(e => {
        val key = e._1
        val value = e._2
        pojo.get.setter(key, value)
      })
    }
    return pojo.getOrElse(null).asInstanceOf[T]
  }

}
