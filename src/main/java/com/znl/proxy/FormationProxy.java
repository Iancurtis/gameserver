package com.znl.proxy;

import com.znl.base.BaseDbPojo;
import com.znl.base.BasicProxy;
import com.znl.core.PlayerTeam;
import com.znl.core.PlayerTroop;
import com.znl.define.*;
import com.znl.pojo.db.FormationMember;
import com.znl.pojo.db.Soldier;
import com.znl.proto.Common;
import com.znl.proto.M7;
import org.apache.mina.util.ConcurrentHashSet;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by Administrator on 2015/11/24.
 */
public class FormationProxy  extends BasicProxy {
    private Set<FormationMember> dungeoFormation = new ConcurrentHashSet<>();
    private Set<FormationMember> defendFormation = new ConcurrentHashSet<>();
    private Set<FormationMember> areanFormation = new ConcurrentHashSet<>();
    @Override
    public void shutDownProxy() {
        dungeoFormation.forEach(com.znl.pojo.db.FormationMember::finalize);
        defendFormation.forEach(com.znl.pojo.db.FormationMember::finalize);
        areanFormation.forEach(com.znl.pojo.db.FormationMember::finalize);
    }

    @Override
    protected void init() {

    }

    private FormationMember createFormationMember(int post){
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        FormationMember member = BaseDbPojo.create(FormationMember.class,areaKey);
        member.setNum(0);
        member.setPlayerId(playerProxy.getPlayerId());
        member.setPost(post);
        member.setTypeId(0);
        member.save();
        return member;
    }

    public void initFormation(){
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if(dungeoFormation.size() == 0){//如果未初始化过就开始初始化
            Set<Long> formationDungeon = new HashSet<>();
            for(int i=1;i<=6;i++){
                FormationMember member = createFormationMember(i);
                dungeoFormation.add(member);
                formationDungeon.add(member.getId());
            }
            playerProxy.addFormationToPlayer(formationDungeon,SoldierDefine.FORMATION_DUNGEO);
        }

        if(defendFormation.size() == 0){
            Set<Long> formationsDefend = new HashSet<>();
            for(int i=1;i<=6;i++){
                FormationMember member = createFormationMember(i);
                defendFormation.add(member);
                formationsDefend.add(member.getId());
            }
            playerProxy.addFormationToPlayer(formationsDefend,SoldierDefine.FORMATION_DEFEND);
        }

        if(areanFormation.size() == 0){
            Set<Long> formationsArena = new HashSet<>();
            for(int i=1;i<=6;i++){
                FormationMember member = createFormationMember(i);
                areanFormation.add(member);
                formationsArena.add(member.getId());
            }
            playerProxy.addFormationToPlayer(formationsArena,SoldierDefine.FORMATION_ARENA);
        }

    }

    public FormationProxy(Set<Long> formationDungeon,Set<Long> formationsDefend,Set<Long> formationsArena,String areaKey){
        this.areaKey = areaKey;
        for(Long id : formationDungeon){
            FormationMember formationMember = BaseDbPojo.get(id, FormationMember.class,areaKey);
            dungeoFormation.add(formationMember);
        }
        for(Long id : formationsDefend){
            FormationMember formationMember = BaseDbPojo.get(id, FormationMember.class,areaKey);
            defendFormation.add(formationMember);
        }
        for(Long id : formationsArena){
            FormationMember formationMember = BaseDbPojo.get(id, FormationMember.class,areaKey);
            areanFormation.add(formationMember);
        }
    }

