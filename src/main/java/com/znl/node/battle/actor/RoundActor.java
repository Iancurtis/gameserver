package com.znl.node.battle.actor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.znl.define.SoldierDefine;
import com.znl.node.battle.buff.Buff;
import com.znl.proto.M5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Option;
import akka.actor.UntypedActor;

import com.znl.node.battle.consts.BattleConst.BuffTickType;
import com.znl.node.battle.consts.BattleConst.SkillType;
import com.znl.node.battle.controller.SkillController;
import com.znl.node.battle.entity.PuppetEntity;
import com.znl.node.battle.factory.EntityFactory;
import com.znl.node.battle.message.BattleMessage;
import com.znl.node.battle.message.BattleMessage.BattleMessageType;
import com.znl.node.battle.skill.Skill;

public class RoundActor extends UntypedActor {

	private Logger log = LoggerFactory.getLogger(RoundActor.class);
	private EntityFactory _factory;
	private PuppetEntity _attacker;
	private Skill _curSkill;
	private List<M5.RoleBuff> _startRoleBuffList;
	private List<M5.RoleBuff> _hitRoleBuffList;
	private List<M5.RoleBuff> _endRoleBuffList;

	private int roundCount = 0; //回合数
	@Override
	public void preStart() throws Exception {
		super.preStart();
		_startRoleBuffList = new ArrayList<>();
		_hitRoleBuffList = new ArrayList<>();
		_endRoleBuffList = new ArrayList<>();
		
		log.info("========RoundActor启动===========");
	}
	
	//Round出错异常
	@Override
	public void preRestart(Throwable reason, Option<Object> message)
			throws Exception {
		super.preRestart(reason, message);
		
		getContext().parent().tell(BattleMessage.valueOf(BattleMessageType.BATTLE_ERROR, "RoundActor"), getSelf());
	}
	
	@Override
	public void postStop() throws Exception {
		super.postStop();
	}
	
	@Override
	public void postRestart(Throwable reason) throws Exception {
		super.postRestart(reason);
	}

	@Override
	public void onReceive(Object message) throws Exception {
		// TODO Auto-generated method stub
		if (message instanceof BattleMessage) {
			BattleMessage bmsg = (BattleMessage) message;
			switch (bmsg.getType()) {
			case START_ROUND:
				@SuppressWarnings("unchecked")
				List<Object> data = (List<Object>) bmsg.getData();
				roundCount = (int)data.get(1);
				startRound((EntityFactory) data.get(0));
				break;
			default:
				break;

			}
		}
	}

	private void startRound(EntityFactory factory) {
		this._factory = factory;
		clearRoundData(factory);
		PuppetEntity attacker = factory.getRoundAttacker();
		_attacker = attacker;
		log.info("===========开始回合=================" + _attacker.getAttrValue(SoldierDefine.NOR_POWER_INDEX));
		checkBuffRoundStart(attacker);
	}

	// 将之前的回合缓存数据清除。。TODO， 可能会出现问题
	private void clearRoundData(EntityFactory factory) {
		_startRoleBuffList.clear();
		_hitRoleBuffList.clear();
		_endRoleBuffList.clear();
		_curSkill = null;

		for (PuppetEntity ent : factory.getAllEntity()) {
			ent.clearBloosList();
			ent.clearRemoveBuffList();
		}
	}

	private void checkBuffRoundStart(PuppetEntity attacker) {
//		log.info("===回合者index:"+(int)attacker.getAttrValue(SoldierDefine.NOR_POWER_INDEX));
		clearAllPuppetRemoveBuffList();
		List<Buff> buffList = attacker.getBuffList();
		for (Buff buff : buffList) {
			if (buff.getTickType() == BuffTickType.RoundStart) {
				buff.onRoundTick();
			}
		}

//		packRoleBuffs(_startRoleBuffList);

		boolean isDead = attacker.isDead(); // 是否死亡
		if (isDead == true) {
			endRound();
		} else {
			checkAttack(attacker);
		}

	}

	private void checkAttack(PuppetEntity attacker) {
		int is_vertigo = attacker.getAttrValue(SoldierDefine.NOR_POWER_IS_VERTIGO);
		if(is_vertigo == 1){
			log.info("======被眩晕，无法出手，进入下一回合===========");
			endRound();
		}else{
			startAttack(attacker);
		}
		
	}

