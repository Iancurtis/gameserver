package com.znl.proxy;

import com.znl.base.BasicProxy;
import com.znl.core.PlayerReward;
import com.znl.define.ActorDefine;
import com.znl.define.DataDefine;
import com.znl.define.PlayerPowerDefine;
import com.znl.proto.Common;
import com.znl.proto.M2;
import com.znl.utils.GameUtils;
import com.znl.utils.RandomUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/11/17.
 */
public class RewardProxy extends BasicProxy {

    public RewardProxy(String areaKey){
        this.areaKey = areaKey;
    }

    @Override
    public void shutDownProxy() {

    }

    @Override
    protected void init() {

    }

    public void getPlayerReward(int rewardId, PlayerReward reward) {
        JSONObject rewardDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.REWARD, rewardId);
        if (rewardDefine == null) {
            return;
        }
        JSONArray itemArray = rewardDefine.getJSONArray("RewardItemId");
        if (itemArray.length() > 0) {
            for (int i = 0; i < itemArray.length(); i++) {
                int itemRewardId = itemArray.getInt(i);
                JSONObject itemReward = ConfigDataProxy.getConfigInfoFindById(DataDefine.REWARD_ITEM, itemRewardId);
                int itemId = itemReward.getInt("itemID");
                int num = itemReward.getInt("num");
                addItemToReward(reward, itemId, num);
            }
        }
        JSONArray resoourceArry = rewardDefine.getJSONArray("RewardResourceId");
        if (resoourceArry.length() > 0) {
            for (int i = 0; i < resoourceArry.length(); i++) {
                int resRewardId = resoourceArry.getInt(i);
                JSONObject resReward = ConfigDataProxy.getConfigInfoFindById(DataDefine.REWARD_RESOURCE, resRewardId);
                int resId = resReward.getInt("resourceID");
                int num = resReward.getInt("num");
                addPowerToReward(reward, resId, num);
            }
        }
        JSONArray sodierArry = rewardDefine.getJSONArray("RewardArmyId");
        if (sodierArry.length() > 0) {
            for (int i = 0; i < sodierArry.length(); i++) {
                int sodierRewardId = sodierArry.getInt(i);
                JSONObject sodierReward = ConfigDataProxy.getConfigInfoFindById(DataDefine.REWARD_ARMY, sodierRewardId);
                int sodierId = sodierReward.getInt("armyID");
                int num = sodierReward.getInt("num");
                addSoldierToReward(reward, sodierId, num);
            }
        }

        JSONArray warrioArry = rewardDefine.getJSONArray("RewardWarriorsId");
        if (warrioArry.length() > 0) {
            for (int i = 0; i < warrioArry.length(); i++) {
                int warrioRewardId = warrioArry.getInt(i);
                JSONObject warrioReward = ConfigDataProxy.getConfigInfoFindById(DataDefine.REWARD_WARRIORS, warrioRewardId);
                int warrioId = warrioReward.getInt("warriorsID");
                int num = warrioReward.getInt("num");
                addGeneralToReward(reward, warrioId, num);
            }
        }

        JSONArray ordancepiecArry = rewardDefine.getJSONArray("RewardOrdnancePieceId");
        if (ordancepiecArry.length() > 0) {
            for (int i = 0; i < ordancepiecArry.length(); i++) {
                int ordancepiecRewardId = ordancepiecArry.getInt(i);
                JSONObject ordancepieReward = ConfigDataProxy.getConfigInfoFindById(DataDefine.REWARD_ORDNANCE_PIECE, ordancepiecRewardId);
                int ordancepieId = ordancepieReward.getInt("ordnancePieceID");
                int num = ordancepieReward.getInt("num");
                addOrdanceFragmentToReward(reward, ordancepieId, num);
            }
        }

        JSONArray counsellorArry = rewardDefine.getJSONArray("soilderID");
        if (counsellorArry.length() > 0) {
            for (int i = 0; i < counsellorArry.length(); i++) {
                int counsellorRewardId = counsellorArry.getInt(i);
                JSONObject counsellorReward = ConfigDataProxy.getConfigInfoFindById(DataDefine.REWARD_COUNSELLOR, counsellorRewardId);
                int counsellorId = counsellorReward.getInt("CounsellorID");
                int num = counsellorReward.getInt("num");
                addCounsellorToReward(reward, counsellorId, num);
            }
        }

