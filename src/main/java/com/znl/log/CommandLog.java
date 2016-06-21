package com.znl.log;

import com.znl.base.BaseLog;

/**
 * Created by Administrator on 2015/12/14.
 */
public class CommandLog extends BaseLog {
    private long id;
    private int level;
    private int costGold;
    private int costBook;

    public CommandLog(int level, int costGold, int costBook) {
        this.level = level;
        this.costGold = costGold;
        this.costBook = costBook;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getCostGold() {
        return costGold;
    }

    public void setCostGold(int costGold) {
        this.costGold = costGold;
    }

    public int getCostBook() {
        return costBook;
    }

    public void setCostBook(int costBook) {
        this.costBook = costBook;
    }
}
