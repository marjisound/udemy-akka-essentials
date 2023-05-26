package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.behaviorExercises.Counter.{Decrement, Increment, Print}

/** 1- recreate the counter Actor with context.become and No Mutable State
  * 2- simplified voting system
  */
object behaviorExercises extends App {

  val system = ActorSystem("myActorSystem")

  class Counter extends Actor {

    override def receive: Receive = countReceive(0)

    def countReceive(count: Int): Receive = {
      case Increment => context.become(countReceive(count + 1))
      case Decrement => context.become(countReceive(count - 1))
      case Print     => println(s"$self Current count is $count")
    }
  }

  object Counter {
    case object Increment

    case object Decrement

    case object Print
  }

  import Counter._

//  val counterActor = system.actorOf(Props[Counter], "counterActor")
//  (1 to 5).foreach(_ => counterActor ! Increment)
//  (1 to 10).foreach(_ => counterActor ! Decrement)
//  counterActor ! Print

  /** Exercise 2
    */
  case class Vote(candidate: String)
  case object VoteStatusRequest
  case class VoteStatusReply(candidate: Option[String])
  class Citizen extends Actor {
    override def receive: Receive = voteReceive

    def voteReceive: Receive = {
      case Vote(person)      => context.become(votedState(person))
      case VoteStatusRequest => sender() ! None
    }

    def votedState(candidate: String): Receive = {
      case Vote              => println(s"$self has already voted") // nothing to do
      case VoteStatusRequest => sender() ! VoteStatusReply(Some(candidate))
    }
  }

  case class AggregateVotes(citizens: Set[ActorRef])
  case object Print

  class VoteAggregator extends Actor {
    override def receive: Receive =
      voteAggregatorReceive(Map[String, Int](), Set[ActorRef]())

    def voteAggregatorReceive(
        current: Map[String, Int],
        stillWaiting: Set[ActorRef]
    ): Receive = {
      case AggregateVotes(citizens) =>
        context.become(voteAggregatorReceive(current, citizens))
        citizens.foreach { ci =>
          ci ! VoteStatusRequest
        }
      case VoteStatusReply(maybeCandidate) =>
        maybeCandidate.map { candidate =>
          val voteNumber = current.getOrElse(candidate, 0)
          val newList = current + (candidate -> (voteNumber + 1))
          val newStillWaiting =
            stillWaiting.filter(a => a.path != sender().path)
          context.become(voteAggregatorReceive(newList, newStillWaiting))
          if (newStillWaiting.isEmpty) self ! Print
        }
      case Print => {
        current.foreach(c => println(s"candidate: ${c._1}, votes: ${c._2}"))
      }
    }
  }

  val alice = system.actorOf(Props[Citizen])
  val bob = system.actorOf(Props[Citizen])
  val charlie = system.actorOf(Props[Citizen])
  val daniel = system.actorOf(Props[Citizen])

  alice ! Vote("Martin")
  bob ! Vote("Jonas")
  charlie ! Vote("Roland")
  daniel ! Vote("Roland")

  val voteAggregator = system.actorOf(Props[VoteAggregator])
  voteAggregator ! AggregateVotes(Set(alice, bob, charlie, daniel))

  /** print the status of the votes
    * Martin -> 1
    * Jonas -> 1
    * Roland -> 2
    */

}
