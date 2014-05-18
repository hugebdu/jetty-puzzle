package model

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope
import collection.mutable

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/10/14
 */
class BoardTest extends SpecificationWithJUnit {

  trait ctx extends Scope {
    implicit val size = Size(4)

    lazy val board = Board.create()
  }

  "inAndOutOfPlace" should {

    "handle finished board" in new ctx {
      val (in, out) = board.inAndOutOfPlace

      in must haveSize(size.square - 1)
      out must beEmpty
    }

    "handle general case" in new ctx {
      override implicit val size: Size = Size(2)

      val (in, out) = Board(mutable.IndexedSeq(Piece(1), Piece(0), Piece(2), Empty)).inAndOutOfPlace

      in must contain(exactly(Piece(2)))
      out must contain(exactly(Piece(0), Piece(1)))
    }
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
