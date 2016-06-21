package com.znl.core;


import java.util.List;

/**
 * Created by Administrator on 2015/11/16.
 */
public class ArmyGroupTemp {
    private Long id;
    private String name;//名字
    private String commander;//军团长
    private Long commandid;//军团长id
    private int rank ;//排名
    private int level; //大厅等级
    private int build;//建设度
    private int joinWay;//申请加入方式
    private int conditonlevel;//等级要求
    private int conditoncapity;//战力要求
    private List<Long>  menbers;//成员
    private List<Long>  applymenbers;//申请成员
    private String notice;//公告
    private int sciencelevel; //科技馆等级
    private int welfarelevel;//福利院等级

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommander() {
        return commander;
    }

    public void setCommander(String commander) {
        this.commander = commander;
    }

    public Long getCommandid() {
        return commandid;
    }

    public void setCommandid(Long commandid) {
        this.commandid = commandid;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getBuild() {
        return build;
    }

    public void setBuild(int build) {
        this.build = build;
    }

    public int getJoinWay() {
        return joinWay;
    }

    public void setJoinWay(int joinWay) {
        this.joinWay = joinWay;
    }

    public int getConditonlevel() {
        return conditonlevel;
    }

    public void setConditonlevel(int conditonlevel) {
        this.conditonlevel = conditonlevel;
    }

    public int getConditoncapity() {
        return conditoncapity;
    }

    public void setConditoncapity(int conditoncapity) {
        this.conditoncapity = conditoncapity;
    }

    public List<Long> getMenbers() {
        return menbers;
    }

    public void setMenbers(List<Long> menbers) {
        this.menbers = menbers;
    }

    public List<Long> getApplymenbers() {
        return applymenbers;
    }

    public void setApplymenbers(List<Long> applymenbers) {
        this.applymenbers = applymenbers;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getSciencelevel() {
        return sciencelevel;
    }

    public void setSciencelevel(int sciencelevel) {
        this.sciencelevel = sciencelevel;
    }

    public int getWelfarelevel() {
        return welfarelevel;
    }

    public void setWelfarelevel(int welfarelevel) {
        this.welfarelevel = welfarelevel;
    }
}
