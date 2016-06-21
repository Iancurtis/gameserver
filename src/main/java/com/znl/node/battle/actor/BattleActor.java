package com.znl.node.battle.actor;

import static akka.actor.SupervisorStrategy.restart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.znl.define.SoldierDefine;
import com.znl.proto.M5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Option;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.actor.SupervisorStrategy.Directive;
import akka.actor.UntypedActor;
import akka.japi.Function;

import com.znl.node.battle.consts.BattleConst.Camp;
import com.znl.node.battle.entity.PuppetEntity;
import com.znl.node.battle.factory.EntityFactory;
import com.znl.node.battle.message.BattleMessage;
import com.znl.node.battle.message.BattleMessage.BattleMessageType;

public class BattleActor extends UntypedActor {

	private Logger log = LoggerFactory.getLogger(BattleActor.class);

	private EntityFactory _factory;

	private Queue<PuppetEntity> _leftQueue;
	private Queue<PuppetEntity> _rightQueue;

	private Queue<PuppetEntity> leftQueue;
	private Queue<PuppetEntity> rightQueue;
	private List<M5.Round> _roundDataList;
	private int _curRound = 0;
	private boolean _is_victory = false;
	private final int MAX_ROUND = 72;
	
	final private ActorRef roundActor = context().actorOf(Props.create(RoundActor.class),
			"roundActor");
	{
		this.getContext().watch(roundActor);
	}
	
	private SupervisorStrategy strategy = new OneForOneStrategy(10,
			Duration.create("30 seconds"), new Function<Throwable, Directive>() {
				@Override
				public Directive apply(Throwable t) {
					log.warn("！！！警告警告！！！！roundActor服务出现异常！！！");
					StackTraceElement[] traceElement = t.getStackTrace();
					for (StackTraceElement stackTraceElement : traceElement) {
						log.warn(stackTraceElement.toString());
					}
					
					getContext().parent().tell(
							BattleMessage.valueOf(BattleMessageType.BATTLE_ERROR,
									"BattleActor"), getSelf());
					
					return restart(); 
				}
			});
	
	public SupervisorStrategy supervisorStrategy() {
		return strategy;
	};

	@Override
	public void preStart() throws Exception {
		super.preStart();
		
		_roundDataList = new ArrayList<M5.Round>();

		_leftQueue = new LinkedList<PuppetEntity>();
		_rightQueue = new LinkedList<PuppetEntity>();
		leftQueue = new LinkedList<PuppetEntity>();
		rightQueue = new LinkedList<PuppetEntity>();
	}

	// Round出错异常
	@Override
	public void preRestart(Throwable reason, Option<Object> message)
			throws Exception {
		super.preRestart(reason, message);
		
		log.info("parent preRestart :" + getSelf().path() + "reason:" + reason + " message:" + message);

		getContext().parent().tell(
				BattleMessage.valueOf(BattleMessageType.BATTLE_ERROR,
						"BattleActor"), getSelf());
	}

	@Override
	public void postStop() throws Exception {
		super.postStop();
		
		log.info("parent postStop :" + getSelf().path() + "postStop");
	}

