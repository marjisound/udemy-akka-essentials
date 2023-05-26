package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.DurationInt
import scala.util.Random

class TimedAssertions
    extends TestKit(ActorSystem("TimedAssertionsSpec"))
    with ImplicitSender
    with AnyWordSpecLike
    with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import TimedAssertions._

  "A worker actor" should {
    val workerActor = system.actorOf(Props[WorkerActor])

    "reply with the meaning of life in a timely manner" in {
      // asserting that something happens at least within 500 ms up to 1 second
      within(500.millis, 1.second) {
        workerActor ! "work"
        expectMsg(WorkResult(42))
      }
    }

    "reply with valid work at a reasonable cadence" in {
      within(1.second) {
        workerActor ! "workSequence"

        val results: Seq[Int] =
          receiveWhile[Int](max = 2.second, idle = 500.millis, messages = 10) {
            case WorkResult(result) => result
          }

        assert(results.sum > 5)
      }
    }

    "reply to a test probe in a timely manner" in {
      within(1.second) {
        val probe = TestProbe()
        probe.send(workerActor, "work")
        probe.expectMsg(WorkResult(42))
      }

    }
  }
}

object TimedAssertions {

  case class WorkResult(result: Int)

  // testing scenario
  class WorkerActor extends Actor {
    override def receive: Receive = {
      case "work" =>
        // long computation
        Thread.sleep(500)
        sender() ! WorkResult(42)
      case "workSequence" =>
        val r = new Random()
        (1 to 10).foreach { i =>
          Thread.sleep(r.nextInt(50))
          sender() ! WorkResult(1)
        }
    }
  }
}
