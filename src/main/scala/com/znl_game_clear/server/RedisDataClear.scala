package com.znl_game_clear.server

import java.io.{InputStreamReader, BufferedReader}
import java.util

import akka.actor.ActorSystem
import com.znl.base.{BaseDbPojo, BaseSetDbPojo}
import com.znl.msg.GameMsg.{GMCommand, DelToMysql}
import com.znl.pojo.db.Player
import com.znl.pojo.db.set.AccountNameSetDb
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

  var br : BufferedReader = new BufferedReader(new InputStreamReader(System.in))
  while(true){
    println("输入命令:<start/stop/openServer(了解更多指令)>")
    try {
      var str : String = br.readLine().trim().toString
//      if (str.equals("start")) {
//        println("服务器正在运行")
//      }else if(str.startsWith("clearAll")){
//        val ary : Array[String] = str.split("=")
//        val areaKey = ary(1)
//        RedisDataClear.clearDbData(areaKey)
//        RedisDataClear.clearSetData(areaKey)
//      }else if(str.startsWith("clear")){
//        val ary : Array[String] = str.split("=")
//        val id = ary(1).toLong
//        val name =ary(2)
//        clearOneDbData(id,name)
//      }else if(str.startsWith("allRole")){
//        val ary : Array[String] = str.split("=")
//        val areaKey = ary(1)
//        printAllPlayers(areaKey)
//      }else if(str.startsWith("edit")) {
//        val ary: Array[String] = str.split("=")
//        val id = ary(1).toLong
//        val name = ary(2)
//        val editName = ary(3)
//        val editValue = ary(4)
//      }
      if(str.startsWith("fix")){
          implicit val system = ActorSystem("Admin")
          val actor = system.actorSelection("akka.tcp://game@10.251.28.13:4711/user/root/adminServer")
          actor ! GMCommand("stop")
      }else if(str.startsWith("fix2")) {
        implicit val system = ActorSystem("Admin")
        val actor = system.actorSelection("akka.tcp://game@10.104.67.88:4711/user/root/adminServer")
        actor ! GMCommand("stop")
      }


    }catch {
      case e: Exception => {
        e.printStackTrace()
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
