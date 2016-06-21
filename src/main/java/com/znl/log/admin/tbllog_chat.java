package com.znl.log.admin;
import com.znl.base.BaseLog;
/*
 *auto export class：
 *@author woko
 */
public class tbllog_chat extends BaseLog{
	/***所属平台***/
	private String platform = "" ;
	public String getPlatform(){
	  return platform;
	}
	public void setPlatform(String platform){
	this.platform = platform;
	}

	/***平台唯一用户标识***/
	private String account_name = "" ;
	public String getAccount_name(){
	  return account_name;
	}
	public void setAccount_name(String account_name){
	this.account_name = account_name;
	}

	/***角色ID***/
	private Long role_id = 0l ;
	public Long getRole_id(){
	  return role_id;
	}
	public void setRole_id(Long role_id){
	this.role_id = role_id;
	}

	/***角色名***/
	private String role_name = "" ;
	public String getRole_name(){
	  return role_name;
	}
	public void setRole_name(String role_name){
	this.role_name = role_name;
	}

	/***玩家等级***/
	private Integer dim_level = 0 ;
	public Integer getDim_level(){
	  return dim_level;
	}
	public void setDim_level(Integer dim_level){
	this.dim_level = dim_level;
	}

	/***玩家IP***/
	private String user_ip = "" ;
	public String getUser_ip(){
	  return user_ip;
	}
	public void setUser_ip(String user_ip){
	this.user_ip = user_ip;
	}

	/***聊天频道（提供字典表）***/
	private Integer channel = 0 ;
	public Integer getChannel(){
	  return channel;
	}
	public void setChannel(Integer channel){
	this.channel = channel;
	}

	/***聊天信息***/
	private String msg = "" ;
	public String getMsg(){
	  return msg;
	}
	public void setMsg(String msg){
	this.msg = msg;
	}

	/***内容类型（0代表语言，1代表文本）***/
	private Integer type = 0 ;
	public Integer getType(){
	  return type;
	}
	public void setType(Integer type){
	this.type = type;
	}

	/***两天对象ID***/
	private Long target_role_id = 0l ;
	public Long getTarget_role_id(){
	  return target_role_id;
	}
	public void setTarget_role_id(Long target_role_id){
	this.target_role_id = target_role_id;
	}

	/***聊天发生时间***/
	private Integer happend_time = 0 ;
	public Integer getHappend_time(){
	  return happend_time;
	}
	public void setHappend_time(Integer happend_time){
	this.happend_time = happend_time;
	}

	/***日志记录时间***/
	private Integer log_time = 0 ;
	public Integer getLog_time(){
	  return log_time;
	}
	public void setLog_time(Integer log_time){
	this.log_time = log_time;
	}

	public tbllog_chat() {
	}

	public tbllog_chat(String platform, String account_name, Long role_id, String role_name, Integer dim_level, String user_ip, Integer channel, String msg, Integer type, Long target_role_id, Integer happend_time, Integer log_time){
		this.platform = platform;
		this.account_name = account_name;
		this.role_id = role_id;
		this.role_name = role_name;
		this.dim_level = dim_level;
		this.user_ip = user_ip;
		this.channel = channel;
		this.msg = msg;
		this.type = type;
		this.target_role_id = target_role_id;
		this.happend_time = happend_time;
		this.log_time = log_time;
	}

}
