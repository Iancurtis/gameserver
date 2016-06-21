package com.znl.log.admin;
import com.znl.base.BaseLog;
/*
 *auto export class：
 *@author woko
 */
public class tbllog_skill extends BaseLog{
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

	/***技能ID（对应 dict_action.action_id）***/
	private Integer skill_id = 0 ;
	public Integer getSkill_id(){
	  return skill_id;
	}
	public void setSkill_id(Integer skill_id){
	this.skill_id = skill_id;
	}

	/***技能使用次数***/
	private Integer used_num = 0 ;
	public Integer getUsed_num(){
	  return used_num;
	}
	public void setUsed_num(Integer used_num){
	this.used_num = used_num;
	}

	/***玩家等级***/
	private Integer dim_level = 0 ;
	public Integer getDim_level(){
	  return dim_level;
	}
	public void setDim_level(Integer dim_level){
	this.dim_level = dim_level;
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

	public tbllog_skill() {
	}

	public tbllog_skill(String platform, Long role_id, String account_name, Integer skill_id, Integer used_num, Integer dim_level, Integer happend_time, Integer log_time){
		this.platform = platform;
		this.role_id = role_id;
		this.account_name = account_name;
		this.skill_id = skill_id;
		this.used_num = used_num;
		this.dim_level = dim_level;
		this.happend_time = happend_time;
		this.log_time = log_time;
	}

}
