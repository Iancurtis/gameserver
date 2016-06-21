package com.znl.log.admin;
import com.znl.base.BaseLog;
/*
 *auto export class：
 *@author woko
 */
public class tbllog_online extends BaseLog{
	/***所属平台***/
	private String platform = "" ;
	public String getPlatform(){
	  return platform;
	}
	public void setPlatform(String platform){
	this.platform = platform;
	}

	/***当前在线玩家总人数***/
	private Integer people = 0 ;
	public Integer getPeople(){
	  return people;
	}
	public void setPeople(Integer people){
	this.people = people;
	}

	/***当前在线玩家总设备数***/
	private Long device_cnt = 0l ;
	public Long getDevice_cnt(){
	  return device_cnt;
	}
	public void setDevice_cnt(Long device_cnt){
	this.device_cnt = device_cnt;
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

	public tbllog_online() {
	}

	public tbllog_online(String platform, Integer people, Long device_cnt, Integer happend_time, Integer log_time){
		this.platform = platform;
		this.people = people;
		this.device_cnt = device_cnt;
		this.happend_time = happend_time;
		this.log_time = log_time;
	}

}
