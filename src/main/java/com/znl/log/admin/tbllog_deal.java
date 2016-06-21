package com.znl.log.admin;
import com.znl.base.BaseLog;
/*
 *auto export class：
 *@author woko
 */
public class tbllog_deal extends BaseLog{
	/***所属平台***/
	private String platform = "" ;
	public String getPlatform(){
	  return platform;
	}
	public void setPlatform(String platform){
	this.platform = platform;
	}

	/***交易ID***/
	private Integer deal_id = 0 ;
	public Integer getDeal_id(){
	  return deal_id;
	}
	public void setDeal_id(Integer deal_id){
	this.deal_id = deal_id;
	}

	/***交易出物品角色ID***/
	private Long role_id = 0l ;
	public Long getRole_id(){
	  return role_id;
	}
	public void setRole_id(Long role_id){
	this.role_id = role_id;
	}

	/***获得物品角色ID***/
	private Long owner_id = 0l ;
	public Long getOwner_id(){
	  return owner_id;
	}
	public void setOwner_id(Long owner_id){
	this.owner_id = owner_id;
	}

	/***交易出物品ID***/
	private Integer item_id = 0 ;
	public Integer getItem_id(){
	  return item_id;
	}
	public void setItem_id(Integer item_id){
	this.item_id = item_id;
	}

	/***交易出物品数量***/
	private Long item_number = 0l ;
	public Long getItem_number(){
	  return item_number;
	}
	public void setItem_number(Long item_number){
	this.item_number = item_number;
	}

	/***交易进物品ID***/
	private Integer owner_item_id = 0 ;
	public Integer getOwner_item_id(){
	  return owner_item_id;
	}
	public void setOwner_item_id(Integer owner_item_id){
	this.owner_item_id = owner_item_id;
	}

	/***交易进物品数量***/
	private Long owner_item_number = 0l ;
	public Long getOwner_item_number(){
	  return owner_item_number;
	}
	public void setOwner_item_number(Long owner_item_number){
	this.owner_item_number = owner_item_number;
	}

	/***交易状态（1=成功，2=取消，3=失败）***/
	private Integer status = 0 ;
	public Integer getStatus(){
	  return status;
	}
	public void setStatus(Integer status){
	this.status = status;
	}

	/***交易时间***/
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

	public tbllog_deal() {
	}

	public tbllog_deal(String platform, Integer deal_id, Long role_id, Long owner_id, Integer item_id, Long item_number, Integer owner_item_id, Long owner_item_number, Integer status, Integer happend_time, Integer log_time){
		this.platform = platform;
		this.deal_id = deal_id;
		this.role_id = role_id;
		this.owner_id = owner_id;
		this.item_id = item_id;
		this.item_number = item_number;
		this.owner_item_id = owner_item_id;
		this.owner_item_number = owner_item_number;
		this.status = status;
		this.happend_time = happend_time;
		this.log_time = log_time;
	}

}
