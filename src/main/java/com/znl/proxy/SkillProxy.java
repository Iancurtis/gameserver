package com.znl.proxy;

import com.znl.base.BaseDbPojo;
import com.znl.base.BaseLog;
import com.znl.base.BasicProxy;
import com.znl.define.*;
import com.znl.log.SkillLog;
import com.znl.pojo.db.Skill;
import com.znl.proto.M12;
import org.apache.mina.util.ConcurrentHashSet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by Administrator on 2015/11/26.
 */
public class SkillProxy extends BasicProxy {
    private Set<Skill> skills = new ConcurrentHashSet<>();
    @Override
    public void shutDownProxy() {
        for (Skill skill : skills) {
            skill.finalize();
        }
    }

    @Override
    protected void init() {
        super.expandPowerMap.clear();
        for (Skill skill : skills) {
            JSONObject json = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.SKILL, "ID", skill.getSkillId());
            if(json != null){
                int lv = skill.getLevel();
                JSONArray array = json.getJSONArray("property");
                if(array.length() > 0){
                    if(lv > 0){
                        addSkillPlayerPower(array.getInt(0),array.getLong(1)*lv);
                    }
                }
            }
        }
    }

    public SkillProxy(Set<Long> skillIds,String areaKey){
        this.areaKey = areaKey;
        for(Long id : skillIds) {
            Skill skill = BaseDbPojo.get(id, Skill.class,areaKey);
            skills.add(skill);
        }
        init();
    }
    /**
     *属性效果加成
     */
    private void addSkillPlayerPower(int id, long value) {
        if (super.expandPowerMap.get(id) == null) {
            super.expandPowerMap.put(id, value);
        } else {
            super.expandPowerMap.put(id, super.expandPowerMap.get(id) + value);
        }
    }
    private LinkedList<Skill> changeSkills =  new LinkedList<Skill>();

    /**
     * 保存技能信息
     */
    public void saveSkill(){
        List<Skill> skills = new ArrayList<Skill>();
        synchronized (changeSkills){
            while(true){
                Skill skill = changeSkills.poll();
                if(skill == null){
                    break;
                }
                skills.add(skill);
            }
        }
        for(Skill skill : skills){
            skill.save();
        }
    }
    private void pushSkillToChangeList(Skill skill){
        synchronized (changeSkills){
            if(!changeSkills.contains(skill)){
                changeSkills.offer(skill);
            }
        }
        init();
    }

    /**
     * 创建技能
     */
    public long createSkill(int skillId,int soldierType){
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        Skill skill =  BaseDbPojo.create(Skill.class,areaKey);
        skill.setPlayerId(playerProxy.getPlayerId());
        skill.setSkillId(skillId);
        skill.setSoldierType(soldierType);
        skill.setLevel(0);
        skills.add(skill);
        playerProxy.addSkillToPlayer(skill.getId());
        skill.save();
        return skill.getSkillId();
    }
    /**
     * 初始化技能信息
     */
    public void initSkillInfo(){
        List<JSONObject> skillList = ConfigDataProxy.getConfigAllInfo(DataDefine.SKILL);
        if(skillList.size() > 0){
            for(JSONObject json : skillList){
                if(getSkillBySkillId(json.getInt("ID")) == null){
                    createSkill(json.getInt("ID"),json.getInt("tanktype"));
                }
            }
        }
    }

    /**
     * 获取某个技能 by skillId
     */
    public Skill getSkillBySkillId(long skillId){
        for(Skill skill : skills){
            if(skill.getSkillId() == skillId){
                return skill;
            }
        }
        return null;
    }

    /**
     * 技能等级改变
     */
    public void addSkillLevel(int skillId) {
        Skill skill = getSkillBySkillId(skillId);
        if (skill != null) {
            int level = skill.getLevel();
            skill.setLevel(level + 1);
            pushSkillToChangeList(skill);
        }
    }

    /**
     * 技能升级 type:0技能书，1金币
     */
    public int skillLevelUp(int skillId, int type, List<Integer> itemlist, List<SkillLog> log){
        GameProxy gameProxy = super.getGameProxy();
        PlayerProxy playerProxy = gameProxy.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        ItemProxy itemProxy = gameProxy.getProxy(ActorDefine.ITEM_PROXY_NAME);
        int skillLv = getSkillBySkillId(skillId).getLevel();
        JSONObject skillLvInfo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.SKILLLEVEL,"skilllevel",skillLv);
        if(skillLvInfo.getInt("captainlv")==0){
            return ErrorCodeDefine.M120001_4;
        }
        if(skillLvInfo == null){
            return ErrorCodeDefine.M120001_1;
        }else{
            int captainlv = playerProxy.getLevel();
            JSONArray needArray = skillLvInfo.getJSONArray("itemneed");
            int itemId = needArray.getInt(0);
            int needItemNum = needArray.getInt(1);
            int hasItemNum = itemProxy.getItemNum(itemId);
            long gold = playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold);
            int need = needItemNum;
            if(skillLv >= captainlv){
                return ErrorCodeDefine.M120001_2;
            }else if(captainlv == 0){
                return ErrorCodeDefine.M120001_4;
            }
            //升级类型
            if(type == ActorDefine.DEFINE_UPLV_SKILLBOOK){
                if(hasItemNum < needItemNum){
                    return ErrorCodeDefine.M120001_3;
                }else{
                    itemlist.add(ItemDefine.SKILLBOOK_ID);
                    itemProxy.reduceItemNum(itemId,needItemNum,LogDefine.LOST_SKILL_LV);
                    addSkillLevel(skillId);
                }
            }else if(type == ActorDefine.DEFINE_UPLV_SKILLGOLD){
                int needGold =(needItemNum-hasItemNum)*ActorDefine.DEFINE_MONEY_SKILLBOOK;
                need = needGold;
                if(gold < needGold){
                    return ErrorCodeDefine.M120001_5;
                }else{
                    itemlist.add(ItemDefine.SKILLBOOK_ID);
                    playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold,needGold,LogDefine.LOST_SKILL_LV);
                    if(hasItemNum > 0){
                        itemProxy.reduceItemNum(itemId,hasItemNum,LogDefine.LOST_SKILL_LV);
                    }
                    addSkillLevel(skillId);
                }
            }
            //日志记录
            SkillLog lg;
            if (type == ActorDefine.DEFINE_UPLV_SKILLBOOK) {
                lg = new SkillLog(type, skillId, skillLv, needItemNum, 0);
            } else {
                lg = new SkillLog(type, skillId, skillLv, 0, (skillLv - 1) * ActorDefine.DEFINE_MONEY_SKILLBOOK);
            }
            log.add(lg);
            sendFunctionLog(FunctionIdDefine.SKILL_UPGRADE_FUNCTION_ID,skillId,type,need);
        }
        return 0;
    }


    /**
     * 重置技能
     * return allSkillBook
     */
    public int resetSkill(List<Integer> itemlist){
        GameProxy gameProxy = super.getGameProxy();
        PlayerProxy playerProxy = gameProxy.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        ItemProxy itemProxy = gameProxy.getProxy(ActorDefine.ITEM_PROXY_NAME);
        long gold = playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold);

        //判断是否可重置技能
        boolean isAllLv0 = true;
        for(Skill sk : skills){
            if(sk.getLevel()!=0){
                isAllLv0 = false;
            }
        }
        if(isAllLv0 == true){
            return ErrorCodeDefine.M120002_1;
        }
        //重置技能
        if(gold < ActorDefine.MIN_RESET_SKILL){
            return ErrorCodeDefine.M120002_2;//金币不足
        }else {
            int allSkillBook = 0;
            for (Skill skill : skills) {
                int skillBook = 0;
                int skillLv = skill.getLevel();
                for (int i = 0; i <= skillLv; i++) {
                    skillBook += i;
                }
                skill.setLevel(0);
                skill.save();
                allSkillBook += skillBook;
            }
            playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, ActorDefine.MIN_RESET_SKILL,LogDefine.LOST_SKILL_RESET);
            itemlist.add(ItemDefine.SKILLBOOK_ID);
            itemProxy.addItem(ItemDefine.SKILLBOOK_ID,allSkillBook,LogDefine.GET_RESET_SKILL);
            init();
            return 0;
        }
    }

    /**
     * 获取所有技能信息
     */
    public List<M12.SkillInfo> getAllSkillInfo(){
        List<M12.SkillInfo> skillList = new ArrayList<M12.SkillInfo>();
        for(Skill skill : skills){
            M12.SkillInfo.Builder builder = M12.SkillInfo.newBuilder();
            builder.setSkillId(skill.getSkillId());
            builder.setLevel(skill.getLevel());
            builder.setSoldierType(skill.getSoldierType());
            skillList.add(builder.build());
        }
        return skillList;
    }

    /**
     * 获取某个技能信息
     */
    public M12.SkillInfo getSkillInfo(int skillId){
        M12.SkillInfo.Builder builder = M12.SkillInfo.newBuilder();
          Skill skill = getSkillBySkillId(skillId);
        if(skill != null){
            builder.setLevel(skill.getLevel());
            builder.setSkillId(skill.getSkillId());
            builder.setSoldierType(skill.getSoldierType());
        }
        return builder.build();
    }

}
