package com.znl.proxy;

import com.znl.base.BaseDbPojo;
import com.znl.base.BasicProxy;
import com.znl.define.*;
import com.znl.log.CustomerLogger;
import com.znl.pojo.db.ItemBuff;
import com.znl.proto.M9;
import com.znl.utils.GameUtils;
import org.apache.mina.util.ConcurrentHashSet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by Administrator on 2015/11/30.
 */
public class ItemBuffProxy extends BasicProxy {
    private Set<ItemBuff> itemBuffs = new ConcurrentHashSet<ItemBuff>();

    @Override
    public void shutDownProxy() {
        for (ItemBuff ib : itemBuffs) {
            ib.finalize();
        }
    }

    @Override
    protected void init() {
        super.expandPowerMap.clear();
        for (ItemBuff ibf : itemBuffs) {
            if (ibf.getState() == 1) {
                addItemBuffPlayerPower(ibf.getPowerId(), ibf.getValue());
            }
        }
    }

    private LinkedList<ItemBuff> changeBuff = new LinkedList<ItemBuff>();

    private void pushItemBuffToChangeList(ItemBuff itemBuff) {
        synchronized (changeBuff) {
            if (!changeBuff.contains(itemBuff)) {
                changeBuff.offer(itemBuff);
            }
        }
        refurceExpandPowerMap();
    }

    public void saveItemBuff() {
        List<ItemBuff> buffList = new ArrayList<ItemBuff>();
        synchronized (changeBuff) {
            while (true) {
                ItemBuff itemBuff = changeBuff.poll();
                if (itemBuff == null) {
                    break;
                }
                buffList.add(itemBuff);
            }
        }
        for (ItemBuff ib : buffList) {
            ib.save();
        }

    }

    /**
     * 属性效果加成
     */
    private void addItemBuffPlayerPower(int power, long value) {
        if (super.expandPowerMap.get(power) == null) {
            super.expandPowerMap.put(power, value);
        } else {
            super.expandPowerMap.put(power, super.expandPowerMap.get(power) + value);
        }
    }

    public ItemBuffProxy(Set<Long> buffId,String areaKey) {
        this.areaKey = areaKey;
        for (Long id : buffId) {
            ItemBuff tech = BaseDbPojo.get(id, ItemBuff.class,areaKey);
            if(tech!=null) {
                itemBuffs.add(tech);
            }
        }
        init();
    }

