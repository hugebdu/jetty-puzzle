package model

import model.Surprise.Challenge

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
}

object Surprise {
  
  case class Challenge(kind: String)

  sealed trait Answer
  case object Pick extends Answer
  case object Drop extends Answer
}