    public List<M7.FormationInfo> getFormationInfos(){
        List<M7.FormationInfo> res = new ArrayList<>();
        if(dungeoFormation.size() > 0){
            M7.FormationInfo.Builder formation = M7.FormationInfo.newBuilder();
            for(FormationMember member : dungeoFormation){
                M7.FormationMember.Builder builder = M7.FormationMember.newBuilder();
                builder.setNum(member.getNum());
                builder.setPost(member.getPost());
                builder.setTypeid(member.getTypeId());
                formation.addMembers(builder.build());
            }
            formation.setType(SoldierDefine.FORMATION_DUNGEO);
            res.add(formation.build());
        }
        if(defendFormation.size() > 0){
            M7.FormationInfo.Builder formation = M7.FormationInfo.newBuilder();
            for(FormationMember member : defendFormation){
                M7.FormationMember.Builder builder = M7.FormationMember.newBuilder();
                builder.setNum(member.getNum());
                builder.setPost(member.getPost());
                builder.setTypeid(member.getTypeId());
                formation.addMembers(builder.build());
            }
            formation.setType(SoldierDefine.FORMATION_DEFEND);
            res.add(formation.build());
        }
        if(areanFormation.size() > 0){
            M7.FormationInfo.Builder formation = M7.FormationInfo.newBuilder();
            for(FormationMember member : areanFormation){
                M7.FormationMember.Builder builder = M7.FormationMember.newBuilder();
                builder.setNum(member.getNum());
                builder.setPost(member.getPost());
                builder.setTypeid(member.getTypeId());
                formation.addMembers(builder.build());
            }
            formation.setType(SoldierDefine.FORMATION_ARENA);
            res.add(formation.build());
        }
        return res;
    }

    private Set<FormationMember> getFormationByType(int type){
        switch (type){
            case SoldierDefine.FORMATION_DUNGEO:
                return dungeoFormation;
            case SoldierDefine.FORMATION_DEFEND:
                return defendFormation;
            case SoldierDefine.FORMATION_ARENA:
                return areanFormation;
            default:
                return null;
        }
    }

    private void saveFormation(Set<FormationMember> formation){
        for(FormationMember member : formation){
            member.save();
        }
    }

    public int setFormation(M7.FormationInfo formationInfo){
        int type = formationInfo.getType();
        List<M7.FormationMember> members = formationInfo.getMembersList();
        HashMap<Integer,Integer> memMap = new HashMap<>();
        for (M7.FormationMember memberInfo : members){
            if(memberInfo.getTypeid() == 0 || memberInfo.getNum() == 0){
                continue;
            }
            if(memberInfo.getPost()<1||memberInfo.getPost()>6){
                return ErrorCodeDefine.M70001_5;
            }
            if(memMap.containsKey(memberInfo.getTypeid())){
                int num = memMap.get(memberInfo.getTypeid()) + memberInfo.getNum();
                memMap.put(memberInfo.getTypeid(),num);
            }else {
                memMap.put(memberInfo.getTypeid(),memberInfo.getNum());
            }
        }
        SoldierProxy soldierProxy = getGameProxy().getProxy(ActorDefine.SOLDIER_PROXY_NAME);
        PlayerProxy playerProxy=getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        int all=0;
        for(Integer id : memMap.keySet()){
            int num = memMap.get(id);
            if(num<0){
                return ErrorCodeDefine.M70001_2;
            }
            if(num > soldierProxy.getSoldierNum(id)){
                return ErrorCodeDefine.M70001_1;
            }
            all+=num;
        }
        if(type == SoldierDefine.FORMATION_ARENA){
          if(all==0){
              return ErrorCodeDefine.M70001_4;
          }
        }
        Set<FormationMember> formation = getFormationByType(type);
        for(FormationMember member : formation){
            member.setNum(0);
            member.setTypeId(0);
        }
        for (M7.FormationMember memberInfo : members){
            int post = memberInfo.getPost();
            if (memberInfo.getNum() > 0){
                for(FormationMember member : formation){
                    if(post == member.getPost()){
                        member.setNum(memberInfo.getNum());
                        member.setTypeId(memberInfo.getTypeid());
                        member.setBaseNum(memberInfo.getNum());
                        if(playerProxy.getPowerValue(PlayerPowerDefine.POWER_command)<memberInfo.getNum()){
                            return ErrorCodeDefine.M70001_3;
                        }
                    }
                }
            }
        }
        saveFormation(formation);
        return 0;
    }

