package com.znl.proxy

import java.io._
import java.util
import java.util.concurrent.ConcurrentHashMap

import com.znl.define.{PlayerPowerDefine, DataDefine, GameDefine}
import com.znl.utils.GameUtils
import org.json.JSONObject
import scala.collection.JavaConversions._
/**
 * Created by Administrator on 2015/10/29.
 */
object ConfigDataProxy {

  def loadAllConfig() = {
    this.map.clear()
    val map = GameUtils.getDataDefine(classOf[DataDefine])
    map.values.foreach(e =>
    {
      val name: String = e.asInstanceOf[String]
      reloadConfig(name)
    }
    )
  }

  def reloadConfig(name : String) ={
    getConfigData(name, true)
  }


  var map : util.Map[String, util.ArrayList[JSONObject]] =  new util.HashMap[String, util.ArrayList[JSONObject]]()

  def getConfigData(name : String, reload : Boolean = false) ={

    if(map.contains(name) == false || reload == true) {
      println("读取配置表："+name+"============================");
      val path = GameDefine.DEFINE_PATH + File.separator + name + ".json"
      val reader: InputStreamReader = new InputStreamReader(new FileInputStream(path), "utf-8" )
      val bufferedReader = new BufferedReader(reader)
      var line = ""
      val strBuilder = StringBuilder.newBuilder
      line = bufferedReader.readLine()
      while ( line != null ){
        strBuilder.append(line)
        line = bufferedReader.readLine()
      }
      val jsonObject = new JSONObject(strBuilder.toString())
      val keys = jsonObject.keys()
//      var values : util.Map[Integer, JSONObject] = new util.HashMap[Integer, JSONObject]
      val values : util.ArrayList[JSONObject] = new util.ArrayList[JSONObject]()
      keys.foreach( e =>
      {
        values.add(jsonObject.getJSONObject(e))
//        values += (e.toInt.asInstanceOf[Integer] -> jsonObject.getJSONObject(e))
      }
      )
      map.put(name, values)
    }

    map.get(name)

//    map.search()
  }

  //会返回多行数组 //TODO  应该break
  /*****
    *返回的是列表
    */
  def getConfigInfoFilterByField(name : String, fieldMap : java.util.Map[String, String]) ={

    val config = getConfigData(name)

    val infos = config.filter( e1 => {
      var flag = true
      fieldMap.foreach( e2 => {
        if(e1.get(e2._1).toString().equals(e2._2)){
        }else{
          flag = false;  //TODO  应该break
        }
      })

      flag
    }
    )

    new java.util.ArrayList[JSONObject](infos)
  }

    /*****
     *返回的是列表
     */
  def getConfigInfoFilterByOneKey(name : String, key : String, value : Any) ={
//    var map : Map[String, String] = Map()
//    map += (key -> value.toString)
//    getConfigInfoFilterByField(name, map)

      val jsonObjectList : util.ArrayList[JSONObject] = new util.ArrayList[JSONObject]()
      val config = getConfigData(name)
      val size: Int = config.size
      var i = 0
      while (i < size){
        val obj : JSONObject = config.get(i)
        val srcValue1 = obj.getInt(key).toString
        if(srcValue1.equals(value.toString)){
          jsonObjectList.add(obj)
        }

        i = i + 1
      }

      jsonObjectList
  }

  def getConfigInfoFilterById(name : String, value : Any) ={
//    var map : Map[String, String] = Map()
//    map += ("ID" -> value.toString)
//    getConfigInfoFilterByField(name, map)
    getConfigInfoFilterByOneKey(name, "ID", value)
  }

  ///////////////////////////////////////////////////////////////////////////

  //TODO 偶尔会某个查询超出10毫秒
  //摒弃掉该方法
//  def getConfigInfoFindByField(name : String, fieldMap : java.util.Map[String, String]) ={
//
//
//     val config = getConfigData(name)
//    val iter = fieldMap.iterator
//
//    val size: Int = config.size
//    var i = 0
//    while (i < size){
//
//      config.get(i)
//      i = i + 1
//    }
//
//    new JSONObject()
//
////    val info = config.find( e1 => {
////      var flag = true
////
////      fieldMap.foreach( e2 => {
////        if(e1.get(e2._1).toString().equals(e2._2)){
////        }else{
////          flag = false;  //TODO  应该break
////        }
////      })
////
////      flag
////    }
////    )
////
////    if(info.equals(None)){
////      null
////    }else{
////      info.get
////    }
//  }
  private val configJsonCacheMap: util.Map[String, JSONObject] = new util.HashMap[String, JSONObject]()
  def getConfigInfoFindByOneKey(name : String, key : String, value : Long) ={
    var jsonObject : JSONObject = null
    val cacheValueKey = key + value
    jsonObject = configJsonCacheMap.get(cacheValueKey)
    if(jsonObject == null){
      val config = getConfigData(name)
      val size: Int = config.size
      var i = 0
      while (i < size){
        val obj : JSONObject = config.get(i)
        val srcValue1 = obj.getLong(key)
        if(srcValue1.equals(value)){
          jsonObject = obj
          i = size
        }else{
          i = i + 1
        }
      }
    }
    jsonObject
  }

  def getConfigInfoFindByTwoKey(name : String, key1 : String, value1 : Long, key2 : String, value2 : Long) ={
    var jsonObject : JSONObject = null
    val cacheValueKey = key1 + value1 + key2 + value2
    jsonObject = configJsonCacheMap.get(cacheValueKey)
    if(jsonObject == null){
      val config = getConfigData(name)
      val size: Int = config.size
      var i = 0
      while (i < size){
        val obj : JSONObject = config.get(i)
        val srcValue1 = obj.getLong(key1) //.toString
        val srcValue2 = obj.getLong(key2)//.toString
        if(srcValue1.equals(value1) && srcValue2.equals(value2)){
          jsonObject = obj
          i = size
        }else{
          i = i + 1
        }
      }
    }

    jsonObject
  }


  def getConfigInfoFindByThreeKey(name : String, key1 : String, value1 : Long, key2 : String, value2 : Long,key3 : String, value3 : Long) ={

    var jsonObject : JSONObject = null
    val cacheValueKey = key1 + value1 + key2 + value2 + key3 + value3
    jsonObject = configJsonCacheMap.get(cacheValueKey)
    if(jsonObject == null){
      val config = getConfigData(name)
      val size: Int = config.size
      var i = 0
      while (i < size){
        val obj : JSONObject = config.get(i)
        val srcValue1 = obj.getLong(key1)
        val srcValue2 = obj.getLong(key2)
        val srcValue3 = obj.getLong(key3)
        if(srcValue1.equals(value1) && srcValue2.equals(value2)
          && srcValue3.equals(value3)){
          jsonObject = obj
          i = size
        }else{
          i = i + 1
        }
      }
    }


    jsonObject
  }

  /*返回一条数据*/
  def getConfigInfoFindById(name : String, value : Long) ={
//    var map : java.util.Map[String, String] = new java.util.HashMap[String, String]()
//    map += ("ID" -> value.toString)
//    getConfigInfoFindByField(name, map)
    getConfigInfoFindByOneKey(name, "ID", value)
  }

  /////////////////////////////////////////////////////////////
  def getConfigAllInfo(name : String) ={
    val config = getConfigData(name)
    config
  }

}
