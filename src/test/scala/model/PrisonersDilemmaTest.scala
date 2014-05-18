package model

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import PrisonersDilemma._
import model.Surprise.{Pick, Drop}
import actors.GameActor.CompleteSurprise

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/18/14
 */
class PrisonersDilemmaTest extends SpecificationWithJUnit with Mockito {

  trait ctx extends Scope {

    implicit val size = Size(3)

    val leftBoard = mock[Board]
    val rightBoard = mock[Board]

    leftBoard.toString returns "left-board"
    rightBoard.toString returns "right-board"

    val leftTossing = Seq(1 -> 2)
    val rightTossing = Seq(3 -> 4)

    val tossing = mock[Tossing]

    tossing.toss(any, any) answers { (args, _) =>
      args.asInstanceOf[Array[Any]].apply(0) match {
        case `leftBoard` => leftTossing
        case `rightBoard` => rightTossing
      }
    }

    trait MockedTossing extends Tossing {
      def toss(board: Board, count: Int): Seq[(Int, Int)] = {
        tossing.toss(board, count)
      }
    }

    def dilemma(config: Config = Config()) = new PrisonersDilemma(config) with MockedTossing
  }

  "handle" should {

    "handle both defect" in new ctx {
      dilemma(Config(defection = 2)).handle(leftBoard -> Drop, rightBoard -> Drop) must be_===((CompleteSurprise(Seq(1 -> 2)), CompleteSurprise(Seq(3 -> 4))))

      got {
        one(tossing).toss(leftBoard, 2)
        one(tossing).toss(rightBoard, 2)
      }
    }

    "handle both cooperate" in new ctx {
      dilemma(Config(cooperation = 1)).handle(leftBoard -> Pick, rightBoard -> Pick) must be_===((CompleteSurprise(Seq(1 -> 2)), CompleteSurprise(Seq(3 -> 4))))

      got {
        one(tossing).toss(leftBoard, 1)
        one(tossing).toss(rightBoard, 1)
      }
    }

    "handle cooperate/defect" in new ctx {
      dilemma(Config(cooperationVsDefection = (1, 3))).handle(leftBoard -> Pick, rightBoard -> Drop) must be_===((CompleteSurprise(Seq(1 -> 2)), CompleteSurprise(Seq(3 -> 4))))

      got {
        one(tossing).toss(leftBoard, 1)
        one(tossing).toss(rightBoard, 3)
      }
    }
  }

  "isEligible" should {

    "be true when at least one board completion is over the threshold" in new ctx {
      leftBoard.percentCompleted returns 0.25
      rightBoard.percentCompleted returns 0.0
      dilemma(Config(eligibilityThreshold = 0.24)).isEligible(leftBoard, rightBoard) must beTrue
    }

    "be false otherwise" in new ctx {
      leftBoard.percentCompleted returns 0.21
      rightBoard.percentCompleted returns 0.1
      dilemma(Config(eligibilityThreshold = 0.24)).isEligible(leftBoard, rightBoard) must beFalse
    }
  }
}