	private void startAttack(PuppetEntity attacker) {
		waitUseSkill(attacker);
	}

	//TODO 修改需要释放的技能
	private void waitUseSkill(PuppetEntity attacker) {
		SkillController skillController = attacker.getSkillController();
		SkillType skillType = skillController.getCanUseSkillType();
		if(skillType == null){
			log.info("====没有技能可释放，直接下一回合===");
			endRound();
			return;
		}
		
		int skillIndex = skillController.getSkillIndexByType(skillType);
//		if(skillId == 0){
////			skillId = 101;
//		}
		
//		int skillIndex = skillController.getCanUseSkillIndex(); //roundCount
		
		useSkill(attacker, skillIndex);
	}

	private void useSkill(PuppetEntity attacker, int skillIndex) {
		log.info("=========使用技能====skillIndex==========" + skillIndex);
		// 技能运算 单线程

		SkillController skillController = attacker.getSkillController();

		Skill skill = skillController.getSkill(skillIndex);
		log.info("=========使用技能id为"+skill.toString());
		_curSkill = skill;
		skill.setFactory(_factory);

		skillController.useSkillByIndex(skillIndex);
		checkBuffRoundHit(attacker);

		endRound();
	}

	// 使用技能后的打击buff效果处理
	private void checkBuffRoundHit(PuppetEntity attacker) {
		clearAllPuppetRemoveBuffList();
//		packRoleBuffs(_hitRoleBuffList);
	}

	// 回合结束
	private void endRound() {
		checkBuffRoundEnd(_attacker);
		
		SkillController skillController = _attacker.getSkillController();
		skillController.endRound();

		M5.Round roundData = null;
		if (_curSkill != null) {
			roundData = packRoundData(_attacker, _curSkill);
		} else {
			roundData = packRoundData(_attacker, 0,
					new ArrayList<PuppetEntity>());
		}
		
//		System.err.println("回合数据："  + roundData);
		
		
//		int num = 0;
//		for (int i = 0; i < 10000; i++) {
//			num += 1;
//		}
//		log.error("===RoundActor=====战斗回合结束==下一回合================");
		context().parent().tell(
				BattleMessage.valueOf(BattleMessageType.NEXT_ROUND, roundData),
				self());
	}

	private void checkBuffRoundEnd(PuppetEntity attacker) {
		clearAllPuppetRemoveBuffList();
		List<Buff> buffList = attacker.getBuffList();
		for (Buff buff : buffList) {
			if (buff.getTickType() == BuffTickType.RoundEnd) {
				buff.onRoundTick();
			}
		}

//		packRoleBuffs(_endRoleBuffList);
	}

	private M5.Round packRoundData(PuppetEntity attacker, int skillId,
			List<PuppetEntity> targets) {
		M5.Round.Builder roundBuilder = M5.Round.newBuilder();
		roundBuilder.setIndex(attacker.getAttrValue(SoldierDefine.NOR_POWER_INDEX));
//		roundBuilder.addAllBloods(attacker.getRoundBloosList());
//		roundBuilder.setCurHp(attacker.getAttrValue(SoldierDefine.POWER_hp));
		roundBuilder.setSkillId(skillId);
		boolean isDead = attacker.isDead();
		int status = 1;
		if (isDead == true) {
			status = 2;
		}
//		roundBuilder.setStatus(status);
//		roundBuilder.setAttackType(1);
//		roundBuilder.setCurMp(attacker.getAttrValue(Define.POWER_MP));
//		roundBuilder.setName(attacker.name);
		roundBuilder.setNum(attacker.getAttrValue(SoldierDefine.NOR_POWER_NUM));
		M5.Target.Builder targetBuilder = null;
		for (PuppetEntity target : targets) {
			targetBuilder = M5.Target.newBuilder();

			targetBuilder.setIndex(target.getAttrValue(SoldierDefine.NOR_POWER_INDEX));
//			targetBuilder.setCurMp(target.getAttrValue(Define.POWER_MP));
//			targetBuilder.setEffect(1);

			isDead = target.isDead();
			status = 1;
			if (isDead == true) {
				status = 2;
			}
			targetBuilder.setNum(target.getAttrValue(SoldierDefine.NOR_POWER_NUM));
//			targetBuilder.setStatus(status);
			targetBuilder.addAllBloods(target.getRoundBloosList());
//			targetBuilder.setName(target.name);

			roundBuilder.addTargets(targetBuilder.build());
		}

//		roundBuilder.addAllStartRoleBuffs(_startRoleBuffList);
//		roundBuilder.addAllHitRoleBuffs(_hitRoleBuffList);
//		roundBuilder.addAllEndRoleBuffs(_endRoleBuffList);
		
//		packRoleSkillInfos(roundBuilder);

		return roundBuilder.build();
	}
	
