package com.znl.pojo.db;

import com.znl.base.BaseDbPojo;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2015/11/16.
 */
public class Collect extends BaseDbPojo {
    private String name = "";
    private int icon = 0;
    private int level = 0;
    private int pointx = 0;
    private int pointy = 0;
    private int ower = 0;
    private Set<Integer> typelist = new HashSet<>();


    public int getOwer() {
        return ower;
    }

    public void setOwer(int ower) {
        this.ower = ower;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getPointx() {
        return pointx;
    }

    public void setPointx(int pointx) {
        this.pointx = pointx;
    }

    public int getPointy() {
        return pointy;
    }

    public void setPointy(int pointy) {
        this.pointy = pointy;
    }

    public Set<Integer> getTypelist() {
        return typelist;
    }

    public void setTypelist(Set<Integer> typelist) {
        this.typelist = typelist;
    }
}
