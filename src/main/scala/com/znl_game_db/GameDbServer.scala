package com.znl_game_db

import java.io.{FileInputStream, InputStream, File}
import java.util.Properties

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import com.znl.define.ActorDefine
import com.znl.server.RootGameDBSystem

/**
 * Created by Administrator on 2016/2/2.
 */
object GameDbServer extends App{

  val userDir: String = System.getProperty("user.dir")
  val confPath: String = userDir + File.separator + "properties" + File.separator + "db_game.conf"
  val file = new File(confPath)

  implicit var system = ActorSystem("GameDBServer", ConfigFactory.parseFile(file))
  val gamePropertiesPath: String = userDir + File.separator + "properties" + File.separator + "game.properties"
  val p: Properties = new Properties
  try {
    val inputStream: InputStream = new FileInputStream(gamePropertiesPath)
    p.load(inputStream)
    system.actorOf(RootGameDBSystem.props(p), ActorDefine.ROOT_GAME_NAME)
  }catch{
    case e:Exception =>
      e.printStackTrace()
  }
}
