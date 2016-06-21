package com.znl.log.admin;
import com.znl.base.BaseLog;
/*
 *auto export class：
 *@author woko
 */
public class tbllog_task extends BaseLog{
	/***所属平台***/
	private String platform = "" ;
	public String getPlatform(){
	  return platform;
	}
	public void setPlatform(String platform){
	this.platform = platform;
	}

	/***角色ID***/
	private Long role_id = 0l ;
	public Long getRole_id(){
	  return role_id;
	}
	public void setRole_id(Long role_id){
	this.role_id = role_id;
	}

	/***平台唯一用户标识***/
	private String account_name = "" ;
	public String getAccount_name(){
	  return account_name;
	}
	public void setAccount_name(String account_name){
	this.account_name = account_name;
	}

	/***职业ID***/
	private Integer dim_prof = 0 ;
	public Integer getDim_prof(){
	  return dim_prof;
	}
	public void setDim_prof(Integer dim_prof){
	this.dim_prof = dim_prof;
	}

	/***玩家等级***/
	private Integer dim_level = 0 ;
	public Integer getDim_level(){
	  return dim_level;
	}
	public void setDim_level(Integer dim_level){
	this.dim_level = dim_level;
	}

	/***任务ID***/
	private Integer task_id = 0 ;
	public Integer getTask_id(){
	  return task_id;
	}
	public void setTask_id(Integer task_id){
	this.task_id = task_id;
	}

	/***状态（1=接任务，2=完成任务；3=取消任务(退出任务)，4=提交任务）***/
	private Integer status = 0 ;
	public Integer getStatus(){
	  return status;
	}
	public void setStatus(Integer status){
	this.status = status;
	}

	/***事件发生时间***/
	private Integer happend_time = 0 ;
	public Integer getHappend_time(){
	  return happend_time;
	}
	public void setHappend_time(Integer happend_time){
	this.happend_time = happend_time;
	}

	/***写日志时间，索引字段***/
	private Integer log_time = 0 ;
	public Integer getLog_time(){
	  return log_time;
	}
	public void setLog_time(Integer log_time){
	this.log_time = log_time;
	}

	public tbllog_task() {
	}

	public tbllog_task(String platform, Long role_id, String account_name, Integer dim_prof, Integer dim_level, Integer task_id, Integer status, Integer happend_time, Integer log_time){
		this.platform = platform;
		this.role_id = role_id;
		this.account_name = account_name;
		this.dim_prof = dim_prof;
		this.dim_level = dim_level;
		this.task_id = task_id;
		this.status = status;
		this.happend_time = happend_time;
		this.log_time = log_time;
	}

}
