package com.znl.log.admin;
import com.znl.base.BaseLog;
/*
 *auto export class：
 *@author woko
 */
public class tbllog_user_friend extends BaseLog{
	/***发起动作的角色ID***/
	private Long opt_role_id = 0l ;
	public Long getOpt_role_id(){
	  return opt_role_id;
	}
	public void setOpt_role_id(Long opt_role_id){
	this.opt_role_id = opt_role_id;
	}

	/***角色ID***/
	private Long role_id = 0l ;
	public Long getRole_id(){
	  return role_id;
	}
	public void setRole_id(Long role_id){
	this.role_id = role_id;
	}

	/***操作类型（1-申请，2-接受好友）***/
	private Integer opt = 0 ;
	public Integer getOpt(){
	  return opt;
	}
	public void setOpt(Integer opt){
	this.opt = opt;
	}

	/***发起动作的角色的好友数量***/
	private Integer opt_role_friend_number = 0 ;
	public Integer getOpt_role_friend_number(){
	  return opt_role_friend_number;
	}
	public void setOpt_role_friend_number(Integer opt_role_friend_number){
	this.opt_role_friend_number = opt_role_friend_number;
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

	public tbllog_user_friend() {
	}

	public tbllog_user_friend(Long opt_role_id, Long role_id, Integer opt, Integer opt_role_friend_number, Integer happend_time, Integer log_time){
		this.opt_role_id = opt_role_id;
		this.role_id = role_id;
		this.opt = opt;
		this.opt_role_friend_number = opt_role_friend_number;
		this.happend_time = happend_time;
		this.log_time = log_time;
	}

}
