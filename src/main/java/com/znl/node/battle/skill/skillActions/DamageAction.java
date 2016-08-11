package com.znl.node.battle.skill.skillActions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.znl.define.DataDefine;
import com.znl.define.SoldierDefine;
import com.znl.proxy.ConfigDataProxy;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.znl.define.BattleDefine;
import com.znl.node.battle.consts.BattleConst.BuffTriggerType;
import com.znl.node.battle.consts.BattleConst.DamageType;
import com.znl.node.battle.consts.BattleConst.HurtType;
import com.znl.node.battle.entity.PuppetEntity;
import com.znl.node.battle.formula.Formula;
import com.znl.node.battle.skill.Skill;

public class DamageAction extends SkillAction{
	
	private Logger log = LoggerFactory.getLogger(DamageAction.class);
	
	protected DamageType damageType;
	protected double extra = 0;
	protected int hits = 1;
	
	private Formula _formula;
	
	public DamageAction()
	{
		super.actionName = "DamageAction";
		
		damageType = DamageType.Cannon;
		
		_formula = new Formula();
	}
	
	@Override
    public void depose() {
    	super.depose();
    	damageType = null;
    	_formula = null;
    }
	
	protected double getRatio()
	{
		return 1.2f;
	}
	
	protected double getRatio(PuppetEntity caster, PuppetEntity target)
	{

		List<Integer> castRestrains = caster.getAttrValue(SoldierDefine.NOR_POWER_SOLDIER_RESTRAIN);
		List<Integer> targetRestrains = target.getAttrValue(SoldierDefine.NOR_POWER_SOLDIER_RESTRAIN);
		int castType = caster.getAttrValue(SoldierDefine.NOR_POWER_TYPE);
		int targetType = target.getAttrValue(SoldierDefine.NOR_POWER_TYPE);
		int damRate = 100;
		int resTrainLv = 0;
		for(Integer id : castRestrains){
			JSONObject define = ConfigDataProxy.getConfigInfoFindById(DataDefine.RESTRAIN,id);
			int lv = define.getInt("lv");
			if(castType != define.getInt("Atanktype")){
				continue;
			}else if(targetType != define.getInt("Dtanktype")){
				continue;
			}else if(define.getInt("AtankID") != 0){
				int casterTypeId = caster.getAttrValue(SoldierDefine.NOR_POWER_TYPE_ID);
				if(casterTypeId != define.getInt("AtankID")){
					continue;
				}
			}else if(define.getInt("DtankID") != 0){
				int targetTypeId = target.getAttrValue(SoldierDefine.NOR_POWER_TYPE_ID);
				if(targetTypeId != define.getInt("DtankID")){
					continue;
				}
			}else if(lv > resTrainLv){
				resTrainLv = lv;
				damRate = define.getInt("damRate");
			}
		}
		for(Integer id : targetRestrains){
			JSONObject define = ConfigDataProxy.getConfigInfoFindById(DataDefine.RESTRAIN,id);
			int lv = define.getInt("lv");
			if(castType != define.getInt("Atanktype")){
				continue;
			}else if(targetType != define.getInt("Dtanktype")){
				continue;
			}else if(define.getInt("AtankID") != 0){
				int casterTypeId = caster.getAttrValue(SoldierDefine.NOR_POWER_TYPE_ID);
				if(casterTypeId != define.getInt("AtankID")){
					continue;
				}
			}else if(define.getInt("DtankID") != 0){
				int targetTypeId = target.getAttrValue(SoldierDefine.NOR_POWER_TYPE_ID);
				if(targetTypeId != define.getInt("DtankID")){
					continue;
				}
			}else if(lv > resTrainLv){
				resTrainLv = lv;
				damRate = define.getInt("damRate");
			}
		}

		return damRate / 100.0;
	}
	
	protected double getExtra(PuppetEntity caster, PuppetEntity target)
	{
		this.extra = skill.getSkillFixdam();
		return this.extra;
	}
	
	@Override
	public void onEnter(Skill skill) {
		super.onEnter(skill);
		PuppetEntity caster = super.getCaster();
		log.info("========伤害计算action===============" + caster.getAttrValue(SoldierDefine.NOR_POWER_INDEX));
		checkDamages();
		
		super.endAction();
	}
	
	private void damageAccounting(PuppetEntity ent, HurtType hurtType, int damageValue)
	{
//		log.info("=========打包伤害数据===========");
		ent.addBloos(damageValue, hurtType.value());
	}
	
	private void checkDamages()
	{
		PuppetEntity caster = super.getCaster();
	
		List<PuppetEntity> targets = getCacheData(BattleDefine.ACTION_CACHE_TARGETS);
		
		if(targets == null || targets.size() == 0){
			log.error("=======error====checkDamages==============");
			return;
		}
		
		buffHitTrigger(caster, caster, targets.get(0)); //TODO 受击者暂时只处理一个
		
		checkDamages(caster, targets);
	}
	
	public void checkDamages(PuppetEntity caster, List<PuppetEntity> targets){
		for (PuppetEntity target : targets) {
			checkDamage(caster, target);
		}
	}
	
