package com.znl.log.admin;
import com.znl.base.BaseLog;
/*
 *auto export class：
 *@author woko
 */
public class tbllog_guild extends BaseLog{
	/***申请、退出等发起操作角色ID***/
	private Long member_role_id = 0l ;
	public Long getMember_role_id(){
	  return member_role_id;
	}
	public void setMember_role_id(Long member_role_id){
	this.member_role_id = member_role_id;
	}

	/***批准加入的操作的角色ID***/
	private Long opt_role_id = 0l ;
	public Long getOpt_role_id(){
	  return opt_role_id;
	}
	public void setOpt_role_id(Long opt_role_id){
	this.opt_role_id = opt_role_id;
	}

	/***帮派ID***/
	private Integer guild_id = 0 ;
	public Integer getGuild_id(){
	  return guild_id;
	}
	public void setGuild_id(Integer guild_id){
	this.guild_id = guild_id;
	}

	/***帮派名称***/
	private String guild_name = "" ;
	public String getGuild_name(){
	  return guild_name;
	}
	public void setGuild_name(String guild_name){
	this.guild_name = guild_name;
	}

	/***操作类型（1-申请，2-加入，3-退出）***/
	private Integer opt = 0 ;
	public Integer getOpt(){
	  return opt;
	}
	public void setOpt(Integer opt){
	this.opt = opt;
	}

	/***帮派积分***/
	private Long score = 0l ;
	public Long getScore(){
	  return score;
	}
	public void setScore(Long score){
	this.score = score;
	}

	/***帮派贡献***/
	private Integer contribution = 0 ;
	public Integer getContribution(){
	  return contribution;
	}
	public void setContribution(Integer contribution){
	this.contribution = contribution;
	}

	/***事件发生时间***/
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

	public tbllog_guild() {
	}

	public tbllog_guild(Long member_role_id, Long opt_role_id, Integer guild_id, String guild_name, Integer opt, Long score, Integer contribution, Integer happend_time, Integer log_time){
		this.member_role_id = member_role_id;
		this.opt_role_id = opt_role_id;
		this.guild_id = guild_id;
		this.guild_name = guild_name;
		this.opt = opt;
		this.score = score;
		this.contribution = contribution;
		this.happend_time = happend_time;
		this.log_time = log_time;
	}

}
