package com.znl.log.admin;
import com.znl.base.BaseLog;
/*
 *auto export class：
 *@author woko
 */
public class tbllog_player extends BaseLog{
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

	/***角色名***/
	private String role_name = "" ;
	public String getRole_name(){
	  return role_name;
	}
	public void setRole_name(String role_name){
	this.role_name = role_name;
	}

	/***平台唯一用户标识（UID）***/
	private String account_name = "" ;
	public String getAccount_name(){
	  return account_name;
	}
	public void setAccount_name(String account_name){
	this.account_name = account_name;
	}

	/***玩家帐号名称***/
	private String user_name = "" ;
	public String getUser_name(){
	  return user_name;
	}
	public void setUser_name(String user_name){
	this.user_name = user_name;
	}

	/***阵营***/
	private String dim_nation = "" ;
	public String getDim_nation(){
	  return dim_nation;
	}
	public void setDim_nation(String dim_nation){
	this.dim_nation = dim_nation;
	}

	/***职业***/
	private Integer dim_prof = 0 ;
	public Integer getDim_prof(){
	  return dim_prof;
	}
	public void setDim_prof(Integer dim_prof){
	this.dim_prof = dim_prof;
	}

	/***性别(0=女，1=男，2=未知)***/
	private Integer dim_sex = 0 ;
	public Integer getDim_sex(){
	  return dim_sex;
	}
	public void setDim_sex(Integer dim_sex){
	this.dim_sex = dim_sex;
	}

	/***注册时间***/
	private Integer reg_time = 0 ;
	public Integer getReg_time(){
	  return reg_time;
	}
	public void setReg_time(Integer reg_time){
	this.reg_time = reg_time;
	}

	/***注册IP***/
	private String reg_ip = "" ;
	public String getReg_ip(){
	  return reg_ip;
	}
	public void setReg_ip(String reg_ip){
	this.reg_ip = reg_ip;
	}

	/***用户设备ID***/
	private String did = "" ;
	public String getDid(){
	  return did;
	}
	public void setDid(String did){
	this.did = did;
	}

	/***用户等级***/
	private Integer dim_level = 0 ;
	public Integer getDim_level(){
	  return dim_level;
	}
	public void setDim_level(Integer dim_level){
	this.dim_level = dim_level;
	}

	/***VIP等级***/
	private Integer dim_vip_level = 0 ;
	public Integer getDim_vip_level(){
	  return dim_vip_level;
	}
	public void setDim_vip_level(Integer dim_vip_level){
	this.dim_vip_level = dim_vip_level;
	}

	/***当前经验***/
	private Long dim_exp = 0l ;
	public Long getDim_exp(){
	  return dim_exp;
	}
	public void setDim_exp(Long dim_exp){
	this.dim_exp = dim_exp;
	}

	/***帮派名称***/
	private String dim_guild = "" ;
	public String getDim_guild(){
	  return dim_guild;
	}
	public void setDim_guild(String dim_guild){
	this.dim_guild = dim_guild;
	}

	/***战斗力***/
	private Long dim_power = 0l ;
	public Long getDim_power(){
	  return dim_power;
	}
	public void setDim_power(Long dim_power){
	this.dim_power = dim_power;
	}

	/***铁锭(铁矿)***/
	private Long dim_iron = 0l ;
	public Long getDim_iron(){
	  return dim_iron;
	}
	public void setDim_iron(Long dim_iron){
	this.dim_iron = dim_iron;
	}

	/***银两（宝石）***/
	private Long dim_tael = 0l ;
	public Long getDim_tael(){
	  return dim_tael;
	}
	public void setDim_tael(Long dim_tael){
	this.dim_tael = dim_tael;
	}

	/***木材（铜矿）***/
	private Long dim_wood = 0l ;
	public Long getDim_wood(){
	  return dim_wood;
	}
	public void setDim_wood(Long dim_wood){
	this.dim_wood = dim_wood;
	}

	/***石料（石油）***/
	private Long dim_stones = 0l ;
	public Long getDim_stones(){
	  return dim_stones;
	}
	public void setDim_stones(Long dim_stones){
	this.dim_stones = dim_stones;
	}

