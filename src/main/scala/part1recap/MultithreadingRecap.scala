package part1recap

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object MultithreadingRecap extends App {

  // creating threads on the JVM

  val aThread = new Thread(new Runnable {
    override def run(): Unit = println("I'm running in parallel")
  })

  // This is the same as above - more practical this way
  val aThread2 = new Thread(() => println("I'm running in parallel"))

  aThread.start() // start a thread
  aThread.join() // wait for a thread to finish

  val threadHello = new Thread(() => (1 to 1000).foreach(_ => println("Hello")))
  val threadGoodbye = new Thread(() => (1 to 1000).foreach(_ => println("goodbye")))

  threadHello.start()
  threadGoodbye.start()

  // problem with threads
  // different runs produce different results

  class BankAccount(@volatile private var amount: Int) {
    override def toString: String = "" + amount

    def withdraw(money: Int) = this.amount -= money

    // this is thread safe
    // either do this or add @volatile to the var
    def safeWithdraw(money: Int) = this.synchronized {
      this.amount -= money
    }
  }

  /*
  withdraw is not safe, because 2 threads could be calling it
  BA (1000)

  T1 -> withdraw 1000
  T2 -> withdraw 2000

  T1 -> this.amount = this.amount - ... // preempted BY THE OS
  T2 -> this.amount = this.amount - 2000 = 8000
  T1 -> -1000 = 9000

  => result = 9000

  this.amount = this.amount - 1000 is NOT ATOMIC (thread safe)
   */

  // inter-thread communication on the JVM
  // wait - notify mechanism
  // scala Futures
  import scala.concurrent.ExecutionContext.Implicits.global
  val future = Future {
    // long computation - evaluated on a different thread
    42
  }

  future.onComplete {
    case Success(_) => println("I found the meaning oof life")
    case Failure(_) => println("something happened with the meaning of life")
  }

  val aProcessedFuture = future.map(_ + 1) // Future with 43
  val aFlatFuture: Future[Int] = future.flatMap {
    value =>
      Future(value + 1)
  } // Future with 44

  val filteredFuture: Future[Int] = future.filter(_ % 2 == 0) // fail with NoSuchElementException

  // for comprehension
  val aNonsenseFuture = for {
    meaningOfLife <- future
    filteredMeaning <- filteredFuture
  } yield meaningOfLife + filteredMeaning

  // aneThen, recover/recoverWith

  // Promises
}