    /****创建竞技场出战队列****/
    public List<PlayerTeam> createFormationTeam(int type){
        Set<FormationMember> formation = getFormationByType(type);
        List<Common.FightElementInfo> fightElementInfos = new ArrayList<>();
        BattleProxy battleProxy = getGameProxy().getProxy(ActorDefine.BATTLE_PROXY_NAME);
        for(FormationMember member : formation){
            Common.FightElementInfo.Builder builder = Common.FightElementInfo.newBuilder();
            builder.setNum(member.getNum());
            builder.setPost(member.getPost());
            builder.setTypeid(member.getTypeId());
            fightElementInfos.add(builder.build());
        }
        List<PlayerTeam> res = battleProxy.createFightTeamList(fightElementInfos);
        if (type == SoldierDefine.FORMATION_DEFEND){
            for(PlayerTeam team : res){
                team.basePowerMap.put(SoldierDefine.POWER_initiative,(int)team.basePowerMap.get(SoldierDefine.POWER_initiative) +1);
                team.powerMap.put(SoldierDefine.POWER_initiative,(int)team.powerMap.get(SoldierDefine.POWER_initiative) +1);
            }
        }
        return res;
    }

    /****检查是否需要还原防守阵型（加佣兵的时候调用）****/
    public boolean checkBaseDefendTroop(SoldierProxy soldierProxy){
        boolean needRef = false;
        Set<FormationMember> formation = getFormationByType(SoldierDefine.FORMATION_DEFEND);
        HashSet<Integer> typeIdSet = new HashSet<>();
        for(FormationMember member : formation){
            if (member.getTypeId() != 0 && member.getBaseNum() != 0){
                typeIdSet.add(member.getTypeId());
            }
        }
        HashMap<Integer,Integer> haveNumMap = new HashMap<>();
        for(Integer typeId : typeIdSet){
            int num = soldierProxy.getSoldierNum(typeId);
            haveNumMap.put(typeId,num);
        }
        for (int post = 1;post <=6;post++){
            for(FormationMember member : formation){
                if (post == member.getPost()){
                    if (member.getTypeId() != 0 && member.getBaseNum() != 0){
                        int haveNum = haveNumMap.get(member.getTypeId());
                        if (haveNum > member.getBaseNum()){
                            if (member.getBaseNum() != member.getNum()){
                                needRef = true;
                                member.setNum(member.getBaseNum());
                            }
                            haveNumMap.put(member.getTypeId(),haveNum-member.getBaseNum());
                        }else if(haveNum > 0){
                            if (member.getNum() != haveNum){
                                needRef = true;
                                member.setNum(haveNum);
                            }
                            haveNumMap.put(member.getTypeId(),0);
                        }else {
                            if (member.getNum() > 0){
                                member.setNum(0);
                                needRef = true;
                            }
                        }
                    }
                }
            }
        }
        if (needRef){
            saveFormation(formation);
        }
        return needRef;
    }

    public boolean haveDefendTroop(){
        Set<FormationMember> formation = getFormationByType(SoldierDefine.FORMATION_DEFEND);
        boolean have = false;
        for(FormationMember member : formation){
            if (member.getNum() > 0){
                have = true;
                break;
            }
        }
        return have;
    }

