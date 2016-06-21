package com.znl.log

import akka.event.Logging
import com.znl.GameMainServer

class CustomerLogger{

}

/**
 * Created by Administrator on 2015/10/22.
 */
object CustomerLogger {

  val log = Logging(GameMainServer.system, classOf[CustomerLogger])

  def error(cause : scala.Throwable, message : scala.Predef.String) : scala.Unit = {
    log.error(cause, message)
  }
  def error(cause : scala.Throwable, template : scala.Predef.String, arg1 : scala.Any) : scala.Unit = {
    log.error(cause, template, arg1)
  }
  def error(cause : scala.Throwable, template : scala.Predef.String, arg1 : scala.Any, arg2 : scala.Any) : scala.Unit = {
    log.error(cause, template, arg1, arg2)
  }
  def error(cause : scala.Throwable, template : scala.Predef.String, arg1 : scala.Any, arg2 : scala.Any, arg3 : scala.Any) : scala.Unit = {
    log.error(cause, template, arg1, arg2, arg3)
  }
  def error(cause : scala.Throwable, template : scala.Predef.String, arg1 : scala.Any, arg2 : scala.Any, arg3 : scala.Any, arg4 : scala.Any) : scala.Unit = {
    log.error(cause, template, arg1, arg2, arg3, arg4)
  }
  def error(message : scala.Predef.String) : scala.Unit = {
    log.error( message)
  }
  def error(template : scala.Predef.String, arg1 : scala.Any) : scala.Unit = {
    log.error( template, arg1)
  }
  def error(template : scala.Predef.String, arg1 : scala.Any, arg2 : scala.Any) : scala.Unit = {
    log.error( template, arg1, arg2)
  }
  def error(template : scala.Predef.String, arg1 : scala.Any, arg2 : scala.Any, arg3 : scala.Any) : scala.Unit = {
    log.error( template, arg1, arg2, arg3)
  }
  def error(template : scala.Predef.String, arg1 : scala.Any, arg2 : scala.Any, arg3 : scala.Any, arg4 : scala.Any) : scala.Unit = {
    log.error( template, arg1, arg2, arg3, arg4)
  }
  def warning(message : scala.Predef.String) : scala.Unit = {
    log.warning( message)
  }
  def warning(template : scala.Predef.String, arg1 : scala.Any) : scala.Unit = {
    log.warning( template, arg1)
  }
  def warning(template : scala.Predef.String, arg1 : scala.Any, arg2 : scala.Any) : scala.Unit = {
    log.warning( template, arg1, arg2)
  }
  def warning(template : scala.Predef.String, arg1 : scala.Any, arg2 : scala.Any, arg3 : scala.Any) : scala.Unit = {
    log.warning( template, arg1, arg2, arg3)
  }
  def warning(template : scala.Predef.String, arg1 : scala.Any, arg2 : scala.Any, arg3 : scala.Any, arg4 : scala.Any) : scala.Unit = {
    log.warning( template, arg1, arg2, arg3, arg4)
  }
  def info(message : scala.Predef.String) : scala.Unit = {
    log.info( message)
  }
  def info(template : scala.Predef.String, arg1 : scala.Any) : scala.Unit = {
    log.info( template, arg1)
  }
  def info(template : scala.Predef.String, arg1 : scala.Any, arg2 : scala.Any) : scala.Unit = {
    log.info( template, arg1, arg2)
  }
  def info(template : scala.Predef.String, arg1 : scala.Any, arg2 : scala.Any, arg3 : scala.Any) : scala.Unit = {
    log.info( template, arg1, arg2,arg3)
  }
  def info(template : scala.Predef.String, arg1 : scala.Any, arg2 : scala.Any, arg3 : scala.Any, arg4 : scala.Any) : scala.Unit = {
    log.info( template, arg1, arg2,arg3, arg4)
  }
  def debug(message : scala.Predef.String) : scala.Unit = {
    log.debug( message)
  }
  def debug(template : scala.Predef.String, arg1 : scala.Any) : scala.Unit = {
    log.debug( template, arg1)
  }
  def debug(template : scala.Predef.String, arg1 : scala.Any, arg2 : scala.Any) : scala.Unit = {
    log.debug( template, arg1, arg2)
  }
  def debug(template : scala.Predef.String, arg1 : scala.Any, arg2 : scala.Any, arg3 : scala.Any) : scala.Unit = {
    log.debug( template, arg1, arg2, arg3)
  }
  def debug(template : scala.Predef.String, arg1 : scala.Any, arg2 : scala.Any, arg3 : scala.Any, arg4 : scala.Any) : scala.Unit = {
    log.debug( template, arg1, arg2, arg3, arg4)
  }
  def log(level : akka.event.Logging.LogLevel, message : scala.Predef.String) : scala.Unit = {
    log.log( level, message)
  }
  def log(level : akka.event.Logging.LogLevel, template : scala.Predef.String, arg1 : scala.Any) : scala.Unit = {
    log.log( level, template, arg1)
  }
  def log(level : akka.event.Logging.LogLevel, template : scala.Predef.String, arg1 : scala.Any, arg2 : scala.Any) : scala.Unit = {
    log.log( level, template, arg1, arg2)
  }
  def log(level : akka.event.Logging.LogLevel, template : scala.Predef.String, arg1 : scala.Any, arg2 : scala.Any, arg3 : scala.Any) : scala.Unit = {
    log.log( level, template, arg1, arg2, arg3)
  }
  def log(level : akka.event.Logging.LogLevel, template : scala.Predef.String, arg1 : scala.Any, arg2 : scala.Any, arg3 : scala.Any, arg4 : scala.Any) : scala.Unit = {
    log.log( level, template, arg1, arg2, arg3, arg4)
  }
}
