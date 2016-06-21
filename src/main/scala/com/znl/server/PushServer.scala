package com.znl.server

import akka.actor.{ActorLogging, Actor}
import akka.actor.Actor.Receive
import com.baidu.yun.core.log.{YunLogEvent, YunLogHandler}
import com.baidu.yun.push.auth.PushKeyPair
import com.baidu.yun.push.client.BaiduPushClient
import com.baidu.yun.push.constants.BaiduPushConstants
import com.baidu.yun.push.exception.PushClientException
import com.baidu.yun.push.model.{PushMsgToSingleDeviceRequest, PushMsgToAllRequest}
import com.znl.core.SimplePlayer
import com.znl.msg.GameMsg.PushMsgToPlayerDevice
import org.json.JSONObject

/**
 * 推送服
 * Created by Administrator on 2015/12/23.
 */
class PushServer extends Actor with ActorLogging{

  /*1. 创建PushKeyPair
         *用于app的合法身份认证
         *apikey和secretKey可在应用详情中获取
         */
  val apiKey = "4lfpIpcDmjXvafUFPeVvCsPN"
  val secretKey = "6NGG9G54C3LkHOl8zp8SoK6a1qEqIsqT"
  val pair = new PushKeyPair(apiKey, secretKey)

  // 2. 创建BaiduPushClient，访问SDK接口
  val pushClient = new BaiduPushClient(pair,
    BaiduPushConstants.CHANNEL_REST_URL)

  // 3. 注册YunLogHandler，获取本次请求的交互信息
  pushClient.setChannelLogHandler (new YunLogHandler () {
    @Override
    def onHandle ( event : YunLogEvent) {
      System.out.println(event.getMessage())
    }
  })

  override def receive: Receive = {
    case PushMsgToPlayerDevice(channelId : String, title : String, msg : String, time : Int) =>
      onPushMsgToPlayerDevice(channelId, title, msg, time)
    case _=>

  }

  //将消息推送到玩家的设备去
  //time 几时发送，time为0时，直接发送
  def onPushMsgToPlayerDevice(channelId : String, title : String, msg : String, time : Int) ={
    pushMsgToSingeDevice(channelId, title, msg)
  }

  //单播 先默认发送到Android平台
  def pushMsgToSingeDevice(channelId : String, title : String, msg : String) ={
    try{
      val json = new JSONObject()
      json.put("title", title)
      json.put("description", msg)
      json.put("isForegroundShow", false)
      //4. 设置请求参数，创建请求实例
      val request = new PushMsgToSingleDeviceRequest()
        .addChannelId(channelId)
        .addMsgExpires(3600) //设置消息的有效时间,单位秒,默认3600*5.
        .addMessageType(0) //设置消息类型,0表示透传消息,1表示通知,默认为0.
        .addMessage(json.toString)
        .addDeviceType(3) //3 for android, 4 for ios, 5 for wp.

     // 5. 执行Http请求
      val response = pushClient.pushMsgToSingleDevice(request)
      //6. Http请求返回值解析
      log.info("msgId: " + response.getMsgId()
        + ",sendTime: " + response.getSendTime())
    }catch{
      case e : Exception =>
//        e.printStackTrace()
    }
  }

  //发送到所有的设备
  def pushMsgToAllDevice(title : String, msg : String) ={
    try{
      val json = new JSONObject()
      json.put("title", title)
      json.put("description", msg)
      //4. 设置请求参数，创建请求实例
      val request = new PushMsgToAllRequest()
        .addMsgExpires(3600) //设置消息的有效时间,单位秒,默认3600*5.
        .addMessageType(1) //设置消息类型,0表示透传消息,1表示通知,默认为0.
        .addMessage(json.toString)
        .addDeviceType(3) //3 for android, 4 for ios, 5 for wp.

      // 5. 执行Http请求
      val response = pushClient.pushMsgToAll(request)
      //6. Http请求返回值解析
      log.info("msgId: " + response.getMsgId()
        + ",sendTime: " + response.getSendTime())
    }catch{
      case e : Exception =>
        e.printStackTrace()
    }
  }
}
