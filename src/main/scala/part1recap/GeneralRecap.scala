package part1recap

import scala.util.Try

object GeneralRecap extends App {

  val aCondition: Boolean = false

  var aVariable = 42
  aVariable += 3

  // expressions
  val aConditionedVal = if (aCondition) 42 else 65

  // code block
  val aCodeBlock = {
    if (aCondition) 74
    56
  }

  // types
  //unit
  val theUnit = println("hello scala")

  def aFunction(x: Int) = x + 1

  // recursion - tail recursion
  def factorial(n: Int, acc: Int): Int = {
    if (n <= 0) acc
    else factorial(n - 1, acc * n)
  }

  // OOP
  class Animal

  class Dog extends Animal

  val aDog: Animal = new Dog

  trait Carnivore {
    def eat(a: Animal): Unit
  }

  class Crocodile extends Animal with Carnivore {
    override def eat(a: Animal): Unit = println("crunch")
  }

  // method notations
  val aCroc = new Crocodile
  aCroc.eat(aDog)
  aCroc eat aDog

  // anonymous class
  val aCarnivore = new Carnivore {
    override def eat(a: Animal): Unit = println("roar")
  }

  aCarnivore eat aDog

  // generics
  abstract class MyList[+A]

  // companion objects
  object MyList

  // case classes
  case class Person(name: String, age: Int)

  // Exceptions
  val aPotentialFailure =
    try {
      throw new RuntimeException("I'm innocent")
    } catch {
      case e: Exception => "I caught an exception"
    } finally {
      // side effect
      println("some logs")
    }

  // functional programming
  // functions are objects. We see it as just a function
  // but under the hood it's an instance of a class
  val incrementor = new Function1[Int, Int] {
    override def apply(v1: Int): Int = v1 + 1
  }

  // incrementor.apply(3)
  val incremented = incrementor(3)

  // this translates to the Function1 instance
  // Int => Int === Function1[Int, Int]
  val anonymousIncrementor = (x: Int) => x + 1

  // FP is all about working with functions as first class

  List(1, 2, 3).map(incrementor)

  // for comprehensions
  // List(1, 2, 3, 4).flatMap(num => List('a', 'b', 'c', 'd').map(char => num + "-" + char))
  val pairs = for {
    num <- List(1, 2, 3, 4)
    char <- List('a', 'b', 'c', 'd')
  } yield num + "-" + char

  // Seq, Array, List, Victor, Map, Tuples, Sets

  // "collections"
  // Option and Try
  val anOption = Some(1)
  val aTry = Try {
    throw new RuntimeException("some error")
  }

  // pattern matching
  val unknown = 2
  val order = unknown match {
    case 1 => "first"
    case 2 => "second"
    case _ => "unknown"
  }

  val bob = Person("Bob", 2)
  val greeting = bob match {
    case Person(name, _) => s"Hi, my name is ${name}"
    case _               => "I don't know my name"
  }
}
