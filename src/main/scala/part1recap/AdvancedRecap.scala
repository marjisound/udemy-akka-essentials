package part1recap

import scala.concurrent.Future

object AdvancedRecap extends App {

  // partial functions
  val partialFunction: PartialFunction[Int, Int] = {
    case 1 => 42
    case 2 => 65
    case 5 => 999
  }

  println(partialFunction.isDefinedAt(8))

  val pf = (x: Int) =>
    x match {
      case 1 => 42
      case 2 => 65
      case 5 => 999
    }

  val function: (Int => Int) = partialFunction

  val modifiedList = List(1, 2, 3).map {
    case 1 => 42
    case _ => 0
  }

  println(modifiedList)

  println(partialFunction(2))

  // lifting
  val lifted = partialFunction.lift // total function Int => Option[Int]
  lifted(2)

  val res: Int = partialFunction(2)
  println(lifted(2))

  // chaining partial functions
  val pfChain = partialFunction.orElse[Int, Int] { case 60 =>
    9000
  }

  // type alias
  type ReceiveFunction = PartialFunction[Any, Unit]

  def receive: ReceiveFunction = {
    case 1 => println("Hello")
    case _ => println("confused...")
  }

  // implicits
  implicit val timeout = 3000
  def setTimeout(f: () => Unit)(implicit timeout: Int) = f()

  setTimeout(() => println("timeout")) // omit extra parameter

  // implicit conversions

  // 1) implicit defs
  case class Person(name: String) {
    def greet = s"Hi, my name is ${name}"
  }

  implicit def fromStringToPerson(name: String): Person = Person(name)
  // fromStringToPerson("Peter").greet
  val peterGreet = "Peter".greet
  println(peterGreet)

  // 2) implicit classes
  implicit class Dog(name: String) {
    def bark = println("bark!")
  }

  // new Dog("Lassie").bark - automatically done by compiler
  "Lassie".bark

  // organise

  // implicits are fetched by compiler in this order:
  // 1- local scope
  implicit val inverseOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _)
  val sorted = List(1, 2, 3).sorted
  println(sorted)

  // 2- imported scope
  import scala.concurrent.ExecutionContext.Implicits.global
  val future = Future {
    println("hello future")
  }

  // 3- companion objects of the types included in the call
  object Person {
    implicit val personOrdering: Ordering[Person] =
      Ordering.fromLessThan((a, b) => a.name.compareTo(b.name) < 0)
  }

  // implicit value of personOrdering is fetched by compiler
  List(Person("Marjan"), Person("Ehsan")).sorted
}
