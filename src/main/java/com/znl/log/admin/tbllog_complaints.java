package com.znl.log.admin;
import com.znl.base.BaseLog;
/*
 *auto export class：
 *@author woko
 */
public class tbllog_complaints extends BaseLog{
	/***所属平台***/
	private String platform = "" ;
	public String getPlatform(){
	  return platform;
	}
	public void setPlatform(String platform){
	this.platform = platform;
	}

	/***投诉编号，区服唯一***/
	private Integer complaint_id = 0 ;
	public Integer getComplaint_id(){
	  return complaint_id;
	}
	public void setComplaint_id(Integer complaint_id){
	this.complaint_id = complaint_id;
	}

	/***角色ID***/
	private Long role_id = 0l ;
	public Long getRole_id(){
	  return role_id;
	}
	public void setRole_id(Long role_id){
	this.role_id = role_id;
	}

	/***角色名称***/
	private String role_name = "" ;
	public String getRole_name(){
	  return role_name;
	}
	public void setRole_name(String role_name){
	this.role_name = role_name;
	}

	/***平台唯一用户标识***/
	private String account_name = "" ;
	public String getAccount_name(){
	  return account_name;
	}
	public void setAccount_name(String account_name){
	this.account_name = account_name;
	}

	/***游戏简称（由XXX填写）***/
	private String game_abbrv = "" ;
	public String getGame_abbrv(){
	  return game_abbrv;
	}
	public void setGame_abbrv(String game_abbrv){
	this.game_abbrv = game_abbrv;
	}

	/***游戏服编号（由XXX填写）***/
	private Integer sid = 0 ;
	public Integer getSid(){
	  return sid;
	}
	public void setSid(Integer sid){
	this.sid = sid;
	}

	/***投诉类型("全部",11="bug",12="投诉",13="建议",10="其他", 15="咨询"")***/
	private Integer complaint_type = 0 ;
	public Integer getComplaint_type(){
	  return complaint_type;
	}
	public void setComplaint_type(Integer complaint_type){
	this.complaint_type = complaint_type;
	}

	/***投诉的标题***/
	private String complaint_title = "" ;
	public String getComplaint_title(){
	  return complaint_title;
	}
	public void setComplaint_title(String complaint_title){
	this.complaint_title = complaint_title;
	}

	/***投诉的正文***/
	private String complaint_content = "" ;
	public String getComplaint_content(){
	  return complaint_content;
	}
	public void setComplaint_content(String complaint_content){
	this.complaint_content = complaint_content;
	}

	/***玩家提交投诉的时间***/
	private Integer complaint_time = 0 ;
	public Integer getComplaint_time(){
	  return complaint_time;
	}
	public void setComplaint_time(Integer complaint_time){
	this.complaint_time = complaint_time;
	}

	/***内部编号（由XXX填写）***/
	private Integer internal_id = 0 ;
	public Integer getInternal_id(){
	  return internal_id;
	}
	public void setInternal_id(Integer internal_id){
	this.internal_id = internal_id;
	}

	/***GM回帖数（由XXX填写）***/
	private Integer reply_cnts = 0 ;
	public Integer getReply_cnts(){
	  return reply_cnts;
	}
	public void setReply_cnts(Integer reply_cnts){
	this.reply_cnts = reply_cnts;
	}

	/***用户IP（可不填）***/
	private String user_ip = "" ;
	public String getUser_ip(){
	  return user_ip;
	}
	public void setUser_ip(String user_ip){
	this.user_ip = user_ip;
	}

	/***代理商名称，如XXX(可不填)***/
	private String agent = "" ;
	public String getAgent(){
	  return agent;
	}
	public void setAgent(String agent){
	this.agent = agent;
	}

	/***玩家已经充值总额（可不填）***/
	private Integer pay_amount = 0 ;
	public Integer getPay_amount(){
	  return pay_amount;
	}
	public void setPay_amount(Integer pay_amount){
	this.pay_amount = pay_amount;
	}

	/***玩家的qq账号（可不填）***/
	private Long qq_account = 0l ;
	public Long getQq_account(){
	  return qq_account;
	}
	public void setQq_account(Long qq_account){
	this.qq_account = qq_account;
	}

	/***玩家等级（可不填）***/
	private Integer dim_level = 0 ;
	public Integer getDim_level(){
	  return dim_level;
	}
	public void setDim_level(Integer dim_level){
	this.dim_level = dim_level;
	}

	/***评分：0未评，1优秀，2一般，3很差（可不填）***/
	private Integer evaluate = 0 ;
	public Integer getEvaluate(){
	  return evaluate;
	}
	public void setEvaluate(Integer evaluate){
	this.evaluate = evaluate;
	}

	/***同步次数（可不填）***/
	private Integer sync_numbers = 0 ;
	public Integer getSync_numbers(){
	  return sync_numbers;
	}
	public void setSync_numbers(Integer sync_numbers){
	this.sync_numbers = sync_numbers;
	}

	/***最后回复时间（可不填）***/
	private Integer last_reply_time = 0 ;
	public Integer getLast_reply_time(){
	  return last_reply_time;
	}
	public void setLast_reply_time(Integer last_reply_time){
	this.last_reply_time = last_reply_time;
	}

	/***是否标记为垃圾问题（可不填）***/
	private Integer is_spam = 0 ;
	public Integer getIs_spam(){
	  return is_spam;
	}
	public void setIs_spam(Integer is_spam){
	this.is_spam = is_spam;
	}

	/***spam注释（可不填）***/
	private String spam_reporter = "" ;
	public String getSpam_reporter(){
	  return spam_reporter;
	}
	public void setSpam_reporter(String spam_reporter){
	this.spam_reporter = spam_reporter;
	}

	/***spam生成时间（可不填）***/
	private Integer spam_time = 0 ;
	public Integer getSpam_time(){
	  return spam_time;
	}
	public void setSpam_time(Integer spam_time){
	this.spam_time = spam_time;
	}

	/***写日志时间***/
	private Integer log_time = 0 ;
	public Integer getLog_time(){
	  return log_time;
	}
	public void setLog_time(Integer log_time){
	this.log_time = log_time;
	}

	public tbllog_complaints() {
	}

	public tbllog_complaints(String platform, Integer complaint_id, Long role_id, String role_name, String account_name, String game_abbrv, Integer sid, Integer complaint_type, String complaint_title, String complaint_content, Integer complaint_time, Integer internal_id, Integer reply_cnts, String user_ip, String agent, Integer pay_amount, Long qq_account, Integer dim_level, Integer evaluate, Integer sync_numbers, Integer last_reply_time, Integer is_spam, String spam_reporter, Integer spam_time, Integer log_time){
		this.platform = platform;
		this.complaint_id = complaint_id;
		this.role_id = role_id;
		this.role_name = role_name;
		this.account_name = account_name;
		this.game_abbrv = game_abbrv;
		this.sid = sid;
		this.complaint_type = complaint_type;
		this.complaint_title = complaint_title;
		this.complaint_content = complaint_content;
		this.complaint_time = complaint_time;
		this.internal_id = internal_id;
		this.reply_cnts = reply_cnts;
		this.user_ip = user_ip;
		this.agent = agent;
		this.pay_amount = pay_amount;
		this.qq_account = qq_account;
		this.dim_level = dim_level;
		this.evaluate = evaluate;
		this.sync_numbers = sync_numbers;
		this.last_reply_time = last_reply_time;
		this.is_spam = is_spam;
		this.spam_reporter = spam_reporter;
		this.spam_time = spam_time;
		this.log_time = log_time;
	}

}
