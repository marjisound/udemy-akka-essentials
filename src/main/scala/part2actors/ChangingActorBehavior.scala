package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChangingActorBehavior.FussyKid.{
  HAPPY,
  KidAccept,
  KidReject,
  SAD
}
import part2actors.ChangingActorBehavior.Mom.{
  Ask,
  CHOCOLATE,
  Food,
  MomStart,
  VEGETABLE
}

object ChangingActorBehavior extends App {

  object FussyKid {
    case object KidAccept
    case object KidReject
    val HAPPY = "happy"
    val SAD = "sad"
  }
  class FussyKid extends Actor {
    // internal state of the kid
    var state = HAPPY
    override def receive: Receive = {
      case Food(VEGETABLE) => state = SAD
      case Food(CHOCOLATE) => state = HAPPY
      case Ask(_) =>
        if (state == HAPPY) sender() ! KidAccept
        else sender() ! KidReject
    }
  }

  class StatelessFussyKid extends Actor {
    override def receive: Receive = happyReceive

    def happyReceive: Receive = {
      case Food(VEGETABLE) => {
        println("sad on top of stack")
        context.become(sadReceive, false)
        // change my receive handler to sad receive
      }
      case Food(CHOCOLATE) => {
        println("happy stays unchanged")
      }
      case Ask(_) => sender() ! KidAccept
    }
    def sadReceive: Receive = {
      case Food(VEGETABLE) => {
        println("another sad on top of stack")
        context.become(sadReceive, false) // stay sad
      }
      case Food(CHOCOLATE) => {
        println("one sad removed from stack")
        context.unbecome()
      }
      case Ask(_) => sender() ! KidReject
    }
  }

  object Mom {
    case class Food(food: String)
    case class Ask(message: String) // do you want to play?
    case class MomStart(kidRef: ActorRef)
    val VEGETABLE = "veggies"
    val CHOCOLATE = "chocolate"
  }
  class Mom extends Actor {
    override def receive: Receive = {
      case MomStart(kidRef) =>
        // test our interaction
        kidRef ! Food(VEGETABLE)
        kidRef ! Food(VEGETABLE)
        kidRef ! Food(CHOCOLATE)
        kidRef ! Food(VEGETABLE)
        kidRef ! Food(CHOCOLATE)
        kidRef ! Food(CHOCOLATE)
        kidRef ! Food(CHOCOLATE)
        kidRef ! Ask("do you want to play?")
      case KidAccept => println("Yay, my kid is happy")
      case KidReject => println("My kid is sad, but at least he's healthy!")
    }
  }

  val system = ActorSystem("changingActorBehaviorDemo")
  val fussyKid = system.actorOf(Props[FussyKid])
  val statelessFussyKid = system.actorOf(Props[StatelessFussyKid])

  val mom = system.actorOf(Props[Mom])

  //mom ! MomStart(fussyKid)
  mom ! MomStart(statelessFussyKid)
}
