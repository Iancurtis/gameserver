package com.znl.test;

import com.alibaba.fastjson.JSON;
import com.google.gson.JsonObject;
import com.sun.xml.internal.rngom.parse.host.Base;
import com.znl.base.BaseDbPojo;
import com.znl.base.BaseSetDbPojo;
import com.znl.pojo.db.Player;
import com.znl.pojo.db.set.AccountNameSetDb;
import com.znl.utils.GameUtils;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Tuple;
import scala.Array;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by Administrator on 2016/8/11.
 */
public class DataHandler {
    private static String redisIp = "192.168.10.190";
    private static int redisPort = 7001;
    private static  Set<HostAndPort> jedisClusterNodes= new HashSet<HostAndPort>();
    private static JedisCluster jc =new JedisCluster(jedisClusterNodes);


    public static void main(String[]args){
         initRedis();
         getDbData("9989");
        /*user u=new user();
        u.setId(3);
        u.setName("dddd");
        String json=JSON.toJSONString(u);
        System.err.println(json);

        user d=JSON.parseObject(json,user.class);
        System.err.println(d.getAge());*/
    }

    private static void initRedis(){
        jedisClusterNodes.add(new HostAndPort(redisIp, redisPort));
        jc =new JedisCluster(jedisClusterNodes);
    }



    private static void  redis2Mysql(){
        String[] areaServer3 = GameUtils.getAreaServer3();
        //首先遍历player，把player转换出pojo
    }


    private static Map<String,StringBuilder>updateMap=new HashMap<>();


   private static void getDbData(String areaKey){
       try {
           Set<Long>roleIdList=new HashSet<>();
           AccountNameSetDb setDbPojo = getSetDbPojo(AccountNameSetDb.class, areaKey);
           roleIdList.addAll(setDbPojo.getAllValue());
           for(long rid:roleIdList){
               Player player=getWithoutlogAreaId(rid,Player.class,areaKey);
               if(player==null)continue;

               StringBuilder sqlStr=updateMap.get(player.getClassName());
               if(sqlStr==null){
                   sqlStr=new StringBuilder();
               }
               //sqlStr.append(new JsonObject(player.converFils2RedisDataMap()).toString());

               System.err.println(JSON.toJSONString(player));

               Field[] declaredFields = player.getClass().getDeclaredFields();
               for(Field field:declaredFields){
                   field.setAccessible(true);
                   Object value=field.get(player);
                   String fType = field.getType().toString();
                   String name = field.getName();
                   if (fType.endsWith("Set")) {
                       if (name.endsWith("Set")) {
                           name = name.replace("1", "").replace("2", "").replace("3", "").replace("Set", "");
                           name = name.substring(0, 1).toUpperCase() + name.substring(1);
                           Set<Long>realVal=(HashSet<Long>)value;
                           for (Long id :realVal ){
                              // String key = String.format("%s:[%d]", name, id)
                               String key = name+":["+id+"]";
                               System.err.println("PlayerId:"+player.getId()+" "+player.getName()+"--------------->key:"+key+"===value:"+ JSON.toJSONString(jc.hgetAll(key)));
                           }
                       }
                   }
               }
               System.err.println(player.getName());
           }
       } catch (IllegalAccessException e) {
           e.printStackTrace();
       }
   }


/*
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
                // jc.del(key)
            }
        }
    }
*/


    public static  <T extends BaseSetDbPojo> T getSetDbPojo(Class<T> pojoClass,String areaKey){
        try {
            T pojo = pojoClass.newInstance();
            String className= GameUtils.getClassName(pojoClass);
            pojo.setAreaKey(areaKey);
            initSetDbPojo(pojo,areaKey);
            return pojo;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void initSetDbPojo(BaseSetDbPojo setDbPojo,String areaKey){
      String db_set_key=  getSetDbPojoDbKey(setDbPojo,areaKey);
        Set<Tuple>set=jc.zrangeWithScores(db_set_key, 0, 40000000000L);
        for(Tuple t:set){
            setDbPojo.addKeyValue(t.getElement(), (long)t.getScore(), true);
        }
    }

    private static String  getSetDbPojoDbKey(BaseSetDbPojo setDbPojo,String areaKey){
       String  className = GameUtils.getClassName(setDbPojo);
        //String db_set_key="gset:[%s]:[%s]".format(className, areaKey);
        String db_set_key="gset:["+className+"]:["+areaKey+"]";
        return db_set_key;
    }


    public static <T extends BaseDbPojo> T getWithoutlogAreaId(long id,Class<T> pojoClass, String areaKey){
        try {
            T pojo= pojoClass.newInstance();
            pojo.setId(id);
            String key=pojo.getKey();
            Map<String, String> map = jc.hgetAll(key);
            if(map.isEmpty()){
                pojo=null;
            }else{
                for(String k:map.keySet()){
                    String value=map.get(k);
                    pojo.setter(k,value);
                }
            }
            return pojo;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return  null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }




 /*   def initSetDbPojo(setDbPojo: BaseSetDbPojo,areaKey :String) ={
        val db_set_key = getSetDbPojoDbKey(setDbPojo,areaKey)
        val set: util.Set[Tuple] = jc.zrangeWithScores(db_set_key, 0, 40000000000L)
        for(t: Tuple<-set){
            setDbPojo.addKeyValue(t.getElement, t.getScore.toLong, true)
        }
        *//**//**//**//* set.foreach(f => {
        setDbPojo.addKeyValue(f.getElement, f.getScore.toLong, true)
    })*//**//**//**//*
    }*/

    /*def getSetDbPojo T ]<: BaseSetDbPojo](pojoClass: Class[T], areaKey: String): T = {
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

    def initSetDbPojo(setDbPojo: BaseSetDbPojo,areaKey :String) ={
        val db_set_key = getSetDbPojoDbKey(setDbPojo,areaKey)
        val set: util.Set[Tuple] = jc.zrangeWithScores(db_set_key, 0, 40000000000L)
        for(t: Tuple<-set){
            setDbPojo.addKeyValue(t.getElement, t.getScore.toLong, true)
        }
   *//* set.foreach(f => {
      setDbPojo.addKeyValue(f.getElement, f.getScore.toLong, true)
    })*//*
    }


    def getSetDbPojoDbKey(setDbPojo: BaseSetDbPojo,areaKey :String) ={
        val className = GameUtils.getClassName(setDbPojo)
        val db_set_key = "gset:[%s]:[%s]".format(className, areaKey)
        db_set_key
    }



    *//** ***为清库开的后门 ******//*
    def getWithoutlogAreaId[T <: BaseDbPojo](id: Long, pojoClass: Class[T], areaKey: String): T = {
        //    val result = DbProxy.getDbPojo(id, pojoClass).asInstanceOf[T]
        var pojo: Option[BaseDbPojo] = Some(pojoClass.newInstance().asInstanceOf[BaseDbPojo])
        pojo.get.setId(id)
        val key = pojo.get.getKey
        val map = jc.hgetAll(key)
        if (map.size() == 0) {
            pojo = None
        } else{
            for(key:String<-map.keySet()) {
                val value=map.get(key)
                pojo.get.setter(key, value)
            }
    *//*  map.foreach(e => {
        val key = e._1
        val value = e._2
        pojo.get.setter(key, value)
      })*//*
        }
        return pojo.getOrElse(null).asInstanceOf[T]
    }*/
}
