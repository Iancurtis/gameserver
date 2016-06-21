package com.znl.server.actor.admin

import java.util

import com.znl.GameMainServer
import com.znl.base.BaseSetDbPojo
import com.znl.define.{ActorDefine, ChatAndMailDefine}
import com.znl.framework.http.HttpMessage
import com.znl.log.CustomerLogger
import com.znl.msg.GameMsg.SendMail
import com.znl.pojo.db.set.{AccountNameSetDb, RoleNameSetDb}
import com.znl.template.MailTemplate
import com.znl.utils.GameUtils

/**
 * 管理平台发送邮件
 * Created by Administrator on 2015/12/15.
 */
class AdminMailActor extends BasicAdminActor{

  override def onAdminActionMessage(httpMessage: HttpMessage): Unit = {

    val msg = httpMessage.getHttpRequest
    val parameterList = util.Arrays.asList("sendType", "mailTitle", "mailContent" ) //, "roleName" "accountName",
    val flag = super.checkParameter(httpMessage, parameterList)
    CustomerLogger.info("！！！！！接收到网络消息，发送邮件资源"+flag)
    //最后面都转成roleId
    //TODO 暂时只处理roleId
    if(flag){

      val server_id = msg.getParameter("server").toInt
      val areaKey = GameMainServer.getAreaKeyByAreaId(server_id)

      val roleIdList = new util.HashSet[java.lang.Long]()

      val sendType = msg.getParameter("sendType") //发送类型 1 角色名发送 2角色id发送 3账号名发送 5全服发送
      println()
      sendType match {
        case "1" => //1 角色名发送
          val roleNames = java.net.URLDecoder.decode(msg.getParameter("roleName"), "utf-8")
          CustomerLogger.info("玩家名称："+roleNames)
          roleNames.split(",").foreach( roleName => {
            val playerId = BaseSetDbPojo.getSetDbPojo(classOf[RoleNameSetDb], areaKey).getValueByKey(roleName)
            if(playerId != null){
              roleIdList.add(playerId)
            }else{
              //TODO
            }
          })
        case "2" => //角色id发送
          val roleId = msg.getParameter("roleId")
          CustomerLogger.info("玩家id："+roleId)
          roleId.split(",").foreach( id => {
            roleIdList.add(id.toLong)
          })
        case "3" => //3账号名发送
          val accountNames = java.net.URLDecoder.decode(msg.getParameter("accountName"), "utf-8")
          CustomerLogger.info("玩家账号："+accountNames)
          accountNames.split(",").foreach( accountName => {
            val playerId = BaseSetDbPojo.getSetDbPojo(classOf[AccountNameSetDb], areaKey).getValueByKey(accountName)
            if(playerId != null){
              roleIdList.add(playerId)
            }else{
              //TODO
            }
          })
        case "4" =>  //按类型
        //TODO
        case "5" => //全服
          roleIdList.addAll(BaseSetDbPojo.getSetDbPojo(classOf[AccountNameSetDb], areaKey).getAllValue)
          CustomerLogger.info("全服发送！！！")
        case "6" => //在线玩家
        //TODO
      }

      val mailTitle = java.net.URLDecoder.decode(msg.getParameter("mailTitle"), "utf-8")
      val mailContent = java.net.URLDecoder.decode(msg.getParameter("mailContent"), "utf-8")


      val template: MailTemplate = new MailTemplate(mailTitle, mailContent, 0, "系统邮件", ChatAndMailDefine.MAIL_TYPE_SYSTEM)
      sendMsgToService(context, server_id, ActorDefine.MAIL_SERVICE_NAME, SendMail(roleIdList, template, template.getSenderName, template.getSenderId))

      val result = GameUtils.getAdminStatusJsonMsg(0, "发送成功")
      httpMessage.sendContent(result)
    }
  }
}
