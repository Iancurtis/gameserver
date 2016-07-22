package com.znl.base;

import com.znl.GameMainServer;
import com.znl.log.CustomerLogger;
import com.znl.msg.GameMsg;
import com.znl.proxy.DbProxy;
import com.znl.server.DbServer;
import com.znl.utils.GameUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 初始化服务时，（开服），都会初始化一遍
 * 全服的集合基础类
 * 一个集合类型，全服只会存在一个实例
 * 只是作为一个工具类的包装而已
 * 集合默认会对应一个BaseBdPojo类
 * Created by Administrator on 2016/1/14.
 */
public class BaseSetDbPojo {

    private static Map<String, BaseSetDbPojo> baseSetMap = new ConcurrentHashMap<>();



    public static <T extends BaseSetDbPojo> T getSetDbPojo(Class<T> pojoClass, String areaKey) {
        try {
            String className = GameUtils.getClassName(pojoClass);
            String key = className + areaKey;
            T pojo = null;
            if (baseSetMap.containsKey(key)) {
                pojo = (T) baseSetMap.get(key);
            } else {
                pojo = pojoClass.newInstance();
                pojo.setAreaKey(areaKey);
                pojo.init();
                baseSetMap.put(key, pojo);
            }
            return pojo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private String areaKey = "";

    public int getLogAreaId(){
        int logAreaId = GameMainServer.getLogAreaIdByAreaKey(areaKey);
        return logAreaId;
    }

    protected Map<String, Long> setMap = new ConcurrentHashMap<>();

    public void init() { //初始化
        //最好用ask，开服只调用一次
        DbServer.onInitSetDbPojo(this);
//        DbProxy.ask(new GameMsg.InitSetDbPojo(this), 30);
    }

    /**
     * 添加一个键值
     *
     * @param key
     * @param value
     */

    public void addKeyValue(String key, Long value) {
        this.addKeyValue(key, value, false);
    }

    public void addKeyValue(String key, Long value, Boolean isInit) {
        setMap.put(key, value);
        if (!isInit) {
            this.updateElementToDb(key, value);
        }
    }

    //返回Null则不存在
    //谨记！！该集合类对应的DbPojo只能由开发者自身去管理，谨慎！
    public <T extends BaseDbPojo> T getDbPojoByKey(String key, Class<T> pojoClass) {
        Long id = getValueByKey(key);
        T pojo = BaseDbPojo.get(id, pojoClass,areaKey);

        return pojo;
    }

    //创建一个set对应的dbPojo
    //慎用
    public <T extends BaseDbPojo> T createDbPojo(String key, Class<T> pojoClass) {
        //TODO 这里可能需要再去校验一下
        Long value = getValueByKey(key);
        if (value != null) {
            CustomerLogger.error("!!!!!!!!!!要创建的dbPojo已经有了!!!!!!!!!!!!!!!!" + key);
        } else {
            T pojo = BaseDbPojo.create(pojoClass,areaKey);
            this.addKeyValue(key, pojo.getId());
            return pojo;
        }

        return null;
    }

    public Long getValueByKey(String key) {
        return setMap.get(key);
    }

    //模糊搜索Value
    public Set<Long> getValueByKeyVague(String key){
        Set<Long> set = new HashSet<Long>();

        Iterator<Map.Entry<String, Long>> it = setMap.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String, Long> next = it.next();
            if(next.getKey().indexOf(key) >= 0){
                set.add(next.getValue());
            }
        }

        return set;
    }

    public List<Long> getValueList(List<String> keyList) {
        List<Long> valueList = new ArrayList<>();
        keyList.forEach(key -> valueList.add(getValueByKey(key)));
        return valueList;
    }

    //TODO 进行优化
    public String getKeyByValue(Long value) {
        String key = null;
        Iterator<Map.Entry<String, Long>> it = setMap.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String, Long> next = it.next();
            if(next.getValue().equals(value)){
                key = next.getKey();
                break;
            }
        }
        return key;
    }

    //如果newKey没有对应的值，替换后，oldKey也没有对应的值了
    public void replaceKeyValue(String oldKey, String newKey, Long newValue) {
//        Long oldValue = setMap.get(oldKey);
        Long tempNewValue = setMap.get(newKey);
        this.addKeyValue(newKey, newValue);
        if (tempNewValue == null) {
            this.delElementToDb(oldKey);
        } else {
            this.addKeyValue(oldKey, tempNewValue);
        }
    }

    public void removeKey(String key) {
        this.delElementToDb(key);
    }

    //key值，是否存在
    public boolean isKeyExist(String key) {
        return setMap.containsKey(key);
    }

    public boolean isValueExist(Long value) {
        return setMap.containsValue(value);
    }

    //获取所有的Key
    public Set<String> getAllKey() {
        return setMap.keySet();
    }

    public int getSize() {
        return setMap.size();
    }

    //清除所有key
    public void removeAllKey() {
        Set<String> allkey = getAllKey();
        for (String str : allkey) {
            removeKey(str);
        }
    }

    //获取所有的Value setMap.values()
    public List<Long> getAllValue() {
        Long[] array = new Long[setMap.size()];
        array = setMap.values().toArray(array);
        List<Long> valueList = Arrays.asList(array);
        return valueList;
    }

    private void updateElementToDb(String key, Long value) {
        DbProxy.tell(new GameMsg.UpdateSetDbPojoElement(this, key, value));
    }

    public void updateAllElmentToDb(){
        for (String key : setMap.keySet()){
            updateElementToDb(key,setMap.get(key));
        }
    }

    private void delElementToDb(String key) {
        setMap.remove(key);
        DbProxy.tell(new GameMsg.DelSetDbPojoElement(this, key));
    }

    public String getClassName(){
        return GameUtils.getClassName(this);
    }

    public String getAreaKey() {
        return areaKey;
    }

    public void setAreaKey(String value) {
        this.areaKey = value;
    }
}
