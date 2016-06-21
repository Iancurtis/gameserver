package com.znl.log

import akka.event.Logging
import com.znl.GameMainServer
import com.znl.base.BaseLog
import com.znl.utils.GameUtils
import org.json.JSONObject

/**
 * 日志传输接口
 * Created by Administrator on 2015/11/17.
 */

class FlumeLog{

}

object FlumeLog {
  var client = new MyRpcClientFacade()

  val log = Logging(GameMainServer.system, classOf[FlumeLog])

  def init(ip : String, port : Int) ={
//    client.init(ip, port)
  }

  //日志会被缓存起来，进行分析统计
  def log(logType : String, data : String) : Unit ={
//    client.sendDataToFlume(logType, data) 停止日志
    //TODO 这里会再一次写入本地，二次备份
    log.info(data)
  }

  def log(obj: BaseLog) : Unit ={
    obj.setLogTime(GameUtils.getServerTime)
    val logType = GameUtils.getClassName(obj)
    obj.setLogType(logType)
    val data = new JSONObject(obj)
    this.log(logType, data.toString)
  }
}
