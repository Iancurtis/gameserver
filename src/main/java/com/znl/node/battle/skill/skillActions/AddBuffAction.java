package com.znl.node.battle.skill.skillActions;

import java.util.List;

import com.znl.define.SoldierDefine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.znl.node.battle.buff.Buff;
import com.znl.define.BattleDefine;
import com.znl.node.battle.consts.BattleConst.AddBuffType;
import com.znl.node.battle.consts.BattleConst.BuffType;
import com.znl.node.battle.consts.BattleConst.Camp;
import com.znl.node.battle.entity.PuppetEntity;
import com.znl.node.battle.factory.EntityFactory;
import com.znl.node.battle.skill.Skill;

public class AddBuffAction extends SkillAction{
	private Logger log = LoggerFactory.getLogger(AddBuffAction.class);
	
	protected int buffId;
	protected int buffLayer = 1;
	protected AddBuffType addBuffType = AddBuffType.SkillSelect;
	protected BuffType buffType = BuffType.Buff;
	
    public AddBuffAction()
    {
    	super.actionName = "AddBuffAction";
    }
    
    @Override
    public void depose() {
    	super.depose();
    	addBuffType = null;
    	buffType = null;
    }
    
    @Override
    public void onEnter(Skill skill) {
    	super.onEnter(skill);
    	
    	PuppetEntity owner = getCaster();
    	
    	log.debug("=========进入添加buff回合=============" + owner.getAttrValue(SoldierDefine.NOR_POWER_INDEX));
    	
    	if(addBuffType == AddBuffType.SkillSelect){
    		List<PuppetEntity> targets = getCacheData(BattleDefine.ACTION_CACHE_TARGETS);
    		for (PuppetEntity target : targets) {
    			int is_immune = target.getAttrValue(SoldierDefine.NOR_POWER_IS_IMMUNE);
    			if(buffType == BuffType.Debuff){
    				if(is_immune != 1){
    					if(canAdd(target)){
    						Buff buff = target.addBuff(buffId, owner.getAttrValue(SoldierDefine.NOR_POWER_INDEX), buffLayer);
        					buff.setCaster(owner);
    					}
    				}else{
    					log.info("======免疫一切debuff===========");
    				}
    			}
    			else{
    				if(canAdd(target)){
    				   Buff buff = target.addBuff(buffId, owner.getAttrValue(SoldierDefine.NOR_POWER_INDEX), buffLayer);
    				   buff.setCaster(owner);
    				}
    			}
    		}
    	}
    	else if(addBuffType == AddBuffType.Self)
    	{
    		Buff buff = owner.addBuff(buffId, owner.getAttrValue(SoldierDefine.NOR_POWER_INDEX), buffLayer);
    		buff.setCaster(owner);
    	}else if(addBuffType == AddBuffType.SelfAll){
    		Camp camp = owner.getAttrValue(SoldierDefine.NOR_POWER_CAMP);
    		EntityFactory factory = owner.getFactory();
    		List<PuppetEntity> targets = factory.getSameCampLiveEntity(camp);
    		for (PuppetEntity target : targets) {
    			if(canAdd(target)){
    				Buff buff = target.addBuff(buffId, target.getAttrValue(SoldierDefine.NOR_POWER_INDEX), buffLayer);
        			buff.setCaster(owner);
    			}
    			
			}
    	}
    	
    	
    	super.endAction();
    }
    
    public void addBuff(PuppetEntity caster, List<PuppetEntity> targets){
    	for (PuppetEntity target : targets) {
			if(canAdd(target)){
				Buff buff = target.addBuff(buffId, target.getAttrValue(SoldierDefine.NOR_POWER_INDEX), buffLayer);
    			buff.setCaster(caster);
			}
			
		}
    }
    
    protected boolean canAdd(PuppetEntity target) {
		return true;
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
}
