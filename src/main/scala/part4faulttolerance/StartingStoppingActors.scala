package part4faulttolerance

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import part4faulttolerance.StartingStoppingActors.Parent.StartChild

object StartingStoppingActors extends App {
  val system = ActorSystem("StoppingActorsDemo")

  object Parent {
    case class StartChild(name: String)
    case class StopChild(name: String)
    case object Stop
  }
  class Parent extends Actor with ActorLogging {
    override def receive: Receive = withChildren(Map())
    def withChildren(children: Map[String, ActorRef]): Receive = {
      case StartChild(name) =>
        log.info(s"starting child ${name}")
        context.become(
          withChildren(children + (name -> context.actorOf(Props[Child], name)))
        )
    }
  }

  class Child extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => {
        println("helloooo")
        log.info(message.toString)
      }
    }
  }
  val parent = system.actorOf(Props[Parent], "parent")
  parent ! StartChild("child1")

  val test = system.child("child1")
  val child = system.actorSelection(test)
  println(child.pathString)
  child ! "hi kid"
}
