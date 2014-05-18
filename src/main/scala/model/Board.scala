package model

import collection.mutable
import scala.util.Random

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/10/14
 */
case class Board(cells: mutable.IndexedSeq[Cell]) {

  implicit val size = Size(math.sqrt(cells.length.toDouble).toInt)

  def shuffle(): Unit = {
    swap(positionOf(Empty) -> Position(cells.length - 1))
    val shuffled = Random.shuffle(cells.take(cells.length - 1))
    shuffled.zipWithIndex map { _.swap } foreach { (cells.update _).tupled }
  }

  def click(index: Int): Option[(Int, Int)] = {
    click(Position(index))
  }

  def cellsWithPosition: Seq[(Cell, Position)] = {
    cells.zipWithIndex map { case (cell, index) => (cell, Position(index)) }
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

  private def positionOf(c: Cell): Position = {
    Position(cells.indexWhere(_ == c))
  }

  def swap(p: (Position, Position)): Unit = {
    val tmp = cells(p._1.index)
    cells.update(p._1.index, cells(p._2.index))
    cells.update(p._2.index, tmp)
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

  /** Manhattan distance */
  def distanceTo(other: Position): Int = {
    math.abs(row - other.row) + math.abs(column - other.column)
  }

  override def toString: String = s"Position(row=$row,column=$column,index=$index)"
}

object Position {

  def unapply(index: Int)(implicit size: Size): Option[(Int, Int)] = {
    val p = Position(index)
    Some(p.row -> p.column)
  }

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

  def create()(implicit size: Size): Board = {
    val cells = new mutable.ArrayBuffer[Cell](size.square)
    for (i <- 0 until size.square - 1) {
      cells += Piece(i)
    }
    cells += Empty
    Board(cells)
  }
}

trait DistanceMap {
  def distances(p: Position): Seq[Position]
}

object DistanceMap {

  def apply(size: Size): DistanceMap = {

    implicit val s = size

    val matrix = Array.fill(size.square, size.square)(0)
    for (i <- 0 until size.square) {
      for (j <- 0 until size.square) {
        matrix(j)(i) = Position(i).distanceTo(Position(j))
      }
    }

    val byDistanceByPosition: ((Int, Int), (Int, Int)) => Boolean = {
      case ((d1, _), (d2, _)) if d1 != d2 => d1 > d2
      case ((_, Position(r1, _)), (_, Position(r2, _))) if r1 != r2 => r1 < r2
      case ((_, Position(_, c1)), (_, Position(_, c2))) => c1 < c2
    }

    val toPosition: ((Int, Int)) => Position = { case (_, index) => Position(index) }

    val distancesMap = (matrix.zipWithIndex map {
      case (arr, index) =>
        val distances = arr.zipWithIndex sortWith byDistanceByPosition map toPosition
        Position(index) -> distances
    }).toMap

    new DistanceMap {
      def distances(p: Position): Seq[Position] = distancesMap(p)
    }
  }
}


case class Size(value: Int) extends AnyVal {
  def square: Int = value * value
  def last: Int = square - 1
}

sealed trait Cell {
  def place(implicit size: Size): Position
  def distanceFromPlace(p: Position)(implicit size: Size): Int = {
    place.distanceTo(p)
  }
}

case object Empty extends Cell {

  def place(implicit size: Size): Position = {
    Position(size.last)
  }
}

case class Piece(id: Int) extends Cell {

  def place(implicit size: Size): Position = {
    Position(id)
  }
}


