package model

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import org.specs2.matcher.ThrownExpectations
import model.Surprise.{Pick, Drop}
import actors.GameActor.CompleteSurprise



class BoomSurpriseTest extends SpecificationWithJUnit with Mockito {

  trait ctx extends Scope with TossingTestSupport with ThrownExpectations {
    val leftBoard = mock[Board]
    val rightBoard = mock[Board]

    tossing.toss(any, any) returns Seq(1 -> 2)

    val surprise = new BoomSurprise(2) with MockedTossing
  }

  "handle" should {

    "be None when only one drop arrived - 1" in new ctx {
      surprise.handle(leftBoard -> Some(Drop), rightBoard -> None) must beNone
    }

    "be None when only one drop arrived - 1" in new ctx {
      surprise.handle(leftBoard -> None, rightBoard -> Some(Drop)) must beNone
    }

    "do nothing when both dropped" in new ctx {
      surprise.handle(leftBoard -> Some(Drop), rightBoard -> Some(Drop)) must beSome(CompleteSurprise(Nil), CompleteSurprise(Nil))
    }

    "do nothing when both dropped" in new ctx {
      surprise.handle(leftBoard -> Some(Drop), rightBoard -> Some(Drop)) must beSome(CompleteSurprise(Nil), CompleteSurprise(Nil))
    }

    "explode for picked player's opponent - 1" in new ctx {
      surprise.handle(leftBoard -> Some(Pick), rightBoard -> Some(Drop)) must beSome(CompleteSurprise(Nil), CompleteSurprise(Seq(1 -> 2)))
    }

    "explode for picked player's opponent - 2" in new ctx {
      surprise.handle(leftBoard -> Some(Drop), rightBoard -> Some(Pick)) must beSome(CompleteSurprise(Seq(1 -> 2)), CompleteSurprise())
    }

    "explode for picked player's opponent - 3" in new ctx {
      surprise.handle(leftBoard -> Some(Pick), rightBoard -> None) must beSome(CompleteSurprise(Nil), CompleteSurprise(Seq(1 -> 2)))
    }

    "explode for picked player's opponent - 4" in new ctx {
      surprise.handle(leftBoard -> None, rightBoard -> Some(Pick)) must beSome(CompleteSurprise(Seq(1 -> 2)), CompleteSurprise())
    }

    "explode for both when both picked" in new ctx {
      surprise.handle(leftBoard -> Some(Pick), rightBoard -> Some(Pick)) must beSome(CompleteSurprise(Seq(1 -> 2)), CompleteSurprise(Seq(1 -> 2)))
    }
  }
}
