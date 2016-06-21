package com.znl.log.admin;
import com.znl.base.BaseLog;
/*
 *auto export class：
 *@author woko
 */
public class tbllog_role extends BaseLog{
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

	/***角色名称***/
	private String role_name = "" ;
	public String getRole_name(){
	  return role_name;
	}
	public void setRole_name(String role_name){
	this.role_name = role_name;
	}

	/***平台唯一用户标识***/
	private String account_name = "" ;
	public String getAccount_name(){
	  return account_name;
	}
	public void setAccount_name(String account_name){
	this.account_name = account_name;
	}

	/***玩家IP***/
	private String user_ip = "" ;
	public String getUser_ip(){
	  return user_ip;
	}
	public void setUser_ip(String user_ip){
	this.user_ip = user_ip;
	}

	/***职业ID***/
	private Integer dim_prof = 0 ;
	public Integer getDim_prof(){
	  return dim_prof;
	}
	public void setDim_prof(Integer dim_prof){
	this.dim_prof = dim_prof;
	}

	/***性别(0=女,1=男,2=未知)***/
	private Integer dim_sex = 0 ;
	public Integer getDim_sex(){
	  return dim_sex;
	}
	public void setDim_sex(Integer dim_sex){
	this.dim_sex = dim_sex;
	}

	/***用户设备ID***/
	private String did = "" ;
	public String getDid(){
	  return did;
	}
	public void setDid(String did){
	this.did = did;
	}

	/***游戏版本号***/
	private String game_version = "" ;
	public String getGame_version(){
	  return game_version;
	}
	public void setGame_version(String game_version){
	this.game_version = game_version;
	}

	/***事件发生时间***/
	private Integer happend_time = 0 ;
	public Integer getHappend_time(){
	  return happend_time;
	}
	public void setHappend_time(Integer happend_time){
	this.happend_time = happend_time;
	}

	/***写日志时间,索引字段***/
	private Integer log_time = 0 ;
	public Integer getLog_time(){
	  return log_time;
	}
	public void setLog_time(Integer log_time){
	this.log_time = log_time;
	}

	public tbllog_role() {
	}

	public tbllog_role(String platform, Long role_id, String role_name, String account_name, String user_ip, Integer dim_prof, Integer dim_sex, String did, String game_version, Integer happend_time, Integer log_time){
		this.platform = platform;
		this.role_id = role_id;
		this.role_name = role_name;
		this.account_name = account_name;
		this.user_ip = user_ip;
		this.dim_prof = dim_prof;
		this.dim_sex = dim_sex;
		this.did = did;
		this.game_version = game_version;
		this.happend_time = happend_time;
		this.log_time = log_time;
	}

}
