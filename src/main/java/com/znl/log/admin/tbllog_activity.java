package com.znl.log.admin;
import com.znl.base.BaseLog;
/*
 *auto export class：
 *@author woko
 */
public class tbllog_activity extends BaseLog{
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

	/***玩家等级***/
	private Integer dim_level = 0 ;
	public Integer getDim_level(){
	  return dim_level;
	}
	public void setDim_level(Integer dim_level){
	this.dim_level = dim_level;
	}

	/***功能ID(对应 dict_action.action_id)***/
	private Integer action_id = 0 ;
	public Integer getAction_id(){
	  return action_id;
	}
	public void setAction_id(Integer action_id){
	this.action_id = action_id;
	}

	/***状态（1=参与，2=完成；如果没有统计完成，系统会默认参与即是完成）***/
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

	public tbllog_activity() {
	}

	public tbllog_activity(String platform, Long role_id, String account_name, Integer dim_level, Integer action_id, Integer status, Integer happend_time, Integer log_time){
		this.platform = platform;
		this.role_id = role_id;
		this.account_name = account_name;
		this.dim_level = dim_level;
		this.action_id = action_id;
		this.status = status;
		this.happend_time = happend_time;
		this.log_time = log_time;
	}

}
