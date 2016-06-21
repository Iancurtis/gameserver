package com.znl.node.battle.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.znl.define.DataDefine;
import com.znl.define.SoldierDefine;
import com.znl.proxy.ConfigDataProxy;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.znl.node.battle.consts.BattleConst.SkillType;
import com.znl.node.battle.entity.PuppetEntity;
import com.znl.node.battle.interfaces.IDepose;
import com.znl.node.battle.skill.Skill;

public class SkillController implements IDepose{

	private Logger log = LoggerFactory.getLogger(SkillController.class);

	private Map<Integer, Skill> _skillMap; //index 索引
	private List<Integer> _normalSkillIdList;
	private List<Integer> _magicSkillIdList;
	private List<Integer> _skillIdList; //初始顺序后，保持不变
	private List<Skill> _skillList; //顺序会动态排序
	
	private Skill _defaultSkill; //默认技能 普通攻击

	private PuppetEntity _owner;
	
	private Skill _curUseSkill;

	public SkillController(PuppetEntity owner) {
		_skillMap = new HashMap<Integer, Skill>();
		_normalSkillIdList = new ArrayList<Integer>();
		_magicSkillIdList = new ArrayList<Integer>();
		_skillIdList = new ArrayList<Integer>();
		_skillList = new ArrayList<Skill>();
		_owner = owner;
	}
	
	@Override
	public void depose() {
		
		for (Skill skill : _skillList) {
			skill.depose();
		}
		_skillList.clear();
		_skillMap.clear();
		_normalSkillIdList.clear();
		_magicSkillIdList.clear();
		_skillIdList.clear();
		
		_skillMap = null;
		_normalSkillIdList = null;
		_magicSkillIdList = null;
		_owner = null;
	}

	public void initSkill(List<Integer> skillIdList,int battleType) {
		for (Integer skillId : skillIdList) {

			try {
				JSONObject skillShowDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.FIGHT_SHOW,skillId);
				int realSkillId = skillShowDefine.getInt("meticID");
				JSONObject skillDefine = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.FIGHT_METIC, "type",battleType,"skillID",realSkillId);
				if(skillDefine == null){
					System.out.println("技能出现空值，id="+realSkillId);
				}
				Skill skill = createSkill(realSkillId);
				skill.skillDefine = skillDefine;
				skill.skillShowId = skillId;
				skill.setMaxCoolingRound(0);
				
				skill.init(_owner);
				
				int skillIndex = _skillIdList.size();
				skill.setIndex(skillIndex);
				_skillMap.put(skillIndex, skill);

				if (skill.getSkillType() == SkillType.Normal) {
					_normalSkillIdList.add(realSkillId);
				} else {
					_magicSkillIdList.add(realSkillId);
				}
				_skillIdList.add(realSkillId);
				_skillList.add(skill);

			} catch (InstantiationException | IllegalAccessException
					| ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
//		initDefaultSkill();
	}
	
//	private void initDefaultSkill(){
//
//		int skillId = 10000;
//		try {
//			SkillDefine define = dataCenter.getSkilDefine(skillId);
//			Skill skill = createSkill(skillId);
//			skill.skillDefine = define;
//
//			skill.init(_owner);
//
//			_defaultSkill = skill;
//
//		} catch (InstantiationException | IllegalAccessException
//				| ClassNotFoundException e) {
//			e.printStackTrace();
//		}
//	}

	public Skill getSkill(int skillIndex) {
		Skill skill = null;
		if(_skillMap.containsKey(skillIndex) == true){
			skill = _skillMap.get(skillIndex);
		}else{
			skill = _defaultSkill;
		}
		return skill;
	}
	
	public List<Integer> getSkillIdList() {
		return _skillIdList;
	}
	
	public int getSkillIdByType(SkillType skillType){
		int skillId = 0;
		if(skillType == SkillType.Magic){
			if(_magicSkillIdList.size() > 0){
				skillId = _magicSkillIdList.get(0);
			}
		}else{
			if(_normalSkillIdList.size() > 0){
				skillId = _normalSkillIdList.get(0);
			}
			
		}
		
		return skillId;
	}
	
	public int getSkillIndexByType(SkillType skillType){
		int index = -1;
		if(skillType == SkillType.Magic){
			index = getCanUseSkillIndex();
		}
		
		return index;
	}

	private void useSkill(Skill skill) {
		_curUseSkill = skill;
		
		int count = skill.getCurUseCount();
		skill.setCurUseCount(count + 1);
		
		skill.reset();
		skill.use();
	}
	
	public void useSkillByIndex(int skillIndex){
		Skill skill = this.getSkill(skillIndex);
		useSkill(skill);
	}
	
	public Skill getCurUseSkill(){
		return _curUseSkill;
	}
	