    /***
     * 创建ItemBuff
     */
    public void createItemBuff(int itemId, int type, int powerId, int value, int times, int num) {
        GameProxy gameProxy = super.getGameProxy();
        PlayerProxy playerProxy = gameProxy.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        ItemBuff itemBuff = BaseDbPojo.create(ItemBuff.class,areaKey);
        long beginTime = GameUtils.getServerDate().getTime();
        JSONObject jsonObject=ConfigDataProxy.getConfigInfoFindById(DataDefine.ITEM_BUFF_FACE, powerId);
        List<ItemBuff> del=new ArrayList<ItemBuff>();
        if(jsonObject!=null &&powerId!= 317) {
            for (ItemBuff buff : itemBuffs) {
                if (buff.getType() == ItemDefine.ITEM_CHANGE_ATTR &&buff.getPowerId()>300&&buff.getPowerId()!=317) {
                    del.add(buff);
                }
            }
        }
        for(ItemBuff itemBuff1:del){
            itemBuffs.remove(itemBuff1);
            itemBuff1.del();
            if (changeBuff.contains(itemBuff1)) {
                changeBuff.remove(itemBuff1);
            }
        }
        ItemBuff ib = getBuffByPowerId(powerId);
        itemBuff.setPowerId(powerId);
        itemBuff.setValue(value);
        itemBuff.setPlayerId(playerProxy.getPlayerId());
        itemBuff.setType(type);
        itemBuff.setItemId(itemId);
        itemBuff.setState(0);
        if (powerId == PlayerPowerDefine.NOR_POWER_facade) { /****** 外观 *****/
            if (ib != null) {
                if (ib.getPowerId() == powerId && ib.getValue() == value) {
                    ib.setEndTime(ib.getEndTime() + (times * TimerDefine.BUFF_MSEL) * num);
                    ib.save();
                    playerProxy.setPowerValue(PlayerPowerDefine.NOR_POWER_facade, (long) itemId);
                } else {
                    ib.setBeginTime(beginTime);
                    ib.setValue(value);
                    ib.setEndTime(beginTime + (times * TimerDefine.BUFF_MSEL) * num);
                    ib.save();
                    playerProxy.setPowerValue(PlayerPowerDefine.NOR_POWER_facade, (long) itemId);
                    /**begin**********************外观替换**/
                    int id = ib.getItemId();
                    JSONObject obj = ConfigDataProxy.getConfigInfoFindById(DataDefine.ITEM_METIC, id);
                    JSONArray faceInfo = obj.getJSONArray("effect");
                    for (int i = 0; i < faceInfo.length(); i++) {
                        int buffId = faceInfo.getInt(i);
                        JSONObject objInfo = ConfigDataProxy.getConfigInfoFindById(DataDefine.ITEM_BUFF_FACE, buffId);
                        JSONArray array = objInfo.getJSONArray("power");
                        for (int j = 0; j < array.length(); j++) {
                            JSONArray defien = array.getJSONArray(j);
                            int delpowerId = defien.getInt(0);
                            int delvalue = defien.getInt(1);
                            //int deltimes = defien.getInt(2);
                            if (delpowerId != ib.getPowerId()) {
                                ItemBuff delinfo = getBuffByPowerId(delpowerId);
                                if (delinfo != null) {
                                    if (delinfo.getPowerId() == delpowerId && delinfo.getType() == type && delinfo.getValue() == delvalue) {
                                        playerProxy.reduceItemBuffFormPlayer(delinfo.getId());
                                        itemBuffs.remove(delinfo);
                                        delinfo.del();
                                        if (changeBuff.contains(delinfo)) {
                                            changeBuff.remove(delinfo);
                                        }
                                    }
                                }
                            }
                            System.err.println(powerId + " //TODO 外观 old buff" + id);
                        }
                    }
                    refurceExpandPowerMap();
                    /**end*********************外观替换**/
                    ib.setItemId(itemId);
                    System.err.println(powerId + " //TODO 外观 new buff" + itemId);
                }
                /********相同power，相同效果，时间累加**********/
            } else {
                itemBuff.setBeginTime(beginTime);
                itemBuff.setEndTime(beginTime + (times * TimerDefine.BUFF_MSEL) * num);
                itemBuff.save();
                itemBuffs.add(itemBuff);
                playerProxy.addItemBuff(itemBuff.getId());
                playerProxy.setPowerValue(PlayerPowerDefine.NOR_POWER_facade, (long) itemId);
            }
        } else {/********* 道具buff ********/
            if (ib != null) {//道具相同的情况
                if (ib.getPowerId() == powerId && ib.getValue() == value) {//效果一样，
                   // ib.setEndTime(ib.getEndTime() + (times * TimerDefine.BUFF_MSEL) * num);
                    itemBuff.setEndTime(beginTime+15000);
                    ib.save();
                    /********相同power，相同效果，时间累加**********/
                } else {//效果不一样
                    itemBuff.setBeginTime(ib.getEndTime());
                   // itemBuff.setEndTime(ib.getEndTime() + (times * TimerDefine.BUFF_MSEL) * num);
                    itemBuff.setEndTime(beginTime+15000);
                    itemBuffs.add(itemBuff);
                    itemBuff.save();
                    playerProxy.addItemBuff(itemBuff.getId());
                }
            } else {
                itemBuff.setBeginTime(beginTime);
                itemBuff.setEndTime(beginTime + (times * TimerDefine.BUFF_MSEL) * num);
                itemBuff.setEndTime(beginTime+15000);
                itemBuffs.add(itemBuff);
                itemBuff.save();
                playerProxy.addItemBuff(itemBuff.getId());
            }
        }
        refurceExpandPowerMap();
    }

    /***
     * 使用道具，增加buff
     */
    public void addItemBuff(int itemId, int type, int powerId, int value, int times, int num) {
        createItemBuff(itemId, type, powerId, value, times, num);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        long beginTime = GameUtils.getServerDate().getTime();
        ItemBuff itbuf = getBuffByPowerId(powerId);
        if (itbuf != null) {
            if (itbuf.getBeginTime() <= beginTime && itbuf.getEndTime() > beginTime) {
                    itbuf.setState(1); //开启定时器，设置状态为1，正在使用效果
                    itbuf.save();
                    refurceExpandPowerMap();
                //免战buff
                if (powerId == PlayerPowerDefine.NOR_POWER_protect_date) {
                    long addtime=times * TimerDefine.BUFF_MSEL*num;
                    long overTime=0l;
                    if(playerProxy.getProtectOverDate()<GameUtils.getServerDate().getTime()) {
                         overTime = (GameUtils.getServerDate().getTime()) + addtime;
                    }else{
                        overTime=playerProxy.getProtectOverDate()+addtime;
                    }
                    playerProxy.setProtectOverDate(overTime);
                }
            }
        }
    }

