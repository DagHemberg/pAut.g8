package adventautomation.plugins

import sbt._
import Keys._
import complete.DefaultParsers._
import scala.util.{Try, Success, Failure}
import adventautomation.utils._
import Logging._
import SettingsManager._
import scala.Console._

object Auth extends AutoPlugin {
  object autoImport {
    val auth = inputKey[Unit]("Sets the session token to communicate with the AoC website")
  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    auth := {
      import sys.process._

      def tryAuth(sessionToken: String) = {
        if (sessionToken == "") {
          error("Please provide a session token")
        } else if (sessionToken == SettingsManager.get("token").getOrElse("")) {
          error("Session token already set to this value")
        } else {
          inf(s"Attempting to authenticate with AoC servers...")
          val regex = "(?<=<div class=\"user\">)(.*)(?= <span)".r
          regex.findFirstIn(SystemCommand.exec(s"curl --silent -A 'Scala AdventOfCode Helper v0.1' --cookie 'session=$sessionToken' 'https://adventofcode.com'")) match {
            case Some(name) => {
              suc(s"Logged in as: ${YELLOW}$name${RESET}")
              SettingsManager.set("token", sessionToken)
              SettingsManager.set("username", name)
              suc(s"Set session token")
            }
            case None => {
              error("Did not set session token (invalid token)")
              error("Please check your token and try again (but don't spam!)")
            }
          }
        }
      }
      
      def resetAuth = {
        SettingsManager.set("token", "")
        SettingsManager.set("username", "")
        inf("Cleared session token")
      }

      def getAuth = {
        SettingsManager.get("token") match {
          case None => {
            warn("No session token set")
            inf("Please run 'auth set <sessionToken>' to set the session token")
          }
          case Some(token) => {
            SettingsManager.get("username") match {
              case None => 
              case Some(name) => inf(s"Logged in as: ${YELLOW}$name${RESET}")
            }
            inf(s"Current session token: $token")
            inf("Run 'auth set <sessionToken>' to set a new token, or run 'auth <clear|reset>' to clear the session token")
          }
        }
      }

      def rec(args: List[String]): Unit = {
        args.toList match {
          case Nil => rec(List("get", ""))
          case cmd :: Nil => rec(args ++ List(""))
          case cmd :: token :: _ => cmd match {
            case "retry" => {
              val t = SettingsManager.get("token").getOrElse("")
              SettingsManager.set("token", "")
              tryAuth(t)
            }
            case "clear" | "reset" => resetAuth
            case "get" | "status" => getAuth
            case "set" => tryAuth(token)
            case _ => error(s"Invalid command: $cmd")
          }
        }
      }

      rec(spaceDelimited("<command> <token>").parsed.toList)

    }
  )
}