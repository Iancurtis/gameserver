package com.znl.log.admin;
import com.znl.base.BaseLog;
/*
 *auto export class：
 *@author woko
 */
public class tbllog_money_remain extends BaseLog{
	/***货币类型***/
	private Integer money_type = 0 ;
	public Integer getMoney_type(){
	  return money_type;
	}
	public void setMoney_type(Integer money_type){
	this.money_type = money_type;
	}

	/***剩余货币***/
	private Long money_remain = 0l ;
	public Long getMoney_remain(){
	  return money_remain;
	}
	public void setMoney_remain(Long money_remain){
	this.money_remain = money_remain;
	}

	/***事件发生时间***/
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

	public tbllog_money_remain() {
	}

	public tbllog_money_remain(Integer money_type, Long money_remain, Integer happend_time, Integer log_time){
		this.money_type = money_type;
		this.money_remain = money_remain;
		this.happend_time = happend_time;
		this.log_time = log_time;
	}

}
