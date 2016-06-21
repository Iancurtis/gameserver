package com.znl.log.admin;
import com.znl.base.BaseLog;
/*
 *auto export class：
 *@author woko
 */
public class tbllog_auction extends BaseLog{
	/***所属平台***/
	private String platform = "" ;
	public String getPlatform(){
	  return platform;
	}
	public void setPlatform(String platform){
	this.platform = platform;
	}

	/***拍卖交易ID***/
	private Integer auction_id = 0 ;
	public Integer getAuction_id(){
	  return auction_id;
	}
	public void setAuction_id(Integer auction_id){
	this.auction_id = auction_id;
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

	/***拍卖操作类型（1=寄售，2=流拍，3=出售）***/
	private Integer opt_type_id = 0 ;
	public Integer getOpt_type_id(){
	  return opt_type_id;
	}
	public void setOpt_type_id(Integer opt_type_id){
	this.opt_type_id = opt_type_id;
	}

	/***拍卖物品ID***/
	private Integer item_id = 0 ;
	public Integer getItem_id(){
	  return item_id;
	}
	public void setItem_id(Integer item_id){
	this.item_id = item_id;
	}

	/***拍卖物品数量***/
	private Long item_number = 0l ;
	public Long getItem_number(){
	  return item_number;
	}
	public void setItem_number(Long item_number){
	this.item_number = item_number;
	}

	/***拍卖价格，如：{gold:1,bgold:2,coin:3,bcoin:4}（以json格式记录，其中gold为元宝，bgold为绑定元宝，后面数值为相应的数量）***/
	private String bid_price_list = "" ;
	public String getBid_price_list(){
	  return bid_price_list;
	}
	public void setBid_price_list(String bid_price_list){
	this.bid_price_list = bid_price_list;
	}

	/***一口价价格, 如: {gold:1, bgold:2,coin:3,bcoin:4} (以json格式记录, 其中gold为元宝, bgold为绑定元宝, 后面数值为相应的数量)***/
	private String a_price_list = "" ;
	public String getA_price_list(){
	  return a_price_list;
	}
	public void setA_price_list(String a_price_list){
	this.a_price_list = a_price_list;
	}

	/***拍卖事件发生时间***/
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

	public tbllog_auction() {
	}

	public tbllog_auction(String platform, Integer auction_id, Long role_id, String account_name, Integer opt_type_id, Integer item_id, Long item_number, String bid_price_list, String a_price_list, Integer happend_time, Integer log_time){
		this.platform = platform;
		this.auction_id = auction_id;
		this.role_id = role_id;
		this.account_name = account_name;
		this.opt_type_id = opt_type_id;
		this.item_id = item_id;
		this.item_number = item_number;
		this.bid_price_list = bid_price_list;
		this.a_price_list = a_price_list;
		this.happend_time = happend_time;
		this.log_time = log_time;
	}

}
