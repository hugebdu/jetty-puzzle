package model

import org.specs2.mutable.Specification
import org.specs2.specification.Scope

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/10/14
 */
class BoardSpec extends Specification {

  trait ctx extends Scope {
    implicit val size = Size(4)

    val board = Board.create()
  }

  "percentCompleted" should {

    "be 1 for completed board" in new ctx {
      board.percentCompleted must be_===(1d)
    }

    "be less then one for shuffled board" in new ctx {
      board.shuffle()
      board.percentCompleted must beBetween(0d, 1d).excludingEnd
    }
  }

  "isCompleted" should {

    "be true for newly created board" in new ctx {
      board.isCompleted must beTrue
    }

    "be false for shuffled board" in new ctx {
      board.shuffle()
      board.isCompleted must beFalse
    }
  }

  "click" should {

    "be Some(indexes swapped)" in new ctx {
      board.click(Position(3, 2)) must beSome(Position(3, 2).index -> Position(3, 3).index)
      board.cells(Position(3, 3).index) must be_===(Piece(14))
      board.cells(Position(3, 2).index) must be_===(Empty)
    }

    "be None when move is illegal" in new ctx {
      board.click(Position(0)) must beNone
    }
  }
}
