package part1recap

import scala.concurrent.Future

object ThreadModelLimitations extends App {
  /*
  Daniel's rants
   */

  /**
   * #1: OOP encapsulation is only valid in a single threaded model
   */

  class BankAccount(private var amount: Int) {
    override def toString: String = "" + amount

    def withdraw(money: Int) = this.amount -= money
    def deposit(money: Int) = this.amount += money
    def getAmount = amount
  }

//  val account = new BankAccount(2000)
//  for (_ <- 1 to 1000) {
//    new Thread(() => account.withdraw(1)).start()
//  }
//
//  for (_ <- 1 to 1000) {
//    new Thread(() => account.deposit(1)).start()
//  }
//
//  println(account.getAmount)

  /**
   * #2: delegating something to a thread is a pain
   */

  // you have a running thread and you want to pass a runnable to that thread
  var task: Runnable = null

  val runningThread: Thread = new Thread(() => {
    while (true) {
      while (task == null) {
        println("hello")
        runningThread.synchronized {
          println("[background] waiting for a task...")
          runningThread.wait()
        }
      }

      task.synchronized {
        println("[background] I have a task")
        task.run()
        task = null
      }
    }
  })

  def delegateToBackgroundThread(r: Runnable) = {
    if (task == null) task = r


    runningThread.synchronized {
      println("Hello 2")
      runningThread.notify()
    }
  }

//  runningThread.start()
//  Thread.sleep(1000)
//  delegateToBackgroundThread(() => println(42))
//  Thread.sleep(5000)
//  delegateToBackgroundThread(() => println("this should run in the background"))


  /**
   *  #3: dealing with errors in a multi threaded env is a pain in the neck
   */

  // 1M numbers in between 10 threads
  import scala.concurrent.ExecutionContext.Implicits.global
  val futures = (1 to 10)
    .map(i => 10000 * i until 100000 * (i + 1)) // 0 - 100,000, 100,000 - 200,000
    .map(range => Future {
      if (range contains(546735)) throw new RuntimeException("invalid number")
      range.sum
    })

  val sumFuture: Future[Int] = Future.reduceLeft(futures)(_ + _) // future with the sum of all the numbers

  sumFuture.onComplete(println)

}
