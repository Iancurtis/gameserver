package com.znl.node.battle.skill.skillActions;

import java.util.List;

import com.znl.define.SoldierDefine;
import com.znl.define.BattleDefine;
import com.znl.node.battle.consts.BattleConst.HurtType;
import com.znl.node.battle.entity.PuppetEntity;
import com.znl.node.battle.skill.Skill;


public class ChangeHPAction extends SkillAction {

	@Override
	public void onEnter(Skill skill) {
		super.onEnter(skill);
		
		PuppetEntity caster = getCaster();
		
		HurtType hurtType = HurtType.NormalHurt;
		List<PuppetEntity> targets = getCacheData(BattleDefine.ACTION_CACHE_TARGETS);
		for (PuppetEntity target : targets) {
			int dt = changeHP(caster, target);
			int curHp = caster.getAttrValue(SoldierDefine.POWER_hp);
			int hp = curHp + dt;
			caster.setAttrValue(SoldierDefine.POWER_hp, hp);
			
			if(dt < 0){
				hurtType = HurtType.NormalHurt;
			}else{
				hurtType = HurtType.AddHpHurt;
			}
			
			caster.addBloos(-dt, hurtType.value());
		}
		
		super.endAction();
		
	}
	
	public int changeHP(PuppetEntity caster, PuppetEntity target){
		return 0;
	}

}
