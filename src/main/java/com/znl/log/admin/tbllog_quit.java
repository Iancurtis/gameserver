package com.znl.log.admin;
import com.znl.base.BaseLog;
/*
 *auto export class：
 *@author woko
 */
public class tbllog_quit extends BaseLog{
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

	/***登录等级***/
	private Integer login_level = 0 ;
	public Integer getLogin_level(){
	  return login_level;
	}
	public void setLogin_level(Integer login_level){
	this.login_level = login_level;
	}

	/***登出等级***/
	private Integer logout_level = 0 ;
	public Integer getLogout_level(){
	  return logout_level;
	}
	public void setLogout_level(Integer logout_level){
	this.logout_level = logout_level;
	}

	/***登出IP***/
	private String logout_ip = "" ;
	public String getLogout_ip(){
	  return logout_ip;
	}
	public void setLogout_ip(String logout_ip){
	this.logout_ip = logout_ip;
	}

	/***登录时间***/
	private Integer login_time = 0 ;
	public Integer getLogin_time(){
	  return login_time;
	}
	public void setLogin_time(Integer login_time){
	this.login_time = login_time;
	}

	/***退出时间***/
	private Integer logout_time = 0 ;
	public Integer getLogout_time(){
	  return logout_time;
	}
	public void setLogout_time(Integer logout_time){
	this.logout_time = logout_time;
	}

	/***在线时长***/
	private Integer time_duration = 0 ;
	public Integer getTime_duration(){
	  return time_duration;
	}
	public void setTime_duration(Integer time_duration){
	this.time_duration = time_duration;
	}

	/***退出地图ID***/
	private Integer logout_map_id = 0 ;
	public Integer getLogout_map_id(){
	  return logout_map_id;
	}
	public void setLogout_map_id(Integer logout_map_id){
	this.logout_map_id = logout_map_id;
	}

	/***退出异常，或者reason对应字典表(0表示正常退出)***/
	private Integer reason_id = 0 ;
	public Integer getReason_id(){
	  return reason_id;
	}
	public void setReason_id(Integer reason_id){
	this.reason_id = reason_id;
	}

	/***特殊信息***/
	private String msg = "" ;
	public String getMsg(){
	  return msg;
	}
	public void setMsg(String msg){
	this.msg = msg;
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

	/***写日志时间，索引字段***/
	private Integer log_time = 0 ;
	public Integer getLog_time(){
	  return log_time;
	}
	public void setLog_time(Integer log_time){
	this.log_time = log_time;
	}

	public tbllog_quit() {
	}

	public tbllog_quit(String platform, Long role_id, String account_name, Integer login_level, Integer logout_level, String logout_ip, Integer login_time, Integer logout_time, Integer time_duration, Integer logout_map_id, Integer reason_id, String msg, String did, String game_version, Integer log_time){
		this.platform = platform;
		this.role_id = role_id;
		this.account_name = account_name;
		this.login_level = login_level;
		this.logout_level = logout_level;
		this.logout_ip = logout_ip;
		this.login_time = login_time;
		this.logout_time = logout_time;
		this.time_duration = time_duration;
		this.logout_map_id = logout_map_id;
		this.reason_id = reason_id;
		this.msg = msg;
		this.did = did;
		this.game_version = game_version;
		this.log_time = log_time;
	}

}
