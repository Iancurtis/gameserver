package com.znl.node.battle.skill.skillActions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.znl.define.DataDefine;
import com.znl.define.SoldierDefine;
import com.znl.proxy.ConfigDataProxy;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.znl.define.BattleDefine;
import com.znl.node.battle.consts.BattleConst.Camp;
import com.znl.node.battle.consts.BattleConst.SkillDamageRangeType;
import com.znl.node.battle.consts.BattleConst.SkillSelectCampType;
import com.znl.node.battle.consts.BattleConst.SkillSelectRangeType;
import com.znl.node.battle.entity.PuppetEntity;
import com.znl.node.battle.factory.EntityFactory;
import com.znl.node.battle.skill.Skill;

public class JudgeAction extends SkillAction {
	
	private Logger log = LoggerFactory.getLogger(JudgeAction.class);

	interface compTarget {
		boolean comp(PuppetEntity a, PuppetEntity b);
	}

	protected SkillSelectCampType skillSelectCampType;
	protected SkillSelectRangeType skillSelectRangeType;
	protected SkillDamageRangeType skillDamageRangeType;
//	private HashMap<Integer,Integer[]> targetSelectAiMap = new HashMap<>();
	public JudgeAction() {
		super.actionName = "JudgeAction";

		skillSelectCampType = SkillSelectCampType.EnemyCamp;
		skillSelectRangeType = SkillSelectRangeType.NearestHorizontally;
		skillDamageRangeType = SkillDamageRangeType.Vertical;
//		targetSelectAiMap.put(11,new Integer[]{21,24,22,25,23,26});
//		targetSelectAiMap.put(14,new Integer[]{21,24,22,25,23,26});
//
//		targetSelectAiMap.put(12,new Integer[]{22,25,23,26,21,24});
//		targetSelectAiMap.put(15,new Integer[]{22,25,23,26,21,24});
//
//		targetSelectAiMap.put(13,new Integer[]{23,26,21,24,22,25});
//		targetSelectAiMap.put(16,new Integer[]{23,26,21,24,22,25});
//
//		targetSelectAiMap.put(21,new Integer[]{11,14,12,15,13,16});
//		targetSelectAiMap.put(24,new Integer[]{11,14,12,15,13,16});
//
//		targetSelectAiMap.put(22,new Integer[]{12,15,13,16,11,14});
//		targetSelectAiMap.put(25,new Integer[]{12,15,13,16,11,14});
//
//		targetSelectAiMap.put(23,new Integer[]{13,16,11,14,12,15});
//		targetSelectAiMap.put(26,new Integer[]{13,16,11,14,12,15});
	}
	
	@Override
    public void depose() {
    	super.depose();
    	skillSelectCampType = null;
    	skillSelectRangeType = null;
    	skillDamageRangeType = null;
    }

	@Override
	public void onEnter(Skill skill) {
		super.onEnter(skill);
		
		PuppetEntity attacker = getCaster();
		int attIndex = attacker.getAttrValue(SoldierDefine.NOR_POWER_INDEX);
		log.info("========范围action===============" + attIndex);
		
		List<PuppetEntity> targets = checkRange(attacker);
		
		putCacheData(BattleDefine.ACTION_CACHE_TARGETS, targets);
		
		super.endAction();
	}

	public List<PuppetEntity> checkRange(PuppetEntity attacker) {
		
		List<PuppetEntity> targets = getSelectCampTargets(attacker,
				this.skillSelectCampType);
//		List<PuppetEntity> rangeTargets = getRangeTargets(attacker,
//				this.skillSelectRangeType, targets);

		return waitSelectRangeTarget(attacker.getAttrValue(SoldierDefine.NOR_POWER_INDEX),targets);
	}

	// 直接选出目标
	private List<PuppetEntity> waitSelectRangeTarget(int attackerIndex,List<PuppetEntity> rangeTargets) {
		if(rangeTargets.size() == 0){
			log.error("=========没有目标可以选择==");
			return new ArrayList<PuppetEntity>();
		}
		PuppetEntity target = getTarGetByAi(attackerIndex,rangeTargets);
		List<PuppetEntity> targets = confirmSkillTarget(target);
		
		return targets;
	}