	@Override
	public void postRestart(Throwable reason) throws Exception {
		super.postRestart(reason);
		
		log.info("parent postRestart :" + getSelf().path() + "postRestart reason:" + reason);		
		log.error("parent postRestart Error ",reason);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof BattleMessage) {
			BattleMessage bmsg = (BattleMessage) message;
			switch (bmsg.getType()) {
			case START_BATTLE:
				startBattle((EntityFactory) bmsg.getData());
				break;
			case NEXT_ROUND:
				nextRound((M5.Round) bmsg.getData());
				break;
			case BATTLE_ERROR:  //战斗出错啦
				getContext().parent().tell(
						BattleMessage.valueOf(BattleMessageType.BATTLE_ERROR,
								"BattleActor"), getSelf());
				break;
			default:
				break;

			}
		}
	}

	private void startBattle(EntityFactory factory) {
		_factory = factory;
		_curRound = 0;
		_roundDataList.clear();
		_leftQueue.clear();
		_rightQueue.clear();
		leftQueue.clear();
		rightQueue.clear();
		_is_victory = false;

		isLeft = true; // test

		putQuppetQueue(factory);

		log.info("=====战斗开始========");
		checkFirstAttack(factory);
		checkStartRound(factory);

	}

	private void putQuppetQueue(EntityFactory factory) {
		List<Integer> leftIndexs = Arrays.asList(11, 12, 13, 14, 15, 16);
		List<Integer> rightIndexs = Arrays.asList(21, 22, 23, 24, 25, 26);

		for (Integer index : leftIndexs) {
			PuppetEntity puppet = factory.getEntity(index);
			if (puppet != null) {
				_leftQueue.offer(puppet);
			}
		}

		if(_leftQueue.size() < 6){
			for (int i=0;i<6-_leftQueue.size();i++){
				_leftQueue.offer(null);
			}
		}

		for (Integer index : rightIndexs) {
			PuppetEntity puppet = factory.getEntity(index);
			if (puppet != null) {
				_rightQueue.offer(puppet);
			}
		}
		if(_rightQueue.size() < 6){
			for (int i=0;i<6-_rightQueue.size();i++){
				_rightQueue.offer(null);
			}
		}
	}

	private void checkStartRound(EntityFactory factory) {
		_curRound++;
		checkRoundAttack(factory);
	}

	private boolean isLeft = true;
	private boolean _isLeft = true;
	private PuppetEntity _attack = null;
	
	private void checkFirstAttack(EntityFactory factory){
		int leftInitiative = 0;
		List<PuppetEntity> leftPuppetList = factory.getSameCampLiveEntity(Camp.Left);
		for (PuppetEntity puppetEntity : leftPuppetList) {
			int initiative = puppetEntity.getAttrValue(SoldierDefine.POWER_initiative);
			leftInitiative += initiative;
		}
		
		int rightInitiative = 0;
		List<PuppetEntity> rightPuppetList = factory.getSameCampLiveEntity(Camp.Right);
		for (PuppetEntity puppetEntity : rightPuppetList) {
			int initiative = puppetEntity.getAttrValue(SoldierDefine.POWER_initiative);
			rightInitiative += initiative;
		}
		
		if(leftInitiative < rightInitiative){
			isLeft = false;
			_isLeft = false;
		}
	}


	/***新需求，将死亡单位后移***/
	private void checkRountAttack(){
		if (_leftQueue.size() == 0 && _rightQueue.size() == 0){
			_leftQueue = new LinkedList<>(leftQueue);
			_rightQueue = new LinkedList<>(rightQueue);
			leftQueue.clear();
			rightQueue.clear();
			isLeft = _isLeft;
		}
	}

	//TODO 修改出手规则
	private void checkRoundAttack(EntityFactory factory) {
		_attack = null;
//		checkRountAttack(_rightQueue);
		while (_attack == null) {
			checkRountAttack();
			if (isLeft == true) {
				isLeft = false;
				_attack = _leftQueue.poll();
				if (_attack == null) {
//					_leftQueue.offer(null);
				} else {
					if (_attack.isDead() != true) {
						break;
					} else {
//						_leftQueue.offer(null);
						_attack = null;
//						//拿到死亡单位就直接拿下一个
						isLeft = true;
					}
				}
			}
			if (isLeft == false) {
				isLeft = true;
				_attack = _rightQueue.poll();
				if (_attack == null) {
//					_rightQueue.offer(null);
				} else {
					if (_attack.isDead() != true) {
						break;
					} else {
//						_rightQueue.offer(null);
						_attack = null;
//						//拿到死亡单位就直接拿下一个
						isLeft = false;
					}
				}
			}

		}

		factory.setRoundAttacker(_attack);
		List<Object> data = new ArrayList<Object>();
		data.add(factory);
		data.add(_curRound);
		// int index = _attack.getAttrValue(Define.POWER_INDEX);
		roundActor.tell(
				BattleMessage.valueOf(BattleMessageType.START_ROUND, data),
				self());
	}

	public void nextRound(M5.Round roundData) {
		_roundDataList.add(roundData);
		boolean isEnd = checkIsEndBattle(_factory);

		if (isEnd == true || _curRound >= MAX_ROUND) {
			log.info("=====战斗结束====总共战斗回合====" + _curRound);

			BattleMessageType type = null;
			if (_is_victory == true) {
				type = BattleMessageType.END_BATTLE_VICTORY;
			} else {
				type = BattleMessageType.END_BATTLE_FAILURE;
			}
			context().parent().tell(
					BattleMessage.valueOf(type, _roundDataList), getSelf());
		} else {
			// if(_attack.isDead() == false)
			// {
			Camp camp = _attack.getAttrValue(SoldierDefine.NOR_POWER_CAMP);
			if (camp == Camp.Left) {
				// log.error(String.format("NO=ERROR=左边=攻击结束归队==index:%d==",
				// _attack.getAttrValue("index")));
				leftQueue.offer(_attack);
			} else {
				// log.error(String.format("NO=ERROR=右边=攻击结束归队==index:%d==",
				// _attack.getAttrValue("index")));
				rightQueue.offer(_attack);
			}
			// }
			checkStartRound(_factory);
		}

	}

	private boolean checkIsEndBattle(EntityFactory factory) {
		List<PuppetEntity> entitys = factory.getAllEntity();

		List<PuppetEntity> leftCampList = new ArrayList<PuppetEntity>();
		List<PuppetEntity> rightCampList = new ArrayList<PuppetEntity>();

		for (PuppetEntity ent : entitys) {
			Camp camp = ent.getAttrValue(SoldierDefine.NOR_POWER_CAMP);
			if (camp == Camp.Left) {
				leftCampList.add(ent);
			} else if (camp == Camp.Right) {
				rightCampList.add(ent);
			}
		}

		boolean isLeftEnd = true;
		for (PuppetEntity ent : leftCampList) {
			if (ent.isDead() == false) {
				isLeftEnd = false;
				break;
			}
		}

		boolean isRightEnd = true;
		for (PuppetEntity ent : rightCampList) {
			if (ent.isDead() == false) {
				isRightEnd = false;
				break;
			}
		}

		boolean isEnd = isLeftEnd || isRightEnd;

		if (isLeftEnd == true) {
			log.info("====左边失败=======");
			_is_victory = false;
		}

		if (isRightEnd == true) {
			log.info("====右边失败=======");
			_is_victory = true;
		}

		return isEnd;
	}

}
