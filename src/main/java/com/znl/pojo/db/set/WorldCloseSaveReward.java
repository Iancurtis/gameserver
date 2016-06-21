package com.znl.pojo.db.set;

import com.znl.base.BaseSetDbPojo;
import com.znl.pojo.db.WorldCloseReward;

/**
 * Created by Administrator on 2016/4/14.
 */
public class WorldCloseSaveReward extends BaseSetDbPojo {

    public String getReward(long playerId){
        WorldCloseReward reward = this.getDbPojoByKey(playerId+"",WorldCloseReward.class);
        if (reward == null){
            return null;
        }
        String res = reward.getReward();
        //获取完顺便删除
        removeKey(playerId+"");
        reward.del();
        return res;
    }

    public WorldCloseReward getWorldCloseRewardObj(long playerId){
        WorldCloseReward reward = this.getDbPojoByKey(playerId+"",WorldCloseReward.class);
        return reward;
    }

}
