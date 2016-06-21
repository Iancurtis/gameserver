package com.znl.log

import java.nio.charset.Charset

import org.apache.flume.{EventDeliveryException, Event}
import org.apache.flume.api.{RpcClientFactory, RpcClient}
import org.apache.flume.event.EventBuilder

import scala.collection.JavaConversions._

/**
 * Created by Administrator on 2015/11/17.
 */
class MyRpcClientFacade {
  var client : RpcClient = null
  var hostname : String = ""
  var port : Int = 0

  def init(host : String, port : Int) ={
    this.hostname = host
    this.port = port
    this.client = RpcClientFactory.getDefaultInstance(host, port)
  }

  def sendDataToFlume(logType : String, data : String) ={
    val event : Event = EventBuilder.withBody(data, Charset.forName("UTF-8"))

    var map : Map[String, String] = Map()
    map += ( "logType" -> logType)
    event.setHeaders( map )
    try{
      this.client.append(event)
    }catch {
      case e: EventDeliveryException =>
        this.client.close()
        this.client = null
        this.client = RpcClientFactory.getDefaultInstance(this.hostname, this.port)
    }
  }

  def cleanUp = {
    client.close()
  }
}
