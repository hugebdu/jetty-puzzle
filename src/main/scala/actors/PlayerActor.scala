package actors

import akka.actor.Actor
import org.eclipse.jetty.websocket.api.RemoteEndpoint
import actors.Messages.{InvalidMove, Click, Message}
import json.JsonSupport

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/16/14
 */
class PlayerActor(endpoint: RemoteEndpoint) extends Actor with JsonSupport {

  type MessageHandler = PartialFunction[Message, Unit]

  def onMessage: MessageHandler = {
    case Click(index) => endpoint ! InvalidMove()
  }

  def receive: Receive = {
    case "hello" => endpoint.sendString("world")
    case JsonMessage(msg) if onMessage isDefinedAt msg => onMessage(msg)
  }

  object JsonMessage {
    def unapply(a: Any): Option[Message] = a match {
      case s: String => s.toMessage
      case _ => None
    }
  }
}

object Messages {

  //TODO: use reflection?
  val MessageTypes = List(
    classOf[Message],
    classOf[Click],
    classOf[InvalidMove]
  )

  sealed trait Message
  case class Click(index: Int) extends Message
  case class InvalidMove() extends Message
}