	private PuppetEntity getTarGetByAi(int attackerIndex,List<PuppetEntity> targets){
		int skillType = skill.skillDefine.getInt("targettype");
		JSONObject targetDefine = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.TARGET_CHOOSE, "type", skillType, "postion", attackerIndex);
		JSONArray ais = targetDefine.getJSONArray("chooseNum");
		for (int i=0;i<ais.length();i++){
			Integer index = ais.getInt(i);
			for (PuppetEntity target : targets){
				if (target.isDead()){
					continue;
				}
				int targetIndex = target.getAttrValue(SoldierDefine.NOR_POWER_INDEX);
				if( targetIndex == index){
					return target;
				}
			}
		}
		//TODO 可以加一个容错，拿第一个目标
		return null;
	}

	// 确认范围目标， 通过该目标计算出技能攻击的所有目标
	private List<PuppetEntity> confirmSkillTarget(PuppetEntity target) {
		compTarget compTarget = null;

		if (this.skillDamageRangeType == SkillDamageRangeType.Single) {
			compTarget = (PuppetEntity ent1, PuppetEntity ent2) -> {
				boolean result = false;
				int ent1Value = ent1.getAttrValue(SoldierDefine.NOR_POWER_INDEX);
				int ent2Vaule = ent2.getAttrValue(SoldierDefine.NOR_POWER_INDEX);
				if (ent1Value == ent2Vaule) {
					result = true;
				}

				return result;
			};
		}
		else if(this.skillDamageRangeType == SkillDamageRangeType.All)
		{
			compTarget = (PuppetEntity ent1, PuppetEntity ent2) -> {
				boolean result = false;
				Camp ent1Value = ent1.getAttrValue(SoldierDefine.NOR_POWER_CAMP);
				Camp ent2Vaule = ent2.getAttrValue(SoldierDefine.NOR_POWER_CAMP);
				if (ent1Value == ent2Vaule) {
					result = true;
				}

				return result;
			};
		}
		else if(this.skillDamageRangeType == SkillDamageRangeType.Vertical)
		{
			compTarget = (PuppetEntity ent1, PuppetEntity ent2) -> {
				boolean result = false;
				int ent1Value = ent1.getAttrValue(SoldierDefine.NOR_POWER_GRIDX);
				int ent2Vaule = ent2.getAttrValue(SoldierDefine.NOR_POWER_GRIDX);
				Camp ent1Camp = ent1.getAttrValue(SoldierDefine.NOR_POWER_CAMP);
				Camp ent2Camp = ent2.getAttrValue(SoldierDefine.NOR_POWER_CAMP);
				if ((ent1Value == ent2Vaule) && (ent1Camp == ent2Camp)) {
					result = true;
				}

				return result;
			};
		}
		else if(this.skillDamageRangeType == SkillDamageRangeType.Horizontally)
		{
			compTarget = (PuppetEntity ent1, PuppetEntity ent2) -> {
				boolean result = false;
				int ent1Value = ent1.getAttrValue(SoldierDefine.NOR_POWER_GRIDY);
				int ent2Vaule = ent2.getAttrValue(SoldierDefine.NOR_POWER_GRIDY);
				Camp ent1Camp = ent1.getAttrValue(SoldierDefine.NOR_POWER_CAMP);
				Camp ent2Camp = ent2.getAttrValue(SoldierDefine.NOR_POWER_CAMP);
				if ((ent1Value == ent2Vaule) && (ent1Camp == ent2Camp)) {
					result = true;
				}

				return result;
			};
		}
		
		List<PuppetEntity> targets = new ArrayList<PuppetEntity>();
 		EntityFactory factory = target.getFactory();
		List<PuppetEntity> allTargets = factory.getAllLiveEntity();
		for (PuppetEntity puppetEntity : allTargets) {
			if(compTarget.comp(target, puppetEntity) == true)
			{
				targets.add(puppetEntity);
			}
		}
		
		return targets;
		
//		log.info(String.format("========受击 数：%d===============", super.skill.getTargets().size()));
		
		
	}
	
	//技能阵营范围选择
	public List<PuppetEntity> getSelectCampTargets(PuppetEntity attacker, SkillSelectCampType selectCampType)
	{
		List<PuppetEntity> resultTargets = new ArrayList<PuppetEntity>();
		
		
		List<PuppetEntity> allTargets = attacker.getFactory().getAllLiveEntity();
		
		Camp attCamp = attacker.getAttrValue(SoldierDefine.NOR_POWER_CAMP);
		for (PuppetEntity target : allTargets) {
			boolean isSelect = false;
			Camp tarCamp = target.getAttrValue(SoldierDefine.NOR_POWER_CAMP);
			if(selectCampType == SkillSelectCampType.EnemyCamp)
			{
				isSelect = attCamp != tarCamp;
			}
			else if(selectCampType == SkillSelectCampType.FriendCamp)
			{
				isSelect = attCamp == tarCamp;
			}
			if(isSelect == true)
			{
				resultTargets.add(target);
			}
				
		}
		
		return resultTargets;
	}
	
	//获取技能范围目标列表
	public List<PuppetEntity> getRangeTargets(PuppetEntity attacker, SkillSelectRangeType rangeType, List<PuppetEntity> targets)
	{
		if(rangeType == SkillSelectRangeType.All)
		{
			return targets;
		}
		
		List<PuppetEntity> resultTargets = new ArrayList<PuppetEntity>();
		if(rangeType == SkillSelectRangeType.Self)
		{
			resultTargets.add(attacker);
			return resultTargets;
		}
		
		calcRelativeDistance(attacker, targets);
		
		Comparator<PuppetEntity> comp = null;
		Integer campKey = null;
		if(rangeType == SkillSelectRangeType.NearestVertical)
		{
			comp = new Comparator<PuppetEntity>(){
				public int compare(PuppetEntity a, PuppetEntity b) {
					int arelativeDisGridX = a.getAttrValue(SoldierDefine.NOR_POWER_RELATIVEDISGRIDX);
					int brelativeDisGridX = b.getAttrValue(SoldierDefine.NOR_POWER_RELATIVEDISGRIDX);
					return arelativeDisGridX - brelativeDisGridX;
				}
			};
			campKey = SoldierDefine.NOR_POWER_RELATIVEDISGRIDX;
		}
		else if(rangeType == SkillSelectRangeType.NearestHorizontally)
		{
			comp = new Comparator<PuppetEntity>(){
				public int compare(PuppetEntity a, PuppetEntity b) {
					int arelativeDisGridY = a.getAttrValue(SoldierDefine.NOR_POWER_RELATIVEDISGRIDY);
					int brelativeDisGridY = b.getAttrValue(SoldierDefine.NOR_POWER_RELATIVEDISGRIDY);
					return arelativeDisGridY - brelativeDisGridY;
				}
			};
			campKey = SoldierDefine.NOR_POWER_RELATIVEDISGRIDY;
		}
		else if(rangeType == SkillSelectRangeType.FurthestVertical)
		{
			comp = new Comparator<PuppetEntity>(){
				public int compare(PuppetEntity a, PuppetEntity b) {
					int arelativeDisGridX = a.getAttrValue(SoldierDefine.NOR_POWER_RELATIVEDISGRIDX);
					int brelativeDisGridX = b.getAttrValue(SoldierDefine.NOR_POWER_RELATIVEDISGRIDX);
					return brelativeDisGridX - arelativeDisGridX;
				}
			};
			campKey = SoldierDefine.NOR_POWER_RELATIVEDISGRIDX;
		}
		else if(rangeType == SkillSelectRangeType.FurthestHorizontally)
		{
			comp = new Comparator<PuppetEntity>(){
				public int compare(PuppetEntity a, PuppetEntity b) {
					int arelativeDisGridY = a.getAttrValue(SoldierDefine.NOR_POWER_RELATIVEDISGRIDY);
					int brelativeDisGridY = b.getAttrValue(SoldierDefine.NOR_POWER_RELATIVEDISGRIDY);
					return  brelativeDisGridY - arelativeDisGridY;
				}
			};
			campKey = SoldierDefine.NOR_POWER_RELATIVEDISGRIDY;
		}
		
		targets.sort(comp);
		
		int preDir = -1;
		for (PuppetEntity target : targets) {
			int gridDir = target.getAttrValue(campKey);
			if (preDir >= 0 && preDir != gridDir)
			{
				break;
			}
			preDir = gridDir;
			resultTargets.add(target);
		}
		
		return resultTargets;
	}
	
	private void calcRelativeDistance(PuppetEntity attacker, List<PuppetEntity> targets)
	{
		int attGridX = attacker.getAttrValue(SoldierDefine.NOR_POWER_GRIDX);
		int attGridY = attacker.getAttrValue(SoldierDefine.NOR_POWER_GRIDY);
		for (PuppetEntity target : targets) {
			int tarGridX = target.getAttrValue(SoldierDefine.NOR_POWER_GRIDX);
			int tarGridY = target.getAttrValue(SoldierDefine.NOR_POWER_GRIDY);
			int relativeDisGridX = Math.abs(attGridX - tarGridX);
			int relativeDisGridY = Math.abs(attGridY - tarGridY);
			target.setAttrValue(SoldierDefine.NOR_POWER_RELATIVEDISGRIDX, relativeDisGridX);
			target.setAttrValue(SoldierDefine.NOR_POWER_RELATIVEDISGRIDY, relativeDisGridY);
		}
	}
}




