package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorsIntro extends App {
  // part1 - actor system
  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)

  // part2 - create actors within actor system
  // word count actor

  class WordCountActor extends Actor {
    // internal data
    var totalWords = 0

    // behaviour
    // type Receive is alias for PartialFunction[Any, Unit]
    def receive: Receive = {
      case message: String =>
        totalWords += message.split(" ").length
        println(s"[word counter] I have received: $message and total words is $totalWords")
      case msg => println(s"[word counter] I cannot understand ${msg.toString}")
    }
  }

  // part3 - instantiate our actor
  val wordCounter: ActorRef = actorSystem.actorOf(Props[WordCountActor], "wordCounter")
  val anotherWordCounter = actorSystem.actorOf(Props[WordCountActor], "anotherWordCounter")

  // part4 - communicate with actor
  wordCounter ! "I am learning Akka and it's really cool" // ! known as "tell"
  wordCounter ! "I am continuing" // total words is added to the previous call
  anotherWordCounter ! "A different message" // total words is refreshed as it's a new instance
  // sending a message is completely asynchronous
  // so the order of println can change every time you run the app

  class Person(name: String) extends Actor {
    override def receive: Receive = {
      case "Hi" => println(s"Hi, my name is $name")
      case msg => println(s"unknown message ${msg}")
    }
  }

  // one way to pass parameters for the Person constructor
  val person = actorSystem.actorOf(Props(new Person("Bob")), "personActor")
  person ! "Hi"
  person !  "hello" //fails

  // better way is to create a companion object
  object Person {
    def props(name: String) = Props(new Person(name))
  }

  val person2 = actorSystem.actorOf(Person.props("Marjan"))
  person2 ! "Hi"

}