	private void checkDamage(PuppetEntity caster, PuppetEntity target)
	{
		if(damageType == DamageType.Cannon){ //物理攻击，判断是否对方物理免疫
			int is_immune_normal = target.getAttrValue(SoldierDefine.NOR_POWER_IS_IMMUNE_NORMAL);
			if(is_immune_normal == 1){
				log.info("======物理攻免疫====");
				this.damageAccounting(target, HurtType.ImmuneHurt, 0 );
				return;
			}
		}
		
		if(damageType == DamageType.Missile){ //物理攻击，判断是否对方物理免疫
			int is_immune_magic = target.getAttrValue(SoldierDefine.NOR_POWER_IS_IMMUNE_MAGIC);
			if(is_immune_magic == 1){
				log.info("======魔法攻免疫====");
				this.damageAccounting(target, HurtType.ImmuneHurt, 0 );
				return;
			}
		}
		
		
		int hitformula = skill.skillDefine.getInt("dodmetic");
		boolean isMiss = _formula.metic(hitformula, caster, target);
		
		if(isMiss == true)
		{
			log.info("======未命中====");
			this.damageAccounting(target, HurtType.DodgeHurt, 0 );
			addCount(SoldierDefine.NOR_POWER_TYPE_DODGE_COUNT,target);
		}
		else
		{
			damage(caster, target);
		}
		addCount(SoldierDefine.NOR_POWER_TYPE_BE_ATKED_COUNT,target);
	}
	
	private void damage(PuppetEntity caster, PuppetEntity target)
	{
		int casterHp = caster.getAttrValue(SoldierDefine.POWER_hp);
		log.info("====发动技能着当前血量为"+casterHp);
		double ratio = getRatio(caster, target);
		double extra = getExtra(caster, target);
		
		Map<String, Object> damageResult = getDamage(caster, target,ratio);


		HurtType hurtType = (HurtType) damageResult.get("hurtType");
		double damageBase = (double) damageResult.get("damageBase");
		
		
		int realDamage = (int) Math.rint(damageBase * ratio + extra) ;
		if(realDamage == 0){
			System.out.println("-------------0000000000000---------");
		}
		caster.setAttrValue(SoldierDefine.NOR_POWER_LAST_DAMAGE, realDamage);
		
//		int aIndex = caster.getAttrValue(Define.POWER_INDEX);
		log.info(String.format("=======造成的伤害========realDamage:%d====", realDamage) );
		System.out.println(String.format("=======造成的伤害========realDamage:%d====", realDamage));
		int targetHp = target.getAttrValue(SoldierDefine.POWER_hp);
		int newHp = targetHp - realDamage;
		target.setAttrValue(SoldierDefine.POWER_hp, newHp);

		this.damageAccounting(target, hurtType, realDamage );
		
//		
//		damageVimpire(caster, target, realDamage);
		
		buffBehurtTrigger(target, caster, target);
		
		if(target.isDead() == true){
			buffKillTrigger(caster, caster, target);
		}
	}
	
//	/伤害吸血
//	private void damageVimpire(PuppetEntity caster, PuppetEntity target, int realDamage)
//	{
//		int vimpire = caster.getAttrValue(Define.POWER_VIMPIRE);
//		int vimpireValue = (int) (realDamage * (vimpire / 1000.0));
//		if(vimpireValue > 0){
//			this.damageAccounting(caster, HurtType.AddHpHurt, -vimpireValue );
//		}
//	}
	
	private Map<String, Object> getDamage(PuppetEntity caster, PuppetEntity target,double ratio)
	{
		HurtType hurtType = HurtType.NormalHurt;
		double damageBase = 0.0f;
		
		int criFormula = skill.skillDefine.getInt("crimetic"); //先判定是否暴击
		boolean isCri = _formula.metic(criFormula, caster, target);
		
		int damFormula = 0;

		if(isCri == true){
			damFormula = skill.skillDefine.getInt("cirdam");
			hurtType = HurtType.CritHurt;
			addCount(SoldierDefine.NOR_POWER_TYPE_CIRT_COUNT,caster);
		}else if(ratio > 1){//判断克制类型
			hurtType = HurtType.RefrainHurt;
			damFormula = skill.skillDefine.getInt("normaldam");
		}else{
			damFormula = skill.skillDefine.getInt("normaldam");
		}
		addCount(SoldierDefine.NOR_POWER_TYPE_ATK_COUNT,caster);
		damageBase =  Double.parseDouble(_formula.metic(damFormula, caster, target).toString());
		log.info(String.format("--caster-:%s=>---target:%s-----damageBase:%f",caster.name, target.name, damageBase));
		System.out.println(String.format("--caster-:%s=>---target:%s-----damageBase:%f", caster.name, target.name, damageBase));
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("hurtType", hurtType);
		resultMap.put("damageBase", damageBase);
		
		return resultMap;
	}

	private void addCount(int power,PuppetEntity entity){
		int value = entity.getAttrValue(power);
		entity.setAttrValue(power,value+1);
	}

	protected void buffBehurtTrigger(PuppetEntity entity, PuppetEntity caster, PuppetEntity target){
		entity.buffTrigger(BuffTriggerType.BeHurt,caster, target);
	}
	
	protected void buffHitTrigger(PuppetEntity entity, PuppetEntity caster,PuppetEntity target){
		entity.buffTrigger(BuffTriggerType.Hit, caster, target);
	}
	
	protected void buffKillTrigger(PuppetEntity entity, PuppetEntity caster,PuppetEntity target){
		entity.buffTrigger(BuffTriggerType.Killed, caster, target);
	}
}



