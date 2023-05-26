package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChildActors.CreditCard.AttachToAccount
import part2actors.ChildActors.NaiveBankAccount.InitializeAccount
import part2actors.ChildActors.Parent.{CreateActor, TellChild}
import part2actors.Exercises.BankAccount.Deposit

object ChildActors extends App {

  object Parent {
    case class CreateActor(name: String)
    case class TellChild(message: String)
  }
  class Parent extends Actor {
    import Parent._

    override def receive: Receive = {
      case CreateActor(name) =>
        println(s"${self.path} creating child: ${name}")
        // create an actor here
        val childRef = context.actorOf(Props[Child], name)
        context.become(receiveWithChild(childRef))
      case TellChild(message) =>
        println(s"Child doesn't exist to recieve message: ${message}")
    }

    def receiveWithChild(child: ActorRef): Receive = {
      case CreateActor(name) =>
        println(s"${self.path} creating child: ${name}")
        // create an actor here
        val childRef = context.actorOf(Props[Child], name)
        context.become(receiveWithChild(childRef))
      case TellChild(message) =>
        child ! message
    }
  }

  class Child extends Actor {
    override def receive: Receive = { case message =>
      println(s"${self.path} I got: $message")
    //println(s"sender is ${context.sender()}")
    }
  }

  val system = ActorSystem("ParentChildActor")
  val parent = system.actorOf(Props[Parent], "parent")
  parent ! CreateActor("Marji")
  parent ! TellChild("Hey kid!")

  // actor hierarchies
  // parent -> child -> grandchild
  //        -> child2 ->
  /*
  Guardian actors
  - /system = system guardian
  - /user = user-level guardian (system.actorOf)
  - / the root guardian
   */

  /** Actor selection
    */

  val childSelection = system.actorSelection("/user/parent/Marji")
  childSelection ! "I found you"

  /** DANGER!
    *
    * NEVER PASS MUTABLE ACTOR STATE, OR THE `THIS`REFERENCE TO CHILD ACTORS
    *
    * NEVER IN YOUR LIFE.
    */

  object NaiveBankAccount {
    case class Deposit(amount: Int)
    case class Withdraw(amount: Int)
    case object InitializeAccount
  }
  class NaiveBankAccount extends Actor {
    var amount = 0
    override def receive: Receive = {
      case InitializeAccount =>
        val creditCardRef = context.actorOf(Props[CreditCard])
        creditCardRef ! AttachToAccount(this)
      case Deposit(funds) =>
    }
  }

  object CreditCard {
    case class AttachToAccount(account: NaiveBankAccount)
    case object CheckStatus
  }
  class CreditCard extends Actor {
    override def receive: Receive = ???
  }
}
