package com.znl.log.admin;
import com.znl.base.BaseLog;
/*
 *auto export class：
 *@author woko
 */
public class tbllog_login extends BaseLog{
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

	/***等级***/
	private Integer dim_level = 0 ;
	public Integer getDim_level(){
	  return dim_level;
	}
	public void setDim_level(Integer dim_level){
	this.dim_level = dim_level;
	}

	/***登录IP***/
	private String user_ip = "" ;
	public String getUser_ip(){
	  return user_ip;
	}
	public void setUser_ip(String user_ip){
	this.user_ip = user_ip;
	}

	/***登录地图ID***/
	private Integer login_map_id = 0 ;
	public Integer getLogin_map_id(){
	  return login_map_id;
	}
	public void setLogin_map_id(Integer login_map_id){
	this.login_map_id = login_map_id;
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

	/***手游专用手机操作系统，如：android、iOS***/
	private String os = "" ;
	public String getOs(){
	  return os;
	}
	public void setOs(String os){
	this.os = os;
	}

	/***手游专用操作系统版本号，如：2.3.4***/
	private String os_version = "" ;
	public String getOs_version(){
	  return os_version;
	}
	public void setOs_version(String os_version){
	this.os_version = os_version;
	}

	/***手游专用设备名称，如：三星GT-S5830MI 2S , Nexus 5***/
	private String device = "" ;
	public String getDevice(){
	  return device;
	}
	public void setDevice(String device){
	this.device = device;
	}

	/***手游专用设备类型，如：xiaomi、samsung、apple***/
	private String device_type = "" ;
	public String getDevice_type(){
	  return device_type;
	}
	public void setDevice_type(String device_type){
	this.device_type = device_type;
	}

	/***手游专用屏幕分辨率，如：480*800***/
	private String screen = "" ;
	public String getScreen(){
	  return screen;
	}
	public void setScreen(String screen){
	this.screen = screen;
	}

	/***手游专用,移动网络运营商(mobile network operators)，如：中国移动、中国联通***/
	private String mno = "" ;
	public String getMno(){
	  return mno;
	}
	public void setMno(String mno){
	this.mno = mno;
	}

	/***手游专用,联网方式(Networking mode)，如：3G、WIFI***/
	private String nm = "" ;
	public String getNm(){
	  return nm;
	}
	public void setNm(String nm){
	this.nm = nm;
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

	public tbllog_login() {
	}

	public tbllog_login(String platform, Long role_id, String account_name, Integer dim_level, String user_ip, Integer login_map_id, String did, String game_version, String os, String os_version, String device, String device_type, String screen, String mno, String nm, Integer happend_time, Integer log_time){
		this.platform = platform;
		this.role_id = role_id;
		this.account_name = account_name;
		this.dim_level = dim_level;
		this.user_ip = user_ip;
		this.login_map_id = login_map_id;
		this.did = did;
		this.game_version = game_version;
		this.os = os;
		this.os_version = os_version;
		this.device = device;
		this.device_type = device_type;
		this.screen = screen;
		this.mno = mno;
		this.nm = nm;
		this.happend_time = happend_time;
		this.log_time = log_time;
	}

}
