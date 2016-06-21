package com.znl.server.actor.admin

import java.nio.charset.Charset
import java.util

import com.znl.GameMainServer
import com.znl.base.{BaseDbPojo, BaseSetDbPojo}
import com.znl.define.ActorDefine
import com.znl.framework.http.HttpMessage
import com.znl.msg.GameMsg.QueryToMysql
import com.znl.pojo.db.Player
import com.znl.pojo.db.set.{AccountNameSetDb, RoleNameSetDb}
import com.znl.proxy.DbProxy
import com.znl.utils.GameUtils
import org.json.JSONObject
import scala.collection.JavaConversions._

/**
 * Created by Administrator on 2016/1/20.
 */
class AdminUserInfoListActor extends BasicAdminActor{
  override def onAdminActionMessage(httpMessage: HttpMessage): Unit = {
    val msg = httpMessage.getHttpRequest
    val parameterList = util.Arrays.asList("server") //todo
    val flag = super.checkParameter(httpMessage, parameterList)
    if(flag){
      val actor = context.actorSelection(ActorDefine.DB_SERVER_PATH + "/" + ActorDefine.MYSQL_ACTOR_NAME)

      val server_id = msg.getParameter("server").toInt
//      val countSql = "select count(id) from Player where json_extract(data, '$.areaId') = %d".format(server_id)
//      val countSqlList : util.ArrayList[String] = GameUtils.futureAsk(actor, QueryToMysql(countSql), 30)
//      val totalNum = countSqlList.get(0).toInt

      var pageNumStr = msg.getParameter("pageNum")
      if(pageNumStr == null || pageNumStr.equals("")){
        pageNumStr = "1"
      }
      val pageNum = pageNumStr.toInt

      var pageSizeStr = msg.getParameter("pageSize")
      if(pageSizeStr == null || pageSizeStr.equals("")){
        pageSizeStr = "5"
      }
      var pageSize = pageSizeStr.toInt
      if(pageSize > 15){
        pageSize = 15
      }
      if(pageSize < 1){
        pageSize = 1
      }

      val start = (pageNum - 1) * pageSize

      //TODO 直接查redis
      val roleName = java.net.URLDecoder.decode(msg.getParameter("roleName"), "utf-8")
      val roleId = java.net.URLDecoder.decode(msg.getParameter("roleId"), "utf-8")
      val accountName = java.net.URLDecoder.decode(msg.getParameter("accountName"), "utf-8")


      val playerIdList = new util.ArrayList[Long]()
      val areaKey = GameMainServer.getAreaKeyByAreaId(server_id)

      var condition = false
      if(roleId != null && roleId.length > 0 ){
        val accountNameSetDb = BaseSetDbPojo.getSetDbPojo(classOf[AccountNameSetDb], areaKey)
        val key = accountNameSetDb.getKeyByValue(roleId.toLong)
        if(key != null){
          playerIdList.add(roleId.toLong)
        }
        condition = true
      }

      if(accountName != null && accountName.length > 0 && condition == false){
        val accountNameSetDb = BaseSetDbPojo.getSetDbPojo(classOf[AccountNameSetDb], areaKey)
        val id = accountNameSetDb.getValueByKey(accountName)
        if(id != null){
          playerIdList.add(id)
        }
        condition = true
      }

      if(roleName != null && roleName.length > 0 && condition == false){
        val roleNameSetDb = BaseSetDbPojo.getSetDbPojo(classOf[RoleNameSetDb], areaKey)
        val idSet = roleNameSetDb.getValueByKeyVague(roleName)
        idSet.foreach( id => playerIdList.add(id) )
        condition = true
      }

      val roleNameSetDb = BaseSetDbPojo.getSetDbPojo(classOf[RoleNameSetDb], areaKey)
      val totalNum = roleNameSetDb.getSize

      //没有条件，将所有的值都导入
      if(!condition){

        roleNameSetDb.getAllValue.foreach( id => playerIdList.add(id))
      }

      //分页处理
      val resultPlayerIdList =  new util.ArrayList[Long]()
      for(index <- start until (start + pageSize)){
        if (index < playerIdList.size()){
          resultPlayerIdList.add(playerIdList.get(index))
        }
      }

      val resultList : util.ArrayList[JSONObject] = new util.ArrayList[JSONObject]()
      resultPlayerIdList.foreach( playerId => {
        val player = BaseDbPojo.getOfflineDbPojo(playerId,classOf[Player],areaKey)
        val jsonObject = new JSONObject()
        jsonObject.put("roleId", player.getId)
        jsonObject.put("roleName", player.getName )
        jsonObject.put("accountName", player.getAccountName )
        jsonObject.put("regTime", player.getRegTime)  //TODO 创角时间
        jsonObject.put("level", player.getLevel)
        jsonObject.put("lastLoginTime", player.getLastLoginTime)  ////TODO 最后登陆时间

        resultList.add(jsonObject)

        player.finalize()
      })

      val descJson = new JSONObject()
      descJson.put("roleId" , "角色ID")
      descJson.put("roleName", "角色名")
      descJson.put("accountName", "账号名称")
      descJson.put("regTime", "注册时间")
      descJson.put("level", "等级")
       descJson.put("lastLoginTime", "最后登录时间")

      val resultObj = new JSONObject()
      resultObj.put("ret", 0)
      resultObj.put("msg", "成功")
      resultObj.put("desc", descJson)
      resultObj.put("data", resultList)
      resultObj.put("totalNum", totalNum )

      val resultString = resultObj.toString
      httpMessage.sendContent(resultString)




//      var where = ""
//      if(roleName != null && roleName.length > 0){
//        where += "and json_extract(data, '$.name') like '%" + roleName + "%'"
//      }
//      if(accountName != null && accountName.length > 0){
//        where += "and json_extract(data, '$.accountName') = '%s'".format(accountName)
//      }
//      if(roleId != null && roleId.length > 0){
//        where += "and id = %s".format(roleId)
//      }
//
//      val sql = "select data from Player where json_extract(data, '$.areaId') = '%d' %s limit %d, %d".format(server_id, where, start, pageSize)
//
//
//      val list : util.ArrayList[String] = GameUtils.futureAsk(actor, QueryToMysql(sql), 30)
//
//      val resultList : util.ArrayList[JSONObject] = new util.ArrayList[JSONObject]()
//      list.foreach( result => {
//        System.out.println("===============:" + result)
//        val jsonObj = new JSONObject(result)
//        val jsonObject = new JSONObject()
//        jsonObject.put("roleId", jsonObj.getLong("id"))
//        jsonObject.put("roleName", java.net.URLDecoder.decode(jsonObj.getString("name"), "utf-8") )
//        jsonObject.put("accountName", jsonObj.getString("accountName") )
//        jsonObject.put("regTime", jsonObj.optInt("regTime"))  //TODO 创角时间
//        jsonObject.put("level", jsonObj.getInt("level"))
//        jsonObject.put("lastLoginTime", jsonObj.optInt("lastLoginTime"))  ////TODO 最后登陆时间
//
//        resultList.add(jsonObject)
//      })
//
//      val descJson = new JSONObject()
//      descJson.put("roleId" , "角色ID")
//      descJson.put("roleName", "角色名")
//      descJson.put("accountName", "账号名称")
//      descJson.put("regTime", "注册时间")
//      descJson.put("level", "等级")
//      descJson.put("lastLoginTime", "最后登录时间")
//
//      val resultObj = new JSONObject()
//      resultObj.put("ret", 0)
//      resultObj.put("msg", "成功")
//      resultObj.put("desc", descJson)
//      resultObj.put("data", resultList)
//      resultObj.put("totalNum", totalNum )
//
//      val resultString = resultObj.toString
//      httpMessage.sendContent(resultString)

    }
  }
}
