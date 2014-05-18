package model

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope
import org.specs2.matcher.Matcher
import org.specs2.mock.mockito.{MockitoStubs, MocksCreation}

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/18/14
 */
class DefaultSurpriseProducerTest extends SpecificationWithJUnit with MocksCreation with MockitoStubs {

  trait ctx extends Scope {
    val board = mock[Board]

    def aSurpriseOfKind(s: String): Matcher[Surprise] = {
      be_===(s) ^^ { (_: Surprise).kind aka "kind" }
    }
  }

  "maybeSurprise" should {

    "be None when neither board is eligible" in new ctx {
      board.percentCompleted returns 0.0
      DefaultSurpriseProducer.maybeSurprise(board, board) must beNone
    }

    "be Some" in new ctx {
      board.percentCompleted returns 1

      val result = for (_ <- 0 until 10) yield DefaultSurpriseProducer.maybeSurprise(board, board)

      result must beSome[Surprise].forall
      result must contain(beSome[Surprise](aSurpriseOfKind("prisoners")))
      result must contain(beSome[Surprise](aSurpriseOfKind("chicken")))
    }
  }
}
