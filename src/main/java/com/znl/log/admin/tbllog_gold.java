package com.znl.log.admin;
import com.znl.base.BaseLog;
/*
 *auto export class：
 *@author woko
 */
public class tbllog_gold extends BaseLog{
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

	/***货币类型(1=金币，2=绑定金币，3=铜币，4=绑定铜币，5=礼券，6=积分/荣誉，7=兑换)***/
	private Integer money_type = 0 ;
	public Integer getMoney_type(){
	  return money_type;
	}
	public void setMoney_type(Integer money_type){
	this.money_type = money_type;
	}

	/***货币数量***/
	private Long amount = 0l ;
	public Long getAmount(){
	  return amount;
	}
	public void setAmount(Long amount){
	this.amount = amount;
	}

	/***剩余货币数量***/
	private Long money_remain = 0l ;
	public Long getMoney_remain(){
	  return money_remain;
	}
	public void setMoney_remain(Long money_remain){
	this.money_remain = money_remain;
	}

	/***涉及的道具ID***/
	private Integer item_id = 0 ;
	public Integer getItem_id(){
	  return item_id;
	}
	public void setItem_id(Integer item_id){
	this.item_id = item_id;
	}

	/***货币加减(1=增加，2=减少)***/
	private Integer opt = 0 ;
	public Integer getOpt(){
	  return opt;
	}
	public void setOpt(Integer opt){
	this.opt = opt;
	}

	/***行为分类1（一级消费点）对应(dict_action.action_1)***/
	private Integer action_1 = 0 ;
	public Integer getAction_1(){
	  return action_1;
	}
	public void setAction_1(Integer action_1){
	this.action_1 = action_1;
	}

	/***若存在一级消费点，则不存在二级消费点，将二级消费点设为一级消费点的值***/
	private Integer action_2 = 0 ;
	public Integer getAction_2(){
	  return action_2;
	}
	public void setAction_2(Integer action_2){
	this.action_2 = action_2;
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

	public tbllog_gold() {
	}

	public tbllog_gold(String platform, Long role_id, String account_name, Integer dim_level, Integer dim_prof, Integer money_type, Long amount, Long money_remain, Integer item_id, Integer opt, Integer action_1, Integer action_2, Long item_number, Integer happend_time, Integer log_time){
		this.platform = platform;
		this.role_id = role_id;
		this.account_name = account_name;
		this.dim_level = dim_level;
		this.dim_prof = dim_prof;
		this.money_type = money_type;
		this.amount = amount;
		this.money_remain = money_remain;
		this.item_id = item_id;
		this.opt = opt;
		this.action_1 = action_1;
		this.action_2 = action_2;
		this.item_number = item_number;
		this.happend_time = happend_time;
		this.log_time = log_time;
	}

}
