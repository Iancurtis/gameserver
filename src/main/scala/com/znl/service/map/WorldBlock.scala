package com.znl.service.map

import java.util

/**
 * Created by Administrator on 2015/11/12.
 */
class WorldBlock {
  private[this] var _sortId: Int = 0
  private[this] var _curPlayerNum: Int = 0
  private[this] var _maxPlayerNum: Int = 0
  private[this] var _curEmptyTileNum: Int = 0
  private[this] var _worldTileMap: util.HashMap[String, WorldTile] = null

  def worldTileMap: util.HashMap[String, WorldTile] = _worldTileMap

  def worldTileMap_(value: util.HashMap[String, WorldTile]): Unit = {
    _worldTileMap = value
  }

  def sortId: Int = _sortId

  def sortId_(value: Int): Unit = {
    _sortId = value
  }

  def curPlayerNum: Int = _curPlayerNum

  def curPlayerNum_(value: Int): Unit = {
    _curPlayerNum = value
  }

  def maxPlayerNum: Int = _maxPlayerNum

  def maxPlayerNum_(value: Int): Unit = {
    _maxPlayerNum = value
  }

  def curEmptyTileNum: Int = _curEmptyTileNum

  def curEmptyTileNum_(value: Int): Unit = {
    _curEmptyTileNum = value
  }

  private[this] var _xOrigin: Int = 0

  def xOrigin: Int = _xOrigin

  def xOrigin_(value: Int): Unit = {
    _xOrigin = value
  }

  private[this] var _xEnd: Int = 0

  def xEnd: Int = _xEnd

  def xEnd_(value: Int): Unit = {
    _xEnd = value
  }

  private[this] var _yOrigin: Int = 0

  def yOrigin: Int = _yOrigin

  def yOrigin_(value: Int): Unit = {
    _yOrigin = value
  }

  private[this] var _yEnd: Int = 0

  def yEnd: Int = _yEnd

  def yEnd_(value: Int): Unit = {
    _yEnd = value
  }

}
