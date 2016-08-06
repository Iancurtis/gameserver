package com.znl.server.action;

import java.util.Map;

/**
 * Db行为操作
 * Created by Administrator on 2015/12/18.
 */
public class DbAction {

    private Long id;
    private String pojoClassName;
    private DbActionType type;
    private String key;
    private Map<String, String> map;
    private Integer expireAt = -1;//过期时间，时间戳
    private Integer expire= -1;//过期时间s

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPojoClassName() {
        return pojoClassName;
    }

    public void setPojoClassName(String pojoClassName) {
        this.pojoClassName = pojoClassName;
    }

    public DbActionType getType() {
        return type;
    }

    public void setType(DbActionType type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

    public Integer getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Integer expireAt) {
        this.expireAt = expireAt;
    }

    public Integer getExpire() {return expire;}

    public void setExpire(Integer expire) {this.expire = expire;}
}
