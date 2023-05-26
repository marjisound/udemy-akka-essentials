package part2actors

import akka.actor.{Actor, ActorPath, ActorRef, ActorSystem, Props}
import part2actors.ChildActorExercise.Runner.Run
import part2actors.ChildActorExercise.WordCounterMaster.{
  Initialize,
  WordCountReply,
  WordCountTask
}
import part2actors.ChildActors.Child

object ChildActorExercise extends App {

  // Distributed Word Counting
  object WordCounterMaster {
    case class Initialize(nChildren: Int)
    case class WordCountTask(text: String, path: ActorPath)
    case class WordCountReply(count: Int, text: String, path: ActorPath)
  }
  class WordCounterMaster extends Actor {
    override def receive: Receive = {
      case Initialize(nChildren) => {
        println(s"${self.path} initializing ${nChildren} WordCounterWorker")
        val workers = (1 to nChildren).toList.map { index =>
          context.actorOf(Props[WordCounterWorker], s"worker-$index")
        }

        val workersStatus = workers.map(worker => (worker.path, true)).toMap
        context.become(initialized(workers, 0))
      }
    }

    def initialized(
        workers: List[ActorRef],
        currentIndex: Int
    ): Receive = {
      case message: String => {
        println(s"worker ${workers(currentIndex).path} is to be used")
        val newIndex = (currentIndex + 1) % workers.length

        context.become(initialized(workers, newIndex))
        workers(currentIndex) forward WordCountTask(message, self.path)

      }

      case WordCountReply(count, text, path) => {
        //println(s"worker ${workers(currentIndex).path} is to be freed")
        val res = s"count for \'${text}\' is ${count}"
        sender() ! res
      }
    }

    def printWorkerStatus(workerStatus: Map[ActorPath, Boolean]) = {
      println("***** workers status")
      workerStatus.foreach(worker => println(s"${worker._1} => ${worker._2}"))
      println("***** end of workers status")
    }
  }

  object WordCounterWorker {}
  class WordCounterWorker extends Actor {
    override def receive: Receive = {
      case WordCountTask(text, path) => {
        context
          .actorSelection(path) forward
          WordCountReply(
            text.length,
            text,
            self.path
          )
      }
    }
  }

  object Runner {
    case class Run(master: ActorRef)
  }
  class Runner extends Actor {
    override def receive: Receive = {
      case Run(actorRef) => {
        actorRef ! Initialize(5)
        Thread.sleep(500)
        1 to 7 foreach (index => actorRef ! s"Akka ${index} is awesome")
      }
      case message: String => {
        println(s"result from ${sender().path} is ${message}")
      }
    }
  }

  val system = ActorSystem("WordCountSystem")
  val master = system.actorOf(Props[WordCounterMaster], "WordCounterMaster")
  val runner = system.actorOf(Props[Runner], "runner")
  runner ! Run(master)

  /*
  create WordCounterMaster
  send Initialize(10) to wordCounterMaster
  send "Akka is awesome" to WordCounterMaster
    wcm will send a WordCountTask("...")
      child replies with a WordCountReply(3) bto the master
    master replies with 3 to the sender

    requester -> wcm -> wcw

   */

  // round robin logic
  // 1, 2, 3, 4, 5 children and 7 tasks
  // 1, 2, 3, 4, 5, 1, 2
}
