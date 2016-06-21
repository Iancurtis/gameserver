package com.znl.log.admin;
import com.znl.base.BaseLog;

/*
 *auto export class：
 *@author woko
 */
public class tbllog_activityrank extends BaseLog{
	/****玩家id*****/
	private long playerId;

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}
	/****玩家id*****/
	private long rank;

	public long getRank() {
		return rank;
	}

	public void setRank(long rank) {
		this.rank = rank;
	}
	/***排行榜类型*****/
	private int type;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	/***写日志时间，索引字段***/
	private Integer log_time = 0 ;
	public Integer getLog_time(){
		return log_time;
	}
	public void setLog_time(Integer log_time){
		this.log_time = log_time;
	}

	public tbllog_activityrank(long playerId, long rank, int type) {
		this.playerId = playerId;
		this.rank = rank;
		this.type = type;
	}
}