	public void endRound(){
		for (Skill skill : _skillList) {
			if(_curUseSkill == skill){
				
			}else{
				skill.endRound();
			}
		}
		
		_curUseSkill = null;
	}
	
	public void coolingAllSkill(){
		for (Skill skill : _skillList) {
			skill.coolingRound();
		}
	}
	
	/***新版获取技能逻辑***/
	public int getCanUseSkillIndex(){
		int index = -1;
		
		List<Skill> skillList = getSkillListByUseSort();
		
		for (Skill skill : skillList) {
			boolean isCooling = skill.isCooling(); //技能不冷却，直接释放
			if(isCooling == false){
				index = skill.getIndex();
				break;
			}
		}
		
		return index;
	}
	
	@SuppressWarnings("unchecked")
	private List<Skill> getSkillListByUseSort(){
		
		Collections.sort(_skillList);
		
		return _skillList;
	}
	
	
    
	/***获取可以使用的技能****/
	public int getCanUseSkillId(int curRoundCount){
		
		int skillId = 0;
		for (int id : _skillIdList) {

			Skill skill = getSkill(id);
//			boolean isCanUseSkill = isCanUseSkill(skill, define, curRoundCount);
//			if(isCanUseSkill == true){
				skillId = id;
				break;
//			}
		}
		
		return skillId;
	}
	
//	private boolean isCanUseSkill(Skill skill, SkillDefine define, int curRoundCount){
//		
//		if(define.forceCondition == 1 ){ //判断自己血量
//			int forceNum = define.forceNum;
//			int force = _owner.getAttrValue(Define.POWER_FORCE_TOTAL);
//			int maxForce = _owner.getAttrValue(Define.POWER_FORCE_MAX);
//			if(maxForce * forceNum / 100 < force){
//				return false;
//			}
//		}
//		
//		if(define.forceCondition == 2){ //判断敌对自己血量
//			int forceNum = define.forceNum;
//			EntityFactory factory = _owner.getFactory();
//			Camp camp = _owner.getAttrValue(Define.POWER_CAMP);
//			List<PuppetEntity> puppets = factory.getDiffCampLiveEntity(camp);
//			
//			if(puppets.size() > 0){
//				PuppetEntity target = puppets.get(0);
//				int force = target.getAttrValue(Define.POWER_FORCE_TOTAL);
//				int maxForce = target.getAttrValue(Define.POWER_FORCE_MAX);
//				if(maxForce * forceNum / 100 < force){
//					return false;
//				}
//			}
//		}
//		
//		if(define.roundCondition == 1){
//			int roundNum = define.roundNum;
//			if(roundNum > curRoundCount){
//				return false;
//			}
//		}
//		
//		if(define.totalNum <= skill.getCurUseCount() && define.totalNum != 0){
//			return false;
//		}
//		
//		int random = getRandom(0, 100);
//		if(define.rate < random){
//			return false;
//		}
//		
//		return true;
//	}
	
	protected int getRandom(int start, int end)
	{
		return start + (int) Math.rint( Math.random() * (end - start)) ;
	}

	/** 获取可以使用的技能类型 普通 、技能 */
	public SkillType getCanUseSkillType() {
		SkillType skillType = null;
		boolean is_slience = false;
		boolean is_disarm = false;

		int is_silence_i = _owner.getAttrValue(SoldierDefine.NOR_POWER_IS_SILENCE);
		int is_disarm_i = _owner.getAttrValue(SoldierDefine.NOR_POWER_IS_DISARM);

		if (is_silence_i == 1) {
			is_slience = true;
		}

		if (is_disarm_i == 1) {
			is_disarm = true;
		}

		if (is_slience == true && is_disarm == true) {
			log.info("=同时被沉默、缴械=没有技能可释放=");
			return skillType;
		}

		if (is_slience == true) {
			log.info("=被沉默，只能释放普通攻击=");
			skillType = SkillType.Normal;
			return skillType;
		}
		
		skillType = SkillType.Magic;
		
//		int mp = _owner.getAttrValue(Define.POWER_MP);
//		int mp_max = _owner.getAttrValue(Define.POWER_MP_MAX);
//		if(is_disarm == true){
//			if(mp >= mp_max){
//				log.info("=被缴械, 只能释放技能=");
//				skillType = SkillType.Magic;
//			}
//			return skillType;
//		}
//		
//		if(mp >= mp_max){
//			skillType = SkillType.Magic;
//		}
//		else
//		{
//			skillType = SkillType.Normal;
//		}

		return skillType;
	}

	private Skill createSkill(int skillId) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		Class<?> ownerClass = Class
				.forName("com.znl.node.battle.data.skillData.Skill" // TODO包名头配置
						+ skillId);
		Object obj = ownerClass.newInstance();

		return (Skill) obj;
	}

}
