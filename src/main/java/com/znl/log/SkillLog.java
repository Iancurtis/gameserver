package com.znl.log;

import com.znl.base.BaseLog;

/**
 * Created by Administrator on 2015/12/14.
 */
public class SkillLog extends BaseLog {
    private long id;
    private int type;
    private int skillId;
    private int level;
    private int skillBook;
    private int costGold;

    public SkillLog(int type, int skillId, int level, int skillBook,int costGold) {
        this.type = type;
        this.skillId = skillId;
        this.level = level;
        this.skillBook = skillBook;
        this.costGold = costGold;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getSkillBook() {
        return skillBook;
    }

    public void setSkillBook(int skillBook) {
        this.skillBook = skillBook;
    }

    public int getCostGold() {
        return costGold;
    }

    public void setCostGold(int costGold) {
        this.costGold = costGold;
    }
}
