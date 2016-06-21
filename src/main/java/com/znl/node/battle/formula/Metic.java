package com.znl.node.battle.formula;

import java.util.HashMap;
import java.util.Map;

import com.znl.define.SoldierDefine;
import com.znl.node.battle.entity.PuppetEntity;
import com.znl.utils.GameUtils;

public class Metic {

	interface Formula {
		Object calc(PuppetEntity caster, PuppetEntity target);
	}

	private Map<Integer, Formula> _mapMetic;

	public Metic() {
		_mapMetic = new HashMap<Integer, Metic.Formula>();
		initMetic();
	}

	private void initMetic() {

		Formula formula = null;

		//暴击判定
		formula = (PuppetEntity caster, PuppetEntity target) -> {
			int A = caster.getAttrValue(SoldierDefine.POWER_critRate);
			int B = target.getAttrValue(SoldierDefine.POWER_defRate);
			int C = GameUtils.getRandomValueByInterval(1, 10000);
			return C < (A - B);
		};
		_mapMetic.put(10001, formula);

		//闪避判定
		formula = (PuppetEntity caster, PuppetEntity target) -> {
			int A = caster.getAttrValue(SoldierDefine.POWER_hitRate);
			int B = target.getAttrValue(SoldierDefine.POWER_dodgeRate);
			int C = GameUtils.getRandomValueByInterval(1, 10000);
			return C < (B - A);
		};
		_mapMetic.put(10002, formula);

		//普通PVE攻击
		formula = (PuppetEntity caster, PuppetEntity target) -> {
			int A = caster.getAttrValue(SoldierDefine.POWER_atk);
			int B = caster.getAttrValue(SoldierDefine.POWER_pveDamAdd);
			int C = target.getAttrValue(SoldierDefine.POWER_pveDamDer);
			int D = caster.getAttrValue(SoldierDefine.POWER_damadd);
			int E = target.getAttrValue(SoldierDefine.POWER_damder);
			int F = caster.getAttrValue(SoldierDefine.POWER_wreck);
			int G = target.getAttrValue(SoldierDefine.POWER_defend);


			return Math.max(10,A * (1 + B/10000.0 - C/10000.0 + D/10000.0 - E/10000.0) * (1 + Math.max(-0.7,(F - G)/(F - G + 8000.0))));
		};
		_mapMetic.put(10003, formula);


		//暴击PVE伤害
		formula = (PuppetEntity caster, PuppetEntity target) -> {
			double A = (double) _mapMetic.get(10003).calc(caster, target);
			return A * 2.0;
		};
		_mapMetic.put(10004, formula);


		//普通PVP伤害
		formula = (PuppetEntity caster, PuppetEntity target) -> {
			int A = caster.getAttrValue(SoldierDefine.POWER_atk);
			int B = caster.getAttrValue(SoldierDefine.POWER_pvpDamAdd);
			int C = target.getAttrValue(SoldierDefine.POWER_pvpDamDer);
			int D = caster.getAttrValue(SoldierDefine.POWER_damadd);
			int E = target.getAttrValue(SoldierDefine.POWER_damder);
			int F = caster.getAttrValue(SoldierDefine.POWER_wreck);
			int G = target.getAttrValue(SoldierDefine.POWER_defend);


			return Math.max(10,A * (1 + B/10000.0 - C/10000.0 + D/10000.0 - E/10000.0) * (1 + Math.max(-0.7,(F - G)/(F - G + 8000))));
		};
		_mapMetic.put(10005, formula);



		//暴击PVP伤害
		formula = (PuppetEntity caster, PuppetEntity target) -> {
			double A = (double) _mapMetic.get(10005).calc(caster, target);
			return A * 2.0;
		};
		_mapMetic.put(10006, formula);


	}

	
	
	@SuppressWarnings("unchecked")
	public<T> T getMeticValue(int meticId, PuppetEntity caster, PuppetEntity target)
	{
		Object result = _mapMetic.get(meticId).calc(caster, target);
		return (T)result;
	}

	private int getRandom(int start, int end)
	{
		return start + (int) Math.rint( Math.random() * (end - start)) ;
	}
}
