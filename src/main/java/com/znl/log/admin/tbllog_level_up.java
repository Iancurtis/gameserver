package com.znl.log.admin;
import com.znl.base.BaseLog;
/*
 *auto export class：
 *@author woko
 */
public class tbllog_level_up extends BaseLog{
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

	/***上一等级***/
	private Integer last_level = 0 ;
	public Integer getLast_level(){
	  return last_level;
	}
	public void setLast_level(Integer last_level){
	this.last_level = last_level;
	}

	/***当前等级***/
	private Integer current_level = 0 ;
	public Integer getCurrent_level(){
	  return current_level;
	}
	public void setCurrent_level(Integer current_level){
	this.current_level = current_level;
	}

	/***上一经验值***/
	private Long last_exp = 0l ;
	public Long getLast_exp(){
	  return last_exp;
	}
	public void setLast_exp(Long last_exp){
	this.last_exp = last_exp;
	}

	/***当前经验值***/
	private Long current_exp = 0l ;
	public Long getCurrent_exp(){
	  return current_exp;
	}
	public void setCurrent_exp(Long current_exp){
	this.current_exp = current_exp;
	}

	/***变动时间***/
	private Integer happend_time = 0 ;
	public Integer getHappend_time(){
	  return happend_time;
	}
	public void setHappend_time(Integer happend_time){
	this.happend_time = happend_time;
	}

	/***记录时间***/
	private Integer log_time = 0 ;
	public Integer getLog_time(){
	  return log_time;
	}
	public void setLog_time(Integer log_time){
	this.log_time = log_time;
	}

	public tbllog_level_up() {
	}

	public tbllog_level_up(String platform, Long role_id, String account_name, Integer last_level, Integer current_level, Long last_exp, Long current_exp, Integer happend_time, Integer log_time){
		this.platform = platform;
		this.role_id = role_id;
		this.account_name = account_name;
		this.last_level = last_level;
		this.current_level = current_level;
		this.last_exp = last_exp;
		this.current_exp = current_exp;
		this.happend_time = happend_time;
		this.log_time = log_time;
	}

}
