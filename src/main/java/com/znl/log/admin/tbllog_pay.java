package com.znl.log.admin;
import com.znl.base.BaseLog;
/*
 *auto export class：
 *@author woko
 */
public class tbllog_pay extends BaseLog{
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

	/***玩家IP***/
	private String user_ip = "" ;
	public String getUser_ip(){
	  return user_ip;
	}
	public void setUser_ip(String user_ip){
	this.user_ip = user_ip;
	}

	/***等级***/
	private Integer dim_level = 0 ;
	public Integer getDim_level(){
	  return dim_level;
	}
	public void setDim_level(Integer dim_level){
	this.dim_level = dim_level;
	}

	/***充值类型，0为测试订单***/
	private Integer pay_type = 0 ;
	public Integer getPay_type(){
	  return pay_type;
	}
	public void setPay_type(Integer pay_type){
	this.pay_type = pay_type;
	}

	/***订单号***/
	private String order_id = "" ;
	public String getOrder_id(){
	  return order_id;
	}
	public void setOrder_id(String order_id){
	this.order_id = order_id;
	}

	/***充值金额(总充值金额)***/
	private Object pay_money = null ;
	public Object getPay_money(){
	  return pay_money;
	}
	public void setPay_money(Object pay_money){
	this.pay_money = pay_money;
	}

	/***充值获得的元宝/金币数***/
	private Long pay_gold = 0l ;
	public Long getPay_gold(){
	  return pay_gold;
	}
	public void setPay_gold(Long pay_gold){
	this.pay_gold = pay_gold;
	}

	/***用户ID设备***/
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

	public tbllog_pay() {
	}

	public tbllog_pay(String platform, Long role_id, String account_name, String user_ip, Integer dim_level, Integer pay_type, String order_id, Object pay_money, Long pay_gold, String did, String game_version, Integer happend_time, Integer log_time){
		this.platform = platform;
		this.role_id = role_id;
		this.account_name = account_name;
		this.user_ip = user_ip;
		this.dim_level = dim_level;
		this.pay_type = pay_type;
		this.order_id = order_id;
		this.pay_money = pay_money;
		this.pay_gold = pay_gold;
		this.did = did;
		this.game_version = game_version;
		this.happend_time = happend_time;
		this.log_time = log_time;
	}

}
