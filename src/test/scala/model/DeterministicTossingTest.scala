package model

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope
import org.specs2.matcher.Matcher

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/18/14
 */
class DeterministicTossingTest extends SpecificationWithJUnit {

  trait ctx extends Scope with DeterministicTossing {
    implicit val size = Size(3)

    def notHaveIndexRepetitions: Matcher[Seq[(Int, Int)]] = {

      val check: Seq[(Int, Int)] => Boolean = s => {
        (s flatMap { case (x, y) => Seq(x, y) }).distinct.length / 2 == s.length
      }

      val message: Seq[(Int, Int)] => String = s => s"$s has index repetitions"
      check -> message
    }
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

      toss(board, 1) must be_===(Seq(2 -> 6))

      board.cells(2) must be_===(Piece(6))
      board.cells(6) must be_===(Piece(2))
    }

    "handle simple case - unshuffled board, two pieces" in new ctx {
      val board = Board.create()

      toss(board, 2) must contain(exactly(
        (2, 6),
        (0, 8)))

      board.cells(2) must be_===(Piece(6))
      board.cells(6) must be_===(Piece(2))
      board.cells(0) must be_===(Empty)
      board.cells(8) must be_===(Piece(0))
    }

    "do not have repetitions in indexes" in new ctx {
      val board = Board.create()(Size(4))

      val shuffles = toss(board, 10)

      shuffles must notHaveIndexRepetitions
    }
  }
}
