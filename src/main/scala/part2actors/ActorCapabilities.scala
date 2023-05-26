package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {

  class SimpleActor extends Actor {
    //context.self is same as just self
    override def receive: Receive = {
      case "Marjan" => {
        println(s"$self received the message Marjan")
        println("1 sender: " + context.sender())
        context.sender() ! s"googooli googooli $self"
      }
      case message: String => {
        println(s"$self received the message $message")
        println("2 sender: " + context.sender())
      }
      case number: Int =>
        println(s"[simple actor] I have received a number $number")
      case SpecialMessage(contents) =>
        println(s"[simple actor] I have received something SPECIAL: $contents")
      case SendMessageToYourself(content) =>
        self ! content // this is to call self to give it the content
      case SayHiTo(ref) => ref ! "Marjan"
      case WirelessPhoneMessage(message, ref) => {
        println(s"$self received the message $message")
        ref forward (message + "n")
        // keeps the original sender, meaning actor ref
        // is given a message from this actor, but the sender
        // won't be this actor, it will be the sender that
        // gave us the message in the first place
      }
      case WirelessPhoneMessage2(message, ref) => {
        println(s"$self received the message $message")
        ref ! (message + "n")
      }
    }
  }

  val system = ActorSystem("actorCapabilitiesDemo")

  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")

  // 1- messages can be of any type
  // a) messages must be IMMUTABLE
  // B) messages must be SERIALIZABLE (java interface) - often case class or case objects are used for messages

  case class SpecialMessage(contents: String)

  // 2 - actors have information about their context and about their themselves
  // context.self === 'this' in OOP

  case class SendMessageToYourself(content: String)

  // 3 - actors can REPLY to messages
  val alice = system.actorOf(Props[SimpleActor], "alice")
  val bob = system.actorOf(Props[SimpleActor], "bob")

  case class SayHiTo(red: ActorRef)

  // 4 - dead letters
  //alice ! "Marjan" // reply to me

  // 5 - forwarding messages
  case class WirelessPhoneMessage(content: String, ref: ActorRef)
  case class WirelessPhoneMessage2(content: String, ref: ActorRef)

//  simpleActor ! "hello actor"
//  simpleActor ! 42
//  simpleActor ! SpecialMessage("some special message")
//  simpleActor ! SendMessageToYourself("I am an actor and I'm proud of it")
//  alice ! SayHiTo(bob)
//  alice ! "Marjan" // reply to me
  alice ! WirelessPhoneMessage("Marja", bob)
}
