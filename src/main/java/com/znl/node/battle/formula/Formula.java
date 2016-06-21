package com.znl.node.battle.formula;

import com.znl.node.battle.entity.PuppetEntity;

public class Formula {
    private Metic _metic;
    
    public Formula()
    {
    	_metic = new Metic();
    }
    
    public<T> T metic(int meticId, PuppetEntity caster, PuppetEntity target)
    {
    	return _metic.getMeticValue(meticId, caster, target);
    }
    
    public<T> T isCrit(PuppetEntity caster, PuppetEntity target)
    {
    	return metic(10001, caster, target);
    }
    
    public<T> T isHit(PuppetEntity caster, PuppetEntity target)
    {
    	return metic(10002, caster, target);
    }
    
    public<T> T isDodge(PuppetEntity caster, PuppetEntity target)
    {
    	return metic(10003, caster, target);
    }
    
    /**正常机枪伤害*/
    public<T> T normalCannonDamage(PuppetEntity caster, PuppetEntity target)
    {
    	return metic(10004, caster, target);
    }
    
    /**正常火炮伤害*/
    public<T> T normalMissileDamage(PuppetEntity caster, PuppetEntity target)
    {
    	return metic(10005, caster, target);
    }
    
    /**暴击机枪伤害*/
    public<T> T critCannonDamage(PuppetEntity caster, PuppetEntity target)
    {
    	return metic(10006, caster, target);
    }
    
    /**暴击火炮伤害*/
    public<T> T critMissileDamage(PuppetEntity caster, PuppetEntity target)
    {
    	return metic(10007, caster, target);
    }
    
    /**格挡机枪伤害*/
    public<T> T dodgeCannonDamage(PuppetEntity caster, PuppetEntity target)
    {
    	return metic(10008, caster, target);
    }
    
    /**格挡火炮伤害*/
    public<T> T dodgeMissileDamage(PuppetEntity caster, PuppetEntity target)
    {
    	return metic(10009, caster, target);
    }
}
