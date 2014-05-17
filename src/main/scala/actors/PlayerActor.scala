package actors

import akka.actor.{ActorRef, Actor}
import org.eclipse.jetty.websocket.api.RemoteEndpoint

import json.JsonSupport
import actors.GamesRegistryActor.{InitGame, WaitingForPair}
import model.Turn
import actors.GameActor.{Swap, GameFinished, Click}

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/16/14
 */
class PlayerActor(endpoint: Endpoint) extends Actor with JsonSupport {

  var game: InGame = _

  def playing: Receive = {

    case JsonMessage(Messages.Click(index)) => game.actor ! Click(game.turn, index)

    case GameFinished(winner) => endpoint ! Messages.GameFinished(winner == game.turn)

    case Swap(pieces) => endpoint ! Messages.Swap(pieces)
  }

  def receive: Receive = {

    case WaitingForPair => endpoint ! Messages.WaitingForPair()

    case InitGame(id, turn, imageUrl, gameActor) =>
      endpoint ! Messages.InitGame(imageUrl)
      game = InGame(turn, gameActor)
      context.become(playing)

    case JsonMessage(Messages.Click(index)) => endpoint ! Messages.InvalidMove()
  }

  object JsonMessage {
    import actors.Messages.Message
    def unapply(a: Any): Option[Message] = a match {
      case s: String => s.toMessage
      case _ => None
    }
  }

  case class InGame(turn: Turn, actor: ActorRef)
}

trait Endpoint {
  import actors.Messages.Message
  def send(m: Message): Unit
  def ! (m: Message): Unit = { send(m) }
}

object Endpoint {

  import actors.Messages.Message
  def apply(websocket: RemoteEndpoint): Endpoint = new Endpoint {
    def send(m: Message): Unit = {
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
    classOf[WaitingForPair],
    classOf[InitGame],
    classOf[InvalidMove],
    classOf[Swap],
    classOf[GameFinished]
  )

  sealed trait Message
  case class Click(index: Int) extends Message
  case class InvalidMove() extends Message
  case class WaitingForPair() extends Message
  case class InitGame(imageUrl: String) extends Message
  case class GameFinished(winner: Boolean) extends Message
  case class Swap(indexes: (Int, Int)*) extends Message
}
