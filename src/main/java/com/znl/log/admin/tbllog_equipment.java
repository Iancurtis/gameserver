package com.znl.log.admin;
import com.znl.base.BaseLog;
/*
 *auto export class：
 *@author woko
 */
public class tbllog_equipment extends BaseLog{
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

	/***装备ID***/
	private Integer item_id = 0 ;
	public Integer getItem_id(){
	  return item_id;
	}
	public void setItem_id(Integer item_id){
	this.item_id = item_id;
	}

	/***装备属性***/
	private Integer item_property = 0 ;
	public Integer getItem_property(){
	  return item_property;
	}
	public void setItem_property(Integer item_property){
	this.item_property = item_property;
	}

	/***装备变化前数值***/
	private Integer value_before = 0 ;
	public Integer getValue_before(){
	  return value_before;
	}
	public void setValue_before(Integer value_before){
	this.value_before = value_before;
	}

	/***装备变化后数值***/
	private Integer value_after = 0 ;
	public Integer getValue_after(){
	  return value_after;
	}
	public void setValue_after(Integer value_after){
	this.value_after = value_after;
	}

	/***装备经过锻造或合成后，装备状态变化的类型。
		1=品质/强化等级2=属性变化通常品质/强化等级发生变化时，属性也会发生变化。
		此时只需要记录是品质/强化等级发生变化即可。当单纯发生属性变化时才记录为属性变化。（通常对应游戏的功能叫“洗练”）***/
	private Integer change_type = 0 ;
	public Integer getChange_type(){
	  return change_type;
	}
	public void setChange_type(Integer change_type){
	this.change_type = change_type;
	}

	/***所需的材料，可能是道具或货币
		通过json格式数组来记录，每个json数组元素第一个值为道具ID或者货币类型，第二个值为数量，第三个值为类型，用于区分道具还是货币，道具为1，货币为2
		[{item_id1, amount1, type},{item_id2, amount2, type},{money_type3, amount3, type}]
		例如：[{111111, 10, 1},{222222, 5, 1},{1, 10000, 2}]***/
	private String material = "" ;
	public String getMaterial(){
	  return material;
	}
	public void setMaterial(String material){
	this.material = material;
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

	public tbllog_equipment() {
	}

	public tbllog_equipment(String platform, Long role_id, String account_name, Integer dim_level, Integer item_id, Integer item_property, Integer value_before, Integer value_after, Integer change_type, String material, Integer happend_time, Integer log_time){
		this.platform = platform;
		this.role_id = role_id;
		this.account_name = account_name;
		this.dim_level = dim_level;
		this.item_id = item_id;
		this.item_property = item_property;
		this.value_before = value_before;
		this.value_after = value_after;
		this.change_type = change_type;
		this.material = material;
		this.happend_time = happend_time;
		this.log_time = log_time;
	}

}
