package model

import model.Surprise.{Drop, Pick, Answer, Challenge}
import actors.GameActor.CompleteSurprise

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
  def handle(outcomes: ((Board, Answer), (Board, Answer))): (CompleteSurprise, CompleteSurprise)
  def isEligible(boards: (Board, Board)): Boolean

  protected def kind: String
}

class PrisonersDilemma(config: PrisonersDilemma.Config = PrisonersDilemma.Config()) extends Surprise { this: Tossing =>

  def isEligible(boards: (Board, Board)): Boolean = {
    Seq(boards._1.percentCompleted, boards._2.percentCompleted) exists { _ >= config.eligibilityThreshold }
  }

  def handle(outcomes: ((Board, Answer), (Board, Answer))): (CompleteSurprise, CompleteSurprise) = {
    import config._
    outcomes match {
      case ((b1, Pick), (b2, Pick)) => (CompleteSurprise(toss(b1, cooperation)), CompleteSurprise(toss(b2, cooperation)))
      case ((b1, Drop), (b2, Drop)) => (CompleteSurprise(toss(b1, defection)), CompleteSurprise(toss(b2, defection)))
      case ((b1, Pick), (b2, Drop)) => (CompleteSurprise(toss(b1, cooperationVsDefection._1)), CompleteSurprise(toss(b2, cooperationVsDefection._2)))
      case ((b1, Drop), (b2, Pick)) => (CompleteSurprise(toss(b1, cooperationVsDefection._2)), CompleteSurprise(toss(b2, cooperationVsDefection._1)))
    }
  }

  protected val kind = "prisoners"
}

object PrisonersDilemma {
  case class Config(cooperation: Int = 1,
                    defection: Int = 2,
                    cooperationVsDefection: (Int, Int) = (0, 3),
                    eligibilityThreshold: Double = 0.2)
}

private[model] trait Tossing {
  def toss(board: Board, count: Int): Seq[(Int, Int)]
}

private[model] trait DeterministicTossing extends Tossing {

  private val gainFunc: ((Int, (Position, Position))) => Int = _._1

  def toss(board: Board, count: Int): Seq[(Int, Int)] = {

    if (count == 0) Nil else {

      implicit val size = board.size

      val swaps = board.cellsWithPosition.combinations(2).map {
        case Seq((c1, p1), (c2, p2)) =>
          val current = c1.distanceFromPlace(p1) + c2.distanceFromPlace(p2)
          val possible = c1.distanceFromPlace(p2) + c2.distanceFromPlace(p1)
          val gain = possible - current
          gain -> (p1, p2)
      }.toSeq.sortBy(gainFunc).takeRight(count) map { case (_, p) => p }

      swaps foreach board.swap
      swaps map { case (p1, p2) => (p1.index, p2.index) }
    }
  }
}

object Surprise {
  
  case class Challenge(kind: String)

  sealed trait Answer
  case object Pick extends Answer
  case object Drop extends Answer
}






