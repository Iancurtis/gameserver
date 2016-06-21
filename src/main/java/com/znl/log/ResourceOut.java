package com.znl.log;

import com.znl.base.BaseLog;

/**
 * Created by Administrator on 2015/12/14.
 *
 * 资源产出
 */
public class ResourceOut extends BaseLog {
    private Long id;
    private int power;
    private int value;

    public ResourceOut(int power, int value) {
        this.power = power;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
