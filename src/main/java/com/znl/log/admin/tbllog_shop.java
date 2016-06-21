package com.znl.log.admin;
import com.znl.base.BaseLog;
/*
 *auto export class：
 *@author woko
 */
public class tbllog_shop extends BaseLog{
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

	/***商城类型ID***/
	private Integer shopId = 0 ;
	public Integer getShopId(){
	  return shopId;
	}
	public void setShopId(Integer shopId){
	this.shopId = shopId;
	}

	/***玩家等级***/
	private Integer dim_level = 0 ;
	public Integer getDim_level(){
	  return dim_level;
	}
	public void setDim_level(Integer dim_level){
	this.dim_level = dim_level;
	}

	/***职业ID***/
	private Integer dim_prof = 0 ;
	public Integer getDim_prof(){
	  return dim_prof;
	}
	public void setDim_prof(Integer dim_prof){
	this.dim_prof = dim_prof;
	}

	/***货币类型（1=金币，2=绑定金币，3=铜币，4=绑定铜币，5=礼券，6=积分/荣誉, 7=兑换）***/
	private Integer money_type = 0 ;
	public Integer getMoney_type(){
	  return money_type;
	}
	public void setMoney_type(Integer money_type){
	this.money_type = money_type;
	}

	/***货币数量***/
	private Integer amount = 0 ;
	public Integer getAmount(){
	  return amount;
	}
	public void setAmount(Integer amount){
	this.amount = amount;
	}

	/***物品分类1***/
	private Integer item_type_1 = 0 ;
	public Integer getItem_type_1(){
	  return item_type_1;
	}
	public void setItem_type_1(Integer item_type_1){
	this.item_type_1 = item_type_1;
	}

	/***物品分类2***/
	private Integer item_type_2 = 0 ;
	public Integer getItem_type_2(){
	  return item_type_2;
	}
	public void setItem_type_2(Integer item_type_2){
	this.item_type_2 = item_type_2;
	}

	/***物品ID***/
	private Integer item_id = 0 ;
	public Integer getItem_id(){
	  return item_id;
	}
	public void setItem_id(Integer item_id){
	this.item_id = item_id;
	}

	/***物品数量***/
	private Long item_number = 0l ;
	public Long getItem_number(){
	  return item_number;
	}
	public void setItem_number(Long item_number){
	this.item_number = item_number;
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

	public tbllog_shop() {
	}

	public tbllog_shop(String platform, Long role_id, String account_name, Integer shopId, Integer dim_level, Integer dim_prof, Integer money_type, Integer amount, Integer item_type_1, Integer item_type_2, Integer item_id, Long item_number, Integer happend_time, Integer log_time){
		this.platform = platform;
		this.role_id = role_id;
		this.account_name = account_name;
		this.shopId = shopId;
		this.dim_level = dim_level;
		this.dim_prof = dim_prof;
		this.money_type = money_type;
		this.amount = amount;
		this.item_type_1 = item_type_1;
		this.item_type_2 = item_type_2;
		this.item_id = item_id;
		this.item_number = item_number;
		this.happend_time = happend_time;
		this.log_time = log_time;
	}

}
