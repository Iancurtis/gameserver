package com.znl.service.map

/**
 * Created by Administrator on 2015/11/11.
 */
object TileType extends Enumeration{
  type TileType = Value
  val Building = Value(1)  //有人的建筑
  val Resource = Value(2) //资源点
  val Empty = Value(3) //空地
}
