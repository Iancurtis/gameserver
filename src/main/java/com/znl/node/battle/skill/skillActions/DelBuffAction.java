package com.znl.node.battle.skill.skillActions;

import java.util.List;

import com.znl.define.SoldierDefine;
import com.znl.node.battle.buff.Buff;
import com.znl.node.battle.consts.BattleConst.DelBuffType;
import com.znl.define.BattleDefine;
import com.znl.node.battle.consts.BattleConst.BuffType;
import com.znl.node.battle.consts.BattleConst.Camp;
import com.znl.node.battle.entity.PuppetEntity;
import com.znl.node.battle.factory.EntityFactory;
import com.znl.node.battle.skill.Skill;

public class DelBuffAction extends SkillAction {

	protected BuffType delBuffType = BuffType.Buff;
	protected DelBuffType delBuffSelectType = DelBuffType.SkillSelect;
	
	//删除相关类型的所有Buff
	public DelBuffAction(){
		super.actionName = "DelBuffAction";
	}
	
	@Override
    public void onEnter(Skill skill) {
    	super.onEnter(skill);
    	
    	PuppetEntity owner = getCaster();
    	
    	if(delBuffSelectType == DelBuffType.SkillSelect){
    		List<PuppetEntity> targets = getCacheData(BattleDefine.ACTION_CACHE_TARGETS);
        	for (PuppetEntity target : targets) {
        		delBuff(target);
    		}
    	}
    	else if(delBuffSelectType == DelBuffType.Self)
    	{
    		delBuff(owner);
    	}else if(delBuffSelectType == DelBuffType.SelfAll){
    		Camp camp = owner.getAttrValue(SoldierDefine.NOR_POWER_CAMP);
    		EntityFactory factory = owner.getFactory();
    		List<PuppetEntity> targets = factory.getSameCampLiveEntity(camp);
    		for (PuppetEntity target : targets) {
    			delBuff(target);
			}
    	}
    	
    	super.endAction();
	}
	
	private void delBuff(PuppetEntity target){
		List<Buff> buffList = target.getBuffList();
		for (Buff buff : buffList) {
			if(buff.getBuffType() == delBuffType ){
				target.onTriggerRemoveBuff(buff.getId());
			}
		}
	}
}
