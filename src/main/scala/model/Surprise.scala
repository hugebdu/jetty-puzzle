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
  def challenge(): Challenge
  def handle(outcomes: (Answer, Answer)): (CompleteSurprise, CompleteSurprise)
}

object Surprise {
  
  case class Challenge(kind: String)

  sealed trait Answer
  case object Pick extends Answer
  case object Drop extends Answer
}






