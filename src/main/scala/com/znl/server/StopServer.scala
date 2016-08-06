package com.znl.server

import java.util.Properties
import java.util.concurrent.ConcurrentHashMap

import akka.actor.Actor.Receive
import akka.actor.{Props, ActorLogging, Actor}
import com.znl.GameMainServer
import com.znl.define.ActorDefine
import com.znl.framework.http.base.GameHttpServer
import com.znl.framework.socket.base.GameNetServer
import com.znl.log.CustomerLogger
import com.znl.msg.GameMsg._
import com.znl.service.WorldService
import com.znl.utils.GameUtils
import scala.collection.JavaConversions._
/**
 * Created by Administrator on 2016/4/13.
 */

object StopServer {
  def props(gameNetServer : GameNetServer,gameHttpServer : GameHttpServer,areaKeyMap : ConcurrentHashMap[Int, String]) = Props(classOf[StopServer], gameNetServer,gameHttpServer,areaKeyMap)
}

class StopServer(gameNetServer : GameNetServer,gameHttpServer : GameHttpServer,areaKeyMap : ConcurrentHashMap[Int, String]) extends Actor with ActorLogging{
  override def receive: Receive = {
    case StopGame()=>
      GameMainServer.stopServer()
    case _=>

  }

  override def preStart() = {
    log.info("StopServer start")
//    stopServer()
  }


