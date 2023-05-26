package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.Exercises.BankAccount.{Deposit, Statement, Withdraw}
import part2actors.Exercises.Person.LiveTheLife

import scala.util.{Failure, Success}

object Exercises extends App {

  /** 1. a Counter actor
    * - Increment
    * - Decrement
    * - Print
    */

  val system = ActorSystem("myActorSystem")

  class Counter extends Actor {
    import Counter._
    var count = 0

    override def receive: Receive = {
      case Increment => count += 1
      case Decrement => count -= 1
      case Print     => println(s"$self Current count is $count")
    }
  }

// domain of the counter
  object Counter {
    case object Increment
    case object Decrement
    case object Print
  }

  import Counter._
  val counterActor = system.actorOf(Props[Counter], "counterActor")

//  counterActor ! Decrement
//  counterActor ! Increment
//  counterActor ! Increment
//  counterActor ! Print

  (1 to 3).foreach(_ => counterActor ! Increment)
  (1 to 5).foreach(_ => counterActor ! Decrement)
  counterActor ! Print

  /** 2. a Bank account as an actor
    * receives
    * - deposit an amount
    * - withdraw an amount
    * - Statement
    *  replies with
    * - Success
    * - Failure
    *
    *  interact with some other kind of actor
    */

  class BankAccount extends Actor {
    import BankAccount._
    var balance = 0
    override def receive: Receive = {
      case Deposit(amount) =>
        if (amount < 0)
          sender() ! TransactionFailure(s"invalid deposit amount: $amount")
        else {
          balance += amount
          sender() ! TransactionSuccess(
            s"Successfully deposited $amount, new balance is $balance"
          )
        }

      case Withdraw(amount) =>
        if (balance < amount)
          sender() ! TransactionFailure(
            s"balance $balance is not enough for $amount withdrawal"
          )
        else if (amount < 0)
          sender() ! TransactionFailure(s"invalid withdrawal amount: $amount")
        else {
          balance -= amount
          sender() ! TransactionSuccess(
            s"Successfully withdrawn $amount, new balance is $balance"
          )
        }

      case Statement =>
        sender() ! s"balance is $balance"
    }
  }

  object BankAccount {
    case class Deposit(amount: Int)
    case class Withdraw(amount: Int)

    case object Statement

    case class TransactionSuccess(message: String)
    case class TransactionFailure(message: String)
  }

  object Person {
    case class LiveTheLife(account: ActorRef)
  }

  class Person extends Actor {
    import Person._
    override def receive: Receive = {
      case LiveTheLife(account) =>
        account ! Deposit(10000)
        account ! Withdraw(90000)
        account ! Withdraw(500)
        account ! Statement
      case message => println(message.toString)
    }
  }

//  val bankAccount = system.actorOf(Props[BankAccount], "bankAccount")
//  val person = system.actorOf(Props[Person], "person")
//
//  person ! LiveTheLife(bankAccount)

}
