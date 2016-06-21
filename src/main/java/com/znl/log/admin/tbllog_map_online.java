package com.znl.log.admin;
import com.znl.base.BaseLog;
/*
 *auto export class：
 *@author woko
 */
public class tbllog_map_online extends BaseLog{
	/***所属平台***/
	private String platform = "" ;
	public String getPlatform(){
	  return platform;
	}
	public void setPlatform(String platform){
	this.platform = platform;
	}

	/***地图ID***/
	private Integer map_id = 0 ;
	public Integer getMap_id(){
	  return map_id;
	}
	public void setMap_id(Integer map_id){
	this.map_id = map_id;
	}

	/***地图人数***/
	private Integer player_num = 0 ;
	public Integer getPlayer_num(){
	  return player_num;
	}
	public void setPlayer_num(Integer player_num){
	this.player_num = player_num;
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

	public tbllog_map_online() {
	}

	public tbllog_map_online(String platform, Integer map_id, Integer player_num, Integer happend_time, Integer log_time){
		this.platform = platform;
		this.map_id = map_id;
		this.player_num = player_num;
		this.happend_time = happend_time;
		this.log_time = log_time;
	}

}
