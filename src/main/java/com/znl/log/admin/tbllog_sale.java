package com.znl.log.admin;
import com.znl.base.BaseLog;
/*
 *auto export class：
 *@author woko
 */
public class tbllog_sale extends BaseLog{
	/***所属平台***/
	private String platform = "" ;
	public String getPlatform(){
	  return platform;
	}
	public void setPlatform(String platform){
	this.platform = platform;
	}

	/***寄卖流水号***/
	private Integer sales_id = 0 ;
	public Integer getSales_id(){
	  return sales_id;
	}
	public void setSales_id(Integer sales_id){
	this.sales_id = sales_id;
	}

	/***寄卖品角色ID***/
	private Long role_id = 0l ;
	public Long getRole_id(){
	  return role_id;
	}
	public void setRole_id(Long role_id){
	this.role_id = role_id;
	}

	/***寄卖品ID***/
	private Integer item_id = 0 ;
	public Integer getItem_id(){
	  return item_id;
	}
	public void setItem_id(Integer item_id){
	this.item_id = item_id;
	}

	/***寄卖品价格货币类型（铜钱/元宝...）(1=元宝，2=绑定金币，3=铜币，4=绑定铜币)***/
	private Integer price_type = 0 ;
	public Integer getPrice_type(){
	  return price_type;
	}
	public void setPrice_type(Integer price_type){
	this.price_type = price_type;
	}

	/***寄卖品价格***/
	private Long price_unit = 0l ;
	public Long getPrice_unit(){
	  return price_unit;
	}
	public void setPrice_unit(Long price_unit){
	this.price_unit = price_unit;
	}

	/***寄卖品物品数量***/
	private Long item_number = 0l ;
	public Long getItem_number(){
	  return item_number;
	}
	public void setItem_number(Long item_number){
	this.item_number = item_number;
	}

	/***寄卖操作行为ID***/
	private Integer action_id = 0 ;
	public Integer getAction_id(){
	  return action_id;
	}
	public void setAction_id(Integer action_id){
	this.action_id = action_id;
	}

	/***寄卖行为记录时间***/
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

	public tbllog_sale() {
	}

	public tbllog_sale(String platform, Integer sales_id, Long role_id, Integer item_id, Integer price_type, Long price_unit, Long item_number, Integer action_id, Integer happend_time, Integer log_time){
		this.platform = platform;
		this.sales_id = sales_id;
		this.role_id = role_id;
		this.item_id = item_id;
		this.price_type = price_type;
		this.price_unit = price_unit;
		this.item_number = item_number;
		this.action_id = action_id;
		this.happend_time = happend_time;
		this.log_time = log_time;
	}

}
