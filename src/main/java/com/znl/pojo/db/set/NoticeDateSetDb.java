package com.znl.pojo.db.set;

import com.znl.base.BaseSetDbPojo;
import com.znl.core.Notice;
import com.znl.pojo.db.NoticeDate;
import com.znl.utils.GameUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 系统公告集合数据
 * Created by Administrator on 2016/1/14.
 */
public class NoticeDateSetDb extends BaseSetDbPojo {

    public void addTeamDate(Notice notice){
        String key = notice.getPlayerId() + "_" + notice.getBeginTime();
        NoticeDate noticeDate = null;
        if(this.isKeyExist(key)){
            noticeDate = this.getDbPojoByKey(key, NoticeDate.class);
        }else {
            noticeDate = this.createDbPojo(key, NoticeDate.class);
        }
        byte[] bytes = GameUtils.objectToBytes(notice);
        noticeDate.setTeam(bytes);
        noticeDate.save();
    }

    public List<Notice> getAllTeamDatas(){
        Set<String> keys = getAllKey();
        List<Notice> res = new ArrayList<>();
        for (String key : keys){
            NoticeDate noticeDate = getDbPojoByKey(key,NoticeDate.class);
            try {
                Object obj = GameUtils.ByteToObject(noticeDate.getTeam());
                res.add((Notice) obj);
            }catch (Exception e){

            }
        }
        return res;
    }

    public void deletNotice(Notice notice){
        String key = notice.getPlayerId() + "_" + notice.getBeginTime();
        removeKey(key);
        NoticeDate noticeDate = getDbPojoByKey(key,NoticeDate.class);
        if(noticeDate!=null) {
            noticeDate.del();
        }
    }
}
