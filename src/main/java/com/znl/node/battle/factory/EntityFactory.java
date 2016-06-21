package com.znl.node.battle.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.znl.base.BasicBattleAttr;
import com.znl.define.SoldierDefine;
import com.znl.node.battle.consts.BattleConst.Camp;
import com.znl.node.battle.entity.PuppetEntity;
import com.znl.node.battle.interfaces.IDepose;

public class EntityFactory implements IDepose{

	private Map<Integer, PuppetEntity> _entMap = new HashMap<Integer, PuppetEntity>();
	private PuppetEntity _roundAttacker; // 当前回合攻击者
	
	@Override
	public void depose() {
		
		for (Entry<Integer, PuppetEntity> entry : _entMap.entrySet()) {
			PuppetEntity puppet = entry.getValue();
			puppet.depose();
		}
		_entMap.clear();
		_entMap = null;
		_roundAttacker = null;
	}
	
	public PuppetEntity create(BasicBattleAttr attr){
		PuppetEntity ent = new PuppetEntity(attr);
		ent.setFactory(this);
		
		int index = ent.getAttrValue(SoldierDefine.NOR_POWER_INDEX);
		_entMap.put(index, ent);
		return ent;
	}

	public PuppetEntity getEntity(int index) {
		if(_entMap.containsKey(index) == false)
			return null;
		
		return _entMap.get(index);
	}

	public List<PuppetEntity> getAllEntity() {
		List<PuppetEntity> list = new ArrayList<PuppetEntity>();

		for (Entry<Integer, PuppetEntity> entry : _entMap.entrySet()) {
			list.add(entry.getValue());
		}

		return list;
	}

	public List<PuppetEntity> getAllLiveEntity() {
		List<PuppetEntity> list = new ArrayList<PuppetEntity>();

		for (Entry<Integer, PuppetEntity> entry : _entMap.entrySet()) {
			PuppetEntity ent = entry.getValue();
			if (ent.isDead() != true) {
				list.add(ent);
			}

		}

		return list;
	}
	
	public List<PuppetEntity> getSameCampLiveEntity(Camp camp){
		List<PuppetEntity> list = new ArrayList<PuppetEntity>();
		List<PuppetEntity> allLiveEntity = getAllLiveEntity();
		for (PuppetEntity puppetEntity : allLiveEntity) {
			Camp tCamp = puppetEntity.getAttrValue(SoldierDefine.NOR_POWER_CAMP);
			if(tCamp == camp){
				list.add(puppetEntity);
			}
		}
		
		return list;
	}
	
	public List<PuppetEntity> getDiffCampLiveEntity(Camp camp){
		List<PuppetEntity> list = new ArrayList<PuppetEntity>();
		List<PuppetEntity> allLiveEntity = getAllLiveEntity();
		for (PuppetEntity puppetEntity : allLiveEntity) {
			Camp tCamp = puppetEntity.getAttrValue(SoldierDefine.NOR_POWER_CAMP);
			if(tCamp != camp){
				list.add(puppetEntity);
			}
		}
		
		return list;
	}
	

	public PuppetEntity getRoundAttacker() {
		return _roundAttacker;
	}

	public void setRoundAttacker(PuppetEntity _roundAttacker) {
		this._roundAttacker = _roundAttacker;
	}
	
	public void resetAllPuppetAttr(){
		for (Entry<Integer, PuppetEntity> entry : _entMap.entrySet()) {
			entry.getValue().resetAttr();
		}
	}
}
