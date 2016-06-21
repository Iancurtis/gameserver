package com.znl.proxy;

import com.znl.base.BaseDbPojo;
import com.znl.base.BaseSetDbPojo;
import com.znl.base.BasicProxy;
import com.znl.core.PlayerCache;
import com.znl.core.PlayerReward;
import com.znl.define.*;
import com.znl.log.ItemGet;
import com.znl.log.ItemLost;
import com.znl.log.admin.tbllog_items;
import com.znl.pojo.db.Armygroup;
import com.znl.pojo.db.Collect;
import com.znl.pojo.db.Item;
import com.znl.pojo.db.set.RoleNameSetDb;
import com.znl.proto.Common;
import com.znl.proto.M8;
import com.znl.proto.M9;
import com.znl.utils.GameUtils;
import org.apache.mina.util.ConcurrentHashSet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by Administrator on 2015/10/28.
 */
public class CollectProxy extends BasicProxy {
    private Set<Collect> cols = new ConcurrentHashSet<>();

    @Override
    public void shutDownProxy() {
        for (Collect col : cols) {
            col.finalize();
        }
    }

    @Override
    protected void init() {

    }

    public void saveCollect(){
        for(Collect col:cols){
            col.save();
        }
    }


    public CollectProxy(Set<Long> icIds,String areaKey) {
        this.areaKey = areaKey;
        for (Long id : icIds) {
            Collect col = BaseDbPojo.get(id, Collect.class,areaKey);
            cols.add(col);
        }
    }


    public long addCollect(String name,int icon,int pointx,int pointy, List<Integer> typelist,int level,int ower){
        Collect col=isHasCollect(pointx,pointy);
        Set<Integer> set=new HashSet<Integer>();
        set.addAll(typelist);
        if(col!=null){
            col.setIcon(icon);
            col.setLevel(level);
            col.setName(name);
            col.setPointx(pointx);
            col.setPointy(pointy);
            col.setIcon(icon);
            col.setTypelist(set);
            col.setOwer(ower);
            col.save();
            return col.getId();
        }else {
            Collect colnew=BaseDbPojo.create(Collect.class,areaKey);
            colnew.setIcon(icon);
            colnew.setLevel(level);
            colnew.setName(name);
            colnew.setPointx(pointx);
            colnew.setPointy(pointy);
            colnew.setIcon(icon);
            colnew.setTypelist(set);
            colnew.setOwer(ower);
            colnew.save();
            cols.add(colnew);
            PlayerProxy playerProxy=getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            playerProxy.addColllecttoPlayer(col.getId());
            return colnew.getId();
        }
    }

    //删除收藏
    public void removeCol(long id){
       Collect col=getCollecet(id);
        if(col!=null){
            col.del();
            cols.remove(col);
        }
        sendFunctionLog(FunctionIdDefine.DELETE_COLLECT_FUNCTION_ID,col.getPointx(),col.getPointy(),0l,col.getName());
    }


    private Collect getCollecet(long id){
      for(Collect col:cols){
          if(col.getId()==id){
              return col;
          }
      }
        return null;
    }
    //判断改坐标的收藏有没有
    public  Collect isHasCollect(int x,int y){
        for(Collect col:cols){
            if(col.getPointx()==x&&col.getPointy()==y){
                return col;
            }
        }
        return null;
    }

    //添加收藏
    public int addCollectByrequest(int rs, M8.CollectInfo collectInfo,int ower){
        if(rs==0){
            return -rs;
        }
        if (ower==3){
           return  ErrorCodeDefine.M80008_2;
        }
        addCollect(collectInfo.getName(),collectInfo.getIconId(),collectInfo.getX(),collectInfo.getY(),collectInfo.getTypelistList(),collectInfo.getLevel(),ower);
        return 0;
    }

    public List<M8.CollectInfo>  getCollectInfos(){
        List<M8.CollectInfo> list=new ArrayList<M8.CollectInfo>();
        for(Collect collect:cols){
            M8.CollectInfo.Builder info=M8.CollectInfo.newBuilder();
            info.setIconId(collect.getIcon());
            info.setId(collect.getId());
            info.setLevel(collect.getLevel());
            info.setName(collect.getName());
            info.setOwer(collect.getOwer());
            List<Integer> typelist=new ArrayList<Integer>();
            typelist.addAll(collect.getTypelist());
            info.addAllTypelist(typelist);
            info.setX(collect.getPointx());
            info.setY(collect.getPointy());
            list.add(info.build());
        }
        return list;
    }

}