    /****检查是否需要扣除防守阵型（减佣兵的时候调用）****/
    public boolean checkDefendTroop(SoldierProxy soldierProxy, int settingAutoAddDefendList, List<PlayerTeam> teams, HashMap<Integer, Integer> deathMap) {
        if(haveDefendTroop() == false){
            return false;
        }
        boolean refurceDefendTeam = false;
        Set<FormationMember> formation = getFormationByType(SoldierDefine.FORMATION_DEFEND);
        if (settingAutoAddDefendList == ActorDefine.SETTING_AUTO_ADD_DEFEND_TEAM_OFF){
            for (PlayerTeam team : teams){
                //没有开启自动补充的话就直接扣除就好啦
                int nowNum = (int) team.getValue(SoldierDefine.NOR_POWER_NUM);
                int index = (int) team.getValue(SoldierDefine.NOR_POWER_INDEX);
                for(FormationMember member : formation){
                    if(index-20 == member.getPost()){
                        if (nowNum != member.getNum()){
                            refurceDefendTeam = true;
                        }
                        member.setNum(nowNum);
                        member.setBaseNum(nowNum);
                        if (nowNum <=0){
                            member.setNum(0);
                            member.setBaseNum(0);
                            member.setTypeId(0);
                        }
                    }
                }
            }
        }else {
            //开启自动补充的话就查看玩家身上的佣兵到底够不够，不够的就缩减（在日常行为扣除佣兵之类的都需要调用）
            HashMap<Integer,Integer> soldierNum = new HashMap<>();
            for(FormationMember member : formation){
                int soldierId = member.getTypeId();
                int num = soldierProxy.getSoldierNum(soldierId);
                if (soldierNum.containsKey(soldierId) == false){
                    soldierNum.put(soldierId,num);
                }
//                else {
//                    soldierNum.put(soldierId,soldierNum.get(soldierId) + num);
//                }
            }
            for (int post = 1;post <=6;post++){
                for(FormationMember member : formation){
                    if (post == member.getPost()){
                        int num = soldierNum.get(member.getTypeId());
                        if (num == 0){
                            member.setNum(0);
//                            member.setTypeId(0);
                            refurceDefendTeam = true;
                        }else if (num >= member.getNum()){
                            soldierNum.put(member.getTypeId(),num - member.getNum());
                        }else {
                            member.setNum(num);
                            soldierNum.put(member.getTypeId(),0);
                            refurceDefendTeam = true;
                        }
                    }
                }
            }
        }

        saveFormation(formation);
        return refurceDefendTeam;
    }

    public PlayerTroop refurceDefendTeam(List<PlayerTeam> teams,long playerId){
        Set<FormationMember> formation = getFormationByType(SoldierDefine.FORMATION_DEFEND);
        for (int i=0;i<teams.size();i++){
            PlayerTeam team = teams.get(i);
            int index = (int) team.getValue(SoldierDefine.NOR_POWER_INDEX);
            int num = (int) team.getValue(SoldierDefine.NOR_POWER_NUM);
            for(FormationMember member : formation){
                if (member.getPost() == index-20){
                    if (member.getNum() <= 0){
                        teams.remove(i);
                        i--;
                        continue;
                    }else {
                        if (num != member.getNum()){
                            team.basePowerMap.put(SoldierDefine.NOR_POWER_NUM,member.getNum());
                            int attackPercent = (int) team.basePowerMap.get(SoldierDefine.POWER_atkRate);
                            int hpPercent = (int) team.basePowerMap.get(SoldierDefine.POWER_atkRate);
                            JSONObject soldierDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.ARM_KINDS, member.getTypeId());
                            int attack = (int) (soldierDefine.getInt("atk") * ( 1 + attackPercent/10000.0) * member.getNum());
                            int hp = (int) (soldierDefine.getInt("hpmax") * ( 1 + hpPercent/10000.0) * member.getNum());
                            team.basePowerMap.put(SoldierDefine.POWER_atk,attack);
                            team.basePowerMap.put(SoldierDefine.POWER_hp,hp);
                            team.basePowerMap.put(SoldierDefine.POWER_hpMax,hp);
                            team.init();
                        }
                    }
                }
            }
        }
        for (int i=0;i<teams.size();i++){
            PlayerTeam team = teams.get(i);
            int num = (int) team.getValue(SoldierDefine.NOR_POWER_NUM);
            if (num <= 0){
                teams.remove(i);
                i --;
            }
        }
        saveFormation(formation);
        return createPlayerTroop(teams,playerId);
    }

    public PlayerTroop createPlayerTroop(List<PlayerTeam> teams,long playerId){
        PlayerTroop troop = new PlayerTroop();
        troop.setPlayerTeams(teams);
        troop.setPlayerId(playerId);
        List<M7.FormationMember> fightElementInfos = new ArrayList<>();
        for (PlayerTeam team : teams){
            M7.FormationMember.Builder builder = M7.FormationMember.newBuilder();
            builder.setNum((Integer) team.getValue(SoldierDefine.NOR_POWER_NUM));
            builder.setPost((Integer) team.getValue(SoldierDefine.NOR_POWER_INDEX)-20);
            builder.setTypeid((Integer) team.getValue(SoldierDefine.NOR_POWER_TYPE_ID));
            fightElementInfos.add(builder.build());
        }
        troop.setFightElementInfos(fightElementInfos);
        return troop;
    }
}
