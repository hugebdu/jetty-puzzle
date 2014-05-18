package model

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/18/14
 */
class DeterministicTossingTest extends SpecificationWithJUnit {

  trait ctx extends Scope with DeterministicTossing {
    implicit val size = Size(3)
  }

  "toss" should {

    "do nothing on zero" in new ctx {
      val board = Board.create()
      val cells = Seq(board.cells: _*)
      toss(board, 0) must beEmpty
      board.cells must be_===(cells)
    }

    "handle simple case - unshuffled board, 1 piece" in new ctx {
      val board = Board.create()

      toss(board, 1) must be_===(Seq(Position(2) -> Position(6)))

      board.cells(2) must be_===(Piece(6))
      board.cells(6) must be_===(Piece(2))
    }

    "handle simple case - unshuffled board, two pieces" in new ctx {
      val board = Board.create()

      toss(board, 2) must contain(exactly(
        (Position(2), Position(6)),
        (Position(0), Position(8))))

      board.cells(2) must be_===(Piece(6))
      board.cells(6) must be_===(Piece(2))
      board.cells(0) must be_===(Empty)
      board.cells(8) must be_===(Piece(0))
    }
  }
}
