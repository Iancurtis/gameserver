package com.znl.base;

import akka.actor.*;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.znl.GameMainServer;
import com.znl.define.ActorDefine;
import com.znl.log.CustomerLogger;
import com.znl.msg.GameMsg;
import com.znl.proxy.DbProxy;
import com.znl.server.DbServer;
import com.znl.utils.GameUtils;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2015/10/16.
 */
public class BaseDbPojo {

    private int logAreaId = 0;
    private Long id = 1L;

    //过期的时间戳，这个时间是跟着系统时间的
    private Integer expireAt = -1;

    private List<String> updateFieldList = new ArrayList<String>();

    @Override
    public void finalize(){
        DbProxy.finalizeDbPojo(this);
        DbProxy.tell(new GameMsg.FinalizeDbPojo(this), ActorRef.noSender());
        try {
            super.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public Object getter(String att){
        Object value = null;
        try
        {
            att = toUpperCaseFirstOne(att);
            Method method = this.getClass().getDeclaredMethod("get" + att);
            method.setAccessible(true);
            value = method.invoke(this);
        }catch (Exception e){
            e.printStackTrace();
        }

        return value;
    }

    public  void setter(String att, String value){
        try {
            updateFieldList.add(att);
            att = toUpperCaseFirstOne(att);

            Method[] methods = this.getClass().getDeclaredMethods();
            String methodName = "set" + att;
            for(Method method : methods){

                if(method.getName().equals(methodName)){
//                    System.out.println(method.getName());
                    Class<?>[] clazz = method.getParameterTypes();
                    Class cla = clazz[0];

                    method.setAccessible(true);
                    String claName = cla.getName();
                    if (claName == "java.lang.String"){//TODO 格式转化
                        method.invoke(this, value);
                    }else if(claName == "java.lang.Integer" || claName == "int" ) {
                        if(value.toString().length() == 0){
                            method.invoke(this, 0);
                        }else{
                            method.invoke(this, Integer.parseInt(value.toString()));
                        }
                    }else if(claName == "java.lang.Long" || claName == "long"){
                        if(value.toString().length() == 0){
                            method.invoke(this, 0);
                        }else{
                            method.invoke(this, Long.parseLong(value.toString()));
                        }
                    }else if(claName == "java.util.Set" || claName == "java.util.HashSet"){
                        Set<Long> set = GameUtils.str2set(value);
                        method.invoke(this,set );
                    }else if(claName == "java.util.List" || claName == "java.util.ArrayList"){
                        method.invoke(this, GameUtils.str2list(value));
                    }else if(claName == "[B"){
                        method.invoke(this, com.znl.framework.socket.websocket.Base64.decode(value));
                    }else {
                        method.invoke(this, cla.cast(value));
                    }

                }

            }

//            Method method = this.getClass().getDeclaredMethod("set" + att, value.getClass());
//            method.getParameterTypes();
//
//            method.setAccessible(true);
//            method.invoke(this, value);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void save(){
        DbServer.pushIntoSaveMap(this);//先推进去缓存起来
        DbProxy.tell(new GameMsg.SaveDBPojo(this), ActorRef.noSender());
    }

    /***
     * 删除对应的数据
     */
    public void del(){

        DbProxy.delDbPojo(this);
        DbProxy.tell(new GameMsg.DelDBPojo(this), ActorRef.noSender());

        String stack = GameUtils.getCallStatckString();
        CustomerLogger.info(this.getKey() + " : is be del----\n" + stack);

    }

    public void saveSuccess(){
        updateFieldList.clear();
    }

    //静态创建
    public static  <T extends BaseDbPojo>  T create( Class<T> pojoClass,String areaKey){ //int areaId,

        T result = null;
        try {

//            ActorSystem system = GameMainServer.system();
//            ActorSelection dbService = system.actorSelection(ActorDefine.DB_SERVER_PATH);
//
//            result = DbProxy.ask(new GameMsg.CreateDBPojo( pojoClass ), 5); //TODO 这里有可能会超时
            int logAreaId = GameMainServer.getLogAreaIdByAreaKey(areaKey);
            result = (T)DbProxy.createDbPojo(pojoClass,logAreaId);
            result.setLogAreaId(logAreaId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
//        dbService.tell(new GameMsg.CreateDBPojo(this), ActorRef.noSender());
    }


    /*******注意！此方法只在proxy和一些特定情况调用********/
    public static  <T extends BaseDbPojo>  T get( Long id, Class<T> pojoClass,String areaKey){ //int areaId,
        T result = null;
        if(id == null){
            return result;
        }
        try {
            int logAreaId = GameMainServer.getLogAreaIdByAreaKey(areaKey);
            result = (T)DbProxy.getDbPojo(id, pojoClass, true);
            if (result != null){
                result.setLogAreaId(logAreaId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /****获得离线玩家相关数据******/
    public static <T extends BaseDbPojo>  T getOfflineDbPojo( Long id, Class<T> pojoClass,String areaKey){
        T result = null;
        if(id == null){
            return result;
        }
        try {
            int logAreaId = GameMainServer.getLogAreaIdByAreaKey(areaKey);
            result = (T)DbProxy.getDbPojo(id, pojoClass, false);
            if (result != null){
                result.setLogAreaId(logAreaId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private String toUpperCaseFirstOne(String s){
        return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
    }

    public String getClassName(){
        return GameUtils.getClassName(this);
    }

    /**
     * 唯一KEY
     * className + areaId + id
     * @return
     */
    public String getKey(){
        String key = String.format("%s:[%d]", getClassName(), id);  //areaId
        return  key;
    }

    /**
     * 组最大ID KEY
     * className + areaId
     * @return
     */
    public String getGroupIdKey(){
        String key = String.format("%s:id", getClassName()); //areaId
        return  key;
    }

    /**
     * 组ID集合KEY
     *
     * @return
     */
    public String getGroupIdSetKey(){
        String key = String.format("%s:id.set", getClassName()); //areaId
        return  key;
    }

    public List<String> getUpdateFieldList() {
        return updateFieldList;
    }

    public List<String> getFieldNameList(){
        List<String> list = new ArrayList<String>();
        Field[] fields = this.getClass().getDeclaredFields();
        for(Field field : fields ){
            String name = field.getName();
            if(name != "id"){  //&& name != "areaId"
                list.add(name);
            }
        }


        return list;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Integer expireAt) {
        this.expireAt = expireAt;
    }

    public int getLogAreaId() {
        return logAreaId;
    }

    public void setLogAreaId(int logAreaId) {
        this.logAreaId = logAreaId;
    }
}