    public boolean isPushBuff = false;

    /**
     * 时间过期,清除buff
     * overdue 过期时间
     */
    public void overTimeClearBuff(long overdue) {
        isPushBuff = false;
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        List<ItemBuff> delList = new ArrayList<ItemBuff>();
        for (ItemBuff itembuff : itemBuffs) {
            if (itembuff.getEndTime() <= overdue) { //过期的删掉
                delList.add(itembuff);
                //免战buff
                if (itembuff.getPowerId() == PlayerPowerDefine.NOR_POWER_protect_date) {
                    long overTime = (GameUtils.getServerDate().getTime());
                    playerProxy.setProtectOverDate(overTime);
                }
            }
        }
        //过期buff的删掉
        if (delList.size() > 0) {
            for (int i = 0; i < delList.size(); i++) {
                ItemBuff itembuff = delList.get(i);
                playerProxy.reduceItemBuffFormPlayer(itembuff.getId());
                itemBuffs.remove(itembuff);
                itembuff.del();
                if (changeBuff.contains(itembuff)) {
                    changeBuff.remove(itembuff);
                }
            }
        }
        for (ItemBuff itembuff : itemBuffs) {
            long beginTime = GameUtils.getServerDate().getTime();
            if (itembuff.getBeginTime() <= overdue && itembuff.getEndTime() > overdue) { //开始执行可以执行的
                    itembuff.setState(1); //开启定时器，设置状态为1，正在使用效果
                    itembuff.save();
                    isPushBuff = true;
            }

        }
    }


    public void delBuffer(int powerId){
        ItemBuff itembuff=getBuffByPowerId(powerId);
        if(itembuff!=null) {
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            playerProxy.reduceItemBuffFormPlayer(itembuff.getId());
            itemBuffs.remove(itembuff);
            itembuff.del();
            if (changeBuff.contains(itembuff)) {
                changeBuff.remove(itembuff);
            }
        }
        refurceExpandPowerMap();
    }

    /**
     * 取某个使用中的buff
     */
    private ItemBuff getBuffByPowerId(int powerId) {
        for (ItemBuff itemBuff : itemBuffs) {
            if (itemBuff.getPowerId() == powerId) {
                return itemBuff;
            }
        }
        return null;
    }

    /**
     * 返回离过期时间最近的一个buff
     */
    public long getWillBeOverdueBuff(long orverTime) {
        long now=GameUtils.getServerDate().getTime();
        if(orverTime>now){
            return now;
        }
        overTimeClearBuff(orverTime);
        long minTime = GameUtils.getServerDate().getTime();
        for (ItemBuff ibf : itemBuffs) {
            if (minTime == -1) {
                minTime = ibf.getEndTime();
            } else if (minTime > ibf.getEndTime()) {
                minTime = ibf.getEndTime();
            }
        }
        if(minTime>now){
            return now;
        }
        return minTime;
    }

    /**
     * 推送正在生效的ItemBuffInfo
     */
    public List<M9.ItemBuffInfo> sendItemBuffInfoToClient() {
        List<M9.ItemBuffInfo> itemBuffList = new ArrayList<M9.ItemBuffInfo>();
        itemBuffList.clear();
        for (ItemBuff itemBuff : itemBuffs) {
            if (itemBuff.getState() == 1) {
                System.err.println("bufferPower"+itemBuff.getPowerId()+"生效中");
                M9.ItemBuffInfo.Builder builder = M9.ItemBuffInfo.newBuilder();
                builder.setItemId(itemBuff.getItemId());
                builder.setType(itemBuff.getType());
                builder.setPowerId(itemBuff.getPowerId());
                builder.setValue(itemBuff.getValue());
                builder.setTime((int) ((itemBuff.getEndTime() - itemBuff.getBeginTime()) / 1000));
                System.err.println(itemBuff.getPowerId() + "发给客户端bufer时间" + ((itemBuff.getEndTime() - itemBuff.getBeginTime()) / 1000));
                builder.setRemainTime((int)(itemBuff.getEndTime() - GameUtils.getServerDate().getTime()) / 1000);//剩余时间
                itemBuffList.add(builder.build());
            }
        }
        return itemBuffList;
    }