        JSONArray ordanceArry = rewardDefine.getJSONArray("RewardOrdnanceId");
        if (ordanceArry.length() > 0) {
            for (int i = 0; i < ordanceArry.length(); i++) {
                int ordanceRewardId = ordanceArry.getInt(i);
                JSONObject ordanceReward = ConfigDataProxy.getConfigInfoFindById(DataDefine.REWARD_ORDNANCE, ordanceRewardId);
                int ordanceId = ordanceReward.getInt("ordnanceID");
                int num = ordanceReward.getInt("num");
                addOrdanceToReward(reward, ordanceId, num);
            }
        }


    }

    public void getPlayerRewardByFixReward(int id, PlayerReward reward) {
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.FIX_REWARD, id);
        if (jsonObject == null) {
            return;
        }
        int rewardType = jsonObject.getInt("type");
        int rewardInfo = jsonObject.getInt("contentID");
        int num = jsonObject.getInt("num");
        getRewardContent(reward, rewardType, rewardInfo, num);
    }

    //无满值
    public Integer getPlayerRewardByRandContent(int groupId, PlayerReward reward,List<Integer> rewardList) {
        List<JSONObject> randList = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.RANK_CONTENT, "groupId", groupId);
        if (randList == null || randList.size() == 0) {
            return null;
        }
        int max = 0;
        for (JSONObject randDefine : randList) {
            max += randDefine.getInt("percent");
        }
        int ran = GameUtils.getRandomValueByRange(max);
        JSONObject jsonObject = null;
        int add = 0;
        for (JSONObject randDefine : randList) {
            int persent = randDefine.getInt("percent");
            add += persent;
            if (ran <= add) {
                jsonObject = randDefine;
                break;
            }
        }
        int rs = 0;
        if (jsonObject != null) {
            int rewardType = jsonObject.getInt("rewardType");
            int rewardInfo = jsonObject.getInt("rewardInfo");
            rs = jsonObject.getInt("ID");
            int num = jsonObject.getInt("num");
            rewardList.add(rewardType);
            rewardList.add(rewardInfo);
            rewardList.add(num);
            getRewardContent(reward, rewardType, rewardInfo, num);
        }
        return rs;
    }

    //有满值
    public void getPlayerRewardByRandFullContent(int groupId, PlayerReward reward) {
        List<JSONObject> randList = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.RANK_FULL_CONTENT, "groupId", groupId);
        if (randList == null || randList.size() == 0) {
            return;
        }
        int maxPercent = randList.get(0).getInt("fullpercent");
        int ran = GameUtils.getRandomValueByRange(maxPercent);
        int temp = 0;
        JSONObject getDefine = null;
        for (JSONObject randDefine : randList) {
            temp += randDefine.getInt("percent");
            if (ran < temp) {
                getDefine = randDefine;
                break;
            }
        }
        if (getDefine == null) {
            return;
        }
        int rewardType = getDefine.getInt("rewardType");
        int rewardInfo = getDefine.getInt("rewardInfo");
        int num = getDefine.getInt("num");
        getRewardContent(reward, rewardType, rewardInfo, num);
    }

    /**
     * 将奖励内容合成到reward里面
     **/
    public void getRewardContent(PlayerReward reward, int rewardType, int rewardInfo, int num) {
        switch (rewardType) {
            case PlayerPowerDefine.BIG_POWER_ITEM: {
                addItemToReward(reward, rewardInfo, num);
                break;
            }
            case PlayerPowerDefine.BIG_POWER_GENERAL: {
                addGeneralToReward(reward, rewardInfo, num);
                break;
            }
            case PlayerPowerDefine.BIG_POWER_ORDNANCE: {
                addOrdanceToReward(reward, rewardInfo, num);
                break;
            }
            case PlayerPowerDefine.BIG_POWER_ORDNANCE_FRAGMENT: {
                addOrdanceFragmentToReward(reward, rewardInfo, num);
                break;
            }
            case PlayerPowerDefine.BIG_POWER_COUNSELLOR: {
                addCounsellorToReward(reward, rewardInfo, num);
                break;
            }
            case PlayerPowerDefine.BIG_POWER_SOLDIER: {
                addSoldierToReward(reward, rewardInfo, num);
                break;
            }
            case PlayerPowerDefine.BIG_POWER_RESOURCE: {
                addPowerToReward(reward, rewardInfo, num);
                break;
            }
        }
    }

    private void addPowerToReward(PlayerReward reward, int power, int num) {
        if (reward.addPowerMap.containsKey(power) == true) {
            int _num = reward.addPowerMap.get(power);
            reward.addPowerMap.put(power, num + _num);
        } else {
            reward.addPowerMap.put(power, num);
        }
    }


    public void addSoldierToReward(PlayerReward reward, int rewardInfo, int num) {
        if (reward.soldierMap.containsKey(rewardInfo) == true) {
            int _num = reward.soldierMap.get(rewardInfo);
            reward.soldierMap.put(rewardInfo, num + _num);
        } else {
            reward.soldierMap.put(rewardInfo, num);
        }
    }

    public void addCounsellorToReward(PlayerReward reward, int rewardInfo, int num) {
        if (reward.counsellorMap.containsKey(rewardInfo) == true) {
            int _num = reward.counsellorMap.get(rewardInfo);
            reward.counsellorMap.put(rewardInfo, num + _num);
        } else {
            reward.counsellorMap.put(rewardInfo, num);
        }
    }

    public void addOrdanceFragmentToReward(PlayerReward reward, int rewardInfo, int num) {
        if (reward.ordanceFragmentMap.containsKey(rewardInfo) == true) {
            int _num = reward.ordanceFragmentMap.get(rewardInfo);
            reward.ordanceFragmentMap.put(rewardInfo, num + _num);
        } else {
            reward.ordanceFragmentMap.put(rewardInfo, num);
        }
    }

    public void addOrdanceToReward(PlayerReward reward, int rewardInfo, int num) {
        if (reward.ordanceMap.containsKey(rewardInfo) == true) {
            int _num = reward.ordanceMap.get(rewardInfo);
            reward.ordanceMap.put(rewardInfo, num + _num);
        } else {
            reward.ordanceMap.put(rewardInfo, num);
        }
    }

    public void addGeneralToReward(PlayerReward reward, int rewardInfo, int num) {
        if (reward.generalMap.containsKey(rewardInfo) == true) {
            int _num = reward.generalMap.get(rewardInfo);
            reward.generalMap.put(rewardInfo, num + _num);
        } else {
            reward.generalMap.put(rewardInfo, num);
        }
    }

    public void addItemToReward(PlayerReward reward, int rewardInfo, int num) {
        if (reward.addItemMap.containsKey(rewardInfo) == true) {
            int _num = reward.addItemMap.get(rewardInfo);
            reward.addItemMap.put(rewardInfo, num + _num);
        } else {
            reward.addItemMap.put(rewardInfo, num);
        }
    }


    public void addEquipIdtoReward(PlayerReward reward, long equpId) {
        reward.generalList.add(equpId);
    }

    public void addOrdnanceIdtoReward(PlayerReward reward, long ordId) {
        reward.ordanceList.add(ordId);
    }

    /**
     * 将rewawrd发送到玩家背包
     **/
    public void getRewardToPlayer(PlayerReward reward, int getType) {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (reward.addPowerMap.size() > 0) {
            for (Integer power : reward.addPowerMap.keySet()) {
                playerProxy.addPowerValue(power, reward.addPowerMap.get(power), getType);
            }
        }
        if (reward.addItemMap.size() > 0) {
            ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
            for (Integer id : reward.addItemMap.keySet()) {
                int num = reward.addItemMap.get(id);
                itemProxy.addItem(id, num, getType);
            }
        }
        if (reward.soldierMap.size() > 0) {
            SoldierProxy soldierProxy = getGameProxy().getProxy(ActorDefine.SOLDIER_PROXY_NAME);
            for (Integer id : reward.soldierMap.keySet()) {
                int num = reward.soldierMap.get(id);
                soldierProxy.addSoldierNum(id, num, getType);
            }
        }

        if (reward.generalMap.size() > 0) {
            EquipProxy equipProxy = getGameProxy().getProxy(ActorDefine.EQUIP_PROXY_NAME);
            for (Integer id : reward.generalMap.keySet()) {
                int num = reward.generalMap.get(id);
                for (int i = 0; i < num; i++) {
                    long equId = equipProxy.addEquip(id, getType);
                    if (!reward.generalList.contains(equId)) {
                        reward.generalList.add(equId);
                    }
                }
            }
        }

        if (reward.ordanceMap.size() > 0) {
            OrdnanceProxy ordnanceProxy = getGameProxy().getProxy(ActorDefine.ORDANCE_PROXY_NAME);
            for (Integer id : reward.ordanceMap.keySet()) {
                int num = reward.ordanceMap.get(id);
                for (int i = 0; i < num; i++) {
                    long ordId = ordnanceProxy.creatOrdnance(id, 0, 0, getType);
                    if (!reward.ordanceList.contains(ordId)) {
                        reward.ordanceList.add(ordId);
                    }
                }
            }
        }
        if (reward.ordanceFragmentMap.size() > 0) {
            OrdnancePieceProxy ordnancePieceProxy = getGameProxy().getProxy(ActorDefine.ORDANCEPIECE_PROXY_NAME);
            for (Integer id : reward.ordanceFragmentMap.keySet()) {
                int num = reward.ordanceFragmentMap.get(id);
                ordnancePieceProxy.addOrdnancePiece(id, num, getType);
            }
        }


    }

   /* */

    /**********
     * 推送给客户端
     ******************//*
    public void sendRewardToClient(PlayerReward reward) {
        if (reward.addPowerMap.size() > 0) {
            for (Integer power : reward.addPowerMap.keySet()) {

            }
        }
        if (reward.addItemMap.size() > 0) {
            for (Integer id : reward.addItemMap.keySet()) {

            }
        }
        if (reward.soldierMap.size() > 0) {
            for (Integer id : reward.soldierMap.keySet()) {

            }
        }
        if (reward.counsellorMap.size() > 0) {
            for (Integer id : reward.counsellorMap.keySet()) {

            }
        }
        if (reward.generalMap.size() > 0) {
            for (Integer id : reward.generalMap.keySet()) {

            }
        }
        if (reward.ordanceMap.size() > 0) {
            for (Integer id : reward.ordanceMap.keySet()) {

            }
        }
        if (reward.ordanceFragmentMap.size() > 0) {
            for (Integer id : reward.ordanceFragmentMap.keySet()) {

            }
        }

    }
*/
    public M2.M20009.S2C getRewardInfoClientMsg(PlayerReward reward) {
        List<Common.RewardInfo> rewardInfoList = new ArrayList<>();
        getRewardInfoByReward(reward, rewardInfoList);
        M2.M20009.S2C.Builder builder = M2.M20009.S2C.newBuilder();
        builder.addAllRewards(rewardInfoList);
        return builder.build();
    }

    public void getRewardInfoByReward(PlayerReward reward, List<Common.RewardInfo> rewardInfoList) {
        if (reward.addPowerMap.size() > 0) {
            for (Integer power : reward.addPowerMap.keySet()) {
                Common.RewardInfo.Builder rewardInfo = Common.RewardInfo.newBuilder();
                rewardInfo.setPower(PlayerPowerDefine.BIG_POWER_RESOURCE);
                rewardInfo.setNum(reward.addPowerMap.get(power));
                rewardInfo.setTypeid(power);
                rewardInfoList.add(rewardInfo.build());
            }
        }
        if (reward.addItemMap.size() > 0) {
            for (Integer id : reward.addItemMap.keySet()) {
                Common.RewardInfo.Builder rewardInfo = Common.RewardInfo.newBuilder();
                rewardInfo.setPower(PlayerPowerDefine.BIG_POWER_ITEM);
                rewardInfo.setNum(reward.addItemMap.get(id));
                rewardInfo.setTypeid(id);
                rewardInfoList.add(rewardInfo.build());
            }
        }
        if (reward.soldierMap.size() > 0) {
            for (Integer id : reward.soldierMap.keySet()) {
                Common.RewardInfo.Builder rewardInfo = Common.RewardInfo.newBuilder();
                rewardInfo.setPower(PlayerPowerDefine.BIG_POWER_SOLDIER);
                rewardInfo.setNum(reward.soldierMap.get(id));
                rewardInfo.setTypeid(id);
                rewardInfoList.add(rewardInfo.build());
            }
        }
        if (reward.counsellorMap.size() > 0) {
            for (Integer id : reward.counsellorMap.keySet()) {
                Common.RewardInfo.Builder rewardInfo = Common.RewardInfo.newBuilder();
                rewardInfo.setPower(PlayerPowerDefine.BIG_POWER_COUNSELLOR);
                rewardInfo.setNum(reward.counsellorMap.get(id));
                rewardInfo.setTypeid(id);
                rewardInfoList.add(rewardInfo.build());
            }
        }
        if (reward.generalMap.size() > 0) {
            for (Integer id : reward.generalMap.keySet()) {
                Common.RewardInfo.Builder rewardInfo = Common.RewardInfo.newBuilder();
                rewardInfo.setPower(PlayerPowerDefine.BIG_POWER_GENERAL);
                rewardInfo.setNum(reward.generalMap.get(id));
                rewardInfo.setTypeid(id);
                rewardInfoList.add(rewardInfo.build());
            }
        }
        if (reward.ordanceMap.size() > 0) {
            for (Integer id : reward.ordanceMap.keySet()) {
                Common.RewardInfo.Builder rewardInfo = Common.RewardInfo.newBuilder();
                rewardInfo.setPower(PlayerPowerDefine.BIG_POWER_ORDNANCE);
                rewardInfo.setNum(reward.ordanceMap.get(id));
                rewardInfo.setTypeid(id);
                rewardInfoList.add(rewardInfo.build());
            }
        }
        if (reward.ordanceFragmentMap.size() > 0) {
            for (Integer id : reward.ordanceFragmentMap.keySet()) {
                Common.RewardInfo.Builder rewardInfo = Common.RewardInfo.newBuilder();
                rewardInfo.setPower(PlayerPowerDefine.BIG_POWER_ORDNANCE_FRAGMENT);
                rewardInfo.setNum(reward.ordanceFragmentMap.get(id));
                rewardInfo.setTypeid(id);
                rewardInfoList.add(rewardInfo.build());
            }
        }
    }

    public M2.M20007.S2C getRewardClientInfo(PlayerReward reward) {
        M2.M20007.S2C.Builder builder = M2.M20007.S2C.newBuilder();
        ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
        for (Integer id : reward.addItemMap.keySet()) {
            Common.ItemInfo info = itemProxy.getItemInfo(id);
            builder.addItemList(info);
        }
        SoldierProxy soldierProxy = getGameProxy().getProxy(ActorDefine.SOLDIER_PROXY_NAME);
        for (Integer id : reward.soldierMap.keySet()) {
            Common.SoldierInfo info = soldierProxy.getSoldierInfo(id);
            builder.addSoldierList(info);
        }
        EquipProxy equipProxy = getGameProxy().getProxy(ActorDefine.EQUIP_PROXY_NAME);
        for (Long id : reward.generalList) {
            Common.EquipInfo equipInfo = equipProxy.getEquipInfo(id);
            builder.addEquipinfos(equipInfo);
        }
        OrdnancePieceProxy ordnancePieceProxy = getGameProxy().getProxy(ActorDefine.ORDANCEPIECE_PROXY_NAME);
        for (Integer id : reward.ordanceFragmentMap.keySet()) {
            Common.OrdnancePieceInfo pieceInfo = ordnancePieceProxy.getOrdnancePieceInfo(id);
            builder.addOdpInfos(pieceInfo);

        }
        OrdnanceProxy ordnanceProxy = getGameProxy().getProxy(ActorDefine.ORDANCE_PROXY_NAME);
        for (Long id : reward.ordanceList) {
            Common.OrdnanceInfo ordnanceInfo = ordnanceProxy.getOrdnanceInfo(id);
            builder.addOdInfos(ordnanceInfo);

        }
        AdviserProxy adviserProxy=getGameProxy().getProxy(ActorDefine.ADVISER_PROXY_NAME);
        for(int id:reward.counsellorMap.keySet()){
            Common.AdviserInfo info=adviserProxy.getAdviserInfoBytypeId(id);
            builder.addAdviserInfos(info);
        }
        return builder.build();
    }


    public M2.M20007.S2C getItemListClientInfo(List<Integer> itemlist) {
        M2.M20007.S2C.Builder builder = M2.M20007.S2C.newBuilder();
        ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
        for (Integer id : itemlist) {
            Common.ItemInfo info = itemProxy.getItemInfo(id);
            builder.addItemList(info);
        }
        return builder.build();
    }

    public String reward2String(PlayerReward reward) {
        List<Common.RewardInfo> rewardInfos = new ArrayList<Common.RewardInfo>();
        getRewardInfoByReward(reward, rewardInfos);
        StringBuffer buffer = new StringBuffer();
        for (Common.RewardInfo rewardInfo : rewardInfos) {
            buffer.append(rewardInfo.getPower());
            buffer.append(",");
            buffer.append(rewardInfo.getTypeid());
            buffer.append(",");
            buffer.append(rewardInfo.getNum());
            buffer.append("&");
        }
        return buffer.toString();
    }
}
