package model

import model.Surprise.{Answer, Challenge}
import actors.GameActor.{CompleteSurprise, Swap}

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/16/14
 */

trait SurpriseProducer {
  def maybeSurprise(boards: (Board, Board)): Option[Surprise]
}

object DefaultSurpriseProducer extends SurpriseProducer {

  def maybeSurprise(boards: (Board, Board)): Option[Surprise] = {
    ???
  }
}

trait Surprise {

  def challenge(): Challenge = Challenge(kind)
  def handle(outcomes: (Answer, Answer)): (CompleteSurprise, CompleteSurprise)
  def isEligible(boards: (Board, Board)): Boolean

  protected def kind: String
}

case class PrisonersDilemma(config: PrisonersDilemma.Config = PrisonersDilemma.Config()) extends Surprise {

  def isEligible(boards: (Board, Board)): Boolean = {
    ???
  }

  def handle(outcomes: (Answer, Answer)): (CompleteSurprise, CompleteSurprise) = {
    ???
  }

  protected val kind = "prisoners"
}

object PrisonersDilemma {
  case class Config(cooperation: (Int, Int) = (-1, -1), defection: (Int, Int) = (-2, -2), cooperationVsDefection: (Int, Int) = (0, -3))
}

private[model] trait Tossing {

  def toss(board: Board, count: Int): Seq[(Int, Int)] = {
    ???
  }
}

object Surprise {
  
  case class Challenge(kind: String)

  sealed trait Answer
  case object Pick extends Answer
  case object Drop extends Answer
}






