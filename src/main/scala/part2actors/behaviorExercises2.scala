package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.behaviorExercises.Counter.{Decrement, Increment}

/** 1- recreate the counter Actor with context.become and No Mutable State
  * 2- simplified voting system
  */
object behaviorExercises2 extends App {
  val system = ActorSystem("myActorSystem")

  /** Exercise 2
    */
  case class Vote(candidate: String)
  case object VoteStatusRequest
  case class VoteStatusReply(candidate: Option[String])
  class Citizen extends Actor {
    override def receive: Receive = {
      case Vote(candidate) =>
        println(s"${self} voted for $candidate")
        context.become(VoteState(candidate))
      case VoteStatusRequest =>
        println(s"vote reply none is returned for citizen $self")
        sender() ! VoteStatusReply(None)
    }

    def VoteState(candidate: String): Receive = {
      case Vote(_) => println(s"${self} already voted")
      case VoteStatusRequest =>
        println(s"vote reply ${candidate} is returned for citizen $self")
        sender() ! VoteStatusReply(Some(candidate))
    }

  }

  case class AggregateVotes(citizens: Set[ActorRef])
  case object Print
  class VoteAggregator extends Actor {

    override def receive: Receive = {
      case AggregateVotes(citizens) => {
        context.become(voteReplyReceive(Map(), citizens.size))
        citizens.map(_ ! VoteStatusRequest)
      }
    }

    def voteReplyReceive(candidates: Map[String, Int], count: Int): Receive = {
      case VoteStatusReply(Some(candidate)) =>
        val newVoteNumber = candidates.getOrElse(candidate, 0) + 1
        context.become(
          voteReplyReceive(
            candidates + (candidate -> newVoteNumber),
            count - 1
          )
        )
        if (count == 1) self ! Print

      case None =>
        context.become(voteReplyReceive(candidates, count - 1))
        if (count == 1) self ! Print

      case Print =>
        candidates.foreach(c => println(s"${c._1} -> ${c._2}"))
    }

  }

  val alice = system.actorOf(Props[Citizen], "alice")
  val bob = system.actorOf(Props[Citizen], "bob")
  val charlie = system.actorOf(Props[Citizen], "charlie")
  val daniel = system.actorOf(Props[Citizen], "daniel")

  //alice ! Vote("Martin")
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
