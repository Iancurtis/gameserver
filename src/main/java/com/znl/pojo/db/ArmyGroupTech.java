package com.znl.pojo.db;

import com.znl.base.BaseDbPojo;

/**
 * Created by Administrator on 2016/1/13.
 */
public class ArmyGroupTech extends BaseDbPojo {
    private Long armyGroupId;//军团Id
    private int typeId;//类型ID
    private int techExp;//科技经验
    private int techLv;//科技等级

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public Long getArmyGroupId() {
        return armyGroupId;
    }

    public void setArmyGroupId(Long armyGroupId) {
        this.armyGroupId = armyGroupId;
    }

    public int getTechExp() {
        return techExp;
    }

    public void setTechExp(int techExp) {
        this.techExp = techExp;
    }

    public int getTechLv() {
        return techLv;
    }

    public void setTechLv(int techLv) {
        this.techLv = techLv;
    }
}
