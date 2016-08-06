package com.znl.proxy;

import com.znl.base.BasicProxy;
import com.znl.core.SimplePlayer;
import com.znl.define.ActorDefine;
import com.znl.proto.M17;
import com.znl.service.PlayerService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2015/12/7.
 */
public class FriendProxy extends BasicProxy {
    @Override
    public void shutDownProxy() {

    }

    @Override
    protected void init() {

    }

    public FriendProxy(String areaKey){
        this.areaKey = areaKey;
    }

    public List<M17.FriendInfo> getFriendInfoList(List<SimplePlayer> simplePlayerList){

        List<M17.FriendInfo> list = new ArrayList<>();
        for(SimplePlayer simplePlayer : simplePlayerList){
            if(simplePlayer!= null){
                list.add(getFriendInfo(simplePlayer));
            }
        }
        return list;
    }


    public M17.FriendBlessInfos getFriendBlessInfo(){
        M17.FriendBlessInfos.Builder builder=M17.FriendBlessInfos.newBuilder();
        PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        Set<Long> friendIdSet = playerProxy.getFriendSet();
        Set<Long> beBlessIdSet = playerProxy.getBeBlessSet();
        List<SimplePlayer> friendList= PlayerService.onGetPlayerSimpleInfoList(friendIdSet,playerProxy.getAreaKey());
        List<SimplePlayer> beBlessList=PlayerService.onGetPlayerSimpleInfoList(beBlessIdSet,playerProxy.getAreaKey());
        List<M17.FriendInfo> list =getFriendInfoList(friendList);
        List<M17.BlessInfo> blessInfoList = getBlessInfoList(beBlessList);
        builder.addAllBlessInfos(blessInfoList);
        builder.addAllFriendInfos(list);
        return builder.build();
    }

    public List<M17.BlessInfo> getBlessInfoList(List<SimplePlayer> simplePlayerList){
        List<M17.BlessInfo> list = new ArrayList<>();
        simplePlayerList.forEach( simplePlayer -> list.add(getBlessInfo(simplePlayer)));

        return list;
    }

    public M17.FriendInfo getFriendInfo(SimplePlayer simplePlayer){

        PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        boolean isBless = playerProxy.isBlessed(simplePlayer.getId());
        int blessState = 0;
        if(isBless){
            blessState = 1;
        }

        return M17.FriendInfo.newBuilder()
                .setLevel(simplePlayer.getLevel())
                .setName(simplePlayer.getName())
                .setBlessState(blessState)
                .setFight(simplePlayer.getCapacity())
                .setIconId(simplePlayer.getIconId())
                .setPendantId(simplePlayer.getPendant())
                .setPlayerId(simplePlayer.getId()).build();
    }

    public M17.BlessInfo getBlessInfo(SimplePlayer simplePlayer){
        PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        boolean isGetBless = playerProxy.isGetBless(simplePlayer.getId());
        int getBlessState = 0;
        if(isGetBless){
            getBlessState = 1;
        }

        return M17.BlessInfo.newBuilder()
                .setLevel(simplePlayer.getLevel())
                .setName(simplePlayer.getName())
                .setGetState(getBlessState)
                .setIconId(simplePlayer.getIconId())
                .setPendantId(simplePlayer.getPendant())
                .setPlayerId(simplePlayer.getId()).build();
    }
}
