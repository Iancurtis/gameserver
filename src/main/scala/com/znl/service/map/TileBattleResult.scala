package com.znl.service.map

import java.util

import com.znl.core.{PlayerReward, SimplePlayer, PlayerTeam}
import com.znl.define.PlayerPowerDefine
import com.znl.proto.Common.RewardInfo
import com.znl.proto.M5
import com.znl.service.map.TileType._

/**
 * Created by Administrator on 2015/12/30.
 * 世界战斗结果的包装
 */
class TileBattleResult {
  var rs : Int = 0
  var attackSortId : Int = 0
  var attackX : Int = 0
  var attackY : Int = 0
  var defendSortId :Int = 0
  var defendX : Int = 0
  var defendY : Int = 0
  var defendTileType : TileType = TileType.Empty
  var defendId : Long = 0l
  var attackId : Long = 0l
  var winner : Int = 0;//1:防守赢了，2进攻赢了
  var attackTeam : util.List[PlayerTeam] = new util.ArrayList[PlayerTeam]()
  var defendTeam : util.List[PlayerTeam] = new util.ArrayList[PlayerTeam]()
  var rewardMap : util.HashMap[Integer,Integer] = new util.HashMap[Integer,Integer]()
  var attBoomAdd : Int = 0
  var defBoomReduce :Int = 0
  var battleBuilder: M5.Battle.Builder = null
  var rewardInfo : util.List[RewardInfo] = new util.ArrayList[RewardInfo]()
  var reward : PlayerReward = new PlayerReward
  var honner : Int = 0
  var firstHandle = 0 //0进攻先手，1防守先手
  var powerMap: util.Map[Integer, java.lang.Long] =new util.HashMap[Integer, java.lang.Long](){{put(PlayerPowerDefine.NOR_POWER_speedRate,0l);put(PlayerPowerDefine.NOR_POWER_resexprate,0l);put(PlayerPowerDefine.NOR_POWER_rescollectrate,0l);put(PlayerPowerDefine.NOR_POWER_loadRate,0l);}}
}
