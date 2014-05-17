package model

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 5/16/14
 */
sealed trait Turn
object Turn {
  val all: Set[Turn] = Set(Left, Right)
  case object Left extends Turn
  case object Right extends Turn
}
