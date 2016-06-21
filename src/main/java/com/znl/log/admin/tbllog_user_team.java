package com.znl.log.admin;
import com.znl.base.BaseLog;
/*
 *auto export class：
 *@author woko
 */
public class tbllog_user_team extends BaseLog{
	/***角色ID***/
	private Long role_id = 0l ;
	public Long getRole_id(){
	  return role_id;
	}
	public void setRole_id(Long role_id){
	this.role_id = role_id;
	}

	/***组队ID***/
	private Long team_id = 0l ;
	public Long getTeam_id(){
	  return team_id;
	}
	public void setTeam_id(Long team_id){
	this.team_id = team_id;
	}

	/***组队类型ID（提供字典表）***/
	private Integer team_type_id = 0 ;
	public Integer getTeam_type_id(){
	  return team_type_id;
	}
	public void setTeam_type_id(Integer team_type_id){
	this.team_type_id = team_type_id;
	}

	/***队长角色ID***/
	private Long leader_role_id = 0l ;
	public Long getLeader_role_id(){
	  return leader_role_id;
	}
	public void setLeader_role_id(Long leader_role_id){
	this.leader_role_id = leader_role_id;
	}

	/***操作类型（1-组队[加入]，2-解散，3-退出）***/
	private Integer opt = 0 ;
	public Integer getOpt(){
	  return opt;
	}
	public void setOpt(Integer opt){
	this.opt = opt;
	}

	/***队伍人数***/
	private Long player_num = 0l ;
	public Long getPlayer_num(){
	  return player_num;
	}
	public void setPlayer_num(Long player_num){
	this.player_num = player_num;
	}

	/***组队对应的任务***/
	private Integer task_id = 0 ;
	public Integer getTask_id(){
	  return task_id;
	}
	public void setTask_id(Integer task_id){
	this.task_id = task_id;
	}

	/***战斗时长（解散时记录）***/
	private Integer time_duration = 0 ;
	public Integer getTime_duration(){
	  return time_duration;
	}
	public void setTime_duration(Integer time_duration){
	this.time_duration = time_duration;
	}

	/***事件发生时间***/
	private Integer happend_time = 0 ;
	public Integer getHappend_time(){
	  return happend_time;
	}
	public void setHappend_time(Integer happend_time){
	this.happend_time = happend_time;
	}

	/***写日志时间***/
	private Integer log_time = 0 ;
	public Integer getLog_time(){
	  return log_time;
	}
	public void setLog_time(Integer log_time){
	this.log_time = log_time;
	}

	public tbllog_user_team() {
	}

	public tbllog_user_team(Long role_id, Long team_id, Integer team_type_id, Long leader_role_id, Integer opt, Long player_num, Integer task_id, Integer time_duration, Integer happend_time, Integer log_time){
		this.role_id = role_id;
		this.team_id = team_id;
		this.team_type_id = team_type_id;
		this.leader_role_id = leader_role_id;
		this.opt = opt;
		this.player_num = player_num;
		this.task_id = task_id;
		this.time_duration = time_duration;
		this.happend_time = happend_time;
		this.log_time = log_time;
	}

}
