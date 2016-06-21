package com.znl.node.battle.skill.skillActions;

import com.znl.node.battle.entity.PuppetEntity;
import com.znl.node.battle.skill.Skill;

public class ChangeAttrAction extends SkillAction {

	protected PuppetEntity owner;
	@Override
	public void onEnter(Skill skill) {
		super.onEnter(skill);
		owner = getCaster();
		this.onChangeAttr();
	}
	
	protected void changeAttr(Integer key, int value){
		int oldValue = owner.getAttrValue(key);
		int newValue = oldValue + value;
		owner.setAttrValue(key, newValue);
	}
	
	protected void onChangeAttr(){
		super.endAction();
	}
}
