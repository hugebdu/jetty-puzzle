package model

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/18/14
 */
class CellsTest extends SpecificationWithJUnit {

  trait ctx extends Scope {
    implicit val size = Size(3)
  }

  "place" should {

    "handle Empty" in new ctx {
      Empty.place === Position(8)
    }

    "handle Piece" in new ctx {
      Piece(2).place === Position(2)
    }
  }
}
