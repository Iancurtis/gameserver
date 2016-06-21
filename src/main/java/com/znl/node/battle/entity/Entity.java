package com.znl.node.battle.entity;


import com.znl.base.BasicBattleAttr;
import com.znl.define.SoldierDefine;
import com.znl.node.battle.factory.EntityFactory;
import com.znl.node.battle.interfaces.IDepose;

public class Entity implements IDepose {

	private EntityFactory _factory; // 缓存工厂
	protected BasicBattleAttr _attr;

	public String name = "";
	public Integer modelIdList;
//	public List<Integer> colorList;

	public Entity(BasicBattleAttr attr) {
		_attr = attr;
	}

	@Override
	public void depose() {
		_factory = null;
		_attr = null;
	}

	public boolean isDead() {
		int hp = getAttrValue(SoldierDefine.POWER_hp);
		if (hp <= 0) {
			return true;
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	public <T> T getAttrValue(Integer key) {
		Object value = _attr.getValue(key);
		if (value == null) {
			value = 0;
		} else {

		}
		return (T) value;
	}

	/**根据当前血量刷新单位数量和攻击**/
	private void refurceNumAndAtk(){
		int index = getAttrValue(SoldierDefine.NOR_POWER_INDEX);

		int soldierTypeHp = getAttrValue(SoldierDefine.NOR_POWER_SOLDIER_TYPE_HP);
		int hp = getAttrValue(SoldierDefine.POWER_hp);
		int num = hp / soldierTypeHp;
		if(hp % soldierTypeHp > 0){
			num ++;
		}

		setAttrValue(SoldierDefine.NOR_POWER_NUM,num);
		int soldierTypeAtk = getAttrValue(SoldierDefine.NOR_POWER_SOLDIER_TYPE_ATK);
		setAttrValue(SoldierDefine.POWER_atk,num * soldierTypeAtk);
	}

	public void setAttrValue(Integer key, Object value) {

		if (key == SoldierDefine.POWER_hp) // 都是血量，历史兼容
		{
			int hp = (int) value;
			int hp_max = getAttrValue(SoldierDefine.POWER_hpMax);
			if (hp <= 0) {
				value = 0;
			}else if(hp >= hp_max) {
				value = hp_max;
			}
			_attr.setValue(SoldierDefine.POWER_hp, value);
			refurceNumAndAtk();
		} else if (key == SoldierDefine.POWER_hpMax) {
			int hp = (int) value;
			if (hp <= 0) {
				value = 0;
			}

			_attr.setValue(SoldierDefine.POWER_hpMax, value);
		}
//		else if (key == Define.POWER_MP) {
//			int mp = (int) value;
//			int mp_max = getAttrValue(Define.POWER_MP_MAX);
//			if (mp <= 0) {
//				value = 0;
//			} else if (mp >= mp_max) {
//				value = mp_max;
//			}
//		}

		_attr.setValue(key, value);
	}

	protected void setValue(Integer key, Object value) {
		_attr.setValue(key, value);
	}

	public BasicBattleAttr getAttr() {
		return _attr;
	}

	public void resetAttr() {
		_attr.reset();
	}

	public EntityFactory getFactory() {
		return _factory;
	}

	public void setFactory(EntityFactory factory) {
		this._factory = factory;
	}
}