	/***粮食（硅矿）***/
	private Long dim_food = 0l ;
	public Long getDim_food(){
	  return dim_food;
	}
	public void setDim_food(Long dim_food){
	this.dim_food = dim_food;
	}

	/***元宝数（充值兑换货币）***/
	private Long gold_number = 0l ;
	public Long getGold_number(){
	  return gold_number;
	}
	public void setGold_number(Long gold_number){
	this.gold_number = gold_number;
	}

	/***绑定元宝数（非充值兑换货币）***/
	private Long bgold_number = 0l ;
	public Long getBgold_number(){
	  return bgold_number;
	}
	public void setBgold_number(Long bgold_number){
	this.bgold_number = bgold_number;
	}

	/***金币数***/
	private Long coin_number = 0l ;
	public Long getCoin_number(){
	  return coin_number;
	}
	public void setCoin_number(Long coin_number){
	this.coin_number = coin_number;
	}

	/***绑定金币数***/
	private Long bcoin_number = 0l ;
	public Long getBcoin_number(){
	  return bcoin_number;
	}
	public void setBcoin_number(Long bcoin_number){
	this.bcoin_number = bcoin_number;
	}

	/***总充值***/
	private Long pay_money = 0l ;
	public Long getPay_money(){
	  return pay_money;
	}
	public void setPay_money(Long pay_money){
	this.pay_money = pay_money;
	}

	/***首充时间***/
	private Long first_pay_time = 0l ;
	public Long getFirst_pay_time(){
	  return first_pay_time;
	}
	public void setFirst_pay_time(Long first_pay_time){
	this.first_pay_time = first_pay_time;
	}

	/***最后充值时间***/
	private Long last_pay_time = 0l ;
	public Long getLast_pay_time(){
	  return last_pay_time;
	}
	public void setLast_pay_time(Long last_pay_time){
	this.last_pay_time = last_pay_time;
	}

	/***最后登录时间***/
	private Integer last_login_time = 0 ;
	public Integer getLast_login_time(){
	  return last_login_time;
	}
	public void setLast_login_time(Integer last_login_time){
	this.last_login_time = last_login_time;
	}

	/***事件发生时间***/
	private Integer happend_time = 0 ;
	public Integer getHappend_time(){
		return happend_time;
	}
	public void setHappend_time(Integer happend_time){
		this.happend_time = happend_time;
	}

	public tbllog_player() {
	}

	public tbllog_player(String platform, Long role_id, String role_name, String account_name, String user_name, String dim_nation, Integer dim_prof, Integer dim_sex, Integer reg_time, String reg_ip, String did, Integer dim_level, Integer dim_vip_level, Long dim_exp, String dim_guild, Long dim_power, Long dim_iron, Long dim_tael, Long dim_wood, Long dim_stones, Long dim_food, Long gold_number, Long bgold_number, Long coin_number, Long bcoin_number, Long pay_money, Long first_pay_time, Long last_pay_time, Integer last_login_time){
		this.platform = platform;
		this.role_id = role_id;
		this.role_name = role_name;
		this.account_name = account_name;
		this.user_name = user_name;
		this.dim_nation = dim_nation;
		this.dim_prof = dim_prof;
		this.dim_sex = dim_sex;
		this.reg_time = reg_time;
		this.reg_ip = reg_ip;
		this.did = did;
		this.dim_level = dim_level;
		this.dim_vip_level = dim_vip_level;
		this.dim_exp = dim_exp;
		this.dim_guild = dim_guild;
		this.dim_power = dim_power;
		this.dim_iron = dim_iron;
		this.dim_tael = dim_tael;
		this.dim_wood = dim_wood;
		this.dim_stones = dim_stones;
		this.dim_food = dim_food;
		this.gold_number = gold_number;
		this.bgold_number = bgold_number;
		this.coin_number = coin_number;
		this.bcoin_number = bcoin_number;
		this.pay_money = pay_money;
		this.first_pay_time = first_pay_time;
		this.last_pay_time = last_pay_time;
		this.last_login_time = last_login_time;
	}

}
