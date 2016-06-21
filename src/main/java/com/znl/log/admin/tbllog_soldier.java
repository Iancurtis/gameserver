package com.znl.log.admin;
import com.znl.base.BaseLog;

/*
 *auto export class：
 *@author woko
 */
public class tbllog_soldier extends BaseLog{
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

	/***操作类型(0是消耗，1是增加)***/
	private Integer opt = 0 ;
	public Integer getOpt(){
	  return opt;
	}
	public void setOpt(Integer opt){
	this.opt = opt;
	}

	/***对应各自项目组的军械碎片消耗项目字典，行为类型（dict_action.action_id)***/
	private Integer action_id = 0 ;
	public Integer getAction_id(){
	  return action_id;
	}
	public void setAction_id(Integer action_id){
	this.action_id = action_id;
	}

	/***佣兵片ID***/
	private Integer type_id = 0 ;

	public Integer getType_id() {
		return type_id;
	}

	public void setType_id(Integer type_id) {
		this.type_id = type_id;
	}

	/***佣兵片获得/消耗数量***/
	private Long soldier_number = 0l ;

	public Long getSoldier_number() {
		return soldier_number;
	}

	public void setSoldier_number(Long soldier_number) {
		this.soldier_number = soldier_number;
	}

	/***佣兵产出所在地图，ID(dict_action.action_id)***/
	private Integer map_id = 0 ;
	public Integer getMap_id(){
	  return map_id;
	}
	public void setMap_id(Integer map_id){
	this.map_id = map_id;
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

	/***写日志时间，索引字段***/
	private Long remain_num = 0l ;

	public Long getRemain_num() {
		return remain_num;
	}

	public void setRemain_num(Long remain_num) {
		this.remain_num = remain_num;
	}

	public tbllog_soldier() {
	}

	public tbllog_soldier(String platform, Long role_id, String account_name, Integer dim_level, Integer opt, Integer action_id, Integer type_id, Long soldier_number, Integer map_id, Integer happend_time, Integer log_time, Long remain_num) {
		this.platform = platform;
		this.role_id = role_id;
		this.account_name = account_name;
		this.dim_level = dim_level;
		this.opt = opt;
		this.action_id = action_id;
		this.type_id = type_id;
		this.soldier_number = soldier_number;
		this.map_id = map_id;
		this.happend_time = happend_time;
		this.log_time = log_time;
		this.remain_num = remain_num;
	}
}
