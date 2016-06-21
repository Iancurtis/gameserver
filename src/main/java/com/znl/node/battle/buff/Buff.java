package com.znl.node.battle.buff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.znl.define.SoldierDefine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.znl.node.battle.consts.BattleConst.BuffTickType;
import com.znl.node.battle.consts.BattleConst.BuffType;
import com.znl.define.BattleDefine;
import com.znl.node.battle.entity.PuppetEntity;
import com.znl.node.battle.interfaces.IDepose;
import com.znl.node.battle.skill.Skill;
import com.znl.node.battle.skill.skillActions.SkillAction;

public class Buff implements IDepose{
	
	private Logger log = LoggerFactory.getLogger(Buff.class);
	
	protected PuppetEntity role; //buff 拥有者
	protected PuppetEntity caster = null; //buff的来源者
	
	protected int id;  //buff id
	protected int layer; //buff 层数
	protected BuffTickType tickType = BuffTickType.RoundStart; //触发类型 回合开始触发，回合结束触发
	protected int iconId = 101;
	protected BuffType buffType = BuffType.Buff; //buff类型
	
	protected int lastRound = 0; //持续回合
	protected Integer beHurtCount = null; //被打触发几次
	protected Integer onHitCount = null; //攻击触发几次
	
	
	private Map<Integer, Object> _tempAttrMap;
	
	public Buff(PuppetEntity role)
    {
    	this.role = role;
    	_tempAttrMap = new HashMap<>();
    }

	@Override
	public void depose()
	{
		_tempAttrMap.clear();
		_tempAttrMap = null;
		tickType = null;
	}
	
	public void finalize()
	{
		_tempAttrMap.clear();
		_tempAttrMap = null;
		tickType = null;
		this.role.removeBuff(this.id);
	}
	
	private void resetAttr()
	{
		for (Entry<Integer, Object> entry : _tempAttrMap.entrySet()) {
			this.role.setBuffAttrValue(entry.getKey(), entry.getValue());
		}
	}
	
	//buff添加时触发
	public void onOccur()
	{
		
	}
	
	//buff回合触发
	public void onRoundTick()
	{
		this.lastRound -= 1;
		if (this.lastRound <= 0)
		{
			this.onRemoved();
		}
	}
	
	/***
	 * 其他buff被删除触发
	 * 对于状态Buff,可以解决多个相同类型Buff删除后的bug
	 */
	public void onOtherRemoved()
	{
		
	}
	
	//buff删除触发
	public void onRemoved()
	{
		this.resetAttr();
		this.finalize();
	}
	
	//攻击触发
	public void onHit(PuppetEntity target)
	{
		if(beHurtCount != null)
		{
			this.beHurtCount -= 1;
			if(this.beHurtCount <= 0)
			{
				this.onRemoved();
			}
		}
	}
	
	//受伤害触发 caster伤害来源者
	public void onBeHurt(PuppetEntity caster)
	{
		if(onHitCount != null)
		{
			this.onHitCount -= 1;
			if(this.onHitCount <= 0)
			{
				this.onRemoved();
			}
		}
	}
	
	//被杀后触发 caster被击杀的来源者
	public void onAfterBeKilled()
	{
		
	}
	
	//杀人后触发
	public void onKilled(){
		
	}
	
	/***
	 * 给改Buff的拥有者再添加Buff
	 */
	protected void addRoleBuff(int buffId){
		role.addBuff(buffId, role.getAttrValue(SoldierDefine.NOR_POWER_INDEX), 1);
	}
	
	/***
	 * 给该Buff的施加者添加buff
	 * @param buffId
	 */
	protected void addCasterBuff(int buffId){
		caster.addBuff(buffId, role.getAttrValue(SoldierDefine.NOR_POWER_INDEX), 1);
	}
	
	/***
	 * 给目标添加Buff
	 * @param target
	 * @param buffId
	 */
	protected void addTargetBuff(PuppetEntity target, int buffId){
		target.addBuff(buffId, role.getAttrValue(SoldierDefine.NOR_POWER_INDEX), 1);
	}
	
	protected void insertActionList(List<SkillAction> list)
	{
		log.debug("=============插入动作列表===============");
		Map<Short, Object> cacheMap = new HashMap<Short, Object>();
		cacheMap.put(BattleDefine.ACTION_CACHE_TARGETS, new ArrayList<PuppetEntity>());

		SkillAction skillAction = null; //倒序插入，最前一个最后链接上去
		int size = list.size();
		for (int i = size - 1; i >= 0; i--) {
			skillAction = list.get(i);
			skillAction.setCaster(caster);
			skillAction.setCacheMap(cacheMap);
			insertAction(skillAction);
		}
	}
	
	private void insertAction(SkillAction action)
	{
		Skill skill = caster.getCurUseSkill();
		if(skill == null){
			log.error("没有释放技能，就触发了Buff插入动作！！！");
			return;
		}
		skill.insertAction(action);
	}
	
	//
	protected void modifyAttr(int key, Object value, boolean isTemp, Object tempValue)
	{
//		Object oldValue = role.getAttrValue(key);
		
		if(isTemp == true)
		{
			_tempAttrMap.put(key, tempValue);
		}
		
		role.setBuffAttrValue(key, value);
		
	}
	
	//血量改变
	protected void changeHp(int value)
	{
		int oldHp = role.getAttrValue(SoldierDefine.POWER_hp);
		role.setBuffAttrValue(SoldierDefine.POWER_hp, oldHp + value);
		
		//TODO 需要处理数据给客户端
//		role.addBloos(-value);
	}
	
	protected boolean roll(int num){
    	boolean result = false;  
    	int randomNum = getRandom(1, 1000);
    	if(randomNum > num){
    		result = false;
    	}else
    	{
    		result = true;
    	}
    	
    	return result;
    }
	
	protected int getRandom(int start, int end)
	{
		return start + (int) Math.rint( Math.random() * (end - start)) ;
	}
	
	
	public PuppetEntity getRole() {
		return role;
	}
	
	public BuffTickType getTickType() {
		return tickType;
	}


	public int getLastRound() {
		return lastRound;
	}
	
	public int getId() {
		return id;
	}
	
	public int getIconId(){
		return iconId;
	}
	
	public PuppetEntity getCaster() {
		return caster;
	}

	public void setCaster(PuppetEntity caster) {
		this.caster = caster;
	}

	public BuffType getBuffType() {
		return buffType;
	}

}
