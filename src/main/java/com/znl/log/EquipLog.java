package com.znl.log;

import com.znl.base.BaseLog;

/**
 * Created by Administrator on 2015/12/15.
 */
public class EquipLog extends BaseLog {
    private Long id;
    private int opetype;
    private long equId;
    private int typeId;
    private int position;
    private int newlevel;
    private int oldlevel;
    private int oldposition;
    private int addSize;//
    private int addSizeCost;//

    public EquipLog(int opetype, long equId, int typeId, int position, int newlevel, int oldlevel) {
        this.opetype = opetype;
        this.equId = equId;
        this.typeId = typeId;
        this.position = position;
        this.newlevel = newlevel;
        this.oldlevel = oldlevel;
    }

    public EquipLog(){

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getEquId() {
        return equId;
    }

    public void setEquId(long equId) {
        this.equId = equId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getOpetype() {
        return opetype;
    }

    public void setOpetype(int opetype) {
        this.opetype = opetype;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getNewlevel() {
        return newlevel;
    }

    public void setNewlevel(int newlevel) {
        this.newlevel = newlevel;
    }

    public int getOldlevel() {
        return oldlevel;
    }

    public void setOldlevel(int oldlevel) {
        this.oldlevel = oldlevel;
    }

    public int getOldposition() {
        return oldposition;
    }

    public void setOldposition(int oldposition) {
        this.oldposition = oldposition;
    }

    public int getAddSize() {
        return addSize;
    }

    public void setAddSize(int addSize) {
        this.addSize = addSize;
    }

    public int getAddSizeCost() {
        return addSizeCost;
    }

    public void setAddSizeCost(int addSizeCost) {
        this.addSizeCost = addSizeCost;
    }
}
