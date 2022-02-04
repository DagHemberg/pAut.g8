package adventautomation.utils

import sbt._
import Keys._
import scala.Console._

object Logging {
  def inf(msg: String) = println(s"[info] $msg")
  def error(msg: String) = println(s"[${RED}error${RESET}] $msg")
  def warn(msg: String) = println(s"[${YELLOW}warn${RESET}] $msg")
  def suc(msg: String) = println(s"[${GREEN}success${RESET}] $msg")
}