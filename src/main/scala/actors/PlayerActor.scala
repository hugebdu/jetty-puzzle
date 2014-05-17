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
class PlayerActor(endpoint: Endpoint) extends Actor with JsonSupport {

  def receive: Receive = {
    case JsonMessage(Click(index)) => endpoint ! InvalidMove()
  }

  object JsonMessage {
    def unapply(a: Any): Option[Message] = a match {
      case s: String => s.toMessage
      case _ => None
    }
  }
}

trait Endpoint {
  def ! (m: Message): Unit
}

object Endpoint {

  def apply(websocket: RemoteEndpoint): Endpoint = new Endpoint {
    override def !(m: Message): Unit = {
      websocket.sendString(m.toJSONString)
    }
  }
}

object PlayerActor {


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
