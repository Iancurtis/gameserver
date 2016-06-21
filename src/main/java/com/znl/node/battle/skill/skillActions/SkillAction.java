package com.znl.node.battle.skill.skillActions;

import java.util.Map;

import com.znl.node.battle.entity.PuppetEntity;
import com.znl.node.battle.interfaces.IDepose;
import com.znl.node.battle.skill.Skill;

public abstract class SkillAction implements IDepose{
    protected Skill skill;
    protected String actionName;
    
    private PuppetEntity caster; //该action的发起者
    private Map<Short, Object> cacheMap; //action组 共同共享的缓存

	@Override
    public void depose() {
    	skill = null;
    	actionName = null;
    }
    
    public void onEnter(Skill skill)
    {
    	this.skill = skill;
    }
    
    public void endAction()
    {
    	this.skill.nextAction();
    }

	public void setCacheMap(Map<Short, Object> cacheMap) {
		this.cacheMap = cacheMap;
	}
	
	protected void putCacheData(Short key, Object value)
	{
		this.cacheMap.put(key, value);
	}
	
	protected int getRandom(int start, int end)
	{
		return start + (int) Math.rint( Math.random() * (end - start)) ;
	}
	
	@SuppressWarnings("unchecked")
	public<T> T getCacheData(Short key)
	{
		Object value = this.cacheMap.get(key);
		if(value == null)
		{
			return null;
		}
		return (T)value;
	}

	public PuppetEntity getCaster() {
		return caster;
	}

	public void setCaster(PuppetEntity caster) {
		this.caster = caster;
	}
	
	@Override
	public String toString() {
		return "Action名字：" + actionName;
	}
}