    /**
     * 有效的临时建筑位
     */
    public int getValidBuildSize() {
        int num=0;
        for (ItemBuff itemBuff : itemBuffs) {
            if (itemBuff.getState() == 1 && itemBuff.getPowerId() == PlayerPowerDefine.NOR_POWER_temporary_building) {
                num++;
            }
            if (itemBuff.getState() == 1 && itemBuff.getPowerId() == PlayerPowerDefine.NOR_POWER_tempro) {
                num++;
            }
        }
        return num;
    }

    //获得某种buffer的剩余时间
    public int getBufferLesTimeByPower(int power) {
        for (ItemBuff itemBuff : itemBuffs) {
            if (itemBuff.getState() == 1 && itemBuff.getPowerId() == power) {
                long time = itemBuff.getEndTime() - GameUtils.getServerDate().getTime();
                if (time < 0) {
                    return 0;
                } else {
                    return (int) (time / 1000);
                }

            }
        }
        return 0;
    }


    /**
     * 90002
     * message S2C{
            required int32 rs = 1;//0:成功(已过期：remainTime=null;没有过期: 已过期：remainTime>0);-1：buffer不存在;
            required int32 itemId = 2;
            optional int32 remainTime = 6;
        }
     * 检测buff是否已经倒计时完
     * @param itemId 道具id
     * @return
     */
    public void checkBufferIsOverTime(int itemId,M9.M90002.S2C.Builder builder9002,M9.M90003.S2C.Builder builder9003){
        builder9002.setRs(0);
        builder9002.setItemId(itemId);
        ItemBuff itemBuff=null;
        for(ItemBuff buff:itemBuffs){
            if(buff.getItemId()==itemId){
                itemBuff=buff;
                break;
            }
        }
        if(itemBuff==null){
            builder9002.setRs(ErrorCodeDefine.M90002_2);
        }else{
            if(GameUtils.getServerDate().getTime()>=itemBuff.getEndTime()){
                builder9002.setRemainTime(0);
                int powerId=itemBuff.getPowerId();
                //移除过期的
                itemBuffs.remove(itemBuff);
                //拿到powerId相同的下一个ItemBffer，如果存在发给前端
                ItemBuff nextBuff= getBuffByPowerId(powerId);
                if(nextBuff!=null&&nextBuff.getState()==0&&nextBuff.getEndTime()>GameUtils.getServerDate().getTime()){
                    M9.ItemBuffInfo.Builder builderInfo = M9.ItemBuffInfo.newBuilder();
                    builder9003.setRs(0);
                    builderInfo.setItemId(itemBuff.getItemId());
                    builderInfo.setType(itemBuff.getType());
                    builderInfo.setPowerId(itemBuff.getPowerId());
                    builderInfo.setValue(itemBuff.getValue());
                    builderInfo.setTime((int) ((itemBuff.getEndTime() - itemBuff.getBeginTime()) / 1000));
                    builderInfo.setRemainTime((int) (itemBuff.getEndTime() - GameUtils.getServerDate().getTime()) / 1000);//剩余时间
                    builder9003.addItemBuffInfo(builderInfo);
                    nextBuff.setState(1);
                    nextBuff.save();
                }
            }else{
                long remianTime=itemBuff.getEndTime()-GameUtils.getServerDate().getTime();
                builder9002.setRemainTime((int)remianTime/1000);//重新返回剩余时间给前端
            }
        }
    }

    /**
     * 登录检测buffer的过期
     */
    @Override
    public void afterLoginEvent() {
        try {
            long serverTime=GameUtils.getServerDate().getTime();
            Iterator<ItemBuff>iterator=itemBuffs.iterator();
            while (iterator.hasNext()){
                ItemBuff buff=iterator.next();
                if(serverTime>=buff.getEndTime()){
                    int powerId=buff.getPowerId();
                    iterator.remove();
                    //找出相同类型的buff并且设置生效
                    ItemBuff itemBuff= getBuffByPowerId(powerId);
                    if(itemBuff!=null&&itemBuff.getState()==0&&itemBuff.getEndTime()>serverTime){
                        itemBuff.setState(1);
                        itemBuff.save();
                    }

                }
            }
        }catch (Exception e){
            CustomerLogger.error("afterLoginEvent error:",e);
        }
    }

}
