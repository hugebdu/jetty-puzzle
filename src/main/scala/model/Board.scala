package model

import collection.mutable
import scala.util.Random

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/10/14
 */
case class Board(cells: mutable.IndexedSeq[Cell])(implicit size: Size) {

  def shuffle(): Unit = {
    swap(indexOf(Empty) -> (cells.length - 1))
    val shuffled = Random.shuffle(cells.take(cells.length - 1))
    shuffled.zipWithIndex map { _.swap } foreach { (cells.update _).tupled }
  }

  def click(index: Int): Option[(Int, Int)] = {
    click(Position(index))
  }

  def click(position: Position): Option[(Int, Int)] = {
    if (cells(position) == Empty) None else {
      val positionOfEmptyOpt = findEmptyAround(position)
      for (p <- positionOfEmptyOpt) yield {
        swap(p, position)
        position.index -> p.index
      }
    }
  }

  def percentCompleted: Double = {
    (cells.zipWithIndex count inPlace).toDouble / cells.length
  }

  def inPlaceCount: Int = {
    cells.zipWithIndex count inPlace
  }

  def isCompleted: Boolean = {
    cells.zipWithIndex forall inPlace
  }

  private val inPlace: ((Cell, Int)) => Boolean = {
    case (Empty, _) => true
    case (Piece(x), y) if x == y => true
    case _ => false
  }

  private def findEmptyAround(p: Position): Option[Position] = {
    p.neighbourhood collectFirst { case pos if cells(pos) == Empty => pos }
  }

  private def cells(p: Position): Cell = cells(p.index)

  private def indexOf(c: Cell): Int = {
    cells.indexWhere(_ == c)
  }

  private def swap(p: (Position, Position)): Unit = {
    swap(p._1.index -> p._2.index)
  }

  private def swap(p: (Int, Int)): Unit = {
    val tmp = cells(p._1)
    cells.update(p._1, cells(p._2))
    cells.update(p._2, tmp)
  }

  def prettyString: String = {
    cells.grouped(size.value) map { line => line mkString "\t" } mkString "\n"
  }
}

case class Position private(index: Int, row: Int, column: Int)(implicit size: Size) {

  def up: Option[Position] = {
    if (row > 0) Some(Position(row - 1, column)) else None
  }

  def down: Option[Position] = {
    if (row < size.value - 1) Some(Position(row + 1, column)) else None
  }

  def left: Option[Position] = {
    if (column > 0) Some(Position(row, column - 1)) else None
  }

  def right: Option[Position] = {
    if (column < size.value - 1) Some(Position(row, column + 1)) else None
  }

  def neighbourhood: Seq[Position] = Seq(left, right, up, down) collect { case Some(p) => p }
}

object Position {

  def apply(index: Int)(implicit size: Size): Position = {
    if (index < 0 || index >= size.square) throw new IndexOutOfBoundsException(s"Invalid index $index")
    val row = math.floor(index.toDouble / size.value).toInt
    val column = index % size.value
    Position(index, row, column)
  }

  def apply(row: Int, column: Int)(implicit size: Size): Position = Position(row -> column)

  def apply(p: (Int, Int))(implicit size: Size): Position = {
    val (row, column) = p
    if (row < 0 || row >= size.value) throw new IndexOutOfBoundsException(s"Invalid row $column")
    if (column < 0 || column >= size.value) throw new IndexOutOfBoundsException(s"Invalid column $column")
    Position(row * size.value + column, row, column)
  }
}

object Board {

//  val Size = 4

  def create()(implicit size: Size): Board = {
    val cells = new mutable.ArrayBuffer[Cell](size.square)
    for (i <- 0 until size.square - 1) {
      cells += Piece(i)
    }
    cells += Empty
    Board(cells)
  }
}

case class Size(value: Int) extends AnyVal {
  def square: Int = value * value
}

sealed trait Cell
case object Empty extends Cell
case class Piece(id: Int) extends Cell