  def stopServer(): Unit ={
    //    println("服务器结束")
    //停服流程
    //1停止socket服务
    //2保存所有玩家数据
    //3数据全部保存完毕
    //4关闭程序

    //
    CustomerLogger.error("！！！开始停服操作！！！！切记：不要强制关闭进程！")
    //停止socket、http服务
    CustomerLogger.error("第1步：停止socket、http服务")
    gameNetServer.stop()  //客户端的socket全部断开，触发gateServer的onCloseSession方法
    gameHttpServer.stop()

    ////////////////////////////////////////////////////
    CustomerLogger.error("第2步：通知service保存！")
    Thread.sleep(2000)
    areaKeyMap.foreach( f => {
      val path = "../"+ActorDefine.AREA_SERVER_PRE_NAME+f._1+"/"+ActorDefine.ARMYGROUP_SERVICE_NAME
      context.actorSelection(path) ! saveDateBeforeStop()
    })
    areaKeyMap.foreach( f => {
      val path = "../"+ActorDefine.AREA_SERVER_PRE_NAME+f._1+"/"+ActorDefine.POWERRANKS_SERVICE_NAME
      context.actorSelection(path) ! saveDateBeforeStop()
    })
    areaKeyMap.foreach( f => {
      val path = "../"+ActorDefine.AREA_SERVER_PRE_NAME+f._1+"/"+ActorDefine.ARENA_SERVICE_NAME
      context.actorSelection(path) ! saveDateBeforeStop()
    })
    CustomerLogger.error("第2步：通知service保存完毕")
    ////////////////////////////////////////////////////

    CustomerLogger.error("第3步：关闭所有的野外世界节点！")
    Thread.sleep(2000)
    areaKeyMap.foreach( f => {
      val path = "../"+ActorDefine.AREA_SERVER_PRE_NAME+f._1+"/"+ActorDefine.WORLD_SERVICE_NAME
      context.actorSelection(path) ! StopAllNode()
    })
    Thread.sleep(2000)
    //检测节点是否已经全部关闭
    var worldNodeAllDone = true
    areaKeyMap.foreach( f => {
      val playerService = context.actorSelection("../"+ActorDefine.AREA_SERVER_PRE_NAME+f._1+"/"+ActorDefine.WORLD_SERVICE_NAME)
      val size : Int = GameUtils.futureAsk(playerService, AllNodeTeamBack(), 20)
      if (size > 0){
        worldNodeAllDone = false
      }
    })
    var index = 1
    while (!worldNodeAllDone){
      worldNodeAllDone = true
      Thread.sleep(3000)
      CustomerLogger.error("第3步：第%d次检测关闭所有的野外世界节点开始执行".format(index))
      areaKeyMap.foreach( f => {
        val playerService = context.actorSelection("../"+ActorDefine.AREA_SERVER_PRE_NAME+f._1+"/"+ActorDefine.WORLD_SERVICE_NAME)
        val size : Int = GameUtils.futureAsk(playerService, AllNodeTeamBack(), 20)
        if (size > 0){
          worldNodeAllDone = false
        }
        CustomerLogger.error("第3步：第%d次检测关闭所有的野外世界节点完毕".format(index))
      })
      index = index+1
    }
    CustomerLogger.error("第3步：野外奖励开始入库")
    WorldService.saveWorldCloseReward()
    CustomerLogger.error("第3步：关闭所有的野外世界节点完毕")
    index = 1
    ////////////////////////////////////////////////////
    //发送给GateServer通知，直接保存玩家 //onAutoClearOffPlayerData
    Thread.sleep(3000)
    CustomerLogger.error("第4步：通知GateServer直接保存所有玩家的数据")
    context.actorSelection("../"+ActorDefine.GATE_SERVER_NAME) ! AutoClearOffPlayerData() //直接触发保存玩家数据了

    Thread.sleep(1000)
    CustomerLogger.error("第4步：通知GateServer直接保存所有玩家的数据完毕")
    ////////////////////////////////////////////////////
    //调用清除所有玩家的simplePlayer
    CustomerLogger.error("第5步：开始清除并保存所有玩家的缓存数据！")
    areaKeyMap.foreach( f => {
      val path = "../"+ActorDefine.AREA_SERVER_PRE_NAME+f._1+"/"+ActorDefine.PLAYER_SERVICE_NAME
      println(path)
      context.actorSelection(path) ! StopServerSaveSimplePlayer()
    })
    //检测simplePlayer是否已经保存完成
    var allDone : Boolean = true
    areaKeyMap.foreach( f => {
      val playerService = context.actorSelection("../"+ActorDefine.AREA_SERVER_PRE_NAME+f._1+"/"+ActorDefine.PLAYER_SERVICE_NAME)
      val done : Boolean = GameUtils.futureAsk(playerService, CheckSaveSimplePlayerDone(), 20)
      if (done == false){
        allDone = false
      }
    })
    while (!allDone){
      allDone = true
      Thread.sleep(3000)
      CustomerLogger.error("第5步：第%d次检测清除并保存所有玩家的缓存数据开始执行".format(index))
      areaKeyMap.foreach( f => {
        val playerService = context.actorSelection("../"+ActorDefine.AREA_SERVER_PRE_NAME+f._1+"/"+ActorDefine.PLAYER_SERVICE_NAME)
        val done : Boolean = GameUtils.futureAsk(playerService, CheckSaveSimplePlayerDone(), 20)
        if (done == false){
          allDone = false
        }
        CustomerLogger.error("第5步：第%d次检测清除并保存所有玩家的缓存数据完毕".format(index))
      })
    }
    CustomerLogger.error("第5步：清除并保存所有玩家的缓存数据完毕")
    index = 1
    //////////////////////////////////////////////////////
    //循环检测dbServer是否已经全部入库完毕了
    CustomerLogger.error("第6步：第1次检测redis是否全部入库完毕")

    val dbServer = context.actorSelection("../"+ActorDefine.DB_SERVER_NAME)
    var count : Int = GameUtils.futureAsk(dbServer, IsDbQueueEmpty(), 20)
    while (count > 0){
      Thread.sleep(3000)
      count = GameUtils.futureAsk(dbServer, IsDbQueueEmpty(), 20)
      index = index + 1
      CustomerLogger.error("第6步：第%d次检测redis是否全部入库完毕:剩余数：%d".format(index, count))
    }

    CustomerLogger.error("第6步：redis全部入库完毕")

    //////////////////////////////////////////////////////
    CustomerLogger.error("第7步：第1次检测mysql是否全部入库完毕")
    index = 1
    //循环检测mysqlDb是否已经全部入库完毕了
    val mysqlServer = context.actorSelection("../"+ActorDefine.DB_SERVER_NAME + "/" + ActorDefine.MYSQL_ACTOR_NAME)
    count  = GameUtils.futureAsk(mysqlServer, IsDbQueueEmpty(), 20)
    while (count > 0){
      Thread.sleep(3000)
      count = GameUtils.futureAsk(mysqlServer, IsDbQueueEmpty(), 20)
      index = index + 1
      CustomerLogger.error("第7步：第%d次检测mysql是否全部入库完毕:剩余数:%d".format(index, count))
    }
    CustomerLogger.error("第7步：mysql全部入库完毕")

    ////////////////////////////////////////////
    CustomerLogger.error("第8步：第1次检测日志是否全部入库完毕")
    var allEmpty = true
    index = 1
    while (!allEmpty){
      areaKeyMap.foreach( f => {
        val adminLogService = context.actorSelection("../"+ActorDefine.AREA_SERVER_PRE_NAME+f._1 + "/" + ActorDefine.ADMIN_LOG_SERVICE_NAME)
        count = GameUtils.futureAsk(adminLogService, IsDbQueueEmpty(), 20)
        if(count >  0){
          allEmpty = false
        }
      })
      Thread.sleep(3000)
      CustomerLogger.error("第8步：第%d次检测日志是否全部入库完毕".format(index))
      index = index + 1
    }
    CustomerLogger.error("第8步：日志全部入库完毕")

    //////////////////////////////////////////////////////
    CustomerLogger.error("第9步：关闭system，关闭进程")
//    system.shutdown()
//    system.awaitTermination()
    System.exit(0)

    //////////////////////////////////////////////////////////////

//    system.registerOnTermination( new Runnable {
//      override def run(): Unit = {
//        println("registerOnTermination")
//      }
//    })

  }

}
