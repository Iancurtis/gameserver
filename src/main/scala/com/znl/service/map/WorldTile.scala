package com.znl.service.map

import java.util

import com.znl.core.{PlayerTeam, PlayerTroop}
import com.znl.pojo.db.WorldBuilding
import com.znl.service.map.TileType.TileType

/**
 * 世界瓦格基础类
 * Created by Administrator on 2015/11/11.
 */
class WorldTile {
  private[this] var _x: Int = 0
  private[this] var _y : Int = 0
  private[this] var _tileType: TileType = TileType.Empty
  private[this] var _building : WorldBuilding = null  //
  private[this] var _resType : Int = 1 //随机生成的资源类型
  private[this] var _resId: Int = 1 //随机生成的资源ID
  private[this] var _resLv: Int = 1  //资源等级 2的倍数
  private[this] var _resPointId: Int = 1 //资源位置ID
  private[this] var _monsterGroupId : Int = 0//随机到的怪物组id

  private[this] var _playerName : String = ""  //玩家名称
  private[this] var _playerLevel : Int = 0  //玩家等级
  private[this] var _pendant: Int=0 // 挂件
  private[this] var _degree: Int=0 //繁荣度
  private[this] var _degreemax: Int=0 //繁荣度上限
  private[this] var _legionName : String = ""  //军团名字

  //外观相关
  private[this] var _boomState : Int = 0
  private[this] var _icon : Int = 0
  private[this] var _protect : Boolean = false
  private[this] var _cityicon : Int = 0

  def legionName = _legionName
  def legionName_(value : String) : Unit ={
    _legionName = value
  }

  def cityicon = _cityicon
  def cityicon_(value : Int) : Unit ={
    _cityicon = value
  }

  def degreemax: Int = _degreemax

  def degreemax_(value: Int): Unit = {
    _degreemax = value
  }

  def degree: Int = _degree

  def degree_(value: Int): Unit = {
    _degree = value
  }

  def pendant: Int = _pendant

  def pendant_(value: Int): Unit = {
    _pendant = value
  }


  def playerName = _playerName
  def playerName_(value : String) : Unit ={
    _playerName = value
  }

  def playerLevel = _playerLevel
  def playerLevel_(value : Int) : Unit ={
    _playerLevel = value
  }

  def boomState : Int = _boomState
  def boomState_(value : Int) : Unit ={
    _boomState = value
  }

  def icon : Int = _icon
  def icon_(value : Int) : Unit ={
    _icon = value
  }

  def protect : Boolean = _protect
  def protetc_(value : Boolean) : Unit ={
    _protect = value
  }

  def x: Int = _x
  def x_(value: Int): Unit = {
    _x = value
  }

  def y: Int = _y
  def y_(value: Int): Unit = {
    _y = value
  }

  def tileType: TileType = _tileType

  def tileType_(value: TileType): Unit = {
    _tileType = value
  }

  def resId: Int = _resId

  def resId_(value: Int): Unit = {
    _resId = value
  }

  def resType: Int = _resType

  def resType_(value: Int): Unit = {
    _resType = value
  }

  def resLv: Int = _resLv

  def resLv_(value: Int): Unit = {
    _resLv = value
  }

  def resPointId: Int = _resPointId

  def resPointId_(value: Int): Unit = {
    _resPointId = value
  }

  def building: WorldBuilding = _building

  def building_(value: WorldBuilding): Unit = {
    _building = value
  }

  def monsterGroupId: Int = _monsterGroupId

  def monsterGroupId_(value: Int): Unit = {
    _monsterGroupId = value
  }
}

object WorldTile {

}
