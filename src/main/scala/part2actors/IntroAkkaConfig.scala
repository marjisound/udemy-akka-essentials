package part2actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object IntroAkkaConfig extends App {

  class SimpleLoggingActor extends Actor with ActorLogging {
    override def receive: Receive = { case message =>
      log.info(message.toString)
    }
  }

  /** 1- inline configuration
    */

  val configString =
    """
      | akka {
      |   loglevel = "ERROR"
      | }
      |""".stripMargin

  val config = ConfigFactory.parseString(configString)
  val system = ActorSystem("ConfigurationDemo", ConfigFactory.load(config))

  val actor = system.actorOf(Props[SimpleLoggingActor])
  actor ! "A message to remember"

  /** 1- config file
    */
  val defaultConfigFileSystem = ActorSystem("DefaultConfigFileDemo")
  val defaultConfigActor =
    defaultConfigFileSystem.actorOf(Props[SimpleLoggingActor])
  defaultConfigActor ! "remember me"

  /** 2- seperate config in the same file
    */
  val specialConfig = ConfigFactory.load().getConfig("mySpecialConfig")
  val specialConfigSystem = ActorSystem("SpecialConfigDemo", specialConfig)
  val specialConfigActor =
    specialConfigSystem.actorOf(Props[SimpleLoggingActor])
  specialConfigActor ! "remember me, I'm special"

  /** 2-
    *
    * seperate config in another file
    */

  val separateConfig =
    ConfigFactory.load("secretFolder/secretConfiguration.conf")

  println(s"Hey ${separateConfig.getString("akka.loglevel")}")

  /** 2-
    *
    * different file formats e.g. JSON, properties
    */

  val jsonConfig = ConfigFactory.load("json/jsonConfig.json")
  println(s"json config: ${jsonConfig.getString("aJsonProperty")}")
  println(s"json config: ${jsonConfig.getString("akka.loglevel")}")

  val propsConfig = ConfigFactory.load("props/propsConfiguration.properties")
  println(s"props config: ${propsConfig.getString("my.simpleProperty")}")
  println(s"props config: ${propsConfig.getString("akka.loglevel")}")

}
