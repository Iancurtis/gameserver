package com.znl.log;

import com.znl.base.BaseLog;

/**
 * Created by Administrator on 2015/12/15.
 */
public class OrdnanceLog extends BaseLog {
    private Long id;
    private int opetype;
    private long ordanceId;//军械ID
    private int piceTypeId;//碎片类型
    private int picenum;//碎片数量
    private int ordanceTypeId;//军械类型
    private int strLv;//强化等级
    private int gzLv;//改造等级
    private int adTypeId;//进化后的军械
    private int falg;//0fasle 1true
    private int useItem;//0fasle 1true

    public OrdnanceLog(int opetype){
        this.opetype=opetype;
    }
    public OrdnanceLog(){

    }

    public OrdnanceLog(long ordanceId, int opetype, int ordanceTypeId, int strLv, int gzLv) {
        this.ordanceId = ordanceId;
        this.opetype = opetype;
        this.ordanceTypeId = ordanceTypeId;
        this.strLv = strLv;
        this.gzLv = gzLv;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getOpetype() {
        return opetype;
    }

    public void setOpetype(int opetype) {
        this.opetype = opetype;
    }

    public long getOrdanceId() {
        return ordanceId;
    }

    public void setOrdanceId(long ordanceId) {
        this.ordanceId = ordanceId;
    }

    public int getPiceTypeId() {
        return piceTypeId;
    }

    public void setPiceTypeId(int piceTypeId) {
        this.piceTypeId = piceTypeId;
    }

    public int getPicenum() {
        return picenum;
    }

    public void setPicenum(int picenum) {
        this.picenum = picenum;
    }

    public int getOrdanceTypeId() {
        return ordanceTypeId;
    }

    public void setOrdanceTypeId(int ordanceTypeId) {
        this.ordanceTypeId = ordanceTypeId;
    }

    public int getStrLv() {
        return strLv;
    }

    public void setStrLv(int strLv) {
        this.strLv = strLv;
    }

    public int getGzLv() {
        return gzLv;
    }

    public void setGzLv(int gzLv) {
        this.gzLv = gzLv;
    }

    public int getAdTypeId() {
        return adTypeId;
    }

    public void setAdTypeId(int adTypeId) {
        this.adTypeId = adTypeId;
    }

    public int getFalg() {
        return falg;
    }

    public void setFalg(int falg) {
        this.falg = falg;
    }

    public int getUseItem() {
        return useItem;
    }

    public void setUseItem(int useItem) {
        this.useItem = useItem;
    }
}
