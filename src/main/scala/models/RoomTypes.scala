package models

object RoomTypes extends Enumeration {
  type room = Value
  val Single = Value("single")
  val Double = Value("double")
}