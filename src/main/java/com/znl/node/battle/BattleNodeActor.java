package com.znl.node.battle;

import akka.actor.*;
import akka.japi.Function;
import com.znl.base.BasicBattleAttr;
import com.znl.core.PlayerBattle;
import com.znl.define.PlayerPowerDefine;
import com.znl.define.ProtocolModuleDefine;
import com.znl.define.SoldierDefine;
import com.znl.framework.socket.Request;
import com.znl.framework.socket.Response;
import com.znl.log.CustomerLogger;
import com.znl.modules.battle.BattleModule;
import com.znl.msg.GameMsg;
import com.znl.node.battle.actor.BattleActor;
import com.znl.node.battle.buff.Buff;
import com.znl.node.battle.consts.BattleConst;
import com.znl.node.battle.controller.SkillController;
import com.znl.node.battle.entity.PuppetEntity;
import com.znl.node.battle.factory.EntityFactory;
import com.znl.node.battle.message.BattleMessage;
import com.znl.node.battle.skill.Skill;
import com.znl.proto.M5;
import scala.Option;
import scala.concurrent.duration.Duration;

import java.util.*;

import static akka.actor.SupervisorStrategy.restart;

/**
 * Created by Administrator on 2015/11/13.
 */
public class BattleNodeActor extends UntypedActor{

    private boolean isStartBattle = false;
    private Map<Integer, Map<String, Integer>> _gridInfoList;

    private com.znl.node.battle.factory.EntityFactory _factory;
//    private M5.Battle.Builder battleBuilder;
    private int rc = 0;
    private PlayerBattle _playerBattle;
    private List<M5.Round> _roundDataList;

    private final int BATTLE_CODE_VICTORY = 0;
    private final int BATTLE_CODE_FAILURE = 1;
    private final int BATTLE_CODE_ERROR = -1;

    final private ActorRef battleActor = context().actorOf(
            Props.create(BattleActor.class), "battleActor");
    {
        this.getContext().watch(battleActor);
        initGridInfo();
    }


