package com.znl.log;

import com.znl.base.BaseLog;

/**
 * 玩家登录数据
 * Created by Administrator on 2015/12/4.
 */
public class PlayerLogin extends BaseLog{
    private Long id;
    private String accountName;
    private String name;
    private Integer areaId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAreaId() {
        return areaId;
    }

    public void setAreaId(Integer areaId) {
        this.areaId = areaId;
    }

}
