package com.znl.core;

import com.znl.utils.GameUtils;

/**
 * Created by Administrator on 2015/11/28.
 */
public class PlayerChat {
    public Integer type = 0;//聊天类型
    public Long legionId = 0l;//军团id
    public String context ;
    public Long playerId = 0l;
    public String playerName;
    public Integer iconId =1;
    public Integer vipLevel = 0;
    public Long time = GameUtils.getServerDate().getTime();
    public Integer playerType = 0;
    public Integer pendantId = 0;
    public String legionName = "";
    public int level;//玩家等级
    
    public PlayerChat(){

    }
}
