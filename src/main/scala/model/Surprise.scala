package model

import model.Surprise._
import actors.GameActor.CompleteSurprise
import model.Surprise.Challenge
import scala.util.Random

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/16/14
 */

trait SurpriseProducer {
  def maybeSurprise(boards: (Board, Board)): Option[Surprise]
}

object DefaultSurpriseProducer extends SurpriseProducer {

  private val dilemmas = IndexedSeq(
    new DilemmaSurprise(Config(1, 2, (0, 3), 0), "prisoners") with DeterministicTossing,
    new DilemmaSurprise(Config(0, 10, (2, 0), 0), "chicken") with DeterministicTossing,
    new BoomSurprise() with DeterministicTossing
  )

  def maybeSurprise(boards: (Board, Board)): Option[Surprise] = {
    dilemmas filter { _.isEligible(boards) } match {
      case Seq() => None
      case nonEmpty => Some(nonEmpty(Random.nextInt(nonEmpty.length)))
    }
  }
}

trait Surprise {

  def challenge(): Challenge = Challenge(kind)
  def handle(outcomes: ((Board, Option[Answer]), (Board, Option[Answer]))): Option[(CompleteSurprise, CompleteSurprise)]
  def isEligible(boards: (Board, Board)): Boolean

  def kind: String
}

class BoomSurprise(demolish: Int = 4) extends Surprise { this: Tossing =>

  val kind: String = "boom"

  def isEligible(boards: (Board, Board)): Boolean = true

  def handle(outcomes: ((Board, Option[Answer]), (Board, Option[Answer]))): Option[(CompleteSurprise, CompleteSurprise)] = outcomes match {
    case ((_, Some(Drop)), (_, Some(Drop))) => Some(CompleteSurprise(), CompleteSurprise())
    case ((left, Some(Pick)), (right, Some(Pick))) => Some(CompleteSurprise(toss(left, demolish)), CompleteSurprise(toss(right, demolish)))
    case ((_, Some(Pick)), (board, _)) => Some(CompleteSurprise(), CompleteSurprise(toss(board, demolish)))
    case ((board, _), (_, Some(Pick))) => Some(CompleteSurprise(toss(board, demolish)), CompleteSurprise())
  }
}

class DilemmaSurprise(config: Config = Config(), val kind: String) extends Surprise { this: Tossing =>

  def isEligible(boards: (Board, Board)): Boolean = {
    Seq(boards._1.percentCompleted, boards._2.percentCompleted) exists { _ >= config.eligibilityThreshold }
  }

  def handle(outcomes: ((Board, Option[Answer]), (Board, Option[Answer]))): Option[(CompleteSurprise, CompleteSurprise)] = {
    import config._
    outcomes match {
      case ((b1, Some(Pick)), (b2, Some(Pick))) => Some(CompleteSurprise(toss(b1, cooperation)), CompleteSurprise(toss(b2, cooperation)))
      case ((b1, Some(Drop)), (b2, Some(Drop))) => Some(CompleteSurprise(toss(b1, defection)), CompleteSurprise(toss(b2, defection)))
      case ((b1, Some(Pick)), (b2, Some(Drop))) => Some(CompleteSurprise(toss(b1, cooperationVsDefection._1)), CompleteSurprise(toss(b2, cooperationVsDefection._2)))
      case ((b1, Some(Drop)), (b2, Some(Pick))) => Some(CompleteSurprise(toss(b1, cooperationVsDefection._2)), CompleteSurprise(toss(b2, cooperationVsDefection._1)))
      case _ => None
    }
  }
}

private[model] trait Tossing {
  def toss(board: Board, count: Int): Seq[(Int, Int)]
}

private[model] trait DeterministicTossing extends Tossing {

  private val gainFunc: ((Int, (Position, Position))) => Int = _._1

  def toss(board: Board, count: Int): Seq[(Int, Int)] = {

    if (count == 0) Nil else {

      implicit val size = board.size

      val candidates = board.cellsWithPosition.combinations(2).map {
        case Seq((c1, p1), (c2, p2)) =>
          val current = c1.distanceFromPlace(p1) + c2.distanceFromPlace(p2)
          val possible = c1.distanceFromPlace(p2) + c2.distanceFromPlace(p1)
          val gain = possible - current
          gain -> (p1, p2)
      }.toSeq.sortBy(gainFunc).reverse.map {
        case (_, p @ (from, to)) if from.index > to.index => p.swap
        case (_, p) => p
      }

      val (_, swaps) = candidates.foldLeft[(Set[Position], List[(Position, Position)])](Set.empty[Position] -> Nil) {
        case (s @ (_, picked), _) if picked.length == count => s
        case (s @ (set, _), (from, to)) if set.contains(from) || set.contains(to) => s
        case (s @ (set, picked), p @ (from, to)) => set + (from, to) -> (p :: picked)
      }

      swaps foreach board.swap
      swaps map { case (p1, p2) => (p1.index, p2.index) }
    }
  }
}

object Surprise {

  case class Config(cooperation: Int = 1,
                    defection: Int = 2,
                    cooperationVsDefection: (Int, Int) = (0, 3),
                    eligibilityThreshold: Double = 0.2)
  
  case class Challenge(kind: String)

  sealed trait Answer
  case object Pick extends Answer
  case object Drop extends Answer
}






