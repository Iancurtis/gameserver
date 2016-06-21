package com.znl.server

import akka.actor.Actor
import com.mchange.v2.c3p0.ComboPooledDataSource
import com.znl.base.BaseLog
import com.znl.log.FlumeLog
import com.znl.msg.GameMsg.SendLog
import com.znl.utils.GameUtils
import org.json.JSONObject

/**
 * Created by Administrator on 2015/12/5.
 */

object LogServer{

}

class LogServer extends Actor{

  override def receive: Receive = {
    case SendLog(obj) =>
      onSendLog(obj)
    case _=>

  }

  def onSendLog(obj : BaseLog) ={
    FlumeLog.log(obj)
  }


}
