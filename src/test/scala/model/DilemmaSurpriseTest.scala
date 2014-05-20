package model

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import model.Surprise.{Config, Pick, Drop}
import actors.GameActor.CompleteSurprise
import org.specs2.mock.mockito.MocksCreation
import org.specs2.matcher.ThrownExpectations

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/18/14
 */
class DilemmaSurpriseTest extends SpecificationWithJUnit with Mockito {

  trait ctx extends Scope with TossingTestSupport with ThrownExpectations {

    implicit val size = Size(3)

    val leftBoard = mock[Board]
    val rightBoard = mock[Board]

    leftBoard.toString returns "left-board"
    rightBoard.toString returns "right-board"

    val leftTossing = Seq(1 -> 2)
    val rightTossing = Seq(3 -> 4)

    tossing.toss(any, any) answers { (args, _) =>
      args.asInstanceOf[Array[Any]].apply(0) match {
        case `leftBoard` => leftTossing
        case `rightBoard` => rightTossing
      }
    }

    def dilemma(config: Config = Config()) = new DilemmaSurprise(config, "kind") with MockedTossing
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

trait TossingTestSupport extends MocksCreation {

  val tossing = mock[Tossing]

  trait MockedTossing extends Tossing {
    def toss(board: Board, count: Int): Seq[(Int, Int)] = {
      tossing.toss(board, count)
    }
  }
}
