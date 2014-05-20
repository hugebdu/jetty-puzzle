package actors

import akka.actor.{ActorRef, Actor}
import org.eclipse.jetty.websocket.api.RemoteEndpoint

import json.JsonSupport
import actors.GamesRegistryActor.{UnknownInvitation, WaitingForPair}
import model.Turn
import actors.GameActor._
import actors.GameActor.PickedSurprise
import model.Surprise.Challenge
import actors.GameActor.Swap
import actors.GamesRegistryActor.InitGame
import actors.GameActor.GameFinished
import actors.GameActor.Click



class PlayerActor(endpoint: Endpoint) extends Actor with JsonSupport {

  var game: InGame = _

  def challenged: Receive = {
    case JsonMessage(Messages.ChallengeOutcome(picked)) => game.actor ! (if (picked) PickedSurprise(game.turn) else DroppedSurprise(game.turn))
    case CompleteSurprise(swaps) => endpoint ! Messages.ChallengeFinished(swaps); context.become(playing)
  }

  def playing: Receive = {

    case StartGame(shuffle) => endpoint ! Messages.StartGame(shuffle)

    case JsonMessage(Messages.Click(index)) => game.actor ! Click(game.turn, index)

    case GameFinished(winner) => endpoint ! Messages.GameFinished(winner == game.turn)

    case Swap(pieces) => endpoint ! Messages.Swap(pieces)

    case InvalidMove => endpoint ! Messages.InvalidMove()

    case Challenge(kind) =>
      endpoint ! Messages.Challenge(kind, PlayerActor.ChallengeTimeoutInSeconds)
      context.become(challenged)
  }

  def receive: Receive = {

    case UnknownInvitation => endpoint ! Messages.UnknownInvitation()

    case WaitingForPair => endpoint ! Messages.WaitingForPair()

    case InitGame(id, turn, imageUrl, gameActor) =>
      endpoint ! Messages.InitGame(imageUrl)
      game = InGame(turn, gameActor)
      context.become(playing)
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
  val ChallengeTimeoutInSeconds = 4

}

object Messages {

  //TODO: use reflection?
  val MessageTypes = List(
    classOf[Message],
    classOf[Click],
    classOf[WaitingForPair],
    classOf[InitGame],
    classOf[InvalidMove],
    classOf[UnknownInvitation],
    classOf[Challenge],
    classOf[ChallengeOutcome],
    classOf[ChallengeFinished],
    classOf[StartGame],
    classOf[Swap],
    classOf[GameFinished]
  )

  sealed trait Message
  case class Click(index: Int) extends Message
  case class InvalidMove() extends Message
  case class WaitingForPair() extends Message
  case class InitGame(imageUrl: String) extends Message
  case class UnknownInvitation() extends Message
  case class GameFinished(winner: Boolean) extends Message
  case class Swap(indexes: Seq[(Int, Int)]) extends Message
  case class StartGame(shuffles: Seq[(Int, Int)]) extends Message
  case class ChallengeFinished(shuffles: Seq[(Int, Int)]) extends Message
  case class Challenge(kind: String, timeoutInSeconds: Int) extends Message
  case class ChallengeOutcome(picked: Boolean) extends Message
}
