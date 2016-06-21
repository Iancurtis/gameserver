package com.znl.log.admin;
import com.znl.base.BaseLog;
/*
 *auto export class：
 *@author woko
 */
public class tbllog_box extends BaseLog{
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

	/***宝箱类型（金宝箱/银宝箱等）***/
	private Integer box_type = 0 ;
	public Integer getBox_type(){
	  return box_type;
	}
	public void setBox_type(Integer box_type){
	this.box_type = box_type;
	}

	/***获得道具ID***/
	private Integer item_id = 0 ;
	public Integer getItem_id(){
	  return item_id;
	}
	public void setItem_id(Integer item_id){
	this.item_id = item_id;
	}

	/***获得道具数量***/
	private Long item_number = 0l ;
	public Long getItem_number(){
	  return item_number;
	}
	public void setItem_number(Long item_number){
	this.item_number = item_number;
	}

	/***获得货币类型***/
	private Integer money_type = 0 ;
	public Integer getMoney_type(){
	  return money_type;
	}
	public void setMoney_type(Integer money_type){
	this.money_type = money_type;
	}

	/***获得货币数量***/
	private Long amount = 0l ;
	public Long getAmount(){
	  return amount;
	}
	public void setAmount(Long amount){
	this.amount = amount;
	}

	/***在一次打开宝箱时获得多个物品时，可以通过json的格式记录在该字段中，而此时，item_id与item_number留空***/
	private String source_data = "" ;
	public String getSource_data(){
	  return source_data;
	}
	public void setSource_data(String source_data){
	this.source_data = source_data;
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

	public tbllog_box() {
	}

	public tbllog_box(String platform, Long role_id, String account_name, Integer box_type, Integer item_id, Long item_number, Integer money_type, Long amount, String source_data, Integer happend_time, Integer log_time){
		this.platform = platform;
		this.role_id = role_id;
		this.account_name = account_name;
		this.box_type = box_type;
		this.item_id = item_id;
		this.item_number = item_number;
		this.money_type = money_type;
		this.amount = amount;
		this.source_data = source_data;
		this.happend_time = happend_time;
		this.log_time = log_time;
	}

}
