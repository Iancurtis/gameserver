package com.znl.log.admin;
import com.znl.base.BaseLog;
/*
 *auto export class：
 *@author woko
 */
public class tbllog_user_item extends BaseLog{
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

	/***角色名***/
	private String role_name = "" ;
	public String getRole_name(){
	  return role_name;
	}
	public void setRole_name(String role_name){
	this.role_name = role_name;
	}

	/***道具ID***/
	private Long item_id = 0l ;
	public Long getItem_id(){
	  return item_id;
	}
	public void setItem_id(Long item_id){
	this.item_id = item_id;
	}

	/***是否绑定，可以忽略***/
	private Integer is_bind = 0 ;
	public Integer getIs_bind(){
	  return is_bind;
	}
	public void setIs_bind(Integer is_bind){
	this.is_bind = is_bind;
	}

	/***强化等级，可以忽略***/
	private Integer strengthen_level = 0 ;
	public Integer getStrengthen_level(){
	  return strengthen_level;
	}
	public void setStrengthen_level(Integer strengthen_level){
	this.strengthen_level = strengthen_level;
	}

	/***道具数量***/
	private Long item_amount = 0l ;
	public Long getItem_amount(){
	  return item_amount;
	}
	public void setItem_amount(Long item_amount){
	this.item_amount = item_amount;
	}

	/***道具位置***/
	private Long item_position = 0l ;
	public Long getItem_position(){
	  return item_position;
	}
	public void setItem_position(Long item_position){
	this.item_position = item_position;
	}

	/***记录时间***/
	private Integer happend_time = 0 ;
	public Integer getHappend_time(){
	  return happend_time;
	}
	public void setHappend_time(Integer happend_time){
	this.happend_time = happend_time;
	}

	/***道具状态，可忽略***/
	private Integer state = 0 ;
	public Integer getState(){
	  return state;
	}
	public void setState(Integer state){
	this.state = state;
	}

	/***背包类型，与item_position共同指定位置（bag_type指定道具在背包，银行或者身上等，而item_position指定所在的具体坐标）,可忽略***/
	private Integer bag_type = 0 ;
	public Integer getBag_type(){
	  return bag_type;
	}
	public void setBag_type(Integer bag_type){
	this.bag_type = bag_type;
	}

	public tbllog_user_item() {
	}

	public tbllog_user_item(String platform, Long role_id, String account_name, String role_name, Long item_id, Integer is_bind, Integer strengthen_level, Long item_amount, Long item_position, Integer happend_time, Integer state, Integer bag_type){
		this.platform = platform;
		this.role_id = role_id;
		this.account_name = account_name;
		this.role_name = role_name;
		this.item_id = item_id;
		this.is_bind = is_bind;
		this.strengthen_level = strengthen_level;
		this.item_amount = item_amount;
		this.item_position = item_position;
		this.happend_time = happend_time;
		this.state = state;
		this.bag_type = bag_type;
	}

}
