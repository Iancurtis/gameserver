package com.znl.proxy

import java.io.{BufferedReader, FileInputStream, InputStreamReader, File}
import javax.script.ScriptEngineManager

import com.znl.define.GameDefine

/**
 * Created by Administrator on 2015/10/30.
 */
object ScriptProxy {

  val scriptEngineManager = new ScriptEngineManager()
  val engine = scriptEngineManager.getEngineByName("nashorn")

  def loadScript(name : String) ={

    val path = GameDefine.SCRIPT_PATH + File.separator + name + ".js"
    val reader: InputStreamReader = new InputStreamReader(new FileInputStream(path), "utf-8" )
    val bufferedReader = new BufferedReader(reader)
    var line = ""
    val strBuilder = StringBuilder.newBuilder
    line = bufferedReader.readLine()
    while ( line != null ){
      strBuilder.append(line)
      line = bufferedReader.readLine()
    }

    engine.eval(strBuilder.toString())

  }

  def runScript(script : String) ={
    engine.eval(script)
  }
}
