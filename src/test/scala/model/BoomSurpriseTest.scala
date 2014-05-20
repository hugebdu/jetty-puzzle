package model

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import org.specs2.matcher.ThrownExpectations
import model.Surprise.{Pick, Drop}
import actors.GameActor.CompleteSurprise

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/20/14
 */
class BoomSurpriseTest extends SpecificationWithJUnit with Mockito {

  trait ctx extends Scope with TossingTestSupport with ThrownExpectations {
    val leftBoard = mock[Board]
    val rightBoard = mock[Board]

    tossing.toss(any, any) returns Seq(1 -> 2)

    val surprise = new BoomSurprise(2) with MockedTossing
  }

  "handle" should {

    "do nothing when both dropped" in new ctx {
      surprise.handle(leftBoard -> Drop, rightBoard -> Drop) === (CompleteSurprise(Nil), CompleteSurprise(Nil))
    }

    "explode for picked player's opponent - 1" in new ctx {
      surprise.handle(leftBoard -> Pick, rightBoard -> Drop) === (CompleteSurprise(Nil), CompleteSurprise(Seq(1 -> 2)))
    }

    "explode for picked player's opponent - 1" in new ctx {
      surprise.handle(leftBoard -> Drop, rightBoard -> Pick) === (CompleteSurprise(Seq(1 -> 2)), CompleteSurprise())
    }

    "explode for both when both picked" in new ctx {
      surprise.handle(leftBoard -> Pick, rightBoard -> Pick) === (CompleteSurprise(Seq(1 -> 2)), CompleteSurprise(Seq(1 -> 2)))
    }
  }
}
