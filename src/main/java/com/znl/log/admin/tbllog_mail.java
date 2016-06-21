package com.znl.log.admin;
import com.znl.base.BaseLog;
/*
 *auto export class：
 *@author woko
 */
public class tbllog_mail extends BaseLog{
	/***所属平台***/
	private String platform = "" ;
	public String getPlatform(){
	  return platform;
	}
	public void setPlatform(String platform){
	this.platform = platform;
	}

	/***邮件ID***/
	private Integer mail_id = 0 ;
	public Integer getMail_id(){
	  return mail_id;
	}
	public void setMail_id(Integer mail_id){
	this.mail_id = mail_id;
	}

	/***发送者ID（角色ID）***/
	private Long mail_sender_id = 0l ;
	public Long getMail_sender_id(){
	  return mail_sender_id;
	}
	public void setMail_sender_id(Long mail_sender_id){
	this.mail_sender_id = mail_sender_id;
	}

	/***发送者平台唯一用户标识***/
	private String mail_sender_name = "" ;
	public String getMail_sender_name(){
	  return mail_sender_name;
	}
	public void setMail_sender_name(String mail_sender_name){
	this.mail_sender_name = mail_sender_name;
	}

	/***接受者ID（角色ID）***/
	private Long mail_receiver_id = 0l ;
	public Long getMail_receiver_id(){
	  return mail_receiver_id;
	}
	public void setMail_receiver_id(Long mail_receiver_id){
	this.mail_receiver_id = mail_receiver_id;
	}

	/***接收者平台唯一用户标识***/
	private String mail_receiver_name = "" ;
	public String getMail_receiver_name(){
	  return mail_receiver_name;
	}
	public void setMail_receiver_name(String mail_receiver_name){
	this.mail_receiver_name = mail_receiver_name;
	}

	/***邮件标题***/
	private String mail_title = "" ;
	public String getMail_title(){
	  return mail_title;
	}
	public void setMail_title(String mail_title){
	this.mail_title = mail_title;
	}

	/***邮件内容***/
	private String mail_content = "" ;
	public String getMail_content(){
	  return mail_content;
	}
	public void setMail_content(String mail_content){
	this.mail_content = mail_content;
	}

	/***邮件类型（0系统邮件，1用户邮件）***/
	private Integer mail_type = 0 ;
	public Integer getMail_type(){
	  return mail_type;
	}
	public void setMail_type(Integer mail_type){
	this.mail_type = mail_type;
	}

	/***货币类型：数量，组合用逗号隔，如<gold:1,bind_gold:2>***/
	private String mail_money_list = "" ;
	public String getMail_money_list(){
	  return mail_money_list;
	}
	public void setMail_money_list(String mail_money_list){
	this.mail_money_list = mail_money_list;
	}

	/***道具id：数量，组合用逗号隔开，如<item1:1，item2:2>***/
	private String mail_item_list = "" ;
	public String getMail_item_list(){
	  return mail_item_list;
	}
	public void setMail_item_list(String mail_item_list){
	this.mail_item_list = mail_item_list;
	}

	/***邮件接收状态（1=已读，2=未读）***/
	private Integer mail_status = 0 ;
	public Integer getMail_status(){
	  return mail_status;
	}
	public void setMail_status(Integer mail_status){
	this.mail_status = mail_status;
	}

	/***物品领取状态（1=已领取，2=未领取）***/
	private Integer get_status = 0 ;
	public Integer getGet_status(){
	  return get_status;
	}
	public void setGet_status(Integer get_status){
	this.get_status = get_status;
	}

	/***变动时间***/
	private Integer happend_time = 0 ;
	public Integer getHappend_time(){
	  return happend_time;
	}
	public void setHappend_time(Integer happend_time){
	this.happend_time = happend_time;
	}

	/***邮件发送时间***/
	private Integer log_time = 0 ;
	public Integer getLog_time(){
	  return log_time;
	}
	public void setLog_time(Integer log_time){
	this.log_time = log_time;
	}

	public tbllog_mail() {
	}

	public tbllog_mail(String platform, Integer mail_id, Long mail_sender_id, String mail_sender_name, Long mail_receiver_id, String mail_receiver_name, String mail_title, String mail_content, Integer mail_type, String mail_money_list, String mail_item_list, Integer mail_status, Integer get_status, Integer happend_time, Integer log_time){
		this.platform = platform;
		this.mail_id = mail_id;
		this.mail_sender_id = mail_sender_id;
		this.mail_sender_name = mail_sender_name;
		this.mail_receiver_id = mail_receiver_id;
		this.mail_receiver_name = mail_receiver_name;
		this.mail_title = mail_title;
		this.mail_content = mail_content;
		this.mail_type = mail_type;
		this.mail_money_list = mail_money_list;
		this.mail_item_list = mail_item_list;
		this.mail_status = mail_status;
		this.get_status = get_status;
		this.happend_time = happend_time;
		this.log_time = log_time;
	}

}
