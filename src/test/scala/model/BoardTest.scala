package model

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope
import org.specs2.matcher.Matcher



class BoardTest extends SpecificationWithJUnit {

  trait ctx extends Scope {

    implicit val size = Size(4)

    lazy val board = Board.create()

    def beAValidPermutation: Matcher[IndexedSeq[Cell]] = {
      (cells: IndexedSeq[Cell]) => Board.isValid(cells)
    }
  }

  "Board.isValid" should {

    "be true for new unshuffled board" in new ctx {
      Board.isValid(board.cells) must beTrue
    }

    "be false for known case" in new ctx {
      board.swap(Position(13), Position(14))

      Board.isValid(board.cells) must beFalse
    }
  }

  "shuffles" should {

    "be empty for completed board" in new ctx {
      board.shuffles must beEmpty
    }

    "be a seq of shuffles" in new ctx {
      board.swap(Position(0, 0) -> Position(0, size.value - 1))
      board.swap(Position(1, 1) -> Position(1, 2))

      board.shuffles must contain(exactly(
        (0, 3),
        (3, 0),
        (5, 6),
        (6, 5)))
    }
  }

  "shuffle()" should {

    "produce a valid shuffle" in new ctx {
      board.shuffle()

      board.cells must beAValidPermutation
    }
  }

  "size" should {

    "be correct" in new ctx {
      board.size === size
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
