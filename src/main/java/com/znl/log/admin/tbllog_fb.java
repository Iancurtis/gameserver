package com.znl.log.admin;
import com.znl.base.BaseLog;
/*
 *auto export class：
 *@author woko
 */
public class tbllog_fb extends BaseLog{
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

	/***副本ID***/
	private Integer fb_id = 0 ;
	public Integer getFb_id(){
	  return fb_id;
	}
	public void setFb_id(Integer fb_id){
	this.fb_id = fb_id;
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

	/***副本层数/关卡数***/
	private Integer fb_level = 0 ;
	public Integer getFb_level(){
	  return fb_level;
	}
	public void setFb_level(Integer fb_level){
	this.fb_level = fb_level;
	}

	/***状态（1=参与，2=完成；3=退出，4=超时）***/
	private Integer status = 0 ;
	public Integer getStatus(){
	  return status;
	}
	public void setStatus(Integer status){
	this.status = status;
	}

	/***死亡角色数量（一次副本死亡的次数）***/
	private Integer death_cnt = 0 ;
	public Integer getDeath_cnt(){
	  return death_cnt;
	}
	public void setDeath_cnt(Integer death_cnt){
	this.death_cnt = death_cnt;
	}

	/***参与时间***/
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

	public tbllog_fb() {
	}

	public tbllog_fb(String platform, Long role_id, Integer fb_id, String account_name, Integer dim_level, Integer fb_level, Integer status, Integer death_cnt, Integer happend_time, Integer log_time){
		this.platform = platform;
		this.role_id = role_id;
		this.fb_id = fb_id;
		this.account_name = account_name;
		this.dim_level = dim_level;
		this.fb_level = fb_level;
		this.status = status;
		this.death_cnt = death_cnt;
		this.happend_time = happend_time;
		this.log_time = log_time;
	}

}
