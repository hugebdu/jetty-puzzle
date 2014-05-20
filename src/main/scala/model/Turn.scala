package model



sealed trait Turn
object Turn {
  val all: Set[Turn] = Set(Left, Right)
  case object Left extends Turn
  case object Right extends Turn
}
