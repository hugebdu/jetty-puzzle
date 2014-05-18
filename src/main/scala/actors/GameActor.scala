package actors

import akka.actor.{ActorRef, Actor}
import model._
import collection.mutable
import actors.GameActor.CompleteSurprise

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/16/14
 */
class GameActor(surprises: SurpriseProducer) extends Actor {

  import GameActor._
  import context._

  var left: Player = _
  var right: Player = _
  var surprise: SurpriseInProgress = _
  
  def completeSurprise(swaps: (CompleteSurprise, CompleteSurprise)): Unit = {
    left.actor ! swaps._1
    right.actor ! swaps._2
    surprise = null
    become(playing)
  }

  def surprised: Receive = {
    case PickedSurprise(turn) => surprise(turn -> Surprise.Pick) foreach completeSurprise
    case DroppedSurprise(turn) => surprise(turn -> Surprise.Drop) foreach completeSurprise
  }

  def playing: Receive = {

    case CheckForSurprises =>
      for (s <- surprises.maybeSurprise(left.board, right.board)) {
        surprise = SurpriseInProgress(s)
        left.actor ! s.challenge()
        right.actor ! s.challenge()
        become(surprised)
      }

    case Click(turn, index) =>
      val board = boardFor(turn)
      board.click(index).fold[Unit](sender ! InvalidMove) { x =>
        if (board.isCompleted) {
          left.actor ! GameFinished(turn)
          right.actor ! GameFinished(turn)
          stop(self)
        } else sender ! Swap(Seq(x))
      }
  }

  def receive: Receive = {

    case Init(l, r) =>
      left = l; right = r
      left.actor ! StartGame(Turn.Left, left.board.cells)
      right.actor ! StartGame(Turn.Right, right.board.cells)
      become(playing)
  }

  private def boardFor(turn: Turn) = turn match {
    case Turn.Left => left.board
    case Turn.Right => right.board
  }
}

case class Player(actor: ActorRef, board: Board)

case class SurpriseInProgress(private val surprise: Surprise, private val actions: mutable.Map[Turn, Surprise.Answer] = mutable.Map.empty) {

  def isCompleted = Turn.all forall actions.isDefinedAt

  def apply(outcome: (Turn, Surprise.Answer)): Option[(CompleteSurprise, CompleteSurprise)] = {
    actions += outcome
    if (isCompleted) {
      Some(surprise.handle(actions(Turn.Left), actions(Turn.Right)))
    } else None
  }
}

object GameActor {

  case class Init(left: Player, right: Player)
  case class StartGame(turn: Turn, shuffle: Seq[Cell])
  case class Click(turn: Turn, index: Int)
  case object InvalidMove
  case class Swap(indexes: Seq[(Int, Int)])
  case class CompleteSurprise(swaps: Seq[(Int, Int)])
  case class GameFinished(winner: Turn)
  case object CheckForSurprises

  sealed trait SurpriseAction { def turn: Turn }
  case class PickedSurprise(turn: Turn)
  case class DroppedSurprise(turn: Turn)
}