    private static SupervisorStrategy strategy = new OneForOneStrategy(10,
            Duration.create("30 seconds"),
            new Function<Throwable, SupervisorStrategy.Directive>() {
                @Override
                public SupervisorStrategy.Directive apply(Throwable t) {
                    CustomerLogger.error("！！！警告警告！！！！battleActor服务出现异常！！！");
                    StackTraceElement[] traceElement = t.getStackTrace();
                    for (StackTraceElement stackTraceElement : traceElement) {
                        CustomerLogger.error(stackTraceElement.toString());
                    }

                    return restart();
                }});

    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    };



    @Override
    public void postStop() throws Exception {
        super.postStop();
        CustomerLogger.info("parent postStop :" + getSelf().path());
    }

    @Override
    public void preRestart(Throwable reason, Option<Object> message)
            throws Exception {
        super.preRestart(reason, message);

        CustomerLogger.info("parent preRestart :" + getSelf().path() + "reason:" + reason
                + " message:" + message);
        reset();
    }


    private void reset() {
        isStartBattle = false;

        try {
            if (_factory != null) {
                _factory.resetAllPuppetAttr();
                _factory.depose();
                _factory = null;
            }
        } catch (Exception e) {
            CustomerLogger.error("factory释放失败:" + e.getMessage());
        }

    }

    private void onErrorHandler() {
        reset();
        rc = BATTLE_CODE_ERROR;
        _playerBattle.rs = rc;
        GameMsg.ErrorBattle message = new GameMsg.ErrorBattle(rc);
        sendMessageToParent(message);
//        onBattleEndHandler(null);
    }

    public void onModuleErrorHandler(String moduleName) {
        onErrorHandler();
    }



    @Override
    public void onReceive(Object message) throws Exception {
        if(message instanceof GameMsg.ReqPuppetList){
            PlayerBattle battle = (PlayerBattle) ((GameMsg.ReqPuppetList) message).message();
            onPuppetListResp(battle);
        }else if(message instanceof BattleMessage){
            onReceiveBattleMessage((BattleMessage) message);
        }
    }

    //发消息给上层
    private void sendMessageToParent(Object object){
        context().parent().tell(object, self());
    }


    /**
     * 接受到战斗消息
     *
     * @param
     */
    @SuppressWarnings("unchecked")
    protected void onReceiveBattleMessage(BattleMessage battleMessage) {
        switch (battleMessage.getType()) {
            case END_BATTLE_VICTORY:
                rc = BATTLE_CODE_VICTORY;
                endBattle((List<M5.Round>) battleMessage.getData());
                break;
            case END_BATTLE_FAILURE:
                rc = BATTLE_CODE_FAILURE;
                endBattle((List<M5.Round>) battleMessage.getData());
                break;
            case BATTLE_ERROR: // 战斗出错，客户端重新请求
                onErrorHandler();
                break;
            default:
                break;
        }
    }


    private void onPuppetListResp(PlayerBattle playerBattle) {
        _playerBattle = playerBattle;
        createPuppetEntityList(playerBattle);
        startBattleRound();
    }

    private void createPuppetEntityList(PlayerBattle playerBattle) {
        _factory = new EntityFactory();
//        playerBattle.soldierList.forEach(this::createPuppetEntity);
        for(BasicBattleAttr attr : playerBattle.soldierList){
            createPuppetEntity(attr,playerBattle.type);
        }
        for(BasicBattleAttr attr : playerBattle.monsterList){
            createPuppetEntity(attr,playerBattle.type);
        }
//        playerBattle.monsterList.forEach(this::createPuppetEntity);

    }

    private void createPuppetEntity(BasicBattleAttr attr,int battleType) {

        PuppetEntity ent = _factory.create(attr);

        int index = ent.getAttrValue(SoldierDefine.NOR_POWER_INDEX);
        if (index < 20) {
            ent.setAttrValue(SoldierDefine.NOR_POWER_CAMP, BattleConst.Camp.Left);
        } else {
            ent.setAttrValue(SoldierDefine.NOR_POWER_CAMP, BattleConst.Camp.Right);
        }

        ent.name = ent.getAttrValue(SoldierDefine.NOR_POWER_NAME);

        Map<String, Integer> gridInfo = _gridInfoList.get(index);
        ent.setAttrValue(SoldierDefine.NOR_POWER_GRIDX, gridInfo.get("x"));
        ent.setAttrValue(SoldierDefine.NOR_POWER_GRIDY, gridInfo.get("y"));
        ent.setAttrValue(SoldierDefine.POWER_hpMax, ent.getAttrValue(SoldierDefine.POWER_hpMax));
        ent.setAttrValue(SoldierDefine.POWER_hp, ent.getAttrValue(SoldierDefine.POWER_hp));
//        ent.setAttrValue(Define.POWER_HP_MAX, ent.getAttrValue(Define.POWER_FORCE_TOTAL));

        Integer modelIdList = ent.getAttrValue(SoldierDefine.NOR_POWER_ICON);
        ent.modelIdList = modelIdList;

        List<Integer> skillIdList = ent.getAttrValue(SoldierDefine.NOR_POWER_SKILL);
        SkillController skillController = ent.getSkillController();
        skillController.initSkill(skillIdList,battleType);
//
        List<Integer> birthBuffList = ent.getAttrValue(SoldierDefine.NOR_POWER_BUFF);
        for (Integer birthBuff : birthBuffList) {
            if (birthBuff <= 0) {
                continue;
            }
            ent.addBuff(birthBuff, index, 1);
        }
//		ent.addBuff(1000, index, 1); //test
//
        M5.PuppetAttr puppetAttr = packPuppet(ent);
        GameMsg.PackPuppet message = new GameMsg.PackPuppet(puppetAttr);
        sendMessageToParent(message);
    }

    private void startBattleRound() {
        battleActor
                .tell(BattleMessage.valueOf(BattleMessage.BattleMessageType.START_BATTLE,
                        _factory), self());
    }

    // 结束战斗，这里需要将数据包发送给客户端
    private void endBattle(List<M5.Round> roundDataList) {

        isStartBattle = false;
        _roundDataList = roundDataList;
        reset();

        sendBattleResult(roundDataList.size());
        CustomerLogger.info("=======战斗结束=============");
    }

    // 发送战斗结果给上层，进行结果统计
    private void sendBattleResult(int roundCount) {
        boolean battleResult = false;
        if (rc == 0) {
            battleResult = true;
        }
        _playerBattle.roundCount = roundCount;
        _playerBattle.battleResult = battleResult;
        int battleType = _playerBattle.type;
        _playerBattle._roundDataList = _roundDataList;
        GameMsg.ServerBattleEndHandle message = new GameMsg.ServerBattleEndHandle(_playerBattle);

        sendMessageToParent(message);

//        sendModuleMessage(DungeonModule.NAME, ModuleMessage.valueOf(ModuleMessageType.SERVER_BATTLE_END_HANDLE, _playerBattle));

    }



    private M5.PuppetAttr packPuppet(PuppetEntity ent) {
        M5.PuppetAttr.Builder attr = M5.PuppetAttr.newBuilder();
        attr.setIndex(ent.getAttrValue(SoldierDefine.NOR_POWER_INDEX));
        attr.setHp(ent.getAttrValue(SoldierDefine.POWER_hp));
        attr.setModelList(ent.modelIdList);
        attr.setName(ent.name);
        attr.setNum(ent.getAttrValue(SoldierDefine.NOR_POWER_NUM));
        M5.Buff.Builder buffBuilder;
        List<Buff> buffList = ent.getBuffList();
        for (Buff buff : buffList) {
            buffBuilder = M5.Buff.newBuilder();
            buffBuilder.setId(buff.getId());
//            buffBuilder.setLastRound(buff.getLastRound());
//            buffBuilder.setIconId(buff.getIconId());

            attr.addBuffs(buffBuilder.build());
        }

        SkillController skillController = ent.getSkillController();
        List<Integer> skillIdList = skillController.getSkillIdList();
//        int index = 0;
//        for (Integer skillId : skillIdList) {
//            Skill skill = skillController.getSkill(index);
//
//            M5.SkillInfo.Builder builder = M5.SkillInfo.newBuilder();
//            builder.setSkillId(skill.getSkillId());
//            builder.setCurCoolingRound(skill.getCurCoolingRound());
//
//            attr.addSkillInfos(builder.build());
//
//            index++;
//        }


//        M5.Puppet.Builder builder = M5.Puppet.newBuilder();
//        builder.setAttr(attr);
//
//        battleBuilder.addPuppets(builder.build());
        return attr.build();
    }

    private void initGridInfo() {
        _gridInfoList = new HashMap<Integer, Map<String, Integer>>();

        Map<String, Integer> gridInfo = new HashMap<String, Integer>();
        gridInfo.put("x", 1);
        gridInfo.put("y", 2);
        _gridInfoList.put(11, gridInfo);

        gridInfo = new HashMap<String, Integer>();
        gridInfo.put("x", 2);
        gridInfo.put("y", 2);
        _gridInfoList.put(12, gridInfo);

        gridInfo = new HashMap<String, Integer>();
        gridInfo.put("x", 3);
        gridInfo.put("y", 2);
        _gridInfoList.put(13, gridInfo);

        gridInfo = new HashMap<String, Integer>();
        gridInfo.put("x", 1);
        gridInfo.put("y", 1);
        _gridInfoList.put(14, gridInfo);

        gridInfo = new HashMap<String, Integer>();
        gridInfo.put("x", 2);
        gridInfo.put("y", 1);
        _gridInfoList.put(15, gridInfo);

        gridInfo = new HashMap<String, Integer>();
        gridInfo.put("x", 3);
        gridInfo.put("y", 1);
        _gridInfoList.put(16, gridInfo);

        //
        gridInfo = new HashMap<String, Integer>();
        gridInfo.put("x", 1);
        gridInfo.put("y", 3);
        _gridInfoList.put(21, gridInfo);

        gridInfo = new HashMap<String, Integer>();
        gridInfo.put("x", 2);
        gridInfo.put("y", 3);
        _gridInfoList.put(22, gridInfo);

        gridInfo = new HashMap<String, Integer>();
        gridInfo.put("x", 3);
        gridInfo.put("y", 3);
        _gridInfoList.put(23, gridInfo);

        gridInfo = new HashMap<String, Integer>();
        gridInfo.put("x", 1);
        gridInfo.put("y", 4);
        _gridInfoList.put(24, gridInfo);

        gridInfo = new HashMap<String, Integer>();
        gridInfo.put("x", 2);
        gridInfo.put("y", 4);
        _gridInfoList.put(25, gridInfo);

        gridInfo = new HashMap<String, Integer>();
        gridInfo.put("x", 3);
        gridInfo.put("y", 4);
        _gridInfoList.put(26, gridInfo);
    }


    @Override
    public String toString() {
        return "";
    }

}
