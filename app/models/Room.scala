package models

import akka.actor._
import akka.util.duration._

import play.api.libs.iteratee._
import play.api.libs.concurrent._

import akka.util.Timeout
import akka.pattern.ask

import play.api.Play.current

case class Join(id: Int)
case class Connected(enumerator: Enumerator[String])
case class Quit(id: Int)
case class Message(text: String)

class Room extends Actor {

  var members = Map.empty[Int, PushEnumerator[String]]

  def receive = {
    case Join(id) => {
      val channel = Enumerator.imperative[String]()
      members += (id -> channel)
      sender ! Connected(channel)
    }
    case Message(text) => {
      // It's like `outbound.send(text)` in Play 1
      members.foreach(_._2.push(text))
    }
    case Quit(id) => {
      members -= id
    }
  }
}

object Room {

  var joined = 0

  lazy val defaultRoom = Akka.system.actorOf(Props[Room])
  
  implicit val timeout = Timeout(1 second)

  def join(): Promise[(Iteratee[String, _], Enumerator[String])] = {
    joined += 1
    val id = joined
    (defaultRoom ? (Join(id), 1 second)).asPromise.map {
      case Connected(enumerator) =>
        val iteratee = Iteratee.foreach[String] { text =>
          defaultRoom ! Message(text)
        }.mapDone { _ =>
          defaultRoom ! Quit(id)
        }
        (iteratee, enumerator)
      case _ =>
        val iteratee = Done[String, Unit]((), Input.EOF)
        val enumerator = Enumerator[String]("error").andThen(Enumerator.enumInput(Input.EOF))
        (iteratee, enumerator)
    }

  }

  def publish(message: String) {
    defaultRoom ! Message(message)
  }
}

