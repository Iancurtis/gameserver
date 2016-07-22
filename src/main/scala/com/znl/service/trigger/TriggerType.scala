package com.znl.service.trigger

/**
 * 触发类型
 * Created by Administrator on 2015/11/20.
 */
object TriggerType extends Enumeration{
  type TriggerType = Value
  val COUNT_DOWN = Value(1)  //倒计时,通过传入剩余时间进行触发
  val WHOLE_MINUTE = Value(2) //整分 触发 每到 * : * : 00 触发
  val WHOLE_HOUR = Value(3) //整小时触发 每到 * : 00 : 00 触发
}
