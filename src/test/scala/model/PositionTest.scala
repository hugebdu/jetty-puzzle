package model

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/18/14
 */
class PositionTest extends SpecificationWithJUnit {

  trait ctx extends Scope {
    implicit val size = Size(4)
  }

  "distanceTo" should {

    "be zero for same position" in new ctx {
      Position(5).distanceTo(Position(5)) must be_===(0)
    }

    "max case" in new ctx {
      Position(15).distanceTo(Position(0)) must be_===(6)
    }

    "neighbours" in new ctx {
      Position(1, 1).distanceTo(Position(0, 1)) must be_===(1)
    }

    "diagonal neighbours" in new ctx {
      Position(1, 1).distanceTo(Position(0, 0)) must be_===(2)
    }
  }
}
