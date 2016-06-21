package com.znl.base;

/**
 * Created by Administrator on 2015/11/9.
 */
/**基本的战斗属性*/
public abstract class BasicBattleAttr {

    /***
     * 技能固定伤害
     * @param skillId
     * @return
     */
    public abstract int getSkillFixdam(int skillId);
    public abstract Object getValue(Integer key);
    public abstract void setValue(Integer key, Object value);
    public abstract void reset();
}
