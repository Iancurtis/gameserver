package com.znl.utils

import java.util
import java.util.Collections

import scala.collection.JavaConversions._
/**
 * 概率为10的倍数
 * 随机发射器
 * 已10为单位 一组 id -> rate 对应的列表，进行随机出 对应的id
 * 千分比
 * Created by Administrator on 2015/11/11.
 */
class RandomEmitter(val list : java.util.List[(Integer, Integer)], val precision : Int = 10) {

  val idList = new util.ArrayList[Int]()
  list.foreach( f => {
    val id : Int = f._1.asInstanceOf[Int]
    val rate : Int = f._2.asInstanceOf[Int]

    val num : Int = math.rint( rate / precision).toInt
    for(i <- 0 until num){
      idList.add(id)
    }
  })

  Collections.shuffle(idList, GameUtils.random) //打乱
  val randomNum = math.floor(1000 / precision).toInt

  def emitter() : Int = {
    val index = GameUtils.getRandomValueByRange(randomNum)
    if(index >= idList.size()){
      -1
    }else
    {
      idList.get(index)
    }
  }


}