	// 打包回合数据
	private M5.Round packRoundData(PuppetEntity attacker, Skill skill) {
		return packRoundData(attacker, skill.skillShowId, skill.getTargets());
	}
	
	private void packRoleSkillInfos(M5.Round.Builder roundBuilder){
		for (PuppetEntity puppet : _factory.getAllEntity()) {
			
			M5.RoleSkillInfo.Builder roleSkillInfoBuilder = M5.RoleSkillInfo.newBuilder();
			roleSkillInfoBuilder.setIndex(puppet.getAttrValue(SoldierDefine.NOR_POWER_INDEX));
			
			SkillController skillController = puppet.getSkillController();
			List<Integer> skillIdList = skillController.getSkillIdList();
			int index = 0;
			for (Integer skillId : skillIdList) {
				Skill skill = skillController.getSkill(index);
				
				M5.SkillInfo.Builder builder = M5.SkillInfo.newBuilder();
				builder.setSkillId(skill.getSkillId());
				builder.setCurCoolingRound(skill.getCurCoolingRound());
				
				roleSkillInfoBuilder.addSkillInfos(builder.build());
				
				index++;
			}
			
			roundBuilder.addRoleSkillInfos(roleSkillInfoBuilder.build());
		}
	}

	private void packRoleBuffs(List<M5.RoleBuff> roleBuffList) {
		for (PuppetEntity puppet : _factory.getAllEntity()) { //死了 也飘 BUFF血
			boolean hasBuff = puppet.hasBuff();
			List<Integer> list = puppet.getRemoveBuffList();
			int buffAttrMapSize = puppet.getBuffAttrMapSize();
			if (hasBuff == true || list.size() > 0 || buffAttrMapSize > 0) {
				M5.RoleBuff.Builder roleBuffBuilder = M5.RoleBuff.newBuilder();
				roleBuffBuilder.setIndex(puppet.getAttrValue(SoldierDefine.NOR_POWER_INDEX));
				M5.Buff.Builder buffBuilder;
				List<Buff> buffList = puppet.getBuffList();
				for (Buff buff : buffList) {
					buffBuilder = M5.Buff.newBuilder();
					buffBuilder.setId(buff.getId());
					buffBuilder.setLastRound(buff.getLastRound());
					buffBuilder.setIconId(buff.getIconId());
					roleBuffBuilder.addBuffs(buffBuilder.build());

					// log.info(String.format("打包buff数据, id:%d,lastRound:%d,index:%d",
					// buff.getId(), buff.getLastRound(),
					// puppet.getAttrValue("index")));
				}

				for (Integer buffId : list) {
					buffBuilder = M5.Buff.newBuilder();
					buffBuilder.setId(buffId);
					buffBuilder.setLastRound(0); // 0表示已被删除
					buffBuilder.setIconId(0);
					roleBuffBuilder.addBuffs(buffBuilder.build());
				}
				
				
				Map<Integer, List<Integer>> buffAttrMap = puppet.getBuffAttrMap();
				for (Entry<Integer, List<Integer>> entry : buffAttrMap.entrySet()) {
					for (Integer value : entry.getValue()) {
						M5.AttrMap.Builder attrMapBuilder = M5.AttrMap.newBuilder();
						attrMapBuilder.setKey(entry.getKey());
						attrMapBuilder.setDelta(value);
						attrMapBuilder.setValue(puppet.getAttrValue(entry.getKey()));
						
						roleBuffBuilder.addAttrMaps(attrMapBuilder.build());
					}
				}
				
//				System.err.println("打包buff数据：buffAttrMap size: " + buffAttrMap.size());
				
				puppet.clearBuffAttrMap();

				roleBuffList.add(roleBuffBuilder.build());
			}

		}
	}

	private void clearAllPuppetRemoveBuffList() {
		for (PuppetEntity puppet : _factory.getAllLiveEntity()) {
			puppet.clearRemoveBuffList();
		}
	}

}
