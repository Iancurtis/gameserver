package com.znl.pojo.db.set;

import com.znl.base.BaseSetDbPojo;
import com.znl.core.Notice;
import com.znl.core.Situation;
import com.znl.pojo.db.SituationDate;
import com.znl.utils.GameUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 系统公告集合数据
 * Created by Administrator on 2016/1/14.
 */
public class SituationDateSetDb extends BaseSetDbPojo {

    public void addTeamDate(Situation situation){
        String key = situation.getArmyId() + "_" + situation.getEvenTime();
        SituationDate situationDate = null;
        if(this.isKeyExist(key)){
            situationDate = this.getDbPojoByKey(key, SituationDate.class);
        }else {
            situationDate = this.createDbPojo(key, SituationDate.class);
        }
        byte[] bytes = GameUtils.objectToBytes(situation);
        situationDate.setTeam(bytes);
        situationDate.setTeam(bytes);
        situationDate.save();
    }

    public List<Situation> getAllSituationDatas(long armyId){
        Set<String> keys = getAllKey();
        List<Situation> res = new ArrayList<Situation>();
        for (String key : keys){
            try {
                Long id=Long.parseLong(key.split("_")[0]);
                if(id==armyId) {
                    SituationDate situationDate = this.getDbPojoByKey(key, SituationDate.class);
                    Object obj = GameUtils.ByteToObject(situationDate.getTeam());
                    res.add((Situation) obj);
                }
            }catch (Exception e){

            }
        }
        return res;
    }

    public void deletNotice(Situation situation){
        String key = situation.getArmyId() + "_" +situation.getEvenTime();
        removeKey(key);
        SituationDate noticeDate = getDbPojoByKey(key,SituationDate.class);
        if(noticeDate!=null) {
            noticeDate.del();
        }
    }
